package be.domain.exception;

import be.util.exception.*;

public class UnacceptableContentTypeException extends BadRequestException {

    public UnacceptableContentTypeException(String message) {
        super(message);
    }
}
