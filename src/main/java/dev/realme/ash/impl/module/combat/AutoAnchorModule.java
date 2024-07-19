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
    final Config<Float> updateDelay = new NumberConfig<>("UpdateDelay", "", 0.0f, 32.0f, 500.0f);
    final Config<Float> calcDelay = new NumberConfig<>("CalcDelay", "", 0.0f, 0.3f, 0.5f);
    final Config<Float> targetRange = new NumberConfig<>("EnemyRange", "", 0.0f, 10.0f, 13.0f);
    final Config<Float> range = new NumberConfig<>("Range", "", 0.0f, 5.0f, 8.0f);
    final Config<Double> spamDelay = new NumberConfig<>("Delay", "", 0.0, 2.0, 5.0, NumberDisplay.DEGREES);
    final Config<Double> headDelay = new NumberConfig<>("HeadDelay", "", 0.0, 2.0, 5.0, NumberDisplay.DEGREES);
    final Config<SwapMode> swapMode = new EnumConfig<>("SwapMode", "", SwapMode.SILENT, SwapMode.values());
    final Config<Boolean> pauseEat = new BooleanConfig("PauseEat", "Not attacking while using items", true);
    final Config<Double> minPrefer = new NumberConfig<>("MinDmg", "", 0.0, 6.0, 36.0);
    final Config<Double> maxSelfDamage = new NumberConfig<>("MaxSelfDamage", "", 0.0, 6.0, 36.0);
    public final Config<Integer> predictTicks = new NumberConfig<>("PredictTicks", "", 0, 8, 20);
    final Config<Double> minDamage = new NumberConfig<>("PlaceMinDmg", "", 0.0, 6.0, 36.0);
    final Config<Double> breakMin = new NumberConfig<>("BreakMinDmg", "", 0.0, 7.5, 36.0);
    final Config<Boolean> rotate = new BooleanConfig("Rotate", "", true);
    final Config<Boolean> newRotate = new BooleanConfig("YawStep", "", false);
    final Config<Float> yawStep = new NumberConfig<>("Step", "", 0.0f, 0.1f, 1.0f);
    final Config<Boolean> checkLook = new BooleanConfig("CheckLook", "", true);
    final Config<Float> fov = new NumberConfig<>("Fov", "", 0.0f, 10.0f, 30.0f);
    final Config<Boolean> box = new BooleanConfig("Box", "", true);
    final Config<Boolean> outline = new BooleanConfig("Outline", "", true);
    final Config<Color> color = new ColorConfig("Color", "", new Color(90, 90, 255), false, false);
    final Config<Integer> boxAlpha = new NumberConfig<>("BoxAlpha", "", 0, 80, 255);
    final Config<Integer> olAlpha = new NumberConfig<>("OLAlpha", "", 0, 80, 255);
    final Config<Float> olWidth = new NumberConfig<>("OLWidth", "", 0.1f, 1.5f, 5.0f);
    public final Config<Float> attackDelay = new NumberConfig<>("AttackDelay", "", 0.0f, 10.0f, 100.0f);
    private final Timer updateTimer = new CacheTimer();
    private final Timer delayTimer = new CacheTimer();
    private final Timer calcTimer = new CacheTimer();
    public Vec3d directionVec = null;
    private float lastYaw = 0.0f;
    private float lastPitch = 0.0f;
    private final ArrayList<BlockPos> chargeList = new ArrayList<>();
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
            if (this.box.getValue()) {
                RenderManager.renderBox(event.getMatrices(), this.currentPos, first.getRgb(this.boxAlpha.getValue()));
            }
            if (this.outline.getValue()) {
                RenderManager.renderBoundingBox(event.getMatrices(), this.currentPos, this.olWidth.getValue(), first.getRgb(this.olAlpha.getValue()));
            }
        }
    }

    @EventListener
    public void onRotate(RotateEvent event) {
        if (this.currentPos != null && this.newRotate.getValue() && this.directionVec != null) {
            float[] newAngle = this.injectStep(EntityUtil.getLegitRotations(this.directionVec), this.yawStep.getValue());
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
        if (this.newRotate.getValue() && this.currentPos != null && this.directionVec != null && !Managers.ROTATION.rotating && (packet = event.getPacket()) instanceof PlayerMoveC2SPacket) {
            PlayerMoveC2SPacket packet2 = (PlayerMoveC2SPacket) packet;
            if (!packet2.changesLook()) {
                return;
            }
            float yaw = packet2.getYaw(114514.0f);
            float pitch = packet2.getPitch(114514.0f);
            assert AutoAnchorModule.mc.player != null;
            if (yaw == AutoAnchorModule.mc.player.getYaw() && pitch == AutoAnchorModule.mc.player.getPitch()) {
                float[] angle = this.injectStep(EntityUtil.getLegitRotations(this.directionVec), this.yawStep.getValue());
                ((IPlayerMoveC2SPacket) event.getPacket()).setYaw(angle[0]);
                ((IPlayerMoveC2SPacket) event.getPacket()).setPitch(angle[1]);
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
        assert AutoAnchorModule.mc.player != null;
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
        if (this.pauseEat.getValue() && AutoAnchorModule.mc.player.isUsingItem()) {
            this.currentPos = null;
            return;
        }
        this.updateTimer.reset();
        if (this.calcTimer.passed((long)(this.calcDelay.getValue() * 1000.0f))) {
            this.calcTimer.reset();
            this.currentPos = null;
            double placeDamage = this.minDamage.getValue();
            double breakDamage = this.breakMin.getValue();
            boolean anchorFound = false;
            this.target = EntityUtil.getTarget(this.targetRange.getValue());
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
                        assert AutoAnchorModule.mc.world != null;
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
            EntityUtil.attackCrystal(this.currentPos, this.attackDelay.getValue());
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
                assert AutoAnchorModule.mc.world != null;
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
                assert AutoAnchorModule.mc.world != null;
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
        if (!this.newRotate.getValue()) {
            this.setRotation(directionVec);
            return true;
        }
        this.directionVec = directionVec;
        float[] angle = EntityUtil.getLegitRotations(directionVec);
        if (Math.abs(MathHelper.wrapDegrees(angle[0] - this.lastYaw)) < this.fov.getValue() && Math.abs(MathHelper.wrapDegrees(angle[1] - this.lastPitch)) < this.fov.getValue()) {
            return true;
        }
        return !this.checkLook.getValue();
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
        assert angle != null;
        return new float[]{angle[0], angle[1]};
    }

    public enum SwapMode {
        OFF,
        NORMAL,
        SILENT,
        Inventory,
        Pick

    }
}
