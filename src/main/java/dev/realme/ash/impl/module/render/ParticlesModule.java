package dev.realme.ash.impl.module.render;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.ColorConfig;
import dev.realme.ash.api.config.setting.EnumConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.particle.ParticleEvent;
import dev.realme.ash.impl.event.particle.TotemParticleEvent;
import java.awt.Color;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.MathHelper;

public class ParticlesModule
extends ToggleModule {
    final Config<TotemParticle> totemConfig = new EnumConfig<>("Totem", "Renders totem particles", TotemParticle.OFF, TotemParticle.values());
    final Config<Color> totemColorConfig = new ColorConfig("TotemColor", "Color of the totem particles", new Color(25, 120, 0), false, false, () -> this.totemConfig.getValue() == TotemParticle.COLOR);
    final Config<Boolean> fireworkConfig = new BooleanConfig("Firework", "Renders firework particles", false);
    final Config<Boolean> potionConfig = new BooleanConfig("Effects", "Renders potion effect particles", true);
    final Config<Boolean> bottleConfig = new BooleanConfig("BottleSplash", "Render bottle splash particles", true);
    final Config<Boolean> portalConfig = new BooleanConfig("Portal", "Render portal particles", true);

    public ParticlesModule() {
        super("Particles", "Change the rendering of particles", ModuleCategory.RENDER);
    }

    @EventListener
    public void onParticle(ParticleEvent event) {
        if (this.potionConfig.getValue() && event.getParticleType() == ParticleTypes.ENTITY_EFFECT || this.fireworkConfig.getValue() && event.getParticleType() == ParticleTypes.FIREWORK || this.bottleConfig.getValue() && (event.getParticleType() == ParticleTypes.EFFECT || event.getParticleType() == ParticleTypes.INSTANT_EFFECT) || this.portalConfig.getValue() && event.getParticleType() == ParticleTypes.PORTAL) {
            event.cancel();
        }
    }

    @EventListener
    public void onTotemParticle(TotemParticleEvent event) {
        if (this.totemConfig.getValue() == TotemParticle.COLOR) {
            event.cancel();
            Color color = this.totemColorConfig.getValue();
            float r = (float)color.getRed() / 255.0f;
            float g = (float)color.getGreen() / 255.0f;
            float b = (float)color.getBlue() / 255.0f;
            event.setColor(new Color(MathHelper.clamp(r + RANDOM.nextFloat() * 0.1f, 0.0f, 1.0f), MathHelper.clamp(g + RANDOM.nextFloat() * 0.1f, 0.0f, 1.0f), MathHelper.clamp(b + RANDOM.nextFloat() * 0.1f, 0.0f, 1.0f)));
        }
    }

    @EventListener
    public void onParticleEmitter(ParticleEvent.Emitter event) {
        if (this.totemConfig.getValue() == TotemParticle.REMOVE && event.getParticleType() == ParticleTypes.TOTEM_OF_UNDYING) {
            event.cancel();
        }
    }

    private enum TotemParticle {
        OFF,
        REMOVE,
        COLOR

    }
}
