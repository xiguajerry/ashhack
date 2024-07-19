package dev.realme.ash.impl.module.combat;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.EnumConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.RotationModule;
import dev.realme.ash.impl.event.RotateEvent;
import dev.realme.ash.impl.event.network.PlayerTickEvent;
import dev.realme.ash.impl.event.network.UpdateWalkingEvent;
import dev.realme.ash.init.Managers;
import dev.realme.ash.init.Modules;
import dev.realme.ash.util.math.timer.CacheTimer;
import dev.realme.ash.util.math.timer.Timer;
import dev.realme.ash.util.player.InventoryUtil;
import dev.realme.ash.util.player.PlayerUtil;
import dev.realme.ash.util.world.BlockPosX;
import dev.realme.ash.util.world.BlockUtil;
import dev.realme.ash.util.world.EntityUtil;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class AutoWebModule
extends RotationModule {
    Config<Float> delay = new NumberConfig<Float>("Delay", "", Float.valueOf(0.0f), Float.valueOf(60.0f), Float.valueOf(500.0f));
    Config<SwapMode> swapMode = new EnumConfig("SwapMode", "", (Enum)SwapMode.SILENT, (Enum[])SwapMode.values());
    Config<Boolean> pauseEat = new BooleanConfig("PauseEat", "Not attacking while using items", true);
    Config<Integer> predictTicks = new NumberConfig<Integer>("PredictTicks", "", 0, 8, 20);
    Config<Integer> multiPlace = new NumberConfig<Integer>("MultiPlace", "", 0, 1, 5);
    Config<Integer> surCheck = new NumberConfig<Integer>("SurCheck", "", 0, 3, 4);
    Config<Float> range = new NumberConfig<Float>("Range", "", Float.valueOf(0.0f), Float.valueOf(5.0f), Float.valueOf(8.0f));
    Config<Boolean> face = new BooleanConfig("Face", "", true);
    Config<Boolean> feet = new BooleanConfig("Feet", "", true);
    Config<Boolean> down = new BooleanConfig("Down", "", true);
    Config<Boolean> extend = new BooleanConfig("Extend", "", false);
    Config<Boolean> checkAutoCrystal = new BooleanConfig("CheckAutoCrystal", "", false);
    Config<Boolean> sendPacket = new BooleanConfig("SendPacket", "", false);
    Config<Boolean> onlyPlaceOne = new BooleanConfig("OnlyPlaceOne", "", false);
    Config<Boolean> noAnchor = new BooleanConfig("NoAnchor", "", false);
    Config<Boolean> swing = new BooleanConfig("Swing", "", false);
    Config<Boolean> rotate = new BooleanConfig("Rotate", "", true);
    Config<Float> yawStep = new NumberConfig<Float>("Step", "", Float.valueOf(0.0f), Float.valueOf(0.1f), Float.valueOf(1.0f));
    Config<Float> fov = new NumberConfig<Float>("Fov", "", Float.valueOf(0.0f), Float.valueOf(10.0f), Float.valueOf(30.0f));
    private final Timer delayTimer = new CacheTimer();
    int progress = 0;
    PlayerEntity target;
    int slot;
    public Vec3d directionVec = null;
    private float lastYaw = 0.0f;
    private float lastPitch = 0.0f;

    public AutoWebModule() {
        super("AutoWeb", "Automatically traps nearby entities in webs", ModuleCategory.COMBAT, 700);
    }

    @Override
    public String getModuleData() {
        return this.target == null ? null : this.target.getName().getString();
    }

    @EventListener
    public void onUpdateWalking(UpdateWalkingEvent event) {
        this.update();
    }

    @EventListener
    public void onPlayerTick(PlayerTickEvent event) {
        this.update();
    }

    @EventListener(priority=98)
    public void onRotate(RotateEvent event) {
        if (this.directionVec != null) {
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
        Vec3d playerPos;
        if (AutoWebModule.nullCheck()) {
            return;
        }
        this.progress = 0;
        if (this.noAnchor.getValue().booleanValue() && Modules.AUTO_ANCHOR.currentPos != null) {
            this.target = null;
            return;
        }
        if (!this.delayTimer.passed(this.delay.getValue())) {
            return;
        }
        this.directionVec = null;
        switch (this.swapMode.getValue()) {
            case SILENT: 
            case NORMAL: {
                this.slot = InventoryUtil.findItem(Items.COBWEB);
                break;
            }
            case Inventory: {
                this.slot = InventoryUtil.findInventorySlot(Items.COBWEB, false);
                break;
            }
            case Pick: {
                this.slot = InventoryUtil.findInventorySlot(Items.COBWEB, true);
            }
        }
        if (this.slot == -1) {
            this.target = null;
            return;
        }
        if (this.pauseEat.getValue().booleanValue() && AutoWebModule.mc.player.isUsingItem()) {
            return;
        }
        this.target = EntityUtil.getTarget(this.range.getValue().floatValue());
        if (this.target == null) {
            return;
        }
        Vec3d vec3d = playerPos = this.predictTicks.getValue() > 0 ? EntityUtil.getEntityPosVec(this.target, this.predictTicks.getValue()) : this.target.getPos();
        if (this.feet.getValue().booleanValue() && this.surCheck(this.target)) {
            this.placeWeb(new BlockPosX(playerPos.getX(), playerPos.getY(), playerPos.getZ()));
            if (this.canExtend() || !PlayerUtil.isInWeb(this.target)) {
                this.placeWeb(new BlockPosX(playerPos.getX() + 0.3, playerPos.getY(), playerPos.getZ() + 0.3));
                this.placeWeb(new BlockPosX(playerPos.getX() - 0.3, playerPos.getY(), playerPos.getZ() - 0.3));
                this.placeWeb(new BlockPosX(playerPos.getX() - 0.3, playerPos.getY(), playerPos.getZ() + 0.3));
                this.placeWeb(new BlockPosX(playerPos.getX() + 0.3, playerPos.getY(), playerPos.getZ() - 0.3));
            }
        }
        if (this.down.getValue().booleanValue()) {
            this.placeWeb(new BlockPosX(playerPos.getX(), playerPos.getY() - 0.8, playerPos.getZ()));
            if (this.canExtend() || !PlayerUtil.isInWeb(this.target)) {
                this.placeWeb(new BlockPosX(playerPos.getX() + 0.3, playerPos.getY() - 0.8, playerPos.getZ() + 0.3));
                this.placeWeb(new BlockPosX(playerPos.getX() - 0.3, playerPos.getY() - 0.8, playerPos.getZ() - 0.3));
                this.placeWeb(new BlockPosX(playerPos.getX() - 0.3, playerPos.getY() - 0.8, playerPos.getZ() + 0.3));
                this.placeWeb(new BlockPosX(playerPos.getX() + 0.3, playerPos.getY() - 0.8, playerPos.getZ() - 0.3));
            }
        }
        if (this.face.getValue().booleanValue() && this.surCheck(this.target)) {
            this.placeWeb(new BlockPosX(playerPos.getX(), playerPos.getY() + 1.2, playerPos.getZ()));
            if (this.canExtend() || !PlayerUtil.isInWeb(this.target)) {
                this.placeWeb(new BlockPosX(playerPos.getX() + 0.3, playerPos.getY() + 1.2, playerPos.getZ() + 0.3));
                this.placeWeb(new BlockPosX(playerPos.getX() - 0.3, playerPos.getY() + 1.2, playerPos.getZ() - 0.3));
                this.placeWeb(new BlockPosX(playerPos.getX() - 0.3, playerPos.getY() + 1.2, playerPos.getZ() + 0.3));
                this.placeWeb(new BlockPosX(playerPos.getX() + 0.3, playerPos.getY() + 1.2, playerPos.getZ() - 0.3));
            }
        }
    }

    private boolean canExtend() {
        if (!this.extend.getValue().booleanValue()) {
            return false;
        }
        return Modules.AUTO_CRYSTAL.placePos == null || this.checkAutoCrystal.getValue() == false;
    }

    public boolean surCheck(PlayerEntity player) {
        int n = 0;
        BlockPos pos = PlayerUtil.playerPos(player);
        if (BlockUtil.getBlock(pos.add(0, 0, 1)) == Blocks.BEDROCK) {
            ++n;
        }
        if (BlockUtil.getBlock(pos.add(0, 0, -1)) == Blocks.BEDROCK) {
            ++n;
        }
        if (BlockUtil.getBlock(pos.add(1, 0, 0)) == Blocks.BEDROCK) {
            ++n;
        }
        if (BlockUtil.getBlock(pos.add(-1, 0, 0)) == Blocks.BEDROCK) {
            ++n;
        }
        return n < this.surCheck.getValue();
    }

    private boolean canPlace(BlockPos pos) {
        Direction direction = Managers.INTERACT.getPlaceDirection(pos);
        if (direction == null) {
            return false;
        }
        if (BlockUtil.getBlock(pos.down()) == Blocks.COBWEB && this.onlyPlaceOne.getValue().booleanValue() && Modules.AUTO_CRYSTAL.placePos != null) {
            return false;
        }
        return BlockUtil.canReplace(pos);
    }

    public static boolean selfCheck(BlockPos pos) {
        for (Entity entity : AutoWebModule.mc.world.getNonSpectatingEntities(Entity.class, new Box(pos))) {
            if (!(entity instanceof PlayerEntity) || !entity.equals(AutoWebModule.mc.player)) continue;
            return true;
        }
        return false;
    }

    private void placeWeb(BlockPos pos) {
        if (Managers.INTERACT.getPlaceDirection(pos) == null) {
            return;
        }
        if (!this.canPlace(pos)) {
            return;
        }
        if (AutoWebModule.selfCheck(pos)) {
            return;
        }
        if (this.progress >= this.multiPlace.getValue()) {
            return;
        }
        if (BlockUtil.isMining(pos)) {
            return;
        }
        int oldSlot = AutoWebModule.mc.player.getInventory().selectedSlot;
        switch (this.swapMode.getValue()) {
            case SILENT: 
            case NORMAL: {
                InventoryUtil.doSwap(this.slot);
                break;
            }
            case Inventory: {
                InventoryUtil.doInvSwap(this.slot);
                break;
            }
            case Pick: {
                InventoryUtil.doPickSwap(this.slot);
            }
        }
        if (this.rotate.getValue().booleanValue()) {
            Direction side = Managers.INTERACT.getPlaceDirection(pos);
            if (side == null) {
                return;
            }
            Vec3d vec = pos.toCenterPos().add((double)side.getVector().getX() * 0.5, (double)side.getVector().getY() * 0.5, (double)side.getVector().getZ() * 0.5);
            this.faceVector(vec);
        }
        Managers.INTERACT.placeBlock(pos, false, this.swing.getValue());
        switch (this.swapMode.getValue()) {
            case SILENT: {
                InventoryUtil.doSwap(oldSlot);
                break;
            }
            case Inventory: {
                InventoryUtil.doInvSwap(this.slot);
                break;
            }
            case Pick: {
                InventoryUtil.doPickSwap(this.slot);
            }
        }
        ++this.progress;
        this.delayTimer.reset();
    }

    private void faceVector(Vec3d directionVec) {
        if (!this.rotate.getValue().booleanValue()) {
            return;
        }
        this.directionVec = directionVec;
        float[] angle = EntityUtil.getLegitRotations(directionVec);
        if (Math.abs(MathHelper.wrapDegrees(angle[0] - this.lastYaw)) < this.fov.getValue().floatValue() && Math.abs(MathHelper.wrapDegrees(angle[1] - this.lastPitch)) < this.fov.getValue().floatValue()) {
            this.setRotation(directionVec, this.sendPacket.getValue());
        }
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
