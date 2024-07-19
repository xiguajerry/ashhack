package dev.realme.ash.impl.module.world;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.network.BreakBlockEvent;
import dev.realme.ash.impl.event.network.InteractBlockEvent;
import dev.realme.ash.init.Managers;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;

public class NoGlitchBlocksModule
extends ToggleModule {
    Config<Boolean> placeConfig = new BooleanConfig("Place", "Places blocks only after the server confirms", true);
    Config<Boolean> destroyConfig = new BooleanConfig("Destroy", "Destroys blocks only after the server confirms", true);

    public NoGlitchBlocksModule() {
        super("NoGlitchBlocks", "Prevents blocks from being glitched in the world", ModuleCategory.WORLD);
    }

    @EventListener
    public void onInteractBlock(InteractBlockEvent event) {
        if (this.placeConfig.getValue().booleanValue() && !mc.isInSingleplayer()) {
            event.cancel();
            Managers.NETWORK.sendSequencedPacket(id -> new PlayerInteractBlockC2SPacket(event.getHand(), event.getHitResult(), id));
        }
    }

    @EventListener
    public void onBreakBlock(BreakBlockEvent event) {
        if (this.destroyConfig.getValue().booleanValue() && !mc.isInSingleplayer()) {
            event.cancel();
            BlockState state = NoGlitchBlocksModule.mc.world.getBlockState(event.getPos());
            state.getBlock().onBreak(NoGlitchBlocksModule.mc.world, event.getPos(), state, NoGlitchBlocksModule.mc.player);
        }
    }
}
