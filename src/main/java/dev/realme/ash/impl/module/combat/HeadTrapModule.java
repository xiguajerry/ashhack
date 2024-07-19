package dev.realme.ash.impl.module.combat;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.EnumConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.RotationModule;
import dev.realme.ash.impl.event.network.UpdateWalkingEvent;
import dev.realme.ash.init.Managers;
import dev.realme.ash.init.Modules;
import dev.realme.ash.util.math.timer.CacheTimer;
import dev.realme.ash.util.math.timer.Timer;
import dev.realme.ash.util.player.InventoryUtil;
import dev.realme.ash.util.player.PlayerUtil;
import dev.realme.ash.util.world.BlockUtil;
import dev.realme.ash.util.world.EntityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

public class HeadTrapModule
extends RotationModule {
    Config<Float> delay = new NumberConfig<Float>("Delay", "", 0.0f, 60.0f, 500.0f);
    Config<Integer> multiPlace = new NumberConfig<Integer>("MultiPlace", "", 0, 1, 5);
    Config<Float> targetRange = new NumberConfig<Float>("TargetRange", "", 0.0f, 5.2f, 6.0f);
    Config<InventoryUtil.SwapMode> swapMode = new EnumConfig("SwapMode", "", InventoryUtil.SwapMode.SILENT, InventoryUtil.SwapMode.values());
    Config<Boolean> rotate = new BooleanConfig("Rotate", "", true);
    Config<Boolean> pauseEat = new BooleanConfig("PauseEat", "", true);
    Config<Boolean> anchorHelper = new BooleanConfig("AnchorHelper", "", false);
    Config<Boolean> antiStep = new BooleanConfig("AntiStep", "", false);
    Config<Boolean> extend = new BooleanConfig("Extend", "", false);
    private final Timer delayTimer = new CacheTimer();
    int progress = 0;
    PlayerEntity target;
    int slot = -1;

    public HeadTrapModule() {
        super("HeadTrap", "moongod.", ModuleCategory.COMBAT, 900);
    }

    @Override
    public String getModuleData() {
        return this.target == null ? null : this.target.getName().getString();
    }

    @EventListener
    public void onUpdateWalking(UpdateWalkingEvent event) {
        if (HeadTrapModule.mc.player == null || HeadTrapModule.mc.world == null) {
            return;
        }
        this.progress = 0;
        this.target = EntityUtil.getTarget(this.targetRange.getValue().floatValue());
        if (this.target == null) {
            return;
        }
        if (!this.delayTimer.passed(this.delay.getValue())) {
            return;
        }
        if (this.pauseEat.getValue().booleanValue() && HeadTrapModule.mc.player.isUsingItem()) {
            return;
        }
        this.doTrap(PlayerUtil.playerPos(this.target));
    }

    private void doTrap(BlockPos pos) {
        if (this.extend.getValue().booleanValue()) {
            for (int x : new int[]{1, 0, -1}) {
                for (int z : new int[]{1, 0, -1}) {
                    BlockPos offsetPos = pos.add(z, 0, x);
                    if (!this.checkEntity(new BlockPos(offsetPos))) continue;
                    this.placeBlock(offsetPos.up(2), this.anchorHelper.getValue());
                }
            }
        }
        if (BlockUtil.clientCanPlace(pos.up(2))) {
            if (Managers.INTERACT.getPlaceDirection(pos.up(2)) == null) {
                boolean trapChest = true;
                if (this.getHelper(pos.up(2)) != null) {
                    this.placeBlock(this.getHelper(pos.up(2)), false);
                    trapChest = false;
                }
                if (trapChest) {
                    for (Direction i : Direction.values()) {
                        BlockPos offsetPos;
                        if (i == Direction.DOWN || i == Direction.UP || !Managers.INTERACT.isStrictDirection(pos.offset(i).up(), i.getOpposite()) || !BlockUtil.clientCanPlace((offsetPos = pos.offset(i).up()).up()) || !BlockUtil.canPlace(offsetPos)) continue;
                        this.placeBlock(offsetPos, false);
                        trapChest = false;
                        break;
                    }
                    if (trapChest) {
                        for (Direction i : Direction.values()) {
                            BlockPos offsetPos;
                            if (i == Direction.DOWN || i == Direction.UP || !Managers.INTERACT.isStrictDirection(pos.offset(i).up(), i.getOpposite()) || !BlockUtil.clientCanPlace((offsetPos = pos.offset(i).up()).up()) || Managers.INTERACT.getPlaceDirection(offsetPos) != null || !BlockUtil.clientCanPlace(offsetPos) || this.getHelper(offsetPos) == null) continue;
                            this.placeBlock(this.getHelper(offsetPos), false);
                            trapChest = false;
                            break;
                        }
                        if (trapChest) {
                            for (Direction i : Direction.values()) {
                                BlockPos offsetPos;
                                if (i == Direction.DOWN || i == Direction.UP || !Managers.INTERACT.isStrictDirection(pos.offset(i).up(), i.getOpposite()) || !BlockUtil.clientCanPlace((offsetPos = pos.offset(i).up()).up()) || Managers.INTERACT.getPlaceDirection(offsetPos) != null || !BlockUtil.clientCanPlace(offsetPos) || this.getHelper(offsetPos) == null || Managers.INTERACT.getPlaceDirection(offsetPos.down()) != null || !BlockUtil.clientCanPlace(offsetPos.down()) || this.getHelper(offsetPos.down()) == null) continue;
                                this.placeBlock(this.getHelper(offsetPos.down()), false);
                                break;
                            }
                        }
                    }
                }
            }
            this.placeBlock(pos.up(2), this.anchorHelper.getValue());
        }
        if (this.antiStep.getValue().booleanValue()) {
            if (Managers.INTERACT.getPlaceDirection(pos.up(3)) == null && BlockUtil.clientCanPlace(pos.up(3)) && this.getHelper(pos.up(3)) != null) {
                this.placeBlock(this.getHelper(pos.up(3)), false);
            }
            this.placeBlock(pos.up(3), false);
        }
    }

    private boolean checkEntity(BlockPos pos) {
        if (HeadTrapModule.mc.player.getBoundingBox().intersects(new Box(pos))) {
            return false;
        }
        for (Entity entity : HeadTrapModule.mc.world.getNonSpectatingEntities(PlayerEntity.class, new Box(pos))) {
            if (!entity.isAlive()) continue;
            return true;
        }
        return false;
    }

    public BlockPos getHelper(BlockPos pos) {
        for (Direction i : Direction.values()) {
            if (BlockUtil.isMining(pos.offset(i)) || !Managers.INTERACT.isStrictDirection(pos.offset(i), i.getOpposite()) || !BlockUtil.canPlace(pos.offset(i))) continue;
            return pos.offset(i);
        }
        return null;
    }

    private void placeBlock(BlockPos pos, boolean placeAnchor) {
        if (this.progress >= this.multiPlace.getValue()) {
            return;
        }
        if (!BlockUtil.canPlace(pos)) {
            return;
        }
        EntityUtil.attackCrystal(pos);
        if (pos.equals(Modules.PACKET_DIGGING.breakPos)) {
            Modules.PACKET_DIGGING.breakPos = null;
            Modules.PACKET_DIGGING.startMine = false;
            Modules.PACKET_DIGGING.breakNumber = 0;
        }
        if (BlockUtil.isMining(pos)) {
            return;
        }
        int n = placeAnchor ? (this.anchor() != -1 ? this.anchor() : this.obsidian()) : (this.slot = this.obsidian());
        if (this.slot == -1) {
            return;
        }
        int oldSlot = HeadTrapModule.mc.player.getInventory().selectedSlot;
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
        Managers.INTERACT.placeBlock(pos, this.rotate.getValue());
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

    private int anchor() {
        int slot = -1;
        switch (this.swapMode.getValue()) {
            case SILENT: 
            case NORMAL: {
                slot = InventoryUtil.findItem(Items.RESPAWN_ANCHOR);
                break;
            }
            case Inventory: {
                slot = InventoryUtil.findInventorySlot(Items.RESPAWN_ANCHOR, false);
                break;
            }
            case Pick: {
                slot = InventoryUtil.findInventorySlot(Items.RESPAWN_ANCHOR, true);
            }
        }
        return slot;
    }

    private int obsidian() {
        int slot = -1;
        switch (this.swapMode.getValue()) {
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
        return slot;
    }
}
