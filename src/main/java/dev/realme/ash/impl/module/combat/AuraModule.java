package dev.realme.ash.impl.module.combat;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.NumberDisplay;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.EnumConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.RotationModule;
import dev.realme.ash.api.render.Interpolation;
import dev.realme.ash.api.render.RenderManager;
import dev.realme.ash.impl.event.network.DisconnectEvent;
import dev.realme.ash.impl.event.network.PacketEvent;
import dev.realme.ash.impl.event.network.PlayerTickEvent;
import dev.realme.ash.impl.event.render.RenderWorldEvent;
import dev.realme.ash.impl.event.world.RemoveEntityEvent;
import dev.realme.ash.impl.manager.tick.TickSync;
import dev.realme.ash.init.Managers;
import dev.realme.ash.init.Modules;
import dev.realme.ash.util.math.timer.CacheTimer;
import dev.realme.ash.util.math.timer.Timer;
import dev.realme.ash.util.player.InventoryUtil;
import dev.realme.ash.util.player.PlayerUtil;
import dev.realme.ash.util.player.RotationUtil;
import dev.realme.ash.util.world.EntityUtil;
import java.util.Comparator;
import java.util.stream.Stream;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.item.TridentItem;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class AuraModule
extends RotationModule {
    Config<Boolean> swingConfig = new BooleanConfig("Swing", "Swings the hand after attacking", true);
    Config<TargetMode> modeConfig = new EnumConfig("Mode", "The mode for targeting entities to attack", (Enum)TargetMode.SWITCH, (Enum[])TargetMode.values());
    Config<Priority> priorityConfig = new EnumConfig("Priority", "The value to prioritize when searching for targets", (Enum)Priority.HEALTH, (Enum[])Priority.values());
    Config<Float> searchRangeConfig = new NumberConfig<Float>("EnemyRange", "Range to search for targets", Float.valueOf(1.0f), Float.valueOf(5.0f), Float.valueOf(10.0f));
    Config<Float> rangeConfig = new NumberConfig<Float>("Range", "Range to attack entities", Float.valueOf(1.0f), Float.valueOf(4.5f), Float.valueOf(6.0f));
    Config<Float> wallRangeConfig = new NumberConfig<Float>("WallRange", "Range to attack entities through walls", Float.valueOf(1.0f), Float.valueOf(4.5f), Float.valueOf(6.0f));
    Config<Boolean> vanillaRangeConfig = new BooleanConfig("VanillaRange", "Only attack within vanilla range", false);
    Config<Float> fovConfig = new NumberConfig<Float>("FOV", "Field of view to attack entities", Float.valueOf(1.0f), Float.valueOf(180.0f), Float.valueOf(180.0f));
    Config<Boolean> attackDelayConfig = new BooleanConfig("AttackDelay", "Delays attacks according to minecraft hit delays for maximum damage per attack", true);
    Config<Float> attackSpeedConfig = new NumberConfig<Float>("AttackSpeed", "Delay for attacks (Only functions if AttackDelay is off)", Float.valueOf(1.0f), Float.valueOf(20.0f), Float.valueOf(20.0f), () -> this.attackDelayConfig.getValue() == false);
    Config<Float> randomSpeedConfig = new NumberConfig<Float>("RandomSpeed", "Randomized delay for attacks (Only functions if AttackDelay is off)", Float.valueOf(0.0f), Float.valueOf(0.0f), Float.valueOf(10.0f), () -> this.attackDelayConfig.getValue() == false);
    Config<Float> swapDelayConfig = new NumberConfig<Float>("SwapPenalty", "Delay for attacking after swapping items which prevents NCP flags", Float.valueOf(0.0f), Float.valueOf(0.0f), Float.valueOf(10.0f));
    Config<TickSync> tpsSyncConfig = new EnumConfig("TPS-Sync", "Syncs the attacks with the server TPS", (Enum)TickSync.NONE, (Enum[])TickSync.values());
    Config<Boolean> awaitCritsConfig = new BooleanConfig("AwaitCriticals", "Aura will wait for a critical hit when falling", false);
    Config<Boolean> autoSwapConfig = new BooleanConfig("AutoSwap", "Automatically swaps to a weapon before attacking", true);
    Config<Boolean> swordCheckConfig = new BooleanConfig("Sword-Check", "Checks if a weapon is in the hand before attacking", true);
    Config<Vector> hitVectorConfig = new EnumConfig("HitVector", "The vector to aim for when attacking entities", (Enum)Vector.FEET, (Enum[])Vector.values());
    Config<Boolean> rotateConfig = new BooleanConfig("Rotate", "Rotate before attacking", false);
    Config<Boolean> silentRotateConfig = new BooleanConfig("RotateSilent", "Rotates silently to server", false, () -> this.rotateConfig.getValue());
    Config<Boolean> strictRotateConfig = new BooleanConfig("YawStep", "Rotates yaw over multiple ticks to prevent certain rotation flags in NCP", false, () -> this.rotateConfig.getValue());
    Config<Integer> rotateLimitConfig = new NumberConfig<Integer>("YawStep-Limit", "Maximum yaw rotation in degrees for one tick", Integer.valueOf(1), Integer.valueOf(180), Integer.valueOf(180), NumberDisplay.DEGREES, () -> this.rotateConfig.getValue() != false && this.strictRotateConfig.getValue() != false);
    Config<Integer> ticksExistedConfig = new NumberConfig<Integer>("TicksExisted", "The minimum age of the entity to be considered for attack", 0, 50, 200);
    Config<Boolean> armorCheckConfig = new BooleanConfig("ArmorCheck", "Checks if target has armor before attacking", false);
    Config<Boolean> stopSprintConfig = new BooleanConfig("StopSprint", "Stops sprinting before attacking to maintain vanilla behavior", false);
    Config<Boolean> stopShieldConfig = new BooleanConfig("StopShield", "Automatically handles shielding before attacking", false);
    Config<Boolean> playersConfig = new BooleanConfig("Players", "Target players", true);
    Config<Boolean> monstersConfig = new BooleanConfig("Monsters", "Target monsters", false);
    Config<Boolean> neutralsConfig = new BooleanConfig("Neutrals", "Target neutrals", false);
    Config<Boolean> animalsConfig = new BooleanConfig("Animals", "Target animals", false);
    Config<Boolean> invisiblesConfig = new BooleanConfig("Invisibles", "Target invisible entities", true);
    Config<Boolean> renderConfig = new BooleanConfig("Render", "Renders an indicator over the target", true);
    Config<Boolean> disableDeathConfig = new BooleanConfig("DisableOnDeath", "Disables during disconnect/death", false);
    private Entity entityTarget;
    private long randomDelay = -1L;
    private boolean shielding;
    private boolean sneaking;
    private boolean sprinting;
    private final Timer attackTimer = new CacheTimer();
    private final Timer critTimer = new CacheTimer();
    private final Timer autoSwapTimer = new CacheTimer();
    private final Timer switchTimer = new CacheTimer();
    private boolean rotated;
    private float[] silentRotations;

    public AuraModule() {
        super("Aura", "Attacks nearby entities", ModuleCategory.COMBAT, 700);
    }

    @Override
    public String getModuleData() {
        return this.entityTarget == null ? null : this.entityTarget.getName().getString();
    }

    @Override
    public void onDisable() {
        this.entityTarget = null;
        this.silentRotations = null;
    }

    @EventListener
    public void onDisconnect(DisconnectEvent event) {
        if (this.disableDeathConfig.getValue().booleanValue()) {
            this.disable();
        }
    }

    @EventListener
    public void onRemoveEntity(RemoveEntityEvent event) {
        if (this.disableDeathConfig.getValue().booleanValue() && event.getEntity() == AuraModule.mc.player) {
            this.disable();
        }
    }

    @EventListener
    public void onPlayerUpdate(PlayerTickEvent event) {
        int slot;
        Vec3d eyepos = Managers.POSITION.getEyePos();
        switch (this.modeConfig.getValue()) {
            case SWITCH: {
                this.entityTarget = this.getAttackTarget(eyepos);
                break;
            }
            case SINGLE: {
                if (this.entityTarget != null && this.entityTarget.isAlive() && this.isInAttackRange(eyepos, this.entityTarget)) break;
                this.entityTarget = this.getAttackTarget(eyepos);
            }
        }
        if (this.entityTarget == null || !this.switchTimer.passed(Float.valueOf(this.swapDelayConfig.getValue().floatValue() * 25.0f))) {
            this.silentRotations = null;
            return;
        }
        if (AuraModule.mc.player.isUsingItem() && AuraModule.mc.player.getActiveHand() == Hand.MAIN_HAND || AuraModule.mc.options.attackKey.isPressed() || PlayerUtil.isHotbarKeysPressed()) {
            this.autoSwapTimer.reset();
        }
        boolean sword = AuraModule.mc.player.getMainHandStack().getItem() instanceof SwordItem;
        if (this.autoSwapConfig.getValue().booleanValue() && this.autoSwapTimer.passed(500) && !sword && (slot = this.getSwordSlot()) != -1) {
            InventoryUtil.doSwap(slot);
        }
        if (!this.isHoldingSword()) {
            return;
        }
        if (this.rotateConfig.getValue().booleanValue()) {
            float[] rotation = RotationUtil.getRotationsTo(AuraModule.mc.player.getEyePos(), this.getAttackRotateVec(this.entityTarget));
            if (!this.silentRotateConfig.getValue().booleanValue() && this.strictRotateConfig.getValue().booleanValue()) {
                float yaw;
                float serverYaw = Managers.ROTATION.getWrappedYaw();
                float diff = serverYaw - rotation[0];
                float diff1 = Math.abs(diff);
                if (diff1 > 180.0f) {
                    diff += diff > 0.0f ? -360.0f : 360.0f;
                }
                int dir = diff > 0.0f ? -1 : 1;
                float deltaYaw = dir * this.rotateLimitConfig.getValue();
                if (diff1 > (float)this.rotateLimitConfig.getValue().intValue()) {
                    yaw = serverYaw + deltaYaw;
                    this.rotated = false;
                } else {
                    yaw = rotation[0];
                    this.rotated = true;
                }
                rotation[0] = yaw;
            } else {
                this.rotated = true;
            }
            if (this.silentRotateConfig.getValue().booleanValue()) {
                this.silentRotations = rotation;
            } else {
                this.setRotation(rotation[0], rotation[1]);
            }
        }
        if (!this.shouldWaitCrit() || !this.rotated && this.rotateConfig.getValue().booleanValue() || !this.isInAttackRange(eyepos, this.entityTarget)) {
            return;
        }
        if (this.attackDelayConfig.getValue().booleanValue()) {
            float ticks = 20.0f - Managers.TICK.getTickSync(this.tpsSyncConfig.getValue());
            float progress = AuraModule.mc.player.getAttackCooldownProgress(ticks);
            if (progress >= 1.0f && this.attackTarget(this.entityTarget)) {
                AuraModule.mc.player.resetLastAttackedTicks();
            }
        } else {
            float delay;
            if (this.randomDelay < 0L) {
                this.randomDelay = (long)RANDOM.nextFloat(this.randomSpeedConfig.getValue().floatValue() * 10.0f + 1.0f);
            }
            if (this.attackTimer.passed(Float.valueOf(1000.0f - (delay = this.attackSpeedConfig.getValue().floatValue() * 50.0f + (float)this.randomDelay))) && this.attackTarget(this.entityTarget)) {
                this.randomDelay = -1L;
                this.attackTimer.reset();
            }
        }
    }

    @EventListener
    public void onPacketOutbound(PacketEvent.Send event) {
        if (AuraModule.mc.player == null) {
            return;
        }
        if (event.getPacket() instanceof UpdateSelectedSlotC2SPacket) {
            this.switchTimer.reset();
        }
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event) {
        if (this.entityTarget != null && this.renderConfig.getValue().booleanValue() && this.isHoldingSword()) {
            int attackDelay;
            float delay = this.attackSpeedConfig.getValue().floatValue() * 50.0f + (float)this.randomDelay;
            if (this.attackDelayConfig.getValue().booleanValue()) {
                float animFactor = 1.0f - AuraModule.mc.player.getAttackCooldownProgress(0.0f);
                attackDelay = (int)(100.0 * (double)animFactor);
            } else {
                float animFactor = 1.0f - MathHelper.clamp((float)this.attackTimer.getElapsedTime() / (1000.0f - delay), 0.0f, 1.0f);
                attackDelay = (int)(100.0 * (double)animFactor);
            }
            RenderManager.renderBox(event.getMatrices(), Interpolation.getInterpolatedEntityBox(this.entityTarget), Modules.CLIENT_SETTING.getRGB(60 + attackDelay));
            RenderManager.renderBoundingBox(event.getMatrices(), Interpolation.getInterpolatedEntityBox(this.entityTarget), 1.5f, Modules.CLIENT_SETTING.getRGB(145));
        }
    }

    private boolean attackTarget(Entity entity) {
        this.preAttackTarget();
        PlayerInteractEntityC2SPacket packet = PlayerInteractEntityC2SPacket.attack(entity, Managers.POSITION.isSneaking());
        Managers.NETWORK.sendPacket(packet);
        this.postAttackTarget(entity);
        if (this.swingConfig.getValue().booleanValue()) {
            AuraModule.mc.player.swingHand(Hand.MAIN_HAND);
        } else {
            Managers.NETWORK.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        }
        return true;
    }

    private int getSwordSlot() {
        float sharp = 0.0f;
        int slot = -1;
        for (int i = 0; i < 9; ++i) {
            float sharpness;
            float dmg;
            ItemStack stack = AuraModule.mc.player.getInventory().getStack(i);
            Item item = stack.getItem();
            if (item instanceof SwordItem) {
                SwordItem swordItem = (SwordItem)((Object)item);
                float sharpness2 = (float)EnchantmentHelper.getLevel(Enchantments.SHARPNESS, stack) * 0.5f + 0.5f;
                dmg = swordItem.getAttackDamage() + sharpness2;
                if (!(dmg > sharp)) continue;
                sharp = dmg;
                slot = i;
                continue;
            }
            Item sharpness2 = stack.getItem();
            if (sharpness2 instanceof AxeItem) {
                AxeItem axeItem = (AxeItem)((Object)sharpness2);
                float sharpness3 = (float)EnchantmentHelper.getLevel(Enchantments.SHARPNESS, stack) * 0.5f + 0.5f;
                dmg = axeItem.getAttackDamage() + sharpness3;
                if (!(dmg > sharp)) continue;
                sharp = dmg;
                slot = i;
                continue;
            }
            if (!(stack.getItem() instanceof TridentItem) || !((dmg = 8.0f + (sharpness = (float)EnchantmentHelper.getLevel(Enchantments.SHARPNESS, stack) * 0.5f + 0.5f)) > sharp)) continue;
            sharp = dmg;
            slot = i;
        }
        return slot;
    }

    private void preAttackTarget() {
        ItemStack offhand = AuraModule.mc.player.getOffHandStack();
        this.shielding = false;
        if (this.stopShieldConfig.getValue().booleanValue()) {
            boolean bl = this.shielding = offhand.getItem() == Items.SHIELD && AuraModule.mc.player.isBlocking();
            if (this.shielding) {
                Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, Managers.POSITION.getBlockPos(), Direction.getFacing(AuraModule.mc.player.getX(), AuraModule.mc.player.getY(), AuraModule.mc.player.getZ())));
            }
        }
        this.sneaking = false;
        this.sprinting = false;
        if (this.stopSprintConfig.getValue().booleanValue()) {
            this.sneaking = Managers.POSITION.isSneaking();
            if (this.sneaking) {
                Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(AuraModule.mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
            }
            this.sprinting = Managers.POSITION.isSprinting();
            if (this.sprinting) {
                Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(AuraModule.mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
            }
        }
    }

    private void postAttackTarget(Entity entity) {
        if (this.shielding) {
            Managers.NETWORK.sendSequencedPacket(s -> new PlayerInteractItemC2SPacket(Hand.OFF_HAND, s));
        }
        if (this.sneaking) {
            Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(AuraModule.mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
        }
        if (this.sprinting) {
            Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(AuraModule.mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
        }
        if (Modules.CRITICALS.isEnabled() && this.critTimer.passed(500)) {
            if (!AuraModule.mc.player.isOnGround() || AuraModule.mc.player.isRiding() || AuraModule.mc.player.isSubmergedInWater() || AuraModule.mc.player.isInLava() || AuraModule.mc.player.isHoldingOntoLadder() || AuraModule.mc.player.hasStatusEffect(StatusEffects.BLINDNESS) || AuraModule.mc.player.input.jumping) {
                return;
            }
            this.critTimer.reset();
            AuraModule.mc.player.addCritParticles(entity);
        }
    }

    private Entity getAttackTarget(Vec3d pos) {
        double min = Double.MAX_VALUE;
        Entity attackTarget = null;
        block5: for (Entity entity : AuraModule.mc.world.getEntities()) {
            double dist;
            PlayerEntity player;
            if (entity == null || entity == AuraModule.mc.player || !entity.isAlive() || !this.isEnemy(entity) || Managers.SOCIAL.isFriend(entity.getName()) || entity instanceof EndCrystalEntity || entity instanceof ItemEntity || entity instanceof ArrowEntity || entity instanceof ExperienceBottleEntity || entity instanceof PlayerEntity && (player = (PlayerEntity)entity).isCreative() || this.armorCheckConfig.getValue().booleanValue() && entity instanceof LivingEntity && !entity.getArmorItems().iterator().hasNext() || !((dist = pos.distanceTo(entity.getPos())) <= (double)this.searchRangeConfig.getValue().floatValue()) || entity.age < this.ticksExistedConfig.getValue()) continue;
            switch (this.priorityConfig.getValue()) {
                case DISTANCE: {
                    if (!(dist < min)) break;
                    min = dist;
                    attackTarget = entity;
                    break;
                }
                case HEALTH: {
                    LivingEntity e;
                    float health;
                    if (!(entity instanceof LivingEntity) || !((double)(health = (e = (LivingEntity)entity).getHealth() + e.getAbsorptionAmount()) < min)) continue block5;
                    min = health;
                    attackTarget = entity;
                    break;
                }
                case ARMOR: {
                    float armor;
                    LivingEntity e;
                    if (!(entity instanceof LivingEntity) || !((double)(armor = this.getArmorDurability(e = (LivingEntity)entity)) < min)) break;
                    min = armor;
                    attackTarget = entity;
                }
            }
        }
        return attackTarget;
    }

    private float getArmorDurability(LivingEntity e) {
        float edmg = 0.0f;
        float emax = 0.0f;
        for (ItemStack armor : e.getArmorItems()) {
            if (armor == null || armor.isEmpty()) continue;
            edmg += (float)armor.getDamage();
            emax += (float)armor.getMaxDamage();
        }
        return 100.0f - edmg / emax;
    }

    public boolean isInAttackRange(Vec3d pos, Entity entity) {
        Vec3d entityPos = this.getAttackRotateVec(entity);
        double dist = pos.distanceTo(entityPos);
        return this.isInAttackRange(dist, pos, entityPos);
    }

    public boolean isInAttackRange(double dist, Vec3d pos, Vec3d entityPos) {
        if (this.vanillaRangeConfig.getValue().booleanValue() && dist > 3.0) {
            return false;
        }
        if (dist > (double)this.rangeConfig.getValue().floatValue()) {
            return false;
        }
        BlockHitResult result = AuraModule.mc.world.raycast(new RaycastContext(pos, entityPos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, AuraModule.mc.player));
        if (result != null && dist > (double)this.wallRangeConfig.getValue().floatValue()) {
            return false;
        }
        if (this.fovConfig.getValue().floatValue() != 180.0f) {
            float[] rots = RotationUtil.getRotationsTo(pos, entityPos);
            float diff = MathHelper.wrapDegrees(AuraModule.mc.player.getYaw()) - rots[0];
            float magnitude = Math.abs(diff);
            return magnitude <= this.fovConfig.getValue().floatValue();
        }
        return true;
    }

    public boolean isHoldingSword() {
        return this.swordCheckConfig.getValue() == false || AuraModule.mc.player.getMainHandStack().getItem() instanceof SwordItem || AuraModule.mc.player.getMainHandStack().getItem() instanceof AxeItem || AuraModule.mc.player.getMainHandStack().getItem() instanceof TridentItem;
    }

    public boolean shouldWaitCrit() {
        return !AuraModule.mc.player.isOnGround() && AuraModule.mc.player.fallDistance > 0.0f && AuraModule.mc.player.fallDistance < 1.0f && !AuraModule.mc.player.hasStatusEffect(StatusEffects.BLINDNESS) && !AuraModule.mc.player.isClimbing() && !AuraModule.mc.player.isTouchingWater() || this.awaitCritsConfig.getValue() == false || !AuraModule.mc.options.jumpKey.isPressed();
    }

    private Vec3d getAttackRotateVec(Entity entity) {
        Vec3d feetPos = entity.getPos();
        return switch (this.hitVectorConfig.getValue()) {
            default -> throw new IncompatibleClassChangeError();
            case FEET -> feetPos;
            case TORSO -> feetPos.add(0.0, entity.getHeight() / 2.0f, 0.0);
            case EYES -> entity.getEyePos();
            case AUTO -> {
                Vec3d torsoPos = feetPos.add(0.0, entity.getHeight() / 2.0f, 0.0);
                Vec3d eyesPos = entity.getEyePos();
                yield Stream.of(feetPos, torsoPos, eyesPos).min(Comparator.comparing(b -> AuraModule.mc.player.getEyePos().squaredDistanceTo((Vec3d)b))).orElse(eyesPos);
            }
        };
    }

    private boolean isEnemy(Entity e) {
        return (!e.isInvisible() || this.invisiblesConfig.getValue() != false) && e instanceof PlayerEntity && this.playersConfig.getValue() != false || EntityUtil.isMonster(e) && this.monstersConfig.getValue() != false || EntityUtil.isNeutral(e) && this.neutralsConfig.getValue() != false || EntityUtil.isPassive(e) && this.animalsConfig.getValue() != false;
    }

    public Entity getEntityTarget() {
        return this.entityTarget;
    }

    public static enum TargetMode {
        SWITCH,
        SINGLE;

    }

    public static enum Priority {
        HEALTH,
        DISTANCE,
        ARMOR;

    }

    public static enum Vector {
        EYES,
        TORSO,
        FEET,
        AUTO;

    }
}
