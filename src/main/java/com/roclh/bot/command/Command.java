package com.roclh.bot.command;

import discord4j.core.event.domain.message.MessageCreateEvent;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;

public interface Command {
    SendMessage telegramHandle(Update update);

    Mono<Void> discordHandle(MessageCreateEvent event);
}
