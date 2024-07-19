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
    final Config<Float> positionXConfig = new NumberConfig<>("X", "Translation in x-direction", -3.0f, 0.0f, 3.0f);
    final Config<Float> positionYConfig = new NumberConfig<>("Y", "Translation in y-direction", -3.0f, 0.0f, 3.0f);
    final Config<Float> positionZConfig = new NumberConfig<>("Z", "Translation in z-direction", -3.0f, 0.0f, 3.0f);
    final Config<Float> scaleXConfig = new NumberConfig<>("ScaleX", "Scaling in x-direction", 0.1f, 1.0f, 2.0f);
    final Config<Float> scaleYConfig = new NumberConfig<>("ScaleY", "Scaling in y-direction", 0.1f, 1.0f, 2.0f);
    final Config<Float> scaleZConfig = new NumberConfig<>("ScaleZ", "Scaling in z-direction", 0.1f, 1.0f, 2.0f);
    final Config<Float> rotateXConfig = new NumberConfig<>("RotateX", "Rotation in x-direction", -180.0f, 0.0f, 180.0f);
    final Config<Float> rotateYConfig = new NumberConfig<>("RotateY", "Rotation in y-direction", -180.0f, 0.0f, 180.0f);
    final Config<Float> rotateZConfig = new NumberConfig<>("RotateZ", "Rotation in z-direction", -180.0f, 0.0f, 180.0f);

    public ViewModelModule() {
        super("ViewModel", "Changes the first-person viewmodel", ModuleCategory.RENDER);
    }

    @EventListener
    public void onRenderFirstPerson(RenderFirstPersonEvent event) {
        event.matrices.scale(this.scaleXConfig.getValue(), this.scaleYConfig.getValue(), this.scaleZConfig.getValue());
        event.matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(this.rotateXConfig.getValue()));
        if (event.hand == Hand.MAIN_HAND) {
            event.matrices.translate(this.positionXConfig.getValue(), this.positionYConfig.getValue(), this.positionZConfig.getValue());
            event.matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(this.rotateYConfig.getValue()));
            event.matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(this.rotateZConfig.getValue()));
        } else {
            event.matrices.translate(-this.positionXConfig.getValue(), this.positionYConfig.getValue(), this.positionZConfig.getValue());
            event.matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-this.rotateYConfig.getValue()));
            event.matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-this.rotateZConfig.getValue()));
        }
    }
}
