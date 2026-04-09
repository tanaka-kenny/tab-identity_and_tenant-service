package za.co.pacifish.identity_and_tenant_service.dto;

import za.co.pacifish.identity_and_tenant_service.enumeration.Role;

public record FirebaseUserDetailsDto(
    String email,
    String firebaseUid,
    String tenantId,
    Role role
) {
}