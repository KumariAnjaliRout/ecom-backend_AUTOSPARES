package com.automobile.ecom.repository;

import com.automobile.ecom.entity.Role;
import com.automobile.ecom.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<User> findAllByRole(Role role, Pageable pageable);
    @Query("SELECT u.id FROM User u WHERE u.role = 'ADMIN'")
    List<UUID> findAllAdminIds();
}
