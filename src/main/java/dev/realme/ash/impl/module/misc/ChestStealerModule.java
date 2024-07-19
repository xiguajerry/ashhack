// Decompiled with: FernFlower
// Class Version: 17
package dev.realme.ash.impl.module.misc;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.network.UpdateWalkingEvent;
import dev.realme.ash.init.Managers;
import dev.realme.ash.init.Modules;
import dev.realme.ash.util.chat.ChatUtil;
import dev.realme.ash.util.math.timer.CacheTimer;
import dev.realme.ash.util.math.timer.Timer;
import dev.realme.ash.util.player.InventoryUtil;
import dev.realme.ash.util.world.BlockUtil;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

public class ChestStealerModule extends ToggleModule {
    Config<Float> disableTime = new NumberConfig("DisableTime", "Added delays", 0.0F, 60.0F, 500.0F);
    Config<Boolean> smart = new BooleanConfig("Smart", "", true);
    Config<Boolean> place = new BooleanConfig("Place", "", true);
    Config<Boolean> rotate = new BooleanConfig("Rotate", "", false);
    Config<Float> range = new NumberConfig("Range", "", 0.0F, 5.0F, 8.0F);
    Config<Boolean> preferOpen = new BooleanConfig("PreferOpen", "", true);
    Config<Float> minRange = new NumberConfig("MinRange", "", 0.0F, 1.0F, 3.0F);
    Config<Boolean> mine = new BooleanConfig("Mine", "", true);
    Config<Boolean> open = new BooleanConfig("Open", "", true);
    Config<Boolean> take = new BooleanConfig("Take", "", true);
    Config<Boolean> autoDisable = new BooleanConfig("AutoDisable", "", true);
    Config<Integer> crystal = new NumberConfig("Crystal", "", 0, 256, 512);
    Config<Integer> exp = new NumberConfig("Exp", "", 0, 256, 512);
    Config<Integer> totem = new NumberConfig("Totem", "", 0, 7, 36);
    Config<Integer> gapple = new NumberConfig("GApple", "", 0, 256, 512);
    Config<Integer> endChest = new NumberConfig("EndChest", "", 0, 64, 512);
    Config<Integer> web = new NumberConfig("Web", "", 0, 256, 512);
    Config<Integer> glowstone = new NumberConfig("GlowStone", "", 0, 256, 512);
    Config<Integer> anchor = new NumberConfig("Anchor", "", 0, 256, 512);
    Config<Integer> pearl = new NumberConfig("Pearl", "", 0, 16, 512);
    Config<Integer> turtle = new NumberConfig("Turtle", "", 0, 6, 36);
    final int[] stealCountList = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private final Timer timer = new CacheTimer();
    BlockPos placePos = null;
    private final Timer disableTimer = new CacheTimer();
    boolean opend = false;

    public ChestStealerModule() {
        super("ChestStealer", "yz.exe", ModuleCategory.MISCELLANEOUS);
    }

    public int findShulker() {
        AtomicInteger atomicInteger = new AtomicInteger(-1);
        if (InventoryUtil.findClass(ShulkerBoxBlock.class) != -1) {
            atomicInteger.set(InventoryUtil.findClass(ShulkerBoxBlock.class));
        }

        return atomicInteger.get();
    }

