package com.roclh.bot.handlers;

import com.roclh.bot.bots.BotsStorage;
import com.roclh.bot.bots.DiscordBot;
import com.roclh.bot.bots.TelegramBot;
import com.roclh.bot.data.Message;
import com.roclh.bot.repository.TokenService;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Attachment;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import reactor.core.publisher.Mono;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Scope(scopeName = "singleton")
@Slf4j
public class MessageHandler {

    private final TokenService tokenService;
    private final BotsStorage botsStorage;

    @Autowired
    public MessageHandler(TokenService tokenService, BotsStorage botsStorage) {
        this.tokenService = tokenService;
        this.botsStorage = botsStorage;
    }

    public Mono<Void> handleMessage(Event event) {
        Message initial = fromEvent((MessageCreateEvent) event);
        log.info("Received a message {}", initial);
        attachFiles(initial, (MessageCreateEvent) event);
        if (tokenService.isAssigned(Long.parseLong(initial.getChatId()))) {
            initial.setText(buildTgMessage(initial));
            if (initial.isPhotoAttached() || initial.isAudioAttached() || initial.isVideoAttached() || initial.isDocumentAttached()) {
                String chatId = tokenService.getTelegramChatId(tokenService.getKeyByDiscordChatId(Long.parseLong(initial.getChatId())));
                botsStorage.getTelegramBot().sendMedia(initial, chatId);
            } else {
                botsStorage.getTelegramBot().sendMessage(of(initial));
            }
        } else {
            log.info("The message with id {} is not assigned, ignoring the output", initial.getId());
        }
        return Mono.empty();
    }

    public void handleMessage(Update update) {
        Message initial = fromUpdate(update);
        attachFiles(initial, botsStorage.getTelegramBot());
        log.info("Received a message {}", initial);
        Long chatId = tokenService.getDiscordChatId(tokenService.getKeyByTelegramChatId(update.getMessage().getChatId()));
        if (chatId == null) {
            return;
        }
        if (tokenService.isAssigned(Long.parseLong(initial.getChatId()))) {
            initial.setText(buildDiscordMessage(initial));
            botsStorage.getDiscordBot().sendMessage(initial, chatId);
        } else {
            log.info("The message with id {} is not assigned, ignoring the output", initial.getId());
        }
//        if (initial.isPhotoAttached()) {
//            telegramBot.sendPhoto(SendPhoto.builder()
//                    .chatId(initial.getChatId())
//                    .photo(new InputFile(initial.getPhotos().get(initial.getPhotos().size() - 1)))
//                    .build());
//        }


    }


    private Message fromUpdate(Update update) {
        return Message.builder()
                .id(String.valueOf(update.getMessage().getMessageId()))
                .text(update.getMessage().getText())
                .caption(update.getMessage().getCaption())
                .chatId(String.valueOf(update.getMessage().getChatId()))
                .userId(String.valueOf(update.getMessage().getFrom().getId()))
                .userName(String.valueOf(update.getMessage().getFrom().getUserName()))
                .isDocumentAttached(update.getMessage().hasDocument())
                .documentId(update.getMessage().hasDocument() ? update.getMessage().getDocument().getFileId() : null)
                .documentExtension(update.getMessage().hasDocument() ? "." + update.getMessage().getDocument().getFileName().split("\\.")[1] : null)
                .isAudioAttached(update.getMessage().hasAudio())
                .audioId(update.getMessage().hasAudio() ? update.getMessage().getAudio().getFileId() : null)
                .isPhotoAttached(update.getMessage().hasPhoto())
                .photoId(update.getMessage().hasPhoto() ? update.getMessage().getPhoto().stream().map(PhotoSize::getFileId).collect(Collectors.toList()) : null)
                .photoUniqueId(update.getMessage().hasPhoto() ? update.getMessage().getPhoto().stream().map(PhotoSize::getFileUniqueId).collect(Collectors.toList()) : null)
                .isVideoAttached(update.getMessage().hasVideo())
                .videoId(update.getMessage().hasVideo() ? update.getMessage().getVideo().getFileId() : null)
                .isStickerAttached(update.getMessage().hasSticker())
                .stickerId(update.getMessage().hasSticker() ? update.getMessage().getSticker().getFileId() : null)
                .stickerExtension(update.getMessage().hasSticker() ?
                        update.getMessage().getSticker().getIsAnimated() ?
                                ".webp" : update.getMessage().getSticker().getIsVideo() ?
                                ".webm" :
                                null
                        : null)
                .isVoiceMessage(update.getMessage().hasVoice())
                .voiceId(update.getMessage().hasVoice() ? update.getMessage().getVoice().getFileId() : null)
                .isVideoMessage(update.getMessage().hasVideoNote())
                .videoMessageId(update.getMessage().hasVideoNote() ? update.getMessage().getVideoNote().getFileId() : null)
                .build();
    }


