package com.university.reminderapp.repository;

import com.university.reminderapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findAllByUniversityIdAndRolesContaining(Long universityId, String role);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
}



