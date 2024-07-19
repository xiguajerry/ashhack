package dev.realme.ash.util.player;

import dev.realme.ash.mixin.accessor.AccessorCreativeInventoryScreen;
import dev.realme.ash.mixin.accessor.AccessorHorseScreenHandler;
import dev.realme.ash.util.Globals;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.entity.mob.SkeletonHorseEntity;
import net.minecraft.entity.mob.ZombieHorseEntity;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.BeaconScreenHandler;
import net.minecraft.screen.BlastFurnaceScreenHandler;
import net.minecraft.screen.BrewingStandScreenHandler;
import net.minecraft.screen.CartographyTableScreenHandler;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.screen.FurnaceScreenHandler;
import net.minecraft.screen.Generic3x3ContainerScreenHandler;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.screen.HopperScreenHandler;
import net.minecraft.screen.HorseScreenHandler;
import net.minecraft.screen.LecternScreenHandler;
import net.minecraft.screen.LoomScreenHandler;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.screen.SmokerScreenHandler;
import net.minecraft.screen.StonecutterScreenHandler;

public class SlotUtils {
   public static final int HOTBAR_START = 0;
   public static final int HOTBAR_END = 8;
   public static final int OFFHAND = 45;
   public static final int MAIN_START = 9;
   public static final int MAIN_END = 35;
   public static final int ARMOR_START = 36;
   public static final int ARMOR_END = 39;

   public static int indexToId(int i) {
      if (Globals.mc.player == null) {
         return -1;
      } else {
         ScreenHandler handler = Globals.mc.player.currentScreenHandler;
         if (handler instanceof PlayerScreenHandler) {
            return survivalInventory(i);
         } else if (handler instanceof CreativeInventoryScreen.CreativeScreenHandler) {
            return creativeInventory(i);
         } else if (handler instanceof GenericContainerScreenHandler) {
            GenericContainerScreenHandler genericContainerScreenHandler = (GenericContainerScreenHandler)handler;
            return genericContainer(i, genericContainerScreenHandler.getRows());
         } else if (handler instanceof CraftingScreenHandler) {
            return craftingTable(i);
         } else if (handler instanceof FurnaceScreenHandler) {
            return furnace(i);
         } else if (handler instanceof BlastFurnaceScreenHandler) {
            return furnace(i);
         } else if (handler instanceof SmokerScreenHandler) {
            return furnace(i);
         } else if (handler instanceof Generic3x3ContainerScreenHandler) {
            return generic3x3(i);
         } else if (handler instanceof EnchantmentScreenHandler) {
            return enchantmentTable(i);
         } else if (handler instanceof BrewingStandScreenHandler) {
            return brewingStand(i);
         } else if (handler instanceof MerchantScreenHandler) {
            return villager(i);
         } else if (handler instanceof BeaconScreenHandler) {
            return beacon(i);
         } else if (handler instanceof AnvilScreenHandler) {
            return anvil(i);
         } else if (handler instanceof HopperScreenHandler) {
            return hopper(i);
         } else if (handler instanceof ShulkerBoxScreenHandler) {
            return genericContainer(i, 3);
         } else if (handler instanceof HorseScreenHandler) {
            return horse(handler, i);
         } else if (handler instanceof CartographyTableScreenHandler) {
            return cartographyTable(i);
         } else if (handler instanceof GrindstoneScreenHandler) {
            return grindstone(i);
         } else if (handler instanceof LecternScreenHandler) {
            return lectern();
         } else if (handler instanceof LoomScreenHandler) {
            return loom(i);
         } else {
            return handler instanceof StonecutterScreenHandler ? stonecutter(i) : -1;
         }
      }
   }

   private static int survivalInventory(int i) {
      if (isHotbar(i)) {
         return 36 + i;
      } else {
         return isArmor(i) ? 5 + (i - 36) : i;
      }
   }

   private static int creativeInventory(int i) {
      return Globals.mc.currentScreen instanceof CreativeInventoryScreen && AccessorCreativeInventoryScreen.getSelectedTab() == Registries.ITEM_GROUP.get(ItemGroups.INVENTORY) ? survivalInventory(i) : -1;
   }

