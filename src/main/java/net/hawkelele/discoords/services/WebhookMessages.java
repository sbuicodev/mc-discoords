package net.hawkelele.discoords.services;

import club.minnced.discord.webhook.send.AllowedMentions;
import club.minnced.discord.webhook.send.WebhookMessage;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;

// A webhook message that looks like the player posted it
public final class WebhookMessages {
    private static final String AVATAR_URL = "https://mc-heads.net/avatar/";

    private WebhookMessages() {
    }

    public static WebhookMessage of(String text, String player) {
        WebhookMessageBuilder builder = new WebhookMessageBuilder()
                .setAvatarUrl(AVATAR_URL + player)
                .setAllowedMentions(AllowedMentions.none());

        // Discord rejects these as webhook names, so the player goes in the text instead
        if (isRejectedName(player)) {
            return builder.setContent(MessageSignature.of(text, player)).build();
        }

        return builder.setUsername(player).setContent(text).build();
    }

    private static boolean isRejectedName(String player) {
        String name = player.toLowerCase();
        return name.contains("discord") || name.contains("clyde");
    }
}
