package dev.realme.ash.mixin.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import io.netty.handler.codec.DecoderException;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({PacketByteBuf.class})
public abstract class MixinPacketByteBuf {
   @Shadow
   public abstract @Nullable NbtElement readNbt(NbtSizeTracker var1);

   @Inject(
      method = {"decode(Lcom/mojang/serialization/DynamicOps;Lcom/mojang/serialization/Codec;Lnet/minecraft/nbt/NbtSizeTracker;)Ljava/lang/Object;"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookDecode(DynamicOps ops, Codec codec, NbtSizeTracker sizeTracker, CallbackInfoReturnable cir) {
      cir.cancel();

      try {
         NbtElement nbtElement = this.readNbt(sizeTracker);
         cir.setReturnValue(Util.getResult(codec.parse(ops, nbtElement), (error) -> new DecoderException("Failed to decode: " + error + " " + nbtElement)));
      } catch (DecoderException var6) {
         cir.setReturnValue(null);
      }

   }
}
