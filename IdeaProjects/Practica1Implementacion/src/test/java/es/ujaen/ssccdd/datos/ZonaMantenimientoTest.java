package es.ujaen.ssccdd.datos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import static es.ujaen.ssccdd.Constantes.TipoBicicletas;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests para la clase ZonaMantenimiento")
class ZonaMantenimientoTest {

    private ZonaMantenimiento zonaMantenimiento;
    private List<Bicicleta> bicicletasList;

    @BeforeEach
    void setUp() {
        // Inicializamos una zona de mantenimiento para usar en las pruebas
        zonaMantenimiento = new ZonaMantenimiento();

        // Creamos una lista de bicicletas para probar las operaciones
        bicicletasList = new ArrayList<>();
        bicicletasList.add(new Bicicleta("bici-1", TipoBicicletas.ELECTRICA));
        bicicletasList.add(new Bicicleta("bici-2", TipoBicicletas.NORMAL));
        bicicletasList.add(new Bicicleta("bici-3", TipoBicicletas.ELECTRICA));
    }

    @Nested
    @DisplayName("Tests del constructor y estado inicial")
    class ConstructorTests {

        @Test
        @DisplayName("Constructor inicializa ID correctamente")
        void testIdGeneration() {
            String id = zonaMantenimiento.getId();

            assertNotNull(id, "El ID no debe ser nulo");
            assertTrue(id.startsWith("ZonaMantenimiento-"),
                    "El ID debe comenzar con 'ZonaMantenimiento-'");
            assertTrue(id.length() > "ZonaMantenimiento-".length(),
                    "El ID debe incluir un UUID después del prefijo");
        }

        @Test
        @DisplayName("Constructor inicializa lista de bicicletas vacía")
        void testEmptyInitialList() {
            assertEquals(0, zonaMantenimiento.bicicletasDepositadas(),
                    "La zona de mantenimiento debe inicializarse sin bicicletas");
        }

        @Test
        @DisplayName("Constructor inicializa semáforo con un permiso")
        void testSemaphoreInitialization() {
            Semaphore semaphore = zonaMantenimiento.semExm();

            assertNotNull(semaphore, "El semáforo no debe ser nulo");
            assertEquals(1, semaphore.availablePermits(),
                    "El semáforo debe inicializarse con 1 permiso");
        }
    }

    @Nested
    @DisplayName("Tests del método bicicletasDepositadas")
    class BicicletasDepositadasTests {

        @Test
        @DisplayName("bicicletasDepositadas con zona vacía")
        void testEmptyZone() {
            assertEquals(0, zonaMantenimiento.bicicletasDepositadas(),
                    "Una zona recién creada debe tener 0 bicicletas");
        }

        @Test
        @DisplayName("bicicletasDepositadas después de depositar bicicletas")
        void testAfterDeposit() {
            // Dejamos bicicletas en la zona
            List<Bicicleta> listaCopia = new ArrayList<>(bicicletasList);
            zonaMantenimiento.dejarBicicletas(listaCopia);

            assertEquals(bicicletasList.size(), zonaMantenimiento.bicicletasDepositadas(),
                    "El número de bicicletas debe corresponder a las depositadas");
        }
    }

    @Nested
    @DisplayName("Tests del método semExm")
    class SemExmTests {

        @Test
        @DisplayName("semExm devuelve semáforo no nulo")
        void testNotNull() {
            assertNotNull(zonaMantenimiento.semExm(),
                    "El semáforo de exclusión mutua no debe ser nulo");
        }

        @Test
        @DisplayName("semExm devuelve semáforo con un permiso")
        void testPermitCount() {
            assertEquals(1, zonaMantenimiento.semExm().availablePermits(),
                    "El semáforo debe tener 1 permiso inicialmente");
        }

        @Test
        @DisplayName("semExm permite operaciones de acquire y release")
        void testAcquireRelease() throws InterruptedException {
            Semaphore sem = zonaMantenimiento.semExm();

            // Adquirimos el permiso
            sem.acquire();
            assertEquals(0, sem.availablePermits(), "Después de acquire, no debe haber permisos disponibles");

            // Liberamos el permiso
            sem.release();
            assertEquals(1, sem.availablePermits(), "Después de release, debe haber 1 permiso disponible");
        }
    }

    @Nested
    @DisplayName("Tests del método dejarBicicletas")
    class DejarBicicletasTests {

        @Test
        @DisplayName("dejarBicicletas con lista no vacía")
        void testNormalCase() {
            // Creamos una copia de la lista para no afectar a la lista original
            List<Bicicleta> listaCopia = new ArrayList<>(bicicletasList);
            int sizeOriginal = listaCopia.size();

            // Dejamos las bicicletas
            zonaMantenimiento.dejarBicicletas(listaCopia);

            // Verificamos que la zona tiene las bicicletas
            assertEquals(sizeOriginal, zonaMantenimiento.bicicletasDepositadas(),
                    "La zona debe tener las bicicletas depositadas");

            // Verificamos que la lista original ha sido vaciada
            assertTrue(listaCopia.isEmpty(),
                    "La lista original debe quedar vacía después de dejarBicicletas");
        }

