package ua.nure.beautysalon.user.bootstrap;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ua.nure.beautysalon.user.service.UserService;

@Configuration
@RequiredArgsConstructor
public class AdminUserBootstrap {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserBootstrap.class);

    private final UserService userService;

    @Value("${admin.credentials.email}")
    private String adminEmail;

    @Value("${admin.credentials.password}")
    private String adminPassword;

    @Bean
    public CommandLineRunner createAdminUser() {
        return args -> {
            try {
                logger.info("Checking if admin user exists with email: {}", adminEmail);
                userService.findByEmail(adminEmail);
                logger.info("Admin user already exists with email: {}", adminEmail);
            } catch (IllegalArgumentException e) {
                try {
                    logger.info("Creating admin user with email: {}", adminEmail);
                    userService.signup(adminEmail, adminPassword, "ADMIN");
                    logger.info("Admin user created successfully with email: {}", adminEmail);

                    // Verify the user was created
                    try {
                        userService.findByEmail(adminEmail);
                        logger.info("Admin user verification successful");
                    } catch (Exception verifyError) {
                        logger.error("Admin user verification failed: {}", verifyError.getMessage());
                    }

                } catch (Exception createError) {
                    logger.error("Failed to create admin user: {}", createError.getMessage(), createError);
                }
            } catch (Exception e) {
                logger.error("Unexpected error during admin user bootstrap: {}", e.getMessage(), e);
            }
        };
    }
}