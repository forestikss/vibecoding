package ru.etc1337.api.config;

import com.google.gson.*;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.MalformedJsonException;
import lombok.Getter;
import net.minecraft.util.text.TextFormatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.etc1337.Client;
import ru.etc1337.api.TempColor;
import ru.etc1337.api.game.Chat;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.impl.render.Interface;
import ru.etc1337.protection.interfaces.Include;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

@Getter
public class Config {
    public static File DIRECTORY = new File(Directory.DIRECTORY, "configs");
    public static Logger log = LogManager.getLogger();

    private final List<Module> modules;
    private final File file;
    private final String name;

    public Config(String name) {
        this.modules = Client.getInstance().getModuleManager().getModules();
        this.name = Objects.requireNonNull(name, "Config name cannot be null");
        DIRECTORY.mkdirs();
        this.file = new File(DIRECTORY, name + ".json");
    }

    public static JsonElement parseString(String json) throws JsonSyntaxException {
        return parseReader(new StringReader(json));
    }

    public static JsonElement parseReader(JsonReader reader) throws JsonIOException, JsonSyntaxException {
        boolean lenient = reader.isLenient();
        reader.setLenient(true);

        try {
            return Streams.parse(reader);
        } catch (StackOverflowError | OutOfMemoryError e) {
            throw new JsonParseException("Failed parsing JSON source: " + reader + " to Json", e);
        } finally {
            reader.setLenient(lenient);
        }
    }

    public static JsonElement parseReader(Reader reader) throws JsonIOException, JsonSyntaxException {
        try (JsonReader jsonReader = new JsonReader(reader)) {
            JsonElement element = parseReader(jsonReader);
            if (!element.isJsonNull() && jsonReader.peek() != JsonToken.END_DOCUMENT) {
                throw new JsonSyntaxException("Did not consume the entire document.");
            }
            return element;
        } catch (MalformedJsonException | NumberFormatException e) {
            throw new JsonSyntaxException("Malformed JSON", e);
        } catch (IOException e) {
            throw new JsonIOException("IO error while reading JSON", e);
        }
    }

    public void save() {
        try {
            if (!file.exists() && !file.createNewFile()) {
                throw new IOException("Failed to create config file: " + file.getAbsolutePath());
            }

            JsonObject json = new JsonObject();
            json.add("Modules", getModulesJsonArray());
            json.add("Draggables", Client.getInstance().getDraggableManager().save());
            json.add("Friends", Client.getInstance().getFriendManager().save());
            json.add("Targets", Client.getInstance().getTargetManager().save());
            json.add("Macros", Client.getInstance().getMacroManager().save());
            json.add("Staffs", Client.getInstance().getStaffManager().save());
            json.addProperty("Theme", TempColor.getCurrentTheme().name());

            Files.writeString(file.toPath(), Client.GSON.toJson(json));
        } catch (IOException e) {
            log.error("Failed to save config {}", name, e);
        }
    }

    @Include
    public void start() {
        if (!file.exists()) {
            return;
        }

        resetModules();

        try {
            String configContent = Files.readString(file.toPath());
            JsonObject jsonObject = parseString(configContent).getAsJsonObject();

            Client.getInstance().getDraggableManager().load(jsonObject.getAsJsonObject("Draggables"));
            Client.getInstance().getFriendManager().load(jsonObject.getAsJsonObject("Friends"));
            Client.getInstance().getTargetManager().load(jsonObject.getAsJsonObject("Targets"));
            Client.getInstance().getMacroManager().load(jsonObject.getAsJsonObject("Macros"));
            Client.getInstance().getStaffManager().load(jsonObject.getAsJsonObject("Staffs"));

            loadModules(jsonObject.getAsJsonArray("Modules"));

            if (jsonObject.has("Theme")) {
                try {
                    TempColor.setTheme(TempColor.Theme.valueOf(jsonObject.get("Theme").getAsString()));
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid theme value in config {}, defaulting to DARK", name);
                    TempColor.setTheme(TempColor.Theme.DARK);
                }
            } else {
                log.warn("No theme specified in config {}, defaulting to DARK", name);
                TempColor.setTheme(TempColor.Theme.DARK);
            }

            Chat.send("Config with name " + TextFormatting.AQUA + name + TextFormatting.RESET + " was loaded");
        } catch (IOException e) {
            log.error("Failed to read config file {}", file.getAbsolutePath(), e);
        } catch (JsonSyntaxException e) {
            log.error("Invalid JSON in config file {}", file.getAbsolutePath(), e);
        } catch (Exception e) {
            log.error("Unexpected error while loading config {}", name, e);
        }
    }

