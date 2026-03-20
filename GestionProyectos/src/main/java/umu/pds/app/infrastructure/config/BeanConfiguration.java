package umu.pds.app.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import umu.pds.app.application.ports.input.GestionTableroUseCase;
import umu.pds.app.application.services.TableroService;
import umu.pds.app.domain.ports.output.TableroRepository;
import umu.pds.app.infrastructure.persistence.InMemoryTableroRepository;

@Configuration
public class BeanConfiguration {

    @Bean
    public TableroRepository tableroRepository() {
        return new InMemoryTableroRepository();
    }

    @Bean
    public GestionTableroUseCase gestionTableroUseCase(TableroRepository tableroRepository) {
        return new TableroService(tableroRepository);
    }
}
