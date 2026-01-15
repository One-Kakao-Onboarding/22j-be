package be.controller.dto;

import org.springframework.http.*;

public record FileInfo(
        Long fileId,
        MediaType fileMediaType,
        String savedFileName
) {

}
