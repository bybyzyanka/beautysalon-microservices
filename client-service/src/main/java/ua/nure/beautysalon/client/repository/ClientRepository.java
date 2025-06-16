package ua.nure.beautysalon.client.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ua.nure.beautysalon.client.entity.Client;

public interface ClientRepository extends JpaRepository<Client, Long> {
    boolean existsByPhoneOrEmail(String phone, String email);
    Page<Client> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email, Pageable pageable);
}