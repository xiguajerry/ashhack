package dev.realme.ash.impl.module.movement;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.EnumConfig;
import dev.realme.ash.api.event.EventStage;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.network.PacketEvent;
import dev.realme.ash.impl.event.network.UpdateWalkingEvent;
import dev.realme.ash.init.Managers;
import dev.realme.ash.init.Modules;
import dev.realme.ash.mixin.accessor.AccessorPlayerMoveC2SPacket;
import dev.realme.ash.util.string.EnumFormatter;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.world.World;

public class NoFallModule
extends ToggleModule {
    Config<NoFallMode> modeConfig = new EnumConfig("Mode", "The mode to prevent fall damage", NoFallMode.ANTI, NoFallMode.values());

    public NoFallModule() {
        super("NoFall", "Prevents all fall damage", ModuleCategory.MOVEMENT);
    }

    @Override
    public String getModuleData() {
        return EnumFormatter.formatEnum(this.modeConfig.getValue());
    }

    @EventListener
    public void onPlayerUpdate(UpdateWalkingEvent event) {
        if (event.getStage() != EventStage.PRE || !this.checkFalling()) {
            return;
        }
        if (this.modeConfig.getValue() == NoFallMode.LATENCY) {
            if (NoFallModule.mc.world.getRegistryKey() == World.NETHER) {
                Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(NoFallModule.mc.player.getX(), 0.0, NoFallModule.mc.player.getZ(), true));
            } else {
                Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(0.0, 64.0, 0.0, true));
            }
            NoFallModule.mc.player.fallDistance = 0.0f;
        } else if (this.modeConfig.getValue() == NoFallMode.GRIM) {
            Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.Full(NoFallModule.mc.player.getX(), NoFallModule.mc.player.getY() + 1.0E-9, NoFallModule.mc.player.getZ(), NoFallModule.mc.player.getYaw(), NoFallModule.mc.player.getPitch(), true));
            NoFallModule.mc.player.onLanding();
        }
    }

    @EventListener
    public void onPacketOutbound(PacketEvent.Send event) {
        if (NoFallModule.mc.player == null || !this.checkFalling()) {
            return;
        }
        Packet<?> packet = event.getPacket();
        if (packet instanceof PlayerMoveC2SPacket) {
            PlayerMoveC2SPacket packet2 = (PlayerMoveC2SPacket) packet;
            if (this.modeConfig.getValue() == NoFallMode.PACKET) {
                ((AccessorPlayerMoveC2SPacket) packet2).hookSetOnGround(true);
            } else if (this.modeConfig.getValue() == NoFallMode.ANTI) {
                double y = packet2.getY(NoFallModule.mc.player.getY());
                ((AccessorPlayerMoveC2SPacket) packet2).hookSetY(y + (double)0.1f);
            }
        }
    }

    private boolean checkFalling() {
        return NoFallModule.mc.player.fallDistance > (float)NoFallModule.mc.player.getSafeFallDistance() && !NoFallModule.mc.player.isOnGround() && !NoFallModule.mc.player.isFallFlying() && !Modules.FLIGHT.isEnabled() && !Modules.PACKET_FLY.isEnabled();
    }

    public enum NoFallMode {
        ANTI,
        LATENCY,
        PACKET,
        GRIM

    }
}
