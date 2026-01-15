package be.config;

import java.util.*;
import lombok.*;
import org.springframework.boot.web.error.*;
import org.springframework.boot.web.servlet.error.*;
import org.springframework.stereotype.*;
import org.springframework.web.context.request.*;

@Component
@RequiredArgsConstructor
public class CustomErrorAttributes extends DefaultErrorAttributes {

    private final RequestIdInjector requestIdInjector;

    @Override
    public Map<String, Object> getErrorAttributes(
            WebRequest webRequest,
            ErrorAttributeOptions options
    ) {
        var errorAttributes = super.getErrorAttributes(webRequest, options);

        String requestId = requestIdInjector.currentRequestId();
        errorAttributes.put("requestId", requestId);

        return errorAttributes;
    }
}
