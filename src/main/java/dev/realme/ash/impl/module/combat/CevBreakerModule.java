package dev.realme.ash.impl.module.combat;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.EnumConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.network.UpdateWalkingEvent;
import dev.realme.ash.init.Managers;
import dev.realme.ash.init.Modules;
import dev.realme.ash.util.chat.ChatUtil;
import dev.realme.ash.util.math.timer.CacheTimer;
import dev.realme.ash.util.math.timer.Timer;
import dev.realme.ash.util.player.InventoryUtil;
import dev.realme.ash.util.player.PlayerUtil;
import dev.realme.ash.util.world.BlockUtil;
import dev.realme.ash.util.world.EntityUtil;
import java.util.List;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

public class CevBreakerModule
extends ToggleModule {
    public final Config<Float> attackDelay = new NumberConfig<>("AttackDelay", "", 0.0f, 20.0f, 2000.0f);
    public final Config<Float> placeDelay = new NumberConfig<>("PlaceDelay", "", 0.0f, 100.0f, 2000.0f);
    final Config<InventoryUtil.SwapMode> swapMode = new EnumConfig<>("SwapMode", "", InventoryUtil.SwapMode.SILENT, InventoryUtil.SwapMode.values());
    final Config<Boolean> pauseEat = new BooleanConfig("PauseEat", "", true);
    final Config<Boolean> placeRotate = new BooleanConfig("PlaceObsRotate", "", true);
    final Config<Boolean> placeCryRotate = new BooleanConfig("PlaceCryRotate", "", false);
    public final Timer attackTimer = new CacheTimer();
    public final Timer placeTimer = new CacheTimer();
    int obsSlot = -1;
    int crySlot = -1;
    public boolean isCrystal = false;

    public CevBreakerModule() {
        super("CevBreaker", "crystal boomer.", ModuleCategory.COMBAT);
    }

    @EventListener
    public void onUpdateWalking(UpdateWalkingEvent event) {
        if (CevBreakerModule.nullCheck()) {
            return;
        }
        if (this.pauseEat.getValue() && CevBreakerModule.mc.player.isUsingItem()) {
            return;
        }
        switch (this.swapMode.getValue()) {
            case SILENT: 
            case NORMAL: {
                this.obsSlot = InventoryUtil.findItem(Items.OBSIDIAN);
                this.crySlot = InventoryUtil.findItem(Items.END_CRYSTAL);
                break;
            }
            case Inventory: {
                this.obsSlot = InventoryUtil.findInventorySlot(Items.OBSIDIAN, false);
                this.crySlot = InventoryUtil.findInventorySlot(Items.END_CRYSTAL, false);
                break;
            }
            case Pick: {
                this.obsSlot = InventoryUtil.findInventorySlot(Items.OBSIDIAN, true);
                this.crySlot = InventoryUtil.findInventorySlot(Items.END_CRYSTAL, true);
            }
        }
        if (this.obsSlot == -1 || this.crySlot == -1) {
            ChatUtil.sendChatMessageWidthId("No Item.", this.hashCode() + 777);
            this.disable();
            return;
        }
        BlockPos breakPos = Modules.PACKET_DIGGING.breakPos;
        if (breakPos == null) {
            return;
        }
        this.isCrystal = EntityUtil.hasCrystal(breakPos.up());
        if (this.placeTimer.passed(this.placeDelay.getValue())) {
            if (BlockUtil.isAir(breakPos) && !this.isCrystal) {
                this.doPlaceObs(breakPos);
            }
            if (BlockUtil.getBlock(breakPos) == Blocks.OBSIDIAN && !this.isCrystal) {
                this.doPlaceCry(breakPos);
            }
        }
        if (this.isCrystal && BlockUtil.isAir(breakPos)) {
            this.attackCrystal(breakPos.up());
        }
    }

    public void attackCrystal(BlockPos pos) {
        if (!this.attackTimer.passed(this.attackDelay.getValue())) {
            return;
        }
        List<Entity> entities = CevBreakerModule.mc.world.getOtherEntities(null, new Box(pos)).stream().filter(e -> e instanceof EndCrystalEntity).toList();
        for (Entity entity : entities) {
            Managers.NETWORK.sendPacket(PlayerInteractEntityC2SPacket.attack(entity, CevBreakerModule.mc.player.isSneaking()));
            PlayerUtil.doSwing();
            this.attackTimer.reset();
        }
    }

    private void doPlaceObs(BlockPos pos) {
        if (!BlockUtil.canPlace(pos)) {
            return;
        }
        int oldSlot = CevBreakerModule.mc.player.getInventory().selectedSlot;
        switch (this.swapMode.getValue()) {
            case SILENT: 
            case NORMAL: {
                InventoryUtil.doSwap(this.obsSlot);
                break;
            }
            case Inventory: {
                InventoryUtil.doInvSwap(this.obsSlot);
                break;
            }
            case Pick: {
                InventoryUtil.doPickSwap(this.obsSlot);
            }
        }
        Managers.INTERACT.placeBlock(pos, this.placeRotate.getValue(), false);
        switch (this.swapMode.getValue()) {
            case SILENT: {
                InventoryUtil.doSwap(oldSlot);
                break;
            }
            case Inventory: {
                InventoryUtil.doInvSwap(this.obsSlot);
                break;
            }
            case Pick: {
                InventoryUtil.doPickSwap(this.obsSlot);
            }
        }
        this.placeTimer.reset();
    }

    private void doPlaceCry(BlockPos pos) {
        Direction facing = Managers.INTERACT.getClickDirection(pos);
        if (facing == null) {
            return;
        }
        int oldSlot = CevBreakerModule.mc.player.getInventory().selectedSlot;
        switch (this.swapMode.getValue()) {
            case SILENT: 
            case NORMAL: {
                InventoryUtil.doSwap(this.crySlot);
                break;
            }
            case Inventory: {
                InventoryUtil.doInvSwap(this.crySlot);
                break;
            }
            case Pick: {
                InventoryUtil.doPickSwap(this.crySlot);
            }
        }
        Managers.INTERACT.clickBlock(pos, facing, this.placeCryRotate.getValue());
        switch (this.swapMode.getValue()) {
            case SILENT: {
                InventoryUtil.doSwap(oldSlot);
                break;
            }
            case Inventory: {
                InventoryUtil.doInvSwap(this.crySlot);
                break;
            }
            case Pick: {
                InventoryUtil.doPickSwap(this.crySlot);
            }
        }
        this.placeTimer.reset();
    }
}
