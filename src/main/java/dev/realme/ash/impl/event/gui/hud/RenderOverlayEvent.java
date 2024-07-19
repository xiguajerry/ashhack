package dev.realme.ash.impl.event.gui.hud;

import dev.realme.ash.api.event.Cancelable;
import dev.realme.ash.api.event.Event;
import net.minecraft.client.gui.DrawContext;

public class RenderOverlayEvent extends Event {
   private final DrawContext context;

   public RenderOverlayEvent(DrawContext context) {
      this.context = context;
   }

   public DrawContext getContext() {
      return this.context;
   }

   @Cancelable
   public static class Frostbite extends RenderOverlayEvent {
      public Frostbite(DrawContext context) {
         super(context);
      }
   }

   @Cancelable
   public static class BossBar extends RenderOverlayEvent {
      public BossBar(DrawContext context) {
         super(context);
      }
   }

   @Cancelable
   public static class Pumpkin extends RenderOverlayEvent {
      public Pumpkin(DrawContext context) {
         super(context);
      }
   }

   @Cancelable
   public static class Spyglass extends RenderOverlayEvent {
      public Spyglass(DrawContext context) {
         super(context);
      }
   }

   @Cancelable
   public static class Block extends RenderOverlayEvent {
      public Block(DrawContext context) {
         super(context);
      }
   }

   @Cancelable
   public static class Water extends RenderOverlayEvent {
      public Water(DrawContext context) {
         super(context);
      }
   }

   @Cancelable
   public static class Fire extends RenderOverlayEvent {
      public Fire(DrawContext context) {
         super(context);
      }
   }

   @Cancelable
   public static class ItemName extends RenderOverlayEvent {
      private int x;
      private int y;

      public ItemName(DrawContext context) {
         super(context);
      }

      public boolean isUpdateXY() {
         return this.x != 0 && this.y != 0;
      }

      public int getX() {
         return this.x;
      }

      public void setX(int x) {
         this.x = x;
      }

      public int getY() {
         return this.y;
      }

      public void setY(int y) {
         this.y = y;
      }
   }

   @Cancelable
   public static class StatusEffect extends RenderOverlayEvent {
      public StatusEffect(DrawContext context) {
         super(context);
      }
   }

   public static class Post extends RenderOverlayEvent {
      private final float tickDelta;

      public Post(DrawContext context, float tickDelta) {
         super(context);
         this.tickDelta = tickDelta;
      }

      public float getTickDelta() {
         return this.tickDelta;
      }
   }
}
