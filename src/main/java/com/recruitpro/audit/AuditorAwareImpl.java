package com.recruitpro.audit;

import com.recruitpro.security.UserPrincipal;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Provides the current user's email to Spring Data JPA auditing so that
 * {@code @CreatedBy} and {@code @LastModifiedBy} on {@link Auditable} are
 * auto-populated on every save.
 *
 * Registered as "auditorProvider" so that {@link com.recruitpro.config.JpaAuditingConfig}
 * can reference it via {@code @EnableJpaAuditing(auditorAwareRef = "auditorProvider")}.
 */
@Component("auditorProvider")
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()
                || auth.getPrincipal().equals("anonymousUser")) {
            // System operations (scheduler, data initializer) fall back to "SYSTEM"
            return Optional.of("SYSTEM");
        }

        if (auth.getPrincipal() instanceof UserPrincipal principal) {
            return Optional.of(principal.getEmail());
        }

        return Optional.of(auth.getName());
    }
}
