package es.ujaen.ssccdd.datos;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import static es.ujaen.ssccdd.Constantes.*;
import static es.ujaen.ssccdd.Constantes.EstadosBicicletas.DISPONIBLE;
import static es.ujaen.ssccdd.Constantes.TipoBicicletas.ELECTRICA;

public class Bicicleta implements Comparable<Bicicleta> {
    private final String id;
    private EstadosBicicletas estado;
    private final TipoBicicletas tipo;
    Instant fechaEstado;
    Instant fechaMantenimiento;

    public Bicicleta() {
        this.id = UUID.randomUUID().toString();
        this.estado = DISPONIBLE;
        this.tipo = TipoBicicletas.generarTipo();
        this.fechaEstado = Instant.now();
        this.fechaMantenimiento = sumarSegundos.apply(fechaEstado, TIEMPO_HASTA_MANTENIMIENTO);
    }

    public Bicicleta(String id, TipoBicicletas tipo) {
        if ( tipo == null ) {
            throw new IllegalArgumentException("El tipo de la bicicleta tiene que estar definido");
        }

        this.id = (id == null || id.trim().isEmpty()) ? UUID.randomUUID().toString() : id;
        this.estado = DISPONIBLE;
        this.tipo = tipo;
        this.fechaEstado = Instant.now();
        this.fechaMantenimiento = sumarSegundos.apply(fechaEstado, TIEMPO_HASTA_MANTENIMIENTO);
    }

    public Bicicleta(String id, EstadosBicicletas estado, TipoBicicletas tipo) {
        if (estado == null || tipo == null) {
            throw new IllegalArgumentException("El estado y el tipo de la bicicleta deben estar definidos");
        }

        this.id = (id == null || id.trim().isEmpty()) ? UUID.randomUUID().toString() : id;
        this.estado = estado;
        this.tipo = tipo;
        this.fechaEstado = Instant.now();
        this.fechaMantenimiento = sumarSegundos.apply(fechaEstado, TIEMPO_HASTA_MANTENIMIENTO);

    }

    public Bicicleta(String id, EstadosBicicletas estado, TipoBicicletas tipo, Instant fechaEstado) {
        if (estado == null || tipo == null || fechaEstado == null) {
            throw new IllegalArgumentException("Solo el id puede no estar definido");
        }

        this.id = (id == null || id.trim().isEmpty()) ? UUID.randomUUID().toString() : id;
        this.estado = estado;
        this.tipo = tipo;
        this.fechaEstado = fechaEstado;
        this.fechaMantenimiento = sumarSegundos.apply(fechaEstado, TIEMPO_HASTA_MANTENIMIENTO);
    }

    public Bicicleta(Bicicleta original) {
        this.id = original.id;
        this.estado = original.estado;
        this.tipo = original.tipo;
        this.fechaEstado = original.fechaEstado;
        this.fechaMantenimiento = original.fechaMantenimiento;
    }

    /**
     * Hacer una copia de la bicicleta original salvo que se altera su id. Este método es de utilidad
     * para el gestor pueda hacer las reservas de bicicletas a los usuarios que la solicitan
     * @param id nuevo id de la bicicleta
     * @param original la bicicleta original.
     */
    public Bicicleta(String id, Bicicleta original) {
        this.id = id;
        this.estado = original.estado;
        this.tipo = original.tipo;
        this.fechaEstado = original.fechaEstado;
        this.fechaMantenimiento = original.fechaMantenimiento;
    }

    public String getId() {
        return id;
    }

    public EstadosBicicletas getEstado() {
        return estado;
    }

    public TipoBicicletas getTipo() {
        return tipo;
    }

    public Instant getFechaEstado() {
        return fechaEstado;
    }

    public Instant getFechaMantenimiento() {
        return fechaMantenimiento;
    }

    /**
     * Se cambia el estado de la bicicleta y se actualizan las fechas asociadas
     * @param estado el nuevo estado de la bicicleta
     */
    public void setEstado(EstadosBicicletas estado) {
        this.fechaEstado = Instant.now();
        if( estado.equals(DISPONIBLE) )
            this.fechaMantenimiento = sumarSegundos.apply(fechaEstado, TIEMPO_HASTA_MANTENIMIENTO);
        this.estado = estado;
    }

    /**
     * Nos indica si la bicicleta necesita carga para poder completar un viaje
     * @return true si es necesaria una recarga para completar el viaje
     */
    public boolean necesitaCarga() {
        int valor = aleatorio.nextInt(D100);

        return tipo.equals(ELECTRICA) && valor < PROB_CARGAR_BICICLETA;
    }

    /**
     * Permite comparar dos bicicletas para ordenarlas.
     * Si ambos IDs son numéricos, realiza una comparación numérica.
     * De lo contrario, realiza una comparación lexicográfica estándar.
     *
     * @param bicicleta con la que se tiene que hacer la comparación.
     * @return un valor negativo si esta bicicleta es menor que la proporcionada,
     *         un valor positivo si esta bicicleta es mayor, o cero si son iguales.
     */
    @Override
    public int compareTo(Bicicleta bicicleta) {
        String idOriginal = this.id;
        String idOtra = bicicleta.getId();

        // Intentar interpretar ambos IDs como números
        boolean esOriginalNumero = esUnNumero(idOriginal);
        boolean esOtraNumero = esUnNumero(idOtra);

        // Si ambos son numéricos, comparar como números
        if (esOriginalNumero && esOtraNumero) {
            try {
                long numOriginal = Long.parseLong(idOriginal);
                long numOtra = Long.parseLong(idOtra);
                return Long.compare(numOriginal, numOtra);
            } catch (NumberFormatException e) {
                // Si hay algún problema (ej. número demasiado grande),
                // volvemos a la comparación lexicográfica
                return idOriginal.compareTo(idOtra);
            }
        }

        // De lo contrario, usar comparación lexicográfica estándar
        return idOriginal.compareTo(idOtra);
    }

    /**
     * Comprueba si una cadena representa un número entero válido.
     *
     * @param cadena la cadena a comprobar
     * @return true si la cadena representa un número entero, false en caso contrario
     */
    private boolean esUnNumero(String cadena) {
        if (cadena == null || cadena.isEmpty()) {
            return false;
        }

        // Comprobar si hay un signo negativo al principio
        int i = 0;
        if (cadena.charAt(0) == '-') {
            if (cadena.length() == 1) {
                return false; // Solo el signo negativo no es un número
            }
            i = 1;
        }

        // Verificar que todos los caracteres son dígitos
        for (; i < cadena.length(); i++) {
            if (!Character.isDigit(cadena.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean equals(Object otraBicicleta) {
        if (otraBicicleta == null || getClass() != otraBicicleta.getClass()) return false;
        Bicicleta bicicleta = (Bicicleta) otraBicicleta;
        return Objects.equals(getId(), bicicleta.getId()) && getTipo() == bicicleta.getTipo();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getTipo());
    }

    /**
     * Da una representación legible de un objeto bicicleta
     * @return el String que representa a una bicicleta
     */
    @Override
    public String toString() {
        return "Bicicleta{" +
                "id='" + id + '\'' +
                ", estado=" + estado +
                ", tipo=" + tipo +
                ", fechaEstado=" + fechaEstado +
                ", fechaMantenimiento=" + fechaMantenimiento +
                '}';
    }
}
