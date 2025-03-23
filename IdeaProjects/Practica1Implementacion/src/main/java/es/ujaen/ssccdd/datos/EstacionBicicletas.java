package es.ujaen.ssccdd.datos;

import java.util.*;
import java.util.concurrent.Semaphore;

import static es.ujaen.ssccdd.Constantes.*;
import static es.ujaen.ssccdd.Constantes.EstadosBicicletas.*;

public class EstacionBicicletas {
    private final String id;                                                // Identificador de la estación
    private final Map<EstadosBicicletas, Queue<Bicicleta>> bicicletas;      // Bicicletas asignadas a la estación
    private final int capacidadEstacion;                                    // Capacidad máxima de la estación
    public final Semaphore semExm;                                          // Semáforo de exclusión mutua para la estación
    public final Semaphore semMantenimiento;                                // Semáforo para aviar a mantenimiento

    public EstacionBicicletas(String id, int capacidadEstacion, Bicicleta... bicicletas) {
        if (bicicletas == null || Arrays.stream(bicicletas).anyMatch(Objects::isNull) || bicicletas.length == 0
                || Arrays.stream(bicicletas).map(Bicicleta::getId).distinct().count() < bicicletas.length)
            throw new IllegalArgumentException("La lista de bicicletas no es correcta, está vacía o contiene duplicados");


        this.id = (id == null || id.trim().isEmpty()) ? UUID.randomUUID().toString() : id;
        this.bicicletas = new HashMap<>();
        this.capacidadEstacion = (capacidadEstacion < CAPACIDAD_MINIMA) ?
                aleatorio.nextInt(CAPACIDAD_MINIMA, CAPACIDAD_MAXIMA) : capacidadEstacion;
        for (EstadosBicicletas estado : EstadosBicicletas.values()) {
            if (estado.equals(DISPONIBLE)) {
                this.bicicletas.put(estado, new LinkedList<>());
                this.bicicletas.get(DISPONIBLE).addAll(Arrays.asList(bicicletas));
            } else {
                this.bicicletas.put(estado, new LinkedList<>());
            }
        }

        // Inicialización de los semáforos
        this.semExm = new Semaphore(1);
        this.semMantenimiento = new Semaphore(0);
    }

    public EstacionBicicletas(Bicicleta ...bicicletas) {
        if (bicicletas == null || Arrays.stream(bicicletas).anyMatch(Objects::isNull) || bicicletas.length == 0
                || Arrays.stream(bicicletas).map(Bicicleta::getId).distinct().count() < bicicletas.length)
            throw new IllegalArgumentException("La lista de bicicletas no es correcta, está vacía o contiene duplicados");

        this.id = UUID.randomUUID().toString();
        this.capacidadEstacion = CAPACIDAD_MINIMA;
        this.bicicletas = new HashMap<>();
        for (EstadosBicicletas estado : EstadosBicicletas.values()) {
            if (estado.equals(DISPONIBLE)) {
                this.bicicletas.put(estado, new LinkedList<>());
                this.bicicletas.get(DISPONIBLE).addAll(Arrays.asList(bicicletas));
            } else {
                this.bicicletas.put(estado, new LinkedList<>());
            }
        }

        // Inicialización de los semáforos
        this.semExm = new Semaphore(1);
        this.semMantenimiento = new Semaphore(0);
    }

    public EstacionBicicletas(EstacionBicicletas original) {
        this.id = original.id;
        this.bicicletas = new HashMap<>(original.bicicletas);
        this.capacidadEstacion = original.capacidadEstacion;

        // Inicialización de los semáforos
        this.semExm = new Semaphore(1);
        this.semMantenimiento = new Semaphore(0);
    }

    public String getId() {
        return id;
    }

    public Semaphore semExm() {
        return semExm;
    }

    public Semaphore semMantenimiento() {
        return semMantenimiento;
    }

    /**
     * Permite saber el número de bibicletas que hay en la estación disponibles
     * para su alquiler
     * @return el número de bicicletas disponibles
     */
    public int getDisponibles() {
        return bicicletas.get(DISPONIBLE).size();
    }

    /**
     * Comprueba si en la estación es posible recibir nuevas bicicletas
     * @return true si hay huecos, false en otro caso
     */
    public boolean hayEspacio() {

        return capacidadEstacion - ocupacionEstacion() > 0;
    }

    /**
     * Huecos disponibles en la estación para recibir bicicletas
     * @return el número de espacios disponibles en la estación
     */
    public int getCapacidadEstacion() {
        return capacidadEstacion - ocupacionEstacion();
    }

