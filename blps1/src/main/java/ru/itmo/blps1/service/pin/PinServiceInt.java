package ru.itmo.blps1.service.pin;

import org.springframework.web.multipart.MultipartFile;
import ru.itmo.blps1.dto.file.FileUploadResponse;
import ru.itmo.blps1.dto.pin.PinResponse;
import ru.itmo.blps1.entity.Pin;
import ru.itmo.blps1.entity.User;

import java.util.List;

public interface PinServiceInt {

    PinResponse createPinWithFile(String title, String description, Long authorId, MultipartFile file);

    List<PinResponse> getAllPins();

    PinResponse getPinById(Long id);

    Pin getPinEntityById(Long id);

    Pin createPinEntity(String title, String description, User author, FileUploadResponse uploadResponse);
}

