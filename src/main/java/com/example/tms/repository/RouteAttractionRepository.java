package com.example.tms.repository;

import com.example.tms.enity.RouteAttraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.UUID;

public interface RouteAttractionRepository extends JpaRepository<RouteAttraction, UUID>, JpaSpecificationExecutor<RouteAttraction> {
}

