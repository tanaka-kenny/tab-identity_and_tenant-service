package za.co.pacifish.identity_and_tenant_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import za.co.pacifish.identity_and_tenant_service.domain.TenantSettings;

public record TenantRequest(
    @NotBlank(
        message = "Tenant name must not be blank"
    )
    String name,
    @NotNull Boolean useDefaultSettings,
    TenantSettings settings
) {
}
