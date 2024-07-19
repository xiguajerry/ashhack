package dev.realme.ash.mixin.block;

import dev.realme.ash.Ash;
import dev.realme.ash.impl.event.block.PlaceBlockEvent;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({BlockItem.class})
public class BlockItemMixin {
   @Inject(
      method = {"place(Lnet/minecraft/item/ItemPlacementContext;Lnet/minecraft/block/BlockState;)Z"},
      at = {@At("HEAD")}
   )
   private void onPlace(ItemPlacementContext context, BlockState state, CallbackInfoReturnable info) {
      if (context.getWorld().isClient) {
         Ash.EVENT_HANDLER.dispatch(PlaceBlockEvent.get(context.getBlockPos(), state.getBlock()));
      }

   }
}