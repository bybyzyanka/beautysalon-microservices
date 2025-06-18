package ua.nure.beautysalon.user.bootstrap;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ua.nure.beautysalon.user.repository.UserRepository;
import ua.nure.beautysalon.user.service.UserService;

@Configuration
@RequiredArgsConstructor
public class AdminUserBootstrap {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserBootstrap.class);

    private final UserService userService;
    private final UserRepository userRepository;

    @Value("${admin.credentials.email}")
    private String adminEmail;

    @Value("${admin.credentials.password}")
    private String adminPassword;

    @Bean
    public CommandLineRunner createAdminUser() {
        return args -> {
            try {
                logger.info("Checking if admin user exists with email: {}", adminEmail);

                // Check if admin user already exists using repository
                if (userRepository.existsByEmail(adminEmail)) {
                    logger.info("Admin user already exists with email: {}", adminEmail);
                    return; // Exit early if user exists
                }

                // Create admin user only if it doesn't exist
                logger.info("Admin user not found. Creating admin user with email: {}", adminEmail);
                userService.signup(adminEmail, adminPassword, "ADMIN");
                logger.info("Admin user created successfully with email: {}", adminEmail);

                // Verify the user was created
                if (userRepository.existsByEmail(adminEmail)) {
                    logger.info("Admin user verification successful - user exists in database");
                } else {
                    logger.error("Admin user verification failed - user not found in database after creation");
                }

            } catch (Exception e) {
                logger.error("Unexpected error during admin user bootstrap: {}", e.getMessage(), e);
            }
        };
    }
}