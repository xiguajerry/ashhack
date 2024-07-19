package dev.realme.ash.impl.event.render.entity;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.Event;
import net.minecraft.client.network.AbstractClientPlayerEntity;

@Cancelable
public class RenderPlayerEvent extends Event {
   private final AbstractClientPlayerEntity entity;
   private float yaw;
   private float pitch;

   public RenderPlayerEvent(AbstractClientPlayerEntity entity) {
      this.entity = entity;
   }

   public AbstractClientPlayerEntity getEntity() {
      return this.entity;
   }

   public float getYaw() {
      return this.yaw;
   }

   public void setYaw(float yaw) {
      this.yaw = yaw;
   }

   public float getPitch() {
      return this.pitch;
   }

   public void setPitch(float pitch) {
      this.pitch = pitch;
   }
}
