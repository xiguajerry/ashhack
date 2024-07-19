package dev.realme.ash.impl.module.misc;

import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.network.DecodePacketEvent;

public class NoPacketKickModule
extends ToggleModule {
    public NoPacketKickModule() {
        super("NoPacketKick", "Prevents getting kicked by packets", ModuleCategory.MISCELLANEOUS);
    }

    @EventListener
    public void onDecodePacket(DecodePacketEvent event) {
        event.cancel();
    }
}
