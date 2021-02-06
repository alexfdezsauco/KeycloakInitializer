package orc.keycloak;

import com.fasterxml.jackson.databind.ObjectMapper;
import orc.keycloak.models.Config;
import orc.keycloak.services.interfaces.KeycloakInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.io.InputStream;

@SpringBootApplication
public class SpringBootConsoleApplication
        implements CommandLineRunner {

    @Autowired
    private KeycloakInitializer keycloakInitializer;

    private static Logger LOG = LoggerFactory
            .getLogger(SpringBootConsoleApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SpringBootConsoleApplication.class, args);
    }

    @Override
    public void run(String... args) {
        keycloakInitializer.execute();
    }

    @Bean
    public Config getConfig(){
        InputStream resourceAsStream = this.getClass().getResourceAsStream("/config.json");
        Config config = null;
        try {
            config = new ObjectMapper().readValue(resourceAsStream, Config.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return config;
    }
}
