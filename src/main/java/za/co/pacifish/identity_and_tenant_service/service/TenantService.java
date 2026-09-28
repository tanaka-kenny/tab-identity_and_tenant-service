package za.co.pacifish.identity_and_tenant_service.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import za.co.pacifish.identity_and_tenant_service.domain.Tenant;
import za.co.pacifish.identity_and_tenant_service.domain.TenantSettings;
import za.co.pacifish.identity_and_tenant_service.dto.TenantRequest;
import za.co.pacifish.identity_and_tenant_service.dto.CreateTenantUserRequest;
import za.co.pacifish.identity_and_tenant_service.dto.AuthUserDetails;
import za.co.pacifish.identity_and_tenant_service.enumeration.Role;
import za.co.pacifish.identity_and_tenant_service.mapper.TenantMapper;
import za.co.pacifish.identity_and_tenant_service.repository.TenantRepository;
import za.co.pacifish.identity_and_tenant_service.utils.AuthContextUtils;

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
    public Tenant createTenant(TenantRequest request, String userId) {
        log.info("Creating tenant with name: {}", request.name());

        tenantRepository.findByUserId(userId)
            .ifPresent(existingTenant -> {
            throw new IllegalStateException("User with User ID " + userId + " already has a tenant with id " + existingTenant.getId());
        });

        Tenant tenant = TenantMapper.ofDefaultSettings(request, userId);

        tenant = tenantRepository.save(tenant);

        log.info("Creating default tenant user for tenant: {}", tenant.getId());
        AuthUserDetails userDetails = AuthContextUtils.userDetails();
        tenantUserService.createTenantUser(
            new CreateTenantUserRequest(
                userId, userDetails.email(), userDetails.email(), Role.ADMIN), tenant);

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
        tenant.setName(request.name());
        return tenantRepository.save(tenant);
    }

    public Optional<Tenant> getTenantById(String tenantId) {
        return tenantRepository.findById(UUID.fromString(tenantId));
    }


}
