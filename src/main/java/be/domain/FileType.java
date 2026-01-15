package be.domain;

import java.util.*;
import lombok.*;

@Getter
@RequiredArgsConstructor
public enum FileType {
    IMAGE_VIDEO(""),
    DOCUMENT(""),
    LINK(""),
    ETC("");

    private final String description;

    public static FileType resolveOrNull(String name) {
        return Arrays.stream(FileType.values())
                .filter(f -> f.name().equals(name))
                .findFirst()
                .orElseGet(() -> null);
    }
}
