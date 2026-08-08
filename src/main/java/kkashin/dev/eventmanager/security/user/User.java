package kkashin.dev.eventmanager.security.user;

import kkashin.dev.eventmanager.model.entity.UserEntity;
import kkashin.dev.eventmanager.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data
@AllArgsConstructor
public class User implements UserDetails {
    private Long id;
    private String login;
    private String loginNormalized;
    private Integer age;
    private String passwordHash;
    private UserRole userRole;

    public static User fromEntity(UserEntity userEntity) {
        return new User(
                userEntity.getId(),
                userEntity.getLogin(),
                userEntity.getLoginNormalized(),
                userEntity.getAge(),
                userEntity.getPasswordHash(),
                userEntity.getRole()
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + userRole.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return login;
    }
}
