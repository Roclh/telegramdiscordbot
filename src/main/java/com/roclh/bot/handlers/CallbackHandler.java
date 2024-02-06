package com.roclh.bot.handlers;

import com.roclh.bot.callback.CallbackFunction;
import com.roclh.bot.callback.CallbackType;
import com.roclh.bot.utils.JsonHandler;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import com.roclh.bot.callback.Callback;

import java.util.List;
import java.util.Map;

@Component
public class CallbackHandler {
    private final Map<CallbackType, CallbackFunction> callbacks;

    public CallbackHandler() {
        this.callbacks = Map.of();
    }

    public SendMessage handleCallbacks(Update update) {
        List<String> list = JsonHandler.toList(update.getCallbackQuery().getData());
        long chatId = update.getCallbackQuery().getMessage().getChatId();

        SendMessage answer;
        if (list.isEmpty()) {
            answer = new SendMessage(String.valueOf(chatId), "Internal Error");
        } else {
            Callback callback = Callback.builder().callbackType(CallbackType.valueOf(list.get(0))).data(list.get(1)).build();
            CallbackFunction callbackBiFunction = callbacks.get(callback.getCallbackType());
            answer = callbackBiFunction.apply(callback, update);
        }

        return answer;
    }
}
