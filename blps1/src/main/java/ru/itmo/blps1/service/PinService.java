package ru.itmo.blps1.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.itmo.blps1.dto.file.FileUploadResponse;
import ru.itmo.blps1.dto.pin.PinResponse;
import ru.itmo.blps1.entity.Pin;
import ru.itmo.blps1.entity.User;
import ru.itmo.blps1.exception.BadRequestException;
import ru.itmo.blps1.exception.NotFoundException;
import ru.itmo.blps1.mapper.PinMapper;
import ru.itmo.blps1.repository.PinRepository;
import ru.itmo.blps1.repository.UserRepository;
import ru.itmo.blps1.service.storage.FileStorageService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PinService {

    private final PinRepository pinRepository;
    private final UserRepository userRepository;
    private final PinMapper pinMapper;
    private final FileStorageService fileStorageService;

    public PinResponse createPinWithFile(
            String title,
            String description,
            Long authorId,
            MultipartFile file
    ) {
        validateCreatePinRequest(title, authorId);

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new NotFoundException("User with id " + authorId + " not found"));

        FileUploadResponse uploadResponse = fileStorageService.uploadImage(file);
        try {
            Pin pin = Pin.builder()
                    .title(title.trim())
                    .description(description)
                    .imageUrl(uploadResponse.getImageUrl())
                    .imageKey(uploadResponse.getImageKey())
                    .author(author)
                    .build();

            Pin savedPin = pinRepository.save(pin);
            return pinMapper.toResponse(savedPin);
        } catch (Exception e) {
            fileStorageService.deleteFile(uploadResponse.getImageKey());
            throw e;
        }
    }

    public List<PinResponse> getAllPins() {
        return pinRepository.findAll()
                .stream()
                .map(pinMapper::toResponse)
                .toList();
    }

    public PinResponse getPinById(Long id) {
        Pin pin = pinRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pin with id " + id + " not found"));

        return pinMapper.toResponse(pin);
    }

    private void validateCreatePinRequest(String title, Long authorId) {
        if (title == null || title.isBlank()) {
            throw new BadRequestException("Pin title must not be blank");
        }

        if (title.length() > 200) {
            throw new BadRequestException("Pin title must be at most 200 characters");
        }

        if (authorId == null) {
            throw new BadRequestException("Author id must not be null");
        }
    }
}