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

    private static final String systemPrompt, userInputPrompt;

    static {
        StringBuilder systemPromptBuilder = new StringBuilder(String.format(
                """
                        당신은 문서 종류 분류 전문가입니다.
                        당신의 업무는 주어진 문서의 이름, 내용 등을 분석해 **문서가 어떤 종류인지 분류하는 것입니다.**
                        
                        **주어진 문서 종류는 다음과 같습니다** : %s
                        
                        **종류별 세부 설명은 다음과 같습니다** :
                        """,
                Arrays.toString(Category.values())
        ));

        for (Category category : Category.values()) {
            String cat = category.name();
            String desc = category.getDescription();

            systemPromptBuilder.append(String.format("- [%s] : %s\n", cat, desc));
        }

        systemPrompt = systemPromptBuilder.toString();

        userInputPrompt = """
                다음 제공된 정보는 문서의 정보입니다.
                이전 주어진 문서 종류 중, 해당 문서와 적합한 종류를 최소 1개, 최대 3개로 추려내어 종류를 제공해 주세요.
                """;
    }

    public List<Category> extractCategory(File file) {

        List<Category> extractedCategories = chatClient.prompt()
                .system(s -> s.text(systemPrompt)
                )
                .user(u -> u.text(userInputPrompt)
                        .media(new Media(getMimeType(file), getFileResource(file)))
                )
                .call()
                .entity(new ParameterizedTypeReference<>() {
                });

        // structured output을 검증하여 유효한 카테고리만 필터링
        List<Category> validCategories = extractedCategories.stream()
                .filter(Objects::nonNull)
                .filter(category -> Category.resolveOrNull(category.name()) != null)
                .toList();

        // 유효한 카테고리가 없으면 ETC로 처리
        if (validCategories.isEmpty()) {
            return List.of(Category.ETC);
        }

        return validCategories;
    }

    private MimeType getMimeType(File file) {
        return MimeType.valueOf(file.getFileMediaType().toString());
    }

    private Resource getFileResource(File file) {
        return new ByteArrayResource(fileIO.getFileData(file.getSavedFileName()));
    }

}
