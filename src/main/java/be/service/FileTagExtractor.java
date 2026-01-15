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

    private static final String systemPrompt, userInputPrompt;

    static {
        systemPrompt = """
                당신은 문서 태그 생성 전문가입니다.
                당신의 업무는 문서 내용을 상세히 분석하여 **적합한 태그를 제공하는 것입니다.**
                
                **태그는 3 개 이하로 생성하며, 각 태그는 문서의 핵심 주제나 내용을 나타내야 합니다.**
                태그는 한글 또는 영문 단어로 간결하게 작성해야 하며, 태그 글자수는 10 자 이하가 권장됩니다.
                """;

        userInputPrompt = """
                다음 제공된 정보는 문서의 정보입니다.
                문서의 내용을 분석해, **적합한 태그 3 개 이하 생성해주세요.**
                """;
    }

    public List<String> extractTags(File file) {
        return chatClient.prompt()
                .system(s -> s.text(systemPrompt)
                )
                .user(u -> u.text(userInputPrompt)
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
