package ru.itmo.blps1.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.itmo.blps1.dto.boardpin.BoardPinResponse;
import ru.itmo.blps1.dto.boardpin.CreatePinInBoardResponse;
import ru.itmo.blps1.dto.pin.PinResponse;
import ru.itmo.blps1.service.boardpin.BoardPinServiceInt;

import java.util.List;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
@Tag(name = "Board-Pin", description = "Operations for saving pins to boards")
public class BoardPinController {

    private final BoardPinServiceInt boardPinService;

    @PostMapping("/{boardId}/pins/{pinId}")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Save existing pin to board")
    public BoardPinResponse saveExistingPinToBoard(
            @PathVariable Long boardId,
            @PathVariable Long pinId
    ) {
        return boardPinService.saveExistingPinToBoard(boardId, pinId);
    }

    @GetMapping("/{boardId}/pins")
    @Operation(summary = "Get all pins of a board")
    public List<PinResponse> getPinsByBoardId(@PathVariable Long boardId) {
        return boardPinService.getPinsByBoardId(boardId);
    }

    @PostMapping(
            value = "/{boardId}/pins/with-file",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create new pin with file and save it to board")
    public CreatePinInBoardResponse createPinInBoardWithFile(
            @PathVariable Long boardId,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("authorId") Long authorId,
            @Parameter(
                    description = "Image file",
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                            schema = @Schema(type = "string", format = "binary"))
            )
            @RequestPart("file") MultipartFile file
    ) {
        return boardPinService.createPinInBoard(
                boardId,
                title,
                description,
                authorId,
                file
        );
    }
}