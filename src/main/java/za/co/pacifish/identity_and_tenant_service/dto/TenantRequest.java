package za.co.pacifish.identity_and_tenant_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;
import za.co.pacifish.identity_and_tenant_service.domain.TenantSettings;

public record TenantRequest(
    @NotBlank(
        message = "Tenant name must not be blank"
    )
    String name,
    @NonNull Boolean useDefaultSettings,
    TenantSettings settings
) {
}
