package com.roclh.bot.command;

import com.roclh.bot.repository.TokenService;
import com.roclh.bot.utils.Consts;
import discord4j.core.event.domain.message.MessageCreateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@RequiredArgsConstructor
@Component
@Slf4j
public class TokenCommand implements Command {

    private TokenService tokenService;

    @Autowired
    public TokenCommand(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public SendMessage telegramHandle(Update update) {
        long chatId = update.getMessage().getChatId();
        SendMessage sendMessage = new SendMessage();
        if (tokenService.telegramExists(chatId)) {
            sendMessage.setText(Consts.TOKEN_TEXT + tokenService.getKeyByTelegramChatId(chatId));
        } else {
            String uuid = UUID.randomUUID().toString();
            tokenService.setTelegramToken(uuid, chatId);
            sendMessage.setText(Consts.TOKEN_TEXT + uuid);
        }
        sendMessage.setChatId(String.valueOf(chatId));
        return sendMessage;
    }

    @Override
    public Mono<Void> discordHandle(MessageCreateEvent event) {
        long chatId = event.getMessage().getChannelId().asLong();
        AtomicReference<String> response = new AtomicReference<>();
        if (tokenService.discordExists(chatId)) {
            response.set(Consts.TOKEN_TEXT + tokenService.getKeyByDiscordChatId(chatId));
        } else {
            String uuid = UUID.randomUUID().toString();
            tokenService.setDiscordToken(uuid, chatId);
            response.set(Consts.TOKEN_TEXT + uuid);
        }
        log.info("Sending the response: {}", response.get());
        return event.getMessage().getChannel().flatMap(
                channel -> channel.createMessage(response.get())
        ).then();
    }
}
