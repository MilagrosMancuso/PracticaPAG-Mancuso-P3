package es.ujaen.ssccdd;

import java.time.Instant;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public interface Constantes {
    // Generador aleatorio
    Random aleatorio = new Random();


    /**
     * Suma una cantidad de segundos a un instante dado y devuelve el nuevo instante.
     */
    BiFunction<Instant, Integer, Instant> sumarSegundos = Instant::plusSeconds;


    /**
     * Predicado para comprobar si se ha alcanzado el vencimiento de un instante
     * comparando con el instante actual.
     */
    Predicate<Instant> vencimiento = (instante) -> instante.isBefore(Instant.now());

    enum EstadosBicicletas {
        DISPONIBLE(50,0), ALQUILADA(70,4), EN_REPARACION(85,2),
        EN_TRANSITO(95,6), REUBICACION(98, 0), FUERA_DE_SERVICIO(100,0);

        private final int peso;
        private final int tiempoOperacion;

        EstadosBicicletas(int peso, int tiempoOperacion) {
            this.peso = peso;
            this.tiempoOperacion = tiempoOperacion;
        }

        /**
         * Nos devuelve un estado de la bicicleta de forma aleatoria
         * según el peso asignado a cada etiqueta
         * @return el estado en el que se encuentra la bicicleta
         */
        public static EstadosBicicletas getEstado() {
            EstadosBicicletas resultado = null;
            EstadosBicicletas[] estadosBicicletas = EstadosBicicletas.values();
            int peso = aleatorio.nextInt(D100);
            int indice = 0;

            while( (indice < estadosBicicletas.length) && (resultado == null) ) {
                if( estadosBicicletas[indice].peso > peso )
                    resultado = estadosBicicletas[indice];

                indice++;
            }

            return resultado;
        }

        /**
         * Nos devuelve el tiempo en el que estará en este estado
         * @return El tiempo máximo para este estado
         */
        public int getTiempoOperacion() {
            return tiempoOperacion;
        }
    }

    /**
     * El tipo de bicicleta será necesario para decidir si es necesaria
     * una carga para poder completar el viaje
     */
    enum TipoBicicletas {
        NORMAL, ELECTRICA;

        public static TipoBicicletas generarTipo() {
            return aleatorio.nextBoolean() ? ELECTRICA : NORMAL;
        }
    }

    enum TipoEvento {
        // Eventos de Usuario
        USUARIO_SOLICITUD_TRANSPORTE(10, "Solicitud de transporte"),
        USUARIO_ESPERANDO_CONFIRMACION(11, "Esperando confirmación"),
        USUARIO_SOLICITUD_CONFIRMADA(12, "Solicitud confirmada"),
        USUARIO_SOLICITUD_RECHAZADA(13, "Solicitud rechazada"),
        USUARIO_RECOGIDA_BICICLETA(14, "Recogida de bicicleta"),
        USUARIO_INICIANDO_VIAJE(15, "Iniciando viaje"),
        USUARIO_NECESITA_RECARGA(16, "Necesita recarga de bicicleta"),
        USUARIO_FINALIZANDO_VIAJE(17, "Finalizando viaje"),
        USUARIO_ENTREGANDO_BICICLETA(18, "Entregando bicicleta"),

        // Eventos de Estación
        ESTACION_SIN_BICICLETAS(20, "Estación sin bicicletas disponibles"),
        ESTACION_BICICLETAS_BAJO_MINIMO(21, "Estación con bicicletas bajo mínimo"),
        ESTACION_SIN_ESPACIO(22, "Estación sin espacio disponible"),
        ESTACION_MANTENIMIENTO_REQUERIDO(23, "Estación requiere mantenimiento"),

        // Eventos de Bicicleta
        BICICLETA_CAMBIO_ESTADO(30, "Cambio de estado de bicicleta"),
        BICICLETA_NECESITA_MANTENIMIENTO(31, "Bicicleta necesita mantenimiento"),
        BICICLETA_NECESITA_RECARGA(32, "Bicicleta necesita recarga"),
        BICICLETA_AVERIADA(33, "Bicicleta averiada durante uso"),

        // Eventos de Técnicos
        TECNICO_SOLICITADO(40, "Técnico solicitado"),
        TECNICO_RECOGIENDO_BICICLETAS(41, "Técnico recogiendo bicicletas"),
        TECNICO_ENTREGANDO_BICICLETAS(42, "Técnico entregando bicicletas a zona de mantenimiento"),
        TECNICO_REPARANDO_BICICLETAS(43, "Técnico reparando bicicletas"),

        // Eventos de Camiones
        CAMION_SOLICITADO(50, "Camión solicitado para redistribución"),
        CAMION_RECOGIENDO_BICICLETAS(51, "Camión recogiendo bicicletas"),
        CAMION_ENTREGANDO_BICICLETAS(52, "Camión entregando bicicletas"),
        CAMION_EN_TRANSITO(53, "Camión en tránsito"),

        // Eventos de Gestor
        GESTOR_PROCESANDO_SOLICITUD(60, "Gestor procesando solicitud de transporte"),
        GESTOR_VERIFICANDO_ESTACIONES(61, "Gestor verificando estado de estaciones"),
        GESTOR_SOLICITANDO_TECNICO(62, "Gestor solicitando técnico de mantenimiento"),
        GESTOR_SOLICITANDO_CAMION(63, "Gestor solicitando camión de redistribución"),
        GESTOR_REDISTRIBUYENDO_BICICLETAS(64, "Gestor redistribuyendo bicicletas"),

        // Eventos de Recarga
        RECARGA_INICIADA(70, "Recarga de bicicleta iniciada"),
        RECARGA_FINALIZADA(71, "Recarga de bicicleta finalizada"),
        RECARGA_SIN_PUNTOS_DISPONIBLES(72, "Sin puntos de recarga disponibles"),

        // Eventos de Zona de Mantenimiento
        MANTENIMIENTO_BICICLETA_RECIBIDA(80, "Bicicleta recibida en zona de mantenimiento"),
        MANTENIMIENTO_BICICLETA_REPARADA(81, "Bicicleta reparada en zona de mantenimiento"),
        MANTENIMIENTO_BICICLETA_LISTA(82, "Bicicleta lista para redistribución"),

        // Eventos de Sistema
        SISTEMA_INICIADO(90, "Sistema iniciado"),
        SISTEMA_FINALIZADO(91, "Sistema finalizado"),
        SISTEMA_ERROR(99, "Error en el sistema");

        private final int codigo;
        private final String descripcion;

        TipoEvento(int codigo, String descripcion) {
            this.codigo = codigo;
            this.descripcion = descripcion;
        }

        public int getCodigo() {
            return codigo;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }

    int D100 = 100;                             // Simula una tirada de dado de 100 caras
    int TIEMPO_HASTA_MANTENIMIENTO = 12;        // segundo, simula el tiempo necesario para el mantenimiento
    int PROB_AVERIA = 20;                       // 20%, simula la posibilidad de avería en el uso de la bicicleta
    int PROB_CARGAR_BICICLETA = 40;             // 30%, simula la posibilidad de que se necesite cargar la bicicleta
    int CAPACIDAD_MINIMA = 5;                   // Capacidad mínima de bicicletas
    int CAPACIDAD_MAXIMA = 10;                  // Capacidad máxima de bicicletas
    int MIN_MANTENIMIENTO = 2;                  // Número mínimo de bicicletas para avisar a mantenimiento
    int MIN_BICICLETAS = 15;                    // Mínimo número de bicicletas para la simulación del sistema de transporte
    int MAX_BICICLETAS = 30;                    // Máximo número de bicicletas para la simulación del sistema de transporte
    int UNO = 1;
    int TIEMPO_TRANSPORTE = 2;
}
