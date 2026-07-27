package kkashin.dev.eventmanager.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import kkashin.dev.eventmanager.model.enums.UserRole;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_users_username",
                        columnNames = "login_norm"
                ),
        }
)
@Getter
@Setter
@NoArgsConstructor
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login", nullable = false, length = 36)
    private String login;

    @Column(name = "login_norm", nullable = false, length = 36)
    private String loginNormalized;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "age")
    @Min(value = 0)
    private Integer age;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UserEntity(
            String login,
            String loginNormalized,
            String passwordHash,
            Integer age,
            UserRole role
    ) {
        this.login = login;
        this.loginNormalized = loginNormalized;
        this.passwordHash = passwordHash;
        this.age = age;
        this.role = role;
    }
}
