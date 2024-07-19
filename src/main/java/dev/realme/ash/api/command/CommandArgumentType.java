package dev.realme.ash.api.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.realme.ash.init.Managers;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

public class CommandArgumentType implements ArgumentType {
   public static CommandArgumentType command() {
      return new CommandArgumentType();
   }

   public static Command getCommand(CommandContext context, String name) {
      return (Command)context.getArgument(name, Command.class);
   }

   public Command parse(StringReader reader) throws CommandSyntaxException {
      String string = reader.readString();
      Command command = Managers.COMMAND.getCommand(string.toLowerCase());
      if (command == null) {
         throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().createWithContext(reader, (Object)null);
      } else {
         return command;
      }
   }

   public CompletableFuture listSuggestions(CommandContext context, SuggestionsBuilder builder) {
      Iterator var3 = Managers.COMMAND.getCommands().iterator();

      while(var3.hasNext()) {
         Command command = (Command)var3.next();
         builder.suggest(command.getName().toLowerCase());
      }

      return builder.buildFuture();
   }
}
