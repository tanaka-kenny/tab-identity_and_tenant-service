package za.co.pacifish.identity_and_tenant_service.dto;

import za.co.pacifish.identity_and_tenant_service.enumeration.Role;

import java.util.List;

public record AuthUserDetails(
    String email,
    String firebaseUid,
    String userId,
    List<Role> roles
) {
}