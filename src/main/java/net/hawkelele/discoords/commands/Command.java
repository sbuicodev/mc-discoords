package net.hawkelele.discoords.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;

public interface Command {
    LiteralArgumentBuilder<CommandSourceStack> register();
}
