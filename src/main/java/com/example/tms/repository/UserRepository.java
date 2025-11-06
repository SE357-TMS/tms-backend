package com.example.tms.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.tms.enity.User;

public interface UserRepository extends JpaRepository<User, UUID> {
	// Tìm user ACTIVE (deleted_at = 0)
	@Query("SELECT u FROM User u WHERE u.username = :username AND u.deletedAt = 0")
	Optional<User> findByUsername(@Param("username") String username);
	
	@Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.deletedAt = 0")
	boolean existsByEmail(@Param("email") String email);
	
	// Find user by email (only ACTIVE users)
	@Query("SELECT u FROM User u WHERE u.email = :email AND u.deletedAt = 0")
	Optional<User> findByEmail(@Param("email") String email);
	
	// Check if username exists for another ACTIVE user (excluding current user)
	@Query("SELECT COUNT(u) > 0 FROM User u WHERE u.username = :username AND u.id != :userId AND u.deletedAt = 0")
	boolean existsByUsernameAndIdNot(@Param("username") String username, @Param("userId") UUID userId);
	
	// Check if email exists for another ACTIVE user (excluding current user)
	@Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.id != :userId AND u.deletedAt = 0")
	boolean existsByEmailAndIdNot(@Param("email") String email, @Param("userId") UUID userId);
	
	@Query("SELECT u FROM User u WHERE u.role = :role AND u.deletedAt = 0")
	List<User> findByRole(@Param("role") User.Role role);
	
	@Query("SELECT u FROM User u WHERE u.role = :role AND u.deletedAt = 0")
	Page<User> findByRole(@Param("role") User.Role role, Pageable pageable);
	
	// Tìm tất cả users (bao gồm deleted)
	@Query(value = "SELECT * FROM user", nativeQuery = true)
	Page<User> findAllIncludingDeleted(Pageable pageable);
	
	// Chỉ active users
	@Query("SELECT u FROM User u WHERE u.deletedAt = 0")
	Page<User> findAllActive(Pageable pageable);
	
	// Tìm users đã deleted
	@Query(value = "SELECT * FROM user WHERE deleted_at > 0", nativeQuery = true)
	Page<User> findAllDeleted(Pageable pageable);
	
	// Tìm user by username bao gồm cả deleted (cho admin)
	@Query(value = "SELECT * FROM user WHERE username = :username", nativeQuery = true)
	List<User> findAllByUsernameIncludingDeleted(@Param("username") String username);
	
	// Tìm user by email bao gồm cả deleted (cho admin)
	@Query(value = "SELECT * FROM user WHERE email = :email", nativeQuery = true)
	List<User> findAllByEmailIncludingDeleted(@Param("email") String email);
}
