package dev.realme.ash.impl.module.misc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.realme.ash.Ash;
import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.ItemListConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.EventStage;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.file.ConfigFile;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.TickEvent;
import dev.realme.ash.util.math.timer.CacheTimer;
import dev.realme.ash.util.math.timer.Timer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Iterator;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

public class InvCleanerModule
        extends ToggleModule {
    Config<List<Item>> blacklistConfig = new ItemListConfig("Blacklist", "The items to throw");
    Config<Float> delayConfig = new NumberConfig<Float>("Delay", "The delay between removing items from the inventory", 0.05f, 0.0f, 1.0f);
    Config<Boolean> hotbarConfig = new BooleanConfig("Hotbar", "Cleans the hotbar inventory slots", true);
    private final Timer invCleanTimer = new CacheTimer();

    public InvCleanerModule() {
        super("InvCleaner", "Automatically cleans the player inventory", ModuleCategory.MISCELLANEOUS);
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (event.getStage() != EventStage.PRE) {
            return;
        }
        block0:
        for (Item item : this.blacklistConfig.getValue()) {
            for (int i = 35; i >= (this.hotbarConfig.getValue() ? 0 : 9); --i) {
                ItemStack stack = InvCleanerModule.mc.player.getInventory().getStack(i);
                if (stack.isEmpty() || stack.getItem() != item || !this.invCleanTimer.passed(this.delayConfig.getValue().floatValue() * 1000.0f))
                    continue;
                InvCleanerModule.mc.interactionManager.clickSlot(InvCleanerModule.mc.player.currentScreenHandler.syncId, i, 0, SlotActionType.PICKUP, InvCleanerModule.mc.player);
                InvCleanerModule.mc.interactionManager.clickSlot(InvCleanerModule.mc.player.currentScreenHandler.syncId, -999, 0, SlotActionType.PICKUP, InvCleanerModule.mc.player);
                this.invCleanTimer.reset();
                continue block0;
            }
        }
    }

    public ConfigFile getBlacklistFile(Path clientDir) {
        return new InvCleanerFile(clientDir);
    }

    public class InvCleanerFile
            extends ConfigFile {
        public InvCleanerFile(Path clientDir) {
            super(clientDir, "inv-cleaner");
        }

        @Override
        public void save() {
            try {
                Path filepath = this.getFilepath();
                if (!Files.exists(filepath)) {
                    Files.createFile(filepath);
                }
                JsonObject json = new JsonObject();
                JsonArray itemArray = new JsonArray();
                for (Item item : InvCleanerModule.this.blacklistConfig.getValue()) {
                    itemArray.add(item.getTranslationKey());
                }
                json.add("items", itemArray);
                this.write(filepath, this.serialize(json));
            } catch (IOException e) {
                Ash.error("Could not save file for inv cleaner!");
                e.printStackTrace();
            }
        }

        @Override
        public void load() {
            try {
                String content;
                JsonObject object;
                Path filepath = this.getFilepath();
                if (Files.exists(filepath) && (object = this.parseObject(content = this.read(filepath))) != null && object.has("items")) {
                    JsonArray jsonArray = object.getAsJsonArray("items");
                    Iterator iterator = jsonArray.iterator();
                    while (iterator.hasNext()) {
                        JsonElement jsonElement = (JsonElement) iterator.next();
                    }
                }
            } catch (IOException e) {
                Ash.error("Could not read file for inv cleaner!");
                e.printStackTrace();
            }
        }
    }
}
