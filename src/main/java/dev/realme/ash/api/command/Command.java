package dev.realme.ash.api.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import dev.realme.ash.init.Managers;
import dev.realme.ash.util.Globals;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.command.CommandSource;

public abstract class Command implements Globals {
   private final String name;
   private final String desc;
   private final LiteralArgumentBuilder<ClientCommandSource> builder;

   public Command(String name, String desc, LiteralArgumentBuilder<ClientCommandSource> builder) {
      this.name = name;
      this.desc = desc;
      this.builder = builder;
   }

   public abstract void buildCommand(LiteralArgumentBuilder<ClientCommandSource> var1);

   protected static LiteralArgumentBuilder<ClientCommandSource> literal(String name) {
      return LiteralArgumentBuilder.literal(name);
   }

   protected static <T> RequiredArgumentBuilder<ClientCommandSource, T> argument(String name, ArgumentType<T> type) {
      return RequiredArgumentBuilder.argument(name, type);
   }

   protected static SuggestionProvider<ClientCommandSource> suggest(String... suggestions) {
      return (context, builder) -> CommandSource.suggestMatching(Lists.newArrayList(suggestions), builder);
   }

   public LiteralArgumentBuilder<ClientCommandSource> getCommandBuilder() {
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
