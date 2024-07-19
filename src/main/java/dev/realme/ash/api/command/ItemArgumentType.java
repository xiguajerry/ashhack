package dev.realme.ash.api.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class ItemArgumentType implements ArgumentType {
   public static ItemArgumentType item() {
      return new ItemArgumentType();
   }

   public static Item getItem(CommandContext context, String name) {
      return (Item)context.getArgument(name, Item.class);
   }

   public Item parse(StringReader reader) throws CommandSyntaxException {
      String string = reader.readString();
      Item item = (Item)Registries.ITEM.get(new Identifier("minecraft", string));
      if (item == null) {
         throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().createWithContext(reader, (Object)null);
      } else {
         return item;
      }
   }

   public CompletableFuture listSuggestions(CommandContext context, SuggestionsBuilder builder) {
      Iterator var3 = Registries.ITEM.iterator();

      while(var3.hasNext()) {
         Item item = (Item)var3.next();
         builder.suggest(Registries.ITEM.getId(item).getPath());
      }

      return builder.buildFuture();
   }
}
