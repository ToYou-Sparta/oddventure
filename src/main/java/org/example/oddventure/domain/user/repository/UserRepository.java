package org.example.oddventure.domain.user.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.example.oddventure.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u " +
            "WHERE (:email IS NULL OR u.email LIKE %:email%) " +
            "AND (:username IS NULL OR u.username LIKE %:username%)")
    Page<User> findBySearchConditions(
            @Param("email") String email,
            @Param("username") String username,
            Pageable pageable);

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") Long id);
}
