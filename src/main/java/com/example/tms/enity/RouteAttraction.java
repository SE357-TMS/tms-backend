package com.example.tms.enity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "route_attraction")
public class RouteAttraction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attraction_id", nullable = false)
    private Attraction attraction;

    @Column(name = "day", nullable = false)
    private Integer day;

    @Column(name = "order_in_day", nullable = false)
    private Integer orderInDay;

    @Column(name = "activity_description", columnDefinition = "TEXT")
    private String activityDescription;
}
