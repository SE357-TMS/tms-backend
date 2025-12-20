package com.example.tms.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class AbstractBaseEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Soft delete marker:
     * - 0: Active record (not deleted)
     * - Timestamp (milliseconds): Deleted at this time
     * This allows UNIQUE constraints on (field, deleted_at) to work correctly
     */
    @Column(name = "deleted_at", nullable = false)
    private Long deletedAt = 0L;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
    
    /**
     * Helper method to check if entity is deleted
     */
    public boolean isDeleted() {
        return deletedAt != null && deletedAt > 0;
    }
    
    /**
     * Helper method to mark entity as deleted
     */
    public void markAsDeleted() {
        this.deletedAt = System.currentTimeMillis();
    }
}

