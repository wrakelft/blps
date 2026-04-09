package ru.itmo.blps1.service.storage;

import org.springframework.web.multipart.MultipartFile;
import ru.itmo.blps1.dto.file.FileUploadResponse;

public interface FileStorageService {

    FileUploadResponse uploadImage(MultipartFile file);

    void deleteFile(String imageKey);
}
