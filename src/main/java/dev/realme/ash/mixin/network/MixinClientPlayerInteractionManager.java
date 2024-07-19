package dev.realme.ash.mixin.network;

import dev.realme.ash.Ash;
import dev.realme.ash.impl.event.network.AttackBlockEvent;
import dev.realme.ash.impl.event.network.BreakBlockEvent;
import dev.realme.ash.impl.event.network.InteractBlockEvent;
import dev.realme.ash.impl.event.network.ReachEvent;
import dev.realme.ash.init.Modules;
import dev.realme.ash.util.Globals;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ClientPlayerInteractionManager.class})
public abstract class MixinClientPlayerInteractionManager implements Globals {
   @Shadow
   private GameMode gameMode;

   @Shadow
   protected abstract void syncSelectedSlot();

   @Shadow
   protected abstract void sendSequencedPacket(ClientWorld var1, SequencedPacketCreator var2);

   @Inject(
      method = {"attackBlock"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookAttackBlock(BlockPos pos, Direction direction, CallbackInfoReturnable cir) {
      BlockState state = mc.world.getBlockState(pos);
      AttackBlockEvent attackBlockEvent = new AttackBlockEvent(pos, state, direction);
      Ash.EVENT_HANDLER.dispatch(attackBlockEvent);
      if (attackBlockEvent.isCanceled()) {
         cir.cancel();
      }

   }

   @Inject(
      method = {"getReachDistance"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookGetReachDistance(CallbackInfoReturnable cir) {
      ReachEvent reachEvent = new ReachEvent();
      Ash.EVENT_HANDLER.dispatch(reachEvent);
      if (reachEvent.isCanceled()) {
         cir.cancel();
         float reach = this.gameMode.isCreative() ? 5.0F : 4.5F;
         cir.setReturnValue(reach + reachEvent.getReach());
      }

   }

   @Inject(
      method = {"interactBlock"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookInteractBlock(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable cir) {
      InteractBlockEvent interactBlockEvent = new InteractBlockEvent(player, hand, hitResult);
      Ash.EVENT_HANDLER.dispatch(interactBlockEvent);
      if (interactBlockEvent.isCanceled()) {
         cir.setReturnValue(ActionResult.SUCCESS);
         cir.cancel();
      }

   }

   @Inject(
      method = {"breakBlock"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookBreakBlock(BlockPos pos, CallbackInfoReturnable cir) {
      BreakBlockEvent breakBlockEvent = new BreakBlockEvent(pos);
      Ash.EVENT_HANDLER.dispatch(breakBlockEvent);
      if (breakBlockEvent.isCanceled()) {
         cir.setReturnValue(false);
         cir.cancel();
      }

   }

   @Inject(
      method = {"interactItem"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void hookInteractItem(PlayerEntity player, Hand hand, CallbackInfoReturnable cir) {
      cir.cancel();
      if (this.gameMode == GameMode.SPECTATOR) {
         cir.setReturnValue(ActionResult.PASS);
      }

      this.syncSelectedSlot();
      float yaw = mc.player.getYaw();
      float pitch = mc.player.getPitch();
      if (!Modules.NO_SLOW.isEnabled() || !Modules.NO_SLOW.getStrafeFix()) {
         mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(player.getX(), player.getY(), player.getZ(), yaw, pitch, player.isOnGround()));
      }

      MutableObject mutableObject = new MutableObject();
      this.sendSequencedPacket(mc.world, (sequence) -> {
         PlayerInteractItemC2SPacket playerInteractItemC2SPacket = new PlayerInteractItemC2SPacket(hand, sequence);
         ItemStack itemStack = player.getStackInHand(hand);
         if (player.getItemCooldownManager().isCoolingDown(itemStack.getItem())) {
            mutableObject.setValue(ActionResult.PASS);
            return playerInteractItemC2SPacket;
         } else {
            TypedActionResult typedActionResult = itemStack.use(mc.world, player, hand);
            ItemStack itemStack2 = (ItemStack)typedActionResult.getValue();
            if (itemStack2 != itemStack) {
               player.setStackInHand(hand, itemStack2);
            }

            mutableObject.setValue(typedActionResult.getResult());
            return playerInteractItemC2SPacket;
         }
      });
      cir.setReturnValue((ActionResult)mutableObject.getValue());
   }
}
