package com.roclh.bot.command;

import com.roclh.bot.properties.PropertiesContainer;
import com.roclh.bot.repository.TokenService;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Component
public class SetTokenCommand implements Command {

    private final TokenService tokenService;

    private final PropertiesContainer propertiesContainer;

    @Override
    public SendMessage telegramHandle(Update update) {
        boolean isDebug = propertiesContainer.getBoolProperty("debug");
        long chatId = update.getMessage().getChatId();
        String token = update.getMessage().getText().split(" ")[1];
        StringBuilder textMessage = new StringBuilder();
        textMessage.append("Токен для вашего чата был установлен!");
        SendMessage sendMessage = new SendMessage();
        if(tokenService.telegramExists(chatId)){
            tokenService.deleteByTelegramChatId(chatId);
        }
        tokenService.setTelegramToken(token, chatId);
        if(isDebug){
            textMessage.append("\n\nТокен для discord-а: ").append(tokenService.getKeyByDiscordChatId(tokenService.getDiscordChatId(token)));
            textMessage.append("\n\nЧат id discord-канала: ").append(tokenService.get(token).getDiscordChatId());
            textMessage.append("\n\nТокен для telegram-а: ").append(token);
            textMessage.append("\n\nЧат id telegram-канала: ").append(tokenService.get(token).getTelegramChatId());
        }
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textMessage.toString());
        return sendMessage;
    }

    @Override
    public Mono<Void> discordHandle(MessageCreateEvent event) {
        boolean isDebug = propertiesContainer.getBoolProperty("debug");
        long chatId = event.getMessage().getChannelId().asLong();
        Message message = event.getMessage();
        String token = message.getContent().split(" ")[1];
        StringBuilder textMessage = new StringBuilder();
        textMessage.append("Токен для вашего чата был установлен!");
        if(tokenService.discordExists(chatId)){
            tokenService.deleteByDiscordChatId(chatId);
        }
        tokenService.setDiscordToken(token, chatId);
        if(isDebug){
            textMessage.append("\n\nТокен для discord-а: ").append(token);
            textMessage.append("\n\nЧат id discord-канала: ").append(tokenService.get(token).getDiscordChatId());
            textMessage.append("\n\nТокен для telegram-а: ").append(tokenService.getKeyByTelegramChatId(Long.parseLong(tokenService.getTelegramChatId(token))));
            textMessage.append("\n\nЧат id telegram-канала: ").append(tokenService.get(token).getTelegramChatId());
        }
        return message.getChannel().flatMap(
                channel -> channel.createMessage(textMessage.toString())
        ).then();
    }
}