    private void loadModules(JsonArray modulesArray) {
        for (JsonElement moduleElement : modulesArray) {
            JsonObject moduleObject = moduleElement.getAsJsonObject();
            String moduleName = moduleObject.get("name").getAsString();
            Module module = Client.getInstance().getModuleManager().get(moduleName);

            if (module != null) {
                // Interface всегда включён, не трогаем его enabled
                if (!(module instanceof Interface)) {
                    module.setEnabled(moduleObject.get("enabled").getAsBoolean());
                }
                module.setKey(moduleObject.get("key").getAsInt());
                loadSettings(module, moduleObject.getAsJsonObject("settings"));
            } else {
                log.warn("Module {} not found while loading config", moduleName);
            }
        }
    }

    private void resetModules() {
        modules.forEach(module -> {
            // Interface не трогаем — он должен всегда быть включён
            if (module instanceof Interface) return;
            if (module.isEnabled()) {
                module.setEnabled(false);
            }
            module.setKey(-1);
        });
    }

    public boolean delete() {
        return file.exists() && file.delete();
    }

    private void loadSettings(Module module, JsonObject settingsObject) {
        if (module instanceof Interface interfaceModule) {
            if (settingsObject.has("UIElements")) {
                JsonObject uiElementsSettings = settingsObject.getAsJsonObject("UIElements");
                interfaceModule.getElements().forEach(element -> {
                    if (uiElementsSettings.has(element.getName())) {
                        JsonObject elementSettings = uiElementsSettings.getAsJsonObject(element.getName());
                        element.getSettings().forEach(setting -> {
                            if (elementSettings.has(setting.getName())) {
                                setting.load(elementSettings.get(setting.getName()));
                            }
                        });
                    }
                });
            }
        }

        module.getSettings().forEach(setting -> {
            if (settingsObject.has(setting.getName())) {
                setting.load(settingsObject.get(setting.getName()));
            }
        });
    }

    private JsonArray getModulesJsonArray() {
        JsonArray modulesJsonArray = new JsonArray();
        modules.forEach(module -> {
            JsonObject moduleObject = new JsonObject();
            moduleObject.addProperty("name", module.getName());
            moduleObject.addProperty("enabled", module.isEnabled());
            moduleObject.addProperty("key", module.getKey());
            moduleObject.add("settings", getSettingsJsonObject(module));
            modulesJsonArray.add(moduleObject);
        });
        return modulesJsonArray;
    }

    private JsonObject getSettingsJsonObject(Module module) {
        JsonObject settingsObject = new JsonObject();

        if (module instanceof Interface interfaceModule) {
            JsonObject uiElementsSettings = new JsonObject();
            interfaceModule.getElements().forEach(element -> {
                JsonObject elementSettings = new JsonObject();
                element.getSettings().forEach(setting -> {
                    elementSettings.add(setting.getName(), setting.save());
                });
                uiElementsSettings.add(element.getName(), elementSettings);
            });
            settingsObject.add("UIElements", uiElementsSettings);
        }

        module.getSettings().forEach(setting -> {
            settingsObject.add(setting.getName(), setting.save());
        });

        return settingsObject;
    }
}