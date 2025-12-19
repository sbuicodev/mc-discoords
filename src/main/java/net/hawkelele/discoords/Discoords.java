package net.hawkelele.discoords;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.hawkelele.discoords.config.Config;
import net.hawkelele.discoords.exceptions.DiscordServiceException;
import net.hawkelele.discoords.services.ConfigService;
import net.hawkelele.discoords.services.DiscordService;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Discoords implements ModInitializer {
    public static final String MOD_ID = "discoords";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Discoords initialized");

        try {
            Config config = new ConfigService().load();
            LOGGER.info("Config loaded");

            DiscordService discord = DiscordService.connect(config.token, config.guild, config.channel);
            if (discord == null) {
                throw new DiscordServiceException("Couldn't connect to Discord");
            }
            LOGGER.info("Discord service loaded");
            LOGGER.info("Connected to channel \"#{}\" on guild \"{}\"", discord.getChannelName(), discord.getGuildName());

            CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
                dispatcher.register(Commands.literal("dc").then(Commands.argument("name", StringArgumentType.greedyString()).executes(context -> {
                    String name = StringArgumentType.getString(context, "name");
                    CommandSourceStack source = context.getSource();
                    Player player = source.getPlayer();

                    if (player == null || !source.isPlayer()) {
                        source.sendFailure(Component.literal("Command should be run by a player!"));
                        return 0;
                    }

                    Vec3 pos = player.position();
                    String coordinatesText = Math.round(pos.x) + ", " + Math.round(pos.y) + ", " + Math.round(pos.z);

                    String icon = "🧭";

                    discord.sendMessage(icon + " " + name + ": `" + coordinatesText + "` *[" + player.getName().getString() + "]*");
                    source.sendSuccess(() -> Component.literal("Coordinates saved as " + name + ": " + coordinatesText), false);

                    return 1;
                })));
            });


        } catch (InvalidTokenException e) {
            LOGGER.error("Invalid token, please check your configuration file in " + ConfigService.CONFIG_FILE);
        } catch (IOException e) {
            LOGGER.error("Failed to load config", e);
        } catch (InterruptedException | DiscordServiceException e) {
            LOGGER.error("Failed connecting to Discord", e);
        }
    }


}