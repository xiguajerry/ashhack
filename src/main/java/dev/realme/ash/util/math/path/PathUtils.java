package dev.realme.ash.util.math.path;

import dev.realme.ash.util.Globals;
import dev.realme.ash.util.world.BlockPosX;
import java.util.ArrayList;
import java.util.Iterator;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.WallSignBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;

public class PathUtils implements Globals {
   private static boolean canPassThrough(BlockPos pos) {
      Block block = mc.world.getBlockState(new BlockPos(pos.getX(), pos.getY(), pos.getZ())).getBlock();
      return block == Blocks.AIR || block instanceof PlantBlock || block == Blocks.VINE || block == Blocks.LADDER || block == Blocks.WATER || block == Blocks.WATER_CAULDRON || block instanceof WallSignBlock;
   }

   public static ArrayList computePath(LivingEntity fromEntity, LivingEntity toEntity) {
      return computePath(new Vec3(fromEntity.getX(), fromEntity.getY(), fromEntity.getZ()), new Vec3(toEntity.getX(), toEntity.getY(), toEntity.getZ()));
   }

   public static ArrayList computePath(Vec3 topFrom, Vec3 to) {
      if (!canPassThrough(new BlockPosX(topFrom.mc()))) {
         topFrom = topFrom.addVector(0.0, 1.0, 0.0);
      }

      AStarCustomPathFinder pathfinder = new AStarCustomPathFinder(topFrom, to);
      pathfinder.compute();
      int i = 0;
      Vec3 lastLoc = null;
      Vec3 lastDashLoc = null;
      ArrayList path = new ArrayList();
      ArrayList pathFinderPath = pathfinder.getPath();

      for(Iterator var8 = pathFinderPath.iterator(); var8.hasNext(); ++i) {
         Vec3 pathElm = (Vec3)var8.next();
         if (i != 0 && i != pathFinderPath.size() - 1) {
            boolean canContinue = true;
            if (pathElm.squareDistanceTo(lastDashLoc) > 25.0) {
               canContinue = false;
            } else {
               double smallX = Math.min(lastDashLoc.getX(), pathElm.getX());
               double smallY = Math.min(lastDashLoc.getY(), pathElm.getY());
               double smallZ = Math.min(lastDashLoc.getZ(), pathElm.getZ());
               double bigX = Math.max(lastDashLoc.getX(), pathElm.getX());
               double bigY = Math.max(lastDashLoc.getY(), pathElm.getY());
               double bigZ = Math.max(lastDashLoc.getZ(), pathElm.getZ());

               label55:
               for(int x = (int)smallX; (double)x <= bigX; ++x) {
                  for(int y = (int)smallY; (double)y <= bigY; ++y) {
                     for(int z = (int)smallZ; (double)z <= bigZ; ++z) {
                        if (!AStarCustomPathFinder.checkPositionValidity(x, y, z, false)) {
                           canContinue = false;
                           break label55;
                        }
                     }
                  }
               }
            }

            if (!canContinue) {
               path.add(lastLoc.addVector(0.5, 0.0, 0.5));
               lastDashLoc = lastLoc;
            }
         } else {
            if (lastLoc != null) {
               path.add(lastLoc.addVector(0.5, 0.0, 0.5));
            }

            path.add(pathElm.addVector(0.5, 0.0, 0.5));
            lastDashLoc = pathElm;
         }

         lastLoc = pathElm;
      }

      return path;
   }
}