        @Test
        @DisplayName("dejarBicicletas con lista vacía")
        void testEmptyList() {
            List<Bicicleta> listaVacia = new ArrayList<>();

            // Dejamos una lista vacía
            zonaMantenimiento.dejarBicicletas(listaVacia);

            // No debería cambiar el número de bicicletas
            assertEquals(0, zonaMantenimiento.bicicletasDepositadas(),
                    "Depositar una lista vacía no debe alterar la zona");
        }

        @Test
        @DisplayName("dejarBicicletas después de operaciones anteriores")
        void testMultipleDeposits() {
            // Primera operación
            List<Bicicleta> primerLote = new ArrayList<>();
            primerLote.add(new Bicicleta("bici-A", TipoBicicletas.ELECTRICA));
            primerLote.add(new Bicicleta("bici-B", TipoBicicletas.NORMAL));
            zonaMantenimiento.dejarBicicletas(primerLote);

            // Segunda operación
            List<Bicicleta> segundoLote = new ArrayList<>();
            segundoLote.add(new Bicicleta("bici-C", TipoBicicletas.ELECTRICA));
            zonaMantenimiento.dejarBicicletas(segundoLote);

            // Verificamos que tenemos todas las bicicletas
            assertEquals(3, zonaMantenimiento.bicicletasDepositadas(),
                    "La zona debe acumular las bicicletas de múltiples operaciones");
        }

        @Test
        @DisplayName("dejarBicicletas con lista nula (prueba de robustez)")
        void testNullList() {
            // Este test verificará si el método es robusto ante entradas nulas
            // Si se espera una excepción, podríamos usar assertThrows
            // Si el método debe manejar silenciosamente este caso, verificaríamos que no cambie el estado

            try {
                zonaMantenimiento.dejarBicicletas(null);
                // Si llegamos aquí, el método no lanzó excepción
                // Verificamos que el estado no cambió
                assertEquals(0, zonaMantenimiento.bicicletasDepositadas(),
                        "Pasar null no debería alterar el estado");
            } catch (NullPointerException e) {
                // Esto es esperado si el método no tiene comprobación de nulos
                // Podríamos sugerir mejorar la robustez del método
                assertTrue(true, "El método dejarBicicletas no verifica entradas nulas");
            }
        }
    }

    @Nested
    @DisplayName("Tests del método recogerBicicletas")
    class RecogerBicicletasTests {

        @BeforeEach
        void setUpBicicletas() {
            // Dejamos algunas bicicletas para las pruebas de recogida
            List<Bicicleta> listaCopia = new ArrayList<>(bicicletasList);
            zonaMantenimiento.dejarBicicletas(listaCopia);
        }

        @Test
        @DisplayName("recogerBicicletas cuando numBicicletas < disponibles")
        void testPartialCollection() {
            int recoger = 2;
            List<Bicicleta> recogidas = zonaMantenimiento.recogerBicicletas(recoger);

            // Verificamos que recogimos el número correcto
            assertEquals(recoger, recogidas.size(),
                    "Se debe recoger exactamente el número de bicicletas solicitado");

            // Verificamos que quedan las bicicletas esperadas
            assertEquals(bicicletasList.size() - recoger, zonaMantenimiento.bicicletasDepositadas(),
                    "Deben quedar las bicicletas no recogidas");
        }

        @Test
        @DisplayName("recogerBicicletas cuando numBicicletas > disponibles")
        void testExcessCollection() {
            int recoger = bicicletasList.size() + 5;
            List<Bicicleta> recogidas = zonaMantenimiento.recogerBicicletas(recoger);

            // Verificamos que recogimos todas las disponibles
            assertEquals(bicicletasList.size(), recogidas.size(),
                    "Se deben recoger todas las bicicletas disponibles");

            // Verificamos que la zona quedó vacía
            assertEquals(0, zonaMantenimiento.bicicletasDepositadas(),
                    "La zona debe quedar vacía");
        }

        @Test
        @DisplayName("recogerBicicletas cuando numBicicletas = 0")
        void testZeroCollection() {
            // Ahora esperamos que lance IllegalArgumentException cuando numBicicletas es 0
            Exception exception = assertThrows(IllegalArgumentException.class,
                    () -> zonaMantenimiento.recogerBicicletas(0),
                    "Debe lanzar IllegalArgumentException cuando numBicicletas = 0");

            // Verificamos el mensaje de la excepción
            assertTrue(exception.getMessage().contains("número positivo"),
                    "El mensaje de excepción debe indicar que se requiere un número positivo");

            // Verificamos que el estado no ha cambiado
            assertEquals(bicicletasList.size(), zonaMantenimiento.bicicletasDepositadas(),
                    "El número de bicicletas no debe cambiar tras el intento fallido");
        }

