package dev.realme.ash.impl.module.render;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.EnumConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.api.render.BoxRender;
import dev.realme.ash.api.render.RenderManager;
import dev.realme.ash.impl.event.render.RenderBlockOutlineEvent;
import dev.realme.ash.impl.event.render.RenderWorldEvent;
import dev.realme.ash.init.Managers;
import dev.realme.ash.init.Modules;
import java.text.DecimalFormat;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

public class BlockHighlightModule
extends ToggleModule {
    final Config<BoxRender> boxModeConfig = new EnumConfig<>("BoxMode", "Box rendering mode", BoxRender.OUTLINE, BoxRender.values());
    final Config<Boolean> entitiesConfig = new BooleanConfig("Debug-Entities", "Highlights entity bounding boxes for debug purposes", false);
    private double getDistance;

    public BlockHighlightModule() {
        super("BlockHighlight", "Highlights the block the player is facing", ModuleCategory.RENDER);
    }

    @Override
    public String getModuleData() {
        DecimalFormat decimal = new DecimalFormat("0.0");
        return decimal.format(this.getDistance);
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event) {
        if (BlockHighlightModule.mc.world == null) {
            return;
        }
        Box render = null;
        HitResult result = BlockHighlightModule.mc.crosshairTarget;
        if (result != null) {
            Vec3d pos = Managers.POSITION.getEyePos();
            if (this.entitiesConfig.getValue() && result.getType() == HitResult.Type.ENTITY) {
                Entity entity = ((EntityHitResult)result).getEntity();
                render = entity.getBoundingBox();
                this.getDistance = pos.distanceTo(entity.getPos());
            } else if (result.getType() == HitResult.Type.BLOCK) {
                BlockPos hpos = ((BlockHitResult)result).getBlockPos();
                BlockState state = BlockHighlightModule.mc.world.getBlockState(hpos);
                VoxelShape outlineShape = state.getOutlineShape(BlockHighlightModule.mc.world, hpos);
                if (outlineShape.isEmpty()) {
                    return;
                }
                Box render1 = outlineShape.getBoundingBox();
                render = new Box((double)hpos.getX() + render1.minX, (double)hpos.getY() + render1.minY, (double)hpos.getZ() + render1.minZ, (double)hpos.getX() + render1.maxX, (double)hpos.getY() + render1.maxY, (double)hpos.getZ() + render1.maxZ);
                this.getDistance = pos.distanceTo(hpos.toCenterPos());
            }
        }
        if (render != null) {
            switch (this.boxModeConfig.getValue()) {
                case FILL: {
                    RenderManager.renderBox(event.getMatrices(), render, Modules.CLIENT_SETTING.getRGB(60));
                    RenderManager.renderBoundingBox(event.getMatrices(), render, 2.5f, Modules.CLIENT_SETTING.getRGB(145));
                    break;
                }
                case OUTLINE: {
                    RenderManager.renderBoundingBox(event.getMatrices(), render, 2.5f, Modules.CLIENT_SETTING.getRGB(145));
                }
            }
        }
    }

    @EventListener
    public void onRenderBlockOutline(RenderBlockOutlineEvent event) {
        event.cancel();
    }
}
