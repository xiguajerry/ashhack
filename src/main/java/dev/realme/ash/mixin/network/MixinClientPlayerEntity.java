package dev.realme.ash.mixin.network;

import dev.realme.ash.Ash;
import dev.realme.ash.api.event.EventStage;
import dev.realme.ash.impl.event.RotateEvent;
import dev.realme.ash.impl.event.entity.SwingEvent;
import dev.realme.ash.impl.event.entity.player.PlayerMoveEvent;
import dev.realme.ash.impl.event.network.MountJumpStrengthEvent;
import dev.realme.ash.impl.event.network.MovementPacketsEvent;
import dev.realme.ash.impl.event.network.MovementSlowdownEvent;
import dev.realme.ash.impl.event.network.PlayerTickEvent;
import dev.realme.ash.impl.event.network.PushOutOfBlocksEvent;
import dev.realme.ash.impl.event.network.SetCurrentHandEvent;
import dev.realme.ash.impl.event.network.SprintCancelEvent;
import dev.realme.ash.impl.event.network.TickMovementEvent;
import dev.realme.ash.impl.event.network.UpdateWalkingEvent;
import dev.realme.ash.impl.imixin.IClientPlayerEntity;
import dev.realme.ash.init.Managers;
import dev.realme.ash.init.Modules;
import dev.realme.ash.util.Globals;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   value = {ClientPlayerEntity.class},
   priority = 10000
)
public abstract class MixinClientPlayerEntity extends AbstractClientPlayerEntity implements Globals, IClientPlayerEntity {
   @Shadow
   @Final
   public ClientPlayNetworkHandler networkHandler;
   @Shadow
   public double lastX;
   @Shadow
   public double lastBaseY;
   @Shadow
   public double lastZ;
   @Shadow
   public Input input;
   @Shadow
   @Final
   protected MinecraftClient client;
   @Shadow
   private boolean lastSneaking;
   @Shadow
   private float lastYaw;
   @Shadow
   private float lastPitch;
   @Shadow
   private boolean lastOnGround;
   @Shadow
   private int ticksSinceLastPositionPacketSent;
   @Shadow
   private boolean autoJumpEnabled;
   @Unique
   private boolean ticking;
   @Shadow
   private @Nullable Hand activeHand;

   public MixinClientPlayerEntity() {
      super(MinecraftClient.getInstance().world, MinecraftClient.getInstance().player.getGameProfile());
   }

   @Shadow
   protected abstract void sendSprintingPacket();

   @Shadow
   public abstract boolean isSneaking();

   @Shadow
   protected abstract boolean isCamera();

   @Shadow
   protected abstract void autoJump(float var1, float var2);

   @Shadow
   public abstract void tick();

   @Shadow
   protected abstract void sendMovementPackets();

   @Redirect(
      method = {"updateNausea"},
      at = @At(
   value = "FIELD",
   target = "Lnet/minecraft/client/MinecraftClient;currentScreen:Lnet/minecraft/client/gui/screen/Screen;"
)
   )
   private Screen updateNauseaGetCurrentScreenProxy(MinecraftClient client) {
      return null;
   }

