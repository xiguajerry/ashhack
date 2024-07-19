package dev.realme.ash.impl.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.realme.ash.api.command.Command;
import dev.realme.ash.mixin.accessor.AccessorEntity;
import dev.realme.ash.util.chat.ChatUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Entity.RemovalReason;

public class VanishCommand extends Command {
   private Entity mount;

   public VanishCommand() {
      super("Vanish", "Desyncs the riding entity", literal("vanish"));
   }

   public void buildCommand(LiteralArgumentBuilder builder) {
      builder.then(argument("mount", StringArgumentType.string()).suggests(suggest("mount", "remount")).executes((c) -> {
         String dismount = StringArgumentType.getString(c, "mount");
         if (dismount.equalsIgnoreCase("dismount")) {
            if (mc.player.isRiding() && mc.player.getVehicle() != null) {
               if (this.mount != null) {
                  ChatUtil.error("Entity vanished, must remount before mounting!");
                  return 0;
               }

               this.mount = mc.player.getVehicle();
               mc.player.dismountVehicle();
               mc.world.removeEntity(this.mount.getId(), RemovalReason.DISCARDED);
            }
         } else if (dismount.equalsIgnoreCase("remount")) {
            if (this.mount == null) {
               ChatUtil.error("No vanished entity!");
               return 0;
            }

            ((AccessorEntity)this.mount).hookUnsetRemoved();
            mc.world.addEntity(this.mount);
            mc.player.startRiding(this.mount, true);
            this.mount = null;
         }

         return 1;
      })).executes((c) -> {
         ChatUtil.error("Invalid usage! Usage: " + this.getUsage());
         return 1;
      });
   }
}