   private static int genericContainer(int i, int rows) {
      if (isHotbar(i)) {
         return (rows + 3) * 9 + i;
      } else {
         return isMain(i) ? rows * 9 + (i - 9) : -1;
      }
   }

   private static int craftingTable(int i) {
      if (isHotbar(i)) {
         return 37 + i;
      } else {
         return isMain(i) ? i + 1 : -1;
      }
   }

   private static int furnace(int i) {
      if (isHotbar(i)) {
         return 30 + i;
      } else {
         return isMain(i) ? 3 + (i - 9) : -1;
      }
   }

   private static int generic3x3(int i) {
      if (isHotbar(i)) {
         return 36 + i;
      } else {
         return isMain(i) ? i : -1;
      }
   }

   private static int enchantmentTable(int i) {
      if (isHotbar(i)) {
         return 29 + i;
      } else {
         return isMain(i) ? 2 + (i - 9) : -1;
      }
   }

   private static int brewingStand(int i) {
      if (isHotbar(i)) {
         return 32 + i;
      } else {
         return isMain(i) ? 5 + (i - 9) : -1;
      }
   }

   private static int villager(int i) {
      if (isHotbar(i)) {
         return 30 + i;
      } else {
         return isMain(i) ? 3 + (i - 9) : -1;
      }
   }

   private static int beacon(int i) {
      if (isHotbar(i)) {
         return 28 + i;
      } else {
         return isMain(i) ? 1 + (i - 9) : -1;
      }
   }

   private static int anvil(int i) {
      if (isHotbar(i)) {
         return 30 + i;
      } else {
         return isMain(i) ? 3 + (i - 9) : -1;
      }
   }

   private static int hopper(int i) {
      if (isHotbar(i)) {
         return 32 + i;
      } else {
         return isMain(i) ? 5 + (i - 9) : -1;
      }
   }

   private static int horse(ScreenHandler handler, int i) {
      AbstractHorseEntity entity = ((AccessorHorseScreenHandler)handler).getEntity();
      if (entity instanceof LlamaEntity llamaEntity) {
         int strength = llamaEntity.getStrength();
         if (isHotbar(i)) {
            return 2 + 3 * strength + 28 + i;
         }

         if (isMain(i)) {
            return 2 + 3 * strength + 1 + (i - 9);
         }
      } else if (!(entity instanceof HorseEntity) && !(entity instanceof SkeletonHorseEntity) && !(entity instanceof ZombieHorseEntity)) {
         if (entity instanceof AbstractDonkeyEntity) {
            AbstractDonkeyEntity abstractDonkeyEntity = (AbstractDonkeyEntity)entity;
            boolean chest = abstractDonkeyEntity.hasChest();
            if (isHotbar(i)) {
               return (chest ? 44 : 29) + i;
            }

            if (isMain(i)) {
               return (chest ? 17 : 2) + (i - 9);
            }
         }
      } else {
         if (isHotbar(i)) {
            return 29 + i;
         }

         if (isMain(i)) {
            return 2 + (i - 9);
         }
      }

      return -1;
   }

   private static int cartographyTable(int i) {
      if (isHotbar(i)) {
         return 30 + i;
      } else {
         return isMain(i) ? 3 + (i - 9) : -1;
      }
   }

   private static int grindstone(int i) {
      if (isHotbar(i)) {
         return 30 + i;
      } else {
         return isMain(i) ? 3 + (i - 9) : -1;
      }
   }

   private static int lectern() {
      return -1;
   }

   private static int loom(int i) {
      if (isHotbar(i)) {
         return 31 + i;
      } else {
         return isMain(i) ? 4 + (i - 9) : -1;
      }
   }

   private static int stonecutter(int i) {
      if (isHotbar(i)) {
         return 29 + i;
      } else {
         return isMain(i) ? 2 + (i - 9) : -1;
      }
   }

   public static boolean isHotbar(int i) {
      return i >= 0 && i <= 8;
   }

   public static boolean isMain(int i) {
      return i >= 9 && i <= 35;
   }

   public static boolean isArmor(int i) {
      return i >= 36 && i <= 39;
   }
}
