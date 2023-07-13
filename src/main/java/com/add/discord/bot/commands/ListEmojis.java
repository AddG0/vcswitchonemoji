package com.add.discord.bot.commands;

import java.time.Instant;
import java.util.Map;

import com.add.sql.VcSwitchSQL;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class ListEmojis implements Command {
    private CommandData command = Commands.slash("listemojis", "List the emojis to switch to");

    @Override
    public CommandData getCommand() {
        return command;
    }

    @Override
    public void onCommand(Guild guild, SlashCommandInteractionEvent event) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Emojis");
        StringBuilder description = new StringBuilder();
        Map<String, Long> savedEmojis = VcSwitchSQL.getEmojis(event.getGuild().getIdLong());
        savedEmojis.forEach((emoji, channelId) -> {
            description.append(emoji)
                    .append(" -> ")
                    .append(event.getGuild().getVoiceChannelById(channelId).getName())
                    .append("\n");
        });
        builder.setDescription(description.toString());
        builder.setTimestamp(Instant.now());
        event.replyEmbeds(builder.build()).queue();
    }

}
