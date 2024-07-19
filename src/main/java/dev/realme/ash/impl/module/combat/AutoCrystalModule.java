package dev.realme.ash.impl.module.combat;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.ColorConfig;
import dev.realme.ash.api.config.setting.EnumConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.api.render.RenderManager;
import dev.realme.ash.impl.event.RotateEvent;
import dev.realme.ash.impl.event.network.PlayerTickEvent;
import dev.realme.ash.impl.event.network.UpdateWalkingEvent;
import dev.realme.ash.impl.event.render.RenderWorldEvent;
import dev.realme.ash.init.Managers;
import dev.realme.ash.init.Modules;
import dev.realme.ash.util.math.DamageUtil;
import dev.realme.ash.util.math.timer.CacheTimer;
import dev.realme.ash.util.math.timer.Timer;
import dev.realme.ash.util.player.InventoryUtil;
import dev.realme.ash.util.player.PlayerUtil;
import dev.realme.ash.util.render.FadeUtils;
import dev.realme.ash.util.world.BlockPosX;
import dev.realme.ash.util.world.BlockUtil;
import dev.realme.ash.util.world.EntityUtil;
import java.awt.Color;
import java.util.Iterator;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class AutoCrystalModule
extends ToggleModule {
    Config<Float> updateDelay = new NumberConfig<Float>("UpdateDelay", "", Float.valueOf(0.0f), Float.valueOf(32.0f), Float.valueOf(500.0f));
    Config<Float> targetRange = new NumberConfig<Float>("TargetRange", "", Float.valueOf(0.0f), Float.valueOf(8.0f), Float.valueOf(13.0f));
    Config<Boolean> place = new BooleanConfig("Place", "", true);
    Config<Boolean> extraPlace = new BooleanConfig("SpamPlace", "", false);
    Config<Boolean> Break = new BooleanConfig("Break", "", true);
    public Config<Boolean> useOptimizedCalc = new BooleanConfig("UseOptimizedCalc", "", false);
    Config<Boolean> noUsing = new BooleanConfig("NoUsing", "", true);
    Config<Double> antiSuicide = new NumberConfig<Double>("AntiSuicide", "", 0.0, 5.0, 10.0);
    public Config<Integer> predictTicks = new NumberConfig<Integer>("PredictTicks", "", 0, 8, 20);
    Config<SwapMode> autoSwap = new EnumConfig("SwapMode", "", (Enum)SwapMode.SILENT, (Enum[])SwapMode.values());
    Config<Double> placeDelay = new NumberConfig<Double>("PlaceDelay", "", 0.0, 72.0, 1000.0);
    Config<Double> breakDelay = new NumberConfig<Double>("BreakDelay", "", 0.0, 72.0, 1000.0);
    Config<Float> placeRange = new NumberConfig<Float>("PlaceRange", "", Float.valueOf(0.0f), Float.valueOf(5.2f), Float.valueOf(6.0f));
    Config<Float> breakRange = new NumberConfig<Float>("BreakRange", "", Float.valueOf(0.0f), Float.valueOf(5.2f), Float.valueOf(6.0f));
    Config<Float> breakWall = new NumberConfig<Float>("BreakWallRange", "", Float.valueOf(0.0f), Float.valueOf(3.0f), Float.valueOf(6.0f));
    Config<Double> placeMinDamage = new NumberConfig<Double>("PlaceMinDmg", "", 0.0, 5.0, 36.0);
    Config<Double> placeMaxSelf = new NumberConfig<Double>("PlaceMaxSelfDmg", "", 0.0, 6.0, 36.0);
    Config<Double> breakMinDamage = new NumberConfig<Double>("BreakMinDmg", "", 0.0, 5.0, 36.0);
    Config<Double> breakMaxSelf = new NumberConfig<Double>("BreakMaxSelfDmg", "", 0.0, 6.5, 36.0);
    Config<Boolean> pauseDigging = new BooleanConfig("PauseDigging", "", false);
    Config<Double> pauseDiggingHealth = new NumberConfig<Double>("PauseHealth", "", 0.0, 10.0, 36.0);
    Config<Integer> pauseDiggingCount = new NumberConfig<Integer>("PauseCount", "", 0, 1, 4);
    Config<Boolean> helper = new BooleanConfig("Helper", "", false);
    Config<Double> placeBaseMinDamage = new NumberConfig<Double>("PlaceBaseMinDmg", "", 0.0, 7.0, 36.0);
    Config<Float> obsPlaceDelay = new NumberConfig<Float>("ObsPlaceDelay", "", Float.valueOf(0.0f), Float.valueOf(50.0f), Float.valueOf(500.0f));
    Config<Float> calcDelay = new NumberConfig<Float>("CalcDelay", "", Float.valueOf(0.0f), Float.valueOf(50.0f), Float.valueOf(500.0f));
    Config<Boolean> obsRotate = new BooleanConfig("PlaceRotate", "", false);
    Config<Boolean> slowFace = new BooleanConfig("SlowPlaceFace", "", false);
    Config<Double> slowDelay = new NumberConfig<Double>("SlowPlaceDelay", "", 0.0, 300.0, 2000.0);
    Config<Double> slowMinDamage = new NumberConfig<Double>("SlowMinDmg", "", 0.0, 2.0, 36.0);
    Config<Boolean> forcePlace = new BooleanConfig("ForcePlace", "", false);
    Config<Double> forceMaxHealth = new NumberConfig<Double>("ForceMaxHealth", "", 0.0, 10.0, 36.0);
    Config<Double> forceMin = new NumberConfig<Double>("ForceMin", "", 0.0, 5.0, 36.0);
    Config<Rotate> rotate = new EnumConfig("Rotate", "", (Enum)Rotate.Place, (Enum[])Rotate.values());
    Config<Boolean> newRotate = new BooleanConfig("YawStep", "", false);
    Config<Float> yawStep = new NumberConfig<Float>("Step", "", Float.valueOf(0.0f), Float.valueOf(0.1f), Float.valueOf(1.0f));
    Config<Boolean> checkLook = new BooleanConfig("CheckLook", "", true);
    Config<Float> fov = new NumberConfig<Float>("Fov", "", Float.valueOf(0.0f), Float.valueOf(10.0f), Float.valueOf(30.0f));
    Config<Boolean> render = new BooleanConfig("Render", "", true);
    Config<Boolean> smooth = new BooleanConfig("Smooth", "", true);
    Config<Boolean> shrink = new BooleanConfig("Shrink", "", true);
    Config<Boolean> box = new BooleanConfig("Box", "", true);
    Config<Boolean> outline = new BooleanConfig("Outline", "", true);
    Config<Boolean> reset = new BooleanConfig("Reset", "", false);
    Config<Color> color = new ColorConfig("Color", "", new Color(90, 90, 255), false, false);
    Config<Integer> boxAlpha = new NumberConfig<Integer>("BoxAlpha", "", 0, 80, 255);
    Config<Integer> outlineAlpha = new NumberConfig<Integer>("OutlineAlpha", "", 0, 80, 255);
    Config<Float> olWidth = new NumberConfig<Float>("OLWidth", "", Float.valueOf(0.1f), Float.valueOf(1.5f), Float.valueOf(5.0f));
    Config<Double> animationTime = new NumberConfig<Double>("AnimationTime", "", 0.1, 2.0, 10.0);
    Config<Double> startFadeTime = new NumberConfig<Double>("StartFadeTime", "", 0.1, 0.5, 1.0);
    Config<Double> fadeTime = new NumberConfig<Double>("FadeTime", "", 0.0, 5.0, 10.0);
    private final Timer delayTimer = new CacheTimer();
    private final Timer calcTimer = new CacheTimer();
    private final Timer obsPlaceTimer = new CacheTimer();
    public static final Timer placeTimer = new CacheTimer();
    public final Timer lastBreakTimer = new CacheTimer();
    private final Timer noPosTimer = new CacheTimer();
    private final FadeUtils fadeUtils = new FadeUtils(500L);
    private final FadeUtils animation = new FadeUtils(500L);
    double lastSize = 0.0;
    public PlayerEntity target;
    private float lastYaw = 0.0f;
    private float lastPitch = 0.0f;
    public float lastDamage;
    public Vec3d directionVec = null;
    private BlockPos renderPos = null;
    private Box lastBB = null;
    private Box nowBB = null;
    public BlockPos placePos;
    BlockPos obsPos;
    float obsLastDamage;

    public AutoCrystalModule() {
        super("AutoCrystal", "", ModuleCategory.COMBAT);
    }

    @Override
    public void onDisable() {
        this.target = null;
        this.delayTimer.reset();
        this.calcTimer.reset();
        placeTimer.reset();
        this.lastBreakTimer.reset();
        this.noPosTimer.reset();
        this.renderPos = null;
        this.lastBB = null;
        this.nowBB = null;
        this.placePos = null;
        this.lastYaw = Managers.ROTATION.lastYaw;
        this.lastPitch = Managers.ROTATION.lastPitch;
    }

    @Override
    public String getModuleData() {
        return this.target == null ? null : this.target.getName().getString();
    }

    @EventListener
    public void onPlayerTick(PlayerTickEvent event) {
        this.update();
        if (this.helper.getValue().booleanValue()) {
            int slot = -1;
            switch (this.autoSwap.getValue()) {
                case SILENT: 
                case NORMAL: {
                    slot = InventoryUtil.findItem(Items.OBSIDIAN);
                    break;
                }
                case Inventory: {
                    slot = InventoryUtil.findInventorySlot(Items.OBSIDIAN, false);
                    break;
                }
                case Pick: {
                    slot = InventoryUtil.findInventorySlot(Items.OBSIDIAN, true);
                }
            }
            if (slot == -1) {
                return;
            }
            if (this.target != null) {
                if (this.pauseDigging.getValue().booleanValue() && (double)EntityUtil.getHealth(this.target) >= this.pauseDiggingHealth.getValue() && PlayerUtil.isInBurrow(this.target, this.pauseDiggingCount.getValue())) {
                    this.lastBreakTimer.reset();
                    this.placePos = null;
                    this.obsPos = null;
                    return;
                }
                if (this.calcTimer.passed(this.calcDelay.getValue()) && this.placePos == null) {
                    this.obsLastDamage = Float.MAX_VALUE;
                    this.obsPos = null;
                    for (BlockPos pos : BlockUtil.getSphere(this.placeRange.getValue().floatValue())) {
                        if (!BlockUtil.isAir(pos) || !BlockUtil.isAir(pos.up()) && Modules.COMBAT_SETTING.oldVersion.getValue().booleanValue() || BlockUtil.hasEntityBlockCrystal(pos, false, true) || BlockUtil.hasEntityBlockCrystal(pos.up(), false, true) || !BlockUtil.canPlace(pos.down())) continue;
                        float damage = this.calculateBaseDamage(pos, this.target);
                        float selfDamage = this.calculateBaseDamage(pos, AutoCrystalModule.mc.player);
                        if ((double)selfDamage > this.placeMaxSelf.getValue() || damage < EntityUtil.getHealth(this.target) && (double)damage < this.placeBaseMinDamage.getValue() || this.antiSuicide.getValue() > 0.0 && (double)selfDamage > (double)(AutoCrystalModule.mc.player.getHealth() + AutoCrystalModule.mc.player.getAbsorptionAmount()) - this.antiSuicide.getValue() || this.obsPos != null && !(damage > this.obsLastDamage)) continue;
                        this.obsPos = pos;
                        this.obsLastDamage = damage;
                    }
                    this.calcTimer.reset();
                }
                if (this.obsPlaceTimer.passed(this.obsPlaceDelay.getValue()) && this.obsPos != null) {
                    if (!BlockUtil.canPlace(this.obsPos.down())) {
                        return;
                    }
                    if (this.noUsing.getValue().booleanValue() && AutoCrystalModule.mc.player.isUsingItem()) {
                        return;
                    }
                    if (!BlockUtil.isMining(this.obsPos.down())) {
                        int pre = AutoCrystalModule.mc.player.getInventory().selectedSlot;
                        switch (this.autoSwap.getValue()) {
                            case SILENT: 
                            case NORMAL: {
                                InventoryUtil.doSwap(slot);
                                break;
                            }
                            case Inventory: {
                                InventoryUtil.doInvSwap(slot);
                                break;
                            }
                            case Pick: {
                                InventoryUtil.doPickSwap(slot);
                            }
                        }
                        Managers.INTERACT.placeBlock(this.obsPos.down(), this.obsRotate.getValue());
                        switch (this.autoSwap.getValue()) {
                            case SILENT: {
                                InventoryUtil.doSwap(pre);
                                break;
                            }
                            case Inventory: {
                                InventoryUtil.doInvSwap(slot);
                                break;
                            }
                            case Pick: {
                                InventoryUtil.doPickSwap(slot);
                            }
                        }
                        this.obsPlaceTimer.reset();
                    }
                }
            }
        }
    }

    @EventListener
    public void onUpdateWalking(UpdateWalkingEvent event) {
        this.update();
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event) {
        this.update();
        if (this.render.getValue().booleanValue()) {
            ColorConfig first = (ColorConfig)this.color;
            if (this.smooth.getValue().booleanValue()) {
                double quad;
                double d = quad = this.noPosTimer.passed(this.startFadeTime.getValue() * 1000.0) ? this.fadeUtils.easeOutQuad() : 0.0;
                if (this.nowBB != null && quad < 1.0) {
                    Box bb = this.nowBB;
                    if (this.shrink.getValue().booleanValue()) {
                        bb = this.nowBB.shrink(quad * 0.5, quad * 0.5, quad * 0.5);
                        bb = bb.shrink(-quad * 0.5, -quad * 0.5, -quad * 0.5);
                    }
                    if (this.box.getValue().booleanValue()) {
                        RenderManager.renderBox(event.getMatrices(), bb, first.getRgb((int)((double)this.boxAlpha.getValue().intValue() * Math.abs(quad - 1.0))));
                    }
                    if (this.outline.getValue().booleanValue()) {
                        RenderManager.renderBoundingBox(event.getMatrices(), bb, this.olWidth.getValue().floatValue(), first.getRgb((int)((double)this.outlineAlpha.getValue().intValue() * Math.abs(quad - 1.0))));
                    }
                } else if (this.reset.getValue().booleanValue()) {
                    this.nowBB = null;
                }
            } else if (this.renderPos != null) {
                if (this.box.getValue().booleanValue()) {
                    RenderManager.renderBox(event.getMatrices(), this.renderPos.down(), first.getRgb(this.boxAlpha.getValue()));
                }
                if (this.outline.getValue().booleanValue()) {
                    RenderManager.renderBoundingBox(event.getMatrices(), this.renderPos.down(), this.olWidth.getValue().floatValue(), first.getRgb(this.outlineAlpha.getValue()));
                }
            }
        }
    }

    @EventListener
    public void onRotate(RotateEvent event) {
        if (this.placePos != null && this.newRotate.getValue().booleanValue() && this.directionVec != null) {
            float[] newAngle = this.injectStep(EntityUtil.getLegitRotations(this.directionVec), this.yawStep.getValue().floatValue());
            this.lastYaw = newAngle[0];
            this.lastPitch = newAngle[1];
            event.setYaw(this.lastYaw);
            event.setPitch(this.lastPitch);
        } else {
            this.lastYaw = Managers.ROTATION.lastYaw;
            this.lastPitch = Managers.ROTATION.lastPitch;
        }
    }

    private void update() {
        float selfDamage;
        float damage;
        if (AutoCrystalModule.nullCheck()) {
            return;
        }
        this.animUpdate();
        if (!this.delayTimer.passed(this.updateDelay.getValue())) {
            return;
        }
        if (this.noUsing.getValue().booleanValue() && AutoCrystalModule.mc.player.isUsingItem()) {
            this.lastBreakTimer.reset();
            this.placePos = null;
            this.obsPos = null;
            return;
        }
        if (!(AutoCrystalModule.mc.player.getMainHandStack().getItem().equals(Items.END_CRYSTAL) || AutoCrystalModule.mc.player.getOffHandStack().getItem().equals(Items.END_CRYSTAL) || this.findCrystal())) {
            this.placePos = null;
            this.target = null;
            this.obsPos = null;
            return;
        }
        this.delayTimer.reset();
        BlockPosX breakPos = null;
        this.placePos = null;
        this.lastDamage = 0.0f;
        this.target = EntityUtil.getTarget(this.targetRange.getValue().floatValue());
        if (this.pauseDigging.getValue().booleanValue() && (double)EntityUtil.getHealth(this.target) >= this.pauseDiggingHealth.getValue() && PlayerUtil.isInBurrow(this.target, this.pauseDiggingCount.getValue())) {
            this.lastBreakTimer.reset();
            this.placePos = null;
            this.obsPos = null;
            return;
        }
        if (this.target == null) {
            this.delayTimer.reset();
            this.calcTimer.reset();
            placeTimer.reset();
            this.lastBreakTimer.reset();
            this.noPosTimer.reset();
            this.renderPos = null;
            this.lastBB = null;
            this.nowBB = null;
            this.placePos = null;
            this.obsPos = null;
            this.lastYaw = Managers.ROTATION.lastYaw;
            this.lastPitch = Managers.ROTATION.lastPitch;
            return;
        }
        for (Entity crystal : AutoCrystalModule.mc.world.getEntities()) {
            if (!(crystal instanceof EndCrystalEntity) || EntityUtil.getEyesPos().distanceTo(crystal.getPos()) > (double)this.breakRange.getValue().floatValue() || !AutoCrystalModule.mc.player.canSee(crystal) && AutoCrystalModule.mc.player.distanceTo(crystal) > this.breakWall.getValue().floatValue()) continue;
            damage = this.calculateDamage(crystal.getPos(), this.target);
            selfDamage = this.calculateDamage(crystal.getPos(), (PlayerEntity)AutoCrystalModule.mc.player);
            if ((double)selfDamage > this.breakMaxSelf.getValue() || this.antiSuicide.getValue() > 0.0 && (double)selfDamage > (double)(AutoCrystalModule.mc.player.getHealth() + AutoCrystalModule.mc.player.getAbsorptionAmount()) - this.antiSuicide.getValue() || damage < EntityUtil.getHealth(this.target) && (double)damage < this.getBreakDamage(this.target) || breakPos != null && !(damage > this.lastDamage)) continue;
            breakPos = new BlockPosX(crystal.getPos());
            this.lastDamage = damage;
        }
        if (AutoCrystalModule.mc.player.getMainHandStack().getItem().equals(Items.END_CRYSTAL) || AutoCrystalModule.mc.player.getOffHandStack().getItem().equals(Items.END_CRYSTAL) || this.findCrystal()) {
            for (BlockPos pos : BlockUtil.getSphere(this.placeRange.getValue().floatValue())) {
                if (!this.canPlaceCrystal(pos, true, false) || this.behindWall(pos) || !this.canTouch(pos.down())) continue;
                damage = this.calculateDamage(pos, this.target);
                selfDamage = this.calculateDamage(pos, (PlayerEntity)AutoCrystalModule.mc.player);
                if ((double)selfDamage > this.placeMaxSelf.getValue() || this.antiSuicide.getValue() > 0.0 && (double)selfDamage > (double)(AutoCrystalModule.mc.player.getHealth() + AutoCrystalModule.mc.player.getAbsorptionAmount()) - this.antiSuicide.getValue() || damage < EntityUtil.getHealth(this.target) && (double)damage < this.getPlaceDamage(this.target) || this.placePos != null && !(damage > this.lastDamage)) continue;
                this.placePos = pos;
                breakPos = null;
                this.lastDamage = damage;
            }
        }
        if (breakPos != null) {
            this.doBreak(breakPos);
            if (this.extraPlace.getValue().booleanValue() && this.placePos != null) {
                this.doPlace(this.placePos);
            }
            return;
        }
        if (this.placePos != null) {
            this.doCrystal(this.placePos);
        }
    }

    public void doCrystal(BlockPos pos) {
        if (this.canPlaceCrystal(pos, false, true)) {
            if (AutoCrystalModule.mc.player.getMainHandStack().getItem().equals(Items.END_CRYSTAL) || AutoCrystalModule.mc.player.getOffHandStack().getItem().equals(Items.END_CRYSTAL) || this.findCrystal()) {
                this.doPlace(pos);
            }
        } else {
            this.doBreak(pos);
        }
    }

    private void doBreak(BlockPos pos) {
        block9: {
            this.lastBreakTimer.reset();
            if (!this.Break.getValue().booleanValue()) {
                return;
            }
            Iterator iterator = AutoCrystalModule.mc.world.getNonSpectatingEntities(EndCrystalEntity.class, new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1)).iterator();
            if (!iterator.hasNext()) break block9;
            EndCrystalEntity entity = (EndCrystalEntity)iterator.next();
            Direction facing = Managers.INTERACT.getClickDirection(pos.down());
            if (facing == null) {
                return;
            }
            Vec3d vec = pos.down().toCenterPos().add((double)facing.getVector().getX() * 0.5, (double)facing.getVector().getY() * 0.5, (double)facing.getVector().getZ() * 0.5);
            switch (this.rotate.getValue()) {
                case Place: {
                    Managers.ROTATION.faceVector(vec, false);
                    break;
                }
                case Break: 
                case Both: {
                    Managers.ROTATION.faceVector(entity.getPos().add(0.0, 0.25, 0.0), false);
                }
            }
            if (!Modules.COMBAT_SETTING.attackTimer.passed(this.breakDelay.getValue())) {
                return;
            }
            Modules.COMBAT_SETTING.attackTimer.reset();
            AutoCrystalModule.mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(entity, AutoCrystalModule.mc.player.isSneaking()));
            if (!placeTimer.passed(this.placeDelay.getValue()) || !this.extraPlace.getValue().booleanValue()) {
                return;
            }
            if ((double)this.lastDamage >= this.placeMinDamage.getValue() && this.placePos != null) {
                this.doPlace(this.placePos);
            }
        }
    }

    private void doPlace(BlockPos pos) {
        if (!this.place.getValue().booleanValue()) {
            return;
        }
        if (!(AutoCrystalModule.mc.player.getMainHandStack().getItem().equals(Items.END_CRYSTAL) || AutoCrystalModule.mc.player.getOffHandStack().getItem().equals(Items.END_CRYSTAL) || this.findCrystal())) {
            return;
        }
        if (!this.canTouch(pos.down())) {
            return;
        }
        BlockPos obsPos = pos.down();
        Direction facing = Managers.INTERACT.getClickDirection(obsPos);
        if (facing == null) {
            return;
        }
        Vec3d vec = obsPos.toCenterPos().add((double)facing.getVector().getX() * 0.5, (double)facing.getVector().getY() * 0.5, (double)facing.getVector().getZ() * 0.5);
        switch (this.rotate.getValue()) {
            case Place: 
            case Both: {
                if (this.faceVector(vec)) break;
                return;
            }
        }
        if (!placeTimer.passed(this.placeDelay.getValue())) {
            return;
        }
        placeTimer.reset();
        if (AutoCrystalModule.mc.player.getMainHandStack().getItem().equals(Items.END_CRYSTAL) || AutoCrystalModule.mc.player.getOffHandStack().getItem().equals(Items.END_CRYSTAL)) {
            this.placeCrystal(pos);
        } else if (this.findCrystal()) {
            int old = AutoCrystalModule.mc.player.getInventory().selectedSlot;
            int crystal = this.getCrystal();
            if (crystal == -1) {
                return;
            }
            if (this.autoSwap.getValue().equals((Object)SwapMode.Pick) && crystal <= 9) {
                return;
            }
            switch (this.autoSwap.getValue()) {
                case SILENT: 
                case NORMAL: {
                    InventoryUtil.doSwap(crystal);
                    break;
                }
                case Inventory: {
                    InventoryUtil.doInvSwap(crystal);
                    break;
                }
                case Pick: {
                    InventoryUtil.doPickSwap(crystal);
                }
            }
            this.placeCrystal(pos);
            switch (this.autoSwap.getValue()) {
                case SILENT: {
                    InventoryUtil.doSwap(old);
                    break;
                }
                case Inventory: {
                    InventoryUtil.doInvSwap(crystal);
                    break;
                }
                case Pick: {
                    InventoryUtil.doPickSwap(crystal);
                }
            }
        }
    }

    public float calculateDamage(BlockPos pos, PlayerEntity player) {
        return this.calculateDamage(new Vec3d((double)pos.getX() + 0.5, pos.getY(), (double)pos.getZ() + 0.5), player);
    }

    public float calculateBaseDamage(BlockPos pos, PlayerEntity player) {
        return this.calculateBaseDamage(new Vec3d((double)pos.getX() + 0.5, pos.getY(), (double)pos.getZ() + 0.5), player, pos.down());
    }

    public float calculateDamage(Vec3d pos, PlayerEntity player) {
        DamageUtil.terrainIgnore = true;
        float damage = DamageUtil.getCrystalDamage(pos, player);
        DamageUtil.terrainIgnore = false;
        return damage;
    }

    public float calculateBaseDamage(Vec3d pos, PlayerEntity player, BlockPos bp) {
        return DamageUtil.getCrystalDamageOfBase(pos, player, bp);
    }

    private double getPlaceDamage(PlayerEntity target) {
        if (this.slowFace.getValue().booleanValue() && this.lastBreakTimer.passed(this.slowDelay.getValue())) {
            return this.slowMinDamage.getValue();
        }
        if (this.forcePlace.getValue().booleanValue() && (double)EntityUtil.getHealth(target) <= this.forceMaxHealth.getValue()) {
            return this.forceMin.getValue();
        }
        return this.placeMinDamage.getValue();
    }

    private double getBreakDamage(PlayerEntity target) {
        if (this.slowFace.getValue().booleanValue() && this.lastBreakTimer.passed(this.slowDelay.getValue())) {
            return this.slowMinDamage.getValue();
        }
        if (this.forcePlace.getValue().booleanValue() && (double)EntityUtil.getHealth(target) <= this.forceMaxHealth.getValue()) {
            return this.forceMin.getValue();
        }
        return this.breakMinDamage.getValue();
    }

    private boolean findCrystal() {
        if (this.autoSwap.getValue() == SwapMode.OFF) {
            return false;
        }
        return this.getCrystal() != -1;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private boolean canTouch(BlockPos pos) {
        Direction side = Managers.INTERACT.getClickDirection(pos);
        if (side == null) return false;
        Vec3d vec3d = new Vec3d((double)side.getVector().getX() * 0.5, (double)side.getVector().getY() * 0.5, (double)side.getVector().getZ() * 0.5);
        if (!(pos.toCenterPos().add(vec3d).distanceTo(AutoCrystalModule.mc.player.getEyePos()) <= (double)this.placeRange.getValue().floatValue())) return false;
        return true;
    }

    private int getCrystal() {
        if (this.autoSwap.getValue() == SwapMode.SILENT || this.autoSwap.getValue() == SwapMode.NORMAL) {
            return InventoryUtil.findItem(Items.END_CRYSTAL);
        }
        if (this.autoSwap.getValue() == SwapMode.Inventory) {
            return InventoryUtil.findInventorySlot(Items.END_CRYSTAL, false);
        }
        if (this.autoSwap.getValue() == SwapMode.Pick) {
            return InventoryUtil.findInventorySlot(Items.END_CRYSTAL, true);
        }
        return -1;
    }

    public void placeCrystal(BlockPos pos) {
        boolean offhand = AutoCrystalModule.mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL;
        BlockPos obsPos = pos.down();
        Direction facing = Managers.INTERACT.getClickDirection(obsPos);
        if (facing == null) {
            return;
        }
        Managers.INTERACT.clickBlock(obsPos, facing, offhand ? Hand.OFF_HAND : Hand.MAIN_HAND, false);
    }

    private void animUpdate() {
        this.fadeUtils.setLength((long)(this.fadeTime.getValue() * 1000.0));
        if (this.placePos != null) {
            this.lastBB = new Box(new BlockPos(this.placePos.down()));
            this.noPosTimer.reset();
            if (this.nowBB == null) {
                this.nowBB = this.lastBB;
            }
            if (this.renderPos == null || !this.renderPos.equals(this.placePos)) {
                this.animation.setLength(this.animationTime.getValue() * 1000.0 <= 0.0 ? 0L : (long)(Math.abs(this.nowBB.minX - this.lastBB.minX) + Math.abs(this.nowBB.minY - this.lastBB.minY) + Math.abs(this.nowBB.minZ - this.lastBB.minZ) <= 5.0 ? (double)((long)((Math.abs(this.nowBB.minX - this.lastBB.minX) + Math.abs(this.nowBB.minY - this.lastBB.minY) + Math.abs(this.nowBB.minZ - this.lastBB.minZ)) * (this.animationTime.getValue() * 1000.0))) : this.animationTime.getValue() * 5000.0));
                this.animation.reset();
                this.renderPos = this.placePos;
            }
        }
        if (!this.noPosTimer.passed((long)(this.startFadeTime.getValue() * 1000.0))) {
            this.fadeUtils.reset();
        }
        double size = this.animation.easeOutQuad();
        if (this.nowBB != null && this.lastBB != null) {
            if (Math.abs(this.nowBB.minX - this.lastBB.minX) + Math.abs(this.nowBB.minY - this.lastBB.minY) + Math.abs(this.nowBB.minZ - this.lastBB.minZ) > 16.0) {
                this.nowBB = this.lastBB;
            }
            if (this.lastSize != size) {
                this.nowBB = new Box(this.nowBB.minX + (this.lastBB.minX - this.nowBB.minX) * size, this.nowBB.minY + (this.lastBB.minY - this.nowBB.minY) * size, this.nowBB.minZ + (this.lastBB.minZ - this.nowBB.minZ) * size, this.nowBB.maxX + (this.lastBB.maxX - this.nowBB.maxX) * size, this.nowBB.maxY + (this.lastBB.maxY - this.nowBB.maxY) * size, this.nowBB.maxZ + (this.lastBB.maxZ - this.nowBB.maxZ) * size);
                this.lastSize = size;
            }
        }
    }

    public boolean canPlaceCrystal(BlockPos pos, boolean ignoreCrystal, boolean ignoreItem) {
        BlockPos obsPos = pos.down();
        BlockPos boost = obsPos.up();
        return !(BlockUtil.getBlock(obsPos) != Blocks.BEDROCK && BlockUtil.getBlock(obsPos) != Blocks.OBSIDIAN || Managers.INTERACT.getClickDirection(obsPos) == null || BlockUtil.hasEntityBlockCrystal(boost, ignoreCrystal, ignoreItem) || BlockUtil.hasEntityBlockCrystal(boost.up(), ignoreCrystal, ignoreItem) || !BlockUtil.isAir(boost.up()) && Modules.COMBAT_SETTING.oldVersion.getValue() != false || !BlockUtil.isAir(boost) && (!BlockUtil.hasEntityBlockCrystal(boost, false, ignoreItem) || BlockUtil.getBlock(boost) != Blocks.FIRE));
    }

    public boolean behindWall(BlockPos pos) {
        Vec3d testVec = new Vec3d((double)pos.getX() + 0.5, (double)pos.getY() + 1.7, (double)pos.getZ() + 0.5);
        BlockHitResult result = AutoCrystalModule.mc.world.raycast(new RaycastContext(EntityUtil.getEyesPos(), testVec, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, AutoCrystalModule.mc.player));
        if (result == null || ((HitResult)result).getType() == HitResult.Type.MISS) {
            return false;
        }
        return MathHelper.sqrt((float)EntityUtil.getEyesPos().squaredDistanceTo((double)pos.getX() + 0.5, pos.getY(), (double)pos.getZ() + 0.5)) > this.breakWall.getValue().floatValue();
    }

    public boolean faceVector(Vec3d directionVec) {
        if (!this.newRotate.getValue().booleanValue()) {
            Managers.ROTATION.faceVector(directionVec, false);
            return true;
        }
        this.directionVec = directionVec;
        float[] angle = EntityUtil.getLegitRotations(directionVec);
        if (Math.abs(MathHelper.wrapDegrees(angle[0] - this.lastYaw)) < this.fov.getValue().floatValue() && Math.abs(MathHelper.wrapDegrees(angle[1] - this.lastPitch)) < this.fov.getValue().floatValue()) {
            return true;
        }
        return this.checkLook.getValue() == false;
    }

    private float[] injectStep(float[] angle, float steps) {
        if (steps < 0.01f) {
            steps = 0.01f;
        }
        if (steps > 1.0f) {
            steps = 1.0f;
        }
        if (steps < 1.0f && angle != null) {
            float packetPitch;
            float packetYaw = this.lastYaw;
            float diff = MathHelper.wrapDegrees(angle[0] - packetYaw);
            if (Math.abs(diff) > 90.0f * steps) {
                angle[0] = packetYaw + diff * (90.0f * steps / Math.abs(diff));
            }
            if (Math.abs(diff = angle[1] - (packetPitch = this.lastPitch)) > 90.0f * steps) {
                angle[1] = packetPitch + diff * (90.0f * steps / Math.abs(diff));
            }
        }
        return new float[]{angle[0], angle[1]};
    }

    public static enum SwapMode {
        OFF,
        NORMAL,
        SILENT,
        Inventory,
        Pick;

    }

    public static enum Rotate {
        OFF,
        Place,
        Break,
        Both;

    }
}
