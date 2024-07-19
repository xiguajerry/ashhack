// Decompiled with: FernFlower
// Class Version: 17
package dev.realme.ash.impl.module.combat;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.event.EventStage;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.RotationModule;
import dev.realme.ash.impl.event.entity.LookDirectionEvent;
import dev.realme.ash.impl.event.network.UpdateWalkingEvent;
import dev.realme.ash.init.Managers;
import dev.realme.ash.util.world.EntityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;

public class BowAimModule extends RotationModule {
    Config<Boolean> playersConfig = new BooleanConfig("Players", "Aims bow at players", true);
    Config<Boolean> monstersConfig = new BooleanConfig("Monsters", "Aims bow at monsters", false);
    Config<Boolean> neutralsConfig = new BooleanConfig("Neutrals", "Aims bow at neutrals", false);
    Config<Boolean> animalsConfig = new BooleanConfig("Animals", "Aims bow at animals", false);
    Config<Boolean> invisiblesConfig = new BooleanConfig("Invisibles", "Aims bow at invisible entities", false);
    private Entity aimTarget;

    public BowAimModule() {
        super("BowAim", "Automatically aims charged bow at nearby entities", ModuleCategory.COMBAT);
    }

    @EventListener
    public void onPlayerUpdate(UpdateWalkingEvent event) {
        if (event.getStage() == EventStage.PRE) {
            this.aimTarget = null;
            if (mc.player.getMainHandStack().getItem() instanceof BowItem && mc.player.getItemUseTime() >= 3) {
                double minDist = Double.MAX_VALUE;

                for(Entity entity : mc.world.getEntities()) {
                    if (entity != null && entity != mc.player && entity.isAlive() && this.isValidAimTarget(entity) && !Managers.SOCIAL.isFriend(entity.getName())) {
                        double dist = (double)mc.player.distanceTo(entity);
                        if (dist < minDist) {
                            minDist = dist;
                            this.aimTarget = entity;
                        }
                    }
                }

                Entity var9 = this.aimTarget;
                if (var9 instanceof LivingEntity) {
                    LivingEntity target = (LivingEntity)var9;
                    float[] rotations = this.getBowRotationsTo(target);
                    this.setRotationClient(rotations[0], rotations[1]);
                }
            }

        }
    }

    @EventListener
    public void onLookDirection(LookDirectionEvent event) {
        if (this.aimTarget != null) {
            event.cancel();
        }

    }

    private float[] getBowRotationsTo(Entity entity) {
        float duration = (float)(mc.player.getActiveItem().getMaxUseTime() - mc.player.getItemUseTime()) / 20.0F;
        duration = (duration * duration + duration * 2.0F) / 3.0F;
        if (duration >= 1.0F) {
            duration = 1.0F;
        }

        double duration1 = (double)(duration * 3.0F);
        double coeff = (double)0.05F;
        float pitch = (float)(-Math.toDegrees((double)this.calculateArc(entity, duration1, coeff)));
        double ix = entity.getX() - entity.prevX;
        double iz = entity.getZ() - entity.prevZ;
        double d = (double)mc.player.distanceTo(entity);
        d -= d % 2.0D;
        ix = d / 2.0D * ix * (mc.player.isSprinting() ? 1.3D : 1.1D);
        iz = d / 2.0D * iz * (mc.player.isSprinting() ? 1.3D : 1.1D);
        float yaw = (float)Math.toDegrees(Math.atan2(entity.getZ() + iz - mc.player.getZ(), entity.getX() + ix - mc.player.getX())) - 90.0F;
        return new float[]{yaw, pitch};
    }

    private float calculateArc(Entity target, double duration, double coeff) {
        double yArc = target.getY() + (double)(target.getStandingEyeHeight() / 2.0F) - (mc.player.getY() + (double)mc.player.getStandingEyeHeight());
        double dX = target.getX() - mc.player.getX();
        double dZ = target.getZ() - mc.player.getZ();
        double dirRoot = Math.sqrt(dX * dX + dZ * dZ);
        return this.calculateArc(duration, coeff, dirRoot, yArc);
    }

    private float calculateArc(double duration, double coeff, double root, double yArc) {
        double dirCoeff = coeff * root * root;
        yArc = 2.0D * yArc * duration * duration;
        yArc = coeff * (dirCoeff + yArc);
        yArc = Math.sqrt(duration * duration * duration * duration - yArc);
        duration = duration * duration - yArc;
        yArc = Math.atan2(duration * duration + yArc, coeff * root);
        duration = Math.atan2(duration, coeff * root);
        return (float)Math.min(yArc, duration);
    }

    private boolean isValidAimTarget(Entity entity) {
        if (entity.isInvisible() && !this.invisiblesConfig.getValue()) {
            return false;
        } else {
            return entity instanceof PlayerEntity && this.playersConfig.getValue() || EntityUtil.isMonster(entity) && this.monstersConfig.getValue() || EntityUtil.isNeutral(entity) && this.neutralsConfig.getValue() || EntityUtil.isPassive(entity) && this.animalsConfig.getValue();
        }
    }
}
 