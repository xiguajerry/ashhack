package dev.realme.ash.impl.manager.player.rotation;

import dev.realme.ash.Ash;
import dev.realme.ash.api.event.EventStage;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.Module;
import dev.realme.ash.impl.event.RotateEvent;
import dev.realme.ash.impl.event.entity.UpdateVelocityEvent;
import dev.realme.ash.impl.event.entity.player.PlayerJumpEvent;
import dev.realme.ash.impl.event.keyboard.KeyboardTickEvent;
import dev.realme.ash.impl.event.network.PacketEvent;
import dev.realme.ash.impl.event.network.UpdateWalkingEvent;
import dev.realme.ash.init.Managers;
import dev.realme.ash.init.Modules;
import dev.realme.ash.mixin.accessor.IPlayerMoveC2SPacket;
import dev.realme.ash.util.Globals;
import dev.realme.ash.util.math.timer.CacheTimer;
import dev.realme.ash.util.math.timer.Timer;
import dev.realme.ash.util.world.EntityUtil;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RotationManager implements Globals {
   public float preYaw = 0.0F;
   public float prePitch = 0.0F;
   public static final Timer ROTATE_TIMER = new CacheTimer();
   public static Vec3d directionVec = null;
   private static float renderPitch;
   private static float renderYawOffset;
   private static float prevPitch;
   private static float prevRenderYawOffset;
   private static float prevRotationYawHead;
   private static float rotationYawHead;
   private int ticksExisted;
   public float lastYaw = 0.0F;
   public float lastPitch = 0.0F;
   public boolean rotating = false;
   float prevJumpYaw;

   public RotationManager() {
      Ash.EVENT_HANDLER.subscribe(this);
   }

   @EventListener(
      priority = 101
   )
   public void onUpdateWalking(UpdateWalkingEvent event) {
      if (mc.player != null) {
         if (event.getStage() == EventStage.PRE) {
            this.preYaw = mc.player.getYaw();
            this.prePitch = mc.player.getPitch();
            RotateEvent rotateEvent = new RotateEvent(this.preYaw, this.prePitch);
            Ash.EVENT_HANDLER.dispatch(rotateEvent);
            mc.player.setYaw(rotateEvent.getYaw());
            this.setYaw(rotateEvent.getYaw());
            mc.player.setPitch(rotateEvent.getPitch());
         } else if (event.getStage() == EventStage.POST) {
            mc.player.setYaw(this.preYaw);
            this.setYaw(this.preYaw);
            mc.player.setPitch(this.prePitch);
         }

      }
   }

   @EventListener(
      priority = 101
   )
   public void onRotation(RotateEvent event) {
      if (mc.player != null) {
         if (directionVec != null && !ROTATE_TIMER.passed((long)((Float)Modules.COMBAT_SETTING.rotateTime.getValue() * 1000.0F))) {
            float[] angle = EntityUtil.getLegitRotations(directionVec);
            this.rotating = true;
            event.setYaw(angle[0]);
            this.setYaw(angle[0]);
            event.setPitch(angle[1]);
            this.rotating = false;
         }

      }
   }

   @EventListener(
      priority = -200
   )
   public void onPacketSend(PacketEvent.Send event) {
      if (!event.isCanceled()) {
         PlayerMoveC2SPacket packet;
         Packet var3;
         float pitch;
         float yaw;
         if (directionVec != null && !ROTATE_TIMER.passed((long)((Float)Modules.COMBAT_SETTING.rotateTime.getValue() * 1000.0F)) && !this.rotating) {
            var3 = event.getPacket();
            if (var3 instanceof PlayerMoveC2SPacket) {
               packet = (PlayerMoveC2SPacket)var3;
               if (!packet.changesLook()) {
                  return;
               }

               yaw = packet.getYaw(114514.0F);
               pitch = packet.getPitch(114514.0F);
               if (yaw == mc.player.getYaw() && pitch == mc.player.getPitch()) {
                  float[] angle = EntityUtil.getLegitRotations(directionVec);
                  this.setYaw(angle[0]);
                  ((IPlayerMoveC2SPacket)event.getPacket()).setYaw(angle[0]);
                  ((IPlayerMoveC2SPacket)event.getPacket()).setPitch(angle[1]);
               }
            }
         }

         var3 = event.getPacket();
         if (var3 instanceof PlayerMoveC2SPacket) {
            packet = (PlayerMoveC2SPacket)var3;
            yaw = packet.getYaw(114514.0F);
            pitch = packet.getPitch(114514.0F);
            if (yaw == 114514.0F || pitch == 114514.0F) {
               return;
            }

            this.lastYaw = yaw;
            this.lastPitch = pitch;
            this.set(this.lastYaw, this.lastPitch);
         }

      }
   }

   @EventListener(
      priority = 100
   )
   public void onReceivePacket(PacketEvent.Receive event) {
      Packet var3 = event.getPacket();
      if (var3 instanceof PlayerPositionLookS2CPacket packet) {
         this.lastYaw = packet.getYaw();
         this.lastPitch = packet.getPitch();
         this.set(packet.getYaw(), packet.getPitch());
      }

   }

   @EventListener
   public void onUpdateWalkingPre(UpdateWalkingEvent event) {
      if (event.getStage() == EventStage.POST) {
         this.set(this.lastYaw, this.lastPitch);
      }

   }

   @EventListener
   public void onKeyboardTick(KeyboardTickEvent event) {
      if (directionVec != null && mc.player != null && (Boolean)Modules.COMBAT_SETTING.movementFix.getValue()) {
         float mF = mc.player.input.movementForward;
         float mS = mc.player.input.movementSideways;
         float delta = (mc.player.getYaw() - this.lastYaw) * 0.017453292F;
         float cos = MathHelper.cos(delta);
         float sin = MathHelper.sin(delta);
         mc.player.input.movementSideways = (float)Math.round(mS * cos - mF * sin);
         mc.player.input.movementForward = (float)Math.round(mF * cos + mS * sin);
      }

   }

   @EventListener
   public void onUpdateVelocity(UpdateVelocityEvent event) {
      if (directionVec != null && (Boolean)Modules.COMBAT_SETTING.movementFix.getValue()) {
         event.cancel();
         event.setVelocity(this.movementInputToVelocity(this.lastYaw, event.getMovementInput(), event.getSpeed()));
      }

   }

   @EventListener
   public void onPlayerJump(PlayerJumpEvent event) {
      if (directionVec != null && (Boolean)Modules.COMBAT_SETTING.movementFix.getValue()) {
         if (event.getStage() == EventStage.PRE) {
            this.prevJumpYaw = mc.player.getYaw();
            mc.player.setYaw(this.lastYaw);
         } else {
            mc.player.setYaw(this.prevJumpYaw);
         }
      }

   }

   private Vec3d movementInputToVelocity(float yaw, Vec3d movementInput, float speed) {
      double d = movementInput.lengthSquared();
      if (d < 1.0E-7) {
         return Vec3d.ZERO;
      } else {
         Vec3d vec3d = (d > 1.0 ? movementInput.normalize() : movementInput).multiply((double)speed);
         float f = MathHelper.sin(yaw * 0.017453292F);
         float g = MathHelper.cos(yaw * 0.017453292F);
         return new Vec3d(vec3d.x * (double)g - vec3d.z * (double)f, vec3d.y, vec3d.z * (double)g + vec3d.x * (double)f);
      }
   }

   public float getWrappedYaw() {
      return MathHelper.wrapDegrees(this.lastYaw);
   }

   private void setYaw(float yaw) {
      mc.player.headYaw = yaw;
      mc.player.bodyYaw = yaw;
   }

   private void set(float yaw, float pitch) {
      if (!Module.nullCheck()) {
         if (mc.player.age != this.ticksExisted) {
            this.ticksExisted = mc.player.age;
            prevPitch = renderPitch;
            prevRenderYawOffset = renderYawOffset;
            renderYawOffset = this.getRenderYawOffset(yaw, prevRenderYawOffset);
            prevRotationYawHead = rotationYawHead;
            rotationYawHead = yaw;
            renderPitch = pitch;
         }
      }
   }

   public static float getRenderPitch() {
      return renderPitch;
   }

   public static float getRotationYawHead() {
      return rotationYawHead;
   }

   public static float getRenderYawOffset() {
      return renderYawOffset;
   }

   public static float getPrevPitch() {
      return prevPitch;
   }

   public static float getPrevRotationYawHead() {
      return prevRotationYawHead;
   }

   public static float getPrevRenderYawOffset() {
      return prevRenderYawOffset;
   }

   private float getRenderYawOffset(float yaw, float offsetIn) {
      assert mc.player != null;

      float result = offsetIn;
      double xDif = mc.player.getX() - mc.player.prevX;
      float offset;
      double zDif;
      if (xDif * xDif + (zDif = mc.player.getZ() - mc.player.prevZ) * zDif > 0.002500000176951289) {
         offset = (float)MathHelper.atan2(zDif, xDif) * 57.295776F - 90.0F;
         float wrap = MathHelper.abs(MathHelper.wrapDegrees(yaw) - offset);
         result = 95.0F < wrap && wrap < 265.0F ? offset - 180.0F : offset;
      }

      if (mc.player.handSwingProgress > 0.0F) {
         result = yaw;
      }

      if ((offset = MathHelper.wrapDegrees(yaw - (offsetIn + MathHelper.wrapDegrees(result - offsetIn) * 0.3F))) < -75.0F) {
         offset = -75.0F;
      } else if (offset >= 75.0F) {
         offset = 75.0F;
      }

      result = yaw - offset;
      if (offset * offset > 2500.0F) {
         result += offset * 0.2F;
      }

      return result;
   }

   public void setRotationClient(float yaw, float pitch) {
      if (mc.player != null) {
         mc.player.setYaw(yaw);
         mc.player.setPitch(pitch);
      }
   }

   public void facePos(BlockPos pos, Direction side) {
      if (pos != null && side != null) {
         Vec3d hitVec = pos.toCenterPos().add(new Vec3d((double)side.getVector().getX() * 0.5, (double)side.getVector().getY() * 0.5, (double)side.getVector().getZ() * 0.5));
         this.faceVector(hitVec, false);
      }
   }

   public void facePos(BlockPos pos) {
      if (pos != null) {
         this.faceVector(pos.toCenterPos(), false);
      }
   }

   public void sendYawAndPitch(float yaw, float pitch) {
      this.sendLook(new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, Managers.POSITION.isOnGround()));
   }

   public void faceVector(Vec3d directionVec, boolean sendPacket) {
      if (directionVec != null) {
         float[] angle = EntityUtil.getLegitRotations(directionVec);
         RotationManager.directionVec = directionVec;
         ROTATE_TIMER.reset();
         if (angle[0] != this.lastYaw || angle[1] != this.lastPitch) {
            if (sendPacket) {
               this.sendLook(new PlayerMoveC2SPacket.LookAndOnGround(angle[0], angle[1], Managers.POSITION.isOnGround()));
            }

         }
      }
   }

   public void sendLook(PlayerMoveC2SPacket.LookAndOnGround lookAndOnGround) {
      if (lookAndOnGround.getYaw(114514.0F) != Managers.ROTATION.lastYaw || lookAndOnGround.getPitch(114514.0F) != Managers.ROTATION.lastPitch) {
         this.rotating = true;
         Managers.NETWORK.sendPacket(lookAndOnGround);
         this.rotating = false;
      }
   }
}
