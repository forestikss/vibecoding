package ru.etc1337.protection.checks;

import ru.etc1337.protection.interfaces.Include;
import ru.etc1337.protection.userapi.UserAPI;
import ru.kotopushka.compiler.sdk.annotations.Compile;
import ru.kotopushka.compiler.sdk.annotations.VMProtect;
import ru.kotopushka.compiler.sdk.classes.Profile;
import ru.kotopushka.compiler.sdk.enums.VMProtectType;

public class Checks {
    @Compile
    @VMProtect(type = VMProtectType.ULTRA)
    public static boolean check() {
        UserAPI.role = Profile.getRole();
        UserAPI.username = Profile.getUsername();
        UserAPI.uid = Profile.getUid();
        UserAPI.beta = true;
        return true;
    }
}
