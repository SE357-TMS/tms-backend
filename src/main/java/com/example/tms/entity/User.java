package com.example.tms.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user", uniqueConstraints = {
    // Composite unique: (username, deleted_at)
    // Allows same username if one is deleted (deleted_at != 0)
    @UniqueConstraint(name = "uk_username_deleted", columnNames = {"username", "deleted_at"}),
    
    // Composite unique: (email, deleted_at)
    // Allows same email if one is deleted (deleted_at != 0)
    @UniqueConstraint(name = "uk_email_deleted", columnNames = {"email", "deleted_at"})
})
public class User extends AbstractBaseEntity {
    // Inherit UUID id, createdAt, updatedAt, deletedAt, version from AbstractBaseEntity

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "user_password", nullable = false, length = 255)
    private String userPassword;

    @Column(name = "is_lock", nullable = false)
    private Boolean isLock = false;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "birthday")
    private LocalDate birthday;

    public enum Gender { M, F, O }
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", columnDefinition = "ENUM('M','F','O')")
    private Gender gender;

    public enum Role { CUSTOMER, STAFF, ADMIN }
    @Enumerated(EnumType.STRING)
    @Column(name = "role", columnDefinition = "ENUM('CUSTOMER','STAFF','ADMIN')")
    private Role role;
}

