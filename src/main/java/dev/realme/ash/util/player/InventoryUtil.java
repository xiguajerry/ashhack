package dev.realme.ash.util.player;

import dev.realme.ash.util.Globals;
import dev.realme.ash.util.world.BlockUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.network.packet.c2s.play.PickFromInventoryC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.potion.PotionUtil;
import net.minecraft.screen.slot.SlotActionType;

public class InventoryUtil implements Globals {
   private static final Action ACTION = new Action();

   public static boolean isHolding32k() {
      return isHolding32k(1000);
   }

   public static boolean isHolding32k(int lvl) {
      ItemStack mainhand = mc.player.getMainHandStack();
      return EnchantmentHelper.getLevel(Enchantments.SHARPNESS, mainhand) >= lvl;
   }

   public static boolean isGapple(Item item) {
      return item == Items.GOLDEN_APPLE || item == Items.ENCHANTED_GOLDEN_APPLE;
   }

   public static int findInventorySlot(Item item, boolean pick) {
      for(int i = pick ? 9 : 0; i < 45; ++i) {
         ItemStack stack = mc.player.getInventory().getStack(i);
         if (stack.getItem() == item) {
            return i < 9 ? i + 36 : i;
         }
      }

      return -1;
   }

   public static void doSwap(int slot) {
      if (slot != -1) {
         if (slot != mc.player.getInventory().selectedSlot) {
            mc.player.getInventory().selectedSlot = slot;
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot));
         }
      }
   }

   public static void doInvSwap(int slot) {
      if (slot != -1) {
         mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
      }
   }

   public static int findBlock() {
      for(int i = 0; i < 9; ++i) {
         ItemStack stack = getStackInSlot(i);
         if (stack.getItem() instanceof BlockItem && !BlockUtil.shiftBlocks.contains(Block.getBlockFromItem(stack.getItem())) && ((BlockItem)stack.getItem()).getBlock() != Blocks.COBWEB) {
            return i;
         }
      }

      return -1;
   }

   public static int findBlockInventorySlot(Block block) {
      return findInventorySlot(Item.fromBlock(block), false);
   }

   public static void doPickSwap(int slot) {
      if (slot != -1) {
         mc.getNetworkHandler().sendPacket(new PickFromInventoryC2SPacket(slot));
      }
   }

   public static boolean hasItemInInventory(Item item, boolean hotbar) {
      int startSlot = hotbar ? 0 : 9;

      for(int i = startSlot; i < 36; ++i) {
         ItemStack itemStack = mc.player.getInventory().getStack(i);
         if (!itemStack.isEmpty() && itemStack.getItem() == item) {
            return true;
         }
      }

      return false;
   }

   public static ItemStack getStackInSlot(int i) {
      return i == -1 ? null : mc.player.getInventory().getStack(i);
   }

   public static int findBlock(Block blockIn) {
      for(int i = 0; i < 9; ++i) {
         ItemStack stack = getStackInSlot(i);
         if (stack != ItemStack.EMPTY && stack.getItem() instanceof BlockItem && ((BlockItem)stack.getItem()).getBlock() == blockIn) {
            return i;
         }
      }

      return -1;
   }

   public static int findUnBlockItemInventory(boolean pick) {
      for(int i = pick ? 9 : 0; i < 45; ++i) {
         ItemStack stack = getStackInSlot(i);
         if (!(stack.getItem() instanceof BlockItem)) {
            return i < 9 ? i + 36 : i;
         }
      }

      return -1;
   }

   public static int findUnBlock() {
      for(int i = 0; i < 9; ++i) {
         ItemStack stack = getStackInSlot(i);
         if (!(stack.getItem() instanceof BlockItem)) {
            return i;
         }
      }

      return -1;
   }

   public static int findItem(Item input) {
      for(int i = 0; i < 9; ++i) {
         Item item = getStackInSlot(i).getItem();
         if (Item.getRawId(item) == Item.getRawId(input)) {
            return i;
         }
      }

      return -1;
   }

   public static int findPotInventorySlot(StatusEffect statusEffect, boolean pick) {
      for(int i = pick ? 9 : 0; i < 45; ++i) {
         ItemStack stack = mc.player.getInventory().getStack(i);
         if (stack.getItem() == Items.SPLASH_POTION) {

             for (StatusEffectInstance effect : PotionUtil.getPotion(stack).getEffects()) {
                 if (effect.getEffectType() == statusEffect) {
                     return i < 9 ? i + 36 : i;
                 }
             }
         }
      }

      return -1;
   }

   public static int findClassInventorySlot(Class clazz, boolean pick) {
      for(int i = pick ? 9 : 0; i < 45; ++i) {
         ItemStack stack = mc.player.getInventory().getStack(i);
         if (stack != ItemStack.EMPTY) {
            if (clazz.isInstance(stack.getItem())) {
               return i < 9 ? i + 36 : i;
            }

            if (stack.getItem() instanceof BlockItem && clazz.isInstance(((BlockItem)stack.getItem()).getBlock())) {
               return i < 9 ? i + 36 : i;
            }
         }
      }

      return -1;
   }

   public static int count(Item item) {
      ItemStack offhandStack = mc.player.getOffHandStack();
      int itemCount = offhandStack.getItem() == item ? offhandStack.getCount() : 0;

      for(int i = 0; i < 36; ++i) {
         ItemStack slot = mc.player.getInventory().getStack(i);
         if (slot.getItem() == item) {
            itemCount += slot.getCount();
         }
      }

      return itemCount;
   }

   public static int count(StatusEffect potion) {
      int count = 0;
      Iterator var2 = getInventoryAndHotbarSlots().entrySet().iterator();

      while(true) {
         while(true) {
            Map.Entry entry;
            do {
               if (!var2.hasNext()) {
                  return count;
               }

               entry = (Map.Entry)var2.next();
            } while(!(((ItemStack)entry.getValue()).getItem() instanceof SplashPotionItem));

            List effects = new ArrayList(PotionUtil.getPotionEffects((ItemStack)entry.getValue()));

             for (Object effect : effects) {
                 StatusEffectInstance potionEffect = (StatusEffectInstance) effect;
                 if (potionEffect.getEffectType() == potion) {
                     count += ((ItemStack) entry.getValue()).getCount();
                     break;
                 }
             }
         }
      }
   }

   public static int findClass(Class clazz) {
      for(int i = 0; i < 9; ++i) {
         ItemStack stack = getStackInSlot(i);
         if (stack != ItemStack.EMPTY) {
            if (clazz.isInstance(stack.getItem())) {
               return i;
            }

            if (stack.getItem() instanceof BlockItem && clazz.isInstance(((BlockItem)stack.getItem()).getBlock())) {
               return i;
            }
         }
      }

      return -1;
   }

   public static Map<Integer, ItemStack> getInventoryAndHotbarSlots() {
      HashMap fullInventorySlots = new HashMap();

      for(int current = 0; current <= 44; ++current) {
         fullInventorySlots.put(current, mc.player.getInventory().getStack(current));
      }

      return fullInventorySlots;
   }

   public static Action move() {
      ACTION.type = SlotActionType.PICKUP;
      ACTION.two = true;
      return ACTION;
   }

   public static Action click() {
      ACTION.type = SlotActionType.PICKUP;
      return ACTION;
   }

   public static Action quickSwap() {
      ACTION.type = SlotActionType.SWAP;
      return ACTION;
   }

   public static Action shiftClick() {
      ACTION.type = SlotActionType.QUICK_MOVE;
      return ACTION;
   }

   public static Action drop() {
      ACTION.type = SlotActionType.THROW;
      ACTION.data = 1;
      return ACTION;
   }

   public static void dropHand() {
      if (!mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
         mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, -999, 0, SlotActionType.PICKUP, mc.player);
      }

   }

   public static class Action {
      private SlotActionType type = null;
      private boolean two = false;
      private int from = -1;
      private int to = -1;
      private int data = 0;
      private boolean isRecursive = false;

      private Action() {
      }

      public Action fromId(int id) {
         this.from = id;
         return this;
      }

      public Action from(int index) {
         return this.fromId(SlotUtils.indexToId(index));
      }

      public Action fromHotbar(int i) {
         return this.from(i);
      }

      public Action fromOffhand() {
         return this.from(45);
      }

      public Action fromMain(int i) {
         return this.from(9 + i);
      }

      public Action fromArmor(int i) {
         return this.from(36 + (3 - i));
      }

      public void toId(int id) {
         this.to = id;
         this.run();
      }

      public void to(int index) {
         this.toId(SlotUtils.indexToId(index));
      }

      public void toHotbar(int i) {
         this.to(i);
      }

      public void toOffhand() {
         this.to(45);
      }

      public void toMain(int i) {
         this.to(9 + i);
      }

      public void toArmor(int i) {
         this.to(36 + (3 - i));
      }

      public void slotId(int id) {
         this.from = this.to = id;
         this.run();
      }

      public void slot(int index) {
         this.slotId(SlotUtils.indexToId(index));
      }

      public void slotHotbar(int i) {
         this.slot(i);
      }

      public void slotOffhand() {
         this.slot(45);
      }

      public void slotMain(int i) {
         this.slot(9 + i);
      }

      public void slotArmor(int i) {
         this.slot(36 + (3 - i));
      }

      private void run() {
         boolean hadEmptyCursor = Globals.mc.player.currentScreenHandler.getCursorStack().isEmpty();
         if (this.type == SlotActionType.SWAP) {
            this.data = this.from;
            this.from = this.to;
         }

         if (this.type != null && this.from != -1 && this.to != -1) {
            this.click(this.from);
            if (this.two) {
               this.click(this.to);
            }
         }

         SlotActionType preType = this.type;
         boolean preTwo = this.two;
         int preFrom = this.from;
         int preTo = this.to;
         this.type = null;
         this.two = false;
         this.from = -1;
         this.to = -1;
         this.data = 0;
         if (!this.isRecursive && hadEmptyCursor && preType == SlotActionType.PICKUP && preTwo && preFrom != -1 && preTo != -1 && !Globals.mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
            this.isRecursive = true;
            InventoryUtil.click().slotId(preFrom);
            this.isRecursive = false;
         }

      }

      private void click(int id) {
         Globals.mc.interactionManager.clickSlot(Globals.mc.player.currentScreenHandler.syncId, id, this.data, this.type, Globals.mc.player);
      }
   }

   public enum SwapMode {
      OFF,
      NORMAL,
      SILENT,
      Inventory,
      Pick;

      // $FF: synthetic method
      private static SwapMode[] $values() {
         return new SwapMode[]{OFF, NORMAL, SILENT, Inventory, Pick};
      }
   }
}
