package net.hawkelele.discoords.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;

public class ConfigProvider {
    public static final String CONFIG_FILE = "config/discoords.json";
    private final File file = new File(CONFIG_FILE);

    public Config load() {
        if (file.exists()) {
            return readConfigFromFile();
        }

        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            return null;
        }

        try {
            if (!file.createNewFile()) {
                return readConfigFromFile();
            }
        } catch (IOException e) {
            return null;
        }

        if (!writeConfigToFile(new Config())) {
            return null;
        }

        return readConfigFromFile();
    }

    public boolean writeConfigToFile(Config config) {
        try (FileWriter writer = new FileWriter(file)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(config, writer);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private Config readConfigFromFile() {
        try (FileReader reader = new FileReader(file)) {
            return new Gson().fromJson(reader, Config.class);
        } catch (IOException e) {
            return null;
        }
    }
}
