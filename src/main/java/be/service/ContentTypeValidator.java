package be.service;

import org.springframework.stereotype.*;

@Component
public class ContentTypeValidator {

    public boolean acceptableType(String contentType) {

        if (contentType == null)    {
            return false;
        }

        if (contentType.startsWith("text/")) {
            return true;
        }

        if (contentType.startsWith("audio/")) {
            return true;
        }

        if (contentType.equals("application/pdf")) {
            return true;
        }

        if (contentType.startsWith("image/png")) {
            return true;
        }

        if (contentType.startsWith("image/jpeg")) {
            return true;
        }

        if (contentType.equals("audio/mpeg")) {
            return true;
        }

        return false;
    }
}
