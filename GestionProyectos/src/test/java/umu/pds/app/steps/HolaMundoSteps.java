package umu.pds.app.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class HolaMundoSteps {

	@Autowired
	private TestRestTemplate restTemplate;

	@LocalServerPort
	private int port; // puerto aleatorio de Spring boot test

	private String respuesta;

	@When( "el cliente llama al {string}" )
	public void llamarEndpoint( String path ) {
		if ( !path.startsWith( "/" ) ) {
			path = "/" + path;
		}
		String url = "http://localhost:" + port + path;
		respuesta = restTemplate.getForObject( url, String.class );
	}

	@Then( "la respuesta es {string}" )
	public void comprobarRespuesta( String esperado ) {
		assertEquals( esperado, respuesta );
	}
}
