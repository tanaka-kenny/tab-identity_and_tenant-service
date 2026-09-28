package za.co.pacifish.identity_and_tenant_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AcceptInvitationRequest(
    @NotBlank String token,
    @NotBlank String userId,
    @NotBlank String name,
    @NotBlank @Email String email
) {
}
