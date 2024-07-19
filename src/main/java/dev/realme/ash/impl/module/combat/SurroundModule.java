package dev.realme.ash.impl.module.combat;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.NumberDisplay;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.EnumConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.RotationModule;
import dev.realme.ash.impl.event.network.UpdateWalkingEvent;
import dev.realme.ash.init.Managers;
import dev.realme.ash.util.math.timer.CacheTimer;
import dev.realme.ash.util.math.timer.Timer;
import dev.realme.ash.util.player.InventoryUtil;
import dev.realme.ash.util.player.MovementUtil;
import dev.realme.ash.util.player.PlayerUtil;
import dev.realme.ash.util.world.BlockUtil;
import dev.realme.ash.util.world.EntityUtil;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

public class SurroundModule
extends RotationModule {
    final Config<Boolean> rotate = new BooleanConfig("Rotate", "", false);
    final Config<Boolean> pauseEat = new BooleanConfig("PauseEat", "", true);
    final Config<SwapMode> swapMode = new EnumConfig<>("SwapMode", "", SwapMode.SILENT, SwapMode.values());
    final Config<Integer> multiPlace = new NumberConfig<>("MultiPlace", "", 0, 1, 5);
    final Config<Float> delay = new NumberConfig<>("Delay", "Delay to throw xp in ticks.", 0.0f, 0.0f, 10.0f, NumberDisplay.DEFAULT);
    final Config<Boolean> helper = new BooleanConfig("Helper", "", true);
    final Config<Boolean> extend = new BooleanConfig("Extend", "", true);
    final Config<Boolean> onlyGround = new BooleanConfig("OnlyGround", "", true);
    final Config<Boolean> moveDisable = new BooleanConfig("MoveDisable", "", true);
    final Config<Boolean> strictDisable = new BooleanConfig("StrictDisable", "", false);
    final Config<Boolean> isMoving = new BooleanConfig("IsMoving", "", false);
    final Config<Boolean> jumpDisable = new BooleanConfig("JumpDisable", "", true);
    final Config<Boolean> inMoving = new BooleanConfig("InMoving", "", false);
    public final Config<Float> attackDelay = new NumberConfig<>("AttackDelay", "", 0.0f, 10.0f, 100.0f);
    private final Timer timer = new CacheTimer();
    public final Timer attackTimer = new CacheTimer();
    double startX = 0.0;
    double startY = 0.0;
    double startZ = 0.0;
    int progress = 0;
    BlockPos startPos = null;
    int block;

    public SurroundModule() {
        super("Surround", "Surrounds feet with obsidian", ModuleCategory.COMBAT, 950);
    }

    @Override
    public void onEnable() {
        if (SurroundModule.mc.player == null) {
            return;
        }
        this.startPos = PlayerUtil.playerPos(SurroundModule.mc.player);
        this.startX = SurroundModule.mc.player.getX();
        this.startY = SurroundModule.mc.player.getY();
        this.startZ = SurroundModule.mc.player.getZ();
    }

    @EventListener
    public void onUpdateWalking(UpdateWalkingEvent event) {
        if (!this.timer.passed(this.delay.getValue() * 100.0f)) {
            return;
        }
        if (this.pauseEat.getValue() && SurroundModule.mc.player.isUsingItem()) {
            return;
        }
        this.progress = 0;
        BlockPos pos = PlayerUtil.playerPos(SurroundModule.mc.player);
        if (this.startPos == null || !pos.equals(this.startPos) && this.moveDisable.getValue() && this.strictDisable.getValue() && (!this.isMoving.getValue() || MovementUtil.isMoving()) || (double)MathHelper.sqrt((float)SurroundModule.mc.player.squaredDistanceTo(this.startX, this.startY, this.startZ)) > 1.3 && this.moveDisable.getValue() && !this.strictDisable.getValue() && (!this.isMoving.getValue() || MovementUtil.isMoving()) || this.jumpDisable.getValue() && (this.startY - SurroundModule.mc.player.getY() > 0.5 || this.startY - SurroundModule.mc.player.getY() < -0.5) && (!this.inMoving.getValue() || MovementUtil.isMoving())) {
            this.disable();
            return;
        }
        switch (this.swapMode.getValue()) {
            case SILENT: 
            case NORMAL: {
                this.block = InventoryUtil.findItem(Items.OBSIDIAN);
                break;
            }
            case Inventory: {
                this.block = InventoryUtil.findInventorySlot(Items.OBSIDIAN, false);
                break;
            }
            case Pick: {
                this.block = InventoryUtil.findInventorySlot(Items.OBSIDIAN, true);
            }
        }
        if (this.block == -1) {
            this.disable();
            return;
        }
        if (this.onlyGround.getValue() && !SurroundModule.mc.player.isOnGround()) {
            return;
        }
        for (Direction i : Direction.values()) {
            if (i == Direction.UP) continue;
            BlockPos offsetPos = pos.offset(i);
            if (Managers.INTERACT.getPlaceDirection(offsetPos) != null) {
                this.placeBlock(offsetPos);
            } else if (BlockUtil.canReplace(offsetPos)) {
                this.placeBlock(this.getHelper(offsetPos));
            }
            if (!SurroundModule.checkSelf(offsetPos) || !this.extend.getValue()) continue;
            for (Direction i2 : Direction.values()) {
                if (i2 == Direction.UP) continue;
                BlockPos offsetPos2 = offsetPos.offset(i2);
                if (SurroundModule.checkSelf(offsetPos2)) {
                    for (Direction i3 : Direction.values()) {
                        if (i3 == Direction.UP) continue;
                        this.placeBlock(offsetPos2);
                        BlockPos offsetPos3 = offsetPos2.offset(i3);
                        this.placeBlock(Managers.INTERACT.getPlaceDirection(offsetPos3) != null || !BlockUtil.canReplace(offsetPos3) ? offsetPos3 : this.getHelper(offsetPos3));
                    }
                }
                this.placeBlock(Managers.INTERACT.getPlaceDirection(offsetPos2) != null || !BlockUtil.canReplace(offsetPos2) ? offsetPos2 : this.getHelper(offsetPos2));
            }
        }
    }

    private void placeBlock(BlockPos pos) {
        if (pos == null) {
            return;
        }
        if (EntityUtil.hasCrystal(pos)) {
            this.attackCrystal(pos);
        }
        if (!this.canPlace(pos)) {
            return;
        }
        if (EntityUtil.hasEntity(pos)) {
            return;
        }
        if (this.progress >= this.multiPlace.getValue()) {
            return;
        }
        int old = SurroundModule.mc.player.getInventory().selectedSlot;
        switch (this.swapMode.getValue()) {
            case SILENT: 
            case NORMAL: {
                InventoryUtil.doSwap(this.block);
                break;
            }
            case Inventory: {
                InventoryUtil.doInvSwap(this.block);
                break;
            }
            case Pick: {
                InventoryUtil.doPickSwap(this.block);
            }
        }
        Managers.INTERACT.placeBlock(pos, this.rotate.getValue());
        switch (this.swapMode.getValue()) {
            case SILENT: {
                InventoryUtil.doSwap(old);
                break;
            }
            case Inventory: {
                InventoryUtil.doInvSwap(this.block);
                break;
            }
            case Pick: {
                InventoryUtil.doPickSwap(this.block);
            }
        }
        ++this.progress;
        this.timer.reset();
    }

    public void attackCrystal(BlockPos pos) {
        if (!this.attackTimer.passed(this.attackDelay.getValue())) {
            return;
        }
        List<Entity> entities = SurroundModule.mc.world.getOtherEntities(null, new Box(pos)).stream().filter(e -> e instanceof EndCrystalEntity).toList();
        for (Entity entity : entities) {
            Managers.NETWORK.sendPacket(PlayerInteractEntityC2SPacket.attack(entity, SurroundModule.mc.player.isSneaking()));
            PlayerUtil.doSwing();
            this.attackTimer.reset();
        }
    }

    static boolean checkSelf(BlockPos pos) {
        return SurroundModule.mc.player.getBoundingBox().intersects(new Box(pos));
    }

    public BlockPos getHelper(BlockPos pos) {
        if (!this.helper.getValue()) {
            return pos.down();
        }
        for (Direction i : Direction.values()) {
            if (!this.canPlace(pos.offset(i))) continue;
            return pos.offset(i);
        }
        return null;
    }

    private boolean canPlace(BlockPos pos) {
        if (Managers.INTERACT.getPlaceDirection(pos) == null) {
            return false;
        }
        if (!BlockUtil.canReplace(pos)) {
            return false;
        }
        return !EntityUtil.hasEntity(pos);
    }

    public enum SwapMode {
        OFF,
        NORMAL,
        SILENT,
        Inventory,
        Pick

    }
}
