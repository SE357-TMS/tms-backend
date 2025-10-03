package com.example.tms.enity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "route")
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "route_id")
    private Integer routeId;

    @Column(name = "route_name", nullable = false, length = 100)
    private String routeName;

    @Column(name = "start_location", nullable = false, length = 100)
    private String startLocation;

    @Column(name = "end_location", nullable = false, length = 100)
    private String endLocation;

    @Column(name = "duration_days")
    private Integer durationDays;

    @Column(name = "image", length = 255)
    private String image;

    public enum Status { OPEN, ONGOING, CLOSED }
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "ENUM('OPEN','ONGOING','CLOSED')")
    private Status status = Status.OPEN;
}
