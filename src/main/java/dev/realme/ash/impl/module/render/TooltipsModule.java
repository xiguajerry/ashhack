package dev.realme.ash.impl.module.render;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.api.render.RenderManager;
import dev.realme.ash.impl.event.gui.RenderTooltipEvent;
import dev.realme.ash.init.Modules;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.collection.DefaultedList;

public class TooltipsModule
extends ToggleModule {
    Config<Boolean> shulkersConfig = new BooleanConfig("Shulkers", "Renders all the contents of shulkers in tooltips", true);
    Config<Boolean> mapsConfig = new BooleanConfig("Maps", "Renders a preview of maps in tooltips", false);

    public TooltipsModule() {
        super("Tooltips", "Renders detailed tooltips showing items", ModuleCategory.RENDER);
    }

    @EventListener
    public void onRenderTooltip(RenderTooltipEvent event) {
        ItemStack stack = event.getStack();
        if (stack.isEmpty()) {
            return;
        }
        NbtCompound nbtCompound = BlockItem.getBlockEntityNbt(stack);
        if (this.shulkersConfig.getValue().booleanValue() && nbtCompound != null && nbtCompound.contains("Items", 9)) {
            event.cancel();
            event.context.getMatrices().push();
            event.context.getMatrices().translate(0.0f, 0.0f, 600.0f);
            DefaultedList defaultedList = DefaultedList.ofSize(27, ItemStack.EMPTY);
            Inventories.readNbt(nbtCompound, defaultedList);
            RenderManager.rect(event.context.getMatrices(), (double)event.getX() + 8.0, (double)event.getY() - 21.0, 150.0, 13.0, Modules.CLIENT_SETTING.getRGB(170));
            RenderManager.renderText(event.getContext(), stack.getName().getString(), (float)event.getX() + 11.0f, (float)event.getY() - 18.0f, -1);
            RenderManager.rect(event.context.getMatrices(), (double)event.getX() + 8.0, (double)event.getY() - 7.0, 150.0, 55.0, 0x77000000);
            for (int i = 0; i < defaultedList.size(); ++i) {
                event.context.drawItem((ItemStack)defaultedList.get(i), event.getX() + i % 9 * 16 + 9, event.getY() + i / 9 * 16 - 5);
                event.context.drawItemInSlot(TooltipsModule.mc.textRenderer, (ItemStack)defaultedList.get(i), event.getX() + i % 9 * 16 + 9, event.getY() + i / 9 * 16 - 5);
            }
            event.context.getMatrices().pop();
        }
    }
}
