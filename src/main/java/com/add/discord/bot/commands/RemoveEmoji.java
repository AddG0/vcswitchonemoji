package com.add.discord.bot.commands;

import java.util.List;

import com.add.sql.VcSwitchSQL;
import com.vdurmont.emoji.EmojiParser;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class RemoveEmoji implements Command {
    private CommandData command = Commands.slash("removeemoji", "Remove an emoji from the list of emojis to switch to")
            .addOption(OptionType.STRING, "emoji", "The emoji to remove", true);
    @Override
    public CommandData getCommand() {
        return command;
    }

    @Override
    public void onCommand(Guild guild, SlashCommandInteractionEvent event) {
        String emoji = event.getOption("emoji").getAsString();
        List<String> emojis = EmojiParser.extractEmojis(emoji);
        if (emojis.isEmpty()) {
            event.reply("Emoji not found!").queue();
            return;
        }
        emoji = emojis.get(0);
        if (!VcSwitchSQL.hasEmoji(guild.getIdLong(), emoji)) {
            event.reply("Emoji not found!").queue();
            return;
        }
        VcSwitchSQL.delete(guild.getIdLong(), emoji);
        event.reply("Removed emoji " + emoji).queue();
    }
}
