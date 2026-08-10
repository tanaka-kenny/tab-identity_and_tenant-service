package za.co.pacifish.identity_and_tenant_service.dto.external_api;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.Map;

@Builder
public record NotificationRequest(
    @NotBlank String channel,
    @NotBlank String recipient,
    @NotBlank String template,
    @NotNull Map<String, Object> variables
) {
}
