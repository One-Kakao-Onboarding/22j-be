package be.service;

import be.domain.*;
import java.util.*;
import java.util.Map.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.springframework.stereotype.*;

@Component
public class CategoryRecoder {

    private static final Comparator<LongAdder> longDesc
            = new Comparator<LongAdder>() {
        @Override
        public int compare(LongAdder o1, LongAdder o2) {
            long l1 = o1.longValue();
            long l2 = o2.longValue();

            return Long.compare(l2, l1);
        }
    };

    private static final Map<Category, LongAdder> visitCountMap;
    private static final Queue<Category> top20LatestAddedCategory;


    static {
        visitCountMap = new ConcurrentHashMap<>();
        for (Category category : Category.values()) {
            visitCountMap.put(category, new LongAdder());
        }

        top20LatestAddedCategory = new ConcurrentLinkedQueue<>();
    }

    public void increaseVisitCount(Category category) {
        visitCountMap.get(category).increment();
    }

    public void recordAddedCategory(Category category) {
        if (top20LatestAddedCategory.size() >= 20) {
            top20LatestAddedCategory.poll();
        }

        top20LatestAddedCategory.add(category);
    }

    public Category getMaxVisitedCategory() {
        return visitCountMap.entrySet().stream()
                .sorted(Comparator.comparing(Entry::getValue, longDesc))
                .map(Entry::getKey)
                .findFirst()
                .orElse(Category.HEALTH);
    }

    public String representVisitCount() {
        StringBuilder builder = new StringBuilder();
        builder.append("사용자 카테고리 방문 횟수")
                .append('\n');

        for (Category category : Category.values()) {
            long cnt = visitCountMap.get(category).longValue();
            builder.append(String.format("%s - %d", category.name(), cnt))
                    .append('\n');
        }

        return builder.toString();
    }

    public String representLatestAddedCategory() {
        StringBuilder builder = new StringBuilder();
        builder.append("최근 파일이 추가된 카테고리")
                .append('\n');

        Category[] latest = top20LatestAddedCategory.toArray(Category[]::new);
        for (int i = latest.length - 1; i >= 0; i--) {
            int seq = latest.length - i;
            Category category = latest[i];
            builder.append(String.format("%d - %s\n", seq, category.name()));
        }

        return builder.toString();
    }
}
