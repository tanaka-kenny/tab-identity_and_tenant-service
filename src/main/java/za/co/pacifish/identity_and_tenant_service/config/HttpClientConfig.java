package za.co.pacifish.identity_and_tenant_service.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;
import za.co.pacifish.identity_and_tenant_service.service.NotificationHttpExchange;

@Configuration
@ImportHttpServices(group = "notification-service", types = {NotificationHttpExchange.class})
@RequiredArgsConstructor
public class HttpClientConfig {

    @Value("${tab-services.notifications.base-url}")
    private String notificationServiceUrl;

    @Bean
    public RestClientHttpServiceGroupConfigurer groupConfigurer() {

        return groups -> groups.filterByName("notification-service")
            .forEachClient((group, builder) -> builder
                .baseUrl(notificationServiceUrl));
    }


}

