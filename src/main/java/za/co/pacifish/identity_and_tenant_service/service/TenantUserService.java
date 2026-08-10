package za.co.pacifish.identity_and_tenant_service.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import za.co.pacifish.identity_and_tenant_service.domain.Tenant;
import za.co.pacifish.identity_and_tenant_service.domain.TenantInvitation;
import za.co.pacifish.identity_and_tenant_service.domain.TenantUser;
import za.co.pacifish.identity_and_tenant_service.dto.AcceptInvitationRequest;
import za.co.pacifish.identity_and_tenant_service.dto.CreateTenantUserRequest;
import za.co.pacifish.identity_and_tenant_service.dto.FirebaseUserDetailsDto;
import za.co.pacifish.identity_and_tenant_service.dto.InviteUserRequest;
import za.co.pacifish.identity_and_tenant_service.dto.app_events.InvitationNotificationEvent;
import za.co.pacifish.identity_and_tenant_service.enumeration.TenantInvitationStatus;
import za.co.pacifish.identity_and_tenant_service.exception.ExternalServiceException;
import za.co.pacifish.identity_and_tenant_service.mapper.TenantUserMapper;
import za.co.pacifish.identity_and_tenant_service.repository.TenantInvitationRepository;
import za.co.pacifish.identity_and_tenant_service.repository.TenantRepository;
import za.co.pacifish.identity_and_tenant_service.repository.TenantUserRepository;
import za.co.pacifish.identity_and_tenant_service.utils.AuthContextUtils;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantUserService {

    private final TenantUserRepository tenantUserRepository;
    private final TenantInvitationRepository invitationRepository;
    private final TenantRepository tenantRepository;
    private final FirebaseAuth firebaseAuth;
    private final ApplicationEventPublisher eventPublisher;


    @Transactional
    public TenantInvitation inviteUser(InviteUserRequest request) {
        FirebaseUserDetailsDto userDetails = AuthContextUtils.userDetails();
        var tenantId = userDetails.tenantId();

        log.info("Creating invite for tenant {}", tenantId);
        tenantUserRepository.findByEmailAndTenantId(request.email(), UUID.fromString(tenantId))
            .ifPresent(_ -> {
                throw new IllegalStateException("User with email " + request.email() + " already exists in tenant  ");
            });

        Tenant tenant = tenantRepository.findById(UUID.fromString(tenantId)).orElseThrow(
            () -> new IllegalArgumentException("Tenant with does not exist"));

        TenantInvitation invitation = TenantInvitation.builder()
            .email(request.email())
            .role(request.role())
            .status(TenantInvitationStatus.PENDING)
            .tenant(tenant)
            .build();

        invitation = invitationRepository.save(invitation);

        eventPublisher.publishEvent(new InvitationNotificationEvent(
            invitation.getEmail(),
            invitation.getId(),
            tenant.getName(),
            userDetails.email()
        ));

        return invitation;
    }

    @Transactional
    public TenantUser acceptInvite(AcceptInvitationRequest request) {
        TenantInvitation invitation = invitationRepository.findById(request.invitationId())
            .orElseThrow(() -> new IllegalArgumentException("Invitation with id " + request.invitationId() + " does not exist"));

        if (TenantInvitationStatus.PENDING.equals(invitation.getStatus())) {
            throw new IllegalStateException("Invitation already processed");
        }

        log.info("Invitation with id {} has been accepted", request.invitationId());
        CreateTenantUserRequest userRequest = new CreateTenantUserRequest(
            request.firebaseUid(), invitation.getEmail(), invitation.getEmail(), invitation.getRole());
        TenantUser tenantUser = createTenantUser(
            userRequest, invitation.getTenant());

        invitation.setStatus(TenantInvitationStatus.ACCEPTED);
        invitationRepository.save(invitation);
        log.info("Created user: {} for invitation: {}", tenantUser.getId(), invitation.getId());
        return tenantUser;
    }

    public TenantUser createTenantUser(
        CreateTenantUserRequest request, Tenant tenant) {

        try {
            Map<String, Object> claims = new HashMap<>();
            claims.put("tenantId", tenant.getId().toString());
            claims.put("roles", List.of(request.role().name()));

            firebaseAuth.setCustomUserClaims(request.firebaseUid(), claims);

            TenantUser tenantUser = TenantUserMapper.toEntity(
                request, tenant);
            log.info("Creating new tenant user for tenant: {}", tenant.getId());
            tenantUser = tenantUserRepository.save(tenantUser);

            return tenantUser;
        } catch (FirebaseAuthException ex) {
            log.error("Failed to update Firebase user: {} Auth claims: {}", request.firebaseUid(), ex.getMessage());
            throw new ExternalServiceException("An internal error occurred while trying to create tenant user. Please try again later");
        }
    }

    public List<TenantUser> findAllByTenantId() {
        String tenantId = AuthContextUtils.userDetails().tenantId();
        log.info("Finding all tenant users for tenant: {}", tenantId);
        return tenantUserRepository.findAllByTenant_Id(UUID.fromString(tenantId));
    }


}
