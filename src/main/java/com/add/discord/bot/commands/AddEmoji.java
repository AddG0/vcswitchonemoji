package com.add.discord.bot.commands;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.add.discord.bot.helpers.EmojiHelper;
import com.add.sql.VcSwitchSQL;
import com.vdurmont.emoji.EmojiParser;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class AddEmoji implements Command {
    private Logger logger = LoggerFactory.getLogger(AddEmoji.class);
    private CommandData command = Commands.slash("addemoji", "Add an emoji to the list of emojis to switch to")
            .addOption(OptionType.STRING, "emoji", "The emoji to add", true)
            .addOption(OptionType.STRING, "channel_id",
                    "The voice channel to send the user after emoji is sent", true);

    @Override
    public CommandData getCommand() {
        return command;
    }

    @Override
    public void onCommand(Guild guild, SlashCommandInteractionEvent event) {
        long channelId = event.getOption("channel_id").getAsLong();
        Channel channel = guild.getVoiceChannelById(channelId);
        if (channel == null) {
            event.reply("Channel must be a voice channel!").queue();
            return;
        }
        String emoji = event.getOption("emoji").getAsString();
        logger.debug("Emoji: {}", emoji);
        List<String> emojis = EmojiParser.extractEmojis(emoji);
        if (!emojis.isEmpty()) {
            // This is a Unicode emoji
            String unicodeEmoji = emojis.get(0);
            VcSwitchSQL.insert(guild.getIdLong(), unicodeEmoji, channel.getIdLong());
            event.reply("Added emoji " + unicodeEmoji).queue();
        } else {
            VcSwitchSQL.insert(guild.getIdLong(), emoji, channel.getIdLong());
            event.reply("Added emoji " + emoji).queue();
        }
    }

}
