package com.example.tms.repository;

import com.example.tms.enity.RouteAttraction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface RouteAttractionRepository extends JpaRepository<RouteAttraction, UUID> {
}

