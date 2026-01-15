package be.domain;

import lombok.*;

@Getter
@RequiredArgsConstructor
public enum FileMediaType {
    PDF(org.springframework.http.MediaType.APPLICATION_PDF),
    GIF(org.springframework.http.MediaType.IMAGE_GIF),
    JPEG(org.springframework.http.MediaType.IMAGE_JPEG),
    PNG(org.springframework.http.MediaType.IMAGE_PNG),
    TEXT_PLAIN(org.springframework.http.MediaType.TEXT_PLAIN),
    ;

    private final org.springframework.http.MediaType mediaType;
}
