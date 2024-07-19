package dev.realme.ash.impl.module.render;

import com.google.common.collect.Lists;
import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.EnumConfig;
import dev.realme.ash.api.event.EventStage;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.TickEvent;
import dev.realme.ash.impl.event.chunk.light.RenderSkylightEvent;
import dev.realme.ash.impl.event.gui.hud.RenderOverlayEvent;
import dev.realme.ash.impl.event.network.PacketEvent;
import dev.realme.ash.impl.event.particle.ParticleEvent;
import dev.realme.ash.impl.event.render.HurtCamEvent;
import dev.realme.ash.impl.event.render.RenderFloatingItemEvent;
import dev.realme.ash.impl.event.render.RenderFogEvent;
import dev.realme.ash.impl.event.render.RenderNauseaEvent;
import dev.realme.ash.impl.event.render.RenderWorldBorderEvent;
import dev.realme.ash.impl.event.render.block.RenderTileEntityEvent;
import dev.realme.ash.impl.event.render.entity.RenderArmorEvent;
import dev.realme.ash.impl.event.render.entity.RenderItemEvent;
import dev.realme.ash.impl.event.render.entity.RenderWitherSkullEvent;
import dev.realme.ash.impl.event.toast.RenderToastEvent;
import dev.realme.ash.impl.event.world.BlindnessEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;