    /**
     * La ocupación de la estación corresponde a las bicicletas DISPONIBLES, ALQUILADAS y
     * las que deben llegar a la estación en algún momento (EN_TRANSITO, REUBICACION)
     * o necesitan mantenimiento (FUERA_DE_SERVICIO)
     * @return la ocupación actual de la estación de bicicletas
     */
    public int ocupacionEstacion() {

        return  bicicletas.get(DISPONIBLE).size() +
                bicicletas.get(ALQUILADA).size() +
                bicicletas.get(EN_TRANSITO).size() +
                bicicletas.get(REUBICACION).size() +
                bicicletas.get(FUERA_DE_SERVICIO).size();
    }

    /**
     * Saber el número de bicicletas para un estado dado que hay en la estación
     * @param estado el estado que se está consultando
     * @return el número de bicicletas para ese estado
     */
    public int getBicicletas( EstadosBicicletas estado ) {
        if (estado == null)
            throw new IllegalArgumentException("El estado no puede ser null");

        return bicicletas.get(estado).size();
    }

    /**
     * Indica si hay suficientes bicicletas fuera de servicio para necesitar mantenimiento
     * @return true si el número de bicicletas es suficiente, false en otro caso
     */
    public boolean avisarMantenimiento() {
        return bicicletas.get(FUERA_DE_SERVICIO).size() > MIN_MANTENIMIENTO;
    }

    /**
     * Método para que el gestor realice una petición de alquiler de un usuario para la estación
     * de bicicletas
     * @param idUsuario del usuario que hace la solicitud y que será adjudicado a su bicicleta para el alquiler
     * @return un optional para la bicicleta alquilada
     */
    public Optional<Bicicleta> peticionAlquiler(String idUsuario) {
        return Optional.ofNullable(bicicletas.get(DISPONIBLE).poll())
                .map(bicicleta -> {
                    bicicleta.setEstado(ALQUILADA);
                    bicicletas.get(ALQUILADA).add(new Bicicleta(idUsuario, bicicleta));
                    return new Bicicleta(idUsuario, bicicleta);
                });
    }

    /**
     * Se reserva un espacio en la estación para una bicicleta que estará en tránsito y que
     * llegará más adelante
     * @param bicicleta la bicicleta que está en tránsito a esta estación
     * @return true si se ha reservado el espacio, false en otro caso
     */
    public boolean peticionTransito(Bicicleta bicicleta) {
        boolean resultado = false;

        if( hayEspacio() ) {
            bicicleta.setEstado(EN_TRANSITO);
            bicicletas.get(EN_TRANSITO).add(new Bicicleta(bicicleta));
            resultado = true;
        }

        return resultado;
    }

    /**
     * El gestor debe realizar reserva de espacio para asegurar la reubicación de las bicicletas
     * cuando se resuelva la petición
     * @param listaBicicletas bicicletas que se recepcionarán
     * @return true reserva realizada, false en otro caso
     */
    public boolean peticionReubicacion(List<Bicicleta> listaBicicletas) {
        if( listaBicicletas.size() <= getCapacidadEstacion() ) {
            // Se realizar la reserva de espacio
            listaBicicletas.forEach(bicicleta -> bicicleta.setEstado(REUBICACION));
            bicicletas.get(REUBICACION).addAll(listaBicicletas);
            listaBicicletas.clear();
        }

        return listaBicicletas.isEmpty();
    }

    /**
     * El usuario podrá recoger su bicicleta que tiene alquilada. Si el usuario pasa a recoger
     * una bicicleta que no está previamente alquilada se le devuelve un Optional vacío
     * @param idBicicleta la bicicleta que está en el proceso de recogida
     * @return el Optional asociado, está vacío si no localiza la bicicleta
     */
    public Optional<Bicicleta> recogerBicicleta(String idBicicleta) {
        Optional<Bicicleta> resultado;
        Queue<Bicicleta> bicicletasAlquiladas = bicicletas.get(ALQUILADA);

        resultado = bicicletasAlquiladas.stream()
                .filter(b -> b.getId().equals(idBicicleta))
                .findFirst();

        // Si se encuentra la bicicleta, la eliminamos de la lista
        resultado.ifPresent(bicicletasAlquiladas::remove);

        return resultado;
    }

    /**
     * El gestor puede obtener una lista de bicicletas para poder realizar una petición de reubicación
     * en una estación de destino
     * @param numBicicletas número de bicicletas para la reubicación
     * @return la lista de bicicletas para la reubicación
     */
    public List<Bicicleta> listaReubicacion(int numBicicletas) {
        List<Bicicleta> resultado = new ArrayList<>();
        Iterator<Bicicleta> it = this.bicicletas.get(DISPONIBLE).iterator();

        if (numBicicletas < 1) {
            throw new IllegalArgumentException("El número de bicicletas tiene que ser un número positivo");
        }

        while ( it.hasNext() && numBicicletas > 0) {
            Bicicleta bicicleta = it.next();
            resultado.add(bicicleta);
            it.remove();
            numBicicletas--;
        }

        return resultado;
    }

