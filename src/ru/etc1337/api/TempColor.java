package ru.etc1337.api;

import lombok.Getter;
import ru.etc1337.api.color.FixColor;

public class TempColor {
    @Getter
    private static Theme currentTheme = Theme.DARK;

    private static final FixColor DARK_BG     = new FixColor(22, 22, 26);
    private static final FixColor DARK_TEXT   = new FixColor(220, 220, 220);
    private static final FixColor DARK_ACCENT = new FixColor(80, 140, 255);

    private static final FixColor LIGHT_BG = new FixColor(221, 222, 215);
    private static final FixColor LIGHT_TEXT = new FixColor(0, 0, 0);
    private static final FixColor LIGHT_ACCENT = new FixColor(0, 100, 200);

    @Getter
    private static FixColor clientColor;
    @Getter
    private static FixColor fontColor;
    @Getter
    private static FixColor backgroundColor;

    static {
        updateColors();
    }

    /**
     * Переключает текущую тему между светлой и темной
     */
    public static void toggleTheme() {
        currentTheme = currentTheme == Theme.DARK ? Theme.LIGHT : Theme.DARK;
        updateColors();
    }

    /**
     * Устанавливает конкретную тему
     * @param theme тема для установки
     */
    public static void setTheme(Theme theme) {
        currentTheme = theme;
        updateColors();
    }

    /**
     * Обновляет все цвета в соответствии с текущей темой
     */
    public static void updateColors() {
        clientColor = currentTheme == Theme.DARK ? DARK_ACCENT : LIGHT_ACCENT;
        fontColor = currentTheme == Theme.DARK ? DARK_TEXT : LIGHT_TEXT;
        backgroundColor = currentTheme == Theme.DARK ? DARK_BG : LIGHT_BG;
    }

    /**
     * Устанавливает пользовательский акцентный цвет
     * @param color новый акцентный цвет
     */
    public static void setCustomAccentColor(FixColor color) {
        clientColor = color;
    }

    /**
     * Устанавливает пользовательский цвет фона
     * @param color новый цвет фона
     */
    public static void setCustomBackgroundColor(FixColor color) {
        backgroundColor = color;
    }

    /**
     * Устанавливает пользовательский цвет текста
     * @param color новый цвет текста
     */
    public static void setCustomTextColor(FixColor color) {
        fontColor = color;
    }

    /**
     * Сбрасывает все цвета к значениям по умолчанию для текущей темы
     */
    public static void resetToThemeDefaults() {
        updateColors();
    }

    public enum Theme {
        DARK, LIGHT
    }
}