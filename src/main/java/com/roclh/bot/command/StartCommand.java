package com.roclh.bot.command;

import com.roclh.bot.utils.Consts;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Component
public class StartCommand implements Command{
    @Override
    public SendMessage telegramHandle(Update update) {
        long chatId = update.getMessage().getChatId();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(Consts.HELLO_TELEGRAM_TEXT);


        return sendMessage;
    }

    @Override
    public Mono<Void> discordHandle(MessageCreateEvent event) {
        return event.getMessage().getChannel().flatMap(
                channel -> channel.createMessage(
                        Consts.HELLO_TELEGRAM_TEXT
                )
        ).then();
    }
}
