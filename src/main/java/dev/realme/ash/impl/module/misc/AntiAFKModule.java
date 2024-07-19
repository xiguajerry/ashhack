package dev.realme.ash.impl.module.misc;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.config.setting.StringConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.TickEvent;
import dev.realme.ash.impl.event.network.PacketEvent;
import dev.realme.ash.init.Managers;
import dev.realme.ash.util.chat.ChatUtil;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;

public class AntiAFKModule
extends ToggleModule {
    Config<Boolean> messageConfig = new BooleanConfig("Message", "Messages in chat to prevent AFK kick", true);
    Config<Boolean> tabCompleteConfig = new BooleanConfig("TabComplete", "Uses tab complete in chat to prevent AFK kick", true);
    Config<Boolean> rotateConfig = new BooleanConfig("Rotate", "Rotates the player to prevent AFK kick", true);
    Config<Boolean> autoReplyConfig = new BooleanConfig("AutoReply", "Replies to players messaging you in chat", true);
    Config<String> replyConfig = new StringConfig("Reply", "The reply message for AutoReply", "[Ash] I am currently AFK.");
    Config<Float> delayConfig = new NumberConfig<Float>("Delay", "The delay between actions", Float.valueOf(5.0f), Float.valueOf(60.0f), Float.valueOf(270.0f));

    public AntiAFKModule() {
        super("AntiAFK", "Prevents the player from being kicked for AFK", ModuleCategory.MISCELLANEOUS);
    }

    @EventListener
    public void onTick(TickEvent event) {
        Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(AntiAFKModule.mc.player.getX(), AntiAFKModule.mc.player.getY(), AntiAFKModule.mc.player.getZ(), false));
        Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(AntiAFKModule.mc.player.getX(), AntiAFKModule.mc.player.getY(), AntiAFKModule.mc.player.getZ(), false));
        Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(AntiAFKModule.mc.player.getX(), AntiAFKModule.mc.player.getY(), AntiAFKModule.mc.player.getZ(), false));
        Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(AntiAFKModule.mc.player.getX(), AntiAFKModule.mc.player.getY(), AntiAFKModule.mc.player.getZ(), false));
        Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(AntiAFKModule.mc.player.getX(), AntiAFKModule.mc.player.getY(), AntiAFKModule.mc.player.getZ(), false));
        Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(AntiAFKModule.mc.player.getX(), AntiAFKModule.mc.player.getY(), AntiAFKModule.mc.player.getZ(), false));
        Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(AntiAFKModule.mc.player.getX(), AntiAFKModule.mc.player.getY(), AntiAFKModule.mc.player.getZ(), false));
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Receive event) {
        Packet<?> packet = event.getPacket();
        if (packet instanceof ChatMessageS2CPacket) {
            String[] words;
            ChatMessageS2CPacket packet2 = (ChatMessageS2CPacket)((Object)packet);
            if (this.autoReplyConfig.getValue().booleanValue() && (words = packet2.body().content().split(" "))[1].startsWith("whispers:")) {
                ChatUtil.serverSendMessage("/r " + this.replyConfig.getValue());
            }
        }
    }
}
