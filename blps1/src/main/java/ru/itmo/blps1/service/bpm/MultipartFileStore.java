package ru.itmo.blps1.service.bpm;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ru.itmo.blps1.exception.BadRequestException;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MultipartFileStore {

    private final Map<String, MultipartFile> files = new ConcurrentHashMap<>();

    public String put(MultipartFile file) {
        String ref = UUID.randomUUID().toString();
        files.put(ref, file);
        return ref;
    }

    public MultipartFile get(String ref) {
        MultipartFile file = files.get(ref);

        if (file == null) {
            throw new BadRequestException("Multipart file not found by ref: " + ref);
        }

        return file;
    }

    public void remove(String ref) {
        files.remove(ref);
    }
}