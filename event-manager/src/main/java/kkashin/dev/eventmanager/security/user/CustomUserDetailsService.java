package kkashin.dev.eventmanager.security.user;

import kkashin.dev.eventmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userRepository.findByLoginNormalized(username.trim().toLowerCase(Locale.ROOT)).orElseThrow(
                () -> new UsernameNotFoundException("User was not found")
        );

        return User.fromEntity(user);
    }
}
