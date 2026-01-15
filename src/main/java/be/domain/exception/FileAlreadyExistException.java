package be.domain.exception;

import be.util.exception.*;
import org.springframework.http.*;

public class FileAlreadyExistException extends ExpectableServerException {

    private static final String defaultMessage = "주어진 이름의 파일은 이미 존재합니다.";

    public FileAlreadyExistException(String message) {
        this(message, defaultMessage);
    }

    public FileAlreadyExistException(String message, String clientShowMessage) {
        super(HttpStatus.INTERNAL_SERVER_ERROR.value(), message, clientShowMessage);
    }
}
