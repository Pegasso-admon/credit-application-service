package com.coopcredit.infrastructure.persistence.repository;

import com.coopcredit.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for UserEntity.
 * Provides CRUD operations and custom queries for user management.
 */
@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

    /**
     * Finds a user by username.
     *
     * @param username the username to search for
     * @return an Optional containing the user if found
     */
    Optional<UserEntity> findByUsername(String username);

    /**
     * Finds a user by document number.
     *
     * @param document the document number to search for
     * @return an Optional containing the user if found
     */
    Optional<UserEntity> findByDocument(String document);

    /**
     * Finds an active user by username.
     *
     * @param username the username to search for
     * @return an Optional containing the active user if found
     */
    @Query("SELECT u FROM UserEntity u WHERE u.username = :username AND u.active = true")
    Optional<UserEntity> findActiveByUsername(@Param("username") String username);

    /**
     * Checks if a username already exists.
     *
     * @param username the username to check
     * @return true if the username exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Checks if a document already exists.
     *
     * @param document the document to check
     * @return true if the document exists, false otherwise
     */
    boolean existsByDocument(String document);

    /**
     * Checks if an email already exists.
     *
     * @param email the email to check
     * @return true if the email exists, false otherwise
     */
    boolean existsByEmail(String email);
}