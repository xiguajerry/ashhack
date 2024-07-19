package dev.realme.ash.impl.module.misc;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.network.PacketEvent;
import dev.realme.ash.init.Managers;
import dev.realme.ash.util.chat.ChatUtil;
import dev.realme.ash.util.math.timer.CacheTimer;
import dev.realme.ash.util.math.timer.Timer;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;

public class AutoAcceptModule
extends ToggleModule {
    private final Timer acceptTimer = new CacheTimer();
    final Config<Float> delayConfig = new NumberConfig<>("Delay", "The delay before accepting teleport requests", 0.0f, 3.0f, 10.0f);

    public AutoAcceptModule() {
        super("AutoAccept", "Automatically accepts teleport requests", ModuleCategory.MISCELLANEOUS);
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Receive event) {
        ChatMessageS2CPacket packet;
        String text;
        Packet<?> packet2 = event.getPacket();
        if (packet2 instanceof ChatMessageS2CPacket && ((text = (packet = (ChatMessageS2CPacket) packet2).body().content()).contains("has requested to teleport to you.") || text.contains("has requested you teleport to them.")) && this.acceptTimer.passed(this.delayConfig.getValue() * 1000.0f)) {
            for (String friend : Managers.SOCIAL.getFriends()) {
                if (!text.contains(friend)) continue;
                ChatUtil.serverSendMessage("/tpaccept");
                break;
            }
        }
    }
}
