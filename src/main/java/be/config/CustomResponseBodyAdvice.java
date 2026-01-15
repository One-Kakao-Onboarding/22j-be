package be.config;

import be.util.api.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.core.*;
import org.springframework.http.*;
import org.springframework.http.converter.*;
import org.springframework.http.server.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.*;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class CustomResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private final RequestIdInjector requestIdInjector;

    @Override
    public boolean supports(
            @NonNull MethodParameter returnType,
            @NonNull Class<? extends HttpMessageConverter<?>> converterType
    ) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body, @NonNull MethodParameter returnType,
            @NonNull MediaType selectedContentType,
            @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
            @NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response
    ) {

        if (body != null) {
            if (body instanceof ApiResponse<?> api) {
                var statusCode = HttpStatus.valueOf(api.getCode());
                response.setStatusCode(statusCode);

                api.setRequestId(
                        requestIdInjector.currentRequestId()
                );

            } else {
                log.warn(
                        "Incompatible body type encountered: {}",
                        body.getClass().getSimpleName()
                );
            }
        }

        return body;
    }
}
