package kkashin.dev.eventmanager.repository;

import kkashin.dev.eventmanager.model.entity.UserEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByLoginNormalized(String loginNormalized);
    boolean existsByLoginNormalized(String loginNormalized);
}
