package com.add.discord.bot;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.add.discord.bot.commands.CommandManager;
import com.add.discord.bot.listeners.VcSwitch;
import com.add.sql.SQLHelper;
import com.add.sql.VcSwitchSQL;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

public class Bot {
    private static Logger logger = LoggerFactory.getLogger(Bot.class);
    private CommandManager commandManager = new CommandManager();
    private JDA jda;

    public Bot(String botToken) {
        logger.info("Starting bot...");

        JDABuilder builder = JDABuilder.createDefault(botToken);

        builder.setMemberCachePolicy(MemberCachePolicy.ALL);
        builder.enableIntents(GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_EMOJIS_AND_STICKERS,
                GatewayIntent.GUILD_VOICE_STATES,
                GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.MESSAGE_CONTENT);
        jda = builder.build();

        // Wait for bot to load
        try {
            jda.awaitReady();
        } catch (InterruptedException e) {
            logger.error("Failed to load bot!", e);
        }

        //Remove all commands
        jda.updateCommands().queue();

        // Create tables for all guilds
        for (Guild guild : jda.getGuilds()) {
            // try {
            //     SQLHelper.executeSQL(guild.getIdLong(), "DROP TABLE IF EXISTS VcSwitchEmojis");
            // } catch (SQLException e) {
            //     logger.error("Error dropping table", e);
            // }
            commandManager.updateCommands(guild);
            SQLHelper.connectToGuild(guild.getIdLong());
            VcSwitchSQL.createTable(guild.getIdLong());
            logger.info("Created table for guild {}", guild.getIdLong());
        }

        jda.addEventListener(new VcSwitch());
        jda.addEventListener(commandManager);
        logger.info("Bot started!");

    }

    public JDA getJDA() {
        return jda;
    }
}
