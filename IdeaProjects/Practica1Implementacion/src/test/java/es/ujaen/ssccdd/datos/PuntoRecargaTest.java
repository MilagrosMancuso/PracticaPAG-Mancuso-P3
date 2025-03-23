package es.ujaen.ssccdd.datos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Semaphore;

import static es.ujaen.ssccdd.Constantes.*;
import static es.ujaen.ssccdd.Constantes.TipoBicicletas.ELECTRICA;
import static es.ujaen.ssccdd.Constantes.TipoBicicletas.NORMAL;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas completas de PuntoRecarga")
public class PuntoRecargaTest {

    private PuntoRecarga puntoRecarga;
    private final int CAPACIDAD_TEST = 5;

    @BeforeEach
    void setUp() {
        puntoRecarga = new PuntoRecarga(CAPACIDAD_TEST);
    }

    @Nested
    @DisplayName("Pruebas de inicialización")
    class PruebasInicializacion {

        @Test
        @DisplayName("El constructor inicializa el objeto correctamente")
        void testConstructorInicializaObjetoCorrectamente() {
            assertAll("Verificación de inicialización correcta del PuntoRecarga",
                    () -> assertNotNull(puntoRecarga, "El objeto PuntoRecarga no debería ser nulo"),
                    () -> assertTrue(puntoRecarga.getId().startsWith("PuntoRecarga-"),
                            "El ID debe comenzar con 'PuntoRecarga-'"),
                    () -> {
                        int capacidad = puntoRecarga.getCapacidad();
                        assertTrue(capacidad >= 0,
                                "La capacidad debe ser mayor o igual a cero, pero fue " + capacidad);
                    },
                    () -> assertTrue(puntoRecarga.semDeposito().availablePermits() >= 0,
                            "El semáforo de depósito debe tener permisos disponibles no negativos"),
                    () -> assertEquals(1, puntoRecarga.semExm().availablePermits(),
                            "El semáforo de exclusión mutua debe tener exactamente un permiso disponible")
            );
        }

        @Test
        @DisplayName("getId devuelve un ID único para cada PuntoRecarga")
        void testGetIdDevuelveIdUnico() {
            String id = puntoRecarga.getId();
            PuntoRecarga otroPuntoRecarga = new PuntoRecarga(CAPACIDAD_TEST);

            assertAll("Verificación de unicidad del ID",
                    () -> assertNotNull(id, "El ID no debe ser nulo"),
                    () -> assertTrue(id.startsWith("PuntoRecarga-"),
                            "El ID debe comenzar con 'PuntoRecarga-'"),
                    () -> assertNotEquals(puntoRecarga.getId(), otroPuntoRecarga.getId(),
                            "Dos instancias de PuntoRecarga deben tener IDs diferentes")
            );
        }

        @Test
        @DisplayName("Constructor con capacidad negativa")
        void testConstructorConCapacidadNegativa() {
            int capacidadNegativa = -1;
            PuntoRecarga puntoRecargaNegativo = new PuntoRecarga(capacidadNegativa);

            assertAll("Verificación de comportamiento con capacidad negativa",
                    () -> assertEquals(capacidadNegativa, puntoRecargaNegativo.getCapacidad(),
                            "La capacidad debe ser igual al valor negativo proporcionado"),
                    () -> assertEquals(capacidadNegativa, puntoRecargaNegativo.semDeposito().availablePermits(),
                            "El semáforo debe inicializarse con el mismo valor negativo de permisos")
            );
        }

        @Test
        @DisplayName("Constructor con capacidad mayor que CAPACIDAD_MAXIMA")
        void testConstructorConCapacidadMayorQueMaxima() {
            int capacidadExcesiva = CAPACIDAD_MAXIMA + 1;
            PuntoRecarga puntoRecargaExcesivo = new PuntoRecarga(capacidadExcesiva);

            assertAll("Verificación de comportamiento con capacidad excesiva",
                    () -> assertEquals(capacidadExcesiva, puntoRecargaExcesivo.getCapacidad(),
                            "La capacidad debe ser igual al valor excesivo proporcionado"),
                    () -> assertEquals(capacidadExcesiva, puntoRecargaExcesivo.semDeposito().availablePermits(),
                            "El semáforo debe inicializarse con el valor excesivo de permisos")
            );
        }

        @Test
        @DisplayName("Constructor con capacidad igual a CAPACIDAD_MAXIMA")
        void testConstructorConCapacidadIgualAMaxima() {
            PuntoRecarga puntoRecargaMaximo = new PuntoRecarga(CAPACIDAD_MAXIMA);
            int capacidad = puntoRecargaMaximo.getCapacidad();

            assertAll("Verificación de comportamiento con capacidad máxima",
                    () -> assertTrue(capacidad >= CAPACIDAD_MINIMA && capacidad <= CAPACIDAD_MAXIMA,
                            "La capacidad debe estar en el rango válido pero fue " + capacidad)
            );
        }

