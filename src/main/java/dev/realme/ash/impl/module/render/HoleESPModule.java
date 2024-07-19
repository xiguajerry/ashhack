package dev.realme.ash.impl.module.render;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.ColorConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.api.render.RenderManager;
import dev.realme.ash.impl.event.render.RenderWorldEvent;
import dev.realme.ash.impl.manager.combat.hole.Hole;
import dev.realme.ash.impl.manager.combat.hole.HoleType;
import dev.realme.ash.init.Managers;
import dev.realme.ash.util.Globals;
import java.awt.Color;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;

public class HoleESPModule
extends ToggleModule {
    final Config<Float> rangeConfig = new NumberConfig<>("Range", "Range to display holes", 3.0f, 5.0f, 25.0f);
    final Config<Float> heightConfig = new NumberConfig<>("Size", "Render height of holes", -1.0f, 1.0f, 1.0f);
    final Config<Boolean> obsidianCheckConfig = new BooleanConfig("Obsidian", "Displays obsidian holes", true);
    final Config<Boolean> obsidianBedrockConfig = new BooleanConfig("Obsidian-Bedrock", "Displays mixed obsidian and bedrock holes", true);
    final Config<Boolean> doubleConfig = new BooleanConfig("Double", "Displays double holes where the player can stand in the middle of two blocks to block explosion damage", false);
    final Config<Boolean> quadConfig = new BooleanConfig("Quad", "Displays quad holes where the player can stand in the middle of four blocks to block explosion damage", false);
    final Config<Boolean> voidConfig = new BooleanConfig("Void", "Displays void holes in the world", false);
    final Config<Boolean> fadeConfig = new BooleanConfig("Fade", "Fades the opacity of holes based on distance", false);
    final Config<Color> obsidianConfig = new ColorConfig("ObsidianColor", "The color for rendering obsidian holes", new Color(255, 0, 0, 100), () -> this.obsidianCheckConfig.getValue());
    final Config<Color> mixedConfig = new ColorConfig("Obsidian-BedrockColor", "The color for rendering mixed holes", new Color(255, 255, 0, 100), () -> this.obsidianBedrockConfig.getValue());
    final Config<Color> bedrockConfig = new ColorConfig("BedrockColor", "The color for rendering bedrock holes", new Color(0, 255, 0, 100));
    final Config<Color> voidColorConfig = new ColorConfig("VoidColor", "The color for rendering bedrock holes", new Color(255, 0, 0, 160), () -> this.voidConfig.getValue());

    public HoleESPModule() {
        super("HoleESP", "Displays nearby blast resistant holes", ModuleCategory.RENDER);
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event) {
        if (Globals.mc.player == null) {
            return;
        }
        for (Hole hole : Managers.HOLE.getHoles()) {
            double dist;
            if ((hole.isDoubleX() || hole.isDoubleZ()) && !this.doubleConfig.getValue() || hole.isQuad() && !this.quadConfig.getValue() || hole.getSafety() == HoleType.VOID && !this.voidConfig.getValue() || hole.getSafety() == HoleType.OBSIDIAN && !this.obsidianCheckConfig.getValue() || hole.getSafety() == HoleType.OBSIDIAN_BEDROCK && !this.obsidianBedrockConfig.getValue() || (dist = hole.squaredDistanceTo(Globals.mc.player)) > ((NumberConfig)this.rangeConfig).getValueSq()) continue;
            double x = hole.getX();
            double y = hole.getY();
            double z = hole.getZ();
            Box render = null;
            if (hole.getSafety() == HoleType.VOID) {
                render = new Box(x, y, z, x + 1.0, y + 1.0, z + 1.0);
            } else if (hole.isDoubleX()) {
                render = new Box(x, y, z, x + 2.0, y + (double) this.heightConfig.getValue(), z + 1.0);
            } else if (hole.isDoubleZ()) {
                render = new Box(x, y, z, x + 1.0, y + (double) this.heightConfig.getValue(), z + 2.0);
            } else if (hole.isQuad()) {
                render = new Box(x, y, z, x + 2.0, y + (double) this.heightConfig.getValue(), z + 2.0);
            } else if (hole.isStandard()) {
                render = new Box(x, y, z, x + 1.0, y + (double) this.heightConfig.getValue(), z + 1.0);
            }
            if (render == null) {
                return;
            }
            double alpha = 1.0;
            if (this.fadeConfig.getValue()) {
                double fadeRange = (double) this.rangeConfig.getValue() - 1.0;
                double fadeRangeSq = fadeRange * fadeRange;
                alpha = (fadeRangeSq + 9.0 - Globals.mc.player.squaredDistanceTo(hole.getX(), hole.getY(), hole.getZ())) / fadeRangeSq;
                alpha = MathHelper.clamp(alpha, 0.0, 1.0);
            }
            RenderManager.renderBox(event.getMatrices(), render, this.getHoleColor(hole.getSafety(), alpha));
            RenderManager.renderBoundingBox(event.getMatrices(), render, 1.5f, this.getHoleColor(hole.getSafety(), (int)(alpha * 145.0)));
        }
    }

    private int getHoleColor(HoleType holeType, double alpha) {
        ColorConfig obsidian = (ColorConfig)this.obsidianConfig;
        ColorConfig mixed = (ColorConfig)this.mixedConfig;
        ColorConfig bedrock = (ColorConfig)this.bedrockConfig;
        ColorConfig voidColor = (ColorConfig)this.voidColorConfig;
        return switch (holeType) {
            case OBSIDIAN -> obsidian.getRgb((int)((double)obsidian.getAlpha() * alpha));
            case OBSIDIAN_BEDROCK -> mixed.getRgb((int)((double)mixed.getAlpha() * alpha));
            case BEDROCK -> bedrock.getRgb((int)((double)bedrock.getAlpha() * alpha));
            case VOID -> voidColor.getRgb((int)((double)voidColor.getAlpha() * alpha));
        };
    }

    private int getHoleColor(HoleType holeType, int alpha) {
        return switch (holeType) {
            case OBSIDIAN -> ((ColorConfig)this.obsidianConfig).getRgb(alpha);
            case OBSIDIAN_BEDROCK -> ((ColorConfig)this.mixedConfig).getRgb(alpha);
            case BEDROCK -> ((ColorConfig)this.bedrockConfig).getRgb(alpha);
            case VOID -> ((ColorConfig)this.voidColorConfig).getRgb(alpha);
        };
    }

    public double getRange() {
        return this.rangeConfig.getValue();
    }
}
