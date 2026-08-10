package za.co.pacifish.identity_and_tenant_service.service;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import za.co.pacifish.identity_and_tenant_service.dto.external_api.NotificationRequest;

@HttpExchange("/api/notifications")
public interface NotificationHttpExchange {

    @PostExchange("/send")
    void sendEmail(@RequestBody NotificationRequest request);
}
