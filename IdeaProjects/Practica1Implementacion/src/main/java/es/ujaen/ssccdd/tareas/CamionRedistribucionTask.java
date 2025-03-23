package es.ujaen.ssccdd.tareas;

import es.ujaen.ssccdd.Constantes;
import es.ujaen.ssccdd.datos.Bicicleta;
import es.ujaen.ssccdd.datos.EstacionBicicletas;
import es.ujaen.ssccdd.datos.EventoSistema;
import es.ujaen.ssccdd.datos.Peticion;
import es.ujaen.ssccdd.datos.ZonaMantenimiento;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class CamionRedistribucionTask implements Runnable {
    private final String matricula;                                 // Matrícula del camión
    private final Map<String, EstacionBicicletas> estacionesMap;    // Mapa de las estaciones de bicicletas
    private final ZonaMantenimiento zonaMantenimiento;              // Zona de mantenimiento donde están las bicicletas reparadas
    private final Semaphore semCamiones;                            // Semáforo para avisar que el camión haga el transporte
    private final Queue<Peticion> peticionesRedistribucion;         // Buffer para las peticiones de redistribución de bicicletas
    private final Semaphore exmRedistribucion;                      // Semáforo para garantizar el acceso a las peticiones de redistribución
    private final Queue<EventoSistema> eventosSistema;              // Para almacenar los eventos del sistema

    public CamionRedistribucionTask(Map<String, EstacionBicicletas> estacionesMap, ZonaMantenimiento zonaMantenimiento,
                                    Semaphore semCamiones, Queue<Peticion> peticionesRedistribucion,
                                    Semaphore exmRedistribucion, Queue<EventoSistema> eventosSistema) {

        this.matricula = "M - " + java.util.UUID.randomUUID().toString().substring(0, 8);
        this.estacionesMap = estacionesMap;
        this.zonaMantenimiento = zonaMantenimiento;
        this.semCamiones = semCamiones;
        this.peticionesRedistribucion = peticionesRedistribucion;
        this.exmRedistribucion = exmRedistribucion;
        this.eventosSistema = eventosSistema;
    }

    public String getMatricula() {
        return matricula;
    }

    public Peticion obtenerPeticion() throws InterruptedException {
        // Esperar a que haya una petición de redistribución
        semCamiones.acquire();
        // Acceso exclusivo al buffer de peticiones de redistribución
        exmRedistribucion.acquire();
        Peticion peticion = peticionesRedistribucion.poll();
        exmRedistribucion.release();

        return peticion;
    }

    public void resolverPeticion(Peticion peticion) throws InterruptedException {
        // Registrar el inicio de recogida
        eventosSistema.add(new EventoSistema(Constantes.TipoEvento.CAMION_RECOGIENDO_BICICLETAS,
                matricula, peticion.getOrigen(), null, "Recogida de " + peticion.getBicicletasPeticion() + " bicicletas"));

        List<Bicicleta> bicicletasParaReubicar = new ArrayList<>();
        String origen = peticion.getOrigen();

        // origen: zona de mantenimiento o estación
        if (origen.startsWith("ZonaMantenimiento")) {
            zonaMantenimiento.semExm().acquire();
            bicicletasParaReubicar.addAll(zonaMantenimiento.recogerBicicletas(peticion.getBicicletasPeticion()));
            zonaMantenimiento.semExm().release();

        } else {
            //vienen de las estaciones
            EstacionBicicletas estacionOrigen = estacionesMap.get(origen);

            if (estacionOrigen != null) {
                estacionOrigen.semExm().acquire();
                bicicletasParaReubicar.addAll(estacionOrigen.listaReubicacion(peticion.getBicicletasPeticion()));
                estacionOrigen.semExm().release();
            }
        }
        // Simular el transporte
        TimeUnit.SECONDS.sleep(Constantes.TIEMPO_TRANSPORTE);

        // Registrar que el camión está en tránsito
        eventosSistema.add(new EventoSistema(Constantes.TipoEvento.CAMION_EN_TRANSITO,
                matricula, peticion.getDestino(), null, "Transportando bicicletas"));

        // Depositar en el destino
        EstacionBicicletas estacionDestino = estacionesMap.get(peticion.getDestino());

        if (estacionDestino != null) {
            estacionDestino.semExm().acquire();
            // reubicación para depositar las bicicletas
            estacionDestino.peticionReubicacion(bicicletasParaReubicar);
            estacionDestino.semExm().release();
        }

        // Registrar la entrega
        eventosSistema.add(new EventoSistema(Constantes.TipoEvento.CAMION_ENTREGANDO_BICICLETAS,
                matricula, peticion.getDestino(), null, "Entregado " + peticion.getBicicletasPeticion() + " bicicletas"));

        // Indicar que la petición ha sido resuelta
        peticion.semResolucion().release();
    }

    /**
     * 1. Los camiones pueden mover bicicletas entre estaciones para balancear la disponibilidad, así como del punto de reparación.
     * 2. Cada camión puede transportar un número limitado de bicicletas.
     * 3. Los camiones deben esperar la solicitud por parte del gestor para saber el lugar donde recoger las bicicletas y donde deberán entregarlas.
     */
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Peticion peticion = obtenerPeticion();

                if (peticion != null) {
                   resolverPeticion(peticion);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
