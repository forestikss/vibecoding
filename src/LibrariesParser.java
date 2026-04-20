
import ru.kotopushka.compiler.sdk.annotations.Compile;
import ru.kotopushka.compiler.sdk.annotations.VMProtect;
import ru.kotopushka.compiler.sdk.enums.VMProtectType;

import java.io.File;
import java.util.Objects;

public class LibrariesParser {
    @Compile
    @VMProtect(type = VMProtectType.ULTRA)
    public static void main(String[] args) {
        for (File file : Objects.requireNonNull(new File("C:\\Dreamcore\\client-1.16.5\\libraries\\").listFiles())) {
            System.out.printf(";%s", file.getAbsolutePath().replaceAll("\\\\", "/"));
        }
    }
}