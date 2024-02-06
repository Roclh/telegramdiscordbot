package com.roclh.bot.command;

import com.roclh.bot.properties.PropertiesContainer;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;

@Component
public class DebugCommand implements Command {

    public final PropertiesContainer propertiesContainer;

    @Autowired
    public DebugCommand(PropertiesContainer propertiesContainer) {
        this.propertiesContainer = propertiesContainer;
    }

    @Override
    public SendMessage telegramHandle(Update update) {
        this.propertiesContainer.setProperty("debug", !propertiesContainer.getBoolProperty("debug"));
        return new SendMessage(String.valueOf(update.getMessage().getChatId()),
                this.propertiesContainer.getBoolProperty("debug") ? "Режим отладки был включен!" : "Режим отладки был выключен!");
    }

    @Override
    public Mono<Void> discordHandle(MessageCreateEvent event) {
        this.propertiesContainer.setProperty("debug", !propertiesContainer.getBoolProperty("debug"));
        return event.getMessage().getChannel().flatMap(
                messageChannel -> messageChannel.createMessage(
                        this.propertiesContainer.getBoolProperty("debug") ? "Режим отладки был включен!" : "Режим отладки был выключен!"
                )
        ).then();
    }
}
