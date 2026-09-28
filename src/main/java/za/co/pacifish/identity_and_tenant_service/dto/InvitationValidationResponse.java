package za.co.pacifish.identity_and_tenant_service.dto;

import za.co.pacifish.identity_and_tenant_service.enumeration.Role;

import java.time.LocalDateTime;

public record InvitationValidationResponse(
    String email,
    Role role,
    String tenantName,
    LocalDateTime expiration
) {
}
