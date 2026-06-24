package ru.itmo.blps1.bpm.delegate.pin;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ru.itmo.blps1.dto.pin.PinResponse;
import ru.itmo.blps1.service.bpm.MultipartFileStore;
import ru.itmo.blps1.service.pin.PinServiceInt;

@Component("createPinWithFileDelegate")
@RequiredArgsConstructor
public class CreatePinWithFileDelegate implements JavaDelegate {

    private final PinServiceInt pinService;
    private final MultipartFileStore multipartFileStore;

    @Override
    public void execute(DelegateExecution execution) {
        String title = (String) execution.getVariable("title");
        String description = (String) execution.getVariable("description");
        Long authorId = toLong(execution.getVariable("authorId"));
        String fileRef = (String) execution.getVariable("fileRef");

        MultipartFile file = multipartFileStore.get(fileRef);

        PinResponse response = pinService.createPinWithFile(
                title,
                description,
                authorId,
                file
        );

        execution.setVariable("pinId", response.getId());
        execution.setVariable("pinTitle", response.getTitle());
        execution.setVariable("pinDescription", response.getDescription());
        execution.setVariable("pinImageUrl", response.getImageUrl());
        execution.setVariable("pinImageKey", response.getImageKey());
        execution.setVariable("pinAuthorId", response.getAuthorId());
        execution.setVariable(
                "pinCreatedAt",
                response.getCreatedAt() == null ? null : response.getCreatedAt().toString()
        );
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Long longValue) {
            return longValue;
        }

        if (value instanceof Number numberValue) {
            return numberValue.longValue();
        }

        return Long.valueOf(value.toString());
    }
}