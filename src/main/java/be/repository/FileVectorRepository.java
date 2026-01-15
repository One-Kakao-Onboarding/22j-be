package be.repository;

import be.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
@RequiredArgsConstructor
public class FileVectorRepository {
    private final VectorStore vectorStore;
    private final FileRepository fileRepository;
    private final TagRepository tagRepository;

    public void save(File file) {
        Document document = toDocument(file);
        vectorStore.add(List.of(document));
    }

    public List<File> searchSimilarFiles(String query, int topK, double similarityThreshold, Category category, FileType fileType) {
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(similarityThreshold)
                .build();
        
        List<Document> documents = vectorStore.similaritySearch(searchRequest);
        return documents.stream()
                .map(this::toFile)
                .filter(Objects::nonNull)
                .filter(file -> matchesCategory(file, category))
                .filter(file -> matchesFileType(file, fileType))
                .collect(Collectors.toList());
    }

    private boolean matchesCategory(File file, Category category) {
        if (category == null) {
            return true;
        }
        return file.getCategories() != null && file.getCategories().contains(category);
    }

    private boolean matchesFileType(File file, FileType fileType) {
        if (fileType == null) {
            return true;
        }
        return fileType.equals(file.getFileType());
    }

    private Document toDocument(File file) {
        BiFunction<String, Object, String> format = (label, value) ->
                (value == null) ? null : label + ": " + value.toString();

        // 검색에 적합한 텍스트만 포함
        String tagDescriptions = file.getTags().stream()
                .map(Tag::getDescription)
                .collect(Collectors.joining(", "));

        String textForEmbedding = Stream.of(
                format.apply("파일명", file.getOriginalFileName()),
                format.apply("요약", file.getFileOverview()),
                format.apply("카테고리", file.getCategories()),
                format.apply("태그", tagDescriptions)
                ).filter(Objects::nonNull)
                .collect(Collectors.joining("\n"));

        // File 엔티티를 복구할 수 있는 모든 메타데이터 저장
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("id", file.getId());
        metadata.put("originalFileName", file.getOriginalFileName());
        metadata.put("savedFileName", file.getSavedFileName());
        metadata.put("fileMediaType", file.getFileMediaType().toString());
        metadata.put("fileType", file.getFileType().name());
        metadata.put("fileOverview", file.getFileOverview());
        
        // 카테고리를 문자열 리스트로 저장
        if (file.getCategories() != null && !file.getCategories().isEmpty()) {
            List<String> categoryNames = file.getCategories().stream()
                    .map(Category::name)
                    .collect(Collectors.toList());
            metadata.put("categories", categoryNames);
        }
        
        // 태그를 문자열 리스트로 저장
        if (file.getTags() != null && !file.getTags().isEmpty()) {
            List<String> tagDescriptionsList = file.getTags().stream()
                    .map(Tag::getDescription)
                    .collect(Collectors.toList());
            metadata.put("tags", tagDescriptionsList);
        }

        return new Document(
                generateDeterministicUuid(file.getClass().getName(), file.getId()),
                textForEmbedding,
                metadata
        );
    }

    private File toFile(Document document) {
        try {
            Map<String, Object> metadata = document.getMetadata();
            
            // 메타데이터로부터 File 복구 (우선)
            try {
                File reconstructedFile = reconstructFileFromMetadata(metadata);
                if (reconstructedFile != null) {
                    return reconstructedFile;
                }
            } catch (Exception e) {
                // 메타데이터 복구 실패 시 DB 조회 시도
            }
            
            // 메타데이터 복구 실패 시 DB에서 ID로 조회
            Long fileId = getLongFromMetadata(metadata, "id");
            if (fileId != null) {
                Optional<File> fileOptional = fileRepository.findById(fileId);
                if (fileOptional.isPresent()) {
                    return fileOptional.get();
                }
            }
            
            return null;
            
        } catch (Exception e) {
            // 복구 실패 시 null 반환
            return null;
        }
    }

    private File reconstructFileFromMetadata(Map<String, Object> metadata) {
        String originalFileName = (String) metadata.get("originalFileName");
        String savedFileName = (String) metadata.get("savedFileName");
        String fileMediaType = (String) metadata.get("fileMediaType");
        String fileTypeStr = (String) metadata.get("fileType");
        String fileOverview = (String) metadata.get("fileOverview");
        
        FileType fileType = fileTypeStr != null ? FileType.valueOf(fileTypeStr) : FileType.ETC;
        
        // 카테고리 복구
        List<Category> categories = new ArrayList<>();
        Object categoriesObj = metadata.get("categories");
        if (categoriesObj instanceof List<?>) {
            for (Object catObj : (List<?>) categoriesObj) {
                if (catObj instanceof String) {
                    try {
                        categories.add(Category.valueOf((String) catObj));
                    } catch (IllegalArgumentException ignored) {}
                }
            }
        }
        
        // 태그 복구
        List<Tag> tags = new ArrayList<>();
        Object tagsObj = metadata.get("tags");
        if (tagsObj instanceof List<?>) {
            for (Object tagObj : (List<?>) tagsObj) {
                if (tagObj instanceof String) {
                    String tagDesc = (String) tagObj;
                    Tag tag = tagRepository.findByDescription(tagDesc)
                            .orElseGet(() -> Tag.builder().description(tagDesc).build());
                    tags.add(tag);
                }
            }
        }
        
        File file = File.builder()
                .originalFileName(originalFileName)
                .savedFileName(savedFileName)
                .fileMediaType(fileMediaType)
                .fileType(fileType)
                .fileOverview(fileOverview)
                .categories(categories)
                .build();
        
        // 태그는 별도로 설정 (enrichMetadata가 아닌 직접 설정)
        file.enrichMetadata(fileOverview, categories, tags);
        
        return file;
    }

    private Long getLongFromMetadata(Map<String, Object> metadata, String key) {
        Object value = metadata.get(key);
        if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof Integer) {
            return ((Integer) value).longValue();
        } else if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private String generateDeterministicUuid(String className, long originalId) {
        String input = className + ":" + originalId;
        UUID uuid = UUID.nameUUIDFromBytes(input.getBytes(StandardCharsets.UTF_8));
        return uuid.toString();
    }

}
