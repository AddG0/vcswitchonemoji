package com.add;

import com.add.discord.bot.Bot;

import io.github.cdimascio.dotenv.Dotenv;

public class Main {
    private static Dotenv env = Dotenv.load();
    private static final String botToken = env.get("DISCORD_BOT_TOKEN");

    public static void main(String[] args) {
        Bot bot = new Bot(botToken);
    }
}
