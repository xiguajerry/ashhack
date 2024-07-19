package dev.realme.ash.impl.module.client;

import dev.realme.ash.Ash;
import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ConcurrentModule;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.impl.event.network.PacketEvent;
import dev.realme.ash.init.Managers;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;

public class ServerModule
extends ConcurrentModule {
    final Config<Boolean> packetKickConfig = new BooleanConfig("NoPacketKick", "Prevents thrown exceptions from kicking you", true);
    final Config<Boolean> demoConfig = new BooleanConfig("NoDemo", "Prevents servers from forcing you to a demo screen", true);
    final Config<Boolean> resourcePackConfig = new BooleanConfig("NoResourcePack", "Prevents server from forcing resource pack", false);

    public ServerModule() {
        super("Server", "Prevents servers actions on player", ModuleCategory.CLIENT);
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Receive event) {
        GameStateChangeS2CPacket packet;
        Packet<?> packet2 = event.getPacket();
        if (packet2 instanceof GameStateChangeS2CPacket && (packet = (GameStateChangeS2CPacket) packet2).getReason() == GameStateChangeS2CPacket.DEMO_MESSAGE_SHOWN && !mc.isDemo() && this.demoConfig.getValue()) {
            Ash.info("Server attempted to use Demo mode features on you!");
            event.cancel();
        }
        if (event.getPacket() instanceof ResourcePackSendS2CPacket && this.resourcePackConfig.getValue()) {
            event.cancel();
            Managers.NETWORK.sendPacket(new ResourcePackStatusC2SPacket(ServerModule.mc.player.getUuid(), ResourcePackStatusC2SPacket.Status.DECLINED));
        }
    }

    public boolean isPacketKick() {
        return this.packetKickConfig.getValue();
    }
}
