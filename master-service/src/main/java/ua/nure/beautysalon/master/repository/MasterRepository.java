package ua.nure.beautysalon.master.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ua.nure.beautysalon.master.entity.Master;

public interface MasterRepository extends JpaRepository<Master, Long> {
    boolean existsByPhoneOrEmail(String phone, String email);
    Page<Master> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email, Pageable pageable);
}