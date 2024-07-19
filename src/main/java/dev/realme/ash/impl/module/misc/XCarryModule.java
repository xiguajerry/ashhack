package dev.realme.ash.impl.module.misc;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.network.PacketEvent;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;

public class XCarryModule
extends ToggleModule {
    final Config<Boolean> forceCancelConfig = new BooleanConfig("ForceCancel", "Cancels all close window packets", false);

    public XCarryModule() {
        super("XCarry", "Allow player to carry items in the crafting slots", ModuleCategory.MISCELLANEOUS);
    }

    @EventListener
    public void onPacketOutbound(PacketEvent.Send event) {
        CloseHandledScreenC2SPacket packet;
        if (XCarryModule.mc.player == null) {
            return;
        }
        Packet<?> packet2 = event.getPacket();
        if (packet2 instanceof CloseHandledScreenC2SPacket && ((packet = (CloseHandledScreenC2SPacket)packet2).getSyncId() == XCarryModule.mc.player.playerScreenHandler.syncId || this.forceCancelConfig.getValue())) {
            event.cancel();
        }
    }
}
