package dev.realme.ash.util.world;

import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

public class SneakBlocks {
   private static final Set SNEAK_BLOCKS;

   public static boolean isSneakBlock(BlockState state) {
      return isSneakBlock(state.getBlock());
   }

   public static boolean isSneakBlock(Block block) {
      return SNEAK_BLOCKS.contains(block);
   }

   static {
      SNEAK_BLOCKS = Set.of(Blocks.CHEST, Blocks.ENDER_CHEST, Blocks.TRAPPED_CHEST, Blocks.CRAFTING_TABLE, Blocks.FURNACE, Blocks.BLAST_FURNACE, Blocks.FLETCHING_TABLE, Blocks.CARTOGRAPHY_TABLE, Blocks.ENCHANTING_TABLE, Blocks.SMITHING_TABLE, Blocks.STONECUTTER, Blocks.ANVIL, Blocks.CHIPPED_ANVIL, Blocks.DAMAGED_ANVIL, Blocks.JUKEBOX, Blocks.NOTE_BLOCK);
   }
}
