package ua.nure.beautysalon.master.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "masters")
public class Master {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Master name is required")
    @Size(min = 2, max = 100, message = "Master name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s'-]+$", message = "Master name can only contain letters, spaces, hyphens, and apostrophes")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Master email is required")
    @Email(message = "Please provide a valid email address for the master")
    @Size(max = 255, message = "Master email cannot be longer than 255 characters")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Master phone number is required")
    @Pattern(regexp = "^\\+?[0-9\\s\\-()]{7,20}$", message = "Please provide a valid phone number for the master")
    @Column(nullable = false, unique = true)
    private String phone;

    @ElementCollection
    @CollectionTable(name = "master_facilities", joinColumns = @JoinColumn(name = "master_id"))
    @Column(name = "facility_id")
    private List<Long> facilityIds;
}