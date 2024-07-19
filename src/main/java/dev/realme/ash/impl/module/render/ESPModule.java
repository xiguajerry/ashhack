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
import dev.realme.ash.impl.event.EntityOutlineEvent;
import dev.realme.ash.impl.event.entity.decoration.TeamColorEvent;
import dev.realme.ash.impl.event.render.RenderWorldEvent;
import dev.realme.ash.util.world.BlockUtil;
import dev.realme.ash.util.world.EntityUtil;
import java.awt.Color;
import java.util.ArrayList;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;

public class ESPModule
extends ToggleModule {
    Config<ESPMode> modeConfig = new EnumConfig("Mode", "ESP rendering mode", ESPMode.GLOW, ESPMode.values());
    Config<Boolean> renderBox = new BooleanConfig("Box", "", true);
    Config<Integer> boxAlpha = new NumberConfig<Integer>("BoxAlpha", "", 0, 80, 255);
    Config<Boolean> line = new BooleanConfig("lines", "", true);
    Config<Integer> olAlpha = new NumberConfig<Integer>("OLAlpha", "", 0, 255, 255);
    Config<Float> widthConfig = new NumberConfig<Float>("Linewidth", "ESP rendering line width", 0.1f, 1.25f, 5.0f);
    Config<Boolean> playersConfig = new BooleanConfig("Players", "Render players through walls", true);
    Config<Boolean> selfConfig = new BooleanConfig("Self", "Render self through walls", true);
    Config<Color> playersColorConfig = new ColorConfig("PlayersColor", "The render color for players", new Color(200, 60, 60, 80), true, () -> this.playersConfig.getValue() || this.selfConfig.getValue());
    Config<Boolean> monstersConfig = new BooleanConfig("Monsters", "Render monsters through walls", true);
    Config<Color> monstersColorConfig = new ColorConfig("MonstersColor", "The render color for monsters", new Color(200, 60, 60, 80), true, () -> this.monstersConfig.getValue());
    Config<Boolean> animalsConfig = new BooleanConfig("Animals", "Render animals through walls", true);
    Config<Color> animalsColorConfig = new ColorConfig("AnimalsColor", "The render color for animals", new Color(0, 200, 0, 80), true, () -> this.animalsConfig.getValue());
    Config<Boolean> vehiclesConfig = new BooleanConfig("Vehicles", "Render vehicles through walls", false);
    Config<Color> vehiclesColorConfig = new ColorConfig("VehiclesColor", "The render color for vehicles", new Color(200, 100, 0, 80), true, () -> this.vehiclesConfig.getValue());
    Config<Boolean> itemsConfig = new BooleanConfig("Items", "Render dropped items through walls", false);
    Config<Color> itemsColorConfig = new ColorConfig("ItemsColor", "The render color for items", new Color(200, 100, 0, 80), true, () -> this.itemsConfig.getValue());
    Config<Boolean> crystalsConfig = new BooleanConfig("EndCrystals", "Render end crystals through walls", false);
    Config<Color> crystalsColorConfig = new ColorConfig("EndCrystalsColor", "The render color for end crystals", new Color(200, 100, 200, 80), true, () -> this.crystalsConfig.getValue());
    Config<Boolean> blockEntity = new BooleanConfig("BlockEntity", "", false);
    Config<Boolean> blockEntityBox = new BooleanConfig("BlockEntityBox", "", true);
    Config<Integer> blockEntityBoxAlpha = new NumberConfig<Integer>("BlockEntityBoxAlpha", "", 0, 80, 255);
    Config<Boolean> blockEntityLine = new BooleanConfig("BlockEntityLines", "", true);
    Config<Integer> blockEntityOlAlpha = new NumberConfig<Integer>("BlockEntityOLAlpha", "", 0, 255, 255);
    Config<Boolean> chestsConfig = new BooleanConfig("Chests", "Render players through walls", true);
    Config<Color> chestsColorConfig = new ColorConfig("ChestsColor", "The render color for chests", new Color(200, 200, 101, 80), true, () -> this.chestsConfig.getValue());
    Config<Boolean> echestsConfig = new BooleanConfig("EnderChests", "Render players through walls", true);
    Config<Color> echestsColorConfig = new ColorConfig("EnderChestsColor", "The render color for ender chests", new Color(155, 0, 200, 80), true, () -> this.echestsConfig.getValue());
    Config<Boolean> shulkersConfig = new BooleanConfig("Shulkers", "Render players through walls", true);
    Config<Color> shulkersColorConfig = new ColorConfig("ShulkersColor", "The render color for shulkers", new Color(200, 0, 106, 80), true, () -> this.shulkersConfig.getValue());
    Config<Boolean> hoppersConfig = new BooleanConfig("Hoppers", "Render players through walls", false);
    Config<Color> hoppersColorConfig = new ColorConfig("HoppersColor", "The render color for hoppers", new Color(100, 100, 100, 80), true, () -> this.hoppersConfig.getValue());
    Config<Boolean> furnacesConfig = new BooleanConfig("Furnaces", "Render players through walls", false);
    Config<Color> furnacesColorConfig = new ColorConfig("FurnacesColor", "The render color for furnaces", new Color(100, 100, 100, 80), () -> this.furnacesConfig.getValue());

    public ESPModule() {
        super("ESP", "See entities and objects through walls", ModuleCategory.RENDER);
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event) {
        if (ESPModule.nullCheck()) {
            return;
        }
        if (this.blockEntity.getValue().booleanValue()) {
            ArrayList<BlockEntity> blockEntities = BlockUtil.getTileEntities();
            for (BlockEntity blockEntity : blockEntities) {
                if (!this.checkStorageESP(blockEntity)) continue;
                Box box = new Box(blockEntity.getPos());
                ColorConfig tit = this.getStorageESPColor(blockEntity);
                if (this.blockEntityBox.getValue().booleanValue()) {
                    RenderManager.renderBox(event.getMatrices(), box, tit.getRgb(this.blockEntityBoxAlpha.getValue()));
                }
                if (!this.blockEntityLine.getValue().booleanValue()) continue;
                RenderManager.renderBoundingBox(event.getMatrices(), box, this.widthConfig.getValue().floatValue(), tit.getRgb(this.blockEntityOlAlpha.getValue()));
            }
        }
        for (Entity entity : ESPModule.mc.world.getEntities()) {
            if (!this.modeConfig.getValue().equals(ESPMode.BOX) || !this.checkESP(entity)) continue;
            double x = MathHelper.lerp(event.getTickDelta(), entity.lastRenderX, entity.getX()) - entity.getX();
            double y = MathHelper.lerp(event.getTickDelta(), entity.lastRenderY, entity.getY()) - entity.getY();
            double z = MathHelper.lerp(event.getTickDelta(), entity.lastRenderZ, entity.getZ()) - entity.getZ();
            Box box = entity.getBoundingBox();
            ColorConfig sb = (ColorConfig)this.itemsColorConfig;
            if (this.renderBox.getValue().booleanValue()) {
                RenderManager.renderBox(event.getMatrices(), new Box(x + box.minX, y + box.minY, z + box.minZ, x + box.maxX, y + box.maxY, z + box.maxZ), sb.getRgb(this.boxAlpha.getValue()));
            }
            if (!this.line.getValue().booleanValue()) continue;
            RenderManager.renderBoundingBox(event.getMatrices(), new Box(x + box.minX, y + box.minY, z + box.minZ, x + box.maxX, y + box.maxY, z + box.maxZ), this.widthConfig.getValue().floatValue(), sb.getRgb(this.olAlpha.getValue()));
        }
    }

    @EventListener
    public void onEntityOutline(EntityOutlineEvent event) {
        if (this.modeConfig.getValue() == ESPMode.GLOW && this.checkESP(event.getEntity())) {
            event.cancel();
        }
    }

    @EventListener
    public void onTeamColor(TeamColorEvent event) {
        if (this.modeConfig.getValue() == ESPMode.GLOW && this.checkESP(event.getEntity())) {
            event.cancel();
            event.setColor(this.getESPColor(event.getEntity()).getRGB());
        }
    }

    public ColorConfig getStorageESPColor(BlockEntity tileEntity) {
        if (tileEntity instanceof ChestBlockEntity) {
            return (ColorConfig)this.chestsColorConfig;
        }
        if (tileEntity instanceof EnderChestBlockEntity) {
            return (ColorConfig)this.echestsColorConfig;
        }
        if (tileEntity instanceof ShulkerBoxBlockEntity) {
            return (ColorConfig)this.shulkersColorConfig;
        }
        if (tileEntity instanceof HopperBlockEntity) {
            return (ColorConfig)this.hoppersColorConfig;
        }
        if (tileEntity instanceof FurnaceBlockEntity) {
            return (ColorConfig)this.furnacesColorConfig;
        }
        return null;
    }

    public Color getESPColor(Entity entity) {
        if (entity instanceof PlayerEntity) {
            return this.playersColorConfig.getValue();
        }
        if (EntityUtil.isMonster(entity)) {
            return this.monstersColorConfig.getValue();
        }
        if (EntityUtil.isNeutral(entity) || EntityUtil.isPassive(entity)) {
            return this.animalsColorConfig.getValue();
        }
        if (EntityUtil.isVehicle(entity)) {
            return this.vehiclesColorConfig.getValue();
        }
        if (entity instanceof EndCrystalEntity) {
            return this.crystalsColorConfig.getValue();
        }
        if (entity instanceof ItemEntity) {
            return this.itemsColorConfig.getValue();
        }
        return null;
    }

    private boolean checkStorageESP(BlockEntity blockEntity) {
        if (blockEntity instanceof ChestBlockEntity && this.chestsConfig.getValue().booleanValue()) {
            return true;
        }
        if (blockEntity instanceof EnderChestBlockEntity && this.echestsConfig.getValue().booleanValue()) {
            return true;
        }
        if (blockEntity instanceof ShulkerBoxBlockEntity && this.shulkersConfig.getValue().booleanValue()) {
            return true;
        }
        if (blockEntity instanceof HopperBlockEntity && this.hoppersConfig.getValue().booleanValue()) {
            return true;
        }
        return blockEntity instanceof FurnaceBlockEntity && this.furnacesConfig.getValue();
    }

    public boolean checkESP(Entity entity) {
        if (entity instanceof PlayerEntity && this.playersConfig.getValue().booleanValue()) {
            return this.selfConfig.getValue() || entity != ESPModule.mc.player;
        }
        return EntityUtil.isMonster(entity) && this.monstersConfig.getValue() || (EntityUtil.isNeutral(entity) || EntityUtil.isPassive(entity)) && this.animalsConfig.getValue() || EntityUtil.isVehicle(entity) && this.vehiclesConfig.getValue() || entity instanceof EndCrystalEntity && this.crystalsConfig.getValue() || entity instanceof ItemEntity && this.itemsConfig.getValue();
    }

    public enum ESPMode {
        BOX,
        GLOW

    }
}
