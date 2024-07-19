// Decompiled with: CFR 0.152
// Class Version: 17
package dev.realme.ash.impl.manager.client;

import dev.realme.ash.Ash;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.macro.Macro;
import dev.realme.ash.impl.event.MouseClickEvent;
import dev.realme.ash.impl.event.keyboard.KeyboardInputEvent;
import dev.realme.ash.util.Globals;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class MacroManager
        implements Globals {
   private final Set<Macro> macros = new HashSet<Macro>();

   public MacroManager() {
      Ash.EVENT_HANDLER.subscribe(this);
   }

   @EventListener
   public void onKeyboardInput(KeyboardInputEvent event) {
      if (MacroManager.mc.player == null || MacroManager.mc.world == null || MacroManager.mc.currentScreen != null) {
         return;
      }
      if (this.macros.isEmpty()) {
         return;
      }
      for (Macro macro : this.macros) {
         if (macro.getKeycode() >= 1000 || event.getAction() != 1 || event.getKeycode() == -1 || event.getKeycode() != macro.getKeycode()) continue;
         macro.runMacro();
      }
   }

   @EventListener
   public void onMouseInput(MouseClickEvent event) {
      if (MacroManager.mc.player == null || MacroManager.mc.world == null || MacroManager.mc.currentScreen != null) {
         return;
      }
      if (this.macros.isEmpty()) {
         return;
      }
      for (Macro macro : this.macros) {
         if (macro.getKeycode() < 1000 || event.getAction() != 1 || event.getButton() == -1 || event.getButton() + 1000 != macro.getKeycode()) continue;
         macro.runMacro();
      }
   }

   public void postInit() {
   }

   public void setMacro(Macro macro, int keycode) {
      Macro m1 = this.getMacro(m -> m.getId().equals(macro.getId()));
      if (m1 != null) {
         m1.setKeycode(keycode);
      }
   }

   public void register(Macro ... macros) {
      for (Macro macro : macros) {
         this.register(macro);
      }
   }

   public void register(Macro macro) {
      this.macros.add(macro);
   }

   public Macro getMacro(Predicate<? super Macro> predicate) {
      return this.macros.stream().filter(predicate).findFirst().orElse(null);
   }

   public Collection<Macro> getMacros() {
      return this.macros;
   }
}
