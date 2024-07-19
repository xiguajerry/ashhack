package dev.realme.ash.util.world;

import com.mojang.authlib.GameProfile;
import dev.realme.ash.util.Globals;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.player.PlayerEntity;

public class FakePlayerEntity extends OtherClientPlayerEntity implements Globals {
   public static final AtomicInteger CURRENT_ID = new AtomicInteger(1000000);
   private final PlayerEntity player;

   public FakePlayerEntity(PlayerEntity player, String name) {
      super(MinecraftClient.getInstance().world, new GameProfile(UUID.fromString("8667ba71-b85a-4004-af54-457a9734eed7"), name));
      this.player = player;
      this.copyPositionAndRotation(player);
      this.prevYaw = this.getYaw();
      this.prevPitch = this.getPitch();
      this.headYaw = player.headYaw;
      this.prevHeadYaw = this.headYaw;
      this.bodyYaw = player.bodyYaw;
      this.prevBodyYaw = this.bodyYaw;
      Byte playerModel = player.getDataTracker().get(PlayerEntity.PLAYER_MODEL_PARTS);
      this.dataTracker.set(PlayerEntity.PLAYER_MODEL_PARTS, playerModel);
      this.getAttributes().setFrom(player.getAttributes());
      this.setPose(player.getPose());
      this.setHealth(player.getHealth());
      this.setAbsorptionAmount(player.getAbsorptionAmount());
      this.getInventory().clone(player.getInventory());
      this.setId(CURRENT_ID.incrementAndGet());
      this.age = 100;
   }

   public FakePlayerEntity(PlayerEntity player, GameProfile profile) {
      super(MinecraftClient.getInstance().world, profile);
      this.player = player;
      this.copyPositionAndRotation(player);
      this.prevYaw = this.getYaw();
      this.prevPitch = this.getPitch();
      this.headYaw = player.headYaw;
      this.prevHeadYaw = this.headYaw;
      this.bodyYaw = player.bodyYaw;
      this.prevBodyYaw = this.bodyYaw;
      Byte playerModel = player.getDataTracker().get(PlayerEntity.PLAYER_MODEL_PARTS);
      this.dataTracker.set(PlayerEntity.PLAYER_MODEL_PARTS, playerModel);
      this.getAttributes().setFrom(player.getAttributes());
      this.setPose(player.getPose());
      this.setHealth(player.getHealth());
      this.setAbsorptionAmount(player.getAbsorptionAmount());
      this.getInventory().clone(player.getInventory());
      this.setId(CURRENT_ID.incrementAndGet());
      this.age = 100;
   }

   public FakePlayerEntity(PlayerEntity player) {
      this(player, player.getName().getString());
   }

   public void spawnPlayer() {
      if (mc.world != null) {
         this.unsetRemoved();
         mc.world.addEntity(this);
      }

   }

   public void despawnPlayer() {
      if (mc.world != null) {
         mc.world.removeEntity(this.getId(), RemovalReason.DISCARDED);
         this.setRemoved(RemovalReason.DISCARDED);
      }

   }

   public boolean isDead() {
      return false;
   }

   public PlayerEntity getPlayer() {
      return this.player;
   }
}
