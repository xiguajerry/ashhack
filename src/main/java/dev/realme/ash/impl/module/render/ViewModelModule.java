package dev.realme.ash.impl.module.render;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.render.item.RenderFirstPersonEvent;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;

public class ViewModelModule
extends ToggleModule {
    Config<Float> positionXConfig = new NumberConfig<Float>("X", "Translation in x-direction", -3.0f, 0.0f, 3.0f);
    Config<Float> positionYConfig = new NumberConfig<Float>("Y", "Translation in y-direction", -3.0f, 0.0f, 3.0f);
    Config<Float> positionZConfig = new NumberConfig<Float>("Z", "Translation in z-direction", -3.0f, 0.0f, 3.0f);
    Config<Float> scaleXConfig = new NumberConfig<Float>("ScaleX", "Scaling in x-direction", 0.1f, 1.0f, 2.0f);
    Config<Float> scaleYConfig = new NumberConfig<Float>("ScaleY", "Scaling in y-direction", 0.1f, 1.0f, 2.0f);
    Config<Float> scaleZConfig = new NumberConfig<Float>("ScaleZ", "Scaling in z-direction", 0.1f, 1.0f, 2.0f);
    Config<Float> rotateXConfig = new NumberConfig<Float>("RotateX", "Rotation in x-direction", -180.0f, 0.0f, 180.0f);
    Config<Float> rotateYConfig = new NumberConfig<Float>("RotateY", "Rotation in y-direction", -180.0f, 0.0f, 180.0f);
    Config<Float> rotateZConfig = new NumberConfig<Float>("RotateZ", "Rotation in z-direction", -180.0f, 0.0f, 180.0f);

    public ViewModelModule() {
        super("ViewModel", "Changes the first-person viewmodel", ModuleCategory.RENDER);
    }

    @EventListener
    public void onRenderFirstPerson(RenderFirstPersonEvent event) {
        event.matrices.scale(this.scaleXConfig.getValue().floatValue(), this.scaleYConfig.getValue().floatValue(), this.scaleZConfig.getValue().floatValue());
        event.matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(this.rotateXConfig.getValue().floatValue()));
        if (event.hand == Hand.MAIN_HAND) {
            event.matrices.translate(this.positionXConfig.getValue().floatValue(), this.positionYConfig.getValue().floatValue(), this.positionZConfig.getValue().floatValue());
            event.matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(this.rotateYConfig.getValue().floatValue()));
            event.matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(this.rotateZConfig.getValue().floatValue()));
        } else {
            event.matrices.translate(-this.positionXConfig.getValue().floatValue(), this.positionYConfig.getValue().floatValue(), this.positionZConfig.getValue().floatValue());
            event.matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-this.rotateYConfig.getValue().floatValue()));
            event.matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-this.rotateZConfig.getValue().floatValue()));
        }
    }
}
