package kkashin.dev.eventmanager.security.admin;

import kkashin.dev.eventmanager.model.entity.UserEntity;
import kkashin.dev.eventmanager.model.enums.UserRole;
import kkashin.dev.eventmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class SeedAdmin implements ApplicationRunner {
    private final AdminProperties adminProperties;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        String loginNormalized = adminProperties.login().trim().toLowerCase(Locale.ROOT);

        if (!userRepository.existsByLoginNormalized(loginNormalized)) {
            var admin = new UserEntity();

            admin.setLogin(adminProperties.login());
            admin.setLoginNormalized(loginNormalized);
            admin.setPasswordHash(passwordEncoder.encode(adminProperties.password()));
            admin.setRole(UserRole.ADMIN);
            admin.setAge(42);

            userRepository.save(admin);
        }
    }
}
