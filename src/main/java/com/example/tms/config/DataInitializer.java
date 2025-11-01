package com.example.tms.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.tms.enity.User;
import com.example.tms.repository.UserRepository;

@Component
@Profile({"default"})
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        // Create a default admin if not present
        userRepository.findByUsername("admin").ifPresentOrElse(
                u -> {},
                () -> {
                    User user = new User();
                    user.setUsername("admin");
                    user.setUserPassword(passwordEncoder.encode("admin123"));
                    user.setIsLock(false);
                    user.setFullName("Administrator");
                    user.setEmail("admin@example.com");
                    user.setRole(User.Role.ADMIN);
                    userRepository.save(user);
                }
        );
    }
}
