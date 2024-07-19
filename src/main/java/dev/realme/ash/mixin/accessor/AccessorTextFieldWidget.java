package dev.realme.ash.mixin.accessor;

import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({TextFieldWidget.class})
public interface AccessorTextFieldWidget {
   @Accessor("drawsBackground")
   boolean isDrawsBackground();
}
