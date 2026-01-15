package be.domain.exception;

import be.util.exception.*;
import org.springframework.http.*;

public class FailedToRecordFileException extends ExpectableServerException {

    private static final String defaultClientShowMessage = "파일 저장중 문제가 발생했습니다.";

    public FailedToRecordFileException() {
        this(null);
    }

    public FailedToRecordFileException(String message) {
        this(message, null);
    }

    public FailedToRecordFileException(String message, Throwable cause) {
        this(message, defaultClientShowMessage, cause);
    }

    public FailedToRecordFileException(
            String message, String clientShowMessage, Throwable cause
    ) {
        super(HttpStatus.INTERNAL_SERVER_ERROR.value(), message, clientShowMessage, cause);
    }
}
