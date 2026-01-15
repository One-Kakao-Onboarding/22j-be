package be.util.exception;

import org.springframework.http.*;

public class UnauthorizedException extends CustomException {

    public UnauthorizedException(String message) {
        this(message, null);
    }

    public UnauthorizedException(String message, Object data) {
        super(HttpStatus.UNAUTHORIZED.value(), message, data);
    }
}
