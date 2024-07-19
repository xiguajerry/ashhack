package dev.realme.ash.impl.module.render;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.ColorConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.api.render.RenderManager;
import dev.realme.ash.impl.event.render.RenderWorldEvent;
import dev.realme.ash.util.world.BlastResistantBlocks;
import java.awt.Color;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class PhaseESPModule
extends ToggleModule {
    final Config<Boolean> safeConfig = new BooleanConfig("Safe", "Highlights safe phase blocks", false);
    final Config<Color> unsafeConfig = new ColorConfig("UnsafeColor", "The color for rendering unsafe phase blocks", new Color(255, 0, 0), false, false);
    final Config<Color> obsidianConfig = new ColorConfig("ObsidianColor", "The color for rendering obsidian phase blocks", new Color(255, 255, 0), false, false, () -> this.safeConfig.getValue());
    final Config<Color> bedrockConfig = new ColorConfig("BedrockColor", "The color for rendering bedrock phase blocks", new Color(0, 255, 0), false, false, () -> this.safeConfig.getValue());

    public PhaseESPModule() {
        super("PhaseESP", "Displays safe phase blocks", ModuleCategory.RENDER);
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event) {
        if (PhaseESPModule.mc.player == null || PhaseESPModule.mc.world == null || !PhaseESPModule.mc.player.isOnGround()) {
            return;
        }
        BlockPos playerPos = PhaseESPModule.mc.player.getBlockPos();
        for (Direction direction : Direction.values()) {
            BlockPos blockPos;
            if (!direction.getAxis().isHorizontal() || PhaseESPModule.mc.world.getBlockState(blockPos = playerPos.offset(direction)).isReplaceable()) continue;
            Vec3d pos = PhaseESPModule.mc.player.getPos();
            BlockState state = PhaseESPModule.mc.world.getBlockState(blockPos.down());
            Color color = null;
            if (state.isReplaceable()) {
                color = this.unsafeConfig.getValue();
            } else if (this.safeConfig.getValue()) {
                color = BlastResistantBlocks.isUnbreakable(state.getBlock()) ? this.bedrockConfig.getValue() : this.obsidianConfig.getValue();
            }
            if (color == null) continue;
            double x = blockPos.getX();
            double y = blockPos.getY();
            double z = blockPos.getZ();
            double dx = pos.getX() - (double)playerPos.getX();
            double dz = pos.getZ() - (double)playerPos.getZ();
            if (direction == Direction.EAST && dx >= 0.65) {
                RenderManager.drawLine(event.getMatrices(), x, y, z, x, y, z + 1.0, color.getRGB());
                continue;
            }
            if (direction == Direction.WEST && dx <= 0.35) {
                RenderManager.drawLine(event.getMatrices(), x + 1.0, y, z, x + 1.0, y, z + 1.0, color.getRGB());
                continue;
            }
            if (direction == Direction.SOUTH && dz >= 0.65) {
                RenderManager.drawLine(event.getMatrices(), x, y, z, x + 1.0, y, z, color.getRGB());
                continue;
            }
            if (direction != Direction.NORTH || !(dz <= 0.35)) continue;
            RenderManager.drawLine(event.getMatrices(), x, y, z + 1.0, x + 1.0, y, z + 1.0, color.getRGB());
        }
    }
}
