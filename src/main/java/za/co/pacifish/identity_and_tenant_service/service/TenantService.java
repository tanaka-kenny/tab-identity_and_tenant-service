package za.co.pacifish.identity_and_tenant_service.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import za.co.pacifish.identity_and_tenant_service.domain.Tenant;
import za.co.pacifish.identity_and_tenant_service.domain.TenantSettings;
import za.co.pacifish.identity_and_tenant_service.dto.TenantRequest;
import za.co.pacifish.identity_and_tenant_service.dto.CreateTenantUserRequest;
import za.co.pacifish.identity_and_tenant_service.dto.FirebaseUserDetailsDto;
import za.co.pacifish.identity_and_tenant_service.enumeration.Role;
import za.co.pacifish.identity_and_tenant_service.mapper.TenantMapper;
import za.co.pacifish.identity_and_tenant_service.repository.TenantRepository;
import za.co.pacifish.identity_and_tenant_service.utils.AuthContextUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class TenantService {
    private final TenantRepository tenantRepository;
    private final TenantUserService tenantUserService;

    @Transactional
    public Tenant createTenant(TenantRequest request, String ownerFirebaseUid) {
        log.info("Creating tenant with name: {}", request.name());
        Tenant tenant = TenantMapper.ofDefaultSettings(request, ownerFirebaseUid);

        tenant = tenantRepository.save(tenant);

        log.info("Creating default tenant user for tenant: {}", tenant.getId());
        FirebaseUserDetailsDto userDetails = AuthContextUtils.userDetails();
        tenantUserService.createTenantUser(
            new CreateTenantUserRequest(
                ownerFirebaseUid, userDetails.email(), userDetails.email(), Role.ADMIN), tenant);

        return tenant;
    }

    public Tenant updateTenant(UUID id, TenantRequest request) {
        log.info("Updating tenant with id: {}", id);

        Tenant tenant = tenantRepository.findById(id).orElseThrow(
            () -> new IllegalArgumentException("Tenant with id " + id + " does not exist"));

        if (request.useDefaultSettings()) {
            tenant.setSettings(TenantMapper.defaultSettings());
        } else {
            TenantSettings settings = Objects.requireNonNull(
                request.settings(), "Settings cannot be null when useDefaultSettings is false");
            tenant.setSettings(settings);
        }

        log.info("Updating default tenant user for tenant: {}", tenant.getId());
        return tenantRepository.save(tenant);
    }

    public Optional<Tenant> getTenantById(String tenantId) {
        return tenantRepository.findById(UUID.fromString(tenantId));
    }

    public List<Tenant> getUserTenants(String ownerFirebaseUid) {
        return tenantRepository.findByOwnerFirebaseUid(ownerFirebaseUid);
    }

}
