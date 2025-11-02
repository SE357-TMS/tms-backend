package com.example.tms.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.tms.dto.request.CreateUserRequest;
import com.example.tms.dto.request.UpdateUserRequest;
import com.example.tms.dto.response.UserResponse;
import com.example.tms.enity.User;
import com.example.tms.repository.UserRepository;
import com.example.tms.service.interface_.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        try {
            // Check if username already exists (only checks ACTIVE users due to @Where clause)
            // Composite unique key (username, deleted_at) allows reuse after soft delete
            if (userRepository.findByUsername(request.getUsername()).isPresent()) {
                throw new RuntimeException("Username already exists");
            }
            
            // Check if email already exists (only checks ACTIVE users due to @Where clause)
            // Composite unique key (email, deleted_at) allows reuse after soft delete
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email already exists");
            }

            User user = new User();
            user.setUsername(request.getUsername());
            user.setUserPassword(passwordEncoder.encode(request.getPassword()));
            user.setFullName(request.getFullName());
            user.setEmail(request.getEmail());
            user.setPhoneNumber(request.getPhoneNumber());
            user.setAddress(request.getAddress());
            user.setBirthday(request.getBirthday());
            user.setGender(request.getGender());
            user.setRole(User.Role.CUSTOMER); // Default role is CUSTOMER
            user.setIsLock(false);

            User savedUser = userRepository.save(user);
            return new UserResponse(savedUser);
        } catch (DataIntegrityViolationException e) {
            // Bắt lỗi khi có race condition tạo duplicate username/email
            if (e.getMessage().contains("username")) {
                throw new RuntimeException("Username already exists (concurrent creation detected)");
            } else if (e.getMessage().contains("email")) {
                throw new RuntimeException("Email already exists (concurrent creation detected)");
            }
            throw new RuntimeException("Data integrity violation: " + e.getMessage());
        }
    }

    @Override
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return new UserResponse(user);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public Page<UserResponse> getAllUsersWithPagination(Pageable pageable, boolean includeDeleted) {
        Page<User> userPage;
        if (includeDeleted) {
            // Admin view: show all users including deleted ones
            userPage = userRepository.findAllIncludingDeleted(pageable);
        } else {
            // Only active users (deleted_at = 0)
            userPage = userRepository.findAllActive(pageable);
        }
        return userPage.map(UserResponse::new);
    }

    @Override
    @Transactional
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

            if (request.getFullName() != null) {
                user.setFullName(request.getFullName());
            }
            
            // Check if email is being changed and already exists for another ACTIVE user
            // Due to @Where clause, only checks against active users (deleted_at = 0)
            // Composite unique key (email, deleted_at) ensures no conflict with deleted records
            if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
                if (userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
                    throw new RuntimeException("Email already exists");
                }
                user.setEmail(request.getEmail());
            }
            
            if (request.getPhoneNumber() != null) {
                user.setPhoneNumber(request.getPhoneNumber());
            }
            if (request.getAddress() != null) {
                user.setAddress(request.getAddress());
            }
            if (request.getBirthday() != null) {
                user.setBirthday(request.getBirthday());
            }
            if (request.getGender() != null) {
                user.setGender(request.getGender());
            }

            User updatedUser = userRepository.save(user);
            return new UserResponse(updatedUser);
        } catch (ObjectOptimisticLockingFailureException e) {
            // Bắt lỗi khi có xung đột cập nhật đồng thời
            throw new RuntimeException("User was updated by another transaction. Please refresh and try again.");
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage().contains("email")) {
                throw new RuntimeException("Email already exists (concurrent update detected)");
            }
            throw new RuntimeException("Data integrity violation: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
            
            // Soft delete: set deleted_at to current timestamp
            user.setDeletedAt(System.currentTimeMillis());
            userRepository.save(user);
            
            // Force flush to database to update composite unique key immediately
            userRepository.flush();
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new RuntimeException("User was updated by another transaction. Please refresh and try again.");
        }
    }

    @Override
    public List<UserResponse> getUsersByRole(String role) {
        User.Role userRole;
        try {
            userRole = User.Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role: " + role);
        }
        
        return userRepository.findByRole(userRole).stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public Page<UserResponse> getUsersByRoleWithPagination(String role, Pageable pageable) {
        User.Role userRole;
        try {
            userRole = User.Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role: " + role);
        }
        
        Page<User> userPage = userRepository.findByRole(userRole, pageable);
        return userPage.map(UserResponse::new);
    }
}
