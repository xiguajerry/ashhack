package dev.realme.ash.impl.module.render;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.render.CameraClipEvent;

public class ViewClipModule
extends ToggleModule {
    Config<Float> distanceConfig = new NumberConfig<Float>("Distance", "The third-person camera clip distance", Float.valueOf(1.0f), Float.valueOf(3.5f), Float.valueOf(20.0f));

    public ViewClipModule() {
        super("ViewClip", "Clips your third-person camera through blocks", ModuleCategory.RENDER);
    }

    @EventListener
    public void onCameraClip(CameraClipEvent event) {
        event.cancel();
        event.setDistance(this.distanceConfig.getValue().floatValue());
    }
}
