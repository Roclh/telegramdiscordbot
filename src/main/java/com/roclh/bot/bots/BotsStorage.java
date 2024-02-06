package com.roclh.bot.bots;

import org.springframework.stereotype.Component;

@Component
public class BotsStorage {

    private DiscordBot discordBot;
    private TelegramBot telegramBot;

    public void setDiscordBot(DiscordBot discordBot) {
        this.discordBot = discordBot;
    }

    public void setTelegramBot(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    public DiscordBot getDiscordBot() {
        if (discordBot == null) {
            throw new RuntimeException("Illegal state");
        }
        return discordBot;
    }

    public TelegramBot getTelegramBot() {
        if (telegramBot == null) {
            throw new RuntimeException("Illegal state");
        }
        return telegramBot;
    }
}
