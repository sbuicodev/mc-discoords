package net.hawkelele.discoords.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.hawkelele.discoords.services.DiscordService;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class SendCoordinatesCommand implements Command {

    private final DiscordService discord;

    public SendCoordinatesCommand(DiscordService discord) {
        this.discord = discord;
    }

    public LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("dc").then(Commands.argument("name", StringArgumentType.greedyString()).executes(this::handle));
    }

    private int handle(CommandContext<CommandSourceStack> context) {
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

        discord.sendMessage(icon + " " + name + ": `" + coordinatesText + "` ~ *[" + player.getName().getString() + "]*");
        source.sendSuccess(() -> Component.literal("Coordinates saved as " + name + ": " + coordinatesText), false);

        return 1;
    }

}
