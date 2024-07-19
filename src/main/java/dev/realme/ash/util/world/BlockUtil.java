package dev.realme.ash.util.world;

import dev.realme.ash.init.Managers;
import dev.realme.ash.init.Modules;
import dev.realme.ash.util.Globals;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.AirBlock;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.WorldChunk;

public class BlockUtil implements Globals {
   public static final List shiftBlocks;
   public static final List mineBlocks;

   public static boolean canMovement(BlockPos pos) {
      return !getState(pos).blocksMovement();
   }

   public static boolean blockMovement(BlockPos pos) {
      return getState(pos).blocksMovement();
   }

   public static boolean canStand(BlockPos pos) {
      return blockMovement(pos.down());
   }

   public static ArrayList<BlockEntity> getTileEntities() {
      return getLoadedChunks().flatMap((chunk) -> chunk.getBlockEntities().values().stream()).collect(Collectors.toCollection(ArrayList::new));
   }

   public static Stream<WorldChunk> getLoadedChunks() {
      int radius = Math.max(2, mc.options.getClampedViewDistance()) + 3;
      int diameter = radius * 2 + 1;
      ChunkPos center = mc.player.getChunkPos();
      ChunkPos min = new ChunkPos(center.x - radius, center.z - radius);
      ChunkPos max = new ChunkPos(center.x + radius, center.z + radius);
      return Stream.iterate(min, (pos) -> {
         int x = pos.x;
         int z = pos.z;
         ++x;
         if (x > max.x) {
            x = min.x;
            ++z;
         }

         return new ChunkPos(x, z);
      }).limit((long)diameter * (long)diameter).filter((c) -> mc.world.isChunkLoaded(c.x, c.z)).map((c) -> mc.world.getChunk(c.x, c.z)).filter(Objects::nonNull);
   }

   public static boolean isHole(BlockPos pos) {
      return isHole(pos, true, false, false);
   }

   public static boolean isHole(BlockPos pos, boolean canStand, boolean checkTrap, boolean anyBlock) {
      int blockProgress = 0;
      Direction[] var5 = Direction.values();
      int var6 = var5.length;

       for (Direction i : var5) {
           if (i != Direction.UP && i != Direction.DOWN && (anyBlock && !mc.world.isAir(pos.offset(i)) || isHard(pos.offset(i)))) {
               ++blockProgress;
           }
       }

      return (!checkTrap || getBlock(pos) == Blocks.AIR && getBlock(pos.add(0, 1, 0)) == Blocks.AIR && getBlock(pos.add(0, 2, 0)) == Blocks.AIR) && blockProgress > 3 && (!canStand || getState(pos.add(0, -1, 0)).blocksMovement());
   }

   public static boolean isHard(BlockPos pos) {
      return getState(pos).getBlock() == Blocks.OBSIDIAN || getState(pos).getBlock() == Blocks.ENDER_CHEST || getState(pos).getBlock() == Blocks.BEDROCK || getState(pos).getBlock() == Blocks.ANVIL;
   }

   public static boolean isCrystalBase(BlockPos pos) {
      return getState(pos).getBlock() == Blocks.OBSIDIAN || getState(pos).getBlock() == Blocks.BEDROCK;
   }

   public static boolean isMining(BlockPos pos) {
      return Managers.BREAK.isMining(pos) || pos.equals(Modules.PACKET_DIGGING.breakPos);
   }

   public static boolean clientCanPlace(BlockPos pos) {
      if (!canReplace(pos)) {
         return false;
      } else {
         return !EntityUtil.hasEntity(pos);
      }
   }

   public static boolean canClickThrough(BlockPos pos) {
      BlockState blockState = mc.world.getBlockState(pos);
      return blockState.getBlock() instanceof FluidBlock || blockState.getBlock() == Blocks.AIR || !blockState.isFullCube(mc.world, pos) || getBlock(pos) == Blocks.COBWEB || getBlock(pos) == Blocks.PISTON_HEAD || getBlock(pos) instanceof PistonBlock && getState(pos).get(Properties.EXTENDED);
   }

