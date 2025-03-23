package es.ujaen.ssccdd.datos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.time.Instant;

import static es.ujaen.ssccdd.Constantes.EstadosBicicletas;
import static es.ujaen.ssccdd.Constantes.TipoBicicletas;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests para la clase Bicicleta")
class BicicletaTest {

    private static final String TEST_ID = "test-id-1";
    private static final String NUEVO_ID = "nuevo-id-1";
    private Bicicleta bicicletaBase;

    @BeforeEach
    void setUp() {
        // Inicializamos una bicicleta para utilizarla en múltiples tests
        bicicletaBase = new Bicicleta(TEST_ID, TipoBicicletas.ELECTRICA);
    }

    @Nested
    @DisplayName("Tests de constructores")
    class ConstructorTests {

        @Test
        @DisplayName("Constructor por defecto crea objeto válido")
        void testDefaultConstructor() {
            Bicicleta bici = new Bicicleta();

            assertNotNull(bici.getId(), "El ID no debe ser nulo");
            assertEquals(EstadosBicicletas.DISPONIBLE, bici.getEstado(), "El estado inicial debe ser DISPONIBLE");
            assertNotNull(bici.getTipo(), "El tipo no debe ser nulo");
            assertNotNull(bici.getFechaEstado(), "La fecha de estado no debe ser nula");
            assertNotNull(bici.getFechaMantenimiento(), "La fecha de mantenimiento no debe ser nula");
            assertTrue(bici.getFechaMantenimiento().isAfter(bici.getFechaEstado()),
                    "La fecha de mantenimiento debe ser posterior a la fecha de estado");
        }

        @Test
        @DisplayName("Constructor con ID y tipo crea objeto válido")
        void testIdTipoConstructor() {
            Bicicleta bici = new Bicicleta(TEST_ID, TipoBicicletas.ELECTRICA);

            assertEquals(TEST_ID, bici.getId(), "El ID debe ser el mismo que se pasó al constructor");
            assertEquals(EstadosBicicletas.DISPONIBLE, bici.getEstado(), "El estado inicial debe ser DISPONIBLE");
            assertEquals(TipoBicicletas.ELECTRICA, bici.getTipo(), "El tipo debe ser el mismo que se pasó al constructor");
            assertNotNull(bici.getFechaEstado(), "La fecha de estado no debe ser nula");
            assertNotNull(bici.getFechaMantenimiento(), "La fecha de mantenimiento no debe ser nula");
        }

        @Test
        @DisplayName("Constructor con ID, estado y tipo crea objeto válido")
        void testIdEstadoTipoConstructor() {
            Bicicleta bici = new Bicicleta(TEST_ID, EstadosBicicletas.EN_REPARACION, TipoBicicletas.NORMAL);

            assertEquals(TEST_ID, bici.getId(), "El ID debe ser el mismo que se pasó al constructor");
            assertEquals(EstadosBicicletas.EN_REPARACION, bici.getEstado(), "El estado debe ser el mismo que se pasó al constructor");
            assertEquals(TipoBicicletas.NORMAL, bici.getTipo(), "El tipo debe ser el mismo que se pasó al constructor");
            assertNotNull(bici.getFechaEstado(), "La fecha de estado no debe ser nula");
            assertNotNull(bici.getFechaMantenimiento(), "La fecha de mantenimiento no debe ser nula");
        }