   @Inject(
      method = {"sendMovementPackets"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookSendMovementPackets(CallbackInfo ci) {
      MovementPacketsEvent movementPacketsEvent = new MovementPacketsEvent(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.getYaw(), mc.player.getPitch(), mc.player.isOnGround());
      Ash.EVENT_HANDLER.dispatch(movementPacketsEvent);
      double x = movementPacketsEvent.getX();
      double y = movementPacketsEvent.getY();
      double z = movementPacketsEvent.getZ();
      float yaw = movementPacketsEvent.getYaw();
      float pitch = movementPacketsEvent.getPitch();
      boolean ground = movementPacketsEvent.getOnGround();
      if (movementPacketsEvent.isCanceled()) {
         ci.cancel();
         this.sendSprintingPacket();
         boolean bl = this.isSneaking();
         if (bl != this.lastSneaking) {
            ClientCommandC2SPacket.Mode mode = bl ? Mode.PRESS_SHIFT_KEY : Mode.RELEASE_SHIFT_KEY;
            this.networkHandler.sendPacket(new ClientCommandC2SPacket(this, mode));
            this.lastSneaking = bl;
         }

         if (this.isCamera()) {
            double d = x - this.lastX;
            double e = y - this.lastBaseY;
            double f = z - this.lastZ;
            double g = yaw - this.lastYaw;
            double h = pitch - this.lastPitch;
            ++this.ticksSinceLastPositionPacketSent;
            boolean bl2 = MathHelper.squaredMagnitude(d, e, f) > MathHelper.square(2.0E-4) || this.ticksSinceLastPositionPacketSent >= 20;
            boolean bl3 = g != 0.0 || h != 0.0;
            if (this.hasVehicle()) {
               Vec3d vec3d = this.getVelocity();
               this.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(vec3d.x, -999.0, vec3d.z, this.getYaw(), this.getPitch(), ground));
               bl2 = false;
            } else if (bl2 && bl3) {
               this.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(x, y, z, yaw, pitch, ground));
            } else if (bl2) {
               this.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, ground));
            } else if (bl3) {
               this.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, ground));
            } else if (this.lastOnGround != this.isOnGround()) {
               this.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(ground));
            }

            if (bl2) {
               this.lastX = x;
               this.lastBaseY = y;
               this.lastZ = z;
               this.ticksSinceLastPositionPacketSent = 0;
            }

            if (bl3) {
               this.lastYaw = yaw;
               this.lastPitch = pitch;
            }

            this.lastOnGround = ground;
            this.autoJumpEnabled = this.client.options.getAutoJump().getValue();
         }
      }

   }

   @Inject(
      method = {"tick"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;tick()V",
   shift = Shift.BEFORE,
   ordinal = 0
)}
   )
   private void hookTickPre(CallbackInfo ci) {
      PlayerTickEvent playerTickEvent = new PlayerTickEvent();
      Ash.EVENT_HANDLER.dispatch(playerTickEvent);
   }

   @Inject(
      method = {"sendMovementPackets"},
      at = {@At("HEAD")}
   )
   private void preMotion(CallbackInfo info) {
      UpdateWalkingEvent event = new UpdateWalkingEvent();
      event.setStage(EventStage.PRE);
      Ash.EVENT_HANDLER.dispatch(event);
   }

   @Inject(
      method = {"sendMovementPackets"},
      at = {@At("RETURN")}
   )
   private void postMotion(CallbackInfo info) {
      UpdateWalkingEvent event = new UpdateWalkingEvent();
      event.setStage(EventStage.POST);
      Ash.EVENT_HANDLER.dispatch(event);
   }

   @Inject(
      method = {"tick"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/network/ClientPlayerEntity;sendMovementPackets()V",
   ordinal = 0,
   shift = Shift.AFTER
)}
   )
   private void hookTick(CallbackInfo ci) {
      if (!Modules.BURROW.cancelRotate) {
         float yaw = this.getYaw();
         float pitch = this.getPitch();
         RotateEvent rotateEvent = new RotateEvent(yaw, pitch);
         Ash.EVENT_HANDLER.dispatch(rotateEvent);
         yaw = rotateEvent.getYaw();
         pitch = rotateEvent.getPitch();
         Managers.ROTATION.lastYaw = yaw;
         Managers.ROTATION.lastPitch = pitch;
         Managers.ROTATION.sendLook(new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, this.isOnGround()));
      }

      if (!this.ticking) {
         TickMovementEvent tickMovementEvent = new TickMovementEvent();
         Ash.EVENT_HANDLER.dispatch(tickMovementEvent);
         if (tickMovementEvent.isCanceled()) {
            for(int i = 0; i < tickMovementEvent.getIterations(); ++i) {
               this.ticking = true;
               this.tick();
               this.ticking = false;
               this.sendMovementPackets();
            }
         }

      }
   }

   @Inject(
      method = {"tickMovement"},
      at = {@At(
   value = "FIELD",
   target = "Lnet/minecraft/client/network/ClientPlayerEntity;ticksLeftToDoubleTapSprint:I",
   shift = Shift.AFTER
)}
   )
   private void hookTickMovementPost(CallbackInfo ci) {
      MovementSlowdownEvent movementUpdateEvent = new MovementSlowdownEvent(this.input);
      Ash.EVENT_HANDLER.dispatch(movementUpdateEvent);
   }

   @Inject(
      method = {"move"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookMove(MovementType movementType, Vec3d movement, CallbackInfo ci) {
      PlayerMoveEvent playerMoveEvent = new PlayerMoveEvent(movementType, movement);
      Ash.EVENT_HANDLER.dispatch(playerMoveEvent);
      if (playerMoveEvent.isCanceled()) {
         ci.cancel();
         double d = this.getX();
         double e = this.getZ();
         super.move(movementType, playerMoveEvent.getMovement());
         this.autoJump((float)(this.getX() - d), (float)(this.getZ() - e));
      }

   }

   @Inject(
      method = {"pushOutOfBlocks"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onPushOutOfBlocks(double x, double z, CallbackInfo ci) {
      PushOutOfBlocksEvent pushOutOfBlocksEvent = new PushOutOfBlocksEvent();
      Ash.EVENT_HANDLER.dispatch(pushOutOfBlocksEvent);
      if (pushOutOfBlocksEvent.isCanceled()) {
         ci.cancel();
      }

   }

   @Inject(
      method = {"setCurrentHand"},
      at = {@At("HEAD")}
   )
   private void hookSetCurrentHand(Hand hand, CallbackInfo ci) {
      SetCurrentHandEvent setCurrentHandEvent = new SetCurrentHandEvent(hand);
      Ash.EVENT_HANDLER.dispatch(setCurrentHandEvent);
   }

   @Redirect(
      method = {"tickMovement"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/network/ClientPlayerEntity;setSprinting(Z)V",
   ordinal = 3
)
   )
   private void hookSetSprinting(ClientPlayerEntity instance, boolean b) {
      SprintCancelEvent sprintEvent = new SprintCancelEvent();
      Ash.EVENT_HANDLER.dispatch(sprintEvent);
      if (sprintEvent.isCanceled()) {
         instance.setSprinting(true);
      } else {
         instance.setSprinting(b);
      }

   }

   @Inject(
      method = {"getMountJumpStrength"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookGetMountJumpStrength(CallbackInfoReturnable cir) {
      MountJumpStrengthEvent mountJumpStrengthEvent = new MountJumpStrengthEvent();
      Ash.EVENT_HANDLER.dispatch(mountJumpStrengthEvent);
      if (mountJumpStrengthEvent.isCanceled()) {
         cir.cancel();
         cir.setReturnValue(mountJumpStrengthEvent.getJumpStrength());
      }

   }

   @Inject(
      method = {"swingHand"},
      at = {@At("RETURN")}
   )
   private void hookSwingHand(Hand hand, CallbackInfo ci) {
      SwingEvent swingEvent = new SwingEvent(hand);
      Ash.EVENT_HANDLER.dispatch(swingEvent);
   }

   public float getLastSpoofedYaw() {
      return this.lastYaw;
   }

   public float getLastSpoofedPitch() {
      return this.lastPitch;
   }
}
