package dev.realme.ash.api.config;

import com.google.gson.JsonObject;

public interface Serializable {
   JsonObject toJson();

   Object fromJson(JsonObject var1);
}
