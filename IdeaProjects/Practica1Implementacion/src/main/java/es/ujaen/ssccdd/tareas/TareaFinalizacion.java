package es.ujaen.ssccdd.tareas;


import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

/**
 *
 * @author pedroj
 */
public class TareaFinalizacion implements Runnable {
    private final List<Future<?>> listaTareas;
    private final CountDownLatch esperaFinalizacion;

    public TareaFinalizacion(List<Future<?>> listaTareas, CountDownLatch esperaFinalizacion) {
        this.listaTareas = listaTareas;
        this.esperaFinalizacion = esperaFinalizacion;
    }


    /**
     * Esta tarea debe activarse cuando se cumple el tiempo de simulación de la ejecución de la práctica
     * Comparte la lista de todos los Future asociados a las tareas que se han creado durante la ejecución
     * de la práctica para poder solicitar su finalización.
     */
    @Override
    public void run() {
        // Recorre la lista de tareas para solicitar la finalización

        for ( Future<?> tarea : listaTareas)
            tarea.cancel(true);

        // Se sincroniza para que se complete la finalización de todas las tareas
        // antes de que finalice su ejecución
        esperaFinalizacion.countDown();
    }
}
