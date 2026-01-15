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

    public String summarize(File file) {
        Media media = new Media(getMimeType(file), getFileResource(file));
        log.info(media.toString());
        log.info(media.getMimeType().toString());
        return chatClient.prompt()
                .system(s -> s.text("""
                        당신은 문서 요약 전문가입니다.
                        문서의 내용을 한국어로 컴팩트하게 한 줄로 요약해주세요.
                        핵심 내용만 간결하게 표현하되, 문서의 주요 목적이나 내용이 명확히 드러나도록 해주세요.
                        """)
                )
                .user(u -> u.text("""
                        다음 문서의 내용을 한국어로 한 줄로 요약해주세요.
                        """)
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
