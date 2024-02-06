package com.roclh.bot;

import com.roclh.bot.data.TelegramBotProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableConfigurationProperties(TelegramBotProperties.class)
public class TelegramDiscordBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(TelegramDiscordBotApplication.class, args);
    }

}
