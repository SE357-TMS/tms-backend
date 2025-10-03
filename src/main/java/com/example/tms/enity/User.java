package com.example.tms.enity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user", uniqueConstraints = {
    @UniqueConstraint(columnNames = "username"),
    @UniqueConstraint(columnNames = "email")
})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "username", nullable = false, length = 50, unique = true)
    private String username;

    @Column(name = "user_password", nullable = false, length = 255)
    private String userPassword;

    @Column(name = "is_lock", nullable = false)
    private Boolean isLock = false;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "email", nullable = false, length = 100, unique = true)
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
