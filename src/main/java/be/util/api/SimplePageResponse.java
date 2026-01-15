package be.util.api;

import jakarta.annotation.*;
import java.util.*;

public record SimplePageResponse<T>(
        int pageNoRequest,
        int pageSizeRequest,
        int numOfPagedElements,
        long numOfTotalElements,
        boolean hasNext,
        List<T> pagedElements
) {

    public SimplePageResponse(
            int pageNoRequest, int pageSizeRequest, long numOfTotalElements, boolean hasNext,
            @Nonnull List<T> pagedElements
    ) {
        this(
                pageNoRequest, pageSizeRequest, pagedElements.size(),
                numOfTotalElements, hasNext, new ArrayList<>(pagedElements)
        );
    }

    public SimplePageResponse(List<T> pagedElements) {
        this(
                -1, -1, -1, false, pagedElements
        );
    }
}
