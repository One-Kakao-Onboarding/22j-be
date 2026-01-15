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

    public void save(File file) {
        Document document = toDocument(file);
        vectorStore.add(List.of(document));
    }

    public List<Document> similaritySearch(String query, int topK, double similarityThreshold) {
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(similarityThreshold)
                .build();
        return vectorStore.similaritySearch(searchRequest);
    }

    private Document toDocument(File file) {
        BiFunction<String, Object, String> format = (label, value) ->
                (value == null) ? null : label + ": " + value.toString();

        String textForEmbedding = Stream.of(
                format.apply("originalFileName", file.getOriginalFileName()),
                format.apply("fileOverView", file.getFileOverview()),
                format.apply("fileType", file.getFileType()),
                format.apply("category", file.getCategories())
                ).filter(Objects::nonNull)
                .collect(Collectors.joining("\n"));

        Map<String, Object> metadata = Stream.of(
                        Map.entry("fileType", file.getFileType()),
                        Map.entry("category", file.getCategories())
                )
                .filter(e -> e.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return new Document(
                generateDeterministicUuid(file.getClass().getName(),file.getId()),
                textForEmbedding,
                metadata
        );
    }

    private String generateDeterministicUuid(String className, long originalId) {
        String input = className + ":" + originalId;
        UUID uuid = UUID.nameUUIDFromBytes(input.getBytes(StandardCharsets.UTF_8));
        return uuid.toString();
    }

}
