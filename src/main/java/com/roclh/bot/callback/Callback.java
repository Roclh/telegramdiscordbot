package com.roclh.bot.callback;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class Callback {

    private CallbackType callbackType;

    private String data;

}
