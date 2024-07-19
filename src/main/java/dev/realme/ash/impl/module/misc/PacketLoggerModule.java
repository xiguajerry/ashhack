// Decompiled with: FernFlower
// Class Version: 17
package dev.realme.ash.impl.module.misc;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.network.PacketEvent;
import dev.realme.ash.impl.imixin.IPlayerInteractEntityC2SPacket;
import dev.realme.ash.util.math.timer.CacheTimer;
import dev.realme.ash.util.math.timer.Timer;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.CommonPongC2SPacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PickFromInventoryC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.LookAndOnGround;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.OnGroundOnly;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;
import net.minecraft.util.hit.BlockHitResult;

public class PacketLoggerModule extends ToggleModule {
    Config<Float> delay = new NumberConfig("Timeout", "", 0.0F, 50.0F, 5000.0F);
    Config<Boolean> chatConfig = new BooleanConfig("LogChat", "Logs packets in the chats", false);
    Config<Boolean> moveFullConfig = new BooleanConfig("PlayerMoveFull", "Logs PlayerMoveC2SPacket", false);
    Config<Boolean> moveLookConfig = new BooleanConfig("PlayerMoveLook", "Logs PlayerMoveC2SPacket", false);
    Config<Boolean> movePosConfig = new BooleanConfig("PlayerMovePosition", "Logs PlayerMoveC2SPacket", false);
    Config<Boolean> moveGroundConfig = new BooleanConfig("PlayerMoveGround", "Logs PlayerMoveC2SPacket", false);
    Config<Boolean> vehicleMoveConfig = new BooleanConfig("VehicleMove", "Logs VehicleMoveC2SPacket", false);
    Config<Boolean> playerActionConfig = new BooleanConfig("PlayerAction", "Logs PlayerActionC2SPacket", false);
    Config<Boolean> updateSlotConfig = new BooleanConfig("UpdateSelectedSlot", "Logs UpdateSelectedSlotC2SPacket", false);
    Config<Boolean> clickSlotConfig = new BooleanConfig("ClickSlot", "Logs ClickSlotC2SPacket", false);
    Config<Boolean> pickInventoryConfig = new BooleanConfig("PickInventory", "Logs PickFromInventoryC2SPacket", false);
    Config<Boolean> handSwingConfig = new BooleanConfig("HandSwing", "Logs HandSwingC2SPacket", false);
    Config<Boolean> interactEntityConfig = new BooleanConfig("InteractEntity", "Logs PlayerInteractEntityC2SPacket", false);
    Config<Boolean> interactBlockConfig = new BooleanConfig("InteractBlock", "Logs PlayerInteractBlockC2SPacket", false);
    Config<Boolean> interactItemConfig = new BooleanConfig("InteractItem", "Logs PlayerInteractItemC2SPacket", false);
    Config<Boolean> commandConfig = new BooleanConfig("ClientCommand", "Logs ClientCommandC2SPacket", false);
    Config<Boolean> statusConfig = new BooleanConfig("ClientStatus", "Logs ClientStatusC2SPacket", false);
    Config<Boolean> closeScreenConfig = new BooleanConfig("CloseScreen", "Logs CloseHandledScreenC2SPacket", false);
    Config<Boolean> teleportConfirmConfig = new BooleanConfig("TeleportConfirm", "Logs TeleportConfirmC2SPacket", false);
    Config<Boolean> pongConfig = new BooleanConfig("Pong", "Logs CommonPongC2SPacket", false);
    int count = 0;
    private final Timer updateTimer = new CacheTimer();

    public PacketLoggerModule() {
        super("PacketLogger", "Logs client packets", ModuleCategory.MISCELLANEOUS);
    }

    public String getModuleData() {
        return "" + this.count;
    }

    private void logPacket(String msg, Object... args) {
        String s = String.format(msg, args);
        ++this.count;
        if (this.chatConfig.getValue()) {
            this.sendModuleMessage(s);
        } else {
            System.out.println(s);
        }

    }

