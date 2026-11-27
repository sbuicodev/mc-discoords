package net.hawkelele.discoords.services;

import net.hawkelele.discoords.config.Config;

public enum PostingMode {
    WEBHOOK,
    BOT,
    // The config has a webhook, but not one Discord would accept
    BOT_INSTEAD_OF_WEBHOOK;

    public static PostingMode of(Config config) {
        if (!config.usesWebhook()) {
            return BOT;
        }

        return WebhookUrl.isValid(config.webhook.trim()) ? WEBHOOK : BOT_INSTEAD_OF_WEBHOOK;
    }
}
