package be.util;

import java.util.*;
import org.springframework.stereotype.*;

@Component
public class UuidProvider {

    public String getRandomStringUUID()   {
        return UUID.randomUUID().toString();
    }

}
