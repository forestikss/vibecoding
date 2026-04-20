package ru.etc1337.api.game;

import net.minecraft.client.util.InputMappings;
import org.lwjgl.glfw.GLFW;

public class StringHelper {
    public static String getKeyString(int key) {
        String out = InputMappings.getInputByCode(key, GLFW.GLFW_KEY_UNKNOWN).getTranslationKey().replace("key.keyboard.", "").toUpperCase();
        if (out.length() > 4) {
            out = out.substring(0, 3);
        }
        if (key == -1) {
            return "None";
        } else {
            return out;
        }
    }

}