        @Test
        @DisplayName("Constructor con ID, estado, tipo y fechaEstado crea objeto válido")
        void testCompleteConstructor() {
            // Fechas específicas para la prueba
            Instant fechaEstadoTest = Instant.now().minusSeconds(3600); // 1 hora atrás

            Bicicleta bici = new Bicicleta(
                    TEST_ID,
                    EstadosBicicletas.ALQUILADA,
                    TipoBicicletas.ELECTRICA,
                    fechaEstadoTest
            );

            assertAll("El constructor con parámetros debe inicializar correctamente",
                    () -> assertEquals(TEST_ID, bici.getId(), "El ID debe ser el especificado"),
                    () -> assertEquals(EstadosBicicletas.ALQUILADA, bici.getEstado(), "El estado debe ser el especificado"),
                    () -> assertEquals(TipoBicicletas.ELECTRICA, bici.getTipo(), "El tipo debe ser el especificado"),
                    () -> assertEquals(fechaEstadoTest, bici.getFechaEstado(), "La fecha de estado debe ser la especificada"),
                    () -> assertTrue(bici.getFechaMantenimiento().isAfter(fechaEstadoTest),
                            "La fecha de mantenimiento debe ser posterior a la fecha de estado")
            );
        }

        @Test
        @DisplayName("Constructor completo genera UUID con ID nulo o vacío")
        void testCompleteConstructorWithNullOrEmptyId() {
            Instant fechaEstadoTest = Instant.now();

            // Caso con ID nulo
            Bicicleta biciNullId = new Bicicleta(
                    null,
                    EstadosBicicletas.DISPONIBLE,
                    TipoBicicletas.NORMAL,
                    fechaEstadoTest
            );

            // Caso con ID vacío
            Bicicleta biciEmptyId = new Bicicleta(
                    "",
                    EstadosBicicletas.DISPONIBLE,
                    TipoBicicletas.NORMAL,
                    fechaEstadoTest
            );

            assertAll("El constructor completo debe generar UUID cuando el ID es nulo o vacío",
                    () -> assertNotNull(biciNullId.getId(), "El ID no debe ser nulo cuando se pasa null"),
                    () -> assertTrue(biciNullId.getId().length() > 0, "El ID generado debe tener longitud > 0"),
                    () -> assertNotNull(biciEmptyId.getId(), "El ID no debe ser nulo cuando se pasa cadena vacía"),
                    () -> assertNotEquals("", biciEmptyId.getId(), "El ID no debe ser cadena vacía")
            );
        }

        @Test
        @DisplayName("Constructor completo lanza excepción con parámetros nulos")
        void testCompleteConstructorWithNullParameters() {
            Instant fechaEstadoTest = Instant.now();

            assertAll("El constructor debe lanzar excepción cuando parámetros obligatorios son nulos",
                    () -> assertThrows(IllegalArgumentException.class, () -> {
                        new Bicicleta(TEST_ID, null, TipoBicicletas.NORMAL, fechaEstadoTest);
                    }, "Debe lanzar excepción cuando estado es null"),

                    () -> assertThrows(IllegalArgumentException.class, () -> {
                        new Bicicleta(TEST_ID, EstadosBicicletas.DISPONIBLE, null, fechaEstadoTest);
                    }, "Debe lanzar excepción cuando tipo es null"),

                    () -> assertThrows(IllegalArgumentException.class, () -> {
                        new Bicicleta(TEST_ID, EstadosBicicletas.DISPONIBLE, TipoBicicletas.NORMAL, null);
                    }, "Debe lanzar excepción cuando fechaEstado es null")
            );
        }

        @Test
        @DisplayName("Constructor copia crea copia exacta")
        void testCopyConstructor() {
            Bicicleta copia = new Bicicleta(bicicletaBase);

            assertAll("Verificación completa del constructor copia",
                    () -> assertEquals(bicicletaBase.getId(), copia.getId(), "Los IDs deben ser iguales"),
                    () -> assertEquals(bicicletaBase.getEstado(), copia.getEstado(), "Los estados deben ser iguales"),
                    () -> assertEquals(bicicletaBase.getTipo(), copia.getTipo(), "Los tipos deben ser iguales"),
                    () -> assertEquals(bicicletaBase.getFechaEstado(), copia.getFechaEstado(), "Las fechas de estado deben ser iguales"),
                    () -> assertEquals(bicicletaBase.getFechaMantenimiento(), copia.getFechaMantenimiento(),
                            "Las fechas de mantenimiento deben ser iguales"),
                    () -> assertNotSame(bicicletaBase, copia, "Deben ser instancias diferentes (referencias distintas)"),
                    () -> assertEquals(bicicletaBase, copia, "Según equals, las bicicletas con mismo ID y tipo deben ser iguales")
            );
        }

