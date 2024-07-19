package dev.realme.ash.mixin.gui.hud;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import dev.realme.ash.Ash;
import dev.realme.ash.impl.event.gui.hud.PlayerListColumnsEvent;
import dev.realme.ash.impl.event.gui.hud.PlayerListEvent;
import dev.realme.ash.impl.event.gui.hud.PlayerListNameEvent;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({PlayerListHud.class})
public abstract class MixinPlayerListHud {
   @Shadow
   @Final
   private static Comparator ENTRY_ORDERING;
   @Shadow
   @Final
   private MinecraftClient client;

   @Shadow
   protected abstract List collectPlayerEntries();

   @Shadow
   protected abstract Text applyGameModeFormatting(PlayerListEntry var1, MutableText var2);

   @Inject(
      method = {"getPlayerName"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookGetPlayerName(PlayerListEntry entry, CallbackInfoReturnable cir) {
      Text text;
      if (entry.getDisplayName() != null) {
         text = this.applyGameModeFormatting(entry, entry.getDisplayName().copy());
      } else {
         text = this.applyGameModeFormatting(entry, Team.decorateName(entry.getScoreboardTeam(), Text.literal(entry.getProfile().getName())));
      }

      PlayerListNameEvent playerListNameEvent = new PlayerListNameEvent(text, entry.getProfile().getId());
      Ash.EVENT_HANDLER.dispatch(playerListNameEvent);
      if (playerListNameEvent.isCanceled()) {
         cir.cancel();
         cir.setReturnValue(playerListNameEvent.getPlayerName());
      }

   }

   @Inject(
      method = {"collectPlayerEntries"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookCollectPlayerEntries(CallbackInfoReturnable cir) {
      PlayerListEvent playerListEvent = new PlayerListEvent();
      Ash.EVENT_HANDLER.dispatch(playerListEvent);
      if (playerListEvent.isCanceled()) {
         cir.cancel();
         cir.setReturnValue(this.client.player.networkHandler.getListedPlayerListEntries().stream().sorted(ENTRY_ORDERING).limit(playerListEvent.getSize()).toList());
      }

   }

   @Inject(
      method = {"render"},
      at = {@At(
   value = "INVOKE",
   target = "Ljava/lang/Math;min(II)I",
   shift = Shift.BEFORE
)}
   )
   private void hookRender(CallbackInfo ci, @Local(ordinal = 5) LocalIntRef o, @Local(ordinal = 6) LocalIntRef p) {
      int newP = 1;
      int newO;
      int totalPlayers = newO = this.collectPlayerEntries().size();
      PlayerListColumnsEvent playerListColumsEvent = new PlayerListColumnsEvent();
      Ash.EVENT_HANDLER.dispatch(playerListColumsEvent);
      if (playerListColumsEvent.isCanceled()) {
         while(true) {
            if (newO <= playerListColumsEvent.getTabHeight()) {
               o.set(newO);
               p.set(newP);
               break;
            }

            ++newP;
            newO = (totalPlayers + newP - 1) / newP;
         }
      }

   }
}
