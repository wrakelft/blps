package ru.itmo.blps1.dto.board;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ru.itmo.blps1.entity.enums.BoardPrivacy;

@Getter
@Setter
public class UpdateBoardPrivacyRequest {

    @NotNull(message = "Board privacy must not be null")
    private BoardPrivacy privacy;
}
