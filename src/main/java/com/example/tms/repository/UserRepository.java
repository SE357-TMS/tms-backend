package com.example.tms.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.tms.enity.User;

public interface UserRepository extends JpaRepository<User, UUID> {
	Optional<User> findByUsername(String username);
	boolean existsByEmail(String email);
	List<User> findByRole(User.Role role);
	Page<User> findByRole(User.Role role, Pageable pageable);
	
	// Include deleted users for admin view
	@Query("SELECT u FROM User u WHERE u.deleted = false")
	Page<User> findAllActive(Pageable pageable);
	
	@Query("SELECT u FROM User u WHERE u.deleted = true")
	Page<User> findAllDeleted(Pageable pageable);
}
