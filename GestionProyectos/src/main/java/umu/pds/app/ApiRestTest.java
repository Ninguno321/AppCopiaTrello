package umu.pds.app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping( "${app.server.path}" + "/public" )
public class ApiRestTest {

	
	@GetMapping(path = "/holaMundo")
	public String holamundo() {
		return "Hola mundo";
	}
	
	// http://localhost:8080/umu/pds/public/pagina
	 @GetMapping(value = "/pagina", produces = MediaType.TEXT_HTML_VALUE)
	    public String devolverHtml() {
	        return """
	                <!DOCTYPE html>
	                <html lang="es">
	                <head>
	                    <meta charset="UTF-8">
	                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
	                    <title>Página HTML de Prueba</title>
	                    <style>
	                        body {
	                            font-family: Arial, sans-serif;
	                            background-color: #f4f4f4;
	                            margin: 0;
	                            padding: 0;
	                            text-align: center;
	                        }
	                        header {
	                            background-color: #007BFF;
	                            color: white;
	                            padding: 1rem;
	                        }
	                        main {
	                            padding: 2rem;
	                        }
	                        button {
	                            background-color: #28a745;
	                            color: white;
	                            border: none;
	                            padding: 10px 20px;
	                            font-size: 16px;
	                            cursor: pointer;
	                            border-radius: 5px;
	                        }
	                        button:hover {
	                            background-color: #218838;
	                        }
	                        footer {
	                            background-color: #333;
	                            color: white;
	                            padding: 1rem;
	                            position: fixed;
	                            bottom: 0;
	                            width: 100%;
	                        }
	                    </style>
	                </head>
	                <body>

	                    <header>
	                        <h1>Bienvenido a mi página de prueba</h1>
	                    </header>

	                    <main>
	                        <p>Esta es una página HTML sencilla para probar estilos y scripts.</p>
	                        <button onclick="mostrarMensaje()">Haz clic aquí</button>
	                        <p id="mensaje"></p>
	                    </main>

	                    <footer>
	                        &copy; 2026 - Página de prueba HTML
	                    </footer>

	                    <script>
	                        function mostrarMensaje() {
	                            document.getElementById("mensaje").textContent = 
	                            "¡Hola! Has hecho clic en el botón.";
	                        }
	                    </script>

	                </body>
	                </html>
	                """;
	    }
	
	
}
