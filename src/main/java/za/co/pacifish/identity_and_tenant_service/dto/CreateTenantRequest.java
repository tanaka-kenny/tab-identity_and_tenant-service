package za.co.pacifish.identity_and_tenant_service.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateTenantRequest(
    @NotBlank(
        message = "Tenant name must not be blank"
    )
    String name
) {
}