    @EventListener
    public void onPacketOutbound(PacketEvent.Send event) {
        if (this.updateTimer.passed(this.delay.getValue())) {
            this.count = 0;
            this.updateTimer.reset();
        }

        Packet blockHitResult = event.getPacket();
        if (blockHitResult instanceof Full packet) {
            if (this.moveFullConfig.getValue()) {
                StringBuilder builder = new StringBuilder();
                builder.append("PlayerMove Full - ");
                if (packet.changesPosition()) {
                    builder.append("x: ").append(packet.getX(0.0D)).append(", y: ").append(packet.getY(0.0D)).append(", z: ").append(packet.getZ(0.0D)).append(" ");
                }

                if (packet.changesLook()) {
                    builder.append("yaw: ").append(packet.getYaw(0.0F)).append(", pitch: ").append(packet.getPitch(0.0F)).append(" ");
                }

                builder.append(" onground: ").append(packet.isOnGround());
                this.logPacket(builder.toString());
            }
        }

        blockHitResult = event.getPacket();
        if (blockHitResult instanceof PositionAndOnGround packet) {
            if (this.movePosConfig.getValue()) {
                StringBuilder builder = new StringBuilder();
                builder.append("PlayerMove PosGround - ");
                if (packet.changesPosition()) {
                    builder.append("x: ").append(packet.getX(0.0D)).append(", y: ").append(packet.getY(0.0D)).append(", z: ").append(packet.getZ(0.0D)).append(" ");
                }

                builder.append(" onground: ").append(packet.isOnGround());
                this.logPacket(builder.toString());
            }
        }

        blockHitResult = event.getPacket();
        if (blockHitResult instanceof LookAndOnGround packet) {
            if (this.moveLookConfig.getValue()) {
                StringBuilder builder = new StringBuilder();
                builder.append("PlayerMove LookGround - ");
                if (packet.changesLook()) {
                    builder.append("yaw: ").append(packet.getYaw(0.0F)).append(", pitch: ").append(packet.getPitch(0.0F)).append(" ");
                }

                builder.append(" onground: ").append(packet.isOnGround());
                this.logPacket(builder.toString());
            }
        }

        blockHitResult = event.getPacket();
        if (blockHitResult instanceof OnGroundOnly packet) {
            if (this.moveGroundConfig.getValue()) {
                String s = "PlayerMove Ground - onground: " + packet.isOnGround();
                this.logPacket(s);
            }
        }

        blockHitResult = event.getPacket();
        if (blockHitResult instanceof VehicleMoveC2SPacket packet) {
            if (this.vehicleMoveConfig.getValue()) {
                this.logPacket("VehicleMove - x: %s, y: %s, z: %s, yaw: %s, pitch: %s", packet.getX(), packet.getY(), packet.getZ(), packet.getYaw(), packet.getPitch());
            }
        }

        blockHitResult = event.getPacket();
        if (blockHitResult instanceof PlayerActionC2SPacket packet) {
            if (this.playerActionConfig.getValue() && packet.getDirection() != null) {
                this.logPacket("PlayerAction - action: %s, direction: %s, pos: %s", packet.getAction().name(), packet.getDirection().name(), packet.getPos().toShortString());
            }
        }

        blockHitResult = event.getPacket();
        if (blockHitResult instanceof UpdateSelectedSlotC2SPacket packet) {
            if (this.updateSlotConfig.getValue()) {
                this.logPacket("UpdateSlot - slot: %d", packet.getSelectedSlot());
            }
        }

        blockHitResult = event.getPacket();
        if (blockHitResult instanceof HandSwingC2SPacket packet) {
            if (this.handSwingConfig.getValue()) {
                this.logPacket("HandSwing - hand: %s", packet.getHand().name());
            }
        }

        blockHitResult = event.getPacket();
        if (blockHitResult instanceof CommonPongC2SPacket packet) {
            if (this.pongConfig.getValue()) {
                this.logPacket("Pong - %d", packet.getParameter());
            }
        }

        blockHitResult = event.getPacket();
        if (blockHitResult instanceof IPlayerInteractEntityC2SPacket packet) {
            if (this.interactEntityConfig.getValue()) {
                this.logPacket("InteractEntity - %s", packet.getEntity().getName().getString());
            }
        }

        blockHitResult = event.getPacket();
        if (blockHitResult instanceof PlayerInteractBlockC2SPacket packet) {
            if (this.interactBlockConfig.getValue()) {
                BlockHitResult blockHitResult1 = packet.getBlockHitResult();
                this.logPacket("InteractBlock - pos: %s, dir: %s, hand: %s", blockHitResult1.getBlockPos().toShortString(), blockHitResult1.getSide().name(), packet.getHand().name());
            }
        }

        blockHitResult = event.getPacket();
        if (blockHitResult instanceof PlayerInteractItemC2SPacket packet) {
            if (this.interactItemConfig.getValue()) {
                this.logPacket("InteractItem - hand: %s", packet.getHand().name());
            }
        }

        blockHitResult = event.getPacket();
        if (blockHitResult instanceof CloseHandledScreenC2SPacket packet) {
            if (this.closeScreenConfig.getValue()) {
                this.logPacket("CloseScreen - id: %s", packet.getSyncId());
            }
        }

        blockHitResult = event.getPacket();
        if (blockHitResult instanceof ClientCommandC2SPacket packet) {
            if (this.commandConfig.getValue()) {
                this.logPacket("ClientCommand - mode: %s", packet.getMode().name());
            }
        }

        blockHitResult = event.getPacket();
        if (blockHitResult instanceof ClientStatusC2SPacket packet) {
            if (this.statusConfig.getValue()) {
                this.logPacket("ClientStatus - mode: %s", packet.getMode().name());
            }
        }

        blockHitResult = event.getPacket();
        if (blockHitResult instanceof ClickSlotC2SPacket packet) {
            if (this.clickSlotConfig.getValue()) {
                this.logPacket("ClickSlot - type: %s, slot: %s, button: %s, id: %s", packet.getActionType().name(), packet.getSlot(), packet.getButton(), packet.getSyncId());
            }
        }

        blockHitResult = event.getPacket();
        if (blockHitResult instanceof PickFromInventoryC2SPacket packet) {
            if (this.pickInventoryConfig.getValue()) {
                this.logPacket("PickInventory - slot: %s", packet.getSlot());
            }
        }

        blockHitResult = event.getPacket();
        if (blockHitResult instanceof TeleportConfirmC2SPacket packet) {
            if (this.teleportConfirmConfig.getValue()) {
                this.logPacket("TeleportConfirm - id: %s", packet.getTeleportId());
            }
        }

    }
}
