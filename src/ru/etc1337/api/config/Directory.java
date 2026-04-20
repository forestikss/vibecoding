package ru.etc1337.api.config;

import ru.etc1337.Client;
import ru.etc1337.api.interfaces.QuickImports;

import java.io.File;

public class Directory implements QuickImports {
    public static final File DIRECTORY = new File(mc.gameDir, Client.clientName.toLowerCase());

    public Directory() {
        if (!DIRECTORY.exists() && !DIRECTORY.mkdir()) {
            throw new IllegalStateException("Failed to create client directory");
        }
    }
}
