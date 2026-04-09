package ru.itmo.blps1.dto.boardpin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePinInBoardRequest {

    @NotBlank(message = "Pin title must not be blank")
    @Size(max = 200, message = "Pin title must be at most 200 characters")
    private String title;

    @Size(max = 2000, message = "Description must be at most 2000 characters")
    private String description;

    @NotBlank(message = "Image URL must not be blank")
    @Size(max = 1000, message = "Image URL must be at most 1000 characters")
    private String imageUrl;

    @NotBlank(message = "Image key must not be blank")
    @Size(max = 500, message = "Image key must be at most 500 characters")
    private String imageKey;

    @NotNull(message = "Author id must not be null")
    private Long authorId;
}