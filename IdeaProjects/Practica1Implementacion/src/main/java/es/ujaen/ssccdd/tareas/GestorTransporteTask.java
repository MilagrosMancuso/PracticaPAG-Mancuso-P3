package es.ujaen.ssccdd.tareas;

import es.ujaen.ssccdd.Constantes;
import es.ujaen.ssccdd.datos.Bicicleta;
import es.ujaen.ssccdd.datos.EstacionBicicletas;
import es.ujaen.ssccdd.datos.EventoSistema;
import es.ujaen.ssccdd.datos.Peticion;
import es.ujaen.ssccdd.datos.PuntoRecarga;
import es.ujaen.ssccdd.datos.ZonaMantenimiento;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class GestorTransporteTask implements Runnable {
    private final String idGestor;                                            // Identificador del gestor
    private final Map<String, EstacionBicicletas> bicicletasMap;              // Mapa de las estaciones de bicicletas
    private final ZonaMantenimiento zonaMantenimiento;                        // Zona de mantenimiento de las bicicletas
    private final PuntoRecarga puntoRecarga;                                  // Punto de recarga para las bicicletas eléctricas
    private final Queue<Peticion> peticionesTransporte;                       // Buffer para gestionar las peticiones de transporte
    private final Semaphore exmTransporte;                                    // Semáforo para garantizar el acceso a las peticiones de transporte
    private final Queue<Peticion> peticionesRedistribucion;                   // Buffer para las peticiones de redistribución de bicicletas
    private final Semaphore exmRedistribucion;                                // Semáforo para garantizar el acceso a las peticiones de redistribución
    private final Semaphore semCamiones;                                      // Semáforo para avisar a los camiones de redistribución
    private final Semaphore semGestorTransporte;                              // Semáforo para sincronizar las peticiones de transporte
    private final Queue<EventoSistema> eventosSistema;                        // Para almacenar los eventos del sistema
    private final List<Future<?>> tareasSistema;                              // Lista de tareas que se crean en el sistema para su finalización

    // Variables del gestor
    private final Queue<Peticion> peticionesPendientes;                       // Peticiones de transporte por resolver
    private final Semaphore semPeticionesPendientes;                          // Semáforo para las peticiones pendientes
    private final Semaphore semGestor;                                        // Semáforo para sincronizar las peticiones de redistribución

    public GestorTransporteTask(Map<String, EstacionBicicletas> bicicletasMap, ZonaMantenimiento zonaMantenimiento,
                                PuntoRecarga puntoRecarga, List<Future<?>> tareasSistema) {

        if (bicicletasMap.isEmpty() || zonaMantenimiento == null || puntoRecarga == null || tareasSistema == null) {
            throw new IllegalArgumentException("Hay elementos necesarios para la simulación que no están definidos");
        }

        this.idGestor = "Gestor - " + UUID.randomUUID();
        this.bicicletasMap = bicicletasMap;
        this.zonaMantenimiento = zonaMantenimiento;
        this.puntoRecarga = puntoRecarga;
        this.tareasSistema = tareasSistema;

        // Inicialización de estructuras compartidas
        this.peticionesTransporte = new LinkedList<>();
        this.exmTransporte = new Semaphore(1);
        this.peticionesRedistribucion = new LinkedList<>();
        this.exmRedistribucion = new Semaphore(1);
        this.semCamiones = new Semaphore(0);
        this.semGestorTransporte = new Semaphore(0);
        this.semPeticionesPendientes = new Semaphore(0);
        this.semGestor = new Semaphore(0);

        this.eventosSistema = new ConcurrentLinkedQueue<>();
        this.peticionesPendientes = new ConcurrentLinkedQueue<>();
    }

    public String getIdGestor() {
        return idGestor;
    }

    /**
     * Esta es la tarea principal del gestor que se ejecutará cíclicamente.
     * Se encarga de inicializar el sistema y de ejecutar en paralelo las siguientes subtareas:
     * - Recibir peticiones de transporte.
     * - Resolver peticiones pendientes.
     * - Gestionar el mantenimiento.
     * - Gestionar la redistribución.
     * Además, genera peticiones de transporte simulando la llegada de nuevos usuarios.
     */
    @Override
    public void run() {
        // Lanzamos las subtareas en hilos separados
        Thread recibirThread = new Thread(new RecibirPeticionesTransporte(), "Recepcion-" + idGestor);
        Thread resolverThread = new Thread(new ResolverPeticionesTransporte(), "Resolver-" + idGestor);
        Thread mantenimientoThread = new Thread(new GestionMantenimiento(), "Mantenimiento-" + idGestor);
        Thread redistribucionThread = new Thread(new GestionRedistribucion(), "Redistribucion-" + idGestor);

        recibirThread.start();
        resolverThread.start();
        mantenimientoThread.start();
        redistribucionThread.start();

    }

    /** Recibir peticiones de transporte */
    private class RecibirPeticionesTransporte implements Runnable {
        private final String id;

        public RecibirPeticionesTransporte() {
            this.id = "Recepcion - " + idGestor;
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // Espera la señal de que hay una petición nueva
                    semGestorTransporte.acquire();
                    exmTransporte.acquire();
                    Peticion peticion = peticionesTransporte.poll();
                    exmTransporte.release();

                    if (peticion != null) {
                        peticionesPendientes.add(peticion);
                        semPeticionesPendientes.release();

                        eventosSistema.add(new EventoSistema(Constantes.TipoEvento.USUARIO_SOLICITUD_CONFIRMADA,
                                id, peticion.getOrigen(), null, "Petición recibida"));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }



    /** Resolver peticiones de transporte**/
    private class ResolverPeticionesTransporte implements Runnable {
        private final String id;

        public ResolverPeticionesTransporte() {
            this.id = "Resolver - " + idGestor;
        }


        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    semPeticionesPendientes.acquire();
                    Peticion peticion = peticionesPendientes.poll();

                    if (peticion != null) {
                        // Se intenta resolver la petición comprobando la disponibilidad en origen y espacio en destino
                        EstacionBicicletas estacionOrigen = bicicletasMap.get(peticion.getOrigen());
                        EstacionBicicletas estacionDestino = bicicletasMap.get(peticion.getDestino());
                        boolean sePuede = false;

                        // Bloqueo de ambas estaciones
                        estacionOrigen.semExm().acquire();
                        estacionDestino.semExm().acquire();

                        if (estacionOrigen.getDisponibles() > 0 && estacionDestino.hayEspacio()) {
                            // Se simula la reserva. Se extrae una bicicleta para el viaje
                            estacionOrigen.peticionAlquiler("Gestor");
                            sePuede = true;
                        }
                        estacionDestino.semExm().release();
                        estacionOrigen.semExm().release();

                        if (sePuede) {
                            // Se libera el semáforo de la petición para continuar
                            peticion.semResolucion().release();
                            eventosSistema.add(new EventoSistema(Constantes.TipoEvento.USUARIO_SOLICITUD_CONFIRMADA,
                                    id, peticion.getOrigen(), null, "Petición resuelta"));
                        } else {
                            // Si no se puede resolver, se vuelve a añadir a las pendientes
                            peticionesPendientes.add(peticion);
                            semPeticionesPendientes.release();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }



    /**Subtarea: Gestión de mantenimiento*/
    private class GestionMantenimiento implements Runnable {
        private final String idMantenimiento;

        public GestionMantenimiento() {
            this.idMantenimiento = "GestorMantenimiento - " + idGestor;
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    for (EstacionBicicletas estacion : bicicletasMap.values()) {
                        estacion.semExm().acquire();
                        if (estacion.avisarMantenimiento()) {
                            // Avisar al técnico (se libera el semáforo correspondiente de la estación)
                            estacion.semMantenimiento().release();
                            eventosSistema.add(new EventoSistema(Constantes.TipoEvento.ESTACION_MANTENIMIENTO_REQUERIDO,
                                    idMantenimiento, estacion.getId(), null, "Mantenimiento requerido"));
                        }
                        estacion.semExm().release();
                    }
                    TimeUnit.SECONDS.sleep(Constantes.UNO);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }



    /**Subtarea: Gestión de redistribución */
    private class GestionRedistribucion implements Runnable {
        private final String idRedistribucion;

        public GestionRedistribucion() {
            this.idRedistribucion = "GestorRedistribucion - " + idGestor;
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    int numBicicletas = 0;
                    String origen = null;
                    String destino = null;

                    // Comprobar en la zona de mantenimiento
                    zonaMantenimiento.semExm().acquire();

                    //@TODO OJO AL PIOJO
                    //Si hay suficientes bicicletas para llamar a mantenimiento
                    if (zonaMantenimiento.bicicletasDepositadas() > Constantes.MIN_MANTENIMIENTO) {
                        numBicicletas = zonaMantenimiento.bicicletasDepositadas();
                        origen = zonaMantenimiento.getId();
                    }
                    zonaMantenimiento.semExm().release();

                    //buscamos un origen entre las estaciones que tengan mas del minimo de bicicletas
                    if (numBicicletas < Constantes.MIN_MANTENIMIENTO) {
                        for (EstacionBicicletas estacion : bicicletasMap.values()) {
                            estacion.semExm().acquire();
                            if (estacion.getDisponibles() > Constantes.CAPACIDAD_MINIMA) {
                                numBicicletas = estacion.getDisponibles();
                                origen = estacion.getId();
                                estacion.semExm().release();
                                break;
                            }
                            estacion.semExm().release();
                        }
                    }

                    //@TODO AL PIQUE ENRRIQUE
                    // Buscar una estación destino con pocas bicicletas (menos del minimo)
                    for (EstacionBicicletas estacion : bicicletasMap.values()) {
                        estacion.semExm().acquire();
                        if (estacion.getDisponibles() < Constantes.CAPACIDAD_MINIMA) {
                            destino = estacion.getId();
                            estacion.semExm().release();
                            break;
                        }
                        estacion.semExm().release();
                    }

                    //hacemos la peticion
                    if (origen != null && destino != null && numBicicletas > 0) {
                        // Crear la petición de redistribución
                        Peticion peticion = new Peticion("GestorRedistribucion", origen, destino, numBicicletas, semGestor);
                        exmRedistribucion.acquire();
                        peticionesRedistribucion.add(peticion);
                        exmRedistribucion.release();

                        // Avisar a un camión
                        semCamiones.release();

                        // Esperar a que el camión resuelva la petición
                        semGestor.acquire();
                        eventosSistema.add(new EventoSistema(Constantes.TipoEvento.GESTOR_REDISTRIBUYENDO_BICICLETAS,
                                idRedistribucion, origen, destino, "Redistribución completada"));
                    }
                    TimeUnit.SECONDS.sleep(Constantes.UNO);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
