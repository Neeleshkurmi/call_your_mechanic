package com.nilesh.cym.location.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigManager {

    @Bean
    public ObjectMapper getObjectMapper(){
        return new ObjectMapper();
    }
}
