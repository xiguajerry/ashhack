package dev.realme.ash.impl.module.render;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.EnumConfig;
import dev.realme.ash.api.event.EventStage;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.TickEvent;
import dev.realme.ash.impl.event.biome.BiomeEffectsEvent;
import dev.realme.ash.impl.event.network.PacketEvent;
import dev.realme.ash.util.string.EnumFormatter;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.world.biome.BiomeParticleConfig;

public class NoWeatherModule
extends ToggleModule {
    final Config<Weather> weatherConfig = new EnumConfig<>("Weather", "The world weather", Weather.CLEAR, Weather.values());
    private Weather weather;

    public NoWeatherModule() {
        super("NoWeather", "Prevents weather rendering", ModuleCategory.RENDER);
    }

    @Override
    public String getModuleData() {
        return EnumFormatter.formatEnum(this.weatherConfig.getValue());
    }

    @Override
    public void onEnable() {
        if (NoWeatherModule.mc.world != null) {
            this.weather = NoWeatherModule.mc.world.isThundering() ? Weather.THUNDER : (NoWeatherModule.mc.world.isRaining() ? Weather.RAIN : Weather.CLEAR);
            this.setWeather(this.weatherConfig.getValue());
        }
    }

    @Override
    public void onDisable() {
        if (NoWeatherModule.mc.world != null && this.weather != null) {
            this.setWeather(this.weather);
        }
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (event.getStage() == EventStage.POST) {
            this.setWeather(this.weatherConfig.getValue());
        }
    }

    @EventListener
    public void onBiomeEffects(BiomeEffectsEvent event) {
        if (this.weatherConfig.getValue() == Weather.ASH) {
            event.cancel();
            event.setParticleConfig(new BiomeParticleConfig(ParticleTypes.WHITE_ASH, 0.118093334f));
        }
    }

    private void setWeather(Weather weather) {
        switch (weather) {
            case CLEAR: 
            case ASH: {
                NoWeatherModule.mc.world.getLevelProperties().setRaining(false);
                NoWeatherModule.mc.world.setRainGradient(0.0f);
                NoWeatherModule.mc.world.setThunderGradient(0.0f);
                break;
            }
            case RAIN: {
                NoWeatherModule.mc.world.getLevelProperties().setRaining(true);
                NoWeatherModule.mc.world.setRainGradient(1.0f);
                NoWeatherModule.mc.world.setThunderGradient(0.0f);
                break;
            }
            case THUNDER: {
                NoWeatherModule.mc.world.getLevelProperties().setRaining(true);
                NoWeatherModule.mc.world.setRainGradient(2.0f);
                NoWeatherModule.mc.world.setThunderGradient(1.0f);
            }
        }
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Receive event) {
        GameStateChangeS2CPacket packet;
        Packet<?> packet2 = event.getPacket();
        if (packet2 instanceof GameStateChangeS2CPacket && ((packet = (GameStateChangeS2CPacket) packet2).getReason() == GameStateChangeS2CPacket.RAIN_STARTED || packet.getReason() == GameStateChangeS2CPacket.RAIN_STOPPED || packet.getReason() == GameStateChangeS2CPacket.RAIN_GRADIENT_CHANGED || packet.getReason() == GameStateChangeS2CPacket.THUNDER_GRADIENT_CHANGED)) {
            event.cancel();
        }
    }

    public enum Weather {
        CLEAR,
        RAIN,
        THUNDER,
        ASH

    }
}
