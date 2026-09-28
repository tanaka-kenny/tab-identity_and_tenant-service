package za.co.pacifish.identity_and_tenant_service.filter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.json.JsonMapper;
import za.co.pacifish.identity_and_tenant_service.dto.AuthUserDetails;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FirebaseIdTokenFilter extends OncePerRequestFilter {
    private final JsonMapper objectMapper;
    private final FirebaseAuth firebaseAuth;

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.replace("Bearer ", "");
        try {
            AuthUserDetails userDetails = extractUserDetailsFromToken(token);

            List<SimpleGrantedAuthority> simpleGrantedAuthorities = new ArrayList<>();
            if (!userDetails.roles().isEmpty()) {
                simpleGrantedAuthorities = userDetails.roles().stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                    .toList();
            }

            var authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, simpleGrantedAuthorities);
            authenticationToken.setDetails(new WebAuthenticationDetails(request));

            SecurityContext newContext = SecurityContextHolder.createEmptyContext();
            newContext.setAuthentication(authenticationToken);
            SecurityContextHolder.setContext(newContext);

        } catch (FirebaseAuthException _) {
            setAuthErrorDetails(response);
        } finally {
            filterChain.doFilter(request, response);
        }

    }

    private AuthUserDetails extractUserDetailsFromToken(String token)
        throws FirebaseAuthException, ClassCastException, NullPointerException {
        FirebaseToken firebaseToken = firebaseAuth.verifyIdToken(token);
        String firebaseUid = String.valueOf(firebaseToken.getClaims().get("user_id"));
        String userId = String.valueOf(firebaseToken.getClaims().get("internal_user_id"));
        String email = firebaseToken.getEmail();


        return new AuthUserDetails(
            email, firebaseUid, userId, List.of());
    }

    private void setAuthErrorDetails(HttpServletResponse response) throws IOException {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            status, "Authentication failure: Token missing, invalid or expired");
        response.getWriter().write(objectMapper.writeValueAsString(problemDetail));
    }
}
