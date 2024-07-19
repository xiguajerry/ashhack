package dev.realme.ash.mixin.accessor;

import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.screen.HorseScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({HorseScreenHandler.class})
public interface AccessorHorseScreenHandler {
   @Accessor("entity")
   AbstractHorseEntity getEntity();
}
