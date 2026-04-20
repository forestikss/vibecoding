package ru.etc1337.protection.userapi;

import lombok.Getter;
import lombok.experimental.UtilityClass;

@UtilityClass @Getter
public class UserAPI {
    public String username;
    public Integer uid;
    public String role;
    public boolean beta;
}
