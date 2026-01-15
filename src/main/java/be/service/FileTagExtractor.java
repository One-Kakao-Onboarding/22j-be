package be.service;

import be.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.content.Media;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FileTagExtractor {
    private final ChatClient chatClient;
    private final FileIO fileIO;

    public List<String> extractTags(File file) {
        return chatClient.prompt()
                .system(s -> s.text("""
                        당신은 문서 태그 생성 전문가입니다.
                        문서의 내용을 분석하여 적합한 태그를 생성해주세요.
                        태그는 3개 이하로 생성하며, 각 태그는 문서의 핵심 주제나 내용을 나타내야 합니다.
                        태그는 한글 또는 영문 단어로 간결하게 작성해주세요.
                        """)
                )
                .user(u -> u.text("""
                        다음 문서의 내용을 분석하여 적합한 태그 3개 이하를 생성해주세요.
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
