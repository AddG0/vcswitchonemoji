package com.add.discord.bot.commands;

import javax.annotation.Nonnull;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public interface Command {
    CommandData getCommand();

    void onCommand(Guild guild, SlashCommandInteractionEvent event);

}