        @Test
        @DisplayName("Constructor con capacidad igual a CAPACIDAD_MINIMA")
        void testConstructorConCapacidadIgualAMinima() {
            PuntoRecarga puntoRecargaMinimo = new PuntoRecarga(CAPACIDAD_MINIMA);
            int capacidad = puntoRecargaMinimo.getCapacidad();

            assertAll("Verificación de comportamiento con capacidad mínima",
                    () -> assertTrue(capacidad >= CAPACIDAD_MINIMA && capacidad <= CAPACIDAD_MAXIMA,
                            "La capacidad debe estar en el rango válido pero fue " + capacidad)
            );
        }
    }

    @Nested
    @DisplayName("Pruebas de métodos getter")
    class PruebasGetters {

        @Test
        @DisplayName("getCapacidad devuelve la capacidad disponible")
        void testGetCapacidadDevuelveCapacidadDisponible() {
            int capacidad = puntoRecarga.getCapacidad();

            assertAll("Verificación de capacidad disponible",
                    () -> assertTrue(capacidad >= 0,
                            "La capacidad disponible debe ser mayor o igual a cero, pero fue " + capacidad)
            );
        }

        @Test
        @DisplayName("semDeposito devuelve el semáforo de depósito")
        void testSemDepositoDevuelveSemaforo() {
            Semaphore semDeposito = puntoRecarga.semDeposito();

            assertAll("Verificación del semáforo de depósito",
                    () -> assertNotNull(semDeposito,
                            "El semáforo de depósito no debe ser nulo"),
                    () -> assertTrue(semDeposito.availablePermits() >= 0,
                            "El semáforo debe tener permisos disponibles no negativos")
            );
        }

        @Test
        @DisplayName("semExm devuelve el semáforo de exclusión mutua")
        void testSemExmDevuelveSemaforo() {
            Semaphore semExm = puntoRecarga.semExm();

            assertAll("Verificación del semáforo de exclusión mutua",
                    () -> assertNotNull(semExm,
                            "El semáforo de exclusión mutua no debe ser nulo"),
                    () -> assertEquals(1, semExm.availablePermits(),
                            "El semáforo debe tener exactamente un permiso disponible")
            );
        }

        @Test
        @DisplayName("toString devuelve una representación en cadena del objeto")
        void testToStringDevuelveRepresentacion() {
            String toString = puntoRecarga.toString();

            assertAll("Verificación de la representación en cadena",
                    () -> assertNotNull(toString,
                            "El método toString no debe devolver null"),
                    () -> assertTrue(toString.contains(puntoRecarga.getId()),
                            "La cadena debe contener el ID del punto de recarga"),
                    () -> assertTrue(toString.contains("capacidad="),
                            "La cadena debe contener información sobre la capacidad"),
                    () -> assertTrue(toString.contains("listaBicicletas="),
                            "La cadena debe contener información sobre la lista de bicicletas")
            );
        }
    }

    @Nested
    @DisplayName("Pruebas de métodos de carga")
    class PruebasMetodosCarga {

        @Test
        @DisplayName("Prueba de iniciarCarga y finalizadaCarga en secuencia")
        void testIniciarCargaYFinalizadaCarga() {
            Bicicleta bicicletaElectrica = new Bicicleta("", ELECTRICA);
            String idBicicleta = bicicletaElectrica.getId();

            try {
                // Acceder solo al campo privado listaBicicletas mediante reflexión
                Field listaBicicletasField = PuntoRecarga.class.getDeclaredField("listaBicicletas");
                listaBicicletasField.setAccessible(true);

                // Iniciar carga (ahora llamada directa al método público)
                puntoRecarga.iniciarCarga(bicicletaElectrica);

                // Obtenemos el tamaño de la lista antes de finalizar la carga
                List<Bicicleta> listaBicicletas = (List<Bicicleta>) listaBicicletasField.get(puntoRecarga);
                int tamañoAntes = listaBicicletas.size();

                // Finalizar carga con el mismo ID (ahora llamada directa al método público)
                Optional<Bicicleta> resultado = puntoRecarga.finalizadaCarga(idBicicleta);

                // Obtenemos el tamaño de la lista después de finalizar la carga
                int tamañoDespues = listaBicicletas.size();

                assertAll("Verificación de ciclo completo de carga y eliminación de bicicleta",
                        () -> assertTrue(resultado.isPresent(),
                                "Debe encontrar la bicicleta con el ID proporcionado"),
                        () -> assertEquals(idBicicleta, resultado.get().getId(),
                                "El ID de la bicicleta recuperada debe coincidir con el original"),
                        () -> assertEquals(ELECTRICA, resultado.get().getTipo(),
                                "El tipo de la bicicleta recuperada debe ser ELÉCTRICA"),
                        () -> assertEquals(tamañoAntes - 1, tamañoDespues,
                                "La lista de bicicletas debe contener una bicicleta menos después de finalizar la carga"),
                        () -> assertFalse(listaBicicletas.stream().anyMatch(b -> b.getId().equals(idBicicleta)),
                                "No debe encontrarse la bicicleta con el ID proporcionado en la lista después de finalizar la carga")
                );
            } catch (Exception e) {
                fail("Error al probar iniciarCarga y finalizadaCarga: " + e.getMessage());
            }
        }

