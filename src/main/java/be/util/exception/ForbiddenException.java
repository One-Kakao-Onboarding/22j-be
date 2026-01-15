package be.util.exception;

import org.springframework.http.*;

public class ForbiddenException extends CustomException {

    public ForbiddenException(String message) {
        this(message, null);
    }

    public ForbiddenException(String message, Object data)  {
        super(HttpStatus.FORBIDDEN.value(), message, data);
    }
}
