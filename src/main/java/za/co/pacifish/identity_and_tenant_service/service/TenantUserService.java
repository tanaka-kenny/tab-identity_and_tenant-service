package za.co.pacifish.identity_and_tenant_service.service;

import com.google.firebase.auth.FirebaseAuth;
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
import za.co.pacifish.identity_and_tenant_service.dto.AuthUserDetails;
import za.co.pacifish.identity_and_tenant_service.dto.InviteUserRequest;
import za.co.pacifish.identity_and_tenant_service.dto.InvitationValidationResponse;
import za.co.pacifish.identity_and_tenant_service.dto.app_events.InvitationNotificationEvent;
import za.co.pacifish.identity_and_tenant_service.enumeration.TenantInvitationStatus;
import za.co.pacifish.identity_and_tenant_service.mapper.TenantUserMapper;
import za.co.pacifish.identity_and_tenant_service.repository.TenantInvitationRepository;
import za.co.pacifish.identity_and_tenant_service.repository.TenantRepository;
import za.co.pacifish.identity_and_tenant_service.repository.TenantUserRepository;
import za.co.pacifish.identity_and_tenant_service.utils.AuthContextUtils;
import za.co.pacifish.identity_and_tenant_service.utils.TokenUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantUserService {

    private static final String INVALID_INVITATION_MESSAGE = "Invitation token is invalid, expired, or already used";

    private final TenantUserRepository tenantUserRepository;
    private final TenantInvitationRepository invitationRepository;
    private final TenantRepository tenantRepository;
    private final ApplicationEventPublisher eventPublisher;


    @Transactional
    public TenantInvitation inviteUser(
        UUID tenantId,
        InviteUserRequest request) {
        String normalizedEmail = normalizeEmail(request.email());


        log.info("Creating invite for tenant {}", tenantId);
        if (tenantUserRepository.existsByEmailIgnoreCaseAndTenant_Id(normalizedEmail, tenantId)) {
            throw new IllegalStateException("User with email " + normalizedEmail + " already exists in tenant");
        }

        if (invitationRepository.existsByEmailIgnoreCaseAndTenant_IdAndStatus(
            normalizedEmail, tenantId, TenantInvitationStatus.PENDING)) {
            throw new IllegalStateException("An active invitation already exists for this email");
        }

        Tenant tenant = tenantRepository.findById(tenantId).orElseThrow(
            () -> new IllegalArgumentException("Tenant with id " + tenantId + " does not exist"));

        String invitationToken = TokenUtils.generateToken();
        TenantInvitation invitation = TenantInvitation.builder()
            .email(normalizedEmail)
            .tokenHash(TokenUtils.hashToken(invitationToken))
            .role(request.role())
            .status(TenantInvitationStatus.PENDING)
            .tenant(tenant)
            .build();

        invitation = invitationRepository.save(invitation);

        AuthUserDetails userDetails = AuthContextUtils.userDetails();
        eventPublisher.publishEvent(new InvitationNotificationEvent(
            invitation.getEmail(),
            invitation.getId(),
            invitationToken,
            tenant.getName(),
            userDetails.email()
        ));

        return invitation;
    }

    public InvitationValidationResponse validateInvitation(String token) {
        TenantInvitation invitation = resolvePendingInvitation(token);
        return new InvitationValidationResponse(
            invitation.getEmail(),
            invitation.getRole(),
            invitation.getTenant().getName(),
            invitation.getExpiration()
        );
    }

    @Transactional
    public TenantUser acceptInvite(AcceptInvitationRequest request) {
        TenantInvitation invitation = resolvePendingInvitation(request.token());
        String normalizedEmail = normalizeEmail(request.email());

        if (!invitation.getEmail().equalsIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException("Provided email does not match the invitation");
        }

        if (tenantUserRepository.existsByEmailIgnoreCaseAndTenant_Id(normalizedEmail, invitation.getTenant().getId())) {
            throw new IllegalStateException("User with email " + normalizedEmail + " already exists in tenant");
        }

        log.info("Invitation with id {} has been accepted", invitation.getId());
        CreateTenantUserRequest userRequest = new CreateTenantUserRequest(
            request.userId(), request.name(), normalizedEmail, invitation.getRole());
        TenantUser tenantUser = createTenantUser(
            userRequest, invitation.getTenant());

        invitation.setStatus(TenantInvitationStatus.ACCEPTED);
        invitation.setAcceptedAt(LocalDateTime.now());
        invitation.setUserId(request.userId());
        invitationRepository.save(invitation);
        log.info("Created user: {} for invitation: {}", tenantUser.getId(), invitation.getId());
        return tenantUser;
    }

    public TenantUser createTenantUser(
        CreateTenantUserRequest request, Tenant tenant) {

        TenantUser tenantUser = TenantUserMapper.toEntity(
            request, tenant);
        log.info("Creating new tenant user for tenant: {}", tenant.getId());
        tenantUser = tenantUserRepository.save(tenantUser);

        return tenantUser;

    }

    public List<TenantUser> findAllByTenantId(UUID tenantId) {
        return tenantUserRepository.findAllByTenant_Id(tenantId);
    }

    private TenantInvitation resolvePendingInvitation(String token) {
        String hashedToken = TokenUtils.hashToken(token);
        TenantInvitation invitation = invitationRepository.findByTokenHash(hashedToken)
            .orElseThrow(() -> new IllegalArgumentException(INVALID_INVITATION_MESSAGE));

        if (TenantInvitationStatus.PENDING != invitation.getStatus()) {
            throw new IllegalStateException("Invitation already processed");
        }

        if (invitation.isExpired()) {
            invitation.setStatus(TenantInvitationStatus.EXPIRED);
            invitationRepository.save(invitation);
            throw new IllegalArgumentException(INVALID_INVITATION_MESSAGE);
        }

        return invitation;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

}
