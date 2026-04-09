package za.co.pacifish.identity_and_tenant_service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import za.co.pacifish.identity_and_tenant_service.domain.TenantUser;
import za.co.pacifish.identity_and_tenant_service.dto.CreateTenantUserRequest;
import za.co.pacifish.identity_and_tenant_service.service.TenantUserService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/tenantUsers")
@RequiredArgsConstructor
public class TenantUserController {

    private final TenantUserService tenantUserService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<TenantUser> createTenantUser(
        @RequestBody CreateTenantUserRequest request) {

        return tenantUserService.createTenantUser(request)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<TenantUser>> getAllTenantUsers() {
        return ResponseEntity.ok(tenantUserService.findAllByTenantId());
    }

}
