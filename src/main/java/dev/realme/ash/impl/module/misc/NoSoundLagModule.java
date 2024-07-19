package dev.realme.ash.impl.module.misc;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.network.PacketEvent;
import dev.realme.ash.impl.event.world.PlaySoundEvent;
import java.util.Set;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundFromEntityS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public class NoSoundLagModule
extends ToggleModule {
    Config<Boolean> explosion = new BooleanConfig("Explosion", "", false);
    private static final Set<SoundEvent> LAG_SOUNDS =
            Set.of(SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, SoundEvents.ITEM_ARMOR_EQUIP_ELYTRA,
                    SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE, SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND,
                    SoundEvents.ITEM_ARMOR_EQUIP_IRON, SoundEvents.ITEM_ARMOR_EQUIP_GOLD,
                    SoundEvents.ITEM_ARMOR_EQUIP_CHAIN, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER);

    public NoSoundLagModule() {
        super("NoSoundLag", "Prevents sound effects from lagging the game", ModuleCategory.MISCELLANEOUS);
    }

    @EventListener
    public void onPlaySound(PlaySoundEvent event) {
        if (NoSoundLagModule.nullCheck()) {
            return;
        }
        if (this.explosion.getValue().booleanValue() && event.getSound().getId() == SoundEvents.ENTITY_GENERIC_EXPLODE.getId()) {
            event.cancel();
        }
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Receive event) {
        PlaySoundS2CPacket packet2;
        PlaySoundFromEntityS2CPacket packet;
        Packet<?> packet3;
        if (NoSoundLagModule.nullCheck()) {
            return;
        }
        if (event.getPacket() instanceof ExplosionS2CPacket && this.explosion.getValue().booleanValue()) {
            event.cancel();
        }
        if ((packet3 = event.getPacket()) instanceof PlaySoundFromEntityS2CPacket && LAG_SOUNDS.contains((packet = (PlaySoundFromEntityS2CPacket) packet3).getSound().value()) || (packet3 = event.getPacket()) instanceof PlaySoundS2CPacket && LAG_SOUNDS.contains((packet2 = (PlaySoundS2CPacket) packet3).getSound().value())) {
            event.cancel();
        }
    }
}
