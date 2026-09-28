package za.co.pacifish.identity_and_tenant_service.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import za.co.pacifish.identity_and_tenant_service.dto.AuthUserDetails;

import java.util.Objects;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthContextUtils {

    public static AuthUserDetails userDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (Objects.isNull(authentication)) {
            log.error("Authentication not found in SecurityContext");
            throw new IllegalStateException("User is not authenticated");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof AuthUserDetails)) {
            log.error("User details not found in auth context: principal is not a FirebaseUserDetailsDto");
            throw new IllegalStateException("User details not found in auth context");
        }

        return (AuthUserDetails) principal;
    }
}
