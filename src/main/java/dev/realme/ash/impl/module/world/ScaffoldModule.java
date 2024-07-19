package dev.realme.ash.impl.module.world;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.RotationModule;
import dev.realme.ash.impl.event.RotateEvent;
import dev.realme.ash.impl.event.TickEvent;
import dev.realme.ash.impl.event.entity.player.PlayerMoveEvent;
import dev.realme.ash.init.Managers;
import dev.realme.ash.util.math.timer.CacheTimer;
import dev.realme.ash.util.math.timer.Timer;
import dev.realme.ash.util.player.InventoryUtil;
import dev.realme.ash.util.player.MovementUtil;
import dev.realme.ash.util.player.PlayerUtil;
import dev.realme.ash.util.world.BlockUtil;
import dev.realme.ash.util.world.EntityUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class ScaffoldModule
extends RotationModule {
    Config<Boolean> rotate = new BooleanConfig("Rotate", "", true);
    private final Timer timer = new CacheTimer();
    private final Timer lastTimer = new CacheTimer();
    private float[] angle = null;
    private final Timer timer2 = new CacheTimer();
    private BlockPos lastPos;

    public ScaffoldModule() {
        super("Scaffold", "Rapidly places blocks under your feet", ModuleCategory.WORLD);
    }

    @Override
    protected void onDisable() {
        super.onDisable();
    }

    @EventListener(priority=100)
    public void onRotation(RotateEvent event) {
        if (this.rotate.getValue().booleanValue() && !this.timer.passed(500) && this.angle != null) {
            event.setYaw(this.angle[0]);
            event.setPitch(this.angle[1]);
        }
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (ScaffoldModule.nullCheck()) {
            return;
        }
        if (ScaffoldModule.mc.options.jumpKey.isPressed() && !MovementUtil.isMoving()) {
            if (this.lastTimer.passed(500)) {
                this.lastTimer.reset();
                this.lastPos = null;
            }
            if (this.timer2.passed(3000)) {
                MovementUtil.setMotionY(-0.28);
                this.timer2.reset();
                this.lastPos = null;
            } else if (this.lastPos == null || this.lastPos.equals(PlayerUtil.playerPos(ScaffoldModule.mc.player))) {
                this.lastPos = PlayerUtil.playerPos(ScaffoldModule.mc.player).up();
                MovementUtil.setMotionY(0.42);
                MovementUtil.setMotionX(0.0);
                MovementUtil.setMotionZ(0.0);
            }
        } else {
            this.timer2.reset();
            this.lastPos = null;
        }
    }

    @EventListener
    public void onMove(PlayerMoveEvent event) {
        int block = InventoryUtil.findBlock();
        if (block == -1) {
            return;
        }
        BlockPos placePos = PlayerUtil.playerPos(ScaffoldModule.mc.player).down();
        if (BlockUtil.clientCanPlace(placePos)) {
            int old = ScaffoldModule.mc.player.getInventory().selectedSlot;
            if (Managers.INTERACT.getPlaceDirection(placePos) == null) {
                double distance = 1000.0;
                BlockPos bestPos = null;
                for (Direction i : Direction.values()) {
                    if (i == Direction.UP || !BlockUtil.canPlace(placePos.offset(i)) || bestPos != null && !(ScaffoldModule.mc.player.squaredDistanceTo(placePos.offset(i).toCenterPos()) < distance)) continue;
                    bestPos = placePos.offset(i);
                    distance = ScaffoldModule.mc.player.squaredDistanceTo(placePos.offset(i).toCenterPos());
                }
                if (bestPos != null) {
                    placePos = bestPos;
                } else {
                    return;
                }
            }
            if (this.rotate.getValue().booleanValue()) {
                Direction side = Managers.INTERACT.getPlaceDirection(placePos);
                this.angle = EntityUtil.getLegitRotations(placePos.offset(side).toCenterPos().add((double)side.getOpposite().getVector().getX() * 0.5, (double)side.getOpposite().getVector().getY() * 0.5, (double)side.getOpposite().getVector().getZ() * 0.5));
                this.timer.reset();
            }
            InventoryUtil.doSwap(block);
            Managers.INTERACT.placeBlock(placePos, false);
            InventoryUtil.doSwap(old);
            this.lastTimer.reset();
        }
    }
}
