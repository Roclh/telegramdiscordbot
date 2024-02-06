package com.roclh.bot.bots;

import com.roclh.bot.data.DiscordBotProperties;
import com.roclh.bot.handlers.CommandHandler;
import com.roclh.bot.handlers.MessageHandler;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.discordjson.json.ComponentData;
import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.EmbedImageData;
import discord4j.discordjson.json.MessageCreateRequest;
import discord4j.discordjson.json.gateway.MessageCreate;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;


@Component
@Data
@Slf4j
public class DiscordBot {
    private final DiscordBotProperties discordBotProperties;

    private DiscordClient discordClient;
    private final CommandHandler commandHandler;
    private final MessageHandler messageHandler;
    private final BotsStorage botsStorage;

    @Autowired
    public DiscordBot(DiscordBotProperties discordBotProperties, CommandHandler commandHandler, BotsStorage botsStorage, MessageHandler messageHandler) {
        this.discordBotProperties = discordBotProperties;
        this.commandHandler = commandHandler;
        this.botsStorage = botsStorage;
        this.messageHandler = messageHandler;
    }

    public void start() {
        Mono<Void> login = discordClient.withGateway((GatewayDiscordClient gateway) -> {
            Mono<Void> printOnLogin = gateway.on(ReadyEvent.class, readyEvent ->
                    Mono.fromRunnable(() -> {
                        final User self = readyEvent.getSelf();
                        log.info("Logged in as {}", self.getUsername());
                    })
            ).then();
            Mono<Void> handleMessage = gateway.on(MessageCreateEvent.class, event -> {
                Message message = event.getMessage();
                if (message.getAuthor().flatMap(user -> Optional.of(user.isBot())).orElse(false)) {
                    return Mono.empty();
                }
                if (message.getContent().startsWith("!")) {
                    return commandHandler.handleCommands(event);
                }
                return messageHandler.handleMessage(event);
            }).then();
            return printOnLogin.and(handleMessage);
        });

        login.block();
    }


    public void sendMessage(com.roclh.bot.data.Message message, long chatId) {
        MessageCreateSpec.Builder builder = MessageCreateSpec.builder();
        if (message.getText() != null) {
            builder.content(message.getText());
        }
        if (message.isVideoMessage()) {
            try {
                builder.addFile(message.getVideoMessageAttached().getName(), new FileInputStream(message.getVideoMessageAttached()));
            } catch (FileNotFoundException e) {
                log.error("Can't attach video message with name {}", message.getVideoMessageAttached().getName(), e);
            }
        }
        if (message.isVideoAttached()) {
            try {
                builder.addFile(message.getVideo().getName(), new FileInputStream(message.getVideo()));
            } catch (FileNotFoundException e) {
                log.error("Can't attach video with name {}", message.getVideoMessageAttached().getName(), e);
            }
        }
        if (message.isVoiceMessage()) {
            try {
                builder.addFile(message.getVoice().getName(), new FileInputStream(message.getVoice()));
            } catch (FileNotFoundException e) {
                log.error("Can't attach audio message with name {}", message.getVideoMessageAttached().getName(), e);
            }
        }
        if (message.isAudioAttached()) {
            try {
                builder.addFile(message.getAudio().getName(), new FileInputStream(message.getAudio()));
            } catch (FileNotFoundException e) {
                log.error("Can't attach audio with name {}", message.getVideoMessageAttached().getName(), e);
            }
        }
        if (message.isDocumentAttached()) {
            try {
                builder.addFile(message.getDocument().getName(), new FileInputStream(message.getDocument()));
            } catch (FileNotFoundException e) {
                log.error("Can't attach document with name {}", message.getVideoMessageAttached().getName(), e);
            }
        }
        if (message.isStickerAttached()) {
            try {
                builder.addFile(message.getSticker().getName(), new FileInputStream(message.getSticker()));
            } catch (FileNotFoundException e) {
                log.error("Can't attach sticker with name {}", message.getVideoMessageAttached().getName(), e);
            }
        }
        if (message.isPhotoAttached()) {
            message.getPhotos().forEach(
                    photo -> {
                        try {
                            builder.addFile(photo.getName(), new FileInputStream(photo));
                        } catch (FileNotFoundException e) {
                            log.error("Can't attach photo with name {}", photo.getName(), e);
                        }
                    }
            );

        }

        discordClient.getChannelById(Snowflake.of(chatId))
                .createMessage(builder.build().asRequest())
                .publishOn(Schedulers.immediate())
                .block();

    }
}
