package dev.realme.ash.api.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.realme.ash.init.Managers;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

public class CommandArgumentType implements ArgumentType<Command> {
   public static CommandArgumentType command() {
      return new CommandArgumentType();
   }

   public static Command getCommand(CommandContext context, String name) {
      return (Command)context.getArgument(name, Command.class);
   }

   @Override
   public Command parse(StringReader reader) throws CommandSyntaxException {
      String string = reader.readString();
      Command command = Managers.COMMAND.getCommand(string.toLowerCase());
      if (command == null) {
         throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().createWithContext(reader, null);
      } else {
         return command;
      }
   }

   public CompletableFuture listSuggestions(CommandContext context, SuggestionsBuilder builder) {

       for (Object o : Managers.COMMAND.getCommands()) {
           Command command = (Command) o;
           builder.suggest(command.getName().toLowerCase());
       }

      return builder.buildFuture();
   }
}
