package ru.itmo.blps1.service.storage;

import io.minio.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.itmo.blps1.config.properties.MinioProperties;
import ru.itmo.blps1.dto.file.FileUploadResponse;
import ru.itmo.blps1.exception.BadRequestException;

import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MinioFileStorageService implements FileStorageService {

    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024;

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    @Override
    public FileUploadResponse uploadImage(MultipartFile file) {
        validateFile(file);

        String originalFileName = file.getOriginalFilename();
        String contentType = file.getContentType();
        String extension = extractExtension(originalFileName);
        String imageKey = UUID.randomUUID() + extension;

        try (InputStream inputStream = file.getInputStream()) {
            ensureBucketExists();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(imageKey)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(contentType)
                            .build()
            );

            String imageUrl = buildFileUrl(imageKey);

            return FileUploadResponse.builder()
                    .imageKey(imageKey)
                    .imageUrl(imageUrl)
                    .contentType(contentType)
                    .originalFileName(originalFileName)
                    .size(file.getSize())
                    .build();

        } catch (Exception e) {
            throw new BadRequestException("Failed to upload image");
        }
    }

    @Override
    public void deleteFile(String imageKey) {
        if (imageKey == null || imageKey.isBlank()) {
            return;
        }

        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(imageKey)
                            .build()
            );
        } catch (Exception e) {
            throw new BadRequestException("Failed to delete file");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("File size must not exceed 10 MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }
    }

    private void ensureBucketExists() throws Exception {
        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                        .bucket(minioProperties.getBucketName())
                        .build()
        );

        if (!exists) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .build()
            );
        }
    }

    private String buildFileUrl(String imageKey) {
        return minioProperties.getUrl()
                + "/"
                + minioProperties.getBucketName()
                + "/"
                + imageKey;
    }

    private String extractExtension(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "";
        }

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }

        return fileName.substring(lastDotIndex);
    }
}