package kkashin.dev.eventmanager.model.mappers;

import kkashin.dev.eventmanager.model.dto.user.RegisterUserDto;
import kkashin.dev.eventmanager.model.dto.user.UserDto;
import kkashin.dev.eventmanager.model.entity.UserEntity;
import kkashin.dev.securityConstants.UserRoles;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class UserMapper {

    public UserDto toDto(UserEntity user) {
        return new UserDto(
                user.getId(),
                user.getLogin(),
                user.getAge(),
                user.getRole()
        );
    }

    public UserEntity toEntity(RegisterUserDto dto, String passwordHash) {
        var user = new UserEntity();

        user.setLogin(dto.login());
        user.setLoginNormalized(dto.login().trim().toLowerCase(Locale.ROOT));
        user.setAge(dto.age());
        user.setPasswordHash(passwordHash);
        user.setRole(UserRoles.USER);

        return user;
    }
}
