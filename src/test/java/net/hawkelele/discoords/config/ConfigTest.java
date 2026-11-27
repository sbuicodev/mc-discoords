package net.hawkelele.discoords.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigTest {
    @Test
    void noWebhookByDefault() {
        assertFalse(new Config().usesWebhook());
    }

    @Test
    void blankOrMissingWebhookIsNotSet() {
        Config config = new Config();

        config.webhook = "   ";
        assertFalse(config.usesWebhook());

        config.webhook = null;
        assertFalse(config.usesWebhook());
    }

    @Test
    void webhookIsSet() {
        Config config = new Config();
        config.webhook = "https://discord.com/api/webhooks/1/abc";

        assertTrue(config.usesWebhook());
    }
}