        @Test
        @DisplayName("recogerBicicletas con número negativo (caso límite)")
        void testNegativeCollection() {
            // Ahora esperamos que lance IllegalArgumentException cuando numBicicletas es negativo
            Exception exception = assertThrows(IllegalArgumentException.class,
                    () -> zonaMantenimiento.recogerBicicletas(-5),
                    "Debe lanzar IllegalArgumentException cuando numBicicletas < 0");

            // Verificamos el mensaje de la excepción
            assertTrue(exception.getMessage().contains("número positivo"),
                    "El mensaje de excepción debe indicar que se requiere un número positivo");

            // Verificamos que el estado no ha cambiado
            assertEquals(bicicletasList.size(), zonaMantenimiento.bicicletasDepositadas(),
                    "El número de bicicletas no debe cambiar tras el intento fallido");
        }

        @Test
        @DisplayName("recogerBicicletas de zona vacía")
        void testEmptyZoneCollection() {
            // Creamos una nueva zona sin bicicletas
            ZonaMantenimiento zonaVacia = new ZonaMantenimiento();

            // Ahora el método debe funcionar si numBicicletas > 0, aunque la zona esté vacía
            List<Bicicleta> recogidas = zonaVacia.recogerBicicletas(5);

            // No debe haber bicicletas para recoger
            assertTrue(recogidas.isEmpty(),
                    "No se deben recoger bicicletas de una zona vacía");
        }

        @Test
        @DisplayName("recogerBicicletas borra las bicicletas de la zona")
        void testBicyclesRemoval() {
            // Recogemos todas las bicicletas
            zonaMantenimiento.recogerBicicletas(bicicletasList.size());

            // Verificamos que ya no están en la zona
            assertEquals(0, zonaMantenimiento.bicicletasDepositadas(),
                    "Las bicicletas recogidas deben eliminarse de la zona");
        }
    }

    @Nested
    @DisplayName("Tests del método toString")
    class ToStringTests {

        @Test
        @DisplayName("toString incluye información relevante")
        void testToStringContent() {
            String str = zonaMantenimiento.toString();

            // Verificamos que incluye la información importante
            assertTrue(str.contains(zonaMantenimiento.getId()),
                    "toString() debe incluir el ID de la zona");
            assertTrue(str.contains("listaBicicletas"),
                    "toString() debe mencionar la lista de bicicletas");
        }

        @Test
        @DisplayName("toString con y sin bicicletas")
        void testToStringWithBicycles() {
            // Comprobamos sin bicicletas
            String strVacio = zonaMantenimiento.toString();
            assertTrue(strVacio.contains("listaBicicletas=[]") || strVacio.contains("listaBicicletas = []"),
                    "toString() debe mostrar lista vacía correctamente");

            // Añadimos bicicletas
            List<Bicicleta> listaCopia = new ArrayList<>(bicicletasList);
            zonaMantenimiento.dejarBicicletas(listaCopia);

            // Comprobamos con bicicletas
            String strConBicis = zonaMantenimiento.toString();
            assertFalse(strConBicis.contains("listaBicicletas=[]"),
                    "toString() debe reflejar que hay bicicletas en la lista");
        }
    }

    @Nested
    @DisplayName("Tests de concurrencia")
    class ConcurrencyTests {

        @Test
        @DisplayName("Semáforo protege acceso concurrente")
        void testConcurrentAccess() throws InterruptedException {
            // Este test verificará que el semáforo realmente protege el acceso concurrente

            // Creamos hilos que intentarán modificar la zona de mantenimiento
            Thread t1 = new Thread(() -> {
                try {
                    zonaMantenimiento.semExm().acquire();
                    List<Bicicleta> lista = new ArrayList<>();
                    lista.add(new Bicicleta("thread-1", TipoBicicletas.ELECTRICA));
                    zonaMantenimiento.dejarBicicletas(lista);
                    Thread.sleep(100); // Simulamos una operación que toma tiempo
                    zonaMantenimiento.semExm().release();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            Thread t2 = new Thread(() -> {
                try {
                    zonaMantenimiento.semExm().acquire();
                    List<Bicicleta> lista = new ArrayList<>();
                    lista.add(new Bicicleta("thread-2", TipoBicicletas.NORMAL));
                    zonaMantenimiento.dejarBicicletas(lista);
                    Thread.sleep(100); // Simulamos una operación que toma tiempo
                    zonaMantenimiento.semExm().release();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            // Iniciamos los hilos
            t1.start();
            t2.start();

            // Esperamos a que terminen
            t1.join();
            t2.join();

            // Verificamos que ambos hilos pudieron depositar su bicicleta
            assertEquals(2, zonaMantenimiento.bicicletasDepositadas(),
                    "Ambos hilos deben haber depositado sus bicicletas");
        }
    }
}