public class NoRenderModule
extends ToggleModule {
    Config<Boolean> hurtCamConfig = new BooleanConfig("NoHurtCam", "Prevents the hurt camera shake effect from rendering", true);
    Config<Boolean> antiCrashConfig = new BooleanConfig("NoServerCrash", "Prevents server packets from crashing the client", false);
    Config<Boolean> armorConfig = new BooleanConfig("Armor", "Prevents armor pieces from rendering", false);
    Config<Boolean> fireOverlayConfig = new BooleanConfig("Overlay-Fire", "Prevents the fire Hud overlay from rendering", true);
    Config<Boolean> waterOverlayConfig = new BooleanConfig("Overlay-Water", "Prevents the water Hud overlay from rendering", true);
    Config<Boolean> blockOverlayConfig = new BooleanConfig("Overlay-Block", "Prevents the block Hud overlay from rendering", true);
    Config<Boolean> spyglassOverlayConfig = new BooleanConfig("Overlay-Spyglass", "Prevents the spyglass Hud overlay from rendering", false);
    Config<Boolean> pumpkinOverlayConfig = new BooleanConfig("Overlay-Pumpkin", "Prevents the pumpkin Hud overlay from rendering", true);
    Config<Boolean> bossOverlayConfig = new BooleanConfig("Overlay-BossBar", "Prevents the boss bar Hud overlay from rendering", true);
    Config<Boolean> nauseaConfig = new BooleanConfig("Nausea", "Prevents nausea effect from rendering (includes portal effect)", false);
    Config<Boolean> blindnessConfig = new BooleanConfig("Blindness", "Prevents blindness effect from rendering", false);
    Config<Boolean> frostbiteConfig = new BooleanConfig("Frostbite", "Prevents frostbite effect from rendering", false);
    Config<Boolean> skylightConfig = new BooleanConfig("Skylight", "Prevents skylight from rendering", true);
    Config<Boolean> witherSkullsConfig = new BooleanConfig("WitherSkulls", "Prevents flying wither skulls from rendering", false);
    Config<Boolean> tileEntitiesConfig = new BooleanConfig("TileEntities", "Prevents special tile entity properties from rendering (i.e. enchantment table books or cutting table saws)", false);
    Config<Boolean> explosionsConfig = new BooleanConfig("Explosions", "Prevents explosion particles from rendering", true);
    Config<Boolean> campfiresConfig = new BooleanConfig("Campfires", "Prevents campfire particles from rendering", false);
    Config<Boolean> totemConfig = new BooleanConfig("Totems", "Prevents totem particles from rendering", false);
    Config<Boolean> worldBorderConfig = new BooleanConfig("WorldBorder", "Prevents world border from rendering", false);
    Config<Boolean> interpolationConfig = new BooleanConfig("Interpolation", "Entities will be rendered at their server positions", false);
    Config<FogRender> fogConfig = new EnumConfig("Fog", "Prevents fog from rendering in the world", (Enum)FogRender.OFF, (Enum[])FogRender.values());
    Config<ItemRender> itemsConfig = new EnumConfig("Items", "Prevents dropped items from rendering", (Enum)ItemRender.OFF, (Enum[])ItemRender.values());
    Config<Boolean> guiToastConfig = new BooleanConfig("GuiToast", "Prevents advancements from rendering", true);

    public NoRenderModule() {
        super("NoRender", "Prevents certain game elements from rendering", ModuleCategory.RENDER);
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (this.itemsConfig.getValue() == ItemRender.REMOVE && event.getStage() == EventStage.PRE) {
            for (Entity entity : Lists.newArrayList(NoRenderModule.mc.world.getEntities())) {
                if (!(entity instanceof ItemEntity)) continue;
                NoRenderModule.mc.world.removeEntity(entity.getId(), Entity.RemovalReason.DISCARDED);
            }
        }
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Receive event) {
        if (NoRenderModule.mc.world == null) {
            return;
        }
        if (this.antiCrashConfig.getValue().booleanValue()) {
            PlayerPositionLookS2CPacket packet;
            Packet<?> packet2 = event.getPacket();
            if (packet2 instanceof PlayerPositionLookS2CPacket && ((packet = (PlayerPositionLookS2CPacket)((Object)packet2)).getX() > 3.0E7 || packet.getY() > (double)NoRenderModule.mc.world.getTopY() || packet.getZ() > 3.0E7 || packet.getX() < -3.0E7 || packet.getY() < (double)NoRenderModule.mc.world.getBottomY() || packet.getZ() < -3.0E7)) {
                event.cancel();
            } else {
                ExplosionS2CPacket packet3;
                packet2 = event.getPacket();
                if (packet2 instanceof ExplosionS2CPacket && ((packet3 = (ExplosionS2CPacket)((Object)packet2)).getX() > 3.0E7 || packet3.getY() > (double)NoRenderModule.mc.world.getTopY() || packet3.getZ() > 3.0E7 || packet3.getX() < -3.0E7 || packet3.getY() < (double)NoRenderModule.mc.world.getBottomY() || packet3.getZ() < -3.0E7 || packet3.getRadius() > 1000.0f || packet3.getAffectedBlocks().size() > 1000 || packet3.getPlayerVelocityX() > 1000.0f || packet3.getPlayerVelocityY() > 1000.0f || packet3.getPlayerVelocityZ() > 1000.0f || packet3.getPlayerVelocityX() < -1000.0f || packet3.getPlayerVelocityY() < -1000.0f || packet3.getPlayerVelocityZ() < -1000.0f)) {
                    event.cancel();
                } else {
                    EntityVelocityUpdateS2CPacket packet4;
                    packet2 = event.getPacket();
                    if (packet2 instanceof EntityVelocityUpdateS2CPacket && ((packet4 = (EntityVelocityUpdateS2CPacket)((Object)packet2)).getVelocityX() > 1000 || packet4.getVelocityY() > 1000 || packet4.getVelocityZ() > 1000 || packet4.getVelocityX() < -1000 || packet4.getVelocityY() < -1000 || packet4.getVelocityZ() < -1000)) {
                        event.cancel();
                    } else {
                        ParticleS2CPacket packet5;
                        packet2 = event.getPacket();
                        if (packet2 instanceof ParticleS2CPacket && (packet5 = (ParticleS2CPacket)((Object)packet2)).getCount() > 500) {
                            event.cancel();
                        }
                    }
                }
            }
        }
    }

    @EventListener
    public void onHurtCam(HurtCamEvent event) {
        if (this.hurtCamConfig.getValue().booleanValue()) {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderArmor(RenderArmorEvent event) {
        if (this.armorConfig.getValue().booleanValue() && event.getEntity() instanceof PlayerEntity) {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderOverlayFire(RenderOverlayEvent.Fire event) {
        if (this.fireOverlayConfig.getValue().booleanValue()) {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderOverlayWater(RenderOverlayEvent.Water event) {
        if (this.waterOverlayConfig.getValue().booleanValue()) {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderOverlayBlock(RenderOverlayEvent.Block event) {
        if (this.blockOverlayConfig.getValue().booleanValue()) {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderOverlaySpyglass(RenderOverlayEvent.Spyglass event) {
        if (this.spyglassOverlayConfig.getValue().booleanValue()) {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderOverlayPumpkin(RenderOverlayEvent.Pumpkin event) {
        if (this.pumpkinOverlayConfig.getValue().booleanValue()) {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderOverlayBossBar(RenderOverlayEvent.BossBar event) {
        if (this.bossOverlayConfig.getValue().booleanValue()) {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderOverlayFrostbite(RenderOverlayEvent.Frostbite event) {
        if (this.frostbiteConfig.getValue().booleanValue()) {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderNausea(RenderNauseaEvent event) {
        if (this.nauseaConfig.getValue().booleanValue()) {
            event.cancel();
        }
    }

    @EventListener
    public void onBlindness(BlindnessEvent event) {
        if (this.blindnessConfig.getValue().booleanValue()) {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderSkylight(RenderSkylightEvent event) {
        if (this.skylightConfig.getValue().booleanValue()) {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderWitherSkull(RenderWitherSkullEvent event) {
        if (this.witherSkullsConfig.getValue().booleanValue()) {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderEnchantingTableBook(RenderTileEntityEvent.EnchantingTableBook event) {
        if (this.tileEntitiesConfig.getValue().booleanValue()) {
            event.cancel();
        }
    }

    @EventListener
    public void onParticle(ParticleEvent event) {
        if (this.explosionsConfig.getValue().booleanValue() && (event.getParticleType() == ParticleTypes.EXPLOSION || event.getParticleType() == ParticleTypes.EXPLOSION_EMITTER) || this.campfiresConfig.getValue().booleanValue() && event.getParticleType() == ParticleTypes.CAMPFIRE_COSY_SMOKE) {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderFloatingItem(RenderFloatingItemEvent event) {
        if (this.totemConfig.getValue().booleanValue() && event.getFloatingItem() == Items.TOTEM_OF_UNDYING) {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderWorldBorder(RenderWorldBorderEvent event) {
        if (this.worldBorderConfig.getValue().booleanValue()) {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderFog(RenderFogEvent event) {
        if (this.fogConfig.getValue() == FogRender.LIQUID_VISION && NoRenderModule.mc.player != null && NoRenderModule.mc.player.isSubmergedIn(FluidTags.LAVA)) {
            event.cancel();
        } else if (this.fogConfig.getValue() == FogRender.CLEAR) {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderItem(RenderItemEvent event) {
        if (this.itemsConfig.getValue() == ItemRender.HIDE) {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderToast(RenderToastEvent event) {
        if (this.guiToastConfig.getValue().booleanValue()) {
            event.cancel();
        }
    }

    public static enum FogRender {
        CLEAR,
        LIQUID_VISION,
        OFF;

    }

    public static enum ItemRender {
        REMOVE,
        HIDE,
        OFF;

    }
}
