package dev.realme.ash.api.module;

import dev.realme.ash.init.Managers;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class RotationModule extends ToggleModule {
   private final int rotationPriority;

   public RotationModule(String name, String desc, ModuleCategory category) {
      super(name, desc, category);
      this.rotationPriority = 100;
   }

   public RotationModule(String name, String desc, ModuleCategory category, int rotationPriority) {
      super(name, desc, category);
      this.rotationPriority = rotationPriority;
   }

   public void setRotation(float yaw, float pitch) {
      Managers.ROTATION.sendYawAndPitch(yaw, pitch);
   }

   public void setRotation(PlayerMoveC2SPacket.LookAndOnGround lookAndOnGround) {
      Managers.ROTATION.sendLook(lookAndOnGround);
   }

   public void setRotation(BlockPos pos) {
      Managers.ROTATION.facePos(pos);
   }

   public void setRotation(BlockPos pos, Direction direction) {
      Managers.ROTATION.facePos(pos, direction);
   }

   public void setRotation(Vec3d vec3d) {
      Managers.ROTATION.faceVector(vec3d, false);
   }

   public void setRotation(Vec3d vec3d, boolean sendPacket) {
      Managers.ROTATION.faceVector(vec3d, sendPacket);
   }

   protected void setRotationClient(float yaw, float pitch) {
      Managers.ROTATION.setRotationClient(yaw, pitch);
   }

   protected int getRotationPriority() {
      return this.rotationPriority;
   }
}
