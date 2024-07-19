// Decompiled with: FernFlower
// Class Version: 17
package dev.realme.ash.mixin.option;

import com.mojang.serialization.Codec;

import java.io.File;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.option.SimpleOption.ValidatingIntSliderCallbacks;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({GameOptions.class})
public class MixinGameOptions {
    @Mutable
    @Shadow
    @Final
    private SimpleOption<Integer> fov;

    @Inject(
            method = {"<init>"},
            at = {@At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/option/GameOptions;load()V",
                    shift = Shift.BEFORE
            )}
    )
    private void hookInit(MinecraftClient client, File optionsFile, CallbackInfo ci) {
        this.fov = new SimpleOption<>("options.fov", SimpleOption.emptyTooltip(), (optionText, value) -> switch (value) {
            case 70 -> GameOptions.getGenericValueText(optionText, Text.translatable("options.fov.min"));
            case 110 -> GameOptions.getGenericValueText(optionText, Text.translatable("options.fov.max"));
            default -> GameOptions.getGenericValueText(optionText, value);
        }, new ValidatingIntSliderCallbacks(30, 180), Codec.DOUBLE.xmap((value) -> (int) (value * 40.0D + 70.0D), (value) -> ((double) value.intValue() - 70.0D) / 40.0D), 70, (value) -> MinecraftClient.getInstance().worldRenderer.scheduleTerrainUpdate());
    }
}
 