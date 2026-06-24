package ru.itmo.blps1.bpm.delegate.file;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ru.itmo.blps1.dto.file.FileUploadResponse;
import ru.itmo.blps1.service.bpm.MultipartFileStore;
import ru.itmo.blps1.service.storage.FileStorageService;

@Component("uploadFileDelegate")
@RequiredArgsConstructor
public class UploadFileDelegate implements JavaDelegate {

    private final FileStorageService fileStorageService;
    private final MultipartFileStore multipartFileStore;

    @Override
    public void execute(DelegateExecution execution) {
        String fileRef = (String) execution.getVariable("fileRef");
        MultipartFile file = multipartFileStore.get(fileRef);

        FileUploadResponse response = fileStorageService.uploadImage(file);

        execution.setVariable("imageKey", response.getImageKey());
        execution.setVariable("imageUrl", response.getImageUrl());
        execution.setVariable("contentType", response.getContentType());
        execution.setVariable("originalFileName", response.getOriginalFileName());
        execution.setVariable("fileSize", response.getSize());
    }
}