package com.example.tms.repository;

import com.example.tms.enity.FavoriteTour;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface FavoriteTourRepository extends JpaRepository<FavoriteTour, UUID> {
}

