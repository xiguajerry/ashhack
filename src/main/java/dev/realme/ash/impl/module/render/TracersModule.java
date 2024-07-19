package dev.realme.ash.impl.module.render;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.ColorConfig;
import dev.realme.ash.api.config.setting.EnumConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.api.render.Interpolation;
import dev.realme.ash.api.render.RenderManager;
import dev.realme.ash.impl.event.render.RenderWorldEvent;
import dev.realme.ash.init.Managers;
import dev.realme.ash.util.world.EntityUtil;
import java.awt.Color;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class TracersModule
extends ToggleModule {
    Config<Boolean> playersConfig = new BooleanConfig("Players", "Render tracers to player", true);
    Config<Color> playersColorConfig = new ColorConfig("PlayersColor", "The render color for players", new Color(200, 60, 60), false, () -> this.playersConfig.getValue());
    Config<Boolean> invisiblesConfig = new BooleanConfig("Invisibles", "Render tracers to invisible entities", true);
    Config<Color> invisiblesColorConfig = new ColorConfig("InvisiblesColor", "The render color for invisibles", new Color(200, 100, 0), false, () -> this.invisiblesConfig.getValue());
    Config<Boolean> monstersConfig = new BooleanConfig("Monsters", "Render tracers to monsters", true);
    Config<Color> monstersColorConfig = new ColorConfig("MonstersColor", "The render color for monsters", new Color(200, 60, 60), false, () -> this.monstersConfig.getValue());
    Config<Boolean> animalsConfig = new BooleanConfig("Animals", "Render tracers to animals", true);
    Config<Color> animalsColorConfig = new ColorConfig("AnimalsColor", "The render color for animals", new Color(0, 200, 0), false, () -> this.animalsConfig.getValue());
    Config<Boolean> vehiclesConfig = new BooleanConfig("Vehicles", "Render tracers to vehicles", false);
    Config<Color> vehiclesColorConfig = new ColorConfig("VehiclesColor", "The render color for vehicles", new Color(200, 100, 0), false, () -> this.vehiclesConfig.getValue());
    Config<Boolean> itemsConfig = new BooleanConfig("Items", "Render tracers to items", false);
    Config<Color> itemsColorConfig = new ColorConfig("ItemsColor", "The render color for items", new Color(255, 255, 255), false, () -> this.itemsConfig.getValue());
    Config<Target> targetConfig = new EnumConfig("Target", "The body part of the entity to target", (Enum)Target.FEET, (Enum[])Target.values());
    Config<Float> widthConfig = new NumberConfig<Float>("Width", "The line width of the tracer", Float.valueOf(1.0f), Float.valueOf(1.5f), Float.valueOf(10.0f));

    public TracersModule() {
        super("Tracers", "Draws a tracer to all entities in render distance", ModuleCategory.RENDER);
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event) {
        if (TracersModule.mc.player == null) {
            return;
        }
        boolean prevBobView = (Boolean)TracersModule.mc.options.getBobView().getValue();
        TracersModule.mc.options.getBobView().setValue(false);
        Camera cameraPos = TracersModule.mc.gameRenderer.getCamera();
        Vec3d pos = new Vec3d(0.0, 0.0, 1.0).rotateX(-((float)Math.toRadians(cameraPos.getPitch()))).rotateY(-((float)Math.toRadians(cameraPos.getYaw()))).add(TracersModule.mc.cameraEntity.getEyePos());
        for (Entity entity : TracersModule.mc.world.getEntities()) {
            Color color;
            if (entity == null || !entity.isAlive() || entity == TracersModule.mc.player || (color = this.getTracerColor(entity)) == null) continue;
            Vec3d entityPos = Interpolation.getRenderPosition(entity, event.getTickDelta()).add(0.0, this.getTargetY(entity), 0.0);
            RenderManager.renderLine(event.getMatrices(), pos, entityPos, this.widthConfig.getValue().floatValue(), color.getRGB());
        }
        TracersModule.mc.options.getBobView().setValue(prevBobView);
    }

    private Color getTracerColor(Entity entity) {
        if (entity.isInvisible() && this.invisiblesConfig.getValue().booleanValue()) {
            return this.invisiblesColorConfig.getValue();
        }
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity)entity;
            if (this.playersConfig.getValue().booleanValue()) {
                if (Managers.SOCIAL.isFriend(player.getName())) {
                    return new Color(85, 200, 200, 255);
                }
                return this.playersColorConfig.getValue();
            }
        }
        if (EntityUtil.isMonster(entity) && this.monstersConfig.getValue().booleanValue()) {
            return this.monstersColorConfig.getValue();
        }
        if ((EntityUtil.isPassive(entity) || EntityUtil.isNeutral(entity)) && this.animalsConfig.getValue().booleanValue()) {
            return this.animalsColorConfig.getValue();
        }
        if (EntityUtil.isVehicle(entity) && this.vehiclesConfig.getValue().booleanValue()) {
            return this.vehiclesColorConfig.getValue();
        }
        if (entity instanceof ItemEntity && this.itemsConfig.getValue().booleanValue()) {
            return this.itemsColorConfig.getValue();
        }
        return null;
    }

    private double getTargetY(Entity entity) {
        return switch (this.targetConfig.getValue()) {
            case FEET -> 0.0;
            case TORSO -> (double)entity.getHeight() / 2.0;
            case HEAD -> entity.getStandingEyeHeight();
        };
    }

    public static enum Target {
        FEET,
        TORSO,
        HEAD;

    }
}