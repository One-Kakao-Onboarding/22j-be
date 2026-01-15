package be.util.exception;

import org.springframework.http.*;

public class NotFoundException extends CustomException {

    public NotFoundException(String message) {
        this(message, null);
    }

    public NotFoundException(String message, Object data) {
        super(HttpStatus.NOT_FOUND.value(), message, data);
    }
}
