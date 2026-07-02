package com.recruitpro.config;

import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot auto-configures {@code JavaMailSender} from the
 * {@code spring.mail.*} properties in application-dev/prod.properties.
 * No manual bean definition is needed here.
 *
 * This class is kept as a placeholder so the folder structure matches the spec
 * and can be extended later (e.g. to add a custom MailSender with retry logic
 * or to swap in a different provider such as SendGrid or AWS SES).
 */
@Configuration
public class MailConfig {
    // JavaMailSender is auto-configured by spring-boot-starter-mail
}
