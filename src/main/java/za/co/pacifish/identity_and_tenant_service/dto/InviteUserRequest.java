package za.co.pacifish.identity_and_tenant_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import za.co.pacifish.identity_and_tenant_service.enumeration.Role;

public record InviteUserRequest(
    @NotBlank @Email String email,
    @NotNull Role role
) {
}
