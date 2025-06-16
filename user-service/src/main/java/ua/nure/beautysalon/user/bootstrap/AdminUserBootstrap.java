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
                userService.findByEmail(adminEmail);
                logger.info("Admin user already exists with email: {}", adminEmail);
            } catch (IllegalArgumentException e) {
                userService.signup(adminEmail, adminPassword, "ADMIN");
                logger.info("Admin user created");
            }
        };
    }
}