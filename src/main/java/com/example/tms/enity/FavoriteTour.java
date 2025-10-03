package com.example.tms.enity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "favorite_tour")
@IdClass(FavoriteTour.FavoriteTourId.class)
public class FavoriteTour {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FavoriteTourId implements Serializable {
        private Integer user;
        private Integer route;
    }
}
