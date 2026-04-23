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
import ru.itmo.blps1.dto.pin.PinResponse;
import ru.itmo.blps1.service.pin.PinServiceInt;

import java.util.List;

@RestController
@RequestMapping("/api/pins")
@RequiredArgsConstructor
@Tag(name = "Pins", description = "Operations with pins")
public class PinController {

    private final PinServiceInt pinService;

    @PostMapping(
            value = "/with-file",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create pin with image file")
    public PinResponse createPinWithFile(
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
        return pinService.createPinWithFile(title, description, authorId, file);
    }

    @GetMapping
    @Operation(summary = "Get all pins")
    public List<PinResponse> getAllPins() {
        return pinService.getAllPins();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get pin by id")
    public PinResponse getPinById(@PathVariable Long id) {
        return pinService.getPinById(id);
    }
}