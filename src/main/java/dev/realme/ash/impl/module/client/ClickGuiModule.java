package dev.realme.ash.impl.module.client;

import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.gui.click.ClickGuiScreen;
import dev.realme.ash.init.Modules;
import dev.realme.ash.util.render.animation.Animation;
import dev.realme.ash.util.render.animation.Easing;

public class ClickGuiModule
extends ToggleModule {
    public static ClickGuiScreen CLICK_GUI_SCREEN;
    private final Animation openCloseAnimation = new Animation(false, 300.0f, Easing.CUBIC_IN_OUT);
    public final float scaleConfig = 1.0f;

    public ClickGuiModule() {
        super("ClickGui", "Opens the clickgui screen", ModuleCategory.CLIENT, 344);
    }

    @Override
    public void onEnable() {
        if (ClickGuiModule.mc.player == null || ClickGuiModule.mc.world == null) {
            this.toggle();
            return;
        }
        if (CLICK_GUI_SCREEN == null) {
            CLICK_GUI_SCREEN = new ClickGuiScreen(this);
        }
        mc.setScreen(CLICK_GUI_SCREEN);
        this.openCloseAnimation.setState(true);
    }

    @Override
    public void onDisable() {
        if (ClickGuiModule.mc.player == null || ClickGuiModule.mc.world == null) {
            this.toggle();
            return;
        }
        ClickGuiModule.mc.player.closeScreen();
        this.openCloseAnimation.setState(false);
    }

    public int getColor() {
        return Modules.CLIENT_SETTING.getColor((int)(100.0 * this.openCloseAnimation.getFactor())).getRGB();
    }

    public int getColor1() {
        return Modules.CLIENT_SETTING.getColor((int)(100.0 * this.openCloseAnimation.getFactor())).getRGB();
    }

    public int getColor(float alpha) {
        return Modules.CLIENT_SETTING.getColor((int)((double)(100.0f * alpha) * this.openCloseAnimation.getFactor())).getRGB();
    }

    public int getColor1(float alpha) {
        return Modules.CLIENT_SETTING.getColor((int)((double)(100.0f * alpha) * this.openCloseAnimation.getFactor())).getRGB();
    }

    public Float getScale() {
        return this.scaleConfig;
    }
}
