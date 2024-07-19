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
import dev.realme.ash.util.math.timer.CacheTimer;
import dev.realme.ash.util.math.timer.Timer;
import dev.realme.ash.util.player.InventoryUtil;
import dev.realme.ash.util.player.PlayerUtil;
import dev.realme.ash.util.world.BlockPosX;
import dev.realme.ash.util.world.BlockUtil;
import dev.realme.ash.util.world.EntityUtil;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

public class FlattenModule
extends RotationModule {
    final Config<Float> delay = new NumberConfig<>("Delay", "", 0.0f, 60.0f, 500.0f);
    final Config<Integer> multiPlace = new NumberConfig<>("MultiPlace", "", 0, 1, 5);
    final Config<InventoryUtil.SwapMode> swapMode = new EnumConfig<>("SwapMode", "", InventoryUtil.SwapMode.SILENT, InventoryUtil.SwapMode.values());
    final Config<Boolean> pauseEat = new BooleanConfig("PauseEat", "", true);
    final Config<Boolean> onlyInBlock = new BooleanConfig("OnlyInBlock", "", true);
    final Config<Boolean> rotate = new BooleanConfig("Rotate", "", true);
    final Config<Boolean> swing = new BooleanConfig("Swing", "", false);
    private final Timer delayTimer = new CacheTimer();
    int progress = 0;

    public FlattenModule() {
        super("Flatten", "otto", ModuleCategory.COMBAT, 700);
    }

    @EventListener
    public void onUpdateWalking(UpdateWalkingEvent event) {
        if (FlattenModule.nullCheck()) {
            return;
        }
        this.progress = 0;
        if (!this.delayTimer.passed(this.delay.getValue())) {
            return;
        }
        if (!PlayerUtil.isInsideBlock() && this.onlyInBlock.getValue()) {
            return;
        }
        if (this.pauseEat.getValue() && FlattenModule.mc.player.isUsingItem()) {
            return;
        }
        int oldSlot = FlattenModule.mc.player.getInventory().selectedSlot;
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
        if (slot == -1 && !this.swapMode.getValue().equals(InventoryUtil.SwapMode.OFF)) {
            return;
        }
        BlockPosX pos1 = new BlockPosX(FlattenModule.mc.player.getX() + 0.3, FlattenModule.mc.player.getY() - 1.0, FlattenModule.mc.player.getZ() + 0.3);
        BlockPosX pos2 = new BlockPosX(FlattenModule.mc.player.getX() - 0.3, FlattenModule.mc.player.getY() - 1.0, FlattenModule.mc.player.getZ() + 0.3);
        BlockPosX pos3 = new BlockPosX(FlattenModule.mc.player.getX() + 0.3, FlattenModule.mc.player.getY() - 1.0, FlattenModule.mc.player.getZ() - 0.3);
        BlockPosX pos4 = new BlockPosX(FlattenModule.mc.player.getX() - 0.3, FlattenModule.mc.player.getY() - 1.0, FlattenModule.mc.player.getZ() - 0.3);
        if (!(this.canPlace(pos1) || this.canPlace(pos2) || this.canPlace(pos3) || this.canPlace(pos4))) {
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
        this.doPlace(pos1);
        this.doPlace(pos2);
        this.doPlace(pos3);
        this.doPlace(pos4);
        switch (this.swapMode.getValue()) {
            case SILENT: {
                InventoryUtil.doSwap(oldSlot);
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
    }

    private void doPlace(BlockPos pos) {
        if (this.progress >= this.multiPlace.getValue()) {
            return;
        }
        if (this.canPlace(pos)) {
            Managers.INTERACT.placeBlock(pos, this.rotate.getValue(), this.swing.getValue());
            this.delayTimer.reset();
            ++this.progress;
        }
    }

    private boolean canPlace(BlockPos pos) {
        if (Managers.INTERACT.getPlaceDirection(pos) == null) {
            return false;
        }
        if (!BlockUtil.canReplace(pos)) {
            return false;
        }
        if (BlockUtil.isMining(pos)) {
            return false;
        }
        return !EntityUtil.hasEntity(pos);
    }
}
