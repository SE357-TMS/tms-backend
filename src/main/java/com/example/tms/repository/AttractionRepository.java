package com.example.tms.repository;

import com.example.tms.enity.Attraction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface AttractionRepository extends JpaRepository<Attraction, UUID> {
}

