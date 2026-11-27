package net.hawkelele.discoords.services;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WebhookUrlTest {
    private static final String URL = "https://discord.com/api/webhooks/123/secret-token";

    @Test
    void discordWebhookUrlsAreValid() {
        assertTrue(WebhookUrl.isValid(URL));
        assertTrue(WebhookUrl.isValid(URL + "?thread_id=456"));
        assertTrue(WebhookUrl.isValid(URL + "?wait=true"));
    }

    @Test
    void otherUrlsAreNotValid() {
        assertFalse(WebhookUrl.isValid("https://example.com/hook"));
        assertFalse(WebhookUrl.isValid("not a url"));
    }

    @Test
    void queryIsDropped() {
        assertEquals(URL, WebhookUrl.withoutQuery(URL + "?thread_id=456&wait=true"));
        assertEquals(URL, WebhookUrl.withoutQuery(URL));
    }

    @Test
    void threadIdIsReadFromTheQuery() {
        assertEquals(456, WebhookUrl.threadId(URL + "?wait=true&thread_id=456"));
        assertEquals(0, WebhookUrl.threadId(URL));
        assertEquals(0, WebhookUrl.threadId(URL + "?thread_id=abc"));
    }
}
