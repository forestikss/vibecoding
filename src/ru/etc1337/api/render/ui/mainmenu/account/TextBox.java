package ru.etc1337.api.render.ui.mainmenu.account;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Data;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.math.vector.Vector2f;
import org.lwjgl.glfw.GLFW;
import ru.etc1337.api.color.FixColor;
import ru.etc1337.api.game.Maths;
import ru.etc1337.api.interfaces.QuickImports;
import ru.etc1337.api.render.Hover;
import ru.etc1337.api.render.Render;
import ru.etc1337.api.render.fonts.impl.CFontRenderer;

import java.awt.*; // Explicitly import AWT Color to avoid conflict with net.minecraft.util.math.vector.Vector3f in some contexts

@Data
public class TextBox implements QuickImports {
    private String text = "";
    public Vector2f position;
    private boolean selected;
    private int cursor;
    private double animatedCursorPosition;
    private CFontRenderer font;
    private int color;
    private TextAlign textAlign;
    private float renderPosX; // Renamed posX to renderPosX for clarity
    private String emptyText;
    private float width;
    private boolean hideCharacters;
    private boolean onlyNumbers;

    public TextBox(final Vector2f position, final CFontRenderer font, final float fontSize, final int color, final TextAlign textAlign, final String emptyText, final float width, final boolean hideCharacters, final boolean onlyNumbers) {
        this.position = position;
        this.font = font;
        this.color = color;
        this.textAlign = textAlign;
        this.emptyText = emptyText;
        this.width = width;
        this.hideCharacters = hideCharacters;
        this.onlyNumbers = onlyNumbers;
        this.cursor = 0; // Initialize cursor
        this.animatedCursorPosition = 0; // Initialize animatedCursorPosition
    }

    public static final String NUMERIC_CHARS = "0123456789"; // Renamed NUMBERS to NUMERIC_CHARS
    public static final String ALLOWED_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
            + "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя"
            + "ʙғɢʜɪʟɴǫʀsxʏ"
            + "0123456789"
            + "!?@#$%^&*()-_=+[]{}|\\;:'\"<>,./`~"
            + "©™® "; // Renamed CHARS to ALLOWED_CHARS for better semantics

    public void draw(MatrixStack matrix) {
        // Ensure cursor stays within valid bounds
        cursor = Math.min(Math.max(cursor, 0), this.text.length());

        mc.keyboardListener.enableRepeatEvents(true); // Enable key repeat for continuous input

        String displayText = this.text;
        if (this.hideCharacters && !this.isEmpty()) {
            displayText = "*".repeat(this.text.length());
        }

        switch (this.textAlign) {
            case CENTER -> {
                float fontwidth = (this.font.width(this.isEmpty() ? this.emptyText : displayText) / 2F);
                renderPosX = Maths.lerp(renderPosX, position.x - fontwidth, 0.5F);
            }
            case LEFT -> renderPosX = position.x;
        }

        if (this.isEmpty()) {
            this.font.draw(matrix, this.emptyText, renderPosX, position.y,
                    new Color(color).darker().darker().getRGB()); // Dim empty text color for distinction
        } else {
            this.font.draw(matrix, displayText, renderPosX, position.y, this.color);
        }

        // Calculate cursor offset for drawing
        final String textBeforeCursor = displayText.substring(0, cursor);
        float cursorOffset = this.font.width(textBeforeCursor);

        animatedCursorPosition = Maths.lerp(animatedCursorPosition, cursorOffset, 0.1D).doubleValue();

        if (this.selected) {
            Render.drawRect(matrix, (float) (renderPosX + animatedCursorPosition), position.y, 0.5F, font.height(), new FixColor(color));
        }
    }

    public void mouse(double mouseX, double mouseY, int button) {
        // Calculate the hover area more accurately based on alignment
        float hoverX = (textAlign == TextAlign.CENTER) ? position.x - width / 2f : position.x;
        this.selected = Hlp.isLClick(button) && Hover.isHovered(hoverX, position.y, width, font.height(), mouseX, mouseY);

/*        // If selected, set cursor to the closest character position
        if (this.selected) {
            float clickXRelativeToText = (float) (mouseX - renderPosX);
            this.cursor = this.font.width(text, clickXRelativeToText);
        }*/

    }

