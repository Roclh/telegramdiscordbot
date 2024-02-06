package com.roclh.bot.repository;

import com.roclh.bot.data.Token;
import com.roclh.bot.utils.Safe;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class TokenService {

    private TokenRepository tokenRepository;

    public void setDiscordToken(String key, long value) {
        if (tokenRepository.existsByKey(key)) {
            tokenRepository.updateDiscordChatIdByKey(value, key);
        } else {
            tokenRepository.save(Token.builder()
                    .key(key)
                    .discordChatId(value)
                    .build());
        }
    }

    public Token get(String key) {
        return tokenRepository.findByKey(key);
    }

    public void setTelegramToken(String key, long value) {
        if (tokenRepository.existsByKey(key)) {
            tokenRepository.updateTelegramChatIdByKey(value, key);
        } else {
            tokenRepository.save(Token.builder()
                    .key(key)
                    .telegramChatId(value)
                    .build());
        }
    }

    public Long getDiscordChatId(String key) {
        return Safe.getFrom(tokenRepository.findByKey(key), Token::getDiscordChatId);
    }

    public String getTelegramChatId(String key) {
        return Safe.getFrom(tokenRepository.findByKey(key), value -> value.getTelegramChatId().toString());
    }

    public boolean containsKey(String key) {
        return tokenRepository.existsByKey(key);
    }

    public boolean isAssigned(long chatId){
        if(!tokenRepository.existsByTelegramChatId(chatId) && !tokenRepository.existsByDiscordChatId(chatId)){
            return false;
        }
        String discordKey = getKeyByDiscordChatId(chatId);
        String telegramKey = getKeyByTelegramChatId(chatId);
        return discordKey == null ? get(telegramKey).getDiscordChatId() != null : get(discordKey).getTelegramChatId() != null;
    }

    public boolean telegramExists(long chatId) {
        return tokenRepository.existsByTelegramChatId(chatId);
    }

    public String getKeyByTelegramChatId(long chatId) {
        return Safe.getFrom(tokenRepository.findByTelegramChatId(chatId), Token::getKey);
    }

    public String getKeyByDiscordChatId(long chatId) {
        return Safe.getFrom(tokenRepository.findByDiscordChatId(chatId), Token::getKey);
    }

    public boolean discordExists(long chatId) {
        return tokenRepository.existsByDiscordChatId(chatId);
    }

    public void deleteByTelegramChatId(long chatId) {
        if(telegramExists(chatId)){
            tokenRepository.deleteByTelegramChatId(chatId);
        }
    }

    public void deleteByDiscordChatId(long chatId) {
        if(discordExists(chatId)){
            tokenRepository.deleteByDiscordChatId(chatId);
        }
    }
}
