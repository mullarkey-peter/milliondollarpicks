package com.glizzy.milliondollarpicks.userservice.repository;

import com.glizzy.milliondollarpicks.userservice.entity.User;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends R2dbcRepository<User, Long> {
    /**
     * Find a user by their username
     * @param username the username to search for
     * @return a Mono with the user if found
     */
    Mono<User> findByUsername(String username);

    /**
     * Check if a user with the given username exists
     * @param username the username to check
     * @return a Mono with true if the user exists, false otherwise
     */
    Mono<Boolean> existsByUsername(String username);
}