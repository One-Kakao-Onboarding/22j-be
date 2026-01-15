package be.util.api;

import jakarta.validation.constraints.*;

public record SimplePageRequest(
        @Min(value = 0, message = "페이지 번호는 0 보다 크거나 같아야 합니다")
        Integer pageNum,
        @Min(value = 1, message = "페이지 크기는 1 보다 크거나 같아야 합니다")
        Integer pageSize
) {

    private static final int
            DEFAULT_PAGE_NUM = 0,
            DEFAULT_PAGE_SIZE = 10;

    @Override
    public Integer pageNum() {
        return pageNum == null ? DEFAULT_PAGE_NUM : pageNum;
    }

    @Override
    public Integer pageSize() {
        return pageSize == null ? DEFAULT_PAGE_SIZE : pageSize;
    }
}
