package dev.realme.ash.impl.module.render;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.render.entity.RenderEntityInvisibleEvent;
import net.minecraft.entity.player.PlayerEntity;

public class TrueSightModule
extends ToggleModule {
    final Config<Boolean> onlyPlayersConfig = new BooleanConfig("OnlyPlayers", "If to only reveal invisible players", true);

    public TrueSightModule() {
        super("TrueSight", "Allows you to see invisible entities", ModuleCategory.RENDER);
    }

    @EventListener
    public void onRenderEntityInvisible(RenderEntityInvisibleEvent event) {
        if (event.getEntity().isInvisible() && (!this.onlyPlayersConfig.getValue() || event.getEntity() instanceof PlayerEntity)) {
            event.cancel();
        }
    }
}
