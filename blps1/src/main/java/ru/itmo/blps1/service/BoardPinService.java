package ru.itmo.blps1.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.itmo.blps1.dto.boardpin.BoardPinResponse;
import ru.itmo.blps1.dto.boardpin.CreatePinInBoardResponse;
import ru.itmo.blps1.dto.file.FileUploadResponse;
import ru.itmo.blps1.dto.pin.PinResponse;
import ru.itmo.blps1.entity.Board;
import ru.itmo.blps1.entity.BoardPin;
import ru.itmo.blps1.entity.Pin;
import ru.itmo.blps1.entity.User;
import ru.itmo.blps1.exception.BadRequestException;
import ru.itmo.blps1.exception.ConflictException;
import ru.itmo.blps1.exception.NotFoundException;
import ru.itmo.blps1.mapper.BoardPinMapper;
import ru.itmo.blps1.mapper.PinMapper;
import ru.itmo.blps1.repository.BoardPinRepository;
import ru.itmo.blps1.repository.BoardRepository;
import ru.itmo.blps1.repository.PinRepository;
import ru.itmo.blps1.repository.UserRepository;
import ru.itmo.blps1.service.storage.FileStorageService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardPinService {

    private final BoardRepository boardRepository;
    private final PinRepository pinRepository;
    private final BoardPinRepository boardPinRepository;
    private final UserRepository userRepository;
    private final BoardPinMapper boardPinMapper;
    private final PinMapper pinMapper;
    private final FileStorageService fileStorageService;

    public BoardPinResponse saveExistingPinToBoard(Long boardId, Long pinId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new NotFoundException("Board with id " + boardId + " not found"));

        Pin pin = pinRepository.findById(pinId)
                .orElseThrow(() -> new NotFoundException("Pin with id " + pinId + " not found"));

        if (boardPinRepository.existsByBoardIdAndPinId(boardId, pinId)) {
            throw new ConflictException("Pin is already saved to this board");
        }

        BoardPin boardPin = BoardPin.builder()
                .board(board)
                .pin(pin)
                .build();

        BoardPin savedBoardPin = boardPinRepository.save(boardPin);
        return boardPinMapper.toResponse(savedBoardPin);
    }

    public List<PinResponse> getPinsByBoardId(Long boardId) {
        if (!boardRepository.existsById(boardId)) {
            throw new NotFoundException("Board with id " + boardId + " not found");
        }

        return boardPinRepository.findAllByBoardId(boardId)
                .stream()
                .map(BoardPin::getPin)
                .map(pinMapper::toResponse)
                .toList();
    }

    public CreatePinInBoardResponse createPinInBoard(
            Long boardId,
            String title,
            String description,
            Long authorId,
            MultipartFile file
    ) {
        validateCreatePinRequest(title, authorId);

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new NotFoundException("Board with id " + boardId + " not found"));

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

            BoardPin boardPin = BoardPin.builder()
                    .board(board)
                    .pin(savedPin)
                    .build();

            BoardPin savedBoardPin = boardPinRepository.save(boardPin);

            return CreatePinInBoardResponse.builder()
                    .pin(pinMapper.toResponse(savedPin))
                    .boardPin(boardPinMapper.toResponse(savedBoardPin))
                    .build();
        } catch (Exception e) {
            fileStorageService.deleteFile(uploadResponse.getImageUrl());
            throw e;
        }
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