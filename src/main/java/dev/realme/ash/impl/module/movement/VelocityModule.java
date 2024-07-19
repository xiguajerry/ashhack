package dev.realme.ash.impl.module.movement;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.NumberDisplay;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.EnumConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.EventStage;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.TickEvent;
import dev.realme.ash.impl.event.entity.player.PushEntityEvent;
import dev.realme.ash.impl.event.entity.player.PushFluidsEvent;
import dev.realme.ash.impl.event.network.PacketEvent;
import dev.realme.ash.impl.event.network.PushOutOfBlocksEvent;
import dev.realme.ash.init.Managers;
import dev.realme.ash.mixin.accessor.AccessorClientWorld;
import dev.realme.ash.mixin.accessor.AccessorEntityVelocityUpdateS2CPacket;
import dev.realme.ash.mixin.accessor.AccessorExplosionS2CPacket;
import dev.realme.ash.util.string.EnumFormatter;
import java.text.DecimalFormat;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Direction;

public class VelocityModule
extends ToggleModule {
    final Config<Boolean> knockbackConfig = new BooleanConfig("Knockback", "Removes player knockback velocity", true);
    final Config<Boolean> explosionConfig = new BooleanConfig("Explosion", "Removes player explosion velocity", true);
    final Config<VelocityMode> modeConfig = new EnumConfig<>("Mode", "The mode for velocity", VelocityMode.NORMAL, VelocityMode.values());
    final Config<Float> horizontalConfig = new NumberConfig<>("Horizontal", "How much horizontal knock-back to take", 0.0f, 0.0f, 100.0f, NumberDisplay.PERCENT, () -> this.modeConfig.getValue() == VelocityMode.NORMAL);
    final Config<Float> verticalConfig = new NumberConfig<>("Vertical", "How much vertical knock-back to take", 0.0f, 0.0f, 100.0f, NumberDisplay.PERCENT, () -> this.modeConfig.getValue() == VelocityMode.NORMAL);
    final Config<Boolean> pushEntitiesConfig = new BooleanConfig("NoPush-Entities", "Prevents being pushed away from entities", true);
    final Config<Boolean> pushBlocksConfig = new BooleanConfig("NoPush-Blocks", "Prevents being pushed out of blocks", true);
    final Config<Boolean> pushLiquidsConfig = new BooleanConfig("NoPush-Liquids", "Prevents being pushed by flowing liquids", true);
    final Config<Boolean> pushFishhookConfig = new BooleanConfig("NoPush-Fishhook", "Prevents being pulled by fishing rod hooks", true);
    private boolean cancelVelocity;

    public VelocityModule() {
        super("Velocity", "Reduces the amount of player knockback velocity", ModuleCategory.MOVEMENT);
    }

    @Override
    public String getModuleData() {
        if (this.modeConfig.getValue() == VelocityMode.NORMAL) {
            DecimalFormat decimal = new DecimalFormat("0.0");
            return String.format("H:%s%%, V:%s%%", decimal.format(this.horizontalConfig.getValue()), decimal.format(this.verticalConfig.getValue()));
        }
        return EnumFormatter.formatEnum(this.modeConfig.getValue());
    }

    @Override
    public void onEnable() {
        this.cancelVelocity = false;
    }

    @Override
    public void onDisable() {
        if (this.cancelVelocity) {
            if (this.modeConfig.getValue() == VelocityMode.GRIM) {
                if (!VelocityModule.mc.player.isCrawling()) {
                    Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, VelocityModule.mc.player.getBlockPos().up(), Direction.DOWN));
                }
                Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, VelocityModule.mc.player.isCrawling() ? VelocityModule.mc.player.getBlockPos() : VelocityModule.mc.player.getBlockPos().up(), Direction.DOWN));
            }
            this.cancelVelocity = false;
        }
    }

    /*
     * Enabled aggressive block sorting
     */
    @EventListener
    public void onPacketInbound(PacketEvent.Receive event) {
        if (VelocityModule.mc.player == null) return;
        if (VelocityModule.mc.world == null) {
            return;
        }
        Packet<?> packet = event.getPacket();
        if (packet instanceof EntityVelocityUpdateS2CPacket) {
            EntityVelocityUpdateS2CPacket packet2 = (EntityVelocityUpdateS2CPacket) packet;
            if (this.knockbackConfig.getValue()) {
                if (packet2.getId() != VelocityModule.mc.player.getId()) {
                    return;
                }
                switch (this.modeConfig.getValue()) {
                    case NORMAL: {
                        if (this.horizontalConfig.getValue() == 0.0f && this.verticalConfig.getValue() == 0.0f) {
                            event.cancel();
                            return;
                        }
                        ((AccessorEntityVelocityUpdateS2CPacket) packet2).setVelocityX((int)((float)packet2.getVelocityX() * (this.horizontalConfig.getValue() / 100.0f)));
                        ((AccessorEntityVelocityUpdateS2CPacket) packet2).setVelocityY((int)((float)packet2.getVelocityY() * (this.verticalConfig.getValue() / 100.0f)));
                        ((AccessorEntityVelocityUpdateS2CPacket) packet2).setVelocityZ((int)((float)packet2.getVelocityZ() * (this.horizontalConfig.getValue() / 100.0f)));
                        return;
                    }
                    case GRIM: {
                        event.cancel();
                        this.cancelVelocity = true;
                        return;
                    }
                }
                return;
            }
        }
        if ((packet = event.getPacket()) instanceof ExplosionS2CPacket) {
            ExplosionS2CPacket packet3 = (ExplosionS2CPacket) packet;
            if (this.explosionConfig.getValue()) {
                switch (this.modeConfig.getValue()) {
                    case NORMAL: {
                        if (this.horizontalConfig.getValue() == 0.0f && this.verticalConfig.getValue() == 0.0f) {
                            event.cancel();
                            break;
                        }
                        ((AccessorExplosionS2CPacket) packet3).setPlayerVelocityX(packet3.getPlayerVelocityX() * (this.horizontalConfig.getValue() / 100.0f));
                        ((AccessorExplosionS2CPacket) packet3).setPlayerVelocityY(packet3.getPlayerVelocityY() * (this.verticalConfig.getValue() / 100.0f));
                        ((AccessorExplosionS2CPacket) packet3).setPlayerVelocityZ(packet3.getPlayerVelocityZ() * (this.horizontalConfig.getValue() / 100.0f));
                        break;
                    }
                    case GRIM: {
                        ((AccessorExplosionS2CPacket) packet3).setPlayerVelocityX(0.0f);
                        ((AccessorExplosionS2CPacket) packet3).setPlayerVelocityY(0.0f);
                        ((AccessorExplosionS2CPacket) packet3).setPlayerVelocityZ(0.0f);
                        event.cancel();
                        this.cancelVelocity = true;
                        break;
                    }
                }
                if (!event.isCanceled()) return;
                mc.executeSync(() -> ((AccessorClientWorld) VelocityModule.mc.world).hookPlaySound(packet3.getX(), packet3.getY(), packet3.getZ(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0f, (1.0f + (RANDOM.nextFloat() - RANDOM.nextFloat()) * 0.2f) * 0.7f, false, RANDOM.nextLong()));
                return;
            }
        }
        if (!((packet = event.getPacket()) instanceof EntityStatusS2CPacket)) return;
        EntityStatusS2CPacket packet4 = (EntityStatusS2CPacket) packet;
        if (packet4.getStatus() != 31) return;
        if (!this.pushFishhookConfig.getValue()) return;
        Entity entity = packet4.getEntity(VelocityModule.mc.world);
        if (!(entity instanceof FishingBobberEntity)) return;
        FishingBobberEntity hook = (FishingBobberEntity) entity;
        if (hook.getHookedEntity() != VelocityModule.mc.player) return;
        event.cancel();
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (event.getStage() == EventStage.PRE && this.cancelVelocity) {
            if (this.modeConfig.getValue() == VelocityMode.GRIM) {
                Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.Full(VelocityModule.mc.player.getX(), VelocityModule.mc.player.getY(), VelocityModule.mc.player.getZ(), VelocityModule.mc.player.getYaw(), VelocityModule.mc.player.getPitch(), VelocityModule.mc.player.isOnGround()));
                Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, VelocityModule.mc.player.isCrawling() ? VelocityModule.mc.player.getBlockPos() : VelocityModule.mc.player.getBlockPos().up(), Direction.DOWN));
            }
            this.cancelVelocity = false;
        }
    }

    @EventListener
    public void onPushEntity(PushEntityEvent event) {
        if (this.pushEntitiesConfig.getValue() && event.getPushed().equals(VelocityModule.mc.player)) {
            event.cancel();
        }
    }

    @EventListener
    public void onPushOutOfBlocks(PushOutOfBlocksEvent event) {
        if (this.pushBlocksConfig.getValue()) {
            event.cancel();
        }
    }

    @EventListener
    public void onPushFluid(PushFluidsEvent event) {
        if (this.pushLiquidsConfig.getValue()) {
            event.cancel();
        }
    }

    private enum VelocityMode {
        NORMAL,
        GRIM

    }
}
