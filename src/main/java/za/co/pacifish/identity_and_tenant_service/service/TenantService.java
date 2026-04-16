package za.co.pacifish.identity_and_tenant_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import za.co.pacifish.identity_and_tenant_service.domain.Tenant;
import za.co.pacifish.identity_and_tenant_service.dto.CreateTenantRequest;
import za.co.pacifish.identity_and_tenant_service.dto.CreateTenantUserRequest;
import za.co.pacifish.identity_and_tenant_service.dto.FirebaseUserDetailsDto;
import za.co.pacifish.identity_and_tenant_service.enumeration.Role;
import za.co.pacifish.identity_and_tenant_service.enumeration.TenantStatus;
import za.co.pacifish.identity_and_tenant_service.repository.TenantRepository;
import za.co.pacifish.identity_and_tenant_service.utils.AuthContextUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class TenantService {
    private final TenantRepository tenantRepository;
    private final TenantUserService tenantUserService;

    public Tenant createTenant(CreateTenantRequest request, String ownerFirebaseUid) {
        log.info("Creating tenant with name: {}", request.name());
        Tenant tenant = Tenant.builder()
            .name(request.name())
            .ownerFirebaseUid(ownerFirebaseUid)
            .status(TenantStatus.ACTIVE)
            .build();

        tenant = tenantRepository.save(tenant);

        log.info("Creating default tenant user for tenant: {}", tenant.getId());
        FirebaseUserDetailsDto userDetails = AuthContextUtils.userDetails();
        tenantUserService.createTenantUser(new CreateTenantUserRequest(
            ownerFirebaseUid, userDetails.email(), userDetails.email(), Role.ADMIN
        ));

        return tenant;
    }

    public Optional<Tenant> getTenantById(String tenantId) {
        return tenantRepository.findById(UUID.fromString(tenantId));
    }

    public List<Tenant> getUserTenants(String ownerFirebaseUid) {
        return tenantRepository.findByOwnerFirebaseUid(ownerFirebaseUid);
    }

}
