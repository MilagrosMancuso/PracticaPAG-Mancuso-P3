package es.ujaen.ssccdd.datos;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import static es.ujaen.ssccdd.Constantes.TipoEvento;

public class EventoSistema {
    private final String id;                // Identificador único del evento
    private final TipoEvento tipo;          // Tipo de evento
    private final Instant timestamp;        // Momento en que ocurre el evento
    private final String origen;            // Identificador del origen del evento (usuario, estación, etc.)
    private final String destino;           // Identificador del destino del evento (cuando aplica)
    private final String idBicicleta;       // Identificador de la bicicleta involucrada (cuando aplica)
    private final String detalles;          // Detalles adicionales del evento

    /**
     * Constructor completo para crear un evento del sistema
     *
     * @param tipo Tipo de evento
     * @param origen Identificador del origen del evento
     * @param destino Identificador del destino del evento (puede ser null)
     * @param idBicicleta Identificador de la bicicleta involucrada (puede ser null)
     * @param detalles Detalles adicionales del evento (puede ser null)
     */
    public EventoSistema(TipoEvento tipo, String origen, String destino, String idBicicleta, String detalles) {
        this.id = UUID.randomUUID().toString();
        this.tipo = Objects.requireNonNull(tipo, "El tipo de evento no puede ser null");
        this.timestamp = Instant.now();
        this.origen = Objects.requireNonNull(origen, "El origen del evento no puede ser null");
        this.destino = destino;
        this.idBicicleta = idBicicleta;
        this.detalles = detalles;
    }

    /**
     * Constructor simplificado para eventos sin bicicleta ni destino específicos
     *
     * @param tipo Tipo de evento
     * @param origen Identificador del origen del evento
     * @param detalles Detalles adicionales del evento (puede ser null)
     */
    public EventoSistema(TipoEvento tipo, String origen, String detalles) {
        this(tipo, origen, null, null, detalles);
    }

    /**
     * Constructor para eventos relacionados con bicicletas pero sin destino específico
     *
     * @param tipo Tipo de evento
     * @param origen Identificador del origen del evento
     * @param idBicicleta Identificador de la bicicleta involucrada
     * @param detalles Detalles adicionales del evento (puede ser null)
     */
    public EventoSistema(TipoEvento tipo, String origen, String idBicicleta, String detalles) {
        this(tipo, origen, null, idBicicleta, detalles);
    }

    public String getId() {
        return id;
    }

    public TipoEvento getTipo() {
        return tipo;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getOrigen() {
        return origen;
    }

    public String getDestino() {
        return destino;
    }

    public String getIdBicicleta() {
        return idBicicleta;
    }

    public String getDetalles() {
        return detalles;
    }

    /**
     * Formatea el evento para su representación en el log del sistema
     *
     * @return Representación formateada del evento
     */
    public String formatearEvento() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(timestamp).append("] ");
        sb.append("[").append(tipo.getCodigo()).append("] ");
        sb.append(tipo.getDescripcion()).append(" - ");
        sb.append("Origen: ").append(origen);

        if (destino != null && !destino.isEmpty()) {
            sb.append(", Destino: ").append(destino);
        }

        if (idBicicleta != null && !idBicicleta.isEmpty()) {
            sb.append(", Bicicleta: ").append(idBicicleta);
        }

        if (detalles != null && !detalles.isEmpty()) {
            sb.append(" - ").append(detalles);
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return formatearEvento();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventoSistema evento = (EventoSistema) o;
        return Objects.equals(id, evento.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}