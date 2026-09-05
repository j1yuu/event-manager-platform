package kkashin.dev.eventmanager.service;

import jakarta.persistence.EntityNotFoundException;
import kkashin.dev.eventmanager.exceptions.models.ManagerBadRequestException;
import kkashin.dev.eventmanager.exceptions.models.ManagerUnauthorizedRequestException;
import kkashin.dev.eventmanager.model.dto.user.JwtTokenDto;
import kkashin.dev.eventmanager.model.dto.user.LoginUserDto;
import kkashin.dev.eventmanager.model.dto.user.RegisterUserDto;
import kkashin.dev.eventmanager.model.dto.user.UserDto;
import kkashin.dev.eventmanager.model.entity.UserEntity;
import kkashin.dev.eventmanager.model.mappers.UserMapper;
import kkashin.dev.eventmanager.repository.UserRepository;
import kkashin.dev.eventmanager.security.jwt.JwtManager;
import kkashin.dev.eventmanager.security.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UserService {
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtManager jwtManager;

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public User findByLogin(String login) {
        var user = userRepository.findByLoginNormalized(login.trim().toLowerCase(Locale.ROOT)).orElseThrow(
                () -> new UsernameNotFoundException("User was not found")
        );

        return User.fromEntity(user);
    }

    public UserDto findById(Long id) {
        var user = userRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("User was not found")
        );

        return userMapper.toDto(user);
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof User user) {
                return user;
            }
        }

        throw new ManagerUnauthorizedRequestException("User is not authorized");
    }

    public JwtTokenDto authenticateUser(LoginUserDto loginRequestDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.login(),
                        loginRequestDto.password()
                )
        );

        var authenticatedUser = (User) authentication.getPrincipal();
        assert authenticatedUser != null;
        var token = jwtManager.generate(authenticatedUser);

        return new JwtTokenDto(token);
    }

    public UserEntity getCurrentUserEntity() {
        var user = getCurrentUser();

        return userRepository.findById(user.getId()).orElseThrow(
                () -> new EntityNotFoundException("User not found")
        );
    }

    @Transactional
    public UserDto register(RegisterUserDto dto) {
        var loginNorm = dto.login().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByLoginNormalized(loginNorm)) {
            throw new ManagerBadRequestException("Login already taken");
        }

        var passwordHash = passwordEncoder.encode(dto.password());
        var userToSave = userMapper.toEntity(dto, passwordHash);

        var savedUser = userRepository.save(userToSave);
        return userMapper.toDto(savedUser);
    }
}
