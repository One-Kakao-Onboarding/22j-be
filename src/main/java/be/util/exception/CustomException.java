package be.util.exception;

import lombok.*;

@Getter
public abstract class CustomException extends RuntimeException {

    private final int code;
    private final String message;
    private final Object data;

    protected CustomException(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    protected CustomException(int code, String message, Object data, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
        this.data = data;
    }
}
