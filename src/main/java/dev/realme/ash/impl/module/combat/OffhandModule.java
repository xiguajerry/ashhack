package dev.realme.ash.impl.module.combat;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.NumberDisplay;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.EnumConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.TickEvent;
import dev.realme.ash.impl.event.network.PacketEvent;
import dev.realme.ash.impl.gui.click.ClickGuiScreen;
import dev.realme.ash.init.Managers;
import dev.realme.ash.util.math.timer.CacheTimer;
import dev.realme.ash.util.math.timer.Timer;
import dev.realme.ash.util.player.InventoryUtil;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.screen.slot.SlotActionType;

public class OffhandModule
extends ToggleModule {
    Config<OffHandItem> mode = new EnumConfig("Mode", "", OffHandItem.Totem, OffHandItem.values());
    Config<Boolean> removeApple = new BooleanConfig("RemoveApple", "", false);
    Config<Float> delay = new NumberConfig<Float>("Delay", "", 0.0f, 100.0f, 2000.0f, NumberDisplay.DEFAULT);
    Config<Integer> appleSlot = new NumberConfig<Integer>("AppleSlot", "", 1, 5, 9);
    Config<Boolean> pickaxeSwap = new BooleanConfig("PickaxeSwap", "", false);
    Config<Boolean> swapBack = new BooleanConfig("SwapBack", "", true);
    Config<Boolean> cancelPacket = new BooleanConfig("CancelPacket", "", false);
    Config<Boolean> mainHandTotem = new BooleanConfig("MainHandTotem", "", false);
    Config<Float> health = new NumberConfig<Float>("Health", "", 0.0f, 15.0f, 36.0f);
    Config<Integer> slot = new NumberConfig<Integer>("TotemSlot", "", 1, 1, 9);
    Config<DisplayMode> displayMode = new EnumConfig("DisplayMode", "", DisplayMode.Totem, DisplayMode.values());
    int lastSlot = -1;
    private final Timer delayTimer = new CacheTimer();

    public OffhandModule() {
        super("Offhand", "Automatically replenishes the totem in your offhand", ModuleCategory.COMBAT);
    }

    @Override
    public String getModuleData() {
        Item item = OffhandModule.mc.player.getOffHandStack().getItem();
        String itemName = null;
        if (item.equals(Items.TOTEM_OF_UNDYING)) {
            itemName = "Totems";
        }
        if (item.equals(Items.ENCHANTED_GOLDEN_APPLE)) {
            itemName = "GApples";
        }
        if (itemName == null) {
            return null;
        }
        return switch (this.displayMode.getValue()) {
            case Offhand -> itemName + " " + InventoryUtil.count(item);
            case Totem -> "Totems " + InventoryUtil.count(Items.TOTEM_OF_UNDYING);
        };
    }

    private void swap(int itemSlot) {
        if (itemSlot == -1) {
            return;
        }
        InventoryUtil.move().from(itemSlot).toOffhand();
        OffhandModule.mc.interactionManager.clickSlot(OffhandModule.mc.player.currentScreenHandler.syncId, itemSlot, 0, SlotActionType.PICKUP, OffhandModule.mc.player);
    }

    @EventListener
    public void onTick(TickEvent event) {
        int appleSlot;
        if (OffhandModule.mc.player == null || OffhandModule.mc.world == null) {
            return;
        }
        if (this.pickaxeSwap.getValue().booleanValue() && (appleSlot = InventoryUtil.findItem(Items.ENCHANTED_GOLDEN_APPLE)) != -1) {
            if (OffhandModule.mc.options.useKey.isPressed()) {
                if (OffhandModule.mc.player.getMainHandStack().getItem() instanceof PickaxeItem) {
                    if (this.lastSlot == -1) {
                        this.lastSlot = OffhandModule.mc.player.getInventory().selectedSlot;
                    }
                    InventoryUtil.doSwap(appleSlot);
                }
            } else if (this.lastSlot != -1 && this.swapBack.getValue().booleanValue()) {
                InventoryUtil.doSwap(this.lastSlot);
                this.lastSlot = -1;
            }
        }
        if (OffhandModule.mc.currentScreen == null || OffhandModule.mc.currentScreen instanceof InventoryScreen || OffhandModule.mc.currentScreen instanceof ChatScreen || OffhandModule.mc.currentScreen instanceof ClickGuiScreen) {
            int itemSlot;
            if (this.mainHandTotem.getValue().booleanValue()) {
                int totemSlot = OffhandModule.getItemSlot(Items.TOTEM_OF_UNDYING);
                if (OffhandModule.mc.player.getHealth() + OffhandModule.mc.player.getAbsorptionAmount() < this.health.getValue().floatValue() && totemSlot < 9) {
                    InventoryUtil.doSwap(this.slot.getValue() - 1);
                }
                if (totemSlot != -1 && OffhandModule.mc.player.getInventory().getStack(this.slot.getValue() - 1).getItem() != Items.TOTEM_OF_UNDYING) {
                    OffhandModule.mc.interactionManager.clickSlot(OffhandModule.mc.player.currentScreenHandler.syncId, totemSlot, this.slot.getValue() - 1, SlotActionType.SWAP, OffhandModule.mc.player);
                }
            }
            if (this.removeApple.getValue().booleanValue() && (itemSlot = OffhandModule.getItemSlot(Items.ENCHANTED_GOLDEN_APPLE)) != -1 && OffhandModule.mc.player.getInventory().getStack(this.appleSlot.getValue() - 1).getItem() != Items.ENCHANTED_GOLDEN_APPLE && this.delayTimer.passed(this.delay.getValue())) {
                OffhandModule.mc.interactionManager.clickSlot(OffhandModule.mc.player.currentScreenHandler.syncId, itemSlot, this.appleSlot.getValue() - 1, SlotActionType.SWAP, OffhandModule.mc.player);
                this.delayTimer.reset();
            }
            if (this.mode.getValue() == OffHandItem.Totem && !OffhandModule.mc.player.getOffHandStack().getItem().equals(Items.TOTEM_OF_UNDYING)) {
                this.swap(OffhandModule.getItemSlot(Items.TOTEM_OF_UNDYING));
                return;
            }
            if (this.mode.getValue() == OffHandItem.Gapple && !OffhandModule.mc.player.getOffHandStack().getItem().equals(Items.ENCHANTED_GOLDEN_APPLE)) {
                this.swap(OffhandModule.getItemSlot(Items.ENCHANTED_GOLDEN_APPLE));
                return;
            }
            if (this.mode.getValue() == OffHandItem.Crystal && !OffhandModule.mc.player.getOffHandStack().getItem().equals(Items.END_CRYSTAL)) {
                this.swap(OffhandModule.getItemSlot(Items.END_CRYSTAL));
            }
        }
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Receive event) {
        if (OffhandModule.mc.player == null || OffhandModule.mc.world == null) {
            return;
        }
        if (event.getPacket() instanceof UpdateSelectedSlotS2CPacket) {
            if (this.cancelPacket.getValue().booleanValue()) {
                event.cancel();
            }
            Managers.NETWORK.sendPacket(new UpdateSelectedSlotC2SPacket(OffhandModule.mc.player.getInventory().selectedSlot));
        }
    }

    private static int getItemSlot(Item item) {
        if (item == OffhandModule.mc.player.getOffHandStack().getItem()) {
            return -1;
        }
        for (int i = 36; i >= 0; --i) {
            if (OffhandModule.mc.player.getInventory().getStack(i).getItem() != item) continue;
            if (i < 9) {
                i += 36;
            }
            return i;
        }
        return -1;
    }

    public enum OffHandItem {
        Gapple,
        Crystal,
        Totem

    }

    public enum DisplayMode {
        Totem,
        Offhand

    }
}
