package es.ujaen.ssccdd.datos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Semaphore;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests para la clase Peticion")
class PeticionTest {

    private static final String TEST_ID = "usuario-1";
    private static final String ORIGEN_ID = "estacion-1";
    private static final String DESTINO_ID = "estacion-2";
    private static final int NUM_BICICLETAS = 1;
    private Semaphore semaforo;
    private Peticion peticionBase;

    @BeforeEach
    void setUp() {
        // Inicializamos un semáforo y una petición para utilizarlos en múltiples tests
        semaforo = new Semaphore(0);
        peticionBase = new Peticion(TEST_ID, ORIGEN_ID, DESTINO_ID, NUM_BICICLETAS, semaforo);
    }

    @Nested
    @DisplayName("Tests de constructor")
    class ConstructorTests {

        @Test
        @DisplayName("Constructor completo con parámetros válidos crea objeto correcto")
        void testFullConstructor() {
            Peticion peticion = new Peticion(TEST_ID, ORIGEN_ID, DESTINO_ID, NUM_BICICLETAS, semaforo);

            assertEquals(TEST_ID, peticion.getId(), "El ID del solicitante debe ser el mismo que se pasó al constructor");
            assertEquals(ORIGEN_ID, peticion.getOrigen(), "El ID de origen debe ser el mismo que se pasó al constructor");
            assertEquals(DESTINO_ID, peticion.getDestino(), "El ID de destino debe ser el mismo que se pasó al constructor");
            assertEquals(NUM_BICICLETAS, peticion.getBicicletasPeticion(), "El número de bicicletas debe ser el mismo que se pasó al constructor");
            assertSame(semaforo, peticion.semResolucion(), "El semáforo debe ser el mismo que se pasó al constructor");
        }

        @Test
        @DisplayName("Constructor sin número de bicicletas crea objeto con una bicicleta")
        void testConstructorWithoutBicycleCount() {
            // Prueba del nuevo constructor sin número de bicicletas
            Peticion peticion = new Peticion(TEST_ID, ORIGEN_ID, DESTINO_ID, semaforo);

            assertEquals(TEST_ID, peticion.getId(), "El ID del solicitante debe ser el mismo que se pasó al constructor");
            assertEquals(ORIGEN_ID, peticion.getOrigen(), "El ID de origen debe ser el mismo que se pasó al constructor");
            assertEquals(DESTINO_ID, peticion.getDestino(), "El ID de destino debe ser el mismo que se pasó al constructor");
            assertEquals(1, peticion.getBicicletasPeticion(), "El número de bicicletas debe ser 1 cuando se usa el constructor sin especificar cantidad");
            assertSame(semaforo, peticion.semResolucion(), "El semáforo debe ser el mismo que se pasó al constructor");
        }

        @Test
        @DisplayName("Constructor acepta origen y destino nulos")
        void testNullOrigenDestinoConstructor() {
            Peticion peticion = new Peticion(TEST_ID, null, null, NUM_BICICLETAS, semaforo);

            assertEquals(TEST_ID, peticion.getId(), "El ID del solicitante debe ser el mismo que se pasó al constructor");
            assertNull(peticion.getOrigen(), "El origen debe ser null si se pasa null al constructor");
            assertNull(peticion.getDestino(), "El destino debe ser null si se pasa null al constructor");

            // También probamos el nuevo constructor
            Peticion peticionSimplificada = new Peticion(TEST_ID, null, null, semaforo);

            assertEquals(TEST_ID, peticionSimplificada.getId(), "El ID del solicitante debe ser el mismo que se pasó al constructor");
            assertNull(peticionSimplificada.getOrigen(), "El origen debe ser null si se pasa null al constructor");
            assertNull(peticionSimplificada.getDestino(), "El destino debe ser null si se pasa null al constructor");
            assertEquals(1, peticionSimplificada.getBicicletasPeticion(), "El número de bicicletas debe ser 1");
        }

        @Test
        @DisplayName("Constructor lanza excepción con ID nulo")
        void testNullIdConstructor() {
            assertThrows(IllegalArgumentException.class, () -> {
                new Peticion(null, ORIGEN_ID, DESTINO_ID, NUM_BICICLETAS, semaforo);
            }, "Debe lanzarse una excepción si el ID es null");

            // También probamos el nuevo constructor
            assertThrows(IllegalArgumentException.class, () -> {
                new Peticion(null, ORIGEN_ID, DESTINO_ID, semaforo);
            }, "Debe lanzarse una excepción si el ID es null en el constructor sin número de bicicletas");
        }

        @Test
        @DisplayName("Constructor lanza excepción con ID vacío")
        void testEmptyIdConstructor() {
            assertThrows(IllegalArgumentException.class, () -> {
                new Peticion("", ORIGEN_ID, DESTINO_ID, NUM_BICICLETAS, semaforo);
            }, "Debe lanzarse una excepción si el ID es una cadena vacía");

            // También probamos el nuevo constructor
            assertThrows(IllegalArgumentException.class, () -> {
                new Peticion("", ORIGEN_ID, DESTINO_ID, semaforo);
            }, "Debe lanzarse una excepción si el ID es una cadena vacía en el constructor sin número de bicicletas");
        }

        @Test
        @DisplayName("Constructor lanza excepción con ID en blanco")
        void testBlankIdConstructor() {
            assertThrows(IllegalArgumentException.class, () -> {
                new Peticion("   ", ORIGEN_ID, DESTINO_ID, NUM_BICICLETAS, semaforo);
            }, "Debe lanzarse una excepción si el ID solo contiene espacios en blanco");

            // También probamos el nuevo constructor
            assertThrows(IllegalArgumentException.class, () -> {
                new Peticion("   ", ORIGEN_ID, DESTINO_ID, semaforo);
            }, "Debe lanzarse una excepción si el ID solo contiene espacios en blanco en el constructor sin número de bicicletas");
        }