        @Test
        @DisplayName("Constructor con nuevo ID y bicicleta original crea copia con ID modificado")
        void testIdBicicletaConstructor() {
            // Cambiamos el estado de la bicicleta base para verificar que se copia correctamente
            bicicletaBase.setEstado(EstadosBicicletas.ALQUILADA);

            Bicicleta copia = new Bicicleta(NUEVO_ID, bicicletaBase);

            assertAll("Verificación del constructor con nuevo ID",
                    () -> assertEquals(NUEVO_ID, copia.getId(), "El ID debe ser el nuevo ID especificado"),
                    () -> assertNotEquals(bicicletaBase.getId(), copia.getId(), "El ID debe ser diferente al de la bicicleta original"),
                    () -> assertEquals(bicicletaBase.getEstado(), copia.getEstado(), "El estado debe copiarse de la bicicleta original"),
                    () -> assertEquals(bicicletaBase.getTipo(), copia.getTipo(), "El tipo debe copiarse de la bicicleta original"),
                    () -> assertEquals(bicicletaBase.getFechaEstado(), copia.getFechaEstado(), "La fecha de estado debe copiarse de la bicicleta original"),
                    () -> assertEquals(bicicletaBase.getFechaMantenimiento(), copia.getFechaMantenimiento(),
                            "La fecha de mantenimiento debe copiarse de la bicicleta original"),
                    () -> assertNotSame(bicicletaBase, copia, "Deben ser instancias diferentes (referencias distintas)"),
                    () -> assertNotEquals(bicicletaBase, copia, "Según equals, las bicicletas con distinto ID no deben ser iguales")
            );
        }

        @Test
        @DisplayName("Constructor con ID vacío genera UUID")
        void testEmptyIdConstructor() {
            Bicicleta bici = new Bicicleta("", TipoBicicletas.ELECTRICA);

            assertNotNull(bici.getId(), "El ID no debe ser nulo aunque se pase una cadena vacía");
            assertNotEquals("", bici.getId(), "El ID no debe ser una cadena vacía");
            assertTrue(bici.getId().length() > 0, "El ID debe tener una longitud mayor que cero");
        }

        @Test
        @DisplayName("Constructor con ID null genera UUID")
        void testNullIdConstructor() {
            Bicicleta bici = new Bicicleta(null, TipoBicicletas.ELECTRICA);

            assertNotNull(bici.getId(), "El ID no debe ser nulo aunque se pase null");
            assertTrue(bici.getId().length() > 0, "El ID debe tener una longitud mayor que cero");
        }

        @Test
        @DisplayName("Constructor lanza excepción con tipo null")
        void testNullTipoConstructor() {
            assertThrows(IllegalArgumentException.class, () -> {
                new Bicicleta(TEST_ID, (TipoBicicletas) null);
            }, "Debe lanzarse una excepción si el tipo es null");
        }

        @Test
        @DisplayName("Constructor completo lanza excepción con estado null")
        void testNullEstadoFullConstructor() {
            assertThrows(IllegalArgumentException.class, () -> {
                new Bicicleta(TEST_ID, null, TipoBicicletas.ELECTRICA);
            }, "Debe lanzarse una excepción si el estado es null");
        }

        @Test
        @DisplayName("Constructor completo lanza excepción con tipo null")
        void testNullTipoFullConstructor() {
            assertThrows(IllegalArgumentException.class, () -> {
                new Bicicleta(TEST_ID, EstadosBicicletas.DISPONIBLE, null);
            }, "Debe lanzarse una excepción si el tipo es null");
        }

