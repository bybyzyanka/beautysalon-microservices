package ua.nure.beautysalon.master.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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

    @NotBlank(message = "Name cannot be blank")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Name must contain only letters and spaces")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Phone number cannot be blank")
    @Pattern(regexp = "\\+?[0-9]{7,15}", message = "Phone number must be valid")
    @Column(nullable = false, unique = true)
    private String phone;

    @ElementCollection
    @CollectionTable(name = "master_facilities", joinColumns = @JoinColumn(name = "master_id"))
    @Column(name = "facility_id")
    private List<Long> facilityIds;
}
