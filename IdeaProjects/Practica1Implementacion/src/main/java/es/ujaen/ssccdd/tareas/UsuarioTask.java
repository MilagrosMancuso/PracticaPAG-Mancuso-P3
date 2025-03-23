package es.ujaen.ssccdd.tareas;

import es.ujaen.ssccdd.Constantes;
import es.ujaen.ssccdd.datos.Bicicleta;
import es.ujaen.ssccdd.datos.EstacionBicicletas;
import es.ujaen.ssccdd.datos.EventoSistema;
import es.ujaen.ssccdd.datos.Peticion;
import es.ujaen.ssccdd.datos.PuntoRecarga;

import java.util.*;
import java.util.concurrent.Semaphore;

public class UsuarioTask implements Runnable {
    private final String nombre;                                        // Nombre del usuario
    private final Semaphore semUsuario;                                 // Semáforo del usuario para la petición transporte
    private final Map<String, EstacionBicicletas> estacionesMap;        // Mapa de las estaciones de bicicletas
    private final PuntoRecarga puntoRecarga;                            // Punto de recarga para las bicicletas eléctricas
    private final Queue<Peticion> peticionesTransporte;                 // Buffer para gestionar las peticiones de transporte
    private final Semaphore exmTransporte;                              // Semáforo para garantizar el acceso a las peticiones de transporte
    private final Semaphore semGestorTransporte;                        // Semáforo para sincronizar las peticiones de transporte
    private final Queue<EventoSistema> eventosSistema;                  // Para almacenar los eventos del sistema
    // Convertir claves a una lista
    private List<String> claves;

    // Campo auxiliar para almacenar la bicicleta recogida
    private Bicicleta bicicleta;

    public UsuarioTask(String nombre, Map<String, EstacionBicicletas> estacionesMap, PuntoRecarga puntoRecarga,
                       Queue<Peticion> peticionesTransporte, Semaphore exmTransporte, Semaphore semGestorTransporte,
                       Queue<EventoSistema> eventosSistema) {

        this.nombre = (nombre == null || nombre.trim().isEmpty() ? "Usuario-" + UUID.randomUUID() : nombre);
        this.semUsuario = new Semaphore(0);
        this.estacionesMap = estacionesMap;
        this.puntoRecarga = puntoRecarga;
        this.peticionesTransporte = peticionesTransporte;
        this.exmTransporte = exmTransporte;
        this.semGestorTransporte = semGestorTransporte;
        this.eventosSistema = eventosSistema;

        claves = new ArrayList<>(estacionesMap.keySet());
    }

    public String getNombre() {
        return nombre;
    }

    public String nuevo_ID(){
        return UUID.randomUUID().toString();
    }

    public EstacionBicicletas estacionAleatoria() {
        String claveAleatoria = claves.get(Constantes.aleatorio.nextInt(claves.size()));
        return estacionesMap.get(claveAleatoria);
    }

    /**
     * Realiza la petición de transporte: se crea una petición con la estación de origen y destino,
     * se añade al buffer compartido y se notifica al gestor. Luego espera a que el gestor resuelva la petición.
     */
    public void realizarPeticionTransporte(EstacionBicicletas estacion1, EstacionBicicletas estacion2) throws InterruptedException {
        Peticion peticion = new Peticion(nuevo_ID(), estacion1.getId(), estacion2.getId(), Constantes.UNO, semUsuario);
        exmTransporte.acquire();
        peticionesTransporte.add(peticion);
        exmTransporte.release();
        semGestorTransporte.release();
        // Esperar a que el gestor resuelva la petición (liberando semUsuario)
        semUsuario.acquire();
    }

    /**
     * Inicia el viaje recogiendo la bicicleta en la estación de origen.
     */
    public void iniciarViaje(EstacionBicicletas origen) throws InterruptedException {
        origen.semExm().acquire();
        // solicita el alquiler usando el nombre del usuario, guardo la bicicleta obtenida
        bicicleta = origen.peticionAlquiler(nombre).orElse(null);  //el orElse es para el caso en el que no tenga nada que devolver... es un optional
        origen.semExm().release();
    }

