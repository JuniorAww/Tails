package me.junioraww.tails.utils;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import me.junioraww.tails.data.types.items.Attach;
import me.junioraww.tails.data.types.items.Graffiti;
import me.junioraww.tails.data.types.wallet.Item;
import org.bukkit.Bukkit;

import java.io.IOException;

public class ItemAdapter extends TypeAdapter<Item> {
    private final Gson gson = new Gson();

    @Override
    public void write(JsonWriter out, Item item) throws IOException {
        if (item == null) {
            out.nullValue();
            return;
        }

        JsonElement element = gson.toJsonTree(item);
        JsonObject obj = element.getAsJsonObject();

        Bukkit.getLogger().info("obj has type? test " + obj.has("type"));
        if (!obj.has("type")) {
            obj.addProperty("type", item.getType().asNum());
        }

        // Записываем обратно
        gson.toJson(obj, out);
    }

    @Override
    public Item read(JsonReader in) {
        JsonElement element = JsonParser.parseReader(in);
        if (!element.isJsonObject()) {
            return null;
        }

        JsonObject obj = element.getAsJsonObject();

        int type = obj.get("type").getAsInt();

        return switch (type) {
            case 1 -> gson.fromJson(obj, Attach.class);
            case 0 -> gson.fromJson(obj, Graffiti.class);
            default -> gson.fromJson(obj, Item.class);
        };
    }
}
