package es.ujaen.ssccdd.datos;

import es.ujaen.ssccdd.Constantes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Semaphore;

import static es.ujaen.ssccdd.Constantes.*;
import static es.ujaen.ssccdd.Constantes.TipoBicicletas.NORMAL;

public class PuntoRecarga {
    private final String id;                                // El identificador único del punto de mantenimiento
    private final List<Bicicleta> listaBicicletas;          // Lista de bicicletas dentro del punto de recarga
    private final int capacidad;                            // Capacidad máxima de la estación de bicicletas
    private final Semaphore semDeposito;                    // Semáforo para los espacios disponibles en el punto de recarga
    private final Semaphore semExm;                         // Semáforo para las operaciones en exclusión mutua

    public PuntoRecarga(int capacidad) {
        this.id = "PuntoRecarga-" + UUID.randomUUID();
        this.listaBicicletas = new ArrayList<>();
        this.capacidad = ( capacidad < 0 || capacidad > CAPACIDAD_MAXIMA ) ? capacidad : aleatorio.nextInt(CAPACIDAD_MINIMA, CAPACIDAD_MAXIMA);

        // Inicialización de los semáforos
        this.semDeposito = new Semaphore(capacidad);
        this.semExm = new Semaphore(1);
    }

    public String getId() {
        return id;
    }

    public int getCapacidad() {
        return capacidad - listaBicicletas.size();
    }

    public Semaphore semDeposito() {
        return semDeposito;
    }

    public Semaphore semExm() {
        return semExm;
    }

    /**
     * Deja la bicicleta en el punto de carta
     * @param bicicleta
     */
    public void iniciarCarga(Bicicleta bicicleta) {
        if (bicicleta.getTipo().equals(NORMAL)) {
            throw new IllegalArgumentException("Debe ser una bicicleta ELECTRICA para realizar la carga");
        }

        this.listaBicicletas.add(new Bicicleta(bicicleta));
    }

    /**
     * Una vez finalizada la carga se recoge del punto de carga la bicicleta que previamente fue depositada
     * @param idBicicleta el identificador de la bicicleta que se depositó
     * @return Optional<Bicicleta> que ha finalizado su carga
     */
    public Optional<Bicicleta> finalizadaCarga(String idBicicleta) {
        Optional<Bicicleta> resultado = Optional.empty();

        resultado = listaBicicletas.stream()
                .filter(b -> b.getId().equals(idBicicleta))
                .findFirst();

        // Si se encuentra la bicicleta, la eliminamos de la lista
        resultado.ifPresent(listaBicicletas::remove);

        return resultado;
    }

    @Override
    public String toString() {
        return "PuntoRecarga{" +
                "id='" + id + '\'' +
                ", listaBicicletas=" + listaBicicletas +
                ", capacidad=" + capacidad +
                '}';
    }
}
