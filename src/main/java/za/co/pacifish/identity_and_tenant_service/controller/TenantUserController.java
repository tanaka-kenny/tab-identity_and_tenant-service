package za.co.pacifish.identity_and_tenant_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import za.co.pacifish.identity_and_tenant_service.domain.TenantInvitation;
import za.co.pacifish.identity_and_tenant_service.domain.TenantUser;
import za.co.pacifish.identity_and_tenant_service.dto.AcceptInvitationRequest;
import za.co.pacifish.identity_and_tenant_service.dto.InviteUserRequest;
import za.co.pacifish.identity_and_tenant_service.service.TenantUserService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/tenantUsers")
@RequiredArgsConstructor
public class TenantUserController {

    private final TenantUserService tenantUserService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/invite")
    public ResponseEntity<TenantInvitation> inviteUser(
        @Valid @RequestBody InviteUserRequest request) {
        return  ResponseEntity.ok(tenantUserService.inviteUser(request));
    }

    @PostMapping("/accept-invite")
    public ResponseEntity<TenantUser> acceptInvite(
        @Valid @RequestBody AcceptInvitationRequest request
        ) {
        return ResponseEntity.ok(tenantUserService.acceptInvite(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<TenantUser>> getAllTenantUsers() {
        return ResponseEntity.ok(tenantUserService.findAllByTenantId());
    }

}