        @Test
        @DisplayName("Constructor lanza excepción con semáforo nulo")
        void testNullSemaforoConstructor() {
            assertThrows(IllegalArgumentException.class, () -> {
                new Peticion(TEST_ID, ORIGEN_ID, DESTINO_ID, NUM_BICICLETAS, null);
            }, "Debe lanzarse una excepción si el semáforo es null");

            // También probamos el nuevo constructor
            assertThrows(IllegalArgumentException.class, () -> {
                new Peticion(TEST_ID, ORIGEN_ID, DESTINO_ID, null);
            }, "Debe lanzarse una excepción si el semáforo es null en el constructor sin número de bicicletas");
        }

        @Test
        @DisplayName("Constructor lanza excepción con número de bicicletas menor que 1")
        void testInvalidNumBicicletasConstructor() {
            assertThrows(IllegalArgumentException.class, () -> {
                new Peticion(TEST_ID, ORIGEN_ID, DESTINO_ID, 0, semaforo);
            }, "Debe lanzarse una excepción si el número de bicicletas es menor que 1");

            assertThrows(IllegalArgumentException.class, () -> {
                new Peticion(TEST_ID, ORIGEN_ID, DESTINO_ID, -1, semaforo);
            }, "Debe lanzarse una excepción si el número de bicicletas es negativo");
        }
    }

    @Nested
    @DisplayName("Tests de getters")
    class GetterTests {

        @Test
        @DisplayName("Getter getId devuelve valor correcto")
        void testGetId() {
            assertEquals(TEST_ID, peticionBase.getId(),
                    "getId() debe devolver el ID del solicitante");
        }

        @Test
        @DisplayName("Getter getOrigen devuelve valor correcto")
        void testGetOrigen() {
            assertEquals(ORIGEN_ID, peticionBase.getOrigen(),
                    "getOrigen() debe devolver el ID de origen");
        }

        @Test
        @DisplayName("Getter getDestino devuelve valor correcto")
        void testGetDestino() {
            assertEquals(DESTINO_ID, peticionBase.getDestino(),
                    "getDestino() debe devolver el ID de destino");
        }

        @Test
        @DisplayName("Getter getBicicletasPeticion devuelve valor correcto")
        void testGetBicicletasPeticion() {
            assertEquals(NUM_BICICLETAS, peticionBase.getBicicletasPeticion(),
                    "getBicicletasPeticion() debe devolver el número de bicicletas solicitado");
        }

        @Test
        @DisplayName("Getter semResolucion devuelve referencia correcta")
        void testSemResolucion() {
            assertSame(semaforo, peticionBase.semResolucion(),
                    "semResolucion() debe devolver el semáforo pasado al constructor");
        }
    }

    @Nested
    @DisplayName("Tests de métodos de la clase")
    class MethodTests {

        @Test
        @DisplayName("Método toString genera string con formato correcto")
        void testToString() {
            String str = peticionBase.toString();

            // Verificamos que contenga las partes importantes
            assertTrue(str.contains(TEST_ID), "toString() debe incluir el ID del solicitante");
            assertTrue(str.contains(ORIGEN_ID), "toString() debe incluir el ID de origen");
            assertTrue(str.contains(DESTINO_ID), "toString() debe incluir el ID de destino");
            assertTrue(str.contains(String.valueOf(NUM_BICICLETAS)), "toString() debe incluir el número de bicicletas");

            // Verificamos que NO contenga el semáforo (no está incluido en el toString)
            assertFalse(str.contains("resolucion"), "toString() no debe incluir el semáforo");
        }
    }

    @Nested
    @DisplayName("Tests de comportamiento del semáforo")
    class SemaphoreTests {

        @Test
        @DisplayName("Semáforo permite operaciones de acquire y release")
        void testSemaphoreOperations() throws InterruptedException {
            Semaphore sem = new Semaphore(1);
            Peticion peticion = new Peticion(TEST_ID, ORIGEN_ID, DESTINO_ID, NUM_BICICLETAS, sem);

            // Verificamos que podemos adquirir el permiso
            assertTrue(peticion.semResolucion().tryAcquire(),
                    "Debería poder adquirir el permiso inicialmente disponible");

            // Verificamos que no podemos adquirir otro permiso
            assertFalse(peticion.semResolucion().tryAcquire(),
                    "No debería poder adquirir más permisos de los disponibles");

            // Liberamos el permiso
            peticion.semResolucion().release();

            // Verificamos que podemos volver a adquirir el permiso
            assertTrue(peticion.semResolucion().tryAcquire(),
                    "Debería poder adquirir el permiso después de liberarlo");
        }

        @Test
        @DisplayName("Semáforo inicializado a 0 no permite acquire inmediato")
        void testZeroPermitSemaphore() {
            // El semáforo inicializado en setUp tiene 0 permisos
            assertFalse(peticionBase.semResolucion().tryAcquire(),
                    "No debería poder adquirir un permiso de un semáforo con 0 permisos");

            // Liberamos un permiso
            peticionBase.semResolucion().release();

            // Ahora debería haber un permiso disponible
            assertTrue(peticionBase.semResolucion().tryAcquire(),
                    "Debería poder adquirir un permiso después de liberarlo");
        }
    }
}