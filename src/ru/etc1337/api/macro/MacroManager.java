package ru.etc1337.api.macro;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.ArrayList;

@Getter
public class MacroManager {

    public final ArrayList<Macro> macros = new ArrayList<>();

    public Macro get(String name) {
        return macros.stream().filter(macro -> macro.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public Macro get(int key) {
        return macros.stream().filter(macro -> macro.getKey() == key).findFirst().orElse(null);
    }
    public JsonObject save() {
        JsonObject object = new JsonObject();
        JsonArray array = new JsonArray();

        for (Macro macro : macros) {
            JsonObject dataObject = new JsonObject();
            dataObject.addProperty("name", macro.getName());
            dataObject.addProperty("keyName", macro.getKey());
            dataObject.addProperty("message", macro.getMessage());
            array.add(dataObject);
        }

        object.add("Macros", array);
        return object;
    }
    public void load(JsonObject object) {
        if (object == null) return;
        macros.clear();
        JsonArray jsonArray = object.getAsJsonArray("Macros");

        for (JsonElement data : jsonArray) {
            JsonObject dataObject = data.getAsJsonObject();

            String name = dataObject.get("name").getAsString();
            int keyName = dataObject.get("keyName").getAsInt();
            String message = dataObject.get("message").getAsString();

            macros.add(new Macro(name, keyName, message));
        }
    }

}