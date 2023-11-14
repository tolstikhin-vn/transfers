package ru.sovcombank.petbackendusers.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sovcombank.petbackendusers.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhoneNumber(String phoneNumber);
}
