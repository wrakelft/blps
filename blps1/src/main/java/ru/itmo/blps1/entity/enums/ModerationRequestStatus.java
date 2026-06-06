package ru.itmo.blps1.entity.enums;

public enum ModerationRequestStatus {
    CREATED,
    SENT_TO_EXTERNAL_SYSTEM,
    IN_REVIEW,
    APPROVED,
    REJECTED,
    FAILED
}