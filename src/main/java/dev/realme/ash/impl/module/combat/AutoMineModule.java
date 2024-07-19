package dev.realme.ash.impl.module.combat;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.RotationModule;
import dev.realme.ash.impl.event.network.PlayerTickEvent;
import dev.realme.ash.init.Managers;
import dev.realme.ash.init.Modules;
import dev.realme.ash.util.player.PlayerUtil;
import dev.realme.ash.util.world.BlockPosX;
import dev.realme.ash.util.world.BlockUtil;
import dev.realme.ash.util.world.EntityUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.PickaxeItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AutoMineModule
extends RotationModule {
    final Config<Boolean> onlyGround = new BooleanConfig("OnlyGround", "", false);
    final Config<Boolean> burrow = new BooleanConfig("Burrow", "", true);
    final Config<Boolean> surround = new BooleanConfig("Surround", "", true);
    final Config<Boolean> ground = new BooleanConfig("Ground", "", false);
    final Config<Float> minDamage = new NumberConfig<>("MinDamage", "", 0.0f, 8.0f, 36.0f);
    final Config<Boolean> forceDouble = new BooleanConfig("ForceDouble", "", false);
    final Config<Boolean> selfCheck = new BooleanConfig("SelfCheck", "", false);
    final Config<Boolean> holdPickaxe = new BooleanConfig("HoldPickaxe", "", true);
    final Config<Float> targetRange = new NumberConfig<>("EnemyRange", "", 0.0f, 5.0f, 8.0f);
    PlayerEntity target;
    boolean diggingMine = true;

    public AutoMineModule() {
        super("AutoMine", "Automatically mines enemy blocks", ModuleCategory.COMBAT);
    }

    @Override
    public String getModuleData() {
        return this.target == null ? null : this.target.getName().getString();
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        this.target = null;
        this.diggingMine = false;
    }

    @EventListener
    public void onPlayerTick(PlayerTickEvent event) {
        if (AutoMineModule.nullCheck()) {
            return;
        }
        assert AutoMineModule.mc.player != null;
        if (!AutoMineModule.mc.player.isOnGround() && this.onlyGround.getValue()) {
            return;
        }
        this.target = EntityUtil.getTarget(this.targetRange.getValue());
        if (this.target == null) {
            return;
        }
        this.doBreak(this.target);
    }

    private void doBreak(PlayerEntity player) {
        BlockPosX offsetPos;
        double[] xzOffset;
        double[] yOffset;
        BlockPos breakPos = Modules.PACKET_DIGGING.breakPos;
        BlockPos secondPos = Modules.PACKET_DIGGING.secondPos;
        BlockPos pos = PlayerUtil.playerPos(player);
        if (breakPos != null && !breakPos.equals(secondPos) && secondPos != null && !BlockUtil.isAir(secondPos) && this.forceDouble.getValue()) {
            return;
        }
        if (this.ground.getValue()) {
            assert AutoMineModule.mc.player != null;
            if (this.target.getY() - AutoMineModule.mc.player.getY() > 0.0) {
                yOffset = new double[]{-1.0};
                xzOffset = new double[]{0.3, -0.3, 0.0};
                for (double y : yOffset) {
                    for (double x : xzOffset) {
                        for (double z : xzOffset) {
                            BlockPosX offsetPos2 = new BlockPosX(player.getX() + x, player.getY() + y, player.getZ() + z);
                            if (!this.isObsidian(offsetPos2) || !offsetPos2.equals(breakPos)) continue;
                            return;
                        }
                    }
                }
                this.diggingMine = false;
                for (double y : yOffset) {
                    for (double offset : xzOffset) {
                        BlockPosX blockPosX = new BlockPosX(player.getX() + offset, player.getY() + y, player.getZ() + offset);
                        if (!this.isObsidian(blockPosX)) continue;
                        this.mine(blockPosX);
                        return;
                    }
                }
                for (double y : yOffset) {
                    for (double offset : xzOffset) {
                        for (double offset2 : xzOffset) {
                            offsetPos = new BlockPosX(player.getX() + offset2, player.getY() + y, player.getZ() + offset);
                            if (!this.isObsidian(offsetPos)) continue;
                            this.mine(offsetPos);
                            return;
                        }
                    }
                }
                this.diggingMine = true;
            }
        }
        if (this.burrow.getValue()) {
            yOffset = new double[]{-0.8, 0.5, 1.1};
            xzOffset = new double[]{0.3, -0.3, 0.0};
            for (double y : yOffset) {
                for (double x : xzOffset) {
                    for (double z : xzOffset) {
                        offsetPos = new BlockPosX(player.getX() + x, player.getY() + y, player.getZ() + z);
                        if (!this.isObsidian(offsetPos) || !offsetPos.equals(breakPos)) continue;
                        return;
                    }
                }
            }
            for (double y : yOffset = new double[]{0.5, 1.1}) {
                for (double offset : xzOffset) {
                    BlockPosX blockPosX = new BlockPosX(player.getX() + offset, player.getY() + y, player.getZ() + offset);
                    if (!this.isObsidian(blockPosX)) continue;
                    this.mine(blockPosX);
                    return;
                }
            }
            for (double y : yOffset) {
                for (double offset : xzOffset) {
                    for (double offset2 : xzOffset) {
                        offsetPos = new BlockPosX(player.getX() + offset2, player.getY() + y, player.getZ() + offset);
                        if (!this.isObsidian(offsetPos)) continue;
                        this.mine(offsetPos);
                        return;
                    }
                }
            }
        }
        if (this.surround.getValue()) {
            if (Modules.AUTO_CRYSTAL.isEnabled() && Modules.AUTO_CRYSTAL.placePos != null && Modules.AUTO_CRYSTAL.target.equals(player) && Modules.AUTO_CRYSTAL.lastDamage >= this.minDamage.getValue()) {
                return;
            }
            for (Direction i : Direction.values()) {
                if (i == Direction.UP || i == Direction.DOWN || this.isMining(breakPos, pos) && BlockUtil.isCrystalBase(pos.offset(i).down()) && BlockUtil.isWithin(pos.offset(i), 5.0) || !this.isObsidianSur(pos.offset(i))) continue;
                this.mine(pos.offset(i));
                return;
            }
        }
    }

    private boolean isMining(BlockPos breakPos, BlockPos targetPos) {
        boolean supported = false;
        for (Direction enumFacing : Direction.values()) {
            if (breakPos == null || !breakPos.equals(targetPos.offset(enumFacing))) continue;
            supported = true;
        }
        return supported;
    }

    private void mine(BlockPos pos) {
        Modules.PACKET_DIGGING.mine(pos);
    }

    private boolean isSelfBlock(BlockPos pos) {
        if (!this.selfCheck.getValue()) {
            return false;
        }
        assert (AutoMineModule.mc.player != null);
        if (pos.equals(new BlockPosX(AutoMineModule.mc.player.getX() + 0.3, AutoMineModule.mc.player.getY(), AutoMineModule.mc.player.getZ() + 0.3))) {
            return true;
        }
        if (pos.equals(new BlockPosX(AutoMineModule.mc.player.getX() - 0.3, AutoMineModule.mc.player.getY(), AutoMineModule.mc.player.getZ() - 0.3))) {
            return true;
        }
        if (pos.equals(new BlockPosX(AutoMineModule.mc.player.getX() - 0.3, AutoMineModule.mc.player.getY(), AutoMineModule.mc.player.getZ() + 0.3))) {
            return true;
        }
        return pos.equals(new BlockPosX(AutoMineModule.mc.player.getX() + 0.3, AutoMineModule.mc.player.getY(), AutoMineModule.mc.player.getZ() - 0.3));
    }

    private boolean isObsidian(BlockPos pos) {
        if (this.isSelfBlock(pos) || !BlockUtil.mineBlocks.contains(BlockUtil.getBlock(pos)) || Managers.INTERACT.getClickDirection(pos) == null)
            return false;
        if (!pos.equals(Modules.PACKET_DIGGING.secondPos)) return true;
        assert AutoMineModule.mc.player != null;
        return (!(AutoMineModule.mc.player.getMainHandStack().getItem() instanceof PickaxeItem) && this.holdPickaxe.getValue()) && !Managers.BREAK.isFriendMining(pos);
    }

    private boolean isObsidianSur(BlockPos pos) {
        return !this.isSelfBlock(pos) && BlockUtil.mineBlocks.contains(BlockUtil.getBlock(pos)) && Managers.INTERACT.getClickDirection(pos) != null;
    }
}
