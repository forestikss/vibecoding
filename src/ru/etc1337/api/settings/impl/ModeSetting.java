package ru.etc1337.api.settings.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import ru.etc1337.api.settings.Setting;
import ru.etc1337.api.settings.api.Parent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModeSetting extends Setting {
    @Getter
    private final List<String> modes;
    @Getter
    private String currentMode;
    private final Map<String, Integer> modeKeys;

    public ModeSetting(String name, Parent parent, String... modes) {
        super(name, parent);
        this.modes = Arrays.asList(modes);
        this.currentMode = modes.length > 0 ? modes[0] : "";
        this.modeKeys = new HashMap<>();
        for (String mode : modes) {
            modeKeys.put(mode, -1);
        }
    }

    public int getIndex() {
        int index = 0;
        for (String val : modes) {
            if (val.equalsIgnoreCase(currentMode)) {
                return index;
            }
            index++;
        }
        return 0;
    }

    public void setCurrentMode(String mode) {
        if (modes.contains(mode)) {
            this.currentMode = mode;
        }
    }

    public void setModeKey(String mode, int keyCode) {
        if (modes.contains(mode)) {
            modeKeys.put(mode, keyCode);
        }
    }
    public String getModeString(int key) {
        for (Map.Entry<String, Integer> entry : modeKeys.entrySet()) {
            if (entry.getValue() == key) {
                return entry.getKey();
            }
        }
        return getName();
    }
    public int getModeKey(String mode) {
        return modeKeys.getOrDefault(mode, -1);
    }

    public void nextMode() {
        int index = modes.indexOf(currentMode);
        index = (index + 1) % modes.size();
        currentMode = modes.get(index);
    }

    public boolean is(String mode) {
        return currentMode.equalsIgnoreCase(mode);
    }

    @Override
    public JsonElement save() {
        JsonObject object = new JsonObject();
        object.addProperty("currentMode", currentMode);
        JsonObject keys = new JsonObject();
        for (Map.Entry<String, Integer> entry : modeKeys.entrySet()) {
            keys.addProperty(entry.getKey(), entry.getValue());
        }
        object.add("modeKeys", keys);
        return object;
    }

    @Override
    public void load(JsonElement element) {
        JsonObject object = element.getAsJsonObject();
        if (object.has("currentMode")) {
            setCurrentMode(object.get("currentMode").getAsString());
        }
        if (object.has("modeKeys")) {
            JsonObject keys = object.get("modeKeys").getAsJsonObject();
            for (String mode : modes) {
                if (keys.has(mode)) {
                    modeKeys.put(mode, keys.get(mode).getAsInt());
                }
            }
        }
    }
}