    public void keyPressed(int keyCode) {
        if (!this.selected) {
            return;
        }

        cursor = Math.min(Math.max(cursor, 0), this.text.length()); // Ensure cursor is always valid

        boolean isControlDown = InputMappings.isKeyDown(mc.getMainWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) ||
                InputMappings.isKeyDown(mc.getMainWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_CONTROL);

        if (isControlDown && keyCode == GLFW.GLFW_KEY_V) { // Paste
            String clipboard = mc.keyboardListener.getClipboardString();
            if (onlyNumbers) {
                clipboard = clipboard.replaceAll("\\D", ""); // Remove non-digits if only numbers are allowed
            }
            addText(clipboard, cursor);
            cursor += clipboard.length();
        } else if (keyCode == GLFW.GLFW_KEY_DELETE) { // Delete key
            if (cursor < this.text.length()) { // Only delete if cursor is not at the end
                removeText(cursor + 1); // Delete character to the right of cursor
            }
        } else if (keyCode == GLFW.GLFW_KEY_BACKSPACE) { // Backspace
            if (cursor > 0) {
                removeText(cursor); // Delete character to the left of cursor
                cursor--;

                if (isControlDown) { // Delete whole word
                    while (cursor > 0 && !Character.isWhitespace(this.text.charAt(cursor - 1))) {
                        removeText(cursor);
                        cursor--;
                    }
                }
            }
        } else if (keyCode == GLFW.GLFW_KEY_RIGHT) { // Move cursor right
            if (isControlDown) { // Move to next word boundary
                int oldCursor = cursor;
                while (cursor < this.text.length() && !Character.isWhitespace(this.text.charAt(cursor))) {
                    cursor++;
                }
                while (cursor < this.text.length() && Character.isWhitespace(this.text.charAt(cursor))) {
                    cursor++;
                }
                if (cursor == oldCursor && cursor < this.text.length()) cursor++; // Move at least one char if no word boundary
            } else {
                cursor++;
            }
        } else if (keyCode == GLFW.GLFW_KEY_LEFT) { // Move cursor left
            if (isControlDown) { // Move to previous word boundary
                int oldCursor = cursor;
                while (cursor > 0 && Character.isWhitespace(this.text.charAt(cursor - 1))) {
                    cursor--;
                }
                while (cursor > 0 && !Character.isWhitespace(this.text.charAt(cursor - 1))) {
                    cursor--;
                }
                if (cursor == oldCursor && cursor > 0) cursor--; // Move at least one char if no word boundary
            } else {
                cursor--;
            }
        } else if (keyCode == GLFW.GLFW_KEY_END) { // Move cursor to end of text
            cursor = this.text.length();
        } else if (keyCode == GLFW.GLFW_KEY_HOME) { // Move cursor to beginning of text
            cursor = 0;
        }

        cursor = Math.min(Math.max(cursor, 0), this.text.length()); // Re-clamp cursor after operations
    }

    public void charTyped(char codePoint) {
        if (!this.selected) {
            return;
        }
        cursor = Math.min(Math.max(cursor, 0), this.text.length()); // Ensure cursor is always valid

        boolean allowed = false;
        if (onlyNumbers) {
            allowed = NUMERIC_CHARS.indexOf(codePoint) != -1;
        } else {
            allowed = ALLOWED_CHARS.indexOf(codePoint) != -1;
        }

        if (allowed) {
            addText(Character.toString(codePoint), cursor);
            cursor++;
        }

        cursor = Math.min(Math.max(cursor, 0), this.text.length()); // Re-clamp cursor after operations
    }

    /**
     * Adds text at a specific position in the current text.
     * @param textToAdd The string to add.
     * @param position The index at which to insert the text.
     */
    private void addText(final String textToAdd, final int position) {
        // Prevent text from exceeding the specified width
        if (font.width(this.text + textToAdd) > width && width != 0) { // Check width only if it's set
            return;
        }

        final StringBuilder newText = new StringBuilder(this.text);
        newText.insert(position, textToAdd);
        this.text = newText.toString();
    }

    /**
     * Removes a character at a specific position.
     * @param position The 1-based index of the character to remove.
     */
    private void removeText(final int position) {
        if (position <= 0 || position > this.text.length()) {
            return; // Invalid position
        }
        this.text = new StringBuilder(this.text).deleteCharAt(position - 1).toString();
    }

    public boolean isEmpty() {
        return this.text.isEmpty();
    }

    public enum TextAlign {
        LEFT,
        CENTER
    }
}