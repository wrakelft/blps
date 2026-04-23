package ru.itmo.blps1.service.boardpin;

import org.springframework.web.multipart.MultipartFile;
import ru.itmo.blps1.dto.boardpin.BoardPinResponse;
import ru.itmo.blps1.dto.boardpin.CreatePinInBoardResponse;
import ru.itmo.blps1.dto.pin.PinResponse;

import java.util.List;

public interface BoardPinServiceInt {

    BoardPinResponse saveExistingPinToBoard(Long boardId, Long pinId);

    List<PinResponse> getPinsByBoardId(Long boardId);

    CreatePinInBoardResponse createPinInBoard(
            Long boardId,
            String title,
            String description,
            Long authorId,
            MultipartFile file
    );
}
