package se.idpsim.Idpsimulator.service.saml;

import jakarta.annotation.PostConstruct;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.springframework.context.annotation.Configuration;

@Configuration
class OpenSamlConfig {

    @PostConstruct
    static void init() {
        try {
            InitializationService.initialize();
        } catch (InitializationException e) {
            throw new RuntimeException("Could not initialize OpenSaml", e);
        }
    }
}
