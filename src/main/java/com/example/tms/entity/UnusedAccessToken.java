package com.example.tms.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "unused_access_token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UnusedAccessToken extends AbstractBaseEntity {

    @Column(name = "token", nullable = false, unique = true, length = 512)
    private String token;

    @Column(name = "expire_at", nullable = false)
    private Date expireAt;
}

