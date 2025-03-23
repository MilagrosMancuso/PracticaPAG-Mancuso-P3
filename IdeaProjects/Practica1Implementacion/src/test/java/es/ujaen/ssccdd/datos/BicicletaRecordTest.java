package es.ujaen.ssccdd.datos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static es.ujaen.ssccdd.Constantes.EstadosBicicletas;
import static es.ujaen.ssccdd.Constantes.TipoBicicletas;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests para BicicletaRecord")
class BicicletaRecordTest {

    @Nested
    @DisplayName("Tests de constructores")
    class ConstructorTests {

        @Test
        @DisplayName("Constructor canónico inicializa campos correctamente")
        void testCanonicalConstructor() {
            // Preparación de los datos de prueba
            String id = "test-id";
            TipoBicicletas tipo = TipoBicicletas.ELECTRICA;
            EstadosBicicletas estado = EstadosBicicletas.DISPONIBLE;
            Instant fechaAlquiler = Instant.now();

            // Ejecución del código a probar
            BicicletaRecord record = new BicicletaRecord(id, tipo, estado, fechaAlquiler);

            // Verificación de resultados
            assertAll("Los campos deben inicializarse correctamente",
                    () -> assertEquals(id, record.id(), "El ID debe ser el especificado"),
                    () -> assertEquals(tipo, record.tipo(), "El tipo debe ser el especificado"),
                    () -> assertEquals(estado, record.estado(), "El estado debe ser el especificado"),
                    () -> assertEquals(fechaAlquiler, record.fechaAlquiler(), "La fecha de alquiler debe ser la especificada")
            );
        }

        @Test
        @DisplayName("Constructor compacto crea correctamente desde Bicicleta")
        void testCompactConstructor() {
            // Preparación de los datos de prueba
            String id = "test-id";
            TipoBicicletas tipo = TipoBicicletas.NORMAL;
            EstadosBicicletas estado = EstadosBicicletas.ALQUILADA;

            Bicicleta bicicleta = new Bicicleta(id, estado, tipo);

            // Ejecución del código a probar
            BicicletaRecord record = new BicicletaRecord(bicicleta);

            // Verificación de resultados
            assertAll("El record debe contener los valores de la bicicleta",
                    () -> assertEquals(id, record.id(), "El ID debe coincidir con el de la bicicleta"),
                    () -> assertEquals(tipo, record.tipo(), "El tipo debe coincidir con el de la bicicleta"),
                    () -> assertEquals(estado, record.estado(), "El estado debe coincidir con el de la bicicleta"),
                    () -> assertNotNull(record.fechaAlquiler(), "La fecha de alquiler no debe ser nula")
            );
        }

        @Test
        @DisplayName("Constructor compacto establece fechaAlquiler a la hora actual")
        void testCompactConstructorSetsCurrentTime() {
            // Preparación y ejecución
            Instant before = Instant.now();
            BicicletaRecord record = new BicicletaRecord(new Bicicleta("id", TipoBicicletas.ELECTRICA));
            Instant after = Instant.now();

            // Verificación
            assertNotNull(record.fechaAlquiler(), "La fecha de alquiler no debe ser nula");

            // Comprobamos que la fecha está dentro de un rango razonable (100ms)
            assertTrue(
                    !record.fechaAlquiler().isBefore(before.minus(100, ChronoUnit.MILLIS)) &&
                            !record.fechaAlquiler().isAfter(after.plus(100, ChronoUnit.MILLIS)),
                    "La fecha de alquiler debe estar cerca del momento actual"
            );
        }

        @Test
        @DisplayName("Constructor maneja correctamente diferentes valores")
        void testConstructorWithDifferentValues() {
            // Prueba con diferentes IDs
            assertEquals("id1", new BicicletaRecord("id1", TipoBicicletas.ELECTRICA, EstadosBicicletas.DISPONIBLE, Instant.now()).id());
            assertEquals("", new BicicletaRecord("", TipoBicicletas.ELECTRICA, EstadosBicicletas.DISPONIBLE, Instant.now()).id());

            // Prueba con diferentes tipos
            assertEquals(TipoBicicletas.ELECTRICA, new BicicletaRecord("id", TipoBicicletas.ELECTRICA, EstadosBicicletas.DISPONIBLE, Instant.now()).tipo());
            assertEquals(TipoBicicletas.NORMAL, new BicicletaRecord("id", TipoBicicletas.NORMAL, EstadosBicicletas.DISPONIBLE, Instant.now()).tipo());

            // Prueba con diferentes estados
            assertEquals(EstadosBicicletas.DISPONIBLE, new BicicletaRecord("id", TipoBicicletas.ELECTRICA, EstadosBicicletas.DISPONIBLE, Instant.now()).estado());
            assertEquals(EstadosBicicletas.ALQUILADA, new BicicletaRecord("id", TipoBicicletas.ELECTRICA, EstadosBicicletas.ALQUILADA, Instant.now()).estado());
            assertEquals(EstadosBicicletas.EN_REPARACION, new BicicletaRecord("id", TipoBicicletas.ELECTRICA, EstadosBicicletas.EN_REPARACION, Instant.now()).estado());
        }

