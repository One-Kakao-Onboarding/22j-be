package be.service;

import be.controller.dto.*;
import be.domain.*;
import be.domain.exception.*;
import be.repository.*;
import be.util.*;
import java.util.*;
import java.util.concurrent.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.dao.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import org.springframework.web.multipart.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final UuidProvider uuidProvider;
    private final FileIO fileIO;
    private final FileRepository fileRepository;
    private final FileCategoryExtractor fileCategoryExtractor;
    private final FileSummarizer fileSummarizer;
    private final FileTagExtractor fileTagExtractor;
    private final TagRepository tagRepository;
    private final FileVectorRepository fileVectorRepository;

    public List<File> getFiles(Category category, FileType fileType) {

        List<File> find;

        if (category == null && fileType == null) {
            find = fileRepository.findAllBy();
        } else if (fileType == null) {
            find = fileRepository.findByCategory(category);
        } else if (category == null) {
            find = fileRepository.findByFileType(fileType);
        } else {
            find = fileRepository.findByCategoryAndFileType(category, fileType);
        }

        return find;
    }

    public FileInfo getFileInfo(Long fileId) {
        File find = fileRepository.findById(fileId)
                .orElseThrow(FileNotFoundException::new);

        return new FileInfo(find.getId(), find.getFileMediaType(), find.getSavedFileName());
    }

    public List<File> searchFiles(String query, Integer topK, Double similarityThreshold, Category category, FileType fileType) {
        int k = (topK != null && topK > 0) ? topK : 10;
        double threshold = (similarityThreshold != null && similarityThreshold >= 0 && similarityThreshold <= 1) 
                ? similarityThreshold : 0.7;
        
        log.info("Searching files with query: {}, topK: {}, threshold: {}, category: {}, fileType: {}", 
                query, k, threshold, category, fileType);
        List<File> results = fileVectorRepository.searchSimilarFiles(query, k, threshold, category, fileType);
        log.info("Found {} similar files", results.size());
        
        return results;
    }

    @Transactional
    public File saveFile(MultipartFile multipartFile) {
        // 1. 파일 메타데이터 생성
        String originalFileName = multipartFile.getOriginalFilename();
        String newFileName = uuidProvider.getRandomStringUUID();
        String fileMediaType = multipartFile.getContentType();

        // 2. 파일을 먼저 저장 (LLM이 파일 내용을 읽을 수 있도록)
        fileIO.transferMultipartFile(multipartFile, newFileName);
        log.info("Saved file with content type: {}", fileMediaType);

        // 3. 미디어 타입 기반으로 FileType 추출
        FileType fileType = FileType.fromMediaType(fileMediaType);
        log.info("Resolved file type: {}", fileType);

        // 4. 임시 File 엔티티 생성 (LLM 분석용)
        File tempFile = File.builder()
                .originalFileName(originalFileName)
                .savedFileName(newFileName)
                .fileMediaType(fileMediaType)
                .fileType(fileType)
                .build();

        // 5. LLM을 활용한 메타데이터 추출 (병렬 실행)
        CompletableFuture<String> summaryFuture = CompletableFuture.supplyAsync(
                () -> fileSummarizer.summarize(tempFile)
        );
        CompletableFuture<List<Category>> categoriesFuture = CompletableFuture.supplyAsync(
                () -> fileCategoryExtractor.extractCategory(tempFile)
        );
        CompletableFuture<List<String>> tagsFuture = CompletableFuture.supplyAsync(
                () -> fileTagExtractor.extractTags(tempFile)
        );

        // 모든 LLM 호출이 완료될 때까지 대기
        CompletableFuture.allOf(summaryFuture, categoriesFuture, tagsFuture).join();

        // 결과 병합
        String summary = summaryFuture.join();
        List<Category> categories = categoriesFuture.join();
        List<String> tagDescriptions = tagsFuture.join();

        log.info("Extracted categories: {}", categories);
        log.info("Generated summary: {}", summary);
        log.info("Generated tags: {}", tagDescriptions);

        // 6. 태그 생성
        List<Tag> tags = new ArrayList<>();
        for (String tagDescription : tagDescriptions) {
            Tag tag = tagRepository.findByDescription(tagDescription)
                    .orElseGet(() -> Tag.builder()
                            .description(tagDescription)
                            .build());
            tags.add(tag);
        }

        // 7. File 엔티티 생성 및 메타데이터 enrichment
        File file = File.builder()
                .originalFileName(originalFileName)
                .savedFileName(newFileName)
                .fileMediaType(fileMediaType)
                .fileType(fileType)
                .build();
        file.enrichMetadata(summary, categories, tags);

        // 8. File 엔티티 저장
        File savedFile;
        try {
            savedFile = fileRepository.save(file);
        } catch (DataIntegrityViolationException e) {
            String errMsg = String.format(
                    "Failed to record entity due to ex: %s",
                    e.getClass().getSimpleName()
            );
            FailedToRecordFileException ex = new FailedToRecordFileException(errMsg, e);
            log.warn(errMsg, ex);
            throw ex;
        }
        // 9. 파일 벡터화 저장
        fileVectorRepository.save(savedFile);
        log.info("Successfully saved file with id: {}", savedFile.getId());
        return savedFile;
    }
}
