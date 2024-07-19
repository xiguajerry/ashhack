package dev.realme.ash.api.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.realme.ash.init.Managers;
import dev.realme.ash.util.Globals;
import net.minecraft.command.CommandSource;

public abstract class Command implements Globals {
   private final String name;
   private final String desc;
   private final LiteralArgumentBuilder builder;

   public Command(String name, String desc, LiteralArgumentBuilder builder) {
      this.name = name;
      this.desc = desc;
      this.builder = builder;
   }

   public abstract void buildCommand(LiteralArgumentBuilder var1);

   protected static LiteralArgumentBuilder literal(String name) {
      return LiteralArgumentBuilder.literal(name);
   }

   protected static RequiredArgumentBuilder argument(String name, ArgumentType type) {
      return RequiredArgumentBuilder.argument(name, type);
   }

   protected static SuggestionProvider suggest(String... suggestions) {
      return (context, builder) -> {
         return CommandSource.suggestMatching(Lists.newArrayList(suggestions), builder);
      };
   }

   public LiteralArgumentBuilder getCommandBuilder() {
      return this.builder;
   }

   public String getName() {
      return this.name;
   }

   public String getDescription() {
      return this.desc;
   }

   public String getUsage() {
      return Managers.COMMAND.getDispatcher().getAllUsage(this.builder.build(), Managers.COMMAND.getSource(), false)[0];
   }
}
