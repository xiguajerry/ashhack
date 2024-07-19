package dev.realme.ash.impl.module.render;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.StringConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.text.TextVisitEvent;

public class NameProtectModule
extends ToggleModule {
    final Config<String> placeholderConfig = new StringConfig("Placeholder", "The placeholder name for the player", "Player");

    public NameProtectModule() {
        super("NameProtect", "Hides the player name in chat and tablist", ModuleCategory.RENDER);
    }

    @EventListener
    public void onTextVisit(TextVisitEvent event) {
        if (NameProtectModule.mc.player == null) {
            return;
        }
        String username = mc.getSession().getUsername();
        String text = event.getText();
        if (text.contains(username)) {
            event.cancel();
            event.setText(text.replace(username, this.placeholderConfig.getValue()));
        }
    }
}
