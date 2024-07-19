package dev.realme.ash.impl.manager.combat.hole;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;

public class Hole implements Position {
   private final List holeOffsets;
   private final BlockPos origin;
   private final HoleType safety;

   public Hole(BlockPos origin, HoleType safety, BlockPos... holeOffsets) {
      this.origin = origin;
      this.safety = safety;
      this.holeOffsets = Lists.newArrayList(holeOffsets);
      this.holeOffsets.add(origin);
   }

   public double squaredDistanceTo(Entity entity) {
      return entity.getEyePos().squaredDistanceTo(this.getCenter());
   }

   public boolean isStandard() {
      return this.holeOffsets.size() == 5;
   }

   public boolean isDouble() {
      return this.holeOffsets.size() == 8;
   }

   public boolean isDoubleX() {
      return this.isDouble() && this.holeOffsets.contains(this.origin.add(2, 0, 0));
   }

   public boolean isDoubleZ() {
      return this.isDouble() && this.holeOffsets.contains(this.origin.add(0, 0, 2));
   }

   public boolean isQuad() {
      return this.holeOffsets.size() == 12;
   }

   public HoleType getSafety() {
      return this.safety;
   }

   public BlockPos getPos() {
      return this.origin;
   }

   public List getHoleOffsets() {
      return this.holeOffsets;
   }

   public boolean addHoleOffsets(BlockPos... off) {
      return this.holeOffsets.addAll(Arrays.asList(off));
   }

   public Vec3d getCenter() {
      BlockPos center;
      if (this.isDoubleX()) {
         center = this.origin.add(1, 0, 0);
      } else if (this.isDoubleZ()) {
         center = this.origin.add(0, 0, -1);
      } else {
         if (!this.isQuad()) {
            return this.origin.toCenterPos();
         }

         center = this.origin.add(1, 0, -1);
      }

      return Vec3d.of(center);
   }

   public double getX() {
      return this.origin.getX();
   }

   public double getY() {
      return this.origin.getY();
   }

   public double getZ() {
      return this.origin.getZ();
   }
}
