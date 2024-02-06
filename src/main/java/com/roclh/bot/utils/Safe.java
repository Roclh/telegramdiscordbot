package com.roclh.bot.utils;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

public class Safe {
    public static <S,T> S getFrom(T value, Extractor<S, T> extractor){
        return Optional.ofNullable(value).map(extractor::getFrom).orElse(null);
    }
    public interface Extractor<S, T>{
        S getFrom(T provider);
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor){
            Map<Object, Boolean> seen = new ConcurrentHashMap<>();
            return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
        }
}
