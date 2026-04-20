package ru.etc1337.api.render.fonts;

import ru.etc1337.api.render.fonts.impl.CFontRenderer;
import ru.etc1337.protection.interfaces.Include;

public class Fonts {
    public static CFontRenderer DREAMCORE_8, DREAMCORE_6, DREAMCORE_11, DREAMCORE_12, DREAMCORE_13, DREAMCORE_14, DREAMCORE_15, DREAMCORE_16, DREAMCORE_20, DREAMCORE_28, DREAMCORE_32;
    public static CFontRenderer SEMIBOLD_10, SEMIBOLD_11, SEMIBOLD_12, SEMIBOLD_13, SEMIBOLD_14, SEMIBOLD_15, SEMIBOLD_16, SEMIBOLD_18, SEMIBOLD_20, SEMIBOLD_28;
    public static CFontRenderer MEDIUM_11, MEDIUM_12, MEDIUM_13, MEDIUM_14, MEDIUM_15, MEDIUM_16, MEDIUM_18, MEDIUM_20, MEDIUM_28;

    
    @Include
    public void start() {
        SEMIBOLD_10 = createFont("semibold", 10);
        SEMIBOLD_11 = createFont("semibold", 11);
        SEMIBOLD_12 = createFont("semibold", 12);
        SEMIBOLD_13 = createFont("semibold", 13);
        SEMIBOLD_14 = createFont("semibold", 14);
        SEMIBOLD_15 = createFont("semibold", 15);
        SEMIBOLD_16 = createFont("semibold", 16);
        SEMIBOLD_18 = createFont("semibold", 18);
        SEMIBOLD_20 = createFont("semibold", 20);
        SEMIBOLD_28 = createFont("semibold", 28);

        MEDIUM_11 = createFont("medium", 11);
        MEDIUM_12 = createFont("medium", 12);
        MEDIUM_13 = createFont("medium", 13);
        MEDIUM_14 = createFont("medium", 14);
        MEDIUM_15 = createFont("medium", 15);
        MEDIUM_16 = createFont("medium", 16);
        MEDIUM_18 = createFont("medium", 18);
        MEDIUM_20 = createFont("medium", 20);
        MEDIUM_28 = createFont("medium", 28);

        DREAMCORE_6 = createFont("dreamcore", 6);
        DREAMCORE_8 = createFont("dreamcore", 8);
        DREAMCORE_11 = createFont("dreamcore", 11);
        DREAMCORE_12 = createFont("dreamcore", 12);
        DREAMCORE_13 = createFont("dreamcore", 13);
        DREAMCORE_14 = createFont("dreamcore", 14);
        DREAMCORE_15 = createFont("dreamcore", 15);
        DREAMCORE_16 = createFont("dreamcore", 16);
        DREAMCORE_20 = createFont("dreamcore", 20);
        DREAMCORE_28 = createFont("dreamcore", 28);
        DREAMCORE_32 = createFont("dreamcore", 32);
    }

    private CFontRenderer createFont(String name, float size) {
        return new CFontRenderer(name, size / 2F);
    }
}