    public void onEnable() {
        if (!nullCheck()) {
            this.disableTimer.reset();
            this.placePos = null;
            int oldSlot = mc.player.getInventory().selectedSlot;
            if (this.place.getValue()) {
                double distance = 100.0D;
                BlockPos bestPos = null;

                for(BlockPos pos : BlockUtil.getSphere(this.range.getValue())) {
                    if (BlockUtil.isAir(pos.up())) {
                        if (this.preferOpen.getValue() && mc.world.getBlockState(pos).getBlock() instanceof ShulkerBoxBlock) {
                            return;
                        }

                        if (!(MathHelper.sqrt((float)mc.player.squaredDistanceTo(pos.toCenterPos())) < this.minRange.getValue()) && BlockUtil.clientCanPlace(pos) && BlockUtil.isStrictDirection(pos.offset(Direction.DOWN), Direction.UP) && BlockUtil.canClick(pos.offset(Direction.DOWN)) && (bestPos == null || (double)MathHelper.sqrt((float)mc.player.squaredDistanceTo(pos.toCenterPos())) < distance)) {
                            distance = (double)MathHelper.sqrt((float)mc.player.squaredDistanceTo(pos.toCenterPos()));
                            bestPos = pos;
                        }
                    }
                }

                if (bestPos != null) {
                    if (this.findShulker() == -1) {
                        ChatUtil.sendChatMessageWidthId("No shulkerbox.", this.hashCode() + 1337);
                        return;
                    }

                    InventoryUtil.doSwap(this.findShulker());
                    this.placeBlock(bestPos);
                    this.placePos = bestPos;
                    InventoryUtil.doSwap(oldSlot);
                    this.timer.reset();
                } else {
                    ChatUtil.sendChatMessageWidthId("No place.", this.hashCode() + 1337);
                }

            }
        }
    }

    private void update() {
        this.stealCountList[0] = this.crystal.getValue() - getItemCount(Items.END_CRYSTAL);
        this.stealCountList[1] = this.exp.getValue() - getItemCount(Items.EXPERIENCE_BOTTLE);
        this.stealCountList[2] = this.totem.getValue() - getItemCount(Items.TOTEM_OF_UNDYING);
        this.stealCountList[3] = this.gapple.getValue() - getItemCount(Items.ENCHANTED_GOLDEN_APPLE);
        this.stealCountList[4] = this.endChest.getValue() - getItemCount(Item.fromBlock(Blocks.ENDER_CHEST));
        this.stealCountList[5] = this.web.getValue() - getItemCount(Item.fromBlock(Blocks.COBWEB));
        this.stealCountList[6] = this.glowstone.getValue() - getItemCount(Item.fromBlock(Blocks.GLOWSTONE));
        this.stealCountList[7] = this.anchor.getValue() - getItemCount(Item.fromBlock(Blocks.RESPAWN_ANCHOR));
        this.stealCountList[8] = this.pearl.getValue() - getItemCount(Items.ENDER_PEARL);
        this.stealCountList[9] = this.turtle.getValue() - getItemCount(Items.SPLASH_POTION);
    }

    public void onDisable() {
        this.opend = false;
        if (this.mine.getValue() && this.placePos != null) {
            Modules.PACKET_DIGGING.mine(this.placePos);
        }

    }

    @EventListener
    public void onPlayerUpdate(UpdateWalkingEvent event) {
        if (this.smart.getValue()) {
            this.update();
        }

        if (!(mc.currentScreen instanceof ShulkerBoxScreen)) {
            if (this.opend) {
                this.opend = false;
                if (this.autoDisable.getValue()) {
                    this.disable2();
                }

            } else {
                if (this.open.getValue()) {
                    if (this.placePos == null || !(MathHelper.sqrt((float)mc.player.squaredDistanceTo(this.placePos.toCenterPos())) <= this.range.getValue()) || !mc.world.isAir(this.placePos.up()) || this.timer.passed(500) && !(mc.world.getBlockState(this.placePos).getBlock() instanceof ShulkerBoxBlock)) {
                        boolean found = false;

                        for(BlockPos pos : BlockUtil.getSphere(this.range.getValue())) {
                            if (BlockUtil.isAir(pos.up()) && mc.world.getBlockState(pos).getBlock() instanceof ShulkerBoxBlock) {
                                Managers.INTERACT.clickBlock(pos, Managers.INTERACT.getClickDirection(pos), this.rotate.getValue(), true);
                                found = true;
                                break;
                            }
                        }

                        if (!found && this.autoDisable.getValue()) {
                            this.disable2();
                        }
                    } else if (mc.world.getBlockState(this.placePos).getBlock() instanceof ShulkerBoxBlock) {
                        Managers.INTERACT.clickBlock(this.placePos, Managers.INTERACT.getClickDirection(this.placePos), this.rotate.getValue(), true);
                    }
                } else if (!this.take.getValue() && this.autoDisable.getValue()) {
                    this.disable2();
                }

            }
        } else {
            this.opend = true;
            if (!this.take.getValue()) {
                if (this.autoDisable.getValue()) {
                    this.disable2();
                }

            } else {
                boolean take = false;
                ScreenHandler pos = mc.player.currentScreenHandler;
                if (pos instanceof ShulkerBoxScreenHandler) {
                    ShulkerBoxScreenHandler shulker = (ShulkerBoxScreenHandler)pos;

                    for(Slot slot : shulker.slots) {
                        if (slot.id < 27 && !slot.getStack().isEmpty() && (!this.smart.getValue() || this.needSteal(slot.getStack()))) {
                            mc.interactionManager.clickSlot(shulker.syncId, slot.id, 0, SlotActionType.QUICK_MOVE, mc.player);
                            take = true;
                        }
                    }
                }

                if (this.autoDisable.getValue() && !take) {
                    this.disable2();
                }

            }
        }
    }

