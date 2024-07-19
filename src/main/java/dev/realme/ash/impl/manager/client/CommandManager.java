package dev.realme.ash.impl.manager.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.realme.ash.Ash;
import dev.realme.ash.api.command.Command;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.Module;
import dev.realme.ash.impl.command.BindCommand;
import dev.realme.ash.impl.command.ConfigCommand;
import dev.realme.ash.impl.command.DisableAllCommand;
import dev.realme.ash.impl.command.DrawnCommand;
import dev.realme.ash.impl.command.FriendCommand;
import dev.realme.ash.impl.command.HClipCommand;
import dev.realme.ash.impl.command.HelpCommand;
import dev.realme.ash.impl.command.HideAllCommand;
import dev.realme.ash.impl.command.ModuleCommand;
import dev.realme.ash.impl.command.ModulesCommand;
import dev.realme.ash.impl.command.NbtCommand;
import dev.realme.ash.impl.command.OpenFolderCommand;
import dev.realme.ash.impl.command.PrefixCommand;
import dev.realme.ash.impl.command.ReloadSoundCommand;
import dev.realme.ash.impl.command.ResetCommand;
import dev.realme.ash.impl.command.ToggleCommand;
import dev.realme.ash.impl.command.VClipCommand;
import dev.realme.ash.impl.command.VanishCommand;
import dev.realme.ash.impl.event.gui.chat.ChatMessageEvent;
import dev.realme.ash.impl.event.keyboard.KeyboardInputEvent;
import dev.realme.ash.init.Managers;
import dev.realme.ash.util.Globals;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.command.CommandSource;

public class CommandManager implements Globals {
   private final List commands = new ArrayList();
   private String prefix = ".";
   private int prefixKey = 46;
   private final CommandDispatcher dispatcher = new CommandDispatcher();
   private final CommandSource source;

   public CommandManager() {
      this.source = new ClientCommandSource((ClientPlayNetworkHandler)null, mc);
      Ash.EVENT_HANDLER.subscribe(this);
      this.register(new BindCommand(), new ConfigCommand(), new DisableAllCommand(), new DrawnCommand(), new FriendCommand(), new HClipCommand(), new HelpCommand(), new HideAllCommand(), new ModulesCommand(), new NbtCommand(), new OpenFolderCommand(), new PrefixCommand(), new ResetCommand(), new ReloadSoundCommand(), new ToggleCommand(), new VanishCommand(), new VClipCommand());
      Iterator var1 = Managers.MODULE.getModules().iterator();

      while(var1.hasNext()) {
         Module module = (Module)var1.next();
         this.register((Command)(new ModuleCommand(module)));
      }

      Ash.info("Registered {} commands!", this.commands.size());
      var1 = this.commands.iterator();

      while(var1.hasNext()) {
         Command command = (Command)var1.next();
         command.buildCommand(command.getCommandBuilder());
         this.dispatcher.register(command.getCommandBuilder());
      }

   }

   @EventListener
   public void onChatMessage(ChatMessageEvent.Client event) {
      String text = event.getMessage().trim();
      if (text.startsWith(this.prefix)) {
         String literal = text.substring(1);
         event.cancel();
         mc.inGameHud.getChatHud().addToMessageHistory(text);

         try {
            this.dispatcher.execute(this.dispatcher.parse(literal, this.source));
         } catch (Exception var5) {
         }
      }

   }

   @EventListener
   public void onKeyboardInput(KeyboardInputEvent event) {
      if (event.getAction() == 1 && event.getKeycode() == this.prefixKey && mc.currentScreen == null) {
         event.cancel();
         mc.setScreen(new ChatScreen(""));
      }

   }

   private LiteralArgumentBuilder redirectBuilder(String alias, LiteralCommandNode destination) {
      LiteralArgumentBuilder literalArgumentBuilder = (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)LiteralArgumentBuilder.literal(alias.toLowerCase()).requires(destination.getRequirement())).forward(destination.getRedirect(), destination.getRedirectModifier(), destination.isFork())).executes(destination.getCommand());
      Iterator var4 = destination.getChildren().iterator();

      while(var4.hasNext()) {
         CommandNode child = (CommandNode)var4.next();
         literalArgumentBuilder.then(child);
      }

      return literalArgumentBuilder;
   }

   private void register(Command... commands) {
      Command[] var2 = commands;
      int var3 = commands.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Command command = var2[var4];
         this.register(command);
      }

   }

   private void register(Command command) {
      this.commands.add(command);
   }

   public List getCommands() {
      return this.commands;
   }

   public Command getCommand(String name) {
      Iterator var2 = this.commands.iterator();

      Command command;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         command = (Command)var2.next();
      } while(!command.getName().equalsIgnoreCase(name));

      return command;
   }

   public String getPrefix() {
      return this.prefix;
   }

   public void setPrefix(String prefix, int prefixKey) {
      this.prefix = prefix;
      this.prefixKey = prefixKey;
   }

   public CommandDispatcher getDispatcher() {
      return this.dispatcher;
   }

   public CommandSource getSource() {
      return this.source;
   }
}