    /**
     * Completa el viaje entregando la bicicleta en la estación de destino.
     */
    public void completarViaje(EstacionBicicletas destino) throws InterruptedException {
        destino.semExm().acquire();
        boolean entregado = destino.entregarBicicleta(bicicleta);
        destino.semExm().release();
        if (!entregado) {
            eventosSistema.add(new EventoSistema(Constantes.TipoEvento.ESTACION_SIN_ESPACIO,
                    nombre, destino.getId(), bicicleta.getId(), "No se pudo entregar la bicicleta"));
        }
    }

    /**
     * Método auxiliar para simular la recarga de una bicicleta, en caso de que sea eléctrica y requiera carga.
     */
    private void recargarBicicleta() throws InterruptedException {
        eventosSistema.add(new EventoSistema(Constantes.TipoEvento.USUARIO_NECESITA_RECARGA,
                nombre, null, bicicleta.getId(), "Bicicleta necesita recarga"));

        // Esperar capacidad en el punto de recarga
        puntoRecarga.semDeposito().acquire();
        puntoRecarga.semExm().acquire();
        puntoRecarga.iniciarCarga(bicicleta);
        puntoRecarga.semExm().release();

        // Simular tiempo de recarga
        Thread.sleep(1500);
        puntoRecarga.semExm().acquire();
        Bicicleta biciRecargada = puntoRecarga.finalizadaCarga(bicicleta.getId()).orElse(bicicleta);
        puntoRecarga.semExm().release();
        puntoRecarga.semDeposito().release();
        bicicleta = biciRecargada;
        eventosSistema.add(new EventoSistema(Constantes.TipoEvento.RECARGA_FINALIZADA,
                nombre, puntoRecarga.getId(), bicicleta.getId(), "Recarga completada"));
    }

    /**
     * Flujo completo del usuario:
     * 1. Realiza la petición de transporte (indicando origen y destino).
     * 2. Una vez confirmada, recoge la bicicleta en la estación de origen.
     * 3. Si la bicicleta requiere recarga, se dirige al punto de recarga.
     * 4. Finalmente, entrega la bicicleta en la estación de destino.
     */
    @Override
    public void run() {
        // Selecciona dos estaciones aleatorias (distintas)
        EstacionBicicletas estacionOrigen = estacionAleatoria();
        EstacionBicicletas estacionDestino = estacionAleatoria();

        while (estacionDestino.getId().equals(estacionOrigen.getId())) {
            estacionDestino = estacionAleatoria();
        }

        try {
            // 1. Realizar la petición de transporte
            realizarPeticionTransporte(estacionOrigen, estacionDestino);
            eventosSistema.add(new EventoSistema(Constantes.TipoEvento.USUARIO_SOLICITUD_TRANSPORTE,
                    nombre, estacionOrigen.getId(), "Solicitud de transporte hacia " + estacionDestino.getId()));

            // 2. Iniciar viaje: recoger bicicleta en la estación de origen
            iniciarViaje(estacionOrigen);
            Thread.sleep(1500); //hacemos una espera

            if (bicicleta == null) {
                eventosSistema.add(new EventoSistema(Constantes.TipoEvento.ESTACION_SIN_BICICLETAS,
                        nombre, estacionOrigen.getId(), "No hay bicicletas disponibles"));
                return;
            }

            eventosSistema.add(new EventoSistema(Constantes.TipoEvento.USUARIO_RECOGIDA_BICICLETA,
                    nombre, estacionOrigen.getId(), bicicleta.getId(), "Bicicleta recogida"));

            eventosSistema.add(new EventoSistema(Constantes.TipoEvento.USUARIO_INICIANDO_VIAJE,
                    nombre, estacionOrigen.getId(), bicicleta.getId(), "Iniciando viaje hacia " + estacionDestino.getId()));

            // 3. Durante el viaje: si la bicicleta requiere recarga, realizar el proceso de recarga
            if (bicicleta.necesitaCarga()) {
                recargarBicicleta();
            }

            // 4. Completar el viaje: entregar la bicicleta en la estación de destino
            completarViaje(estacionDestino);
            eventosSistema.add(new EventoSistema(Constantes.TipoEvento.USUARIO_FINALIZANDO_VIAJE,
                    nombre, estacionDestino.getId(), bicicleta.getId(), "Viaje finalizado"));

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            eventosSistema.add(new EventoSistema(Constantes.TipoEvento.SISTEMA_ERROR,
                    nombre, null, "Usuario interrumpido"));
        }
    }
}
