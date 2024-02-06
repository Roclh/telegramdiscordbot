package com.roclh.bot.config;

import com.roclh.bot.bots.BotsStorage;
import com.roclh.bot.bots.DiscordBot;
import discord4j.core.DiscordClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DiscordBotInit {
    private final DiscordBot discordbot;

    private final BotsStorage botsStorage;

    @Autowired
    public DiscordBotInit(DiscordBot discordbot,
                          BotsStorage botsStorage) {
        this.discordbot = discordbot;
        this.botsStorage = botsStorage;
    }



    @EventListener(classes = {ContextRefreshedEvent.class})
    public void init(){
        log.info("Initializing discord bot!");
        DiscordClient discordClient = DiscordClient.create(discordbot.getDiscordBotProperties().getToken());
        discordbot.setDiscordClient(discordClient);
        log.info("Starting discord bot!");
        botsStorage.setDiscordBot(this.discordbot);
        Runnable discordBotThread = discordbot::start;
        Thread discordThread = new Thread(discordBotThread);
        discordThread.start();
    }
}
