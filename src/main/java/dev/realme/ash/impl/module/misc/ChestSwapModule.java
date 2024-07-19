package dev.realme.ash.impl.module.misc;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.EnumConfig;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.util.player.InventoryUtil;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ChestSwapModule
extends ToggleModule {
    Config<Priority> priorityConfig = new EnumConfig("Priority", "The chestplate material to prioritize", Priority.NETHERITE, Priority.values());

    public ChestSwapModule() {
        super("ChestSwap", "Automatically swaps chestplate", ModuleCategory.MISCELLANEOUS);
    }

    @Override
    public void onEnable() {
        ArmorItem armorItem;
        ItemStack armorStack = ChestSwapModule.mc.player.getInventory().getArmorStack(2);
        Item item = armorStack.getItem();
        if (item instanceof ArmorItem && (armorItem = (ArmorItem) item).getSlotType() == EquipmentSlot.CHEST) {
            int elytraSlot = this.getElytraSlot();
            if (elytraSlot != -1) {
                InventoryUtil.move().from(elytraSlot).toArmor(2);
            }
        } else {
            int chestplateSlot = this.getChestplateSlot();
            if (chestplateSlot != -1) {
                InventoryUtil.move().from(chestplateSlot).toArmor(2);
            }
        }
        this.disable();
    }

    private int getChestplateSlot() {
        int slot = -1;
        for (int i = 0; i < 45; ++i) {
            ArmorItem armorItem;
            ItemStack stack = ChestSwapModule.mc.player.getInventory().getStack(i);
            Item item = stack.getItem();
            if (!(item instanceof ArmorItem) || (armorItem = (ArmorItem) item).getSlotType() != EquipmentSlot.CHEST) continue;
            if (armorItem.getMaterial() == ArmorMaterials.NETHERITE && this.priorityConfig.getValue() == Priority.NETHERITE) {
                slot = i;
                break;
            }
            if (armorItem.getMaterial() == ArmorMaterials.DIAMOND && this.priorityConfig.getValue() == Priority.DIAMOND) {
                slot = i;
                break;
            }
            slot = i;
        }
        return slot;
    }

    private int getElytraSlot() {
        int slot = -1;
        for (int i = 0; i < 45; ++i) {
            ItemStack stack = ChestSwapModule.mc.player.getInventory().getStack(i);
            if (!(stack.getItem() instanceof ElytraItem)) continue;
            slot = i;
            break;
        }
        return slot;
    }

    private enum Priority {
        NETHERITE,
        DIAMOND

    }
}
