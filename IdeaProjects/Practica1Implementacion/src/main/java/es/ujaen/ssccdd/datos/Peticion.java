package es.ujaen.ssccdd.datos;

import java.util.concurrent.Semaphore;

public class Peticion {
    private final String id;                        // Identificador del solicitante
    private final String idOrigen;                  // Id donde recoger bicicletas
    private final String idDestino;                 // Id para entregar bicicletas
    private final int numBicicleatas;               // Número de bicicletas requerido en la petición
    private final Semaphore resolucion;             // Para indicar que la petición ha sido resuelta

    /**
     * Este constructor define todas las variables de instancia. Es para que lo utilice el gestor para solicitar
     * a un camión que debe realizar una operación de reubicación
     * @param id el identificador de la tarea solicitante
     * @param idOrigen el origen donde deberán recogerse las bicicletas
     * @param idDestino el destino donde deberán depositarse las bicicletas
     * @param numBicicleatas el número de bicicletas a recoger y luego a depositar
     * @param resolucion el semáforo para indicar al solicitante que se ha completado su solicitud
     */
    public Peticion(String id, String idOrigen, String idDestino, int numBicicleatas, Semaphore resolucion) {
        if ( id == null || id.isBlank() || resolucion == null || numBicicleatas < 1) {
            throw new IllegalArgumentException("El identificador del solicitante no está definido " +
                    " o no hay petición de bicicletas o no se ha incluido el semáforo para la resolución");
        }

        this.id = id;
        this.idOrigen = idOrigen;
        this.idDestino = idDestino;
        this.numBicicleatas = numBicicleatas;
        this.resolucion = resolucion;
    }

    public Peticion(String id, String idOrigen, String idDestino, Semaphore resolucion) {
        if ( id == null || id.isBlank() || resolucion == null ) {
            throw new IllegalArgumentException("El identificador del solicitante no está definido " +
                    " o no se ha incluido el semáforo para la resolución");
        }
        this.id = id;
        this.idOrigen = idOrigen;
        this.idDestino = idDestino;
        this.numBicicleatas = 1; // Petición de un usuario para el transporte
        this.resolucion = resolucion;

    }

    public String getId() {
        return id;
    }

    public String getOrigen() {
        return idOrigen;
    }

    public String getDestino() {
        return idDestino;
    }

    public int getBicicletasPeticion() {
        return numBicicleatas;
    }

    public Semaphore semResolucion() {
        return resolucion;
    }

    @Override
    public String toString() {
        return "Peticion{" +
                "id='" + id + '\'' +
                ", idOrigen='" + idOrigen + '\'' +
                ", idDestino='" + idDestino + '\'' +
                ", numBicicleatas=" + numBicicleatas +
                '}';
    }
}
