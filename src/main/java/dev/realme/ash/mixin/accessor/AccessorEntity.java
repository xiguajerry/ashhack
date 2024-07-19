package dev.realme.ash.mixin.accessor;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({Entity.class})
public interface AccessorEntity {
   @Invoker("unsetRemoved")
   void hookUnsetRemoved();

   @Invoker("setFlag")
   void hookSetFlag(int var1, boolean var2);
}
