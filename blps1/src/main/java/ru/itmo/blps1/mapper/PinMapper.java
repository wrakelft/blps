package ru.itmo.blps1.mapper;

import org.springframework.stereotype.Component;
import ru.itmo.blps1.dto.pin.PinResponse;
import ru.itmo.blps1.entity.Pin;

@Component
public class PinMapper {

    public PinResponse toResponse(Pin pin) {
        if (pin == null) {
            return null;
        }

        return PinResponse.builder()
                .id(pin.getId())
                .title(pin.getTitle())
                .description(pin.getDescription())
                .imageUrl(pin.getImageUrl())
                .imageKey(pin.getImageKey())
                .createdAt(pin.getCreatedAt())
                .authorId(pin.getAuthor() != null ? pin.getAuthor().getId() : null)
                .build();
    }
}