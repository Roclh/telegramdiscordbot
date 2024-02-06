package com.roclh.bot.bots;

import com.roclh.bot.data.Message;
import com.roclh.bot.data.TelegramBotProperties;
import com.roclh.bot.handlers.CallbackHandler;
import com.roclh.bot.handlers.CommandHandler;
import com.roclh.bot.handlers.MessageHandler;
import com.roclh.bot.utils.Safe;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.message.MessageCreateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.send.SendVideoNote;
import org.telegram.telegrambots.meta.api.methods.send.SendVoice;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    public final TelegramBotProperties telegramBotProperties;

    public final CommandHandler commandsHandler;

    public final CallbackHandler callbackHandler;

    public final MessageHandler messageHandler;

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            if (update.getMessage().hasText() && update.getMessage().getText().startsWith("/")) {
                sendMessage(commandsHandler.handleCommands(update));
            } else {
                messageHandler.handleMessage(update);
            }
        } else if (update.hasCallbackQuery()) {
            sendMessage(callbackHandler.handleCallbacks(update));
        }
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        log.info("Received updates {}", updates);
        Update update = updates.get(0);
        update.setMessage(mergeMessages(updates.stream().map(Update::getMessage).toList()));
        update.setEditedMessage(mergeMessages(updates.stream().map(Update::getEditedMessage).toList()));
        onUpdateReceived(update);
    }

    public void sendMessage(SendMessage sendMessage) {
        log.info("Trying to send message to telegram client {}", sendMessage);
        if (sendMessage == null) {
            return;
        }
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    public void sendMedia(Message message, String chatId) {
        if (chatId == null) {
            return;
        }
        if (countMedias(message) > 1) {
            List<File> attachments = new ArrayList<>();
            if (message.isPhotoAttached()) {
                attachments.addAll(message.getPhotos());
            }
            if (message.isVideoMessage()) {
                attachments.add(message.getVideoMessageAttached());
            }
            if (message.isVoiceMessage()) {
                attachments.add(message.getVoice());
            }
            if (message.isStickerAttached()) {
                attachments.add(message.getSticker());
            }
            if (message.isAudioAttached()) {
                attachments.add(message.getAudio());
            }
            if (message.isVideoAttached()) {
                attachments.add(message.getVideo());
            }
            sendMediaGroup(message, chatId, attachments);
            return;
        }
        if (message.isPhotoAttached()) {
            SendPhoto.SendPhotoBuilder builder = SendPhoto.builder();
            builder.chatId(chatId);
            builder.caption(message.getText());
            builder.photo(new InputFile(message.getPhotos().get(0)));
            builder.parseMode("MarkdownV2");
            try {
                execute(builder.build());
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
            }
        }
        if(message.isVideoMessage()){
            SendVideoNote.SendVideoNoteBuilder builder = SendVideoNote.builder();
            builder.chatId(chatId);
            builder.videoNote(new InputFile(message.getVideoMessageAttached()));
            try {
                execute(builder.build());
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
            }
        }
        if(message.isVoiceMessage()){
            SendVoice.SendVoiceBuilder builder = SendVoice.builder();
            builder.chatId(chatId);
            builder.caption(message.getText());
            builder.voice(new InputFile(message.getVoice()));
            builder.parseMode("MarkdownV2");
            try {
                execute(builder.build());
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
            }
        }
        if(message.isVideoAttached()){
            SendVideo.SendVideoBuilder builder = SendVideo.builder();
            builder.chatId(chatId);
            builder.caption(message.getText());
            builder.video(new InputFile(message.getVideo()));
            builder.parseMode("MarkdownV2");
            try {
                execute(builder.build());
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
            }
        }
        if(message.isDocumentAttached()){
            SendDocument.SendDocumentBuilder builder = SendDocument.builder();
            builder.chatId(chatId);
            builder.caption(message.getText());
            builder.document(new InputFile(message.getDocument()));
            builder.parseMode("MarkdownV2");
            try {
                execute(builder.build());
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
            }
        }
        if(message.isAudioAttached()){
            SendAudio.SendAudioBuilder builder = SendAudio.builder();
            builder.caption(message.getText());
            builder.chatId(chatId);
            builder.audio(new InputFile(message.getAudio()));
            builder.parseMode("MarkdownV2");
            try {
                execute(builder.build());
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
            }
        }
    }


    private void sendMediaGroup(Message message, String chatId, List<File> attachments) {
        List<InputMedia> medias = new ArrayList<>(
                attachments.stream()
                        .map(file -> (InputMedia) InputMediaPhoto.builder()
                                .media("attach://" + file.getName())
                                .mediaName(file.getName())
                                .isNewMedia(true)
                                .newMediaFile(file)
                                .parseMode("MarkdownV2")
                                .build())
                        .toList());
        medias.get(0).setCaption(message.getText());

        SendMediaGroup sendMediaGroup = SendMediaGroup.builder()
                .chatId(chatId)
                .medias(medias)
                .build();
        try {
            execute(sendMediaGroup);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }

    }

    private int countMedias(Message message) {
        int mediaCount = 0;
        mediaCount += message.isPhotoAttached() ? message.getPhotos().size() : 0;
        mediaCount += message.isVideoAttached() ? 1 : 0;
        mediaCount += message.isVideoMessage() ? 1 : 0;
        mediaCount += message.isVoiceMessage() ? 1 : 0;
        mediaCount += message.isAudioAttached() ? 1 : 0;
        mediaCount += message.isDocumentAttached() ? 1 : 0;
        mediaCount += message.isStickerAttached() ? 1 : 0;
        return mediaCount;
    }

    @SuppressWarnings("deprecation")
    @Override
    public String getBotToken() {
        return telegramBotProperties.getToken();
    }

    @Override
    public String getBotUsername() {
        return telegramBotProperties.getName();
    }

    private org.telegram.telegrambots.meta.api.objects.Message mergeMessages(List<org.telegram.telegrambots.meta.api.objects.Message> messages) {
        if (messages.stream().filter(Objects::nonNull).findAny().isEmpty()) {
            return null;
        }
        org.telegram.telegrambots.meta.api.objects.Message newMessage = messages.get(0);
        newMessage.setText(messages.stream().map(org.telegram.telegrambots.meta.api.objects.Message::getText).filter(Objects::nonNull).collect(Collectors.joining("\n")));
        newMessage.setCaption(messages.stream().map(org.telegram.telegrambots.meta.api.objects.Message::getCaption).filter(Objects::nonNull).collect(Collectors.joining("\n")));
        newMessage.setCaptionEntities(messages.stream().flatMap(s -> s.getCaptionEntities() != null ? s.getCaptionEntities().stream() : null)
                .filter(Objects::nonNull).toList());
        newMessage.setPhoto(messages.stream().map(org.telegram.telegrambots.meta.api.objects.Message::getPhoto)
                .filter(Objects::nonNull)
                .map(s -> s.get(s.size() - 1))
                .toList());
        return newMessage;
    }
}
