package be.util.exception;

import org.springframework.http.*;

public class BadRequestException extends CustomException {

    public BadRequestException(String message) {
        this(message, null, null);
    }

    public BadRequestException(String message, Object data) {
        this(message, data, null);
    }

    public BadRequestException(String message, Object data, Throwable cause) {
        super(HttpStatus.BAD_REQUEST.value(), message, data, cause);
    }
}
