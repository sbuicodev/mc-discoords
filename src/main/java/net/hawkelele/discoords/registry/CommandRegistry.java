package net.hawkelele.discoords.registry;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.hawkelele.discoords.commands.Command;
import net.hawkelele.discoords.commands.SendCoordinatesCommand;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CommandRegistry {
    protected Set<Command> commands = new HashSet<>();

    public void register(Command... commands) {
        this.commands.addAll(Arrays.asList(commands));

        this.commands.forEach(command -> {
            CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
                dispatcher.register(command.register());
            });
        });
    }
}