    private Message fromEvent(MessageCreateEvent event) {
        log.info("Message create event: {}", event);
        return Message.builder()
                .id(event.getMessage().getId().asString())
                .userId(event.getMessage().getUserData().id().asString())
                .userName(event.getMessage().getUserData().globalName().orElse(event.getMessage().getUserData().username()))
                .chatId(event.getMessage().getChannelId().asString())
                .text(event.getMessage().getContent())
                .isPhotoAttached(!getPhotos(event).isEmpty())
                .photoId(getPhotos(event).stream().map(attachment -> attachment.getId().asString())
                        .toList())
                .isAudioAttached(event.getMessage().getAttachments().stream().anyMatch(attachment -> attachment.getContentType()
                        .flatMap(s -> Optional.of(s.contains("audio"))).orElse(false)))
                .audio(event.getMessage().getAttachments().stream().filter(attachment -> attachment.getContentType()
                        .flatMap(s -> Optional.of(s.contains("audio"))).orElse(false)).findFirst()
                        .flatMap(attachment -> Optional.ofNullable(downloadFile(attachment.getUrl(), attachment.getFilename()))).orElse(null))
                .isVideoAttached(event.getMessage().getAttachments().stream().anyMatch(attachment -> attachment.getContentType()
                        .flatMap(s -> Optional.of(s.contains("video"))).orElse(false)))
                .video(event.getMessage().getAttachments().stream().filter(attachment -> attachment.getContentType()
                        .flatMap(s -> Optional.of(s.contains("video"))).orElse(false)).findFirst()
                        .flatMap(attachment -> Optional.ofNullable(downloadFile(attachment.getUrl(), attachment.getFilename()))).orElse(null))
                .isDocumentAttached(event.getMessage().getAttachments().stream().anyMatch(attachment -> attachment.getContentType()
                        .flatMap(s -> Optional.of(!s.contains("audio") && !s.contains("image") && !s.contains("video")))
                                .orElse(false)))
                .document(event.getMessage().getAttachments().stream().filter(attachment -> attachment.getContentType()
                        .flatMap(s -> Optional.of(!s.contains("audio") && !s.contains("image") && !s.contains("video"))).orElse(false))
                        .findFirst()
                        .flatMap(attachment -> Optional.ofNullable(downloadFile(attachment.getUrl(), attachment.getFilename())))
                        .orElse(null))
                .documentExtension(event.getMessage().getAttachments().stream().filter(attachment -> attachment.getContentType()
                        .flatMap(s -> Optional.of(!s.contains("audio") && !s.contains("image") && !s.contains("video"))).orElse(false))
                        .findFirst()
                        .flatMap(attachment -> Optional.ofNullable(attachment.getFilename().split("\\.")[1]))
                        .flatMap(filename -> Optional.of("." + filename))
                        .orElse(null)
                )
                .build();
    }

