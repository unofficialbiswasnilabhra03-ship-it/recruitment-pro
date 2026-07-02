package com.recruitpro.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables Spring Data JPA auditing so {@code @CreatedDate} / {@code @LastModifiedDate}
 * on {@link com.recruitpro.audit.Auditable} populate automatically.
 *
 * No auditorAwareRef is pinned here on purpose: {@code @CreatedBy} / {@code @LastModifiedBy}
 * will simply stay null until an {@code AuditorAware<String>} bean is registered
 * (added in the security/JWT phase, since it reads the current user from
 * SecurityContextHolder). Spring Data JPA does not fail to start without it -
 * it just skips the "by" fields until that bean exists.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {
}
