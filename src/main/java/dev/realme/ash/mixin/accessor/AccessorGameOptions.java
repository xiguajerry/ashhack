package dev.realme.ash.mixin.accessor;

import java.util.Set;
import net.minecraft.client.option.GameOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({GameOptions.class})
public interface AccessorGameOptions {
   @Accessor("enabledPlayerModelParts")
   @Mutable
   Set getPlayerModelParts();
}