    public Message attachFiles(Message message, TelegramBot telegramBot) {
        try {
            if (message.isVideoMessage()) {
                log.info("Trying to load a video message with id: {}", message.getId());
                message.setVideoMessageAttached(getFile(message.getVideoMessageId(), telegramBot, ".mp4"));
            }
            if (message.isVoiceMessage()) {
                log.info("Trying to load a voice message with id: {}", message.getId());
                message.setVoice(getFile(message.getVoiceId(), telegramBot, ".mp3"));
            }
            if (message.isPhotoAttached()) {
                log.info("Trying to load a photo with id: {}", message.getId());
                message.setPhotos(message.getPhotoId().stream().map(
                        photoId -> {
                            try {
                                return getFile(photoId, telegramBot, ".png");
                            } catch (TelegramApiException | IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                ).collect(Collectors.toList()));
            }
            if (message.isAudioAttached()) {
                log.info("Trying to load an audio with id {}", message.getId());
                message.setAudio(getFile(message.getAudioId(), telegramBot, ".mp3"));
            }
            if (message.isDocumentAttached()) {
                log.info("Trying to load a document with id {}", message.getId());
                message.setDocument(getFile(message.getDocumentId(), telegramBot, message.getDocumentExtension()));
            }
            if (message.isVideoAttached()) {
                log.info("Trying to load a video with id {}", message.getId());
                message.setVideo(getFile(message.getVideoId(), telegramBot, ".mp4"));
            }
            if (message.isStickerAttached()) {
                log.info("Trying to lad a sticker with id {}", message.getId());
                if (message.getStickerExtension() == null) {
                    log.error("Can't parse sticker extension to display correctly for message with id {}", message.getId());
                } else {
                    message.setSticker(getFile(message.getStickerId(), telegramBot, message.getStickerExtension()));
                }
            }
        } catch (TelegramApiException | IOException e) {
            throw new RuntimeException(e);
        }
        return message;
    }

    public Message attachFiles(Message message, MessageCreateEvent event) {
        if (message.isVideoMessage()) {
        }
        if (message.isVoiceMessage()) {
        }
        if (message.isPhotoAttached()) {
            message.setPhotos(getPhotos(event).stream().map(
                    attachment -> downloadFile(attachment.getUrl(), "local/" + attachment.getId().asString())
            ).toList());
        }
        if (message.isAudioAttached()) {

        }
        if (message.isDocumentAttached()) {

        }
        if (message.isVideoAttached()) {

        }
        if (message.isStickerAttached()) {

        }

        return message;
    }

    private java.io.File getFile(String fileId, TelegramBot telegramBot, String fileExtension) throws TelegramApiException, IOException {
        GetFile getFile = new GetFile();
        getFile.setFileId(fileId);
        String filePath = telegramBot.execute(getFile).getFilePath();
        return downloadFile(filePath, "local/" + fileId + fileExtension, telegramBot);
    }

    private java.io.File downloadFile(String filepath, String localFilePath, TelegramBot telegramBot) throws IOException, TelegramApiException {
        File file = new File();
        file.setFilePath(filepath);
        java.io.File localFile = new java.io.File(localFilePath);
        InputStream is = new URL(file.getFileUrl(telegramBot.getBotToken())).openStream();
        FileUtils.copyInputStreamToFile(is, localFile);
        return localFile;
    }

    private SendMessage of(Message message) {
        String chatId = tokenService.getTelegramChatId(tokenService.getKeyByDiscordChatId(Long.parseLong(message.getChatId())));
        if (chatId == null) {
            return null;
        }
        SendMessage.SendMessageBuilder builder = SendMessage.builder();
        builder.chatId(chatId);
        builder.text(message.getText());
        SendMessage sendMessage = new SendMessage(chatId, message.getText());
        sendMessage.setParseMode("MarkdownV2");
        return sendMessage;
    }

    private String buildDiscordMessage(Message message) {
        return "**" + message.getUserName() + "**" + "\n\n" +
                (message.getText() == null || message.getText().isEmpty() ?
                        message.getCaption() == null ?
                                "" :
                                message.getCaption()
                        : message.getText());
    }

    private String buildTgMessage(Message message) {
        return "*" + md(message.getUserName()) + "*" + "\n\n" +
                md(message.getText());
    }

    private String md(String text) {
        return text.replace("_", "\\_")
                .replace("*", "\\*")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("~", "\\~")
                .replace("`", "\\`")
                .replace(">", "\\>")
                .replace("#", "\\#")
                .replace("+", "\\+")
                .replace("-", "\\-")
                .replace("=", "\\=")
                .replace("|", "\\|")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace(".", "\\.")
                .replace("!", "\\!");
    }

    private List<Attachment> getPhotos(MessageCreateEvent event) {
        return event.getMessage().getAttachments().stream().filter(attachment -> attachment.getContentType()
                .flatMap(contentType -> Optional.of(contentType.contains("image"))).orElse(false)).toList();
    }

    private java.io.File downloadFile(String url, String fileName) {
        try (BufferedInputStream inputStream = new BufferedInputStream(new URL(url).openStream())) {
            java.io.File localFile = new java.io.File(fileName);
            FileUtils.copyInputStreamToFile(inputStream, localFile);
            return localFile;
        } catch (IOException e) {
            log.info("Can't read file by url {}", url);
        }
        return null;
    }

}
