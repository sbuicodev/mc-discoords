package net.hawkelele.discoords.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;

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

    public boolean writeConfigToFile(Config config) {
        try (FileWriter writer = new FileWriter(file)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(config, writer);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // Writing the file back adds the options added since it was created
    private Config readAndUpdateConfigFile() {
        Config config = readConfigFromFile();

        if (config != null) {
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