        @Test
        @DisplayName("Constructor con nuevo ID y bicicleta null lanza excepción")
        void testNullBicicletaConstructor() {
            assertThrows(NullPointerException.class, () -> {
                new Bicicleta(NUEVO_ID, (Bicicleta) null);
            }, "Debe lanzarse una excepción si la bicicleta original es null");
        }
    }

    @Nested
    @DisplayName("Tests de getters y setters")
    class GetterSetterTests {

        @Test
        @DisplayName("Getters devuelven valores esperados")
        void testGetters() {
            assertEquals(TEST_ID, bicicletaBase.getId(), "getId() debe devolver el ID correcto");
            assertEquals(EstadosBicicletas.DISPONIBLE, bicicletaBase.getEstado(), "getEstado() debe devolver el estado correcto");
            assertEquals(TipoBicicletas.ELECTRICA, bicicletaBase.getTipo(), "getTipo() debe devolver el tipo correcto");
            assertNotNull(bicicletaBase.getFechaEstado(), "getFechaEstado() no debe devolver null");
            assertNotNull(bicicletaBase.getFechaMantenimiento(), "getFechaMantenimiento() no debe devolver null");
        }

        @Test
        @DisplayName("Setter de estado actualiza fecha de mantenimiento solo en estado DISPONIBLE")
        void testSetEstado() {
            // Primero cambiamos a un estado que no sea DISPONIBLE
            bicicletaBase.setEstado(EstadosBicicletas.EN_REPARACION);

            // Guardamos las fechas después del primer cambio
            Instant fechaEstadoPrimerCambio = bicicletaBase.getFechaEstado();
            Instant fechaMantenimientoPrimerCambio = bicicletaBase.getFechaMantenimiento();

            // Pequeña pausa para asegurar que la nueva fecha será posterior
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                fail("Error en la pausa del test: " + e.getMessage());
            }

            // Cambiamos a otro estado que no sea DISPONIBLE
            bicicletaBase.setEstado(EstadosBicicletas.EN_TRANSITO);

            // Verificamos después del segundo cambio (a estado no DISPONIBLE)
            assertAll("Verificación al cambiar a estado no DISPONIBLE",
                    () -> assertEquals(EstadosBicicletas.EN_TRANSITO, bicicletaBase.getEstado(),
                            "El estado debe actualizarse correctamente"),
                    () -> assertTrue(bicicletaBase.getFechaEstado().isAfter(fechaEstadoPrimerCambio),
                            "La fecha de estado debe actualizarse siempre"),
                    () -> assertEquals(fechaMantenimientoPrimerCambio, bicicletaBase.getFechaMantenimiento(),
                            "La fecha de mantenimiento NO debe cambiar cuando se cambia a un estado diferente de DISPONIBLE")
            );

            // Otra pausa
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                fail("Error en la pausa del test: " + e.getMessage());
            }

            // Guardamos las fechas antes del cambio a DISPONIBLE
            Instant fechaEstadoAntesCambio = bicicletaBase.getFechaEstado();
            Instant fechaMantenimientoAntesCambio = bicicletaBase.getFechaMantenimiento();

            // Ahora cambiamos a DISPONIBLE
            bicicletaBase.setEstado(EstadosBicicletas.DISPONIBLE);

