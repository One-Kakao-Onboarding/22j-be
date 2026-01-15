package be;

import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.data.jpa.repository.config.*;

@SpringBootApplication
@EnableJpaAuditing(modifyOnCreate = false)
@EnableJpaRepositories(considerNestedRepositories = true)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
