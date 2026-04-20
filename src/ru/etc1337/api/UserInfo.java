package ru.etc1337.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserInfo {
    private final String username;
    private final String role;
    private final Integer uid;
    private final Boolean premium;
}