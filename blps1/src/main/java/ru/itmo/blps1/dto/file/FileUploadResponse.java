package ru.itmo.blps1.dto.file;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileUploadResponse {

    private String imageKey;
    private String imageUrl;
    private String contentType;
    private String originalFileName;
    private Long size;
}