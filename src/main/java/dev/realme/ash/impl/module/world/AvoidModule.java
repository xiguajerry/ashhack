package dev.realme.ash.impl.module.world;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.EventStage;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.TickEvent;
import dev.realme.ash.impl.event.world.BlockCollisionEvent;
import dev.realme.ash.util.player.MovementUtil;
import dev.realme.ash.util.world.BlockUtil;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShapes;

public class AvoidModule
extends ToggleModule {
    final Config<Boolean> voidConfig = new BooleanConfig("Void", "Prevents player from falling into the void", true);
    final Config<Integer> voidHeight = new NumberConfig<>("VoidHeight", "", 0, 8, 50);
    final Config<Boolean> fireConfig = new BooleanConfig("Fire", "Prevents player from walking into fire", false);
    final Config<Boolean> berryBushConfig = new BooleanConfig("BerryBush", "Prevents player from walking into sweet berry bushes", false);
    final Config<Boolean> cactiConfig = new BooleanConfig("Cactus", "Prevents player from walking into cacti", false);
    final Config<Boolean> unloadedConfig = new BooleanConfig("Unloaded", "Prevents player from entering chunks that haven't been loaded", false);

    public AvoidModule() {
        super("Avoid", "Prevents player from entering harmful areas", ModuleCategory.WORLD);
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (AvoidModule.nullCheck()) {
            return;
        }
        if (event.getStage() == EventStage.PRE && this.voidConfig.getValue() && !AvoidModule.mc.player.isSpectator() && AvoidModule.mc.player.getY() < (double)(AvoidModule.mc.world.getBottomY() - this.voidHeight.getValue())) {
            MovementUtil.setMotionY(0.0);
        }
    }

    @EventListener
    public void onBlockCollision(BlockCollisionEvent event) {
        if (AvoidModule.nullCheck()) {
            return;
        }
        BlockPos pos = event.getPos();
        if (this.fireConfig.getValue() && event.getBlock() == Blocks.FIRE && AvoidModule.mc.player.getY() < (double)pos.getY() + 1.0 || this.cactiConfig.getValue() && event.getBlock() == Blocks.CACTUS || this.berryBushConfig.getValue() && event.getBlock() == Blocks.SWEET_BERRY_BUSH || this.unloadedConfig.getValue() && !BlockUtil.isBlockLoaded(pos.getX(), pos.getZ())) {
            event.cancel();
            event.setVoxelShape(VoxelShapes.fullCube());
        }
    }
}
