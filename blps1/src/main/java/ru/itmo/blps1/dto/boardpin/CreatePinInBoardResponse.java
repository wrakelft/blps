package ru.itmo.blps1.dto.boardpin;

import lombok.Builder;
import lombok.Getter;
import ru.itmo.blps1.dto.pin.PinResponse;

@Getter
@Builder
public class CreatePinInBoardResponse {
    private BoardPinResponse boardPin;
    private PinResponse pin;
}