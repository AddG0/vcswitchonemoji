package com.add.discord.bot.listeners;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.add.discord.bot.commands.Command;
import com.add.sql.VcSwitchSQL;
import com.vdurmont.emoji.EmojiParser;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class VcSwitch extends ListenerAdapter {
    private static Logger logger = LoggerFactory.getLogger(VcSwitch.class);

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }
        String content = event.getMessage().getContentRaw();

        // Extract Unicode emojis
        List<String> emojis = EmojiParser.extractEmojis(content);

        if (emojis.isEmpty()) {
            Map<String, Long> savedEmojis = VcSwitchSQL.getEmojis(event.getGuild().getIdLong());

            for (String emoji : savedEmojis.keySet()) {
                if (emoji.equals(content)) {
                    event.getGuild().moveVoiceMember(event.getMember(),
                            event.getGuild().getVoiceChannelById(savedEmojis.get(emoji))).queue();
                }
            }
        } else {
            Map<String, Long> savedEmojis = VcSwitchSQL.getEmojis(event.getGuild().getIdLong());
            for (String emoji : emojis) {
                if (savedEmojis.containsKey(emoji)) {
                    event.getGuild().moveVoiceMember(event.getMember(),
                            event.getGuild().getVoiceChannelById(savedEmojis.get(emoji))).queue();
                }

            }
        }
    }
}