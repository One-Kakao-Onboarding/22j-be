package be.domain;

import java.util.*;
import lombok.*;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum Category {
    WORK("sdf"),
    DAILY("asdf"),
    AGENCY_DOCUMENT(""),
    PRIVATE_DOCUMENT(""),
    ETC(""),
    UNCLASSIFIED("");

    private final String description;

    public static Category[] getNetCategories() {
        return Arrays.stream(Category.values())
                .filter(c -> !UNCLASSIFIED.equals(c))
                .toArray(Category[]::new);
    }

    public static Category resolveOrNull(String name) {
        return Arrays.stream(Category.values())
                .filter(f -> f.name().equals(name))
                .findAny()
                .orElseGet(() -> null);
    }
}
