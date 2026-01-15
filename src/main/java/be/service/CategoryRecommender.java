package be.service;

import be.domain.*;
import java.util.*;
import java.util.concurrent.atomic.*;
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
    
    // 영구 캐시: 애플리케이션 실행 중 무한히 유지
    private final AtomicReference<List<Category>> cachedRecommendations = new AtomicReference<>();

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

    /**
     * 캐시를 우선 조회하고, 없으면 LLM 호출하여 캐시 갱신
     */
    public List<Category> recommendCategory() {
        List<Category> cached = cachedRecommendations.get();
        
        if (cached != null) {
            log.info("Returning cached category recommendations: {}", cached);
            return cached;
        }
        
        log.info("Cache miss - generating new category recommendations");
        return refreshRecommendations();
    }
    
    /**
     * LLM을 호출하여 캐시를 갱신 (비동기 백그라운드 작업에서 호출)
     */
    public List<Category> refreshRecommendations() {
        String visitCount = categoryRecoder.representVisitCount();
        String latestAdded = categoryRecoder.representLatestAddedCategory();

        log.info("Visit count : {}", visitCount);
        log.info("Latest added : {}", latestAdded);

        String userInputPrompt = buildUserInputPrompt(
                visitCount, latestAdded
        );

        List<Category> recommendations = chatClient.prompt()
                .system(s -> s.text(systemPrompt)
                )
                .user(u -> u.text(userInputPrompt)
                )
                .call()
                .entity(new ParameterizedTypeReference<>() {
                });
        
        // 캐시 업데이트
        cachedRecommendations.set(recommendations);
        log.info("Updated category recommendation cache: {}", recommendations);
        
        return recommendations;
    }

    private String buildUserInputPrompt(String... userBehaviors) {

        StringBuilder userInputPromptBuilder = new StringBuilder("""
                사용자의 행동 정보를 제공하겠습니다.
                정보를 통해 카테고리 최소 7개 최대 10개를 추천 순으로 제공해주세요.
                
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
