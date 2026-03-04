package umu.pds.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("classpath:application-secrets.properties")
public class GestionProyectosApplication {

	public static void main(String[] args) {
		SpringApplication.run(GestionProyectosApplication.class, args);
	}

}
	