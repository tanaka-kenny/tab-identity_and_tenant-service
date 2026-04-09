package za.co.pacifish.identity_and_tenant_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;
import za.co.pacifish.identity_and_tenant_service.enumeration.Role;

public record CreateTenantUserRequest(
    @NotBlank String firebaseUid,
    @NotBlank String name,
    @NotBlank String email,
    @NonNull Role role
) {
}
