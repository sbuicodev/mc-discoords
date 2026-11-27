package net.hawkelele.discoords.services;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Discord is never called: the HTTP client answers in its place
class DiscordServiceTest {
    private static final String URL = "https://discord.com/api/webhooks/123/secret-token";

    private final List<Request> requests = new ArrayList<>();
    private DiscordService discord;

    @AfterEach
    void close() {
        if (discord != null) {
            discord.close();
        }
    }

    @Test
    void postsAsThePlayer() throws IOException {
        discord = DiscordService.connectWebhook(URL, answering(204, ""));

        discord.sendMessage("hi @everyone", "Steve").join();

        assertEquals(1, requests.size());
        Request request = requests.get(0);
        assertEquals("POST", request.method());
        assertTrue(request.url().encodedPath().endsWith("/webhooks/123/secret-token"));

        Buffer buffer = new Buffer();
        request.body().writeTo(buffer);
        JSONObject body = new JSONObject(buffer.readUtf8());
        assertEquals("hi @everyone", body.getString("content"));
        assertEquals("Steve", body.getString("username"));
        assertEquals("https://mc-heads.net/avatar/Steve", body.getString("avatar_url"));
        assertEquals(0, body.getJSONObject("allowed_mentions").getJSONArray("parse").length());
    }

    @Test
    void failureDoesNotThrow() {
        discord = DiscordService.connectWebhook(URL, answering(404, "{\"message\": \"Unknown Webhook\"}"));

        discord.sendMessage("hi", "Steve").join();

        assertEquals(1, requests.size());
    }

    @Test
    void failureTextHasNoToken() {
        DiscordService direct = DiscordService.connectWebhook(URL, answering(404, "{\"message\": \"Unknown Webhook\"}"));
        discord = direct;

        CompletionException failure = assertThrows(CompletionException.class,
                () -> direct.sendWebhookMessage("hi", "Steve").join());
        String text = DiscordService.failureText(failure);

        assertTrue(text.contains("404"));
        assertFalse(text.contains("secret-token"));
    }

    @Test
    void rateLimitGivesUpAfterTheTimeout() {
        discord = DiscordService.connectWebhook(URL, answering(429, "{}", "Retry-After", "1"), 200);

        long start = System.currentTimeMillis();
        discord.sendMessage("hi", "Steve").join();

        assertTrue(System.currentTimeMillis() - start < 5000);
    }

    @Test
    void postsToTheThreadInTheUrl() {
        discord = DiscordService.connectWebhook(URL + "?thread_id=456", answering(204, ""));

        discord.sendMessage("hi", "Steve").join();

        Request request = requests.get(0);
        assertTrue(request.url().encodedPath().endsWith("/webhooks/123/secret-token"));
        assertEquals("456", request.url().queryParameter("thread_id"));
    }

    @Test
    void failureTextWithoutMessageNamesTheError() {
        assertEquals("IOException", DiscordService.failureText(new CompletionException(new IOException())));
    }

    @Test
    void rejectsOtherUrls() {
        assertThrows(IllegalArgumentException.class, () -> DiscordService.connectWebhook("https://example.com/hook"));
    }

    @Test
    void describesWebhookById() {
        discord = DiscordService.connectWebhook(URL, answering(204, ""));

        assertEquals("webhook 123", discord.describe());
    }

    private OkHttpClient answering(int code, String body, String... headers) {
        return new OkHttpClient.Builder().addInterceptor(chain -> {
            requests.add(chain.request());
            Response.Builder response = new Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(code)
                    .message("")
                    .body(ResponseBody.create(body, MediaType.get("application/json")));
            for (int i = 0; i + 1 < headers.length; i += 2) {
                response.header(headers[i], headers[i + 1]);
            }
            return response.build();
        }).build();
    }
}
