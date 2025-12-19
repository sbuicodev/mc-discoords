package net.hawkelele.discoords.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.hawkelele.discoords.config.Config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigService {
    public static final String CONFIG_FILE = "config/discoords.json";
    private final File file = new File(CONFIG_FILE);

    public Config load() throws IOException {
        if (!file.exists()) {
            if (!file.getParentFile().mkdirs() || !file.createNewFile()) {
                throw new IOException("Failed to create config file");
            }
            save(new Config());
        }
        try (FileReader reader = new FileReader(file)) {
            return new Gson().fromJson(reader, Config.class);
        }
    }

    public void save(Config config) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(config, writer);
        }
    }
}
