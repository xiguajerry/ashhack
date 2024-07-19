package dev.realme.ash.impl.module.render;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.network.PacketEvent;
import dev.realme.ash.mixin.accessor.AccessorPlayerMoveC2SPacket;
import dev.realme.ash.mixin.accessor.AccessorPlayerPositionLookS2CPacket;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

public class NoRotateModule
extends ToggleModule {
    Config<Boolean> positionAdjustConfig = new BooleanConfig("PositionAdjust", "Adjusts outgoing rotation packets", false);
    private float yaw;
    private float pitch;
    private boolean cancelRotate;

    public NoRotateModule() {
        super("NoRotate", "Prevents server from forcing rotations", ModuleCategory.RENDER);
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Receive event) {
        if (NoRotateModule.mc.player == null || NoRotateModule.mc.currentScreen instanceof DownloadingTerrainScreen) {
            return;
        }
        Packet<?> packet = event.getPacket();
        if (packet instanceof PlayerPositionLookS2CPacket) {
            PlayerPositionLookS2CPacket packet2 = (PlayerPositionLookS2CPacket) packet;
            this.yaw = packet2.getYaw();
            this.pitch = packet2.getPitch();
            float yaw = NoRotateModule.mc.player.getYaw();
            float pitch = NoRotateModule.mc.player.getPitch();
            ((AccessorPlayerPositionLookS2CPacket) packet2).setYaw(yaw);
            ((AccessorPlayerPositionLookS2CPacket) packet2).setPitch(pitch);
            this.cancelRotate = true;
        }
    }

    @EventListener
    public void onPacketOutbound(PacketEvent.Send event) {
        Packet<?> packet = event.getPacket();
        if (packet instanceof PlayerMoveC2SPacket.Full packet2) {
            if (this.cancelRotate) {
                if (this.positionAdjustConfig.getValue().booleanValue()) {
                    ((AccessorPlayerMoveC2SPacket) packet2).hookSetYaw(this.yaw);
                    ((AccessorPlayerMoveC2SPacket) packet2).hookSetPitch(this.pitch);
                }
                this.cancelRotate = false;
            }
        }
    }
}
