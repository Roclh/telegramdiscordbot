package com.roclh.bot.handlers;

import com.roclh.bot.command.Command;
import com.roclh.bot.command.DebugCommand;
import com.roclh.bot.command.SetTokenCommand;
import com.roclh.bot.command.StartCommand;
import com.roclh.bot.command.TokenCommand;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class CommandHandler {

    private final Map<String, Command> commands;

    public CommandHandler(@Autowired StartCommand startCommand,
                          @Autowired TokenCommand tokenCommand,
                          @Autowired SetTokenCommand setTokenCommand,
                          @Autowired DebugCommand debugCommand) {
        this.commands = Map.of(
                "/start", startCommand,
                "/token", tokenCommand,
                "/settoken", setTokenCommand,
                "/debug", debugCommand
        );
    }

    public SendMessage handleCommands(Update update) {
        String messageText = update.getMessage().getText();
        String command = messageText.split(" ")[0];
        long chatId = update.getMessage().getChatId();
        log.info("Received a message from user {} from a chat with id:\"{}\", containing message \"{}\"", update.getMessage().getFrom().getUserName(), chatId, messageText);

        if (command.startsWith("/")) {
            Command commandHandler = commands.get(command);
            if (commandHandler != null) {
                log.info("Recognized command {}, starting handling", command);
                return commandHandler.telegramHandle(update);
            } else {
                return new SendMessage(String.valueOf(chatId), "Unknown command");
            }
        }
        return null;
    }

    public Mono<Void> handleCommands(Event event) {
        if (event instanceof MessageCreateEvent messageEvent) {
            long chatId = messageEvent.getMessage().getChannelId().asLong();
            Message message = messageEvent.getMessage();
            String command = message.getContent().split(" ")[0];
            command = command.replace("!", "/");
            log.info("Received a message from user {} from a chat with id:\"{}\", containing message \"{}\"", message.getAuthor()
                    .flatMap(user -> Optional.of(user.getUsername())).orElse(null), chatId, message.getContent());

            if (command.startsWith("/")) {
                Command commandHandler = commands.get(command);
                if (commandHandler != null) {
                    log.info("Recognized command {}, starting handling", command);
                    return commandHandler.discordHandle(messageEvent);
                } else {
                    return message.getChannel().flatMap(
                            channel -> channel.createMessage("Unknown command")
                    ).then();
                }
            }
        }
        return Mono.empty();
    }

}
