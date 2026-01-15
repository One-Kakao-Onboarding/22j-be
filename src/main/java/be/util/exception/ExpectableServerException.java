package be.util.exception;

import lombok.*;

@Getter
public abstract class ExpectableServerException extends CustomException {

    private final String clientShowMessage;

    protected ExpectableServerException(int code, String message, String clientShowMessage) {
        this(code, message, clientShowMessage, null, null);
    }

    protected ExpectableServerException(
            int code, String message, String clientShowMessage, Throwable cause
    )   {
        this(code, message, clientShowMessage, null, cause);
    }

    protected ExpectableServerException(
            int code, String message, String clientShowMessage, Object data, Throwable cause
    ) {
        super(code, message, data, cause);
        this.clientShowMessage = clientShowMessage;
    }
}
