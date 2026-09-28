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
import za.co.pacifish.identity_and_tenant_service.dto.InvitationValidationResponse;
import za.co.pacifish.identity_and_tenant_service.service.TenantUserService;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/tenantUsers")
@RequiredArgsConstructor
public class TenantUserController {

    private final TenantUserService tenantUserService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/invite/{tenantId}")
    public ResponseEntity<TenantInvitation> inviteUser(
        @PathVariable UUID tenantId,
        @Valid @RequestBody InviteUserRequest request) {
        return  ResponseEntity.ok(tenantUserService.inviteUser(tenantId, request));
    }

    @GetMapping("/invitations/validate")
    public ResponseEntity<InvitationValidationResponse> validateInvitation(
        @RequestParam String token
    ) {
        return ResponseEntity.ok(tenantUserService.validateInvitation(token));
    }

    @PostMapping("/invitations/accept")
    public ResponseEntity<TenantUser> acceptInvite(
        @Valid @RequestBody AcceptInvitationRequest request
        ) {
        return ResponseEntity.ok(tenantUserService.acceptInvite(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{tenantId}")
    public ResponseEntity<List<TenantUser>> getAllTenantUsers(
        @PathVariable UUID tenantId
    ) {
        return ResponseEntity.ok(tenantUserService.findAllByTenantId(tenantId));
    }

}
