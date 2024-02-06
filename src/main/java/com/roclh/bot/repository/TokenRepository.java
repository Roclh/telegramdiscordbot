package com.roclh.bot.repository;

import com.roclh.bot.data.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    Token findByTelegramChatId(Long telegramChatId);
    Token findByDiscordChatId(Long discordChatId);
    @Transactional
    void deleteByDiscordChatId(Long discordChatId);
    @Transactional
    void deleteByTelegramChatId(Long telegramChatId);
    boolean existsByDiscordChatId(Long discordChatId);
    boolean existsByTelegramChatId(Long telegramChatId);
    @Transactional
    @Modifying
    @Query("update Token t set t.telegramChatId = ?1 where t.key = ?2")
    int updateTelegramChatIdByKey(Long telegramChatId, String key);
    Token findByKey(String key);
    @Transactional
    @Modifying
    @Query("update Token t set t.discordChatId = ?1 where t.key = ?2")
    int updateDiscordChatIdByKey(Long discordChatId, String key);
    @Transactional
    @Modifying
    @Query("update Token t set t.discordChatId = ?1")
    int updateDiscordChatIdBy(Long discordChatId);
    boolean existsByKey(String key);


}