package ua.nure.beautysalon.facility.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ua.nure.beautysalon.facility.entity.Facility;

public interface FacilityRepository extends JpaRepository<Facility, Long> {
    boolean existsByNameIgnoreCase(String name);
    Page<Facility> findByNameContainingIgnoreCase(String name, Pageable pageable);
}