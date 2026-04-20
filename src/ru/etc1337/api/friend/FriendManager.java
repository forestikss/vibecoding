package ru.etc1337.api.friend;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.Entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Getter @Setter
public class FriendManager {

    private final ArrayList<String> friends = new ArrayList<>();

    public void addFriend(String friend) {
        friends.add(friend);
        
    }

    public void addFriends(String... args) {
        friends.addAll(Arrays.asList(args));
        
    }

    public void removeFriend(Entity friend) {
        friends.remove(friend.getName().getString());
        
    }

    public void removeFriend(String friend) {
        friends.remove(friend);
        
    }

    public boolean isFriend(Entity entity) {
        return friends.contains(entity.getName().getString());
    }

    public boolean isFriend(String entity) {
        return friends.contains(entity);
    }

    public JsonObject save() {
        JsonObject json = new JsonObject();
        try {
            for (String friend : friends) {
                json.addProperty(friend, "friends");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    public void load(JsonObject data) {
        if (data == null) return;

        friends.clear();
        try {
            for (Map.Entry<String, JsonElement> entry : data.entrySet()) {
                String key = entry.getKey();
                JsonElement value = entry.getValue();
                if (value.isJsonPrimitive() && value.getAsString().equals("friends")) {
                    friends.add(key);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}