package dev.realme.ash.impl.module.combat;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.NumberDisplay;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.ColorConfig;
import dev.realme.ash.api.config.setting.EnumConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.RotationModule;
import dev.realme.ash.api.render.RenderManager;
import dev.realme.ash.impl.event.RotateEvent;
import dev.realme.ash.impl.event.network.PacketEvent;
import dev.realme.ash.impl.event.network.UpdateWalkingEvent;
import dev.realme.ash.impl.event.render.RenderWorldEvent;
import dev.realme.ash.impl.manager.player.interaction.InteractionManager;
import dev.realme.ash.init.Managers;
import dev.realme.ash.init.Modules;
import dev.realme.ash.mixin.accessor.IPlayerMoveC2SPacket;
import dev.realme.ash.util.math.DamageUtil;
import dev.realme.ash.util.math.timer.CacheTimer;
import dev.realme.ash.util.math.timer.Timer;
import dev.realme.ash.util.player.InventoryUtil;
import dev.realme.ash.util.player.PlayerUtil;
import dev.realme.ash.util.world.BlockUtil;
import dev.realme.ash.util.world.EntityUtil;
import java.awt.Color;
import java.util.ArrayList;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class AutoAnchorModule
extends RotationModule {
    Config<Float> updateDelay = new NumberConfig<Float>("UpdateDelay", "", Float.valueOf(0.0f), Float.valueOf(32.0f), Float.valueOf(500.0f));
    Config<Float> calcDelay = new NumberConfig<Float>("CalcDelay", "", Float.valueOf(0.0f), Float.valueOf(0.3f), Float.valueOf(0.5f));
    Config<Float> targetRange = new NumberConfig<Float>("EnemyRange", "", Float.valueOf(0.0f), Float.valueOf(10.0f), Float.valueOf(13.0f));
    Config<Float> range = new NumberConfig<Float>("Range", "", Float.valueOf(0.0f), Float.valueOf(5.0f), Float.valueOf(8.0f));
    Config<Double> spamDelay = new NumberConfig<Double>("Delay", "", Double.valueOf(0.0), Double.valueOf(2.0), Double.valueOf(5.0), NumberDisplay.DEGREES);
    Config<Double> headDelay = new NumberConfig<Double>("HeadDelay", "", Double.valueOf(0.0), Double.valueOf(2.0), Double.valueOf(5.0), NumberDisplay.DEGREES);
    Config<SwapMode> swapMode = new EnumConfig("SwapMode", "", (Enum)SwapMode.SILENT, (Enum[])SwapMode.values());
    Config<Boolean> pauseEat = new BooleanConfig("PauseEat", "Not attacking while using items", true);
    Config<Double> minPrefer = new NumberConfig<Double>("MinDmg", "", 0.0, 6.0, 36.0);
    Config<Double> maxSelfDamage = new NumberConfig<Double>("MaxSelfDamage", "", 0.0, 6.0, 36.0);
    public Config<Integer> predictTicks = new NumberConfig<Integer>("PredictTicks", "", 0, 8, 20);
    Config<Double> minDamage = new NumberConfig<Double>("PlaceMinDmg", "", 0.0, 6.0, 36.0);
    Config<Double> breakMin = new NumberConfig<Double>("BreakMinDmg", "", 0.0, 7.5, 36.0);
    Config<Boolean> rotate = new BooleanConfig("Rotate", "", true);
    Config<Boolean> newRotate = new BooleanConfig("YawStep", "", false);
    Config<Float> yawStep = new NumberConfig<Float>("Step", "", Float.valueOf(0.0f), Float.valueOf(0.1f), Float.valueOf(1.0f));
    Config<Boolean> checkLook = new BooleanConfig("CheckLook", "", true);
    Config<Float> fov = new NumberConfig<Float>("Fov", "", Float.valueOf(0.0f), Float.valueOf(10.0f), Float.valueOf(30.0f));
    Config<Boolean> box = new BooleanConfig("Box", "", true);
    Config<Boolean> outline = new BooleanConfig("Outline", "", true);
    Config<Color> color = new ColorConfig("Color", "", new Color(90, 90, 255), false, false);
    Config<Integer> boxAlpha = new NumberConfig<Integer>("BoxAlpha", "", 0, 80, 255);
    Config<Integer> olAlpha = new NumberConfig<Integer>("OLAlpha", "", 0, 80, 255);
    Config<Float> olWidth = new NumberConfig<Float>("OLWidth", "", Float.valueOf(0.1f), Float.valueOf(1.5f), Float.valueOf(5.0f));
    public Config<Float> attackDelay = new NumberConfig<Float>("AttackDelay", "", Float.valueOf(0.0f), Float.valueOf(10.0f), Float.valueOf(100.0f));
    private final Timer updateTimer = new CacheTimer();
    private final Timer delayTimer = new CacheTimer();
    private final Timer calcTimer = new CacheTimer();
    public Vec3d directionVec = null;
    private float lastYaw = 0.0f;
    private float lastPitch = 0.0f;
    private final ArrayList<BlockPos> chargeList = new ArrayList();
    public BlockPos currentPos;
    public PlayerEntity target;

    public AutoAnchorModule() {
        super("AutoAnchor", "Automatically destroys people using anchors.", ModuleCategory.COMBAT, 500);
    }

    @Override
    public String getModuleData() {
        return this.target == null ? null : this.target.getName().getString();
    }

    @Override
    public void onDisable() {
        this.lastYaw = Managers.ROTATION.lastYaw;
        this.lastPitch = Managers.ROTATION.lastPitch;
        this.currentPos = null;
        this.target = null;
    }

    @Override
    public void onEnable() {
        this.lastYaw = Managers.ROTATION.lastYaw;
        this.lastPitch = Managers.ROTATION.lastPitch;
        this.currentPos = null;
        this.target = null;
    }

    @EventListener
    public void onUpdateWalking(UpdateWalkingEvent event) {
        this.update();
    }

    @EventListener
    public void onUpdate() {
        this.update();
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event) {
        this.update();
        if (this.currentPos != null) {
            ColorConfig first = (ColorConfig)this.color;
            if (this.box.getValue().booleanValue()) {
                RenderManager.renderBox(event.getMatrices(), this.currentPos, first.getRgb(this.boxAlpha.getValue()));
            }
            if (this.outline.getValue().booleanValue()) {
                RenderManager.renderBoundingBox(event.getMatrices(), this.currentPos, this.olWidth.getValue().floatValue(), first.getRgb(this.olAlpha.getValue()));
            }
        }
    }

    @EventListener
    public void onRotate(RotateEvent event) {
        if (this.currentPos != null && this.newRotate.getValue().booleanValue() && this.directionVec != null) {
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

    @EventListener(priority=-199)
    public void onPacketSend(PacketEvent.Send event) {
        Packet<?> packet;
        if (event.isCanceled()) {
            return;
        }
        if (this.newRotate.getValue().booleanValue() && this.currentPos != null && this.directionVec != null && !Managers.ROTATION.rotating && (packet = event.getPacket()) instanceof PlayerMoveC2SPacket) {
            PlayerMoveC2SPacket packet2 = (PlayerMoveC2SPacket)((Object)packet);
            if (!packet2.changesLook()) {
                return;
            }
            float yaw = packet2.getYaw(114514.0f);
            float pitch = packet2.getPitch(114514.0f);
            if (yaw == AutoAnchorModule.mc.player.getYaw() && pitch == AutoAnchorModule.mc.player.getPitch()) {
                float[] angle = this.injectStep(EntityUtil.getLegitRotations(this.directionVec), this.yawStep.getValue().floatValue());
                ((IPlayerMoveC2SPacket)((Object)event.getPacket())).setYaw(angle[0]);
                ((IPlayerMoveC2SPacket)((Object)event.getPacket())).setPitch(angle[1]);
            }
        }
    }

    public void update() {
        if (!this.updateTimer.passed(this.updateDelay.getValue())) {
            return;
        }
        int anchor = -1;
        int glowstone = -1;
        int unBlock = -1;
        switch (this.swapMode.getValue()) {
            case SILENT: 
            case NORMAL: {
                anchor = InventoryUtil.findItem(Items.RESPAWN_ANCHOR);
                glowstone = InventoryUtil.findItem(Items.GLOWSTONE);
                unBlock = InventoryUtil.findUnBlock();
                break;
            }
            case Inventory: {
                anchor = InventoryUtil.findInventorySlot(Items.RESPAWN_ANCHOR, false);
                glowstone = InventoryUtil.findInventorySlot(Items.GLOWSTONE, false);
                unBlock = InventoryUtil.findUnBlockItemInventory(false);
                break;
            }
            case Pick: {
                anchor = InventoryUtil.findInventorySlot(Items.RESPAWN_ANCHOR, true);
                glowstone = InventoryUtil.findInventorySlot(Items.GLOWSTONE, true);
                unBlock = InventoryUtil.findUnBlockItemInventory(true);
            }
        }
        int old = AutoAnchorModule.mc.player.getInventory().selectedSlot;
        if (anchor == -1) {
            this.currentPos = null;
            return;
        }
        if (glowstone == -1) {
            this.currentPos = null;
            return;
        }
        if (unBlock == -1) {
            this.currentPos = null;
            return;
        }
        if (AutoAnchorModule.mc.player.isSneaking()) {
            this.currentPos = null;
            return;
        }
        if (this.pauseEat.getValue().booleanValue() && AutoAnchorModule.mc.player.isUsingItem()) {
            this.currentPos = null;
            return;
        }
        this.updateTimer.reset();
        if (this.calcTimer.passed((long)(this.calcDelay.getValue().floatValue() * 1000.0f))) {
            this.calcTimer.reset();
            this.currentPos = null;
            double placeDamage = this.minDamage.getValue();
            double breakDamage = this.breakMin.getValue();
            boolean anchorFound = false;
            this.target = EntityUtil.getTarget(this.targetRange.getValue().floatValue());
            if (this.target == null) {
                return;
            }
            if (this.currentPos == null) {
                for (BlockPos pos : BlockUtil.getSphere(this.range.getValue())) {
                    double selfDamage = this.getAnchorDamage(pos, AutoAnchorModule.mc.player);
                    if (selfDamage > this.maxSelfDamage.getValue() || selfDamage > (double)(AutoAnchorModule.mc.player.getHealth() + AutoAnchorModule.mc.player.getAbsorptionAmount())) continue;
                    if (BlockUtil.getBlock(pos) != Blocks.RESPAWN_ANCHOR) {
                        double damage;
                        if (anchorFound || !BlockUtil.canPlace(pos, true)) continue;
                        BlockState preState = BlockUtil.getState(pos);
                        AutoAnchorModule.mc.world.setBlockState(pos, Blocks.RESPAWN_ANCHOR.getDefaultState());
                        boolean skip = Managers.INTERACT.getClickDirection(pos) == null;
                        AutoAnchorModule.mc.world.setBlockState(pos, preState);
                        if (skip || !((damage = this.getAnchorDamage(pos, this.target)) >= placeDamage) || Modules.AUTO_CRYSTAL.placePos != null && Modules.AUTO_CRYSTAL.isEnabled() && !((double)Modules.AUTO_CRYSTAL.lastDamage < damage)) continue;
                        placeDamage = damage;
                        this.currentPos = pos;
                        continue;
                    }
                    double damage = this.getAnchorDamage(pos, this.target);
                    if (Managers.INTERACT.getClickDirection(pos) == null || !(damage >= breakDamage)) continue;
                    if (damage >= this.minPrefer.getValue()) {
                        anchorFound = true;
                    }
                    if (!anchorFound && damage < placeDamage || Modules.AUTO_CRYSTAL.placePos != null && Modules.AUTO_CRYSTAL.isEnabled() && !((double)Modules.AUTO_CRYSTAL.lastDamage < damage)) continue;
                    breakDamage = damage;
                    this.currentPos = pos;
                }
            }
        }
        if (this.currentPos != null) {
            EntityUtil.attackCrystal(this.currentPos, this.attackDelay.getValue().floatValue());
            boolean bl = this.currentPos.equals(PlayerUtil.playerPos(this.target).up(2)) && !Managers.BREAK.isMining(this.currentPos);
            double delay = bl ? this.headDelay.getValue() : this.spamDelay.getValue();
            if (!this.delayTimer.passed(delay * 100.0)) {
                return;
            }
            this.delayTimer.reset();
            if (BlockUtil.canPlace(this.currentPos, true)) {
                this.placeBlock(this.currentPos, this.rotate.getValue(), anchor);
                switch (this.swapMode.getValue()) {
                    case SILENT: {
                        InventoryUtil.doSwap(old);
                        break;
                    }
                    case Inventory: {
                        InventoryUtil.doInvSwap(anchor);
                        break;
                    }
                    case Pick: {
                        InventoryUtil.doPickSwap(anchor);
                    }
                }
            }
            if (Managers.INTERACT.getClickDirection(this.currentPos) == null) {
                return;
            }
            if (!this.chargeList.contains(this.currentPos)) {
                this.delayTimer.reset();
                this.clickBlock(this.currentPos, Managers.INTERACT.getClickDirection(this.currentPos), this.rotate.getValue(), glowstone);
                switch (this.swapMode.getValue()) {
                    case SILENT: {
                        InventoryUtil.doSwap(old);
                        break;
                    }
                    case Inventory: {
                        InventoryUtil.doInvSwap(glowstone);
                        break;
                    }
                    case Pick: {
                        InventoryUtil.doPickSwap(glowstone);
                    }
                }
                this.chargeList.add(this.currentPos);
            }
            this.chargeList.remove(this.currentPos);
            this.clickBlock(this.currentPos, Managers.INTERACT.getClickDirection(this.currentPos), this.rotate.getValue(), unBlock);
            switch (this.swapMode.getValue()) {
                case SILENT: {
                    InventoryUtil.doSwap(old);
                    break;
                }
                case Inventory: {
                    InventoryUtil.doInvSwap(unBlock);
                    break;
                }
                case Pick: {
                    InventoryUtil.doPickSwap(unBlock);
                }
            }
            if (bl) {
                BlockState preState = BlockUtil.getState(this.currentPos);
                AutoAnchorModule.mc.world.setBlockState(this.currentPos, Blocks.AIR.getDefaultState());
                this.placeBlock(this.currentPos, this.rotate.getValue(), anchor);
                switch (this.swapMode.getValue()) {
                    case SILENT: {
                        InventoryUtil.doSwap(old);
                        break;
                    }
                    case Inventory: {
                        InventoryUtil.doInvSwap(anchor);
                        break;
                    }
                    case Pick: {
                        InventoryUtil.doPickSwap(anchor);
                    }
                }
                AutoAnchorModule.mc.world.setBlockState(this.currentPos, preState);
            }
        }
    }

    public double getAnchorDamage(BlockPos anchorPos, PlayerEntity target) {
        double damage = DamageUtil.getAnchorDamage(anchorPos, target);
        return damage;
    }

    public void placeBlock(BlockPos pos, boolean rotate, int slot) {
        Direction side;
        if (InteractionManager.airPlace()) {
            for (Direction i : Direction.values()) {
                if (!AutoAnchorModule.mc.world.isAir(pos.offset(i))) continue;
                this.clickBlock(pos, i, rotate, slot);
                return;
            }
        }
        if ((side = Managers.INTERACT.getPlaceDirection(pos)) == null) {
            return;
        }
        switch (this.swapMode.getValue()) {
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
        Managers.INTERACT.placeBlock(pos, false);
    }

    public void clickBlock(BlockPos pos, Direction side, boolean rotate, int slot) {
        if (side == null) {
            return;
        }
        Vec3d directionVec = new Vec3d((double)pos.getX() + 0.5 + (double)side.getVector().getX() * 0.5, (double)pos.getY() + 0.5 + (double)side.getVector().getY() * 0.5, (double)pos.getZ() + 0.5 + (double)side.getVector().getZ() * 0.5);
        if (rotate && !this.faceVector(directionVec)) {
            return;
        }
        switch (this.swapMode.getValue()) {
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
        Managers.INTERACT.clickBlock(pos, side, false);
    }

    public boolean faceVector(Vec3d directionVec) {
        if (!this.newRotate.getValue().booleanValue()) {
            this.setRotation(directionVec);
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
}
