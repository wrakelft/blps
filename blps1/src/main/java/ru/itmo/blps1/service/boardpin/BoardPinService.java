package ru.itmo.blps1.service.boardpin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import ru.itmo.blps1.mapper.BoardPinMapper;
import ru.itmo.blps1.mapper.PinMapper;
import ru.itmo.blps1.repository.BoardPinRepository;
import ru.itmo.blps1.service.board.BoardServiceInt;
import ru.itmo.blps1.service.pin.PinServiceInt;
import ru.itmo.blps1.service.storage.FileStorageService;
import ru.itmo.blps1.service.user.UserServiceInt;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardPinService implements BoardPinServiceInt {

    private final BoardPinRepository boardPinRepository;
    private final BoardServiceInt boardService;
    private final PinServiceInt pinService;
    private final UserServiceInt userService;
    private final BoardPinMapper boardPinMapper;
    private final PinMapper pinMapper;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public BoardPinResponse saveExistingPinToBoard(Long boardId, Long pinId) {
        Board board = boardService.getBoardEntityById(boardId);
        Pin pin = pinService.getPinEntityById(pinId);

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

    @Override
    @Transactional(readOnly = true)
    public List<PinResponse> getPinsByBoardId(Long boardId) {
        boardService.getBoardEntityById(boardId);

        return boardPinRepository.findAllByBoardId(boardId)
                .stream()
                .map(BoardPin::getPin)
                .map(pinMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CreatePinInBoardResponse createPinInBoard(
            Long boardId,
            String title,
            String description,
            Long authorId,
            MultipartFile file
    ) {
        validateCreatePinRequest(title, authorId);

        Board board = boardService.getBoardEntityById(boardId);
        User author = userService.getUserEntityById(authorId);
        FileUploadResponse uploadResponse = fileStorageService.uploadImage(file);

        try {
            Pin savedPin = pinService.createPinEntity(title, description, author, uploadResponse);

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
            fileStorageService.deleteFile(uploadResponse.getImageKey());
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