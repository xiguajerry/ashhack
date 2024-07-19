package dev.realme.ash.mixin.item;

import dev.realme.ash.Ash;
import dev.realme.ash.impl.event.item.FireworkUseEvent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({FireworkRocketItem.class})
public class MixinFireworkRocketItem {
   @Inject(
      method = {"use"},
      at = {@At("HEAD")}
   )
   private void hookUse(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable cir) {
      FireworkUseEvent fireworkUseEvent = new FireworkUseEvent();
      Ash.EVENT_HANDLER.dispatch(fireworkUseEvent);
   }
}
