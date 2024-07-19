package dev.realme.ash.impl.module.combat;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.EnumConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.network.PacketEvent;
import dev.realme.ash.impl.imixin.IPlayerInteractEntityC2SPacket;
import dev.realme.ash.util.network.InteractType;
import net.minecraft.block.Blocks;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class CriticalsModule
extends ToggleModule {
    final Config<Mode> mode = new EnumConfig<>("Mode", "Mode for critical attack modifier", Mode.NCP, Mode.values());

    public CriticalsModule() {
        super("Criticals", "Modifies attacks to always land critical hits", ModuleCategory.COMBAT);
    }

    @EventListener
    public void onPacketSend(PacketEvent.Send event) {
        IPlayerInteractEntityC2SPacket packet;
        Packet<?> packet2 = event.getPacket();
        if (packet2 instanceof IPlayerInteractEntityC2SPacket && (packet = (IPlayerInteractEntityC2SPacket) packet2).getType() == InteractType.ATTACK && !(((IPlayerInteractEntityC2SPacket) event.getPacket()).getEntity() instanceof EndCrystalEntity)) {
            this.doCrit();
        }
    }

    public void doCrit() {
        if ((CriticalsModule.mc.player.isOnGround() || CriticalsModule.mc.player.getAbilities().flying) && !CriticalsModule.mc.player.isInLava() && !CriticalsModule.mc.player.isSubmergedInWater()) {
            if (this.mode.getValue() == Mode.Strict && CriticalsModule.mc.world.getBlockState(CriticalsModule.mc.player.getBlockPos()).getBlock() != Blocks.COBWEB) {
                CriticalsModule.mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(CriticalsModule.mc.player.getX(), CriticalsModule.mc.player.getY() + 0.062600301692775, CriticalsModule.mc.player.getZ(), false));
                CriticalsModule.mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(CriticalsModule.mc.player.getX(), CriticalsModule.mc.player.getY() + 0.07260029960661, CriticalsModule.mc.player.getZ(), false));
                CriticalsModule.mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(CriticalsModule.mc.player.getX(), CriticalsModule.mc.player.getY(), CriticalsModule.mc.player.getZ(), false));
                CriticalsModule.mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(CriticalsModule.mc.player.getX(), CriticalsModule.mc.player.getY(), CriticalsModule.mc.player.getZ(), false));
            } else if (this.mode.getValue() == Mode.NCP) {
                CriticalsModule.mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(CriticalsModule.mc.player.getX(), CriticalsModule.mc.player.getY() + 0.0625, CriticalsModule.mc.player.getZ(), false));
                CriticalsModule.mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(CriticalsModule.mc.player.getX(), CriticalsModule.mc.player.getY(), CriticalsModule.mc.player.getZ(), false));
            } else if (this.mode.getValue() == Mode.Normal) {
                CriticalsModule.mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(CriticalsModule.mc.player.getX(), CriticalsModule.mc.player.getY() + 1.058293536E-5, CriticalsModule.mc.player.getZ(), false));
                CriticalsModule.mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(CriticalsModule.mc.player.getX(), CriticalsModule.mc.player.getY() + 9.16580235E-6, CriticalsModule.mc.player.getZ(), false));
                CriticalsModule.mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(CriticalsModule.mc.player.getX(), CriticalsModule.mc.player.getY() + 1.0371854E-7, CriticalsModule.mc.player.getZ(), false));
            } else if (this.mode.getValue() == Mode.New2b2t) {
                CriticalsModule.mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(CriticalsModule.mc.player.getX(), CriticalsModule.mc.player.getY() + 2.71875E-7, CriticalsModule.mc.player.getZ(), false));
                CriticalsModule.mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(CriticalsModule.mc.player.getX(), CriticalsModule.mc.player.getY(), CriticalsModule.mc.player.getZ(), false));
            }
        }
    }

    public enum Mode {
        NCP,
        Strict,
        Normal,
        New2b2t

    }
}
