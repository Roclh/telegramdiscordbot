package com.roclh.bot.utils;

public interface TokenPair<T, V> {

    T getTelegram();

    V getDiscord();

    TokenPair<T, V> setTelegram(T chatId);

    TokenPair<T, V> setDiscord(T chatId);
}
