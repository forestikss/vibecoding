package ru.etc1337.api.settings.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import ru.etc1337.api.settings.Setting;
import ru.etc1337.api.settings.api.Parent;

@Setter
@Getter
public class BooleanSetting extends Setting {
    private boolean enabled = false;
    private int key = -1;

    public BooleanSetting(String name, Parent parent) {
        super(name, parent);
    }

    public BooleanSetting(String name, String description, Parent parent) {
        super(name, description, parent);
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    @Override
    public JsonElement save() {
        JsonObject object = new JsonObject();
        object.addProperty("enabled", enabled);
        object.addProperty("key", key);
        return object;
    }

    @Override
    public void load(JsonElement element) {
        JsonObject object = element.getAsJsonObject();
        if (object.has("enabled")) {
            setEnabled(object.get("enabled").getAsBoolean());
        }
        if (object.has("key")) {
            setKey(object.get("key").getAsInt());
        }
    }
}