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

        LOGGER.info("Posting to {}", discord.describe());

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
            // Never log the token itself: logs get shared
            LOGGER.error("Discord rejected the bot token; check \"token\" in {}", ConfigProvider.CONFIG_FILE);
        } catch (Exception e) {
            LOGGER.error("Couldn't connect to Discord", e);
        }

        return null;
    }
}