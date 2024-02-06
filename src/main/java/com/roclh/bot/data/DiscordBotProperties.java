package com.roclh.bot.data;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "discord")
@Data
public class DiscordBotProperties {
    private String token;
    private String name;
}
