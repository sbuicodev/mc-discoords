package net.hawkelele.discoords.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class ConfigProvider {
    public static final String CONFIG_FILE = "config/discoords.json";
    private final File file;

    public ConfigProvider() {
        this(new File(CONFIG_FILE));
    }

    public ConfigProvider(File file) {
        this.file = file;
    }

    public Config load() {
        if (file.exists()) {
            return readAndUpdateConfigFile();
        }

        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            return null;
        }

        try {
            if (!file.createNewFile()) {
                return readAndUpdateConfigFile();
            }
        } catch (IOException e) {
            return null;
        }

        if (!writeConfigToFile(new Config())) {
            return null;
        }

        return readConfigFromFile();
    }

    // Written next to the config first and then moved over it, so a crash never leaves it half written
    public boolean writeConfigToFile(Config config) {
        Path temporary = new File(file.getPath() + ".tmp").toPath();

        try (Writer writer = Files.newBufferedWriter(temporary)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(config, writer);
        } catch (IOException e) {
            return false;
        }

        try {
            try {
                Files.move(temporary, file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // The file is written back only when it misses options added since it was created
    private Config readAndUpdateConfigFile() {
        JsonObject json;

        try (FileReader reader = new FileReader(file)) {
            json = JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException | IllegalStateException e) {
            return null;
        }

        Gson gson = new Gson();
        Config config = gson.fromJson(json, Config.class);

        if (!json.keySet().containsAll(gson.toJsonTree(config).getAsJsonObject().keySet())) {
            writeConfigToFile(config);
        }

        return config;
    }

    private Config readConfigFromFile() {
        try (FileReader reader = new FileReader(file)) {
            return new Gson().fromJson(reader, Config.class);
        } catch (IOException e) {
            return null;
        }
    }
}
