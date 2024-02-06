package com.roclh.bot.properties;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Scope("singleton")
public class PropertiesContainer {

    private final Map<String, String> properties = new HashMap<>();


    public String getProperty(String key){
        return properties.get(key);
    }

    public void setProperty(String key, String value){
        properties.put(key, value);
    }

    public void setProperty(String key, boolean value){
        properties.put(key, String.valueOf(value));
    }

    public boolean getBoolProperty(String key){
        return Boolean.parseBoolean(properties.get(key));
    }
}
