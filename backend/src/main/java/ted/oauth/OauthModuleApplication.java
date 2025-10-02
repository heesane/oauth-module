package ted.oauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class OauthModuleApplication {

    public static void main(String[] args) {
        SpringApplication.run(OauthModuleApplication.class, args);
    }
}
