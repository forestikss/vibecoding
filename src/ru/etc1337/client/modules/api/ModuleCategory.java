package ru.etc1337.client.modules.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ModuleCategory {
    COMBAT("Combat", "t"),
    MOVEMENT("Movement", "u"),
    RENDER("Render", "r"),
    PLAYER("Player", "b"),
    MISC("Misc", "w");

    final String displayName;
    final String icon;
}