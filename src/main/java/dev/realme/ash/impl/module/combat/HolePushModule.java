// Decompiled with: FernFlower
// Class Version: 17
package dev.realme.ash.impl.module.combat;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.EnumConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.RotationModule;
import dev.realme.ash.impl.event.network.UpdateWalkingEvent;
import dev.realme.ash.impl.module.combat.HolePushModule;
import dev.realme.ash.init.Managers;
import dev.realme.ash.init.Modules;
import dev.realme.ash.util.chat.ChatUtil;
import dev.realme.ash.util.math.timer.CacheTimer;
import dev.realme.ash.util.math.timer.Timer;
import dev.realme.ash.util.player.InventoryUtil;
import dev.realme.ash.util.player.PlayerUtil;
import dev.realme.ash.util.world.BlockUtil;
import dev.realme.ash.util.world.EntityUtil;
import net.minecraft.block.Blocks;
import net.minecraft.block.PistonBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.LookAndOnGround;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class HolePushModule extends RotationModule {
    Config<Float> targetRange = new NumberConfig("TargetRange", "", 0.0F, 5.2F, 6.0F);
    Config<Float> updateDelay = new NumberConfig("UpdateDelay", "Added delays", 0.0F, 50.0F, 500.0F);
    Config<Boolean> pauseEat = new BooleanConfig("PauseEat", "", true);
    Config<Boolean> mine = new BooleanConfig("MineRedStone", "", false);
    Config<HolePushModule.SwapMode> swapMode = new EnumConfig("SwapMode", "", HolePushModule.SwapMode.SILENT, HolePushModule.SwapMode.values());
    Config<Boolean> pistonPacket = new BooleanConfig("PistonPacket", "", false);
    Config<Boolean> redStonePacket = new BooleanConfig("RedStonePacket", "", false);
    Config<Boolean> pistonRotate = new BooleanConfig("PistonRotate", "", true);
    Config<Boolean> redStoneRotate = new BooleanConfig("RedStoneRotate", "", false);
    int redBlock;
    int redTorch;
    int pistonBlock;
    PlayerEntity target;
    private final Timer updateTimer = new CacheTimer();
    BlockPos pistonPos = null;
    BlockPos redStonePos = null;
    int yaw = -1;

    public HolePushModule() {
        super("HolePush", "Push your lover.", ModuleCategory.COMBAT);
    }

    public String getModuleData() {
        return this.target == null ? null : this.target.getName().getString();
    }

    public void onDisable() {
        this.updateTimer.reset();
        this.target = null;
        this.redBlock = -1;
        this.redTorch = -1;
        this.pistonBlock = -1;
        this.redStonePos = null;
        this.pistonPos = null;
        this.yaw = -1;
    }

    @EventListener
    public void onUpdateWalking(UpdateWalkingEvent event) {
        if (mc.player != null && mc.world != null) {
            if (this.updateTimer.passed((Number)this.updateDelay.getValue())) {
                this.updateTimer.reset();
                switch((HolePushModule.SwapMode)this.swapMode.getValue()) {
                    case SILENT:
                    case NORMAL:
                        this.redBlock = InventoryUtil.findItem(Items.REDSTONE_BLOCK);
                        this.redTorch = InventoryUtil.findItem(Items.REDSTONE_TORCH);
                        this.pistonBlock = InventoryUtil.findClass(PistonBlock.class);
                        break;
                    case Inventory:
                        this.redBlock = InventoryUtil.findInventorySlot(Items.REDSTONE_BLOCK, false);
                        this.redTorch = InventoryUtil.findInventorySlot(Items.REDSTONE_TORCH, false);
                        this.pistonBlock = InventoryUtil.findClassInventorySlot(PistonBlock.class, false);
                        break;
                    case Pick:
                        this.redBlock = InventoryUtil.findInventorySlot(Items.REDSTONE_BLOCK, true);
                        this.redTorch = InventoryUtil.findInventorySlot(Items.REDSTONE_TORCH, true);
                        this.pistonBlock = InventoryUtil.findClassInventorySlot(PistonBlock.class, true);
                }

                if ((this.redBlock != -1 || this.redTorch != -1) && this.pistonBlock != -1) {
                    this.update();
                    if (this.redStonePos == null && this.pistonPos == null) {
                        this.disable();
                    } else if (this.target == null) {
                        this.disable();
                    } else if (!this.pauseEat.getValue() || !mc.player.isUsingItem()) {
                        if (this.redStonePos != null) {
                            this.placeRedBlock();
                        }

                        if (this.pistonPos != null && this.yaw != -1) {
                            this.placePiston();
                        }

                        if (this.checkPiston() && this.checkRedStone() && (this.isPiston(this.pistonPos) && this.pistonPos != null || this.redStonePos != null && this.getPiston(this.redStonePos) != null && this.isRedStone(this.redStonePos) && this.redStonePos != null || this.pistonPos != null && this.getRedStone(this.pistonPos) != null)) {
                            if (this.mine.getValue()) {
                                BlockPos minePos = this.redStonePos != null ? this.redStonePos : (this.pistonPos != null ? this.getRedStone(this.pistonPos) : null);
                                if (minePos != null) {
                                    Modules.PACKET_DIGGING.mine(minePos);
                                }
                            }

                            this.disable();
                        }

                    }
                } else {
                    ChatUtil.sendChatMessageWidthId("No Item.", this.hashCode() + 777);
                    this.disable();
                }
            }
        }
    }

    private boolean checkPiston() {
        return this.pistonPos != null || this.redStonePos != null && this.getPiston(this.redStonePos) != null;
    }

    private boolean checkRedStone() {
        return this.redStonePos != null || this.pistonPos != null && this.getRedStone(this.pistonPos) != null;
    }

    private void update() {
        this.target = EntityUtil.getTarget((double)((Float)this.targetRange.getValue()).floatValue());
        if (this.target == null) {
            this.disable();
        } else {
            BlockPos feet = PlayerUtil.playerPos(this.target);
            BlockPos head = feet.up();
            int feetYaw = 0;
            int headYaw = 0;
            if (this.pistonPos == null) {
                for(Direction pistonHeadSide : Direction.values()) {
                    if (pistonHeadSide != Direction.UP && pistonHeadSide != Direction.DOWN) {
                        ++headYaw;
                        if ((BlockUtil.canReplace(head.offset(pistonHeadSide)) || BlockUtil.getBlock(head.offset(pistonHeadSide)) instanceof PistonBlock) && (!(BlockUtil.getBlock(head.offset(pistonHeadSide)) instanceof PistonBlock) || ((Direction)BlockUtil.getState(head.offset(pistonHeadSide)).get(Properties.FACING)).equals(pistonHeadSide.getOpposite())) && this.canPlaceRedStone(head.offset(pistonHeadSide)) && this.canPush(head) && !EntityUtil.hasEntity(head.offset(pistonHeadSide)) && !BlockUtil.blockMovement(head.offset(pistonHeadSide.getOpposite())) && !BlockUtil.blockMovement(head.offset(pistonHeadSide.getOpposite()).up()) && Managers.INTERACT.getPlaceDirection(head.offset(pistonHeadSide)) != null) {
                            this.pistonPos = head.offset(pistonHeadSide);
                            this.yaw = headYaw;
                        }
                    }
                }
            }

            if (this.pistonPos == null) {
                for(Direction pistonFeetSide : Direction.values()) {
                    if (pistonFeetSide != Direction.UP && pistonFeetSide != Direction.DOWN) {
                        ++feetYaw;
                        if ((BlockUtil.canReplace(feet.offset(pistonFeetSide)) || BlockUtil.getBlock(feet.offset(pistonFeetSide)) instanceof PistonBlock) && (!(BlockUtil.getBlock(feet.offset(pistonFeetSide)) instanceof PistonBlock) || ((Direction)BlockUtil.getState(feet.offset(pistonFeetSide)).get(Properties.FACING)).equals(pistonFeetSide.getOpposite())) && this.canPlaceRedStone(feet.offset(pistonFeetSide)) && this.canPush(feet) && !EntityUtil.hasEntity(feet.offset(pistonFeetSide)) && !BlockUtil.blockMovement(head.offset(pistonFeetSide.getOpposite())) && Managers.INTERACT.getPlaceDirection(feet.offset(pistonFeetSide)) != null) {
                            this.pistonPos = feet.offset(pistonFeetSide);
                            this.yaw = feetYaw;
                        }
                    }
                }
            }

            if (this.pistonPos != null && this.redStonePos == null) {
                for(Direction redStoneSide : Direction.values()) {
                    if (this.getRedStone(this.pistonPos) == null && !EntityUtil.hasEntity(this.pistonPos.offset(redStoneSide)) && BlockUtil.canReplace(this.pistonPos.offset(redStoneSide)) && Managers.INTERACT.getPlaceDirection(this.pistonPos.offset(redStoneSide)) != null && (this.redBlock != -1 || !(BlockUtil.getBlock(this.pistonPos.offset(redStoneSide).offset(redStoneSide.getOpposite())) instanceof PistonBlock))) {
                        this.redStonePos = this.pistonPos.offset(redStoneSide);
                    }
                }
            }

            if (this.redStonePos == null && this.pistonPos == null) {
                for(Direction redStoneFeetSide : Direction.values()) {
                    if (redStoneFeetSide != Direction.UP && redStoneFeetSide != Direction.DOWN && Managers.INTERACT.getPlaceDirection(feet.offset(redStoneFeetSide)) != null && this.canPush(head) && BlockUtil.canReplace(head.offset(redStoneFeetSide)) && !EntityUtil.hasEntity(feet.offset(redStoneFeetSide)) && !EntityUtil.hasEntity(head.offset(redStoneFeetSide)) && BlockUtil.canReplace(feet.offset(redStoneFeetSide)) && !BlockUtil.blockMovement(head.offset(redStoneFeetSide.getOpposite())) && !BlockUtil.blockMovement(head.offset(redStoneFeetSide.getOpposite()).up())) {
                        this.redStonePos = feet.offset(redStoneFeetSide);
                    }
                }
            }

        }
    }

    private boolean canPush(BlockPos pos) {
        return BlockUtil.canReplace(pos) || BlockUtil.getBlock(pos) == Blocks.COBWEB;
    }

    private void placePiston() {
        if (BlockUtil.canPlace(this.pistonPos)) {
            EntityUtil.attackCrystal(this.pistonPos);
            if (this.pistonPos.equals(Modules.PACKET_DIGGING.breakPos)) {
                Modules.PACKET_DIGGING.breakPos = null;
                Modules.PACKET_DIGGING.startMine = false;
                Modules.PACKET_DIGGING.breakNumber = 0;
            }

            if (!BlockUtil.isMining(this.pistonPos)) {
                int oldSlot = mc.player.getInventory().selectedSlot;
                switch((HolePushModule.SwapMode)this.swapMode.getValue()) {
                    case SILENT:
                    case NORMAL:
                        InventoryUtil.doSwap(this.pistonBlock);
                        break;
                    case Inventory:
                        InventoryUtil.doInvSwap(this.pistonBlock);
                        break;
                    case Pick:
                        InventoryUtil.doPickSwap(this.pistonBlock);
                }

                this.setRotation(new LookAndOnGround((float)this.getYaw(this.yaw), 5.0F, Managers.POSITION.isOnGround()));
                if (this.pistonRotate.getValue()) {
                    this.setRotation(this.pistonPos.toCenterPos());
                }

                Managers.INTERACT.placeBlock(this.pistonPos, Hand.MAIN_HAND, false, false, this.pistonPacket.getValue());
                switch((HolePushModule.SwapMode)this.swapMode.getValue()) {
                    case SILENT:
                        InventoryUtil.doSwap(oldSlot);
                    case NORMAL:
                    default:
                        break;
                    case Inventory:
                        InventoryUtil.doInvSwap(this.pistonBlock);
                        break;
                    case Pick:
                        InventoryUtil.doPickSwap(this.pistonBlock);
                }

            }
        }
    }

    private void placeRedBlock() {
        if (BlockUtil.canPlace(this.redStonePos)) {
            EntityUtil.attackCrystal(this.redStonePos);
            if (this.redStonePos.equals(Modules.PACKET_DIGGING.breakPos)) {
                Modules.PACKET_DIGGING.breakPos = null;
                Modules.PACKET_DIGGING.startMine = false;
                Modules.PACKET_DIGGING.breakNumber = 0;
            }

            int tempSlot = this.redBlock != -1 ? this.redBlock : this.redTorch;
            int oldSlot = mc.player.getInventory().selectedSlot;
            switch((HolePushModule.SwapMode)this.swapMode.getValue()) {
                case SILENT:
                case NORMAL:
                    InventoryUtil.doSwap(tempSlot);
                    break;
                case Inventory:
                    InventoryUtil.doInvSwap(tempSlot);
                    break;
                case Pick:
                    InventoryUtil.doPickSwap(tempSlot);
            }

            Managers.INTERACT.placeBlock(this.redStonePos, Hand.MAIN_HAND, false, false, this.redStonePacket.getValue());
            if (this.redStoneRotate.getValue()) {
                this.setRotation(this.redStonePos);
            }

            switch((HolePushModule.SwapMode)this.swapMode.getValue()) {
                case SILENT:
                    InventoryUtil.doSwap(oldSlot);
                case NORMAL:
                default:
                    break;
                case Inventory:
                    InventoryUtil.doInvSwap(tempSlot);
                    break;
                case Pick:
                    InventoryUtil.doPickSwap(tempSlot);
            }

        }
    }

    private BlockPos getRedStone(BlockPos pos) {
        BlockPos supported = null;

        for(Direction enumFacing : Direction.values()) {
            if (this.isRedStone(pos.offset(enumFacing))) {
                supported = pos.offset(enumFacing);
            }
        }

        return supported;
    }

    private BlockPos getPiston(BlockPos pos) {
        BlockPos supported = null;

        for(Direction enumFacing : Direction.values()) {
            if (this.isPiston(pos.offset(enumFacing))) {
                supported = pos.offset(enumFacing);
            }
        }

        return supported;
    }

    private boolean canPlaceRedStone(BlockPos pos) {
        boolean bl = false;

        for(Direction enumFacing : Direction.values()) {
            if (BlockUtil.canReplace(pos.offset(enumFacing)) && !EntityUtil.hasEntity(pos.offset(enumFacing))) {
                bl = true;
            }
        }

        return bl;
    }

    private int getYaw(int i) {
        if (i == 1) {
            return 180;
        } else if (i == 2) {
            return 0;
        } else {
            return i == 3 ? 90 : 270;
        }
    }

    private boolean isPiston(BlockPos block) {
        return BlockUtil.getBlock(block) instanceof PistonBlock;
    }

    private boolean isRedStone(BlockPos block) {
        return BlockUtil.getBlock(block) == Blocks.REDSTONE_BLOCK || BlockUtil.getBlock(block) == Blocks.REDSTONE_TORCH;
    }

    public enum SwapMode {
        OFF,
        NORMAL,
        SILENT,
        Inventory,
        Pick;
    }
}
 