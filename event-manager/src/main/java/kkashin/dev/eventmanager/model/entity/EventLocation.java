package kkashin.dev.eventmanager.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import kkashin.dev.eventmanager.model.dto.location.UpdateEventLocationDto;
import lombok.*;

@Entity
@Table(name = "locations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class EventLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String name;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String address;

    @NotNull
    @Min(5)
    @Column(nullable = false)
    private Integer capacity;

    @Size(max = 2000)
    @Column(length = 2000)
    private String description;

    public EventLocation(String name, String address, Integer capacity) {
        this(name, address, capacity, null);
    }

    public EventLocation(String name, String address, Integer capacity, String description) {
        this.name = name;
        this.address = address;
        this.capacity = capacity;
        this.description = description;
    }

    public void updateDetails(String name, String address, Integer capacity, String description) {
        this.name = name;
        this.address = address;
        this.capacity = capacity;
        this.description = description;
    }
}
