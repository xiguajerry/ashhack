package dev.realme.ash.impl.module.render;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.EnumConfig;
import dev.realme.ash.api.event.EventStage;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.TickEvent;
import dev.realme.ash.impl.event.config.ConfigUpdateEvent;
import dev.realme.ash.impl.event.network.GameJoinEvent;
import dev.realme.ash.impl.event.render.LightmapGammaEvent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class FullbrightModule
extends ToggleModule {
    Config<Brightness> brightnessConfig = new EnumConfig("Mode", "Mode for world brightness", Brightness.GAMMA, Brightness.values());

    public FullbrightModule() {
        super("Fullbright", "Brightens the world", ModuleCategory.RENDER);
    }

    @Override
    public void onEnable() {
        if (FullbrightModule.mc.player != null && FullbrightModule.mc.world != null && this.brightnessConfig.getValue() == Brightness.POTION) {
            FullbrightModule.mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, -1, 0));
        }
    }

    @Override
    public void onDisable() {
        if (FullbrightModule.mc.player != null && FullbrightModule.mc.world != null && this.brightnessConfig.getValue() == Brightness.POTION) {
            FullbrightModule.mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
    }

    @EventListener
    public void onGameJoin(GameJoinEvent event) {
        this.onDisable();
        this.onEnable();
    }

    @EventListener
    public void onLightmapGamma(LightmapGammaEvent event) {
        if (this.brightnessConfig.getValue() == Brightness.GAMMA) {
            event.cancel();
            event.setGamma(-1);
        }
    }

    @EventListener
    public void onConfigUpdate(ConfigUpdateEvent event) {
        if (FullbrightModule.mc.player != null && this.brightnessConfig == event.getConfig() && event.getStage() == EventStage.POST && this.brightnessConfig.getValue() != Brightness.POTION) {
            FullbrightModule.mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (this.brightnessConfig.getValue() == Brightness.POTION && !FullbrightModule.mc.player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
            FullbrightModule.mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, -1, 0));
        }
    }

    public enum Brightness {
        GAMMA,
        POTION

    }
}
