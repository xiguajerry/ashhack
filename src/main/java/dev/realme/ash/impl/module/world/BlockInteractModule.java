package dev.realme.ash.impl.module.world;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.NumberDisplay;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.network.PlayerTickEvent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

public class BlockInteractModule
extends ToggleModule {
    Config<Float> range = new NumberConfig<Float>("Range", "", 1.0f, 4.0f, 10.0f, NumberDisplay.DEFAULT);
    Config<Boolean> fluids = new BooleanConfig("Fluids", "", false);

    public BlockInteractModule() {
        super("BlockInteract", "Allows you to place blocks in the air", ModuleCategory.WORLD);
    }

    @EventListener
    public void onPlayerTick(PlayerTickEvent event) {
        BlockHitResult blockHitResult;
        ActionResult actionResult;
        ItemStack stack = BlockInteractModule.mc.player.getMainHandStack();
        if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem) || !BlockInteractModule.mc.options.useKey.isPressed()) {
            return;
        }
        HitResult result = BlockInteractModule.mc.player.raycast(this.range.getValue().floatValue(), 1.0f, this.fluids.getValue());
        if (result instanceof BlockHitResult && (actionResult = BlockInteractModule.mc.interactionManager.interactBlock(BlockInteractModule.mc.player, Hand.MAIN_HAND, blockHitResult = (BlockHitResult)result)).isAccepted() && actionResult.shouldSwingHand()) {
            BlockInteractModule.mc.player.swingHand(Hand.MAIN_HAND);
        }
    }
}
