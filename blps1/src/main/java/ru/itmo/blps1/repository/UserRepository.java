package ru.itmo.blps1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itmo.blps1.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);
}
