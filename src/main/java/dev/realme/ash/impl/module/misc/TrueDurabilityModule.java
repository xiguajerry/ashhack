package dev.realme.ash.impl.module.misc;

import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.item.DurabilityEvent;

public class TrueDurabilityModule
extends ToggleModule {
    public TrueDurabilityModule() {
        super("TrueDurability", "Displays the true durability of unbreakable items", ModuleCategory.MISCELLANEOUS);
    }

    @EventListener
    public void onDurability(DurabilityEvent event) {
        int dura = event.getItemDamage();
        if (event.getDamage() < 0) {
            dura = event.getDamage();
        }
        event.cancel();
        event.setDamage(dura);
    }
}
