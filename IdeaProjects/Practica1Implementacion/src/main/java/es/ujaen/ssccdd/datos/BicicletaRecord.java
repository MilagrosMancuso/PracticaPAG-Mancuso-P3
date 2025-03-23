package es.ujaen.ssccdd.datos;

import es.ujaen.ssccdd.Constantes;

import java.time.Instant;
import java.util.Objects;

/**
 * Record de bicicleta para el intercambio de información inmutable de Bicicleta
 */
public record BicicletaRecord(
        String id,
        Constantes.TipoBicicletas tipo,
        Constantes.EstadosBicicletas estado,
        Instant fechaAlquiler
) {
    // Constructor compacto que crea un record a partir de una Bicicleta
    public BicicletaRecord(Bicicleta bicicleta) {
        this(
                bicicleta.getId(),
                bicicleta.getTipo(),
                bicicleta.getEstado(),
                Instant.now()
        );
    }

    // Métodos adicionales pueden ser añadidos si se necesitan
    public boolean esElectrica() {
        return tipo == Constantes.TipoBicicletas.ELECTRICA;
    }

    // Validación personalizada (opcional)
    public BicicletaRecord {
        Objects.requireNonNull(id, "El ID no puede ser nulo");
        Objects.requireNonNull(tipo, "El tipo no puede ser nulo");
        Objects.requireNonNull(estado, "El estado no puede ser nulo");
    }
}