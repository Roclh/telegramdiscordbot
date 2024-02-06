package com.roclh.bot.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.File;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class Message {
    private String id;
    private String userId;
    private String userName;
    private String chatId;
    private String text;
    private String caption;
    private boolean isDocumentAttached;
    private String documentId;
    private File document;
    private String documentExtension;
    private boolean isPhotoAttached;
    private List<String> photoId;
    private List<String> photoUniqueId;
    private List<File> photos;
    private boolean isVideoAttached;
    private String videoId;
    private File video;
    private boolean isStickerAttached;
    private String stickerId;
    private File sticker;
    private String stickerExtension;
    private boolean isAudioAttached;
    private String audioId;
    private File audio;
    private boolean isVoiceMessage;
    private String voiceId;
    private File voice;
    private boolean isVideoMessage;
    private String videoMessageId;
    private File videoMessageAttached;

}
