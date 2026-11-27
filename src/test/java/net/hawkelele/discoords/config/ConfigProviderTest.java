package net.hawkelele.discoords.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigProviderTest {
    @TempDir
    Path dir;

    @Test
    void createsDefaultFile() throws IOException {
        File file = dir.resolve("config/discoords.json").toFile();

        Config config = new ConfigProvider(file).load();

        assertNotNull(config);
        assertEquals(new Config().token, config.token);
        assertEquals("", config.webhook);
        assertTrue(file.exists());
        assertTrue(Files.readString(file.toPath()).contains("\"webhook\""));
    }

    @Test
    void readsExistingFile() throws IOException {
        File file = write("{\"token\": \"t\", \"guild\": \"g\", \"channel\": \"c\", \"webhook\": \"w\"}");

        Config config = new ConfigProvider(file).load();

        assertEquals("t", config.token);
        assertEquals("g", config.guild);
        assertEquals("c", config.channel);
        assertEquals("w", config.webhook);
    }

    @Test
    void addsMissingWebhookKey() throws IOException {
        File file = write("{\"token\": \"t\", \"guild\": \"g\", \"channel\": \"c\"}");

        Config config = new ConfigProvider(file).load();

        assertEquals("t", config.token);
        assertEquals("g", config.guild);
        assertEquals("c", config.channel);
        assertEquals("", config.webhook);
        assertTrue(Files.readString(file.toPath()).contains("\"webhook\""));
    }

    @Test
    void leavesCompleteFileUntouched() throws IOException {
        String json = "{\"token\":\"t\",\"guild\":\"g\",\"channel\":\"c\",\"webhook\":\"w\"}";
        File file = write(json);

        new ConfigProvider(file).load();

        assertEquals(json, Files.readString(file.toPath()));
    }

    private File write(String json) throws IOException {
        Path path = dir.resolve("discoords.json");
        Files.writeString(path, json);
        return path.toFile();
    }
}
