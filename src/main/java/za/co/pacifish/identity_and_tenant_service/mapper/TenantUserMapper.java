package za.co.pacifish.identity_and_tenant_service.mapper;

import za.co.pacifish.identity_and_tenant_service.domain.Tenant;
import za.co.pacifish.identity_and_tenant_service.domain.TenantUser;
import za.co.pacifish.identity_and_tenant_service.dto.CreateTenantUserRequest;
import za.co.pacifish.identity_and_tenant_service.enumeration.TenantInvitationStatus;

public final class TenantUserMapper {

    public static TenantUser toEntity(
        CreateTenantUserRequest dto, Tenant tenant) {
        return TenantUser.builder()
            .firebaseUid(dto.firebaseUid())
            .name(dto.name())
            .email(dto.email())
            .role(dto.role())
            .tenant(tenant)
            .active(true)
            .build();
    }
}