        @Test
        @DisplayName("Constructor lanza excepción con ID nulo")
        void testNullId() {
            // Verificación de que se lance la excepción esperada
            NullPointerException exception = assertThrows(NullPointerException.class, () -> {
                new BicicletaRecord(null, TipoBicicletas.ELECTRICA, EstadosBicicletas.DISPONIBLE, Instant.now());
            }, "Debe lanzar NullPointerException cuando el ID es nulo");

            // Verificar el mensaje de la excepción
            assertTrue(exception.getMessage().contains("El ID no puede ser nulo"),
                    "El mensaje de error debe mencionar el ID");
        }

        @Test
        @DisplayName("Constructor lanza excepción con tipo nulo")
        void testNullTipo() {
            NullPointerException exception = assertThrows(NullPointerException.class, () -> {
                new BicicletaRecord("id", null, EstadosBicicletas.DISPONIBLE, Instant.now());
            }, "Debe lanzar NullPointerException cuando el tipo es nulo");

            assertTrue(exception.getMessage().contains("El tipo no puede ser nulo"),
                    "El mensaje de error debe mencionar el tipo");
        }

        @Test
        @DisplayName("Constructor lanza excepción con estado nulo")
        void testNullEstado() {
            NullPointerException exception = assertThrows(NullPointerException.class, () -> {
                new BicicletaRecord("id", TipoBicicletas.ELECTRICA, null, Instant.now());
            }, "Debe lanzar NullPointerException cuando el estado es nulo");

            assertTrue(exception.getMessage().contains("El estado no puede ser nulo"),
                    "El mensaje de error debe mencionar el estado");
        }
    }

    @Nested
    @DisplayName("Tests de métodos")
    class MethodTests {

        @Test
        @DisplayName("Método esElectrica devuelve true para bicicletas eléctricas")
        void testEsElectricaForElectricBike() {
            // Preparación y ejecución
            BicicletaRecord record = new BicicletaRecord("id", TipoBicicletas.ELECTRICA, EstadosBicicletas.DISPONIBLE, Instant.now());

            // Verificación
            assertTrue(record.esElectrica(), "esElectrica() debe devolver true para bicicletas eléctricas");
        }

        @Test
        @DisplayName("Método esElectrica devuelve false para bicicletas normales")
        void testEsElectricaForNormalBike() {
            // Preparación y ejecución
            BicicletaRecord record = new BicicletaRecord("id", TipoBicicletas.NORMAL, EstadosBicicletas.DISPONIBLE, Instant.now());

            // Verificación
            assertFalse(record.esElectrica(), "esElectrica() debe devolver false para bicicletas normales");
        }
    }

    @Nested
    @DisplayName("Tests de características de record")
    class RecordFeaturesTests {

        @Test
        @DisplayName("Records iguales tienen mismo equals y hashCode")
        void testEqualsAndHashCode() {
            // Preparación
            Instant now = Instant.now();
            BicicletaRecord record1 = new BicicletaRecord("id", TipoBicicletas.ELECTRICA, EstadosBicicletas.DISPONIBLE, now);
            BicicletaRecord record2 = new BicicletaRecord("id", TipoBicicletas.ELECTRICA, EstadosBicicletas.DISPONIBLE, now);
            BicicletaRecord record3 = new BicicletaRecord("other-id", TipoBicicletas.ELECTRICA, EstadosBicicletas.DISPONIBLE, now);

            // Verificación de equals
            assertAll("Verificación de equals y hashCode",
                    () -> assertEquals(record1, record2, "Records con mismos valores deben ser iguales"),
                    () -> assertEquals(record1.hashCode(), record2.hashCode(), "Records iguales deben tener mismo hashCode"),
                    () -> assertNotEquals(record1, record3, "Records con diferentes valores deben ser diferentes"),
                    () -> assertNotEquals(record1.hashCode(), record3.hashCode(), "Records diferentes deberían tener hashCodes diferentes")
            );
        }

        @Test
        @DisplayName("toString contiene información relevante")
        void testToString() {
            // Preparación
            BicicletaRecord record = new BicicletaRecord("id-test", TipoBicicletas.ELECTRICA, EstadosBicicletas.DISPONIBLE, Instant.now());
            String representacion = record.toString();

            // Verificación
            assertAll("toString debe incluir información importante",
                    () -> assertTrue(representacion.contains("id-test"), "toString() debe incluir el ID"),
                    () -> assertTrue(representacion.contains("ELECTRICA"), "toString() debe incluir el tipo"),
                    () -> assertTrue(representacion.contains("DISPONIBLE"), "toString() debe incluir el estado"),
                    () -> assertTrue(representacion.contains("fechaAlquiler"), "toString() debe mencionar la fecha de alquiler")
            );
        }

        @Test
        @DisplayName("Accesores devuelven valores correctos")
        void testAccessors() {
            // Preparación
            String id = "test-id";
            TipoBicicletas tipo = TipoBicicletas.ELECTRICA;
            EstadosBicicletas estado = EstadosBicicletas.DISPONIBLE;
            Instant fechaAlquiler = Instant.now();

            BicicletaRecord record = new BicicletaRecord(id, tipo, estado, fechaAlquiler);

            // Verificación - probar los métodos generados automáticamente por el record
            assertAll("Los accesores deben devolver los valores correctos",
                    () -> assertEquals(id, record.id(), "id() debe devolver el ID correcto"),
                    () -> assertEquals(tipo, record.tipo(), "tipo() debe devolver el tipo correcto"),
                    () -> assertEquals(estado, record.estado(), "estado() debe devolver el estado correcto"),
                    () -> assertEquals(fechaAlquiler, record.fechaAlquiler(), "fechaAlquiler() debe devolver la fecha correcta")
            );
        }
    }
}