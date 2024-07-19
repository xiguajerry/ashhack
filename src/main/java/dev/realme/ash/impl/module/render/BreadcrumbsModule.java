package dev.realme.ash.impl.module.render;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.EventStage;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.network.UpdateWalkingEvent;
import dev.realme.ash.impl.event.render.RenderWorldEvent;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.util.math.Vec3d;

public class BreadcrumbsModule
extends ToggleModule {
    private final Map<Vec3d, Long> positions = new ConcurrentHashMap<>();
    final Config<Boolean> infiniteConfig = new BooleanConfig("Infinite", "Renders breadcrumbs for all positions since toggle", true);
    final Config<Float> maxTimeConfig = new NumberConfig<>("MaxPosition", "The maximum time for a given position", 1.0f, 2.0f, 20.0f);

    public BreadcrumbsModule() {
        super("Breadcrumbs", "Renders a line connecting all previous positions", ModuleCategory.RENDER);
    }

    @Override
    public void onDisable() {
        this.positions.clear();
    }

    @EventListener
    public void onPlayerUpdate(UpdateWalkingEvent event) {
        if (event.getStage() != EventStage.PRE) {
            return;
        }
        this.positions.put(new Vec3d(BreadcrumbsModule.mc.player.getX(), BreadcrumbsModule.mc.player.getBoundingBox().minY, BreadcrumbsModule.mc.player.getZ()), System.currentTimeMillis());
        if (!this.infiniteConfig.getValue()) {
            this.positions.forEach((p, t) -> {
                if ((float)(System.currentTimeMillis() - t) >= this.maxTimeConfig.getValue() * 1000.0f) {
                    this.positions.remove(p);
                }
            });
        }
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event) {
    }
}
