package za.co.pacifish.identity_and_tenant_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AcceptInvitationRequest(
    @NotNull UUID invitationId,
    @NotBlank String firebaseUid
) {
}
