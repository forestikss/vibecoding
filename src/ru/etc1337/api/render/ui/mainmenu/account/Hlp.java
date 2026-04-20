package ru.etc1337.api.render.ui.mainmenu.account;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Hlp {
    public boolean isLClick(int button) {
        return button == 0;
    }

    public boolean isRClick(int button) {
        return button == 1;
    }

    public boolean isMClick(int button) {
        return button == 2;
    }
}
