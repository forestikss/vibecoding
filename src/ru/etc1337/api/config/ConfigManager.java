package ru.etc1337.api.config;

import lombok.extern.slf4j.Slf4j;
import ru.etc1337.protection.interfaces.Include;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConfigManager {
    private static final String AUTOSAVE_CONFIG_NAME = "autosave";

    public ConfigManager start() {
        init();
        return this;
    }
    @Include
    public void init() {
        if (getAutoSaveConfig() == null) {
            Config config = new Config(AUTOSAVE_CONFIG_NAME);
            config.save();
        }
    }
    public List<String> getConfigs() {
        return streamConfigFiles()
                .map(file -> file.getName().replace(".json", ""))
                .collect(Collectors.toList());
    }

    public List<Config> getAllConfigurations() {
        return streamConfigFiles()
                .map(file -> new Config(file.getName().replace(".json", "")))
                .collect(Collectors.toList());
    }

    public boolean deleteConfig(Config config) {
        if (config == null) {
            Config.log.warn("Attempt to delete null config");
            return false;
        }

        try {
            Path path = config.getFile().toPath();
            if (Files.exists(path)) {
                Files.delete(path);
                return true;
            }
            return false;
        } catch (IOException e) {
            Config.log.error("Failed to delete config {}", config.getName(), e);
            return false;
        }
    }

    public Config getConfig(String name) {
        Objects.requireNonNull(name, "Config name cannot be null");
        return getAllConfigurations().stream()
                .filter(config -> config.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public Config getAutoSaveConfig() {
        return getConfig(AUTOSAVE_CONFIG_NAME);
    }

    private Stream<File> streamConfigFiles() {
        File[] files = Config.DIRECTORY.listFiles();
        if (files == null) {
            return Stream.empty();
        }
        return Arrays.stream(files)
                .filter(file -> file.isFile() && file.getName().endsWith(".json"));
    }
}