   public static boolean isStrictDirection(BlockPos pos, Direction side) {
      return Managers.INTERACT.isStrictDirection(pos, side);
   }

   public static boolean canClick(BlockPos pos) {
      return mc.world.getBlockState(pos).isSolid() && (!shiftBlocks.contains(getBlock(pos)) && !(getBlock(pos) instanceof BedBlock) || mc.player.isSneaking());
   }

   public static boolean hasEntityBlockCrystal(BlockPos pos, boolean ignoreCrystal, boolean ignoreItem) {
      Iterator var3 = mc.world.getNonSpectatingEntities(Entity.class, new Box(pos)).iterator();

      Entity entity;
      do {
         do {
            do {
               do {
                  if (!var3.hasNext()) {
                     return false;
                  }

                  entity = (Entity)var3.next();
               } while(!entity.isAlive());
            } while(ignoreItem && entity instanceof ItemEntity);
         } while(ignoreCrystal && entity instanceof EndCrystalEntity);
      } while(entity instanceof ArmorStandEntity);

      return true;
   }

   public static boolean isAir(BlockPos pos) {
      return pos != null && mc.world.isAir(pos);
   }

   public static boolean solid(BlockPos blockPos) {
      Block block = mc.world.getBlockState(blockPos).getBlock();
      return !(block instanceof AbstractFireBlock) && !(block instanceof FluidBlock) && !(block instanceof AirBlock);
   }

   public static ArrayList<BlockPos> getSphere(float range) {
      return getSphere(range, mc.player.getEyePos());
   }

   public static ArrayList<BlockPos> getSphere(float range, Vec3d center) {
      ArrayList<BlockPos> list = new ArrayList<>();
      BlockPos playerPos = BlockPos.ofFloored(center);

      for(int x = (int)Math.floor((float)playerPos.getX() - range); (double)x <= Math.ceil((float)playerPos.getX() + range); ++x) {
         for(int y = (int)Math.floor((float)playerPos.getY() - range); (double)y <= Math.ceil((float)playerPos.getY() + range); ++y) {
            for(int z = (int)Math.floor((float)playerPos.getZ() - range); (double)z <= Math.ceil((float)playerPos.getZ() + range); ++z) {
               BlockPos curPos = new BlockPosX(x, y, z);
               if (MathHelper.sqrt((float)center.squaredDistanceTo(curPos.toCenterPos())) <= range) {
                  list.add(curPos);
               }
            }
         }
      }

      return list;
   }

   public static double distanceTo(BlockPos blockPos1, BlockPos blockPos2) {
      double d = blockPos1.getX() - blockPos2.getX();
      double e = blockPos1.getY() - blockPos2.getY();
      double f = blockPos1.getZ() - blockPos2.getZ();
      return MathHelper.sqrt((float)(d * d + e * e + f * f));
   }

   public static boolean isWithin(BlockPos blockPos, double r) {
      return squaredDistanceTo(blockPos.getX(), blockPos.getY(), blockPos.getZ()) <= r * r;
   }

   public static double squaredDistanceTo(double x, double y, double z) {
      return squaredDistance(mc.player.getX(), mc.player.getY(), mc.player.getZ(), x, y, z);
   }

