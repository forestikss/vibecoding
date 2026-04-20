package ru.etc1337.api.macro;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter @AllArgsConstructor
public class Macro {
    private final String name;
    private int key;
    private final String message;
}
