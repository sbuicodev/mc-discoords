package net.hawkelele.discoords;

import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.fabricmc.api.ModInitializer;
import net.hawkelele.discoords.commands.SendCoordinatesCommand;
import net.hawkelele.discoords.config.Config;
import net.hawkelele.discoords.config.ConfigProvider;
import net.hawkelele.discoords.registry.CommandRegistry;
import net.hawkelele.discoords.services.DiscordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Discoords implements ModInitializer {
    public static final String MOD_ID = "discoords";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        DiscordService discord = initDiscord();
        if (discord == null) {
            LOGGER.info("Failed to load Discord");
            return;
        }

        LOGGER.info("Connected to channel \"#{}\" on guild \"{}\"", discord.getChannelName(), discord.getGuildName());

        new CommandRegistry().register(new SendCoordinatesCommand(discord));
    }

    private DiscordService initDiscord() {
        Config config = null;

        try {
            config = new ConfigProvider().load();

            if (config == null) {
                LOGGER.warn("Failed to load config. Mod is not properly initialized.");
                return null;
            }
            return DiscordService.connect(config.token, config.guild, config.channel);
        } catch (InvalidTokenException e) {
            assert (config != null);
            LOGGER.error("Invalid token passed to the Discord API: {}", config.token);
        } catch (Exception e) {
            LOGGER.error("Couldn't connect to Discord", e);
        }

        return null;
    }
}