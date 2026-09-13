package app.prompts.infrastructure.config;

import java.time.Clock;
import app.prompts.application.port.InfrastructurePortMarker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PromptsInfrastructureConfig implements InfrastructurePortMarker {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}

