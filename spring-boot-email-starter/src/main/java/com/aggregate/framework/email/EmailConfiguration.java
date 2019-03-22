package com.aggregate.framework.email;

import com.aggregate.framework.email.config.EmailConfig;
import com.aggregate.framework.email.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
@EnableConfigurationProperties(EmailConfig.class)
public class EmailConfiguration {

    @Bean
    public MailService mailService() {
        return new MailService();
    }
}