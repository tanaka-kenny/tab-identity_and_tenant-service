package za.co.pacifish.identity_and_tenant_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import za.co.pacifish.identity_and_tenant_service.dto.app_events.InvitationNotificationEvent;
import za.co.pacifish.identity_and_tenant_service.dto.external_api.NotificationRequest;
import za.co.pacifish.identity_and_tenant_service.enumeration.TenantInvitationStatus;
import za.co.pacifish.identity_and_tenant_service.repository.TenantInvitationRepository;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TenantUserServiceEventListener {

    private final NotificationHttpExchange notificationHttpExchange;
    private final TenantInvitationRepository invitationRepository;

    @Value("${tab-services.admin-frontend.base-url}")
    private String adminFrontendBaseUrl;

    @Async(value = "taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendInvitationNotification(InvitationNotificationEvent data) {
        Map<String, Object> variables = Map.of(
            "inviter_name", data.inviterName(),
            "organisation", data.tenantName(),
            "inviteLink", String.format("%s/profile/invitation?token=%s", adminFrontendBaseUrl, data.invitationToken())
        );

        NotificationRequest request = NotificationRequest.builder()
            .channel("EMAIL")
            .recipient(data.recipient())
            .template("tab-invitation-email.ftl")
            .variables(variables)
            .build();

        try {
            notificationHttpExchange.sendEmail(request);
        } catch (Exception ex) {
            log.error("Error sending email", ex);

            invitationRepository.findById(data.invitationId()).ifPresent(invitation -> {
                invitation.setStatus(TenantInvitationStatus.NOTIFICATION_SEND_FAILED);
                invitationRepository.save(invitation);
            });

        }
    }
}
