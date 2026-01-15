package be.service;

import be.domain.*;
import java.util.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.ai.chat.client.*;
import org.springframework.core.*;
import org.springframework.stereotype.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryRecommender {

    private final ChatClient chatClient;
    private final CategoryRecoder categoryRecoder;

    private static final String systemPrompt;

    static {
        StringBuilder systemPromptBuilder = new StringBuilder(String.format(
                """
                        당신은 추천 전문가입니다.
                        당신의 업무는 제공된 사용자 행동 정보를 통해 **사용자에게 추천할 카테고리 종류를 제공하는 것입니다.**
                        따라서 당신은 치밀한 분석을 통해 **시스템에 존재하는 카테고리에 한해 사용자 친화적으로 추천을 제공해야 합니다.**
                        
                        **시스템에 존재하는 카테고리 종류는 다음과 같습니다** : %s
                        
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
    }

    public List<Category> recommendCategory() {
        String visitCount = categoryRecoder.representVisitCount();
        String latestAdded = categoryRecoder.representLatestAddedCategory();

        log.info("Visit count : {}", visitCount);
        log.info("Latest added : {}", latestAdded);

        String userInputPrompt = buildUserInputPrompt(
                visitCount, latestAdded
        );

        return chatClient.prompt()
                .system(s -> s.text(systemPrompt)
                )
                .user(u -> u.text(userInputPrompt)
                )
                .call()
                .entity(new ParameterizedTypeReference<>() {
                });
    }

    private String buildUserInputPrompt(String... userBehaviors) {

        StringBuilder userInputPromptBuilder = new StringBuilder("""
                사용자의 행동 정보를 제공하겠습니다.
                정보를 통해 카테고리 최대 5 개를 추천해주세요.
                
                """);

        for (int i = 0; i < userBehaviors.length; i++) {
            String behavior = userBehaviors[i];
            userInputPromptBuilder.append(String.format("""
                            %d : %s
                            """.trim(), i + 1, behavior))
                    .append('\n');
        }

        return userInputPromptBuilder.toString();
    }
}
