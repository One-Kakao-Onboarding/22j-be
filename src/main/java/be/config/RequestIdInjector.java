package be.config;

import jakarta.servlet.*;
import java.util.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

@Component
public class RequestIdInjector implements ServletRequestListener {
    
    private final String requestIdMdcKey;

    public RequestIdInjector(
            @Value("${mdc-key.request-id}")
            String requestIdMdcKey
    ) {
        this.requestIdMdcKey = requestIdMdcKey;
    }

    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        MDC.put(
                requestIdMdcKey, this.getRandomId()
        );
    }

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        MDC.remove(requestIdMdcKey);
    }

    private String getRandomId() {
        return UUID.randomUUID().toString();
    }

    public String currentRequestId() {
        return MDC.get(requestIdMdcKey);
    }
}