   public static double squaredDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
      float f = (float)(x1 - x2);
      float g = (float)(y1 - y2);
      float h = (float)(z1 - z2);
      return org.joml.Math.fma(f, f, org.joml.Math.fma(g, g, h * h));
   }

   public static boolean isAir(Vec3d vec3d) {
      return isAir(vec3toBlockPos(vec3d));
   }

   public static BlockPos vec3toBlockPos(Vec3d vec3d) {
      return new BlockPos((int)Math.floor(vec3d.x), (int)Math.round(vec3d.y), (int)Math.floor(vec3d.z));
   }

   public static List blockEntities() {
      List list = new ArrayList();

       for (Object o : loadedChunks()) {
           WorldChunk chunk = (WorldChunk) o;
           list.addAll(chunk.getBlockEntities().values());
       }

      return list;
   }

   public static List loadedChunks() {
      List chunks = new ArrayList();
      int viewDist = mc.options.getViewDistance().getValue();

      for(int x = -viewDist; x <= viewDist; ++x) {
         for(int z = -viewDist; z <= viewDist; ++z) {
            WorldChunk chunk = mc.world.getChunkManager().getWorldChunk((int)mc.player.getX() / 16 + x, (int)mc.player.getZ() / 16 + z);
            if (chunk != null) {
               chunks.add(chunk);
            }
         }
      }

      return chunks;
   }

   public static boolean isBlockAccessible(BlockPos pos) {
      return mc.world.isAir(pos) && !mc.world.isAir(pos.add(0, -1, 0)) && mc.world.isAir(pos.add(0, 1, 0)) && mc.world.isAir(pos.add(0, 2, 0));
   }

   public static boolean isBlockLoaded(double x, double z) {
      ChunkManager chunkManager = mc.world.getChunkManager();
      return chunkManager != null && chunkManager.isChunkLoaded(ChunkSectionPos.getSectionCoord(x), ChunkSectionPos.getSectionCoord(z));
   }

   public static boolean canPlace(BlockPos pos, boolean ignoreCrystal) {
      if (Managers.INTERACT.getPlaceDirection(pos) == null) {
         return false;
      } else if (!canReplace(pos)) {
         return false;
      } else {
         return !EntityUtil.hasEntity(pos, ignoreCrystal);
      }
   }

   public static boolean hasSupport(BlockPos pos) {
      boolean supported = false;
      Direction[] var2 = Direction.values();
      int var3 = var2.length;

       for (Direction enumFacing : var2) {
           if (!isAir(pos.offset(enumFacing))) {
               supported = true;
           }
       }

      return supported;
   }

   public static boolean canPlace(BlockPos pos) {
      if (Managers.INTERACT.getPlaceDirection(pos) == null) {
         return false;
      } else if (!canReplace(pos)) {
         return false;
      } else {
         return !EntityUtil.hasEntity(pos);
      }
   }

   public static boolean canReplace(BlockPos pos) {
      return getState(pos).isReplaceable();
   }

   public static BlockState getState(BlockPos pos) {
      return mc.world.getBlockState(pos);
   }

   public static Block getBlock(BlockPos block) {
      return mc.world.getBlockState(block).getBlock();
   }

   static {
      shiftBlocks = Arrays.asList(Blocks.ENDER_CHEST, Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.CRAFTING_TABLE, Blocks.BIRCH_TRAPDOOR, Blocks.BAMBOO_TRAPDOOR, Blocks.DARK_OAK_TRAPDOOR, Blocks.CHERRY_TRAPDOOR, Blocks.ANVIL, Blocks.BREWING_STAND, Blocks.HOPPER, Blocks.DROPPER, Blocks.DISPENSER, Blocks.ACACIA_TRAPDOOR, Blocks.ENCHANTING_TABLE, Blocks.WHITE_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX, Blocks.MAGENTA_SHULKER_BOX, Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.YELLOW_SHULKER_BOX, Blocks.LIME_SHULKER_BOX, Blocks.PINK_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX, Blocks.CYAN_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX, Blocks.BLUE_SHULKER_BOX, Blocks.BROWN_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX, Blocks.RED_SHULKER_BOX, Blocks.BLACK_SHULKER_BOX);
      mineBlocks = Arrays.asList(Blocks.OBSIDIAN, Blocks.ENDER_CHEST, Blocks.ANCIENT_DEBRIS, Blocks.NETHERITE_BLOCK, Blocks.CRYING_OBSIDIAN, Blocks.RESPAWN_ANCHOR);
   }
}
