package net.hawkelele.discoords.services;

import net.hawkelele.discoords.config.Config;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PostingModeTest {
    @Test
    void botWithoutWebhook() {
        Config config = new Config();
        assertEquals(PostingMode.BOT, PostingMode.of(config));

        config.webhook = "  ";
        assertEquals(PostingMode.BOT, PostingMode.of(config));
    }

    @Test
    void webhookWhenSet() {
        Config config = new Config();
        config.webhook = "  https://discord.com/api/webhooks/123/secret-token?thread_id=456  ";

        assertEquals(PostingMode.WEBHOOK, PostingMode.of(config));
    }

    @Test
    void botInsteadOfAnInvalidWebhook() {
        Config config = new Config();
        config.webhook = "https://example.com/hook";

        assertEquals(PostingMode.BOT_INSTEAD_OF_WEBHOOK, PostingMode.of(config));
    }
}
