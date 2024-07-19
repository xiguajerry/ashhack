package dev.realme.ash.util.world;

import dev.realme.ash.util.Globals;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

public class BlastResistantBlocks implements Globals {
   private static final Set BLAST_RESISTANT;
   private static final Set UNBREAKABLE;

   public static boolean isBreakable(BlockPos pos) {
      return isBreakable(mc.world.getBlockState(pos).getBlock());
   }

   public static boolean isBreakable(Block block) {
      return !UNBREAKABLE.contains(block);
   }

   public static boolean isUnbreakable(BlockPos pos) {
      return isUnbreakable(mc.world.getBlockState(pos).getBlock());
   }

   public static boolean isUnbreakable(Block block) {
      return UNBREAKABLE.contains(block);
   }

   public static boolean isBlastResistant(BlockPos pos) {
      return isBlastResistant(mc.world.getBlockState(pos).getBlock());
   }

   public static boolean isBlastResistant(Block block) {
      return BLAST_RESISTANT.contains(block);
   }

   static {
      BLAST_RESISTANT = new ReferenceOpenHashSet(Set.of(Blocks.OBSIDIAN, Blocks.ANVIL, Blocks.ENCHANTING_TABLE, Blocks.ENDER_CHEST, Blocks.BEACON));
      UNBREAKABLE = new ReferenceOpenHashSet(Set.of(Blocks.BEDROCK, Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK, Blocks.END_PORTAL_FRAME, Blocks.BARRIER));
   }
}
