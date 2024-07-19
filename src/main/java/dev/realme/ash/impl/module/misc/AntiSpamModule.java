package dev.realme.ash.impl.module.misc;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.network.PacketEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;

public class AntiSpamModule
extends ToggleModule {
    final Config<Boolean> unicodeConfig = new BooleanConfig("Unicode", "Prevents unicode characters from being rendered in chat", false);
    private final Map<UUID, String> messages = new HashMap<>();

    public AntiSpamModule() {
        super("AntiSpam", "Prevents players from spamming the game chat", ModuleCategory.MISCELLANEOUS);
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Receive event) {
        if (AntiSpamModule.mc.player == null) {
            return;
        }
        Packet<?> packet = event.getPacket();
        if (packet instanceof ChatMessageS2CPacket) {
            String lastMessage;
            ChatMessageS2CPacket packet2 = (ChatMessageS2CPacket) packet;
            if (this.unicodeConfig.getValue()) {
                String msg = packet2.body().content();
                Pattern pattern = Pattern.compile("[\\x00-\\x7F]", 2);
                Matcher matcher = pattern.matcher(msg);
                if (matcher.find()) {
                    event.cancel();
                    return;
                }
            }
            UUID sender = packet2.sender();
            String chatMessage = packet2.body().content();
            if (chatMessage.equalsIgnoreCase(lastMessage = this.messages.get(sender))) {
                event.cancel();
            } else if (lastMessage != null) {
                this.messages.replace(sender, chatMessage);
            } else {
                this.messages.put(sender, chatMessage);
            }
        }
    }
}
