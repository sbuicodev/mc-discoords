package net.hawkelele.discoords.services;

import club.minnced.discord.webhook.send.WebhookMessage;
import okio.Buffer;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class WebhookMessagesTest {
    private static final String TEXT = "🧭 Base: `1, 2, 3`";

    @Test
    void postedAsThePlayer() {
        WebhookMessage message = WebhookMessages.of(TEXT, "Steve");

        assertEquals("Steve", message.getUsername());
        assertEquals("https://mc-heads.net/avatar/Steve", message.getAvatarUrl());
        assertEquals(TEXT, message.getContent());
    }

    @Test
    void mentionsAreTurnedOff() throws IOException {
        JSONObject body = body(WebhookMessages.of("@everyone", "Steve"));

        assertEquals(0, body.getJSONObject("allowed_mentions").getJSONArray("parse").length());
    }

    @Test
    void namesDiscordRejectsAreWrittenInTheText() {
        for (String player : new String[]{"discordfan", "Mr_CLYDE", "here", "Everyone"}) {
            WebhookMessage message = WebhookMessages.of(TEXT, player);

            assertNull(message.getUsername());
            assertEquals(MessageSignature.of(TEXT, player), message.getContent());
            assertEquals("https://mc-heads.net/avatar/" + player, message.getAvatarUrl());
        }
    }

    static JSONObject body(WebhookMessage message) throws IOException {
        Buffer buffer = new Buffer();
        message.getBody().writeTo(buffer);
        return new JSONObject(buffer.readUtf8());
    }
}
