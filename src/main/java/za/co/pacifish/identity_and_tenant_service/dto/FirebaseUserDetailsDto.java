package za.co.pacifish.identity_and_tenant_service.dto;

public record FirebaseUserDetailsDto(
    String email,
    String firebaseUid,
    String tenantId
) {
}