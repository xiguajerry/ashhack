package dev.realme.ash.mixin.world;

import dev.realme.ash.util.Globals;
import dev.realme.ash.util.math.DamageUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({World.class})
public abstract class MixinWorld {
   @Inject(
      method = {"getBlockState"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void blockStateHook(BlockPos pos, CallbackInfoReturnable cir) {
      if (Globals.mc.player != null && Globals.mc.world != null && Globals.mc.world.isInBuildLimit(pos) && DamageUtil.terrainIgnore) {
         WorldChunk worldChunk = Globals.mc.world.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
         BlockState tempState = worldChunk.getBlockState(pos);
         if (tempState.getBlock() == Blocks.OBSIDIAN || tempState.getBlock() == Blocks.BEDROCK || tempState.getBlock() == Blocks.ENDER_CHEST || tempState.getBlock() == Blocks.RESPAWN_ANCHOR) {
            return;
         }

         cir.setReturnValue(Blocks.AIR.getDefaultState());
      }

   }
}
