package dev.realme.ash.impl.module.misc;

import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.ScreenOpenEvent;
import dev.realme.ash.impl.event.network.PacketEvent;
import dev.realme.ash.impl.gui.beacon.BeaconSelectorScreen;
import dev.realme.ash.mixin.accessor.AccessorUpdateBeaconC2SPacket;
import java.util.Optional;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.BeaconScreen;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.UpdateBeaconC2SPacket;
import net.minecraft.screen.BeaconScreenHandler;

public class BeaconSelectorModule
extends ToggleModule {
    private StatusEffect primaryEffect;
    private StatusEffect secondaryEffect;
    private boolean customBeacon;

    public BeaconSelectorModule() {
        super("BeaconSelector", "Allows you to change beacon effects", ModuleCategory.MISCELLANEOUS);
    }

    @EventListener
    public void onPacketOutbound(PacketEvent.Send event) {
        Packet<?> packet = event.getPacket();
        if (packet instanceof UpdateBeaconC2SPacket) {
            UpdateBeaconC2SPacket packet2 = (UpdateBeaconC2SPacket)((Object)packet);
            ((AccessorUpdateBeaconC2SPacket)((Object)packet2)).setPrimaryEffect(Optional.ofNullable(this.primaryEffect));
            ((AccessorUpdateBeaconC2SPacket)((Object)packet2)).setSecondaryEffect(Optional.ofNullable(this.secondaryEffect));
        }
    }

    @EventListener
    public void onScreenOpen(ScreenOpenEvent event) {
        Screen screen = event.getScreen();
        if (screen instanceof BeaconScreen) {
            BeaconScreen screen2 = (BeaconScreen)screen;
            if (!this.customBeacon) {
                event.cancel();
                this.customBeacon = true;
                mc.setScreen(new BeaconSelectorScreen((BeaconScreenHandler)((Object)screen2.getScreenHandler()), BeaconSelectorModule.mc.player.getInventory(), screen2.getTitle()));
                this.customBeacon = false;
            }
        }
    }
}
