package za.co.pacifish.identity_and_tenant_service.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import za.co.pacifish.identity_and_tenant_service.domain.Tenant;
import za.co.pacifish.identity_and_tenant_service.domain.TenantInvitation;
import za.co.pacifish.identity_and_tenant_service.domain.TenantUser;
import za.co.pacifish.identity_and_tenant_service.dto.AcceptInvitationRequest;
import za.co.pacifish.identity_and_tenant_service.dto.InvitationValidationResponse;
import za.co.pacifish.identity_and_tenant_service.enumeration.Role;
import za.co.pacifish.identity_and_tenant_service.enumeration.TenantInvitationStatus;
import za.co.pacifish.identity_and_tenant_service.repository.TenantInvitationRepository;
import za.co.pacifish.identity_and_tenant_service.repository.TenantRepository;
import za.co.pacifish.identity_and_tenant_service.repository.TenantUserRepository;
import za.co.pacifish.identity_and_tenant_service.utils.TokenUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantUserServiceTest {

    @Mock
    private TenantUserRepository tenantUserRepository;

    @Mock
    private TenantInvitationRepository invitationRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private FirebaseAuth firebaseAuth;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TenantUserService tenantUserService;

    @Test
    void validateInvitation_shouldReturnInviteDetailsWhenTokenIsValid() {
        String token = "valid-token";
        Tenant tenant = Tenant.builder().id(UUID.randomUUID()).name("Tenant A").build();
        TenantInvitation invitation = TenantInvitation.builder()
            .id(UUID.randomUUID())
            .email("invitee@tab.test")
            .tokenHash(TokenUtils.hashToken(token))
            .role(Role.USER)
            .status(TenantInvitationStatus.PENDING)
            .expiration(LocalDateTime.now().plusDays(2))
            .tenant(tenant)
            .build();
        when(invitationRepository.findByTokenHash(TokenUtils.hashToken(token)))
            .thenReturn(Optional.of(invitation));

        InvitationValidationResponse result = tenantUserService.validateInvitation(token);

        assertEquals("invitee@tab.test", result.email());
        assertEquals(Role.USER, result.role());
        assertEquals("Tenant A", result.tenantName());
        assertEquals(invitation.getExpiration(), result.expiration());
    }

    @Test
    void validateInvitation_shouldExpireAndThrowWhenTokenIsExpired() {
        String token = "expired-token";
        Tenant tenant = Tenant.builder().id(UUID.randomUUID()).name("Tenant A").build();
        TenantInvitation invitation = TenantInvitation.builder()
            .id(UUID.randomUUID())
            .email("invitee@tab.test")
            .tokenHash(TokenUtils.hashToken(token))
            .role(Role.USER)
            .status(TenantInvitationStatus.PENDING)
            .expiration(LocalDateTime.now().minusMinutes(1))
            .tenant(tenant)
            .build();
        when(invitationRepository.findByTokenHash(TokenUtils.hashToken(token)))
            .thenReturn(Optional.of(invitation));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            tenantUserService.validateInvitation(token));
        assertEquals("Invitation token is invalid, expired, or already used", ex.getMessage());
        assertEquals(TenantInvitationStatus.EXPIRED, invitation.getStatus());
        verify(invitationRepository).save(invitation);
    }

    @Test
    void acceptInvite_shouldCreateTenantUserAndMarkInvitationAccepted() throws FirebaseAuthException {
        String token = "accept-token";
        UUID tenantId = UUID.randomUUID();
        Tenant tenant = Tenant.builder().id(tenantId).name("Tenant A").build();
        TenantInvitation invitation = TenantInvitation.builder()
            .id(UUID.randomUUID())
            .email("invitee@tab.test")
            .tokenHash(TokenUtils.hashToken(token))
            .role(Role.WAITER)
            .status(TenantInvitationStatus.PENDING)
            .expiration(LocalDateTime.now().plusDays(1))
            .tenant(tenant)
            .build();

        when(invitationRepository.findByTokenHash(TokenUtils.hashToken(token)))
            .thenReturn(Optional.of(invitation));
        when(tenantUserRepository.existsByEmailIgnoreCaseAndTenant_Id("invitee@tab.test", tenantId))
            .thenReturn(false);
        when(tenantUserRepository.save(any(TenantUser.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        AcceptInvitationRequest request = new AcceptInvitationRequest(
            token, "firebase-uid-123", "John Smith", "invitee@tab.test");

        TenantUser result = tenantUserService.acceptInvite(request);

        assertEquals("firebase-uid-123", result.getUserId());
        assertEquals("John Smith", result.getName());
        assertEquals("invitee@tab.test", result.getEmail());
        assertEquals(Role.WAITER, result.getRole());
        assertEquals(TenantInvitationStatus.ACCEPTED, invitation.getStatus());
        assertEquals("firebase-uid-123", invitation.getUserId());
        assertNotNull(invitation.getAcceptedAt());

        ArgumentCaptor<Map<String, Object>> claimsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(firebaseAuth).setCustomUserClaims(eq("firebase-uid-123"), claimsCaptor.capture());
        assertEquals(tenantId.toString(), claimsCaptor.getValue().get("tenantId"));
        assertEquals(java.util.List.of("WAITER"), claimsCaptor.getValue().get("roles"));
        verify(invitationRepository).save(invitation);
    }

    @Test
    void acceptInvite_shouldThrowWhenRequestEmailDoesNotMatchInvitationEmail() {
        String token = "token-mismatch";
        Tenant tenant = Tenant.builder().id(UUID.randomUUID()).name("Tenant A").build();
        TenantInvitation invitation = TenantInvitation.builder()
            .id(UUID.randomUUID())
            .email("invitee@tab.test")
            .tokenHash(TokenUtils.hashToken(token))
            .role(Role.USER)
            .status(TenantInvitationStatus.PENDING)
            .expiration(LocalDateTime.now().plusDays(1))
            .tenant(tenant)
            .build();
        when(invitationRepository.findByTokenHash(TokenUtils.hashToken(token)))
            .thenReturn(Optional.of(invitation));

        AcceptInvitationRequest request = new AcceptInvitationRequest(
            token, "firebase-uid-123", "John Smith", "different@tab.test");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            tenantUserService.acceptInvite(request));
        assertEquals("Provided email does not match the invitation", ex.getMessage());
        verifyNoInteractions(firebaseAuth);
        verify(invitationRepository, never()).save(invitation);
    }
}
