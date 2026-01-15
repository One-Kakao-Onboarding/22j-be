package be.controller;

import be.controller.dto.*;
import be.domain.*;
import be.domain.exception.*;
import be.service.*;
import be.util.api.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import java.util.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
public class FileController {

    private final FileService fileService;
    private final FileIO fileIO;
    private final ContentTypeValidator contentTypeValidator;

    @GetMapping
    public ApiResponse<List<File>> getFiles(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String type
    ) {
        Category cat = Category.resolveOrNull(category);
        FileType fileType = FileType.resolveOrNull(type);

        List<File> resp = fileService.getFiles(cat, fileType);

        return ApiResponse.success(resp);
    }

    @GetMapping("/search")
    public ApiResponse<List<File>> searchFiles(
            @RequestParam String query,
            @RequestParam(required = false) Integer topK,
            @RequestParam(required = false) Double similarityThreshold,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String type
    ) {
        Category cat = Category.resolveOrNull(category);
        FileType fileType = FileType.resolveOrNull(type);
        
        List<File> results = fileService.searchFiles(query, topK, similarityThreshold, cat, fileType);
        return ApiResponse.success(results);
    }

    @GetMapping("/{file-id:\\d+}")
    public ResponseEntity<byte[]> getFile(
            @PathVariable("file-id") Long fileId
    ) {

        FileInfo info = fileService.getFileInfo(fileId);
        String fileName = info.savedFileName();
        MediaType fileMediaType = info.fileMediaType();

        byte[] file = fileIO.getFileData(fileName);

        return ResponseEntity.ok()
                .contentType(fileMediaType)
                .body(file);
    }

    @RequestBody(
            content = @Content(encoding = @Encoding(
                    name = "request", contentType = MediaType.APPLICATION_JSON_VALUE
            ))
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<File> saveFile(
            @RequestPart MultipartFile multipartFile
    ) {
        String contentType = multipartFile.getContentType();
        if (!contentTypeValidator.acceptableType(contentType)) {
            throw new UnacceptableContentTypeException(String.format(
                    "허용되지 않는 파일 형식입니다: %s",
                    contentType
            ));
        }

        File file = fileService.saveFile(multipartFile);
        return ApiResponse.created(file);
    }
}
