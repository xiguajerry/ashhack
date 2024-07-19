package dev.realme.ash.mixin.item;

import dev.realme.ash.Ash;
import dev.realme.ash.impl.event.item.DurabilityEvent;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ItemStack.class})
public abstract class MixinItemStack {
   @Shadow
   public abstract int getDamage();

   @Shadow
   public abstract NbtCompound getOrCreateNbt();

   @Inject(
      method = {"<init>(Lnet/minecraft/item/ItemConvertible;I)V"},
      at = {@At("RETURN")}
   )
   private void hookInitItem(ItemConvertible item, int count, CallbackInfo ci) {
      if (Ash.EVENT_HANDLER != null) {
         DurabilityEvent durabilityEvent = new DurabilityEvent(this.getDamage());
         Ash.EVENT_HANDLER.dispatch(durabilityEvent);
         if (durabilityEvent.isCanceled()) {
            this.getOrCreateNbt().putInt("Damage", durabilityEvent.getDamage());
         }

      }
   }

   @Inject(
      method = {"<init>(Lnet/minecraft/nbt/NbtCompound;)V"},
      at = {@At("RETURN")}
   )
   private void hookInitNbt(NbtCompound nbt, CallbackInfo ci) {
      if (Ash.EVENT_HANDLER != null) {
         DurabilityEvent durabilityEvent = new DurabilityEvent(nbt.getInt("Damage"));
         Ash.EVENT_HANDLER.dispatch(durabilityEvent);
         if (durabilityEvent.isCanceled()) {
            this.getOrCreateNbt().putInt("Damage", durabilityEvent.getDamage());
         }

      }
   }
}