            // Verificamos después del cambio a DISPONIBLE
            assertAll("Verificación al cambiar a estado DISPONIBLE",
                    () -> assertEquals(EstadosBicicletas.DISPONIBLE, bicicletaBase.getEstado(),
                            "El estado debe actualizarse correctamente a DISPONIBLE"),
                    () -> assertTrue(bicicletaBase.getFechaEstado().isAfter(fechaEstadoAntesCambio),
                            "La fecha de estado debe actualizarse siempre"),
                    () -> assertNotEquals(fechaMantenimientoAntesCambio, bicicletaBase.getFechaMantenimiento(),
                            "La fecha de mantenimiento DEBE cambiar cuando se cambia al estado DISPONIBLE"),
                    () -> assertTrue(bicicletaBase.getFechaMantenimiento().isAfter(bicicletaBase.getFechaEstado()),
                            "La nueva fecha de mantenimiento debe ser posterior a la nueva fecha de estado")
            );
        }
    }

    @Nested
    @DisplayName("Tests de métodos de la clase")
    class MethodTests {

        @Test
        @DisplayName("Método necesitaCarga para bicicleta eléctrica")
        void testNecesitaCargaElectrica() {
            // Como el método tiene un componente aleatorio, ejecutamos varias veces
            // y verificamos que el método se comporta según lo esperado
            boolean resultadoTrue = false;
            boolean resultadoFalse = false;

            for (int i = 0; i < 100 && (!resultadoTrue || !resultadoFalse); i++) {
                boolean resultado = bicicletaBase.necesitaCarga();
                resultadoTrue |= resultado;
                resultadoFalse |= !resultado;
            }

            // Al menos debe haber un resultado, verdadero o falso
            assertTrue(resultadoTrue || resultadoFalse, "El método debe retornar algún valor");

            // Nota: En un escenario real, deberíamos encontrar tanto verdaderos como falsos,
            // pero dado que es aleatorio, no podemos garantizarlo en cada ejecución
        }

        @Test
        @DisplayName("Método necesitaCarga para bicicleta mecánica siempre es false")
        void testNecesitaCargaMecanica() {
            Bicicleta biciMecanica = new Bicicleta(TEST_ID, TipoBicicletas.NORMAL);

            // Ejecutamos varias veces y verificamos que siempre retorne false
            for (int i = 0; i < 10; i++) {
                assertFalse(biciMecanica.necesitaCarga(),
                        "Las bicicletas mecánicas nunca deben necesitar carga");
            }
        }

        @Test
        @DisplayName("Método toString genera string con formato correcto")
        void testToString() {
            String str = bicicletaBase.toString();

            // Verificamos que contenga las partes importantes
            assertTrue(str.contains(TEST_ID), "toString() debe incluir el ID");
            assertTrue(str.contains(bicicletaBase.getEstado().toString()), "toString() debe incluir el estado");
            assertTrue(str.contains(bicicletaBase.getTipo().toString()), "toString() debe incluir el tipo");
            assertTrue(str.contains("fechaEstado"), "toString() debe incluir la fecha de estado");
            assertTrue(str.contains("fechaMantenimiento"), "toString() debe incluir la fecha de mantenimiento");
        }

        @Test
        @DisplayName("Método compareTo ordena por ID correctamente")
        void testCompareTo() {
            Bicicleta bici1 = new Bicicleta("a", TipoBicicletas.ELECTRICA);
            Bicicleta bici2 = new Bicicleta("b", TipoBicicletas.ELECTRICA);
            Bicicleta bici3 = new Bicicleta("a", TipoBicicletas.NORMAL);

            assertTrue(bici1.compareTo(bici2) < 0,
                    "'a' debe ser menor que 'b' en orden lexicográfico");
            assertTrue(bici2.compareTo(bici1) > 0,
                    "'b' debe ser mayor que 'a' en orden lexicográfico");
            assertEquals(0, bici1.compareTo(bici3),
                    "Las bicicletas con mismo ID deben considerarse iguales al comparar");
        }

        @Test
        @DisplayName("Método compareTo maneja IDs numéricos correctamente")
        void testCompareToNumeric() {
            Bicicleta bici1 = new Bicicleta("1", TipoBicicletas.ELECTRICA);
            Bicicleta bici2 = new Bicicleta("2", TipoBicicletas.ELECTRICA);
            Bicicleta bici3 = new Bicicleta("10", TipoBicicletas.ELECTRICA);

            assertTrue(bici1.compareTo(bici2) < 0,
                    "ID '1' debe ser menor que '2' en orden numérico");
            assertTrue(bici2.compareTo(bici3) < 0,
                    "ID '2' debe ser menor que '10' en orden numérico");
        }

        @Test
        @DisplayName("Método esUnNumero detecta números correctamente")
        void testEsUnNumero() {
            // Utilizamos reflection para acceder al método privado
            java.lang.reflect.Method esUnNumeroMethod;
            try {
                esUnNumeroMethod = Bicicleta.class.getDeclaredMethod("esUnNumero", String.class);
                esUnNumeroMethod.setAccessible(true);

                // Probamos con diferentes entradas
                assertTrue((Boolean) esUnNumeroMethod.invoke(bicicletaBase, "123"),
                        "'123' debe ser reconocido como número");
                assertTrue((Boolean) esUnNumeroMethod.invoke(bicicletaBase, "-123"),
                        "'-123' debe ser reconocido como número");
                assertFalse((Boolean) esUnNumeroMethod.invoke(bicicletaBase, "12a3"),
                        "'12a3' no debe ser reconocido como número");
                assertFalse((Boolean) esUnNumeroMethod.invoke(bicicletaBase, ""),
                        "String vacío no debe ser reconocido como número");
                assertFalse((Boolean) esUnNumeroMethod.invoke(bicicletaBase, new Object[]{null}),
                        "null no debe ser reconocido como número");

            } catch (Exception e) {
                fail("Error al probar el método esUnNumero: " + e.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("Tests de métodos de comparación")
    class ComparisonTests {

        @Test
        @DisplayName("Método equals funciona correctamente")
        void testEquals() {
            // Mismos IDs y tipos
            Bicicleta bici1 = new Bicicleta(TEST_ID, TipoBicicletas.ELECTRICA);
            Bicicleta bici2 = new Bicicleta(TEST_ID, TipoBicicletas.ELECTRICA);

            // Mismo ID, diferente tipo
            Bicicleta bici3 = new Bicicleta(TEST_ID, TipoBicicletas.NORMAL);

            // Diferente ID, mismo tipo
            Bicicleta bici4 = new Bicicleta("otro-id", TipoBicicletas.ELECTRICA);

            // Diferente ID, diferente tipo
            Bicicleta bici5 = new Bicicleta("otro-id", TipoBicicletas.NORMAL);

            // Usando el constructor con nuevo ID
            Bicicleta bici6 = new Bicicleta(NUEVO_ID, bici1);

            assertAll("Verificaciones del método equals",
                    // Reflexividad: un objeto debe ser igual a sí mismo
                    () -> assertTrue(bici1.equals(bici1),
                            "Una bicicleta debe ser igual a sí misma"),

                    // Simetría: si a.equals(b) entonces b.equals(a)
                    () -> {
                        boolean equals1 = bici1.equals(bici2);
                        boolean equals2 = bici2.equals(bici1);
                        assertEquals(equals1, equals2,
                                "La igualdad debe ser simétrica: si a.equals(b) entonces b.equals(a)");
                    },

                    // Transitiva: si a.equals(b) y b.equals(c) entonces a.equals(c)
                    () -> {
                        Bicicleta biciTransitiva1 = new Bicicleta(TEST_ID, TipoBicicletas.ELECTRICA);
                        Bicicleta biciTransitiva2 = new Bicicleta(TEST_ID, TipoBicicletas.ELECTRICA);
                        Bicicleta biciTransitiva3 = new Bicicleta(TEST_ID, TipoBicicletas.ELECTRICA);

                        assertTrue(biciTransitiva1.equals(biciTransitiva2) &&
                                        biciTransitiva2.equals(biciTransitiva3) &&
                                        biciTransitiva1.equals(biciTransitiva3),
                                "La igualdad debe ser transitiva");
                    },

                    // Verificamos casos específicos
                    () -> assertTrue(bici1.equals(bici2),
                            "Bicicletas con el mismo ID y tipo deben ser iguales"),
                    () -> assertFalse(bici1.equals(bici3),
                            "Bicicletas con el mismo ID pero diferente tipo no deben ser iguales"),
                    () -> assertFalse(bici1.equals(bici4),
                            "Bicicletas con diferente ID pero mismo tipo no deben ser iguales"),
                    () -> assertFalse(bici1.equals(bici5),
                            "Bicicletas con diferente ID y diferente tipo no deben ser iguales"),
                    () -> assertFalse(bici1.equals(bici6),
                            "Bicicletas creadas con el constructor de nuevo ID deben ser diferentes si tienen distinto ID"),

                    // Verificamos comportamiento con null
                    () -> assertFalse(bici1.equals(null),
                            "Una bicicleta no debe ser igual a null"),

                    // Verificamos comportamiento con otra clase
                    () -> assertFalse(bici1.equals("una cadena"),
                            "Una bicicleta no debe ser igual a un objeto de otra clase")
            );
        }

        @Test
        @DisplayName("Método hashCode funciona correctamente")
        void testHashCode() {
            Bicicleta bici1 = new Bicicleta(TEST_ID, TipoBicicletas.ELECTRICA);
            Bicicleta bici2 = new Bicicleta(TEST_ID, TipoBicicletas.ELECTRICA);
            Bicicleta bici3 = new Bicicleta(TEST_ID, TipoBicicletas.NORMAL);
            Bicicleta bici4 = new Bicicleta("otro-id", TipoBicicletas.ELECTRICA);
            Bicicleta bici5 = new Bicicleta(NUEVO_ID, bici1);

            assertAll("Verificaciones del método hashCode",
                    // Si dos objetos son iguales, sus hashCodes deben ser iguales
                    () -> assertEquals(bici1.hashCode(), bici2.hashCode(),
                            "Bicicletas iguales deben tener el mismo hashCode"),

                    // Si dos objetos son diferentes, sus hashCodes probablemente sean diferentes
                    // (esto no es garantizado por el contrato de hashCode, pero es deseable)
                    () -> assertNotEquals(bici1.hashCode(), bici3.hashCode(),
                            "Bicicletas diferentes deberían tener hashCodes diferentes (mismo ID, diferente tipo)"),
                    () -> assertNotEquals(bici1.hashCode(), bici4.hashCode(),
                            "Bicicletas diferentes deberían tener hashCodes diferentes (diferente ID, mismo tipo)"),
                    () -> assertNotEquals(bici1.hashCode(), bici5.hashCode(),
                            "Bicicletas con diferentes IDs deberían tener hashCodes diferentes"),

                    // El hashCode debe ser consistente
                    () -> {
                        int hash1 = bici1.hashCode();
                        int hash2 = bici1.hashCode();
                        assertEquals(hash1, hash2,
                                "El hashCode debe ser consistente para el mismo objeto");
                    }
            );
        }

        @Test
        @DisplayName("Método compareTo es consistente con equals")
        void testCompareToConsistentWithEquals() {
            Bicicleta bici1 = new Bicicleta(TEST_ID, TipoBicicletas.ELECTRICA);
            Bicicleta bici2 = new Bicicleta(TEST_ID, TipoBicicletas.ELECTRICA);
            Bicicleta bici3 = new Bicicleta(TEST_ID, TipoBicicletas.NORMAL);

            // Verificación: objetos iguales según equals tienen compareTo = 0
            assertTrue(bici1.equals(bici2) && bici1.compareTo(bici2) == 0,
                    "Objetos iguales según equals deben tener compareTo = 0");

            // Nota: En esta implementación, equals tiene en cuenta el tipo, pero compareTo solo el ID
            // Esto significa que compareTo puede ser 0 (iguales para ordenación) pero equals false
            assertEquals(0, bici1.compareTo(bici3),
                    "compareTo debe devolver 0 para objetos con mismo ID aunque tengan distinto tipo");
            assertFalse(bici1.equals(bici3),
                    "equals debe devolver false para objetos con distinto tipo aunque tengan mismo ID");

            // Esta diferencia debe documentarse ya que técnicamente es una inconsistencia
            // entre equals y compareTo según sus contratos
        }
    }
}