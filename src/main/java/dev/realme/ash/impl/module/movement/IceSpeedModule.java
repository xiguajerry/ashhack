package dev.realme.ash.impl.module.movement;

import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.mixin.accessor.AccessorAbstractBlock;
import net.minecraft.block.Blocks;

public class IceSpeedModule
extends ToggleModule {
    public IceSpeedModule() {
        super("IceSpeed", "Modifies the walking speed on ice", ModuleCategory.MOVEMENT);
    }

    @Override
    public void onEnable() {
        if (IceSpeedModule.mc.world == null) {
            return;
        }
        ((AccessorAbstractBlock) Blocks.ICE).setSlipperiness(0.4f);
        ((AccessorAbstractBlock) Blocks.PACKED_ICE).setSlipperiness(0.4f);
        ((AccessorAbstractBlock) Blocks.BLUE_ICE).setSlipperiness(0.4f);
        ((AccessorAbstractBlock) Blocks.FROSTED_ICE).setSlipperiness(0.4f);
    }

    @Override
    public void onDisable() {
        if (IceSpeedModule.mc.world == null) {
            return;
        }
        ((AccessorAbstractBlock) Blocks.ICE).setSlipperiness(0.98f);
        ((AccessorAbstractBlock) Blocks.PACKED_ICE).setSlipperiness(0.98f);
        ((AccessorAbstractBlock) Blocks.BLUE_ICE).setSlipperiness(0.98f);
        ((AccessorAbstractBlock) Blocks.FROSTED_ICE).setSlipperiness(0.98f);
    }
}
