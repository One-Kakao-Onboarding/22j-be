package be.service;


import be.domain.*;
import java.util.*;
import lombok.*;
import org.springframework.ai.chat.client.*;
import org.springframework.ai.content.*;
import org.springframework.core.*;
import org.springframework.core.io.*;
import org.springframework.stereotype.*;
import org.springframework.util.*;

@Service
@RequiredArgsConstructor
public class FileCategoryExtractor {
    private final ChatClient chatClient;
    private final FileIO fileIO;

    public List<Category> extractCategory(File file) {
        return chatClient.prompt()
                .system(s -> s.text("""
                        You are an expert at classifying documents into categories.
                        Given the content of a document, identify the most appropriate categories from the following list: {categories}.
                        """)
                        .param("categories", Category.values())
                )
                .user(u -> u.text("""
                        Here is the content of the document
                        Please provide the categories that best fit this document.
                        """)
                        .media(new Media(getMimeType(file), getFileResource(file)))
                )
                .call()
                .entity(new ParameterizedTypeReference<>() {});
    }

    private MimeType getMimeType(File file) {
        return MimeType.valueOf(file.getFileMediaType().toString());
    }

    private Resource getFileResource(File file) {
        return new ByteArrayResource(fileIO.getFileData(file.getSavedFileName()));
    }

}
