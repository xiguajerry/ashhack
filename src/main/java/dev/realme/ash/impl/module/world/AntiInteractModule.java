package dev.realme.ash.impl.module.world;

import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.network.InteractBlockEvent;
import dev.realme.ash.impl.event.network.PacketEvent;
import java.util.Arrays;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.math.BlockPos;

public class AntiInteractModule
extends ToggleModule {
    List<Block> blacklist = Arrays.asList(Blocks.ENDER_CHEST, Blocks.ANVIL);

    public AntiInteractModule() {
        super("AntiInteract", "Prevents player from interacting with certain objects", ModuleCategory.WORLD);
    }

    @EventListener
    public void onInteractBlock(InteractBlockEvent event) {
        BlockPos pos = event.getHitResult().getBlockPos();
        BlockState state = AntiInteractModule.mc.world.getBlockState(pos);
        if (this.blacklist.contains(state.getBlock())) {
            event.cancel();
        }
    }

    @EventListener
    public void onPacketOutbound(PacketEvent.Send event) {
        PlayerInteractBlockC2SPacket packet;
        BlockPos pos;
        BlockState state;
        if (AntiInteractModule.mc.player == null || AntiInteractModule.mc.world == null) {
            return;
        }
        Packet<?> packet2 = event.getPacket();
        if (packet2 instanceof PlayerInteractBlockC2SPacket && this.blacklist.contains((state = AntiInteractModule.mc.world.getBlockState(pos = (packet = (PlayerInteractBlockC2SPacket)packet2).getBlockHitResult().getBlockPos())).getBlock())) {
            event.cancel();
        }
    }
}
