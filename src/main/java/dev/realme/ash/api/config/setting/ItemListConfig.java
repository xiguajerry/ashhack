package dev.realme.ash.api.config.setting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.realme.ash.api.config.Config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class ItemListConfig extends Config<List<Item>> {
    public ItemListConfig(String name, String desc, Item... values) {
        super(name, desc, List.of(values));
    }

    public boolean contains(Object obj) {
        return obj instanceof Item && this.value.contains(obj);
    }

    public JsonObject toJson() {
        JsonObject jsonObj = super.toJson();
        JsonArray array = new JsonArray();

        for (Object o : this.getValue()) {
            Item item = (Item) o;
            Identifier id = Registries.ITEM.getId(item);
            array.add(id.toString());
        }

        jsonObj.add("value", array);
        return jsonObj;
    }

    public List<Item> fromJson(JsonObject jsonObj) {
        if (!jsonObj.has("value")) {
            return null;
        } else {
            JsonElement element = jsonObj.get("value");
            List<Item> temp = new ArrayList<>();

            for (JsonElement je : element.getAsJsonArray()) {
                String val = je.getAsString();
                Item item = Registries.ITEM.get(new Identifier(val));
                temp.add(item);
            }

            return temp;
        }
    }
}
