package ua.nure.beautysalon.facility.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "facilities")
public class Facility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Name cannot be null")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Name must contain only letters and spaces")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "Price cannot be null")
    @Column(nullable = false)
    private Double price;
}