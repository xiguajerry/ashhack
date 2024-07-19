package dev.realme.ash.impl.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.realme.ash.api.command.Command;
import dev.realme.ash.api.command.CommandArgumentType;
import dev.realme.ash.init.Managers;
import dev.realme.ash.util.chat.ChatUtil;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.command.CommandSource;

import java.util.Iterator;

public class HelpCommand extends Command {
    public HelpCommand() {
        super("Help", "Displays command functionality", literal("help"));
    }

    private static String toHelpMessage(Command command) {
        return String.format("%s %s- %s", command.getName(), command.getUsage(), command.getDescription());
    }

    public void buildCommand(LiteralArgumentBuilder<ClientCommandSource> builder) {
        builder.then( argument("command", CommandArgumentType.command())
                        .executes(c -> {
                            Command command = CommandArgumentType.getCommand(c, "command");
                            ChatUtil.clientSendMessage(toHelpMessage(command));
                            return 1;
                        })
                )
                .executes((c) -> {
                    ChatUtil.clientSendMessageRaw("§s[Commands Help]");

                    for (Command c1 : Managers.COMMAND.getCommands()) {
                        if (!(c1 instanceof ModuleCommand)) {
                            ChatUtil.clientSendMessageRaw(toHelpMessage(c1));
                        }
                    }

                    return 1;
                });
    }
}