    private void disable2() {
        if (this.disableTimer.passed((Number)this.disableTime.getValue())) {
            this.disable();
        }

    }

    private boolean needSteal(ItemStack i) {
        if (i.getItem().equals(Items.END_CRYSTAL) && this.stealCountList[0] > 0) {
            this.stealCountList[0] -= i.getCount();
            return true;
        } else if (i.getItem().equals(Items.EXPERIENCE_BOTTLE) && this.stealCountList[1] > 0) {
            this.stealCountList[1] -= i.getCount();
            return true;
        } else if (i.getItem().equals(Items.TOTEM_OF_UNDYING) && this.stealCountList[2] > 0) {
            this.stealCountList[2] -= i.getCount();
            return true;
        } else if (i.getItem().equals(Items.ENCHANTED_GOLDEN_APPLE) && this.stealCountList[3] > 0) {
            this.stealCountList[3] -= i.getCount();
            return true;
        } else if (i.getItem().equals(Item.fromBlock(Blocks.ENDER_CHEST)) && this.stealCountList[4] > 0) {
            this.stealCountList[4] -= i.getCount();
            return true;
        } else if (i.getItem().equals(Item.fromBlock(Blocks.COBWEB)) && this.stealCountList[5] > 0) {
            this.stealCountList[5] -= i.getCount();
            return true;
        } else if (i.getItem().equals(Item.fromBlock(Blocks.GLOWSTONE)) && this.stealCountList[6] > 0) {
            this.stealCountList[6] -= i.getCount();
            return true;
        } else if (i.getItem().equals(Item.fromBlock(Blocks.RESPAWN_ANCHOR)) && this.stealCountList[7] > 0) {
            this.stealCountList[7] -= i.getCount();
            return true;
        } else if (i.getItem().equals(Items.ENDER_PEARL) && this.stealCountList[8] > 0) {
            this.stealCountList[8] -= i.getCount();
            return true;
        } else if (i.getItem().equals(Items.SPLASH_POTION) && this.stealCountList[9] > 0) {
            this.stealCountList[9] -= i.getCount();
            return true;
        } else {
            return false;
        }
    }

    public static int getItemCount(Item item) {
        if (item == null) {
            return -1;
        } else {
            int count = 0;

            for(Entry<Integer, ItemStack> entry : InventoryUtil.getInventoryAndHotbarSlots().entrySet()) {
                if (entry.getValue().getItem() == item) {
                    count += entry.getValue().getCount();
                }
            }

            return count;
        }
    }

    private void placeBlock(BlockPos pos) {
        boolean sneak = BlockUtil.shiftBlocks.contains(BlockUtil.getBlock(pos)) && !mc.player.isSneaking();
        if (sneak) {
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.PRESS_SHIFT_KEY));
        }

        Managers.INTERACT.clickBlock(pos.offset(Direction.DOWN), Direction.UP, this.rotate.getValue(), true);
        if (sneak) {
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.RELEASE_SHIFT_KEY));
        }

    }
}
 