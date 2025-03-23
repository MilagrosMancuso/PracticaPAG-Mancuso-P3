package es.ujaen.ssccdd.tareas;

import es.ujaen.ssccdd.Constantes;
import es.ujaen.ssccdd.datos.Bicicleta;
import es.ujaen.ssccdd.datos.EstacionBicicletas;
import es.ujaen.ssccdd.datos.EventoSistema;
import es.ujaen.ssccdd.datos.ZonaMantenimiento;

import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

public class TecnicoMantenimientoTask implements Runnable {
    private final String id;                                // Identificador único del técnico de mantenimiento
    private final EstacionBicicletas estacionAisgnada;        // Estación de bicicletas a su cargo
    private final ZonaMantenimiento zonaMantenimiento;        // Zona donde deposita las bicicletas una vez reparadas
    private final Queue<EventoSistema> eventosSistema;        // Para almacenar los eventos del sistema

    public TecnicoMantenimientoTask(EstacionBicicletas estacionAisgnada, ZonaMantenimiento zonaMantenimiento,
                                    Queue<EventoSistema> eventosSistema) {

        this.id = "Técnico Mantenimiento - " + estacionAisgnada.getId();
        this.estacionAisgnada = estacionAisgnada;
        this.zonaMantenimiento = zonaMantenimiento;
        this.eventosSistema = eventosSistema;
    }

    public String getId() {
        return id;
    }

    /**
     * recoger bicicletas que necesiten mantenimiento
     */
    public void recogerBicicletas() {
        // La acción de recoger bicicletas se realiza cuando el semáforo de mantenimiento de la estación se libera.
        eventosSistema.add(new EventoSistema(Constantes.TipoEvento.TECNICO_RECOGIENDO_BICICLETAS,
                id, estacionAisgnada.getId(), null, "Recogiendo bicicletas para mantenimiento"));
    }

    /**
     * Este método simula la entrega de bicicletas reparadas en la zona de mantenimiento.
     */
    public void entregarBicicletas() {
        eventosSistema.add(new EventoSistema(Constantes.TipoEvento.TECNICO_ENTREGANDO_BICICLETAS,
                id, zonaMantenimiento.getId(), null, "Entregando bicicletas reparadas"));
    }

    /**
     * Los técnicos de mantenimiento estarán a la espera de recibir órdenes de mantenimiento por parte del gestor.
     * Se dirigirán a la estación de bicicletas asignada para recogerlas, simularán la reparación y entregarán las bicicletas en la zona de mantenimiento.
     */
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // Esperar la señal para recoger bicicletas (la estación libera su semáforo de mantenimiento)
                estacionAisgnada.semMantenimiento().acquire();
                recogerBicicletas();

                // Bloquear la estación para recoger las bicicletas que necesitan mantenimiento
                estacionAisgnada.semExm().acquire();
                List<Bicicleta> bicicletasParaMantenimiento = estacionAisgnada.mantenimientoBicicletas();
                estacionAisgnada.semExm().release();

                // Simular tiempo de reparación
                TimeUnit.SECONDS.sleep(Constantes.TIEMPO_HASTA_MANTENIMIENTO);

                // Depositar bicicletas reparadas en la zona de mantenimiento
                zonaMantenimiento.semExm().acquire();
                zonaMantenimiento.dejarBicicletas(bicicletasParaMantenimiento);
                zonaMantenimiento.semExm().release();

                entregarBicicletas();
                eventosSistema.add(new EventoSistema(Constantes.TipoEvento.TECNICO_REPARANDO_BICICLETAS,
                        id, estacionAisgnada.getId(), null, "Bicicletas reparadas"));

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
