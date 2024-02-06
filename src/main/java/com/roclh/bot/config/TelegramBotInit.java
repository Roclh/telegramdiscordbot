package com.roclh.bot.config;

import com.roclh.bot.bots.BotsStorage;
import com.roclh.bot.bots.TelegramBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramBotInit {
    private final TelegramBot telegramBot;
    private final BotsStorage botsStorage;

    @Async
    @EventListener({ContextRefreshedEvent.class})
    public void init() throws TelegramApiException {
        log.info("Starting initialization of telegram bot");
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        try {
            telegramBotsApi.registerBot(telegramBot);
            botsStorage.setTelegramBot(telegramBot);
            log.info("Registered bot successfully");
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }
}
