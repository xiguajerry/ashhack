// Decompiled with: FernFlower
// Class Version: 17
package dev.realme.ash.impl.module.render;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.ColorConfig;
import dev.realme.ash.api.config.setting.EnumConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.api.render.RenderManager;
import dev.realme.ash.impl.event.render.RenderWorldEvent;
import dev.realme.ash.impl.module.render.PlaceRenderModule;
import dev.realme.ash.init.Modules;
import dev.realme.ash.util.render.FadeUtils;
import java.awt.Color;
import java.util.HashMap;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class PlaceRenderModule extends ToggleModule {
    final Config<PlaceRenderModule.FadeMode> fadeMode = new EnumConfig<>("FadeMode", "", PlaceRenderModule.FadeMode.None, PlaceRenderModule.FadeMode.values());
    final Config<Color> color = new ColorConfig("Color", "", new Color(255, 255, 255), false, false);
    final Config<Boolean> fadeAlpha = new BooleanConfig("FadeAlpha", "", true);
    final Config<Integer> fadeTime = new NumberConfig<>("FadeTime", "", 0, 200, 5000);
    final Config<Boolean> box = new BooleanConfig("Box", "", true);
    final Config<Integer> boxAlpha = new NumberConfig<>("BoxAlpha", "", 0, 80, 255);
    final Config<Boolean> line = new BooleanConfig("lines", "", true);
    final Config<Integer> olAlpha = new NumberConfig<>("OLAlpha", "", 0, 255, 255);
    public final HashMap<BlockPos, PlaceRenderModule.placePosition> PlaceMap = new HashMap<>();

    public PlaceRenderModule() {
        super("PlaceRender", "mio client.", ModuleCategory.RENDER);
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event) {
        boolean shouldClear = true;

        for(PlaceRenderModule.placePosition placePosition : this.PlaceMap.values()) {
            if (placePosition.firstFade.easeOutQuad() != 1.0D) {
                shouldClear = false;
                this.drawBlock(event.getMatrices(), placePosition.pos, placePosition.firstFade.easeOutQuad() - 1.0D, placePosition);
            }
        }

        if (shouldClear) {
            this.PlaceMap.clear();
        }

    }

    private void drawBlock(MatrixStack matrixStack, BlockPos pos, double alpha, PlaceRenderModule.placePosition placePosition) {
        ColorConfig sb = (ColorConfig)this.color;
        double ease = placePosition.firstFade.easeOutQuad() * 0.5D;
        Box var10000;
        switch(this.fadeMode.getValue()) {
            case None:
                var10000 = new Box(pos);
                break;
            case Shrink:
                var10000 = (new Box(pos)).shrink(ease, ease, ease).shrink(-ease, -ease, -ease);
                break;
            default:
                throw new IncompatibleClassChangeError();
        }

        Box bb = var10000;
        if (this.box.getValue()) {
            RenderManager.renderBox(matrixStack, bb, sb.getRgb(this.fadeAlpha.getValue() ? (int)Math.round((double) this.boxAlpha.getValue() * -alpha) : this.boxAlpha.getValue()));
        }

        if (this.line.getValue()) {
            RenderManager.renderBoundingBox(matrixStack, bb, 1.0F, sb.getRgb(this.fadeAlpha.getValue() ? (int)Math.round((double) this.olAlpha.getValue() * -alpha) : this.olAlpha.getValue()));
        }

    }

    public enum FadeMode {
        None,
        Shrink;

        // $FF: synthetic method
        private static PlaceRenderModule.FadeMode[] $values() {
            return new PlaceRenderModule.FadeMode[]{None, Shrink};
        }
    }

    public static class placePosition {
        public final FadeUtils firstFade = new FadeUtils(Modules.PLACE_RENDER.fadeTime.getValue());
        public final BlockPos pos;

        public placePosition(BlockPos placePos) {
            this.pos = placePos;
        }
    }
}
 