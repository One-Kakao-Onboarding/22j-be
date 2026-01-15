package be.domain.exception;

import be.util.exception.*;

public class FileNotFoundException extends NotFoundException {

    private static final String message = "주어진 파일을 찾을 수 없습니다.";

    public FileNotFoundException() {
        super(message);
    }
}
