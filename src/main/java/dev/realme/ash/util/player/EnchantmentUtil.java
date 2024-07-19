package dev.realme.ash.util.player;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class EnchantmentUtil {
   public static Object2IntMap getEnchantments(ItemStack itemStack) {
      Object2IntMap enchants = new Object2IntOpenHashMap();
      NbtList list = itemStack.getEnchantments();

      for(int i = 0; i < list.size(); ++i) {
         NbtCompound tag = list.getCompound(i);
         Registries.ENCHANTMENT.getOrEmpty(Identifier.tryParse(tag.getString("id"))).ifPresent((enchantment) -> enchants.put(enchantment, tag.getInt("lvl")));
      }

      return enchants;
   }
}
