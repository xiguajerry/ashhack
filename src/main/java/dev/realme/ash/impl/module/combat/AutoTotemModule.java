package dev.realme.ash.impl.module.combat;

import com.google.common.collect.Lists;
import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.EnumConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.network.PacketEvent;
import dev.realme.ash.impl.event.network.PlayerTickEvent;
import dev.realme.ash.util.player.InventoryUtil;
import dev.realme.ash.util.player.PlayerUtil;
import dev.realme.ash.util.world.ExplosionUtil;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.item.TridentItem;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.screen.slot.SlotActionType;

public class AutoTotemModule
extends ToggleModule {
    EnumConfig<OffhandItem> itemConfig = new EnumConfig("Item", "The item to wield in your offhand", (Enum)OffhandItem.TOTEM, (Enum[])OffhandItem.values());
    NumberConfig<Float> healthConfig = new NumberConfig<Float>("Health", "The health required to fall below before swapping to a totem", Float.valueOf(0.0f), Float.valueOf(14.0f), Float.valueOf(20.0f));
    Config<Boolean> gappleConfig = new BooleanConfig("OffhandGapple", "If to equip a golden apple if holding down the item use button", true);
    Config<Boolean> crappleConfig = new BooleanConfig("Crapple", "If to use a normal golden apple if Absorption is present", true);
    Config<Boolean> lethalConfig = new BooleanConfig("Lethal", "Calculate lethal damage sources", false);
    Config<Boolean> fastConfig = new BooleanConfig("FastSwap", "Swaps items to offhand", true);
    Config<Boolean> debugConfig = new BooleanConfig("Debug", "If to debug on death", false);
    private int lastHotbarSlot;
    private int lastTotemCount;
    private Item lastHotbarItem;

    public AutoTotemModule() {
        super("AutoTotem", "Automatically replenishes the totem in your offhand", ModuleCategory.COMBAT);
    }

    @Override
    public String getModuleData() {
        return String.valueOf(InventoryUtil.count(Items.TOTEM_OF_UNDYING));
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.lastHotbarSlot = -1;
        this.lastHotbarItem = null;
        this.lastTotemCount = 0;
    }

    @EventListener
    public void onPlayerTick(PlayerTickEvent event) {
        Item itemToWield = this.getItemToWield();
        if (AutoTotemModule.mc.player.getOffHandStack().getItem().equals(itemToWield)) {
            return;
        }
        int itemSlot = this.getSlotFor(itemToWield);
        if (itemSlot != -1) {
            if (itemSlot < 9) {
                this.lastHotbarItem = itemToWield;
                this.lastHotbarSlot = itemSlot;
            }
            if (this.fastConfig.getValue().booleanValue()) {
                AutoTotemModule.mc.interactionManager.clickSlot(AutoTotemModule.mc.player.playerScreenHandler.syncId, itemSlot < 9 ? itemSlot + 36 : itemSlot, 40, SlotActionType.SWAP, AutoTotemModule.mc.player);
            } else {
                AutoTotemModule.mc.interactionManager.clickSlot(AutoTotemModule.mc.player.playerScreenHandler.syncId, itemSlot < 9 ? itemSlot + 36 : itemSlot, 0, SlotActionType.PICKUP, AutoTotemModule.mc.player);
                AutoTotemModule.mc.interactionManager.clickSlot(AutoTotemModule.mc.player.playerScreenHandler.syncId, 45, 0, SlotActionType.PICKUP, AutoTotemModule.mc.player);
                if (!AutoTotemModule.mc.player.playerScreenHandler.getCursorStack().isEmpty()) {
                    AutoTotemModule.mc.interactionManager.clickSlot(AutoTotemModule.mc.player.playerScreenHandler.syncId, itemSlot < 9 ? itemSlot + 36 : itemSlot, 0, SlotActionType.PICKUP, AutoTotemModule.mc.player);
                }
            }
            this.lastTotemCount = InventoryUtil.count(Items.TOTEM_OF_UNDYING) - 1;
        }
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Receive event) {
        HealthUpdateS2CPacket packet;
        Packet<?> packet2 = event.getPacket();
        if (packet2 instanceof HealthUpdateS2CPacket && (packet = (HealthUpdateS2CPacket)((Object)packet2)).getHealth() <= 0.0f && this.debugConfig.getValue().booleanValue()) {
            LinkedHashSet<Object> reasons = new LinkedHashSet<Object>();
            if (this.lastTotemCount <= 0) {
                reasons.add("no_totems");
            }
            if (AutoTotemModule.mc.player.currentScreenHandler.syncId != 0) {
                reasons.add("gui_fail(" + AutoTotemModule.mc.player.currentScreenHandler.syncId + ")");
            }
            if (!AutoTotemModule.mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
                reasons.add("cursor_stack=" + AutoTotemModule.mc.player.currentScreenHandler.getCursorStack().getItem());
            }
            if (!reasons.isEmpty()) {
                this.sendModuleMessage("Possible failure reasons: " + String.join((CharSequence)", ", (CharSequence) reasons));
            } else {
                int totemCount = InventoryUtil.count(Items.TOTEM_OF_UNDYING);
                this.sendModuleMessage("Could not figure out possible reasons. meta:{totemCount=" + totemCount + ", matchesCache=" + ((totemCount = InventoryUtil.count(Items.TOTEM_OF_UNDYING)) == this.lastTotemCount) + ", cached=" + this.lastTotemCount + "}");
            }
        }
    }

    private int getSlotFor(Item item) {
        ItemStack stack;
        if (this.lastHotbarSlot != -1 && this.lastHotbarItem != null && (stack = AutoTotemModule.mc.player.getInventory().getStack(this.lastHotbarSlot)).getItem().equals(item) && this.lastHotbarItem.equals(AutoTotemModule.mc.player.getOffHandStack().getItem())) {
            int tmp = this.lastHotbarSlot;
            this.lastHotbarSlot = -1;
            this.lastHotbarItem = null;
            return tmp;
        }
        for (int slot = 36; slot >= 0; --slot) {
            ItemStack itemStack = AutoTotemModule.mc.player.getInventory().getStack(slot);
            if (itemStack.isEmpty() || !itemStack.getItem().equals(item)) continue;
            return slot;
        }
        return -1;
    }

    private Item getItemToWield() {
        float health = PlayerUtil.getLocalPlayerHealth();
        if (health <= ((Float)this.healthConfig.getValue()).floatValue()) {
            return Items.TOTEM_OF_UNDYING;
        }
        if ((float)PlayerUtil.computeFallDamage(AutoTotemModule.mc.player.fallDistance, 1.0f) + 0.5f > AutoTotemModule.mc.player.getHealth()) {
            return Items.TOTEM_OF_UNDYING;
        }
        if (this.lethalConfig.getValue().booleanValue()) {
            ArrayList<Entity> entities = Lists.newArrayList(AutoTotemModule.mc.world.getEntities());
            for (Entity e : entities) {
                double potential;
                if (e == null || !e.isAlive() || !(e instanceof EndCrystalEntity)) continue;
                EndCrystalEntity crystal = (EndCrystalEntity)e;
                if (AutoTotemModule.mc.player.squaredDistanceTo(e) > 144.0 || (double)health + 0.5 > (potential = ExplosionUtil.getDamageTo(AutoTotemModule.mc.player, crystal.getPos()))) continue;
                return Items.TOTEM_OF_UNDYING;
            }
        }
        if (this.gappleConfig.getValue().booleanValue() && AutoTotemModule.mc.options.useKey.isPressed() && (AutoTotemModule.mc.player.getMainHandStack().getItem() instanceof SwordItem || AutoTotemModule.mc.player.getMainHandStack().getItem() instanceof TridentItem || AutoTotemModule.mc.player.getMainHandStack().getItem() instanceof AxeItem)) {
            return this.getGoldenAppleType();
        }
        return ((OffhandItem)((Object)this.itemConfig.getValue())).getItem();
    }

    private Item getGoldenAppleType() {
        if (this.crappleConfig.getValue().booleanValue() && AutoTotemModule.mc.player.hasStatusEffect(StatusEffects.ABSORPTION) && InventoryUtil.hasItemInInventory(Items.GOLDEN_APPLE, true)) {
            return Items.GOLDEN_APPLE;
        }
        return Items.ENCHANTED_GOLDEN_APPLE;
    }

    private static enum OffhandItem {
        TOTEM(Items.TOTEM_OF_UNDYING),
        GAPPLE(Items.ENCHANTED_GOLDEN_APPLE),
        CRYSTAL(Items.END_CRYSTAL);

        private final Item item;

        private OffhandItem(Item item) {
            this.item = item;
        }

        public Item getItem() {
            return this.item;
        }
    }
}