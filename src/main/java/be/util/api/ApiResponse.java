package be.util.api;

import lombok.*;
import org.springframework.http.*;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public final class ApiResponse<T> {

    private static final int
            OK = HttpStatus.OK.value(),
            CREATED = HttpStatus.CREATED.value(),
            ACCEPTED = HttpStatus.ACCEPTED.value();

    private final boolean success;
    private final int code;
    private final T data;
    private final String message;

    @Setter(AccessLevel.PUBLIC)
    private String requestId;

    public static <T> ApiResponse<T> success() {
        return success(null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(data, null);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return build(OK, data, message);
    }

    public static <T> ApiResponse<T> created(T data) {
        return created(data, null);
    }

    public static <T> ApiResponse<T> created(T data, String message) {
        return build(CREATED, data, message);
    }

    public static <T> ApiResponse<T> accepted(T data) {
        return accepted(data, null);
    }

    public static <T> ApiResponse<T> accepted(T data, String message) {
        return build(ACCEPTED, data, message);
    }

    public static <T> ApiResponse<T> build(int code, T data, String message) {
        return new ApiResponse<>(true, code, data, message);
    }

    public static <T> ApiResponse<T> fail(int code, T data, String message) {
        return new ApiResponse<>(false, code, data, message);
    }
}
