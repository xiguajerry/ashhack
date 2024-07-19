package dev.realme.ash.impl.module.misc;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.EnumConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.RotationModule;
import dev.realme.ash.impl.event.TickEvent;
import dev.realme.ash.impl.manager.player.interaction.InteractionManager;
import dev.realme.ash.init.Managers;
import dev.realme.ash.init.Modules;
import dev.realme.ash.util.player.InventoryUtil;
import dev.realme.ash.util.player.RayCastUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class MiddleClickModule
extends RotationModule {
    final Config<Boolean> friend = new BooleanConfig("Friend", "Friends players when middle click", true);
    final Config<Boolean> pearl = new BooleanConfig("Pearl", "Throws a pearl when middle click", true);
    final Config<SwapMode> swapMode = new EnumConfig<>("SwapMode", "", SwapMode.SILENT, SwapMode.values());
    boolean click = false;

    public MiddleClickModule() {
        super("MiddleClick", "Adds an additional bind on the mouse middle button", ModuleCategory.MISCELLANEOUS);
    }

    @EventListener
    public void onTick(TickEvent event) {
        HitResult result;
        if (MiddleClickModule.nullCheck()) {
            return;
        }
        if (MiddleClickModule.mc.interactionManager == null) {
            return;
        }
        double reachDistance = MiddleClickModule.mc.interactionManager.hasExtendedReach() ? 6.0 : (double)MiddleClickModule.mc.interactionManager.getReachDistance();
        HitResult hitResult = result = Modules.FREECAM.isEnabled() ? RayCastUtil.raycastEntity(reachDistance, Modules.FREECAM.getCameraPosition(), Modules.FREECAM.getCameraRotations()) : RayCastUtil.raycastEntity(reachDistance);
        if (MiddleClickModule.mc.mouse.wasMiddleButtonClicked()) {
            if (!this.click) {
                int slot;
                Entity entity;
                if (result != null && this.friend.getValue() && result instanceof EntityHitResult && (entity = ((EntityHitResult)result).getEntity()) instanceof PlayerEntity) {
                    PlayerEntity target = (PlayerEntity)entity;
                    String targetName = target.getName().getString();
                    if (Managers.SOCIAL.isFriend(targetName)) {
                        Managers.SOCIAL.remove(targetName);
                    } else {
                        Managers.SOCIAL.addFriend(targetName);
                    }
                } else if (this.pearl.getValue() && (slot = this.findPearlSlot()) != -1) {
                    this.doPearlSwap(slot);
                }
                this.click = true;
            }
        } else {
            this.click = false;
        }
    }

    private int findPearlSlot() {
        return switch (this.swapMode.getValue()) {
            case SILENT, NORMAL -> InventoryUtil.findItem(Items.ENDER_PEARL);
            case Inventory -> InventoryUtil.findInventorySlot(Items.ENDER_PEARL, false);
            case Pick -> InventoryUtil.findInventorySlot(Items.ENDER_PEARL, true);
            default -> -1;
        };
    }

    private void doPearlSwap(int slot) {
        if (MiddleClickModule.mc.player.getItemCooldownManager().isCoolingDown(Items.ENDER_PEARL)) {
            return;
        }
        int prevSlot = MiddleClickModule.mc.player.getInventory().selectedSlot;
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
        Managers.ROTATION.sendLook(new PlayerMoveC2SPacket.LookAndOnGround(MiddleClickModule.mc.player.getYaw(), MiddleClickModule.mc.player.getPitch(), Managers.POSITION.isOnGround()));
        MiddleClickModule.mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, InteractionManager.getWorldActionId(MiddleClickModule.mc.world)));
        switch (this.swapMode.getValue()) {
            case SILENT: {
                InventoryUtil.doSwap(prevSlot);
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

    public enum SwapMode {
        OFF,
        NORMAL,
        SILENT,
        Inventory,
        Pick

    }
}
