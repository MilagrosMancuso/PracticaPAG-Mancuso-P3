package es.ujaen.ssccdd.datos;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;

public class ZonaMantenimiento {
    private final String id;                            // Identificador único
    private final List<Bicicleta> listaBicicletas;      // Lista de las bicicletas de en el punto de mantenimiento
    private final Semaphore exm;                        // Semáforo de exclusión mutua para las operaciones


    public ZonaMantenimiento() {
        this.id = "ZonaMantenimiento-" + UUID.randomUUID();
        this.listaBicicletas = new ArrayList<>();

        // Inicialización del semáforo
        this.exm = new Semaphore(1);
    }

    public String getId() {
        return id;
    }

    /**
     * Para saber el número de bicicletas en el punto de mantenimiento
     * @return el tamaño de la lista de bicicletas
     */
    public int bicicletasDepositadas() {
        return listaBicicletas.size();
    }

    /**
     * Para obtener el semáforo de exclusión mutua del punto de mantenimiento
     * @return el semáforo de exclusión mutua
     */
    public Semaphore semExm() {
        return exm;
    }

    /**
     * Se depositan las bicicletas de la lista en el punto de mantenimiento. La operación elimina las bicicletas
     * de la lista de depósito
     * @param listaBicicletas lista de bicicletas a depositar en el punto de mantenimiento
     */
    public void dejarBicicletas(List<Bicicleta> listaBicicletas) {
        this.listaBicicletas.addAll(listaBicicletas);

        // Las bicicletas quedan depositadas en el punto de mantenimiento
        listaBicicletas.clear();
    }

    /**
     * Recoge un número de bicicletas dado del punto de mantenimiento o las que haya en el mismo si el número
     * es menor
     * @param numBicicletas las bicicletas a recoger
     * @return la lista de bicicletas recogida
     */
    public List<Bicicleta> recogerBicicletas(int numBicicletas) {
        List<Bicicleta> recogida = new ArrayList<>();
        Iterator<Bicicleta> it = this.listaBicicletas.iterator();

        if (numBicicletas < 1) {
            throw new IllegalArgumentException("El número de bicicletas tiene que ser un número positivo");
        }

        while ( it.hasNext() && numBicicletas > 0) {
            Bicicleta bicicleta = it.next();
            recogida.add(bicicleta);
            it.remove();
            numBicicletas--;
        }

        return recogida;
    }

    @Override
    public String toString() {
        return "ZonaMantenimiento{" +
                "id='" + id + '\'' +
                ", listaBicicletas=" + listaBicicletas +
                '}';
    }
}
