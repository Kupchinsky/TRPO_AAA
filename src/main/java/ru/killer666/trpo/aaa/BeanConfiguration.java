package ru.killer666.trpo.aaa;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import ru.killer666.trpo.aaa.services.AccountingService;
import ru.killer666.trpo.aaa.services.AuthorizationService;

@Configuration
@ComponentScan
public class BeanConfiguration {
    @Bean
    AuthorizationService authorizationService() {
        return new AuthorizationService();
    }

    @Bean
    AccountingService accountingService() {
        return new AccountingService();
    }
}