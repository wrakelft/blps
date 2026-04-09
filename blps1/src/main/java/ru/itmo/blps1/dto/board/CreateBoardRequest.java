package ru.itmo.blps1.dto.board;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import ru.itmo.blps1.entity.enums.BoardPrivacy;

@Getter
@Setter
public class CreateBoardRequest {

    @NotBlank(message = "Board name must not be blank")
    @Size(max = 150, message = "Board name must be at most 150 characters")
    private String name;

    @Size(max = 1000, message = "Description must be at most 1000 characters")
    private String description;

    @NotNull(message = "Board privacy must not be null")
    private BoardPrivacy privacy;

    @NotNull(message = "Owner id must not be null")
    private Long ownerId;
}