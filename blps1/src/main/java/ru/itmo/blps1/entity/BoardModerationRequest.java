package ru.itmo.blps1.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.itmo.blps1.entity.enums.ExternalSyncStatus;
import ru.itmo.blps1.entity.enums.ModerationRequestStatus;

import java.time.OffsetDateTime;

@Entity
@Table(name = "board_moderation_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardModerationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requested_by_id", nullable = false)
    private User requestedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moderator_id")
    private User moderator;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ModerationRequestStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "external_sync_status", nullable = false, length = 30)
    private ExternalSyncStatus externalSyncStatus;

    @Column(name = "external_system", length = 50)
    private String externalSystem;

    @Column(name = "external_task_id", length = 100)
    private String externalTaskId;

    @Column(length = 2000)
    private String comment;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "processed_at")
    private OffsetDateTime processedAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }

        if (status == null) {
            status = ModerationRequestStatus.CREATED;
        }

        if (externalSyncStatus == null) {
            externalSyncStatus = ExternalSyncStatus.NOT_STARTED;
        }

        if (externalSystem == null) {
            externalSystem = "BITRIX24";
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}