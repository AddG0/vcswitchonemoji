package com.add.discord.bot.helpers;

import java.util.List;

import javax.annotation.Nonnull;

import com.vdurmont.emoji.EmojiParser;

public final class EmojiHelper {
    private EmojiHelper() {
    }

    public static boolean isEmoji(@Nonnull String string) {
        List<String> emojis = EmojiParser.extractEmojis(string);
        return !emojis.isEmpty();
    }
}