        @Test
        @DisplayName("Prueba de iniciarCarga con bicicleta normal")
        void testIniciarCargaConBicicletaNormal() {
            // Preparación
            Bicicleta bicicletaNormal = new Bicicleta("", NORMAL);

            try {
                // Verificación - Ahora verificamos directamente la excepción sin usar reflexión
                assertAll("Verificación de rechazo de bicicleta normal",
                        () -> {
                            // Capturamos la excepción directamente
                            Exception exception = assertThrows(
                                    IllegalArgumentException.class,
                                    () -> puntoRecarga.iniciarCarga(bicicletaNormal),
                                    "El método debe lanzar una IllegalArgumentException al intentar cargar una bicicleta normal"
                            );

                            // Verificamos el mensaje de error
                            assertEquals("Debe ser una bicicleta ELECTRICA para realizar la carga", exception.getMessage(),
                                    "El mensaje de error debe indicar que se requiere una bicicleta eléctrica");
                        }
                );

                // Verificación adicional: comprobamos que no se modifica la lista de bicicletas
                Field listaBicicletasField = PuntoRecarga.class.getDeclaredField("listaBicicletas");
                listaBicicletasField.setAccessible(true);
                List<Bicicleta> listaBicicletas = (List<Bicicleta>) listaBicicletasField.get(puntoRecarga);

                assertEquals(0, listaBicicletas.size(),
                        "La lista de bicicletas debe estar vacía después de intentar cargar una bicicleta normal");

            } catch (Exception e) {
                fail("Error inesperado al probar iniciarCarga con bicicleta normal: " + e.getMessage());
            }
        }

        @Test
        @DisplayName("Prueba de iniciarCarga con bicicleta eléctrica")
        void testIniciarCargaConBicicletaElectrica() {
            Bicicleta bicicletaElectrica = new Bicicleta("", ELECTRICA);

            // Guardamos la capacidad antes de iniciar la carga
            int capacidadAntes = puntoRecarga.getCapacidad();

            // Llamada directa al método público
            puntoRecarga.iniciarCarga(bicicletaElectrica);

            // Obtenemos la capacidad después de iniciar la carga
            int capacidadDespues = puntoRecarga.getCapacidad();

            assertAll("Verificación de aceptación de bicicleta eléctrica",
                    () -> assertEquals(capacidadAntes - 1, capacidadDespues,
                            "La capacidad disponible debe disminuir en 1 después de iniciar la carga")
            );
        }

        @Test
        @DisplayName("Prueba de finalizadaCarga con ID inexistente")
        void testFinalizadaCargaConIdInexistente() {
            // Llamada directa al método público
            Optional<Bicicleta> resultado = puntoRecarga.finalizadaCarga("ID_INEXISTENTE");

            assertAll("Verificación de ID inexistente",
                    () -> assertFalse(resultado.isPresent(),
                            "El resultado debe ser un Optional vacío para un ID inexistente")
            );
        }
    }

    @Nested
    @DisplayName("Pruebas de semáforos")
    class PruebasSemaforos {

        @Test
        @DisplayName("semDeposito permite adquirir y liberar permisos")
        void testSemDepositoPermiteAdquirirYLiberarPermisos() {
            try {
                int permisosIniciales = puntoRecarga.semDeposito().availablePermits();
                puntoRecarga.semDeposito().acquire();
                int permisosDespuesAdquirir = puntoRecarga.semDeposito().availablePermits();
                puntoRecarga.semDeposito().release();
                int permisosDespuesLiberar = puntoRecarga.semDeposito().availablePermits();

                assertAll("Verificación de adquisición y liberación de permisos en semDeposito",
                        () -> assertEquals(permisosIniciales - 1, permisosDespuesAdquirir,
                                "Debe haber un permiso menos después de adquirir"),
                        () -> assertEquals(permisosIniciales, permisosDespuesLiberar,
                                "Debe volver a tener todos los permisos después de liberar")
                );
            } catch (Exception e) {
                fail("No se pudo adquirir y liberar semDeposito: " + e.getMessage());
            }
        }

