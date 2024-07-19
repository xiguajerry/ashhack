package dev.realme.ash.impl.module.render;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.render.CameraClipEvent;

public class ViewClipModule
extends ToggleModule {
    final Config<Float> distanceConfig = new NumberConfig<>("Distance", "The third-person camera clip distance", 1.0f, 3.5f, 20.0f);

    public ViewClipModule() {
        super("ViewClip", "Clips your third-person camera through blocks", ModuleCategory.RENDER);
    }

    @EventListener
    public void onCameraClip(CameraClipEvent event) {
        event.cancel();
        event.setDistance(this.distanceConfig.getValue());
    }
}
