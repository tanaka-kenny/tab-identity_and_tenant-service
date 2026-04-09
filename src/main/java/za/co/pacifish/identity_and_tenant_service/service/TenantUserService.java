package za.co.pacifish.identity_and_tenant_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import za.co.pacifish.identity_and_tenant_service.domain.TenantUser;
import za.co.pacifish.identity_and_tenant_service.dto.CreateTenantUserRequest;
import za.co.pacifish.identity_and_tenant_service.dto.FirebaseUserDetailsDto;
import za.co.pacifish.identity_and_tenant_service.mapper.TenantUserMapper;
import za.co.pacifish.identity_and_tenant_service.repository.TenantRepository;
import za.co.pacifish.identity_and_tenant_service.repository.TenantUserRepository;
import za.co.pacifish.identity_and_tenant_service.utils.AuthContextUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantUserService {

    private final TenantUserRepository tenantUserRepository;
    private final TenantRepository tenantRepository;

    public Optional<TenantUser> createTenantUser(CreateTenantUserRequest request) {
        String tenantId = AuthContextUtils.userDetails().tenantId();
        return tenantRepository.findById(UUID.fromString(tenantId)).map(
            tenant -> {
                TenantUser tenantUser = TenantUserMapper.toEntity(request, tenant);
                log.info("Creating new tenant user for tenant: {}", tenant.getId());
                return tenantUserRepository.save(tenantUser);
            }
        );
    }

    public List<TenantUser> findAllByTenantId() {
        String tenantId = AuthContextUtils.userDetails().tenantId();
        log.info("Finding all tenants for tenant: {}", tenantId);
        return tenantUserRepository.findAllByTenantId(UUID.fromString(tenantId));
    }


}
