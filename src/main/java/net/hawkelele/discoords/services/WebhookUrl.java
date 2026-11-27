package net.hawkelele.discoords.services;

import club.minnced.discord.webhook.WebhookClientBuilder;

// Webhook URLs can be copied with a query, like "?thread_id=" for a thread or a forum post
public final class WebhookUrl {
    private static final String THREAD_ID = "thread_id=";

    private WebhookUrl() {
    }

    public static boolean isValid(String url) {
        return WebhookClientBuilder.WEBHOOK_PATTERN.matcher(withoutQuery(url)).matches();
    }

    public static String withoutQuery(String url) {
        int query = url.indexOf('?');
        return query < 0 ? url : url.substring(0, query);
    }

    // 0 when the URL names no thread, like the library expects
    public static long threadId(String url) {
        int query = url.indexOf('?');
        if (query < 0) {
            return 0;
        }

        for (String parameter : url.substring(query + 1).split("&")) {
            if (parameter.startsWith(THREAD_ID)) {
                try {
                    return Long.parseUnsignedLong(parameter.substring(THREAD_ID.length()));
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }

        return 0;
    }
}
