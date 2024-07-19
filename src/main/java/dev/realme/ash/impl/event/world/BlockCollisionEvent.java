package dev.realme.ash.impl.event.world;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.Event;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;

@Cancelable
public class BlockCollisionEvent extends Event {
   private final BlockPos pos;
   private final BlockState state;
   private VoxelShape voxelShape;

   public BlockCollisionEvent(VoxelShape voxelShape, BlockPos pos, BlockState state) {
      this.pos = pos;
      this.state = state;
      this.voxelShape = voxelShape;
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public BlockState getState() {
      return this.state;
   }

   public Block getBlock() {
      return this.state.getBlock();
   }

   public VoxelShape getVoxelShape() {
      return this.voxelShape;
   }

   public void setVoxelShape(VoxelShape voxelShape) {
      this.voxelShape = voxelShape;
   }
}
