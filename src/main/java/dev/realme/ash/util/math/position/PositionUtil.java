package dev.realme.ash.util.math.position;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class PositionUtil {
   public static List getAllInBox(Box box, BlockPos pos) {
      List intersections = new ArrayList();

      for(int x = (int)Math.floor(box.minX); (double)x < Math.ceil(box.maxX); ++x) {
         for(int z = (int)Math.floor(box.minZ); (double)z < Math.ceil(box.maxZ); ++z) {
            intersections.add(new BlockPos(x, pos.getY(), z));
         }
      }

      return intersections;
   }

   public static List getAllInBox(Box box) {
      List intersections = new ArrayList();

      for(int x = (int)Math.floor(box.minX); (double)x < Math.ceil(box.maxX); ++x) {
         for(int y = (int)Math.floor(box.minY); (double)y < Math.ceil(box.maxY); ++y) {
            for(int z = (int)Math.floor(box.minZ); (double)z < Math.ceil(box.maxZ); ++z) {
               intersections.add(new BlockPos(x, y, z));
            }
         }
      }

      return intersections;
   }
}
