package ru.itmo.blps1.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.itmo.blps1.dto.pin.PinResponse;
import ru.itmo.blps1.service.PinService;

import java.util.List;

@RestController
@RequestMapping("/api/pins")
@RequiredArgsConstructor
public class PinController {

    private final PinService pinService;

    @PostMapping(
            value = "/with-file",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public PinResponse createPinWithFile(
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("authorId") Long authorId,
            @RequestPart("file") MultipartFile file
    ) {
        return pinService.createPinWithFile(title, description, authorId, file);
    }

    @GetMapping
    public List<PinResponse> getAllPins() {
        return pinService.getAllPins();
    }

    @GetMapping("/{id}")
    public PinResponse getPinById(@PathVariable Long id) {
        return pinService.getPinById(id);
    }
}