package dev.realme.ash.impl.module.render;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.gui.hud.PlayerListColumnsEvent;
import dev.realme.ash.impl.event.gui.hud.PlayerListEvent;
import dev.realme.ash.impl.event.gui.hud.PlayerListNameEvent;
import dev.realme.ash.init.Managers;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ExtraTabModule
extends ToggleModule {
    final Config<Integer> sizeConfig = new NumberConfig<>("Size", "The number of players to show", 80, 200, 1000);
    final Config<Integer> columnsConfig = new NumberConfig<>("Columns", "The number columns to show.", 1, 20, 100);
    final Config<Boolean> selfConfig = new BooleanConfig("Self", "Highlights yourself in the tab list.", false);
    final Config<Boolean> friendsConfig = new BooleanConfig("Friends", "Highlights friends in the tab list.", true);

    public ExtraTabModule() {
        super("ExtraTab", "Expands the tab list size to allow for more players", ModuleCategory.RENDER);
    }

    @EventListener
    public void onPlayerListName(PlayerListNameEvent event) {
        if (this.selfConfig.getValue() && event.getPlayerName().getString().equals(mc.getGameProfile().getName())) {
            event.cancel();
            event.setPlayerName(Text.of("§s" + event.getPlayerName().getString()));
        } else if (this.friendsConfig.getValue()) {
            for (String s : Managers.SOCIAL.getFriends()) {
                if (!event.getPlayerName().getString().equals(s)) continue;
                event.cancel();
                event.setPlayerName(Text.of(Formatting.AQUA + event.getPlayerName().getString()));
                break;
            }
        }
    }

    @EventListener
    public void onPlayerList(PlayerListEvent event) {
        event.cancel();
        event.setSize(this.sizeConfig.getValue());
    }

    @EventListener
    public void onPlayerListColumns(PlayerListColumnsEvent event) {
        event.cancel();
        event.setTabHeight(this.columnsConfig.getValue());
    }
}
