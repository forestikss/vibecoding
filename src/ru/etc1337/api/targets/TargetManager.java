package ru.etc1337.api.targets;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.Entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

@Getter @Setter
public class TargetManager {

    private final ArrayList<String> targets = new ArrayList<>();

    public void addTarget(String target) {
        targets.add(target);
    }

    public void addTargets(String... args) {
        targets.addAll(Arrays.asList(args));
        
    }

    public void removeTarget(Entity target) {
        targets.remove(target.getName().getString());
        
    }

    public void removeTarget(String target) {
        targets.remove(target);
        
    }

    public boolean isTarget(Entity entity) {
        return targets.contains(entity.getName().getString());
    }

    public boolean isTarget(String entity) {
        return targets.contains(entity);
    }

    public JsonObject save() {
        JsonObject json = new JsonObject();
        try {
            for (String target : targets) {
                json.addProperty(target, "targets");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    public void load(JsonObject data) {
        if (data == null) return;

        targets.clear();
        try {
            for (Map.Entry<String, JsonElement> entry : data.entrySet()) {
                String key = entry.getKey();
                JsonElement value = entry.getValue();
                if (value.isJsonPrimitive() && value.getAsString().equals("targets")) {
                    targets.add(key);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    } // 1
}