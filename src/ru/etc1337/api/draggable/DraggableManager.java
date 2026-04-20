package ru.etc1337.api.draggable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.screen.ChatScreen;
import ru.etc1337.Client;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.handler.EventListener;
import ru.etc1337.api.events.impl.render.EventRender2D;
import ru.etc1337.api.interfaces.QuickImports;
import ru.etc1337.api.render.Hover;
import ru.etc1337.api.render.fonts.Fonts;
import ru.etc1337.client.modules.impl.render.ui.api.Header;

import java.util.HashMap;

public class DraggableManager implements EventListener, QuickImports {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting()
            .excludeFieldsWithoutExposeAnnotation().create();
    public final HashMap<String, Draggable> draggables = new HashMap<>();

    public DraggableManager() {
        Client.getEventManager().register(this);
    }

    public JsonObject save() {
        try {
            JsonArray jsonArray = GSON.toJsonTree(draggables.values()).getAsJsonArray();
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("draggables", jsonArray);
            return jsonObject;
        } catch (Exception exception) {
            exception.printStackTrace();
            System.out.println("Oops, looks like Draggable couldn't save.");
            return null;
        }
    }

    public void load(JsonObject jsonObject) {
        if (jsonObject == null) return;
        try {
            JsonArray jsonArray = jsonObject.getAsJsonArray("draggables");
            Draggable[] draggings = GSON.fromJson(jsonArray, Draggable[].class);

            for (Draggable dragging : draggings) {
                if (dragging == null) return;
                Draggable currentDrag = draggables.get(dragging.getName());
                if (currentDrag == null) continue;
                currentDrag.setX(dragging.getX());
                currentDrag.setY(dragging.getY());
                draggables.put(dragging.getName(), currentDrag);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            System.out.println("Oops, looks like Draggable couldn't load.");
        }
    }

    public void resetAllPositions() {
        for (Draggable dragging : draggables.values()) {
            dragging.resetPosition();
        }
    }

    @Override
    public void onEvent(Event event) {
        if (mc.currentScreen instanceof ChatScreen && event instanceof EventRender2D eventRender2D) {
            float textWidth = Fonts.MEDIUM_12.width("Reset Draggables");
            float totalWidth = textWidth + 23.5f;
            float centerX = (window.getScaledWidth() - totalWidth) / 2;
            float centerY = 6;

            // todo: сделать
            Header.drawModernHeader(eventRender2D.getMatrixStack(), null, centerX, centerY, "n", "Reset Draggables");
        }
    }

    public final void onClick(int mouseX, int mouseY, int button) {
        if (button != 0) return;
        float textWidth = Fonts.MEDIUM_12.width("Reset Draggables");
        float totalWidth = textWidth + 23.5f;
        float centerX = (window.getScaledWidth() - totalWidth) / 2;
        float centerY = 6;

        if (Hover.isHovered(centerX, centerY, totalWidth, 17, mouseX, mouseY)) {
            Client.getInstance().getDraggableManager().resetAllPositions();
        }
    }
}