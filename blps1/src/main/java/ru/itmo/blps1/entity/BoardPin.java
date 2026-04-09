package ru.itmo.blps1.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "board_pins",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_board_pin", columnNames = {"board_id", "pin_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardPin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "saved_at", nullable = false)
    private OffsetDateTime savedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pin_id", nullable = false)
    private Pin pin;

    @PrePersist
    public void prePersist() {
        if (savedAt == null) {
            savedAt = OffsetDateTime.now();
        }
    }
}