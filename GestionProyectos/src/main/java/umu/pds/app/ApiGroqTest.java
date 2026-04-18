
package umu.pds.app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("${app.server.path}" + "/public")
public class ApiGroqTest {

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping(path = "/holaMundo2")
    public String holamundo() {
        return "Hola mundo";
    }

    //Endpoint para preguntar lo que queramos 
    @PostMapping(path = "/preguntar", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> preguntarIA(@RequestBody Map<String, String> request) {

        String pregunta = request.get("pregunta");
        String contexto = request.get("contexto");
        if (contexto == null) contexto = "";
        if (pregunta == null || pregunta.isEmpty()) {
            return ResponseEntity.badRequest().body("La pregunta no puede estar vacía");
        }

        Map<String, Object> body = new HashMap<>();
        body.put("model", "qwen/qwen3-32b");
        body.put("temperature", 0.7);

        List<Map<String, String>> messages = new ArrayList<>();

        // 🧠 SYSTEM → reglas + contexto dinámico
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content",
            "Responde en español de forma clara y directa. " +
            "No incluyas razonamientos internos ni etiquetas como <think>. " +
            "Usa únicamente el siguiente contexto si es relevante:\n" +
            contexto
        );

        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", pregunta);

        messages.add(systemMessage);
        messages.add(userMessage);

        body.put("messages", messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            List<Map<String, Object>> choices =
                    (List<Map<String, Object>>) response.getBody().get("choices");

            Map<String, Object> firstChoice = choices.get(0);
            Map<String, String> message =
                    (Map<String, String>) firstChoice.get("message");

            String contenido = message.get("content");

            // limpiar <think>
            int index = contenido.indexOf("</think>");
            String respuestaLimpia;

            if(index != -1 && index + 8 < contenido.length()){
                respuestaLimpia = contenido.substring(index + 8).trim();
            } else {
                respuestaLimpia = contenido.trim();
            }

            return ResponseEntity.ok(respuestaLimpia);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error llamando a Groq: " + e.getMessage());
        }
    }
}