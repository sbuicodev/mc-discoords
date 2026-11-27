package net.hawkelele.discoords.services;

import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.external.JDAWebhookClient;
import club.minnced.discord.webhook.receive.ReadonlyMessage;
import club.minnced.discord.webhook.send.AllowedMentions;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.hawkelele.discoords.Discoords;
import okhttp3.OkHttpClient;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

// Posts either through the bot or through a webhook, never both
public class DiscordService {
    private final TextChannel channel;
    private final JDAWebhookClient webhook;

    private DiscordService(TextChannel channel, JDAWebhookClient webhook) {
        this.channel = channel;
        this.webhook = webhook;
    }

    public static DiscordService connect(String token, String guildId, String channelId) throws InterruptedException {
        JDA client = JDABuilder.createLight(token).build();
        client.awaitReady();
        Guild guild = client.getGuildById(guildId);
        TextChannel channel = guild != null ? guild.getTextChannelById(channelId) : null;

        if (channel == null) {
            return null;
        }

        return new DiscordService(channel, null);
    }

    public static DiscordService connectWebhook(String url) {
        return connectWebhook(url, null);
    }

    // The HTTP client is only passed in by tests
    static DiscordService connectWebhook(String url, OkHttpClient http) {
        JDAWebhookClient webhook = new WebhookClientBuilder(url)
                .setHttpClient(http)
                .setWait(false)
                .setDaemon(true)
                .setAllowedMentions(AllowedMentions.none())
                .buildJDA();

        return new DiscordService(null, webhook);
    }

    // Never the webhook URL: it holds the token
    public String describe() {
        if (webhook != null) {
            return "webhook " + webhook.getId();
        }

        return "channel \"#" + channel.getName() + "\" on guild \"" + channel.getGuild().getName() + "\"";
    }

    public CompletableFuture<Void> sendMessage(String text, String player) {
        if (webhook != null) {
            return sendWebhookMessage(text, player)
                    .<Void>thenApply(message -> null)
                    .exceptionally(e -> {
                        Discoords.LOGGER.error("Couldn't post to the webhook: {}", failureText(e));
                        return null;
                    });
        }

        channel.sendMessage(MessageSignature.of(text, player))
                .setAllowedMentions(Collections.emptyList())
                .queue();

        return CompletableFuture.completedFuture(null);
    }

    CompletableFuture<ReadonlyMessage> sendWebhookMessage(String text, String player) {
        return webhook.send(WebhookMessages.of(text, player));
    }

    // The library's errors hold the status code and Discord's answer, not the URL
    static String failureText(Throwable e) {
        Throwable cause = e instanceof CompletionException && e.getCause() != null ? e.getCause() : e;
        return cause.getMessage();
    }

    public void close() {
        if (webhook != null) {
            webhook.close();
        }
    }
}
