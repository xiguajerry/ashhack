package dev.realme.ash.mixin;

import dev.realme.ash.Ash;
import dev.realme.ash.api.event.EventStage;
import dev.realme.ash.impl.event.AttackCooldownEvent;
import dev.realme.ash.impl.event.EntityOutlineEvent;
import dev.realme.ash.impl.event.FinishLoadingEvent;
import dev.realme.ash.impl.event.FramerateLimitEvent;
import dev.realme.ash.impl.event.ItemMultitaskEvent;
import dev.realme.ash.impl.event.RunTickEvent;
import dev.realme.ash.impl.event.ScreenOpenEvent;
import dev.realme.ash.impl.event.TickEvent;
import dev.realme.ash.impl.imixin.IMinecraftClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({MinecraftClient.class})
public abstract class MixinMinecraftClient implements IMinecraftClient {
   @Shadow
   public ClientWorld world;
   @Shadow
   public ClientPlayerEntity player;
   @Shadow
   public @Nullable ClientPlayerInteractionManager interactionManager;
   @Shadow
   protected int attackCooldown;
   @Unique
   private boolean leftClick;
   @Unique
   private boolean rightClick;
   @Unique
   private boolean doAttackCalled;
   @Unique
   private boolean doItemUseCalled;

   @Shadow
   protected abstract void doItemUse();

   @Shadow
   protected abstract boolean doAttack();

   public void leftClick() {
      this.leftClick = true;
   }

   public void rightClick() {
      this.rightClick = true;
   }

   @Inject(
      method = {"run"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/MinecraftClient;render(Z)V",
   shift = Shift.BEFORE
)}
   )
   private void hookRun(CallbackInfo ci) {
      RunTickEvent runTickEvent = new RunTickEvent();
      Ash.EVENT_HANDLER.dispatch(runTickEvent);
   }

   @Inject(
      method = {"onInitFinished"},
      at = {@At("RETURN")}
   )
   private void hookOnInitFinished(MinecraftClient.LoadingContext loadingContext, CallbackInfoReturnable cir) {
      FinishLoadingEvent finishLoadingEvent = new FinishLoadingEvent();
      Ash.EVENT_HANDLER.dispatch(finishLoadingEvent);
   }

   @Inject(
      method = {"tick"},
      at = {@At("HEAD")}
   )
   private void hookTickPre(CallbackInfo ci) {
      this.doAttackCalled = false;
      this.doItemUseCalled = false;
      if (this.player != null && this.world != null) {
         TickEvent tickPreEvent = new TickEvent();
         tickPreEvent.setStage(EventStage.PRE);
         Ash.EVENT_HANDLER.dispatch(tickPreEvent);
      }

      if (this.interactionManager != null) {
         if (this.leftClick && !this.doAttackCalled) {
            this.doAttack();
         }

         if (this.rightClick && !this.doItemUseCalled) {
            this.doItemUse();
         }

         this.leftClick = false;
         this.rightClick = false;
      }
   }

   /**
    * @author
    * @reason
    */
   @Overwrite
   private String getWindowTitle() {
      return "原神";
   }

   @Inject(
      method = {"tick"},
      at = {@At("TAIL")}
   )
   private void hookTickPost(CallbackInfo ci) {
      if (this.player != null && this.world != null) {
         TickEvent tickPostEvent = new TickEvent();
         tickPostEvent.setStage(EventStage.POST);
         Ash.EVENT_HANDLER.dispatch(tickPostEvent);
      }

   }

   @Inject(
      method = {"setScreen"},
      at = {@At("TAIL")}
   )
   private void hookSetScreen(Screen screen, CallbackInfo ci) {
      ScreenOpenEvent screenOpenEvent = new ScreenOpenEvent(screen);
      Ash.EVENT_HANDLER.dispatch(screenOpenEvent);
   }

   @Inject(
      method = {"doItemUse"},
      at = {@At("HEAD")}
   )
   private void hookDoItemUse(CallbackInfo ci) {
      this.doItemUseCalled = true;
   }

   @Inject(
      method = {"doAttack"},
      at = {@At("HEAD")}
   )
   private void hookDoAttack(CallbackInfoReturnable cir) {
      this.doAttackCalled = true;
      AttackCooldownEvent attackCooldownEvent = new AttackCooldownEvent();
      Ash.EVENT_HANDLER.dispatch(attackCooldownEvent);
      if (attackCooldownEvent.isCanceled()) {
         this.attackCooldown = 0;
      }

   }

   @Redirect(
      method = {"handleBlockBreaking"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"
)
   )
   private boolean hookIsUsingItem(ClientPlayerEntity instance) {
      ItemMultitaskEvent itemMultitaskEvent = new ItemMultitaskEvent();
      Ash.EVENT_HANDLER.dispatch(itemMultitaskEvent);
      return !itemMultitaskEvent.isCanceled() && instance.isUsingItem();
   }

   @Redirect(
      method = {"doItemUse"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;isBreakingBlock()Z"
)
   )
   private boolean hookIsBreakingBlock(ClientPlayerInteractionManager instance) {
      ItemMultitaskEvent itemMultitaskEvent = new ItemMultitaskEvent();
      Ash.EVENT_HANDLER.dispatch(itemMultitaskEvent);
      return !itemMultitaskEvent.isCanceled() && instance.isBreakingBlock();
   }

   @Inject(
      method = {"getFramerateLimit"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookGetFramerateLimit(CallbackInfoReturnable cir) {
      FramerateLimitEvent framerateLimitEvent = new FramerateLimitEvent();
      Ash.EVENT_HANDLER.dispatch(framerateLimitEvent);
      if (framerateLimitEvent.isCanceled()) {
         cir.cancel();
         cir.setReturnValue(framerateLimitEvent.getFramerateLimit());
      }

   }

   @Inject(
      method = {"hasOutline"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookHasOutline(Entity entity, CallbackInfoReturnable cir) {
      EntityOutlineEvent entityOutlineEvent = new EntityOutlineEvent(entity);
      Ash.EVENT_HANDLER.dispatch(entityOutlineEvent);
      if (entityOutlineEvent.isCanceled()) {
         cir.cancel();
         cir.setReturnValue(true);
      }

   }
}
