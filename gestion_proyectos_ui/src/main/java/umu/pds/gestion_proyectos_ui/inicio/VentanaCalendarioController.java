package umu.pds.gestion_proyectos_ui.inicio;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import umu.pds.gestion_proyectos_ui.api.dto.ListaDto;
import umu.pds.gestion_proyectos_ui.api.dto.TableroDto;
import umu.pds.gestion_proyectos_ui.api.dto.TarjetaDto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class VentanaCalendarioController {

    @FXML
    private BorderPane contenedorCalendario;

    private CalendarView calendarView;

    // El calendario se crea una sola vez y nunca se sustituye,
    // así CalendarFX mantiene sus suscripciones internas intactas.
    private Calendar calendar;

    // Referencia a los entries activos para poder borrarlos en la siguiente carga.
    private final List<Entry<?>> entriesActuales = new ArrayList<>();

    @FXML
    public void initialize() {
        calendarView = new CalendarView();

        // Ocultar elementos de UI que no necesitamos
        calendarView.setShowAddCalendarButton(false);
        calendarView.setShowSearchField(false);
        calendarView.setShowSourceTrayButton(false);

        // Crear calendario y fuente UNA SOLA VEZ antes de añadir la vista al árbol
        calendar = new Calendar("Tarjetas");
        calendar.setStyle(Calendar.Style.STYLE1);

        CalendarSource source = new CalendarSource("Tablero");
        source.getCalendars().add(calendar);
        calendarView.getCalendarSources().setAll(source);

        // Navegar a la vista de mes por defecto (DESPUÉS de configurar la fuente)
        calendarView.showMonthPage();

        contenedorCalendario.setCenter(calendarView);
    }

    /**
     * Carga las tarjetas con fecha de vencimiento del tablero en el calendario.
     * Puede llamarse varias veces (por ejemplo al cambiar de tablero).
     */
    public void setDatos(TableroDto tablero) {
        if (tablero == null) return;

        // 1. Eliminar entradas del ciclo anterior
        for (Entry<?> entry : entriesActuales) {
            calendar.removeEntry(entry);
        }
        entriesActuales.clear();

        if (tablero.listas == null) return;

        // 2. Recorrer listas → tarjetas
        for (ListaDto lista : tablero.listas) {
            if (lista.tarjetas == null) continue;
            for (TarjetaDto tarjeta : lista.tarjetas) {
                if (tarjeta.fechaVencimiento == null || tarjeta.fechaVencimiento.isBlank()) continue;
                try {
                    LocalDate fecha = parsearFecha(tarjeta.fechaVencimiento);
                    String titulo = tarjeta.titulo != null ? tarjeta.titulo : "(sin título)";

                    System.out.println("Añadiendo al calendario: " + titulo + " en " + fecha);

                    // setInterval(LocalDate) es el método canónico de CalendarFX
                    // para eventos de día completo; equivale a:
                    //   setInterval(date.atStartOfDay(), date.atStartOfDay())
                    //   setFullDay(true)
                    Entry<String> entry = new Entry<>(titulo);
                    entry.setInterval(fecha);

                    calendar.addEntry(entry);
                    entriesActuales.add(entry);
                } catch (Exception e) {
                    System.err.println("Error al parsear fechaVencimiento '"
                            + tarjeta.fechaVencimiento + "': " + e.getMessage());
                }
            }
        }
    }

    /**
     * Parsea una fecha ISO-8601 extrayendo siempre los 10 primeros caracteres (YYYY-MM-DD).
     * Funciona con cualquier variante: "2026-04-03", "2026-04-03T00:00", "2026-04-03T00:00:00", etc.
     */
    private LocalDate parsearFecha(String iso) {
        String texto = iso.trim();
        if (texto.length() < 10) throw new IllegalArgumentException("Fecha demasiado corta: " + texto);
        return LocalDate.parse(texto.substring(0, 10));
    }
}
