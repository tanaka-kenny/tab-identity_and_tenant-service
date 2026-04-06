package za.co.pacifish.identity_and_tenant_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import za.co.pacifish.identity_and_tenant_service.domain.Tenant;
import za.co.pacifish.identity_and_tenant_service.dto.CreateTenantRequest;
import za.co.pacifish.identity_and_tenant_service.repository.TenantRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class TenantService {
    private final TenantRepository tenantRepository;

    public Tenant createTenant(CreateTenantRequest request, String ownerFirebaseUid) {
        log.info("Creating tenant with name: {}", request.name());
        Tenant tenant = Tenant.builder()
            .name(request.name())
            .ownerFirebaseUid(ownerFirebaseUid)
            .build();

        return tenantRepository.save(tenant);
    }

    public Optional<Tenant> getTenantById(String tenantId) {
        return tenantRepository.findById(UUID.fromString(tenantId));
    }

    public List<Tenant> getUserTenants(String ownerFirebaseUid) {
        return tenantRepository.findByOwnerFirebaseUid(ownerFirebaseUid);
    }

}
