package com.centaury.matchleague.utils;

import com.linecorp.bot.client.LineClientConstants;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.client.LineSignatureValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource("classpath:application.properties")
public class Config {

    @Autowired
    private Environment mEnv;

    @Bean(name = "com.linecorp.channel_secret")
    public String getChannelSecret() {
        return mEnv.getProperty("com.linecorp.channel_secret");
    }

    @Bean(name = "com.linecorp.channel_access_token")
    public String getChannelAccessToken() {
        return mEnv.getProperty("com.linecorp.channel_access_token");
    }

    @Bean(name = "lineMessagingClient")
    public LineMessagingClient getLineMessagingClient() {
        return LineMessagingClient
                .builder(getChannelAccessToken())
                .connectTimeout(LineClientConstants.DEFAULT_CONNECT_TIMEOUT_MILLIS)
                .readTimeout(LineClientConstants.DEFAULT_READ_TIMEOUT_MILLIS)
                .writeTimeout(LineClientConstants.DEFAULT_WRITE_TIMEOUT_MILLIS)
                .build();
    }

    @Bean(name = "lineSignatureValidator")
    public LineSignatureValidator getSignatureValidator() {
        return new LineSignatureValidator(getChannelSecret().getBytes());
    }

}
