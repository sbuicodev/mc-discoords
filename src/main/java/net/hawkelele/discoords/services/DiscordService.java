package net.hawkelele.discoords.services;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class DiscordService {
    private final TextChannel channel;

    private DiscordService(TextChannel channel) {
        this.channel = channel;
    }

    public static DiscordService connect(String token, String guildId, String channelId) throws InterruptedException {
        JDA client = JDABuilder.createLight(token).build();
        client.awaitReady();
        Guild guild = client.getGuildById(guildId);
        TextChannel channel = guild != null ? guild.getTextChannelById(channelId) : null;

        if (channel == null) {
            return null;
        }

        System.out.println("Connected to channel: " + channel.getName() + " on server: " + guild.getName());

        return new DiscordService(channel);
    }

    public String getGuildName() {
        return channel.getGuild().getName();
    }

    public String getChannelName() {
        return channel.getName();
    }

    public void sendMessage(String message) {
        if (channel == null) {
            return;
        }

        channel.sendMessage(message).queue();
    }

}
