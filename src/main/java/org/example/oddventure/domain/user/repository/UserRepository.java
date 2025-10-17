package org.example.oddventure.domain.user.repository;

import org.example.oddventure.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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

}
