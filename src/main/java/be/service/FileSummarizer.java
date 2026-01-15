package be.service;

import be.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.content.Media;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileSummarizer {
    private final ChatClient chatClient;
    private final FileIO fileIO;

    private static final String systemPrompt, userInputPrompt;

    static {
        systemPrompt = """
                당신은 문서 요약의 전문가입니다.
                당신의 업무는 주어진 문서의 이름, 내용 등을 상세히 분석하여 **문서를 한 줄로 요약하는 것입니다.**
                
                문서 요약은 **사용자가 정보를 빠르게 파악할 수 있게 제공** 되어야 하며,
                **그럼에도 문서의 주요 목적이나 내용이 생략되어선 안됩니다.**
                """;

        userInputPrompt = """
                다음 제공된 정보는 문서의 정보입니다.
                문서의 내용을 분석해,** 한국어로 한 줄로 요약해 주세요.**
                """;
    }

    public String summarize(File file) {
        Media media = new Media(getMimeType(file), getFileResource(file));
        log.info(media.toString());
        log.info(media.getMimeType().toString());
        return chatClient.prompt()
                .system(s -> s.text(systemPrompt)
                )
                .user(u -> u.text(userInputPrompt)
                        .media(new Media(getMimeType(file), getFileResource(file)))
                )
                .call()
                .content();
    }

    private MimeType getMimeType(File file) {
        return MimeType.valueOf(file.getFileMediaType().toString());
    }

    private Resource getFileResource(File file) {
        return new ByteArrayResource(fileIO.getFileData(file.getSavedFileName()));
    }

}