        @Test
        @DisplayName("semExm permite adquirir y liberar permisos")
        void testSemExmPermiteAdquirirYLiberarPermisos() {
            try {
                puntoRecarga.semExm().acquire();
                int permisosDespuesAdquirir = puntoRecarga.semExm().availablePermits();
                puntoRecarga.semExm().release();
                int permisosDespuesLiberar = puntoRecarga.semExm().availablePermits();

                assertAll("Verificación de adquisición y liberación de permisos en semExm",
                        () -> assertEquals(0, permisosDespuesAdquirir,
                                "No debe haber permisos disponibles después de adquirir el único permiso"),
                        () -> assertEquals(1, permisosDespuesLiberar,
                                "Debe haber exactamente un permiso disponible después de liberar")
                );
            } catch (Exception e) {
                fail("No se pudo adquirir y liberar semExm: " + e.getMessage());
            }
        }

        @Test
        @DisplayName("Prueba de semDeposito con adquisición completa")
        void testSemDepositoAdquisicionCompleta() {
            int capacidad = 3;
            PuntoRecarga puntoRecargaLocal = new PuntoRecarga(capacidad);

            try {
                for (int i = 0; i < capacidad; i++) {
                    puntoRecargaLocal.semDeposito().acquire();
                }
                int permisosDespuesAdquirirTodos = puntoRecargaLocal.semDeposito().availablePermits();

                for (int i = 0; i < capacidad; i++) {
                    puntoRecargaLocal.semDeposito().release();
                }
                int permisosDespuesLiberarTodos = puntoRecargaLocal.semDeposito().availablePermits();

                assertAll("Verificación de adquisición completa de permisos",
                        () -> assertEquals(0, permisosDespuesAdquirirTodos,
                                "No debe quedar ningún permiso disponible después de adquirirlos todos"),
                        () -> assertEquals(capacidad, permisosDespuesLiberarTodos,
                                "Deben estar disponibles todos los permisos después de liberarlos")
                );
            } catch (Exception e) {
                fail("Error al probar semDeposito con adquisición completa: " + e.getMessage());
            }
        }

        @Test
        @DisplayName("Prueba de semDeposito con intento de adquisición por encima de la capacidad")
        void testSemDepositoIntentoDeSobrepasoCapacidad() {
            int capacidad = 2;
            PuntoRecarga puntoRecargaLocal = new PuntoRecarga(capacidad);
            boolean[] resultadoAdquisicion = {true}; // Para capturar el resultado desde el lambda

            try {
                puntoRecargaLocal.semDeposito().acquire(capacidad);
                int permisosDespuesAdquirirTodos = puntoRecargaLocal.semDeposito().availablePermits();

                Thread hiloAuxiliar = new Thread(() -> {
                    try {
                        resultadoAdquisicion[0] = puntoRecargaLocal.semDeposito().tryAcquire(100, java.util.concurrent.TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });

                hiloAuxiliar.start();
                hiloAuxiliar.join();

                puntoRecargaLocal.semDeposito().release(capacidad);

                assertAll("Verificación de intento de adquisición por encima de la capacidad",
                        () -> assertEquals(0, permisosDespuesAdquirirTodos,
                                "No debe quedar ningún permiso disponible después de adquirirlos todos"),
                        () -> assertFalse(resultadoAdquisicion[0],
                                "No debe ser posible adquirir más permisos cuando ya se han agotado")
                );
            } catch (Exception e) {
                fail("Error al probar semDeposito con intento de sobrepaso de capacidad: " + e.getMessage());
            }
        }

        @Test
        @DisplayName("Prueba de semExm con múltiples hilos intentando adquirir simultáneamente")
        void testSemExmMultiplesHilos() {
            int cantidadHilos = 10;
            int[] contador = {0};

            try {
                Thread[] hilos = new Thread[cantidadHilos];
                for (int i = 0; i < cantidadHilos; i++) {
                    hilos[i] = new Thread(() -> {
                        try {
                            puntoRecarga.semExm().acquire();
                            contador[0]++;
                            Thread.sleep(10);
                            puntoRecarga.semExm().release();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    });
                    hilos[i].start();
                }

                for (Thread hilo : hilos) {
                    hilo.join();
                }

                assertAll("Verificación de exclusión mutua con múltiples hilos",
                        () -> assertEquals(cantidadHilos, contador[0],
                                "El contador debe incrementarse exactamente una vez por cada hilo")
                );
            } catch (Exception e) {
                fail("Error al probar semExm con múltiples hilos: " + e.getMessage());
            }
        }
    }
}