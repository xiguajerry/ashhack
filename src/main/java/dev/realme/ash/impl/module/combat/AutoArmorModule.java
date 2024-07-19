// Decompiled with: FernFlower
// Class Version: 17
package dev.realme.ash.impl.module.combat;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.NumberDisplay;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.EnumConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.EventStage;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.TickEvent;
import dev.realme.ash.impl.module.combat.AutoArmorModule;
import dev.realme.ash.util.player.EnchantmentUtil;
import dev.realme.ash.util.player.InventoryUtil;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.PriorityQueue;
import java.util.Queue;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class AutoArmorModule extends ToggleModule {
    final Config<AutoArmorModule.Priority> priorityConfig = new EnumConfig<>("Priority", "Armor enchantment priority", AutoArmorModule.Priority.BLAST_PROTECTION, AutoArmorModule.Priority.values());
    final Config<Float> minDurabilityConfig = new NumberConfig<>("MinDurability", "Durability percent to replace armor", 0.0F, 0.0F, 20.0F, NumberDisplay.PERCENT);
    final Config<Boolean> elytraPriorityConfig = new BooleanConfig("ElytraPriority", "Prioritizes existing elytras in the chestplate armor slot", true);
    final Config<Boolean> blastLeggingsConfig = new BooleanConfig("Leggings-BlastPriority", "Prioritizes Blast Protection leggings", true);
    final Config<Boolean> noBindingConfig = new BooleanConfig("NoBinding", "Avoids armor with the Curse of Binding enchantment", true);
    final Config<Boolean> inventoryConfig = new BooleanConfig("AllowInventory", "Allows armor to be swapped while in the inventory menu", false);
    private final Queue<AutoArmorModule.ArmorSlot> helmet = new PriorityQueue<>();
    private final Queue<AutoArmorModule.ArmorSlot> chestplate = new PriorityQueue<>();
    private final Queue<AutoArmorModule.ArmorSlot> leggings = new PriorityQueue<>();
    private final Queue<AutoArmorModule.ArmorSlot> boots = new PriorityQueue<>();

    public AutoArmorModule() {
        super("AutoArmor", "Automatically replaces armor pieces", ModuleCategory.COMBAT);
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (event.getStage() == EventStage.PRE) {
            if (mc.currentScreen == null || mc.currentScreen instanceof InventoryScreen && this.inventoryConfig.getValue()) {
                this.helmet.clear();
                this.chestplate.clear();
                this.leggings.clear();
                this.boots.clear();

                for(int j = 0; j < 45; ++j) {
                    assert mc.player != null;
                    ItemStack stack = mc.player.getInventory().getStack(j);
                    if (!stack.isEmpty()) {
                        Item bootsSlot = stack.getItem();
                        if (bootsSlot instanceof ArmorItem armor) {
                            if (!this.noBindingConfig.getValue() || !EnchantmentHelper.hasBindingCurse(stack)) {
                                int index = armor.getSlotType().getEntitySlotId();
                                float dura = (float)(stack.getMaxDamage() - stack.getDamage()) / (float)stack.getMaxDamage();
                                if (!(dura < this.minDurabilityConfig.getValue())) {
                                    AutoArmorModule.ArmorSlot data = new AutoArmorModule.ArmorSlot(index, j, stack);
                                    switch(index) {
                                        case 0:
                                            this.helmet.add(data);
                                            break;
                                        case 1:
                                            this.chestplate.add(data);
                                            break;
                                        case 2:
                                            this.leggings.add(data);
                                            break;
                                        case 3:
                                            this.boots.add(data);
                                    }
                                }
                            }
                        }
                    }
                }

                for(int i = 0; i < 4; ++i) {
                    ItemStack armorStack = mc.player.getInventory().getArmorStack(i);
                    if (!this.elytraPriorityConfig.getValue() || armorStack.getItem() != Items.ELYTRA) {
                        float armorDura = (float)(armorStack.getMaxDamage() - armorStack.getDamage()) / (float)armorStack.getMaxDamage();
                        if (armorStack.isEmpty() && !(armorDura >= this.minDurabilityConfig.getValue())) {
                            switch(i) {
                                case 0:
                                    if (!this.helmet.isEmpty()) {
                                        AutoArmorModule.ArmorSlot helmetSlot = this.helmet.poll();
                                        this.swapArmor(helmetSlot.getType(), helmetSlot.getSlot());
                                    }
                                    break;
                                case 1:
                                    if (!this.chestplate.isEmpty()) {
                                        AutoArmorModule.ArmorSlot chestSlot = this.chestplate.poll();
                                        this.swapArmor(chestSlot.getType(), chestSlot.getSlot());
                                    }
                                    break;
                                case 2:
                                    if (!this.leggings.isEmpty()) {
                                        AutoArmorModule.ArmorSlot leggingsSlot = this.leggings.poll();
                                        this.swapArmor(leggingsSlot.getType(), leggingsSlot.getSlot());
                                    }
                                    break;
                                case 3:
                                    if (!this.boots.isEmpty()) {
                                        AutoArmorModule.ArmorSlot bootsSlot = this.boots.poll();
                                        this.swapArmor(bootsSlot.getType(), bootsSlot.getSlot());
                                    }
                            }
                        }
                    }
                }

            }
        }
    }

    public void swapArmor(int armorSlot, int slot) {
        assert mc.player != null;
        ItemStack stack = mc.player.getInventory().getArmorStack(armorSlot);
        boolean rt = !stack.isEmpty();
        InventoryUtil.move().from(slot).toArmor(armorSlot);
    }

    public class ArmorSlot implements Comparable<AutoArmorModule.ArmorSlot> {
        private final int armorType;
        private final int slot;
        private final ItemStack armorStack;

        public ArmorSlot(int armorType, int slot, ItemStack armorStack) {
            this.armorType = armorType;
            this.slot = slot;
            this.armorStack = armorStack;
        }

        public int compareTo(AutoArmorModule.ArmorSlot other) {
            if (this.armorType != other.armorType) {
                return 0;
            } else {
                ItemStack otherStack = other.getArmorStack();
                ArmorItem armorItem = (ArmorItem)this.armorStack.getItem();
                ArmorItem otherItem = (ArmorItem)otherStack.getItem();
                int durabilityDiff = armorItem.getMaterial().getProtection(armorItem.getType()) - otherItem.getMaterial().getProtection(otherItem.getType());
                if (durabilityDiff != 0) {
                    return durabilityDiff;
                } else {
                    Enchantment enchantment = AutoArmorModule.this.priorityConfig.getValue().getEnchantment();
                    if (AutoArmorModule.this.blastLeggingsConfig.getValue() && this.armorType == 2 && this.hasEnchantment(Enchantments.BLAST_PROTECTION)) {
                        return -1;
                    } else if (this.hasEnchantment(enchantment)) {
                        return other.hasEnchantment(enchantment) ? 0 : -1;
                    } else {
                        return other.hasEnchantment(enchantment) ? 1 : 0;
                    }
                }
            }
        }

        public boolean hasEnchantment(Enchantment enchantment) {
            Object2IntMap<Enchantment> enchants = EnchantmentUtil.getEnchantments(this.armorStack);
            return enchants.containsKey(enchantment);
        }

        public ItemStack getArmorStack() {
            return this.armorStack;
        }

        public int getType() {
            return this.armorType;
        }

        public int getSlot() {
            return this.slot;
        }
    }

    public enum Priority {
        BLAST_PROTECTION(Enchantments.BLAST_PROTECTION),
        PROTECTION(Enchantments.PROTECTION),
        PROJECTILE_PROTECTION(Enchantments.PROJECTILE_PROTECTION);

        private final Enchantment enchant;

        Priority(Enchantment enchant) {
            this.enchant = enchant;
        }

        public Enchantment getEnchantment() {
            return this.enchant;
        }

        // $FF: synthetic method
        private static AutoArmorModule.Priority[] $values() {
            return new AutoArmorModule.Priority[]{BLAST_PROTECTION, PROTECTION, PROJECTILE_PROTECTION};
        }
    }
}
 