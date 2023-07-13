package com.add.discord.bot.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class CommandManager extends ListenerAdapter {
    private final Map<String, Command> commands = new HashMap<>();

    public CommandManager() {
        // Add commands to the map
        addCommand(new AddEmoji());
        addCommand(new ListEmojis());
        addCommand(new RemoveEmoji());
    }

    private void addCommand(Command cmd) {
        String commandName = cmd.getCommand().getName();
        if (commands.containsKey(commandName)) {
            throw new IllegalArgumentException("Command already exists: " + commandName);
        }
        commands.put(commandName, cmd);
    }

    public List<CommandData> getCommands() {
        return Arrays.asList(commands.values().stream().map(Command::getCommand).toArray(CommandData[]::new));
    }

    public void updateCommands(Guild guild) {
        List<CommandData> commands = getCommands();
        guild.updateCommands().addCommands(commands).queue();
    }

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        Command command = commands.get(commandName);
        if (command == null) {
            return;
        }
        Guild guild = event.getGuild();
        command.onCommand(guild, event);
    }

}
