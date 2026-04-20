package de.maxhenkel.voicechat.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.text.*;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;

public class KeybindButton extends AbstractButton {

    private static final Minecraft mc = Minecraft.getInstance();

    protected KeyBinding keyMapping;
    @Nullable
    protected ITextComponent description;
    protected boolean listening;

    public KeybindButton(KeyBinding mapping, int x, int y, int width, int height, @Nullable ITextComponent description) {
        super(x, y, width, height, StringTextComponent.EMPTY);
        this.keyMapping = mapping;
        this.description = description;
        updateText();
    }

    public KeybindButton(KeyBinding mapping, int x, int y, int width, int height) {
        this(mapping, x, y, width, height, null);
    }

    protected void updateText() {
        IFormattableTextComponent text;
        text = new TranslationTextComponent("Забиндить в Click Gui");


        setMessage(text);
    }

    private static ITextComponent getText(KeyBinding keyMapping) {
        return keyMapping.getTranslatedKeyMessage();
    }

    public boolean isHovered() {
        return isHovered;
    }

    @Override
    public void onPress() {
      //  listening = true;
     //   updateText();
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
     /*   if (listening) {
            mc.gameSettings.setKeyBindingCode(keyMapping, InputMappings.Type.MOUSE.getOrMakeInput(button));
            listening = false;
            updateText();
            return true;
        }*/
        //mc.displayGuiScreen(new CSGui());
        //CSGui.instance.setCurrentScreen(Category.MISC.getScreen());
        return super.mouseClicked(x, y, button);

    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
   /*     if (listening) {
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                mc.gameSettings.setKeyBindingCode(keyMapping, InputMappings.UNKNOWN);
            } else {
                mc.gameSettings.setKeyBindingCode(keyMapping, InputMappings.getInputByCode(key, scanCode));
            }
            listening = false;
            updateText();
            return true;
        }*/

        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int key, int scanCode, int modifiers) {
     /*   if (listening && key == GLFW.GLFW_KEY_ESCAPE) {
            return true;
        }*/
        return super.keyReleased(key, scanCode, modifiers);
    }

    public boolean isListening() {
        return false;
    }
}
