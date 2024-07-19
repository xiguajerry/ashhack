package dev.realme.ash.init;

import dev.realme.ash.impl.manager.ModuleManager;
import dev.realme.ash.impl.manager.anticheat.AntiCheatManager;
import dev.realme.ash.impl.manager.client.AccountManager;
import dev.realme.ash.impl.manager.client.CapeManager;
import dev.realme.ash.impl.manager.client.CommandManager;
import dev.realme.ash.impl.manager.client.MacroManager;
import dev.realme.ash.impl.manager.client.SocialManager;
import dev.realme.ash.impl.manager.combat.BreakManager;
import dev.realme.ash.impl.manager.combat.TotemManager;
import dev.realme.ash.impl.manager.combat.hole.HoleManager;
import dev.realme.ash.impl.manager.network.NetworkManager;
import dev.realme.ash.impl.manager.player.MovementManager;
import dev.realme.ash.impl.manager.player.PositionManager;
import dev.realme.ash.impl.manager.player.interaction.InteractionManager;
import dev.realme.ash.impl.manager.player.rotation.RotationManager;
import dev.realme.ash.impl.manager.tick.TickManager;

public class Managers {
   public static NetworkManager NETWORK;
   public static ModuleManager MODULE;
   public static MacroManager MACRO;
   public static CommandManager COMMAND;
   public static SocialManager SOCIAL;
   public static AccountManager ACCOUNT;
   public static TickManager TICK;
   public static PositionManager POSITION;
   public static RotationManager ROTATION;
   public static AntiCheatManager ANTICHEAT;
   public static MovementManager MOVEMENT;
   public static HoleManager HOLE;
   public static TotemManager TOTEM;
   public static BreakManager BREAK;
   public static InteractionManager INTERACT;
   public static CapeManager CAPES;
   private static boolean initialized;

   public static void init() {
      if (!isInitialized()) {
         NETWORK = new NetworkManager();
         MODULE = new ModuleManager();
         MACRO = new MacroManager();
         SOCIAL = new SocialManager();
         ACCOUNT = new AccountManager();
         TICK = new TickManager();
         POSITION = new PositionManager();
         ROTATION = new RotationManager();
         ANTICHEAT = new AntiCheatManager();
         MOVEMENT = new MovementManager();
         HOLE = new HoleManager();
         TOTEM = new TotemManager();
         BREAK = new BreakManager();
         INTERACT = new InteractionManager();
         COMMAND = new CommandManager();
         initialized = true;
      }

   }

   public static void postInit() {
      if (isInitialized()) {
         MODULE.postInit();
         MACRO.postInit();
         ACCOUNT.postInit();
         CAPES = new CapeManager();
      }

   }

   public static boolean isInitialized() {
      return initialized;
   }
}
