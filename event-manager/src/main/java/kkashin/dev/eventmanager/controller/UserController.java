package kkashin.dev.eventmanager.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import kkashin.dev.eventmanager.model.dto.user.JwtTokenDto;
import kkashin.dev.eventmanager.model.dto.user.LoginUserDto;
import kkashin.dev.eventmanager.model.dto.user.RegisterUserDto;
import kkashin.dev.eventmanager.model.dto.user.UserDto;
import kkashin.dev.eventmanager.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserDto> register(@RequestBody @Valid RegisterUserDto dto) {
        var user = userService.register(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping("/auth")
    public ResponseEntity<JwtTokenDto> auth(@RequestBody @Valid LoginUserDto dto) {
        var token = userService.authenticateUser(dto);

        return ResponseEntity.ok(token);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<UserDto> getUser(@PathVariable @NotNull Long userId) {
        var user = userService.findById(userId);

        return ResponseEntity.ok(user);
    }
}
