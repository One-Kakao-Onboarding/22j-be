package be.domain;

import java.util.*;
import lombok.*;

@Getter
@RequiredArgsConstructor
public enum FileType {
    IMAGE_VIDEO("이미지 및 비디오"),
    DOCUMENT("문서"),
    LINK("링크"),
    ETC("기타");

    private final String description;

    public static FileType resolveOrNull(String name) {
        return Arrays.stream(FileType.values())
                .filter(f -> f.name().equals(name))
                .findFirst()
                .orElse(null);
    }

    public static FileType fromMediaType(String mediaType) {
        if (mediaType == null || mediaType.isEmpty()) {
            return ETC;
        }

        String lowerMediaType = mediaType.toLowerCase();

        // 이미지 및 비디오
        if (lowerMediaType.startsWith("image/") || lowerMediaType.startsWith("video/")) {
            return IMAGE_VIDEO;
        }

        // 문서
        if (lowerMediaType.startsWith("text/") ||
            lowerMediaType.startsWith("application/pdf") ||
            lowerMediaType.startsWith("application/msword") ||
            lowerMediaType.startsWith("application/vnd.openxmlformats-officedocument") ||
            lowerMediaType.startsWith("application/vnd.ms-excel") ||
            lowerMediaType.startsWith("application/vnd.ms-powerpoint") ||
            lowerMediaType.startsWith("application/rtf") ||
            lowerMediaType.startsWith("application/x-hwp") ||
            lowerMediaType.contains("document")) {
            return DOCUMENT;
        }

        // 링크 (URL 파일 등)
        if (lowerMediaType.contains("url") || lowerMediaType.contains("uri")) {
            return LINK;
        }

        // 기타
        return ETC;
    }
}
