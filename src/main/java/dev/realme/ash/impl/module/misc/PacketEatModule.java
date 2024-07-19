package dev.realme.ash.impl.module.misc;

import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.network.PacketEvent;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;

public class PacketEatModule
extends ToggleModule {
    public PacketEatModule() {
        super("PacketEat", "packet", ModuleCategory.MISCELLANEOUS);
    }

    @EventListener
    public void onPacket(PacketEvent.Send event) {
        PlayerActionC2SPacket packet;
        Packet<?> packet2 = event.getPacket();
        if (packet2 instanceof PlayerActionC2SPacket && (packet = (PlayerActionC2SPacket)packet2).getAction() == PlayerActionC2SPacket.Action.RELEASE_USE_ITEM && PacketEatModule.mc.player.getActiveItem().getItem().isFood()) {
            event.cancel();
        }
    }
}