    /**
     * Cancela una operación de reubicación devolviendo a la estación una lista de bicicletas
     * @param listaBicicletas lista de bicicletas para añadir a disponibles en la estación
     * @return true si ha sido posible la operación, false en otro caso
     */
    public boolean cancelarReubicacion(List<Bicicleta> listaBicicletas) {
        if( listaBicicletas.size() <= getCapacidadEstacion() ) {
            listaBicicletas.forEach(bicicleta -> bicicleta.setEstado(DISPONIBLE));
            bicicletas.get(DISPONIBLE).addAll(listaBicicletas);
            listaBicicletas.clear();
        }

        return listaBicicletas.isEmpty();
    }

    /**
     * El usuario entrega la bicicleta en la estación de destino en su tránsito
     * También comprueba si necesita mantenimiento por algún problema o porque ha alcanzado el plazo para ello.
     * @param bicicleta la bicicleta entregada
     * @return true si se ha completado la acción de devolución
     */
    public boolean entregarBicicleta(Bicicleta bicicleta) {
        boolean encontrada = false;

        if ( bicicletas.get(EN_TRANSITO).remove(bicicleta) ) {
            // Cuando se almacena comprobamos si hay necesidad de mantenimiento
            if( necesitaMantenimiento(bicicleta) ) {
                bicicleta.setEstado(FUERA_DE_SERVICIO);
                bicicletas.get(FUERA_DE_SERVICIO).add(bicicleta);
            } else {
                bicicleta.setEstado(DISPONIBLE);
                bicicletas.get(DISPONIBLE).add(bicicleta);
            }
            encontrada = true;
        }

        return encontrada;
    }

    /**
     * El método permite la entrega de una lista de bicicletas en la estación. Este método
     * es necesario para la redistribución de las bicicletas entre las estaciones. La lista de bicicletas
     * debe estar previamente reservada para su entrega.
     * @param listaBicicletas la lista de bicicletas que se entregan
     * @return true si es posible hacer la entrega, false en otro caso
     */
    public boolean reubicarBicicleatas(List<Bicicleta> listaBicicletas) {
        Queue<Bicicleta> reubicacion = bicicletas.get(REUBICACION);

        if( reubicacion.containsAll(listaBicicletas) ) {
            // Estaba reservada su reubicación y se trasladan a disponibles
            listaBicicletas.forEach(bicicleta -> {
                reubicacion.remove(bicicleta);
                bicicleta.setEstado(DISPONIBLE);
            });
            bicicletas.get(DISPONIBLE).addAll(listaBicicletas);
            listaBicicletas.clear();
        }

        return listaBicicletas.isEmpty();
    }

    /**
     * Comprueba si una bicicleta presenta algún tipo de avería o necesita mantenimiento.
     *
     * @param bicicleta la bicicleta que se va a verificar
     * @return true si la bicicleta tiene una avería o requiere mantenimiento, false en caso contrario
     */
    private boolean necesitaMantenimiento(Bicicleta bicicleta) {
        int posibleAveria = aleatorio.nextInt(D100);

        return posibleAveria < PROB_AVERIA || vencimiento.test(bicicleta.getFechaMantenimiento());
    }

    /**
     * Se almacenan las bicicletas FUERA_DE_SERVICIO en una lista para su mantenimiento (EN_REPARACION). Luego
     * se eliminan de la estación porque dejan hueco para recibir bicicletas.
     * @return la lista de bicicletas que se han de reparar
     */
    public List<Bicicleta> mantenimientoBicicletas() {
        List<Bicicleta> resultado;
        Queue<Bicicleta> paraMantenimiento = bicicletas.get(FUERA_DE_SERVICIO);

        resultado = paraMantenimiento.stream().toList();
        resultado.forEach(bicicleta -> bicicleta.setEstado(EN_REPARACION));
        paraMantenimiento.clear();

        return resultado;
    }

    /**
     * Una representación legible de la estación de bicicletas
     *
     * @return el String que representa la estación de bicicletas
     */
    @Override
    public String toString() {
        return "EstacionBicicletas{" +
                "id='" + id + '\'' +
                ", bicicletas=" + bicicletas +
                ", capacidadEstacion=" + capacidadEstacion +
                '}';
    }
}
