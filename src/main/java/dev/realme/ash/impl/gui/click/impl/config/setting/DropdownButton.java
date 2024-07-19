package dev.realme.ash.impl.gui.click.impl.config.setting;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.render.RenderManager;
import dev.realme.ash.impl.gui.click.impl.config.CategoryFrame;
import dev.realme.ash.impl.gui.click.impl.config.ModuleButton;
import dev.realme.ash.init.Modules;
import dev.realme.ash.util.string.EnumFormatter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;

public class DropdownButton extends ConfigButton<Enum<?>> {
    private int index;

    public DropdownButton(CategoryFrame frame, ModuleButton moduleButton, Config<Enum<?>> config, float x, float y) {
        super(frame, moduleButton, config, x, y);
    }

    public void render(DrawContext context, float ix, float iy, float mouseX, float mouseY, float delta) {
        this.x = ix;
        this.y = iy;
        String val = EnumFormatter.formatEnum(this.config.getValue());
        this.rectGradient(context, Modules.CLICK_GUI.getColor(), Modules.CLICK_GUI.getColor1());
        RenderManager.renderText(context, this.config.getName() + Formatting.GRAY + " " + val, ix + 2.0F, iy + 4.0F, -1);
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isWithin(mouseX, mouseY)) {
            Enum<?> val = this.config.getValue();
            Class<? extends Enum<?>> clz = (Class<? extends Enum<?>>) val.getClass();
            Enum<?>[] constants = clz.getEnumConstants();
            if (constants == null) {
                clz = val.getDeclaringClass();
                try {
                    Method method = clz.getDeclaredMethod("$values");
                    method.setAccessible(true);
                    constants = (Enum<?>[]) method.invoke(null);
                    if (constants == null) return;
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            String[] values = Arrays.stream(constants).map(Enum::name).toArray(String[]::new);
            if (button == 0) {
                this.index = this.index + 1 > values.length - 1 ? 0 : this.index + 1;
                this.config.setValue(Enum.valueOf((Class) clz, values[this.index]));
            }
        }

    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
    }

    public void keyPressed(int keyCode, int scanCode, int modifiers) {
    }
}
