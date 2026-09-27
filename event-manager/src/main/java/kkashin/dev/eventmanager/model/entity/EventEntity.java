package kkashin.dev.eventmanager.model.entity;

import jakarta.persistence.*;
import kkashin.dev.eventmanager.model.enums.EventStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@DynamicUpdate
public class EventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private UserEntity user;

    @Column(name = "max_places", nullable = false)
    private Integer maxPlaces;

    @Column(name = "occupied_places", nullable = false)
    private Integer occupiedPlaces;

    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    @Column(name = "cost", nullable = false)
    private Long cost;

    @Column(name = "duration", nullable = false)
    private Integer duration;

    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    private EventLocation eventLocation;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EventStatus status;

    @ManyToMany(mappedBy = "registrations")
    private List<UserEntity> users = new ArrayList<>();

    public void registerUser(UserEntity user) {
        this.users.add(user);
        user.getRegistrations().add(this);

        this.occupiedPlaces++;
    }

    public void cancelRegistration(UserEntity user) {
        this.users.remove(user);
        user.getRegistrations().remove(this);

        this.occupiedPlaces--;
    }
}
