package dev.realme.ash.impl.module.render;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.ColorConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.EventStage;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.TickEvent;
import dev.realme.ash.impl.event.network.PacketEvent;
import dev.realme.ash.impl.event.world.SkyboxEvent;
import java.awt.Color;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

public class SkyboxModule
extends ToggleModule {
    Config<Integer> dayTimeConfig = new NumberConfig<Integer>("WorldTime", "The world time of day", 0, 6000, 24000);
    Config<Boolean> skyConfig = new BooleanConfig("Sky", "Changes the world skybox color", true);
    Config<Color> skyColorConfig = new ColorConfig("SkyColor", "The color for the world skybox", new Color(255, 0, 0), false, true, () -> this.skyConfig.getValue());
    Config<Boolean> cloudConfig = new BooleanConfig("Cloud", "Changes the world cloud color", false);
    Config<Color> cloudColorConfig = new ColorConfig("CloudColor", "The color for the world clouds", new Color(255, 0, 0), false, true, () -> this.cloudConfig.getValue());
    Config<Boolean> fogConfig = new BooleanConfig("Fog", "Changes the world fog color", false);
    Config<Color> fogColorConfig = new ColorConfig("FogColor", "The color for the world fog", new Color(255, 0, 0), false, true, () -> this.fogConfig.getValue());

    public SkyboxModule() {
        super("Skybox", "Changes the rendering of the world skybox", ModuleCategory.RENDER);
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (event.getStage() == EventStage.POST) {
            SkyboxModule.mc.world.setTimeOfDay(this.dayTimeConfig.getValue().intValue());
        }
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Receive event) {
        if (event.getPacket() instanceof WorldTimeUpdateS2CPacket) {
            event.cancel();
        }
    }

    @EventListener
    public void onSkyboxSky(SkyboxEvent.Sky event) {
        if (this.skyConfig.getValue().booleanValue()) {
            event.cancel();
            event.setColor(this.skyColorConfig.getValue());
        }
    }

    @EventListener
    public void onSkyboxCloud(SkyboxEvent.Cloud event) {
        if (this.cloudConfig.getValue().booleanValue()) {
            event.cancel();
            event.setColor(this.cloudColorConfig.getValue());
        }
    }

    @EventListener
    public void onSkyboxFog(SkyboxEvent.Fog event) {
        if (this.fogConfig.getValue().booleanValue()) {
            event.cancel();
            event.setColor(this.fogColorConfig.getValue());
        }
    }
}
