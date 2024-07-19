package dev.realme.ash.impl.module.world;

import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.network.AttackBlockEvent;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;

public class AutoToolModule
extends ToggleModule {
    public AutoToolModule() {
        super("AutoTool", "Automatically switches to a tool before mining", ModuleCategory.WORLD);
    }

    @EventListener
    public void onBreakBlock(AttackBlockEvent event) {
        BlockState state = AutoToolModule.mc.world.getBlockState(event.getPos());
        int blockSlot = this.getBestToolNoFallback(state);
        if (blockSlot != -1) {
            AutoToolModule.mc.player.getInventory().selectedSlot = blockSlot;
        }
    }

    public int getBestTool(BlockState state) {
        int slot = this.getBestToolNoFallback(state);
        if (slot != -1) {
            return slot;
        }
        return AutoToolModule.mc.player.getInventory().selectedSlot;
    }

    public int getBestToolNoFallback(BlockState state) {
        int slot = -1;
        float bestTool = 0.0f;
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = AutoToolModule.mc.player.getInventory().getStack(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof ToolItem)) continue;
            float speed = stack.getMiningSpeedMultiplier(state);
            int efficiency = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, stack);
            if (efficiency > 0) {
                speed += (float)(efficiency * efficiency) + 1.0f;
            }
            if (!(speed > bestTool)) continue;
            bestTool = speed;
            slot = i;
        }
        return slot;
    }
}
