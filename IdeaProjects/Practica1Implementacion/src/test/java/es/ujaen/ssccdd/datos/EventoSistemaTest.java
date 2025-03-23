package es.ujaen.ssccdd.datos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static es.ujaen.ssccdd.Constantes.TipoEvento;

class EventoSistemaTest {

    // Utilizamos el primer valor del enumerado para las pruebas
    private static final TipoEvento TIPO_TEST = TipoEvento.values()[0];

    @Test
    @DisplayName("Verificación del constructor completo y funcionamiento de getters")
    void testConstructorCompletoYGetters() {
        // Preparación
        String origen = "usuario1";
        String destino = "estacion1";
        String idBicicleta = "bici1";
        String detalles = "Detalles del evento";

        // Ejecución
        EventoSistema evento = new EventoSistema(TIPO_TEST, origen, destino, idBicicleta, detalles);
        Instant ahora = Instant.now();

        // Verificación
        assertAll("El evento debe almacenar correctamente todos los valores proporcionados",
                () -> assertNotNull(evento.getId(), "El ID no debe ser null"),
                () -> assertEquals(TIPO_TEST, evento.getTipo(), "El tipo debe coincidir con el proporcionado"),
                () -> assertTrue(ChronoUnit.SECONDS.between(evento.getTimestamp(), ahora) < 2,
                        "El timestamp debe ser cercano al momento de creación"),
                () -> assertEquals(origen, evento.getOrigen(), "El origen debe coincidir con el proporcionado"),
                () -> assertEquals(destino, evento.getDestino(), "El destino debe coincidir con el proporcionado"),
                () -> assertEquals(idBicicleta, evento.getIdBicicleta(), "El ID de bicicleta debe coincidir con el proporcionado"),
                () -> assertEquals(detalles, evento.getDetalles(), "Los detalles deben coincidir con los proporcionados")
        );
    }

    @Test
    @DisplayName("Verificación del constructor simplificado (sin destino ni bicicleta)")
    void testConstructorSimplificado() {
        // Preparación
        String origen = "usuario1";
        String detalles = "Detalles del evento";

        // Ejecución
        EventoSistema evento = new EventoSistema(TIPO_TEST, origen, detalles);

        // Verificación
        assertAll("El constructor simplificado debe establecer correctamente los campos obligatorios y dejar los opcionales a null",
                () -> assertNotNull(evento.getId(), "El ID no debe ser null"),
                () -> assertEquals(TIPO_TEST, evento.getTipo(), "El tipo debe coincidir con el proporcionado"),
                () -> assertEquals(origen, evento.getOrigen(), "El origen debe coincidir con el proporcionado"),
                () -> assertNull(evento.getDestino(), "El destino debe ser null al no proporcionarse"),
                () -> assertNull(evento.getIdBicicleta(), "El ID de bicicleta debe ser null al no proporcionarse"),
                () -> assertEquals(detalles, evento.getDetalles(), "Los detalles deben coincidir con los proporcionados")
        );
    }

    @Test
    @DisplayName("Verificación del constructor con bicicleta pero sin destino")
    void testConstructorConBicicleta() {
        // Preparación
        String origen = "usuario1";
        String idBicicleta = "bici1";
        String detalles = "Detalles del evento";

        // Ejecución
        EventoSistema evento = new EventoSistema(TIPO_TEST, origen, idBicicleta, detalles);

        // Verificación
        assertAll("El constructor debe establecer correctamente los campos proporcionados y dejar el destino a null",
                () -> assertNotNull(evento.getId(), "El ID no debe ser null"),
                () -> assertEquals(TIPO_TEST, evento.getTipo(), "El tipo debe coincidir con el proporcionado"),
                () -> assertEquals(origen, evento.getOrigen(), "El origen debe coincidir con el proporcionado"),
                () -> assertNull(evento.getDestino(), "El destino debe ser null al no proporcionarse"),
                () -> assertEquals(idBicicleta, evento.getIdBicicleta(), "El ID de bicicleta debe coincidir con el proporcionado"),
                () -> assertEquals(detalles, evento.getDetalles(), "Los detalles deben coincidir con los proporcionados")
        );
    }

    @Test
    @DisplayName("Verificación del método formatearEvento con todos los campos")
    void testFormatearEventoCompleto() {
        // Preparación
        String origen = "usuario1";
        String destino = "estacion1";
        String idBicicleta = "bici1";
        String detalles = "Detalles del evento";
        EventoSistema evento = new EventoSistema(TIPO_TEST, origen, destino, idBicicleta, detalles);

        // Ejecución
        String resultado = evento.formatearEvento();

        // Verificación
        assertAll("El formato debe incluir correctamente todos los campos del evento",
                () -> assertTrue(resultado.contains(evento.getTimestamp().toString()),
                        "Debe incluir el timestamp correcto"),
                () -> assertTrue(resultado.contains(String.valueOf(TIPO_TEST.getCodigo())),
                        "Debe incluir el código del tipo de evento"),
                () -> assertTrue(resultado.contains(TIPO_TEST.getDescripcion()),
                        "Debe incluir la descripción del tipo de evento"),
                () -> assertTrue(resultado.contains("Origen: " + origen),
                        "Debe incluir el origen correctamente formateado"),
                () -> assertTrue(resultado.contains("Destino: " + destino),
                        "Debe incluir el destino correctamente formateado"),
                () -> assertTrue(resultado.contains("Bicicleta: " + idBicicleta),
                        "Debe incluir el ID de bicicleta correctamente formateado"),
                () -> assertTrue(resultado.contains(detalles),
                        "Debe incluir los detalles del evento")
        );
    }

    @Test
    @DisplayName("Verificación del método formatearEvento con campos opcionales null")
    void testFormatearEventoConNulls() {
        // Preparación
        String origen = "usuario1";
        EventoSistema evento = new EventoSistema(TIPO_TEST, origen, null, null, null);

        // Ejecución
        String resultado = evento.formatearEvento();

        // Verificación
        assertAll("El formato debe incluir solo los campos no nulos y omitir los nulos",
                () -> assertTrue(resultado.contains(evento.getTimestamp().toString()),
                        "Debe incluir el timestamp correcto"),
                () -> assertTrue(resultado.contains(String.valueOf(TIPO_TEST.getCodigo())),
                        "Debe incluir el código del tipo de evento"),
                () -> assertTrue(resultado.contains(TIPO_TEST.getDescripcion()),
                        "Debe incluir la descripción del tipo de evento"),
                () -> assertTrue(resultado.contains("Origen: " + origen),
                        "Debe incluir el origen correctamente formateado"),
                () -> assertFalse(resultado.contains("Destino:"),
                        "No debe incluir el campo destino cuando es null"),
                () -> assertFalse(resultado.contains("Bicicleta:"),
                        "No debe incluir el campo bicicleta cuando es null"),
                () -> assertFalse(resultado.contains(" - null"),
                        "No debe incluir 'null' como detalles")
        );
    }

    @Test
    @DisplayName("Verificación del método formatearEvento con campos opcionales vacíos")
    void testFormatearEventoConVacios() {
        // Preparación
        String origen = "usuario1";
        EventoSistema evento = new EventoSistema(TIPO_TEST, origen, "", "", "");

        // Ejecución
        String resultado = evento.formatearEvento();

        // Verificación
        assertAll("El formato debe omitir los campos vacíos igual que los nulos",
                () -> assertTrue(resultado.contains(evento.getTimestamp().toString()),
                        "Debe incluir el timestamp correcto"),
                () -> assertTrue(resultado.contains(String.valueOf(TIPO_TEST.getCodigo())),
                        "Debe incluir el código del tipo de evento"),
                () -> assertTrue(resultado.contains(TIPO_TEST.getDescripcion()),
                        "Debe incluir la descripción del tipo de evento"),
                () -> assertTrue(resultado.contains("Origen: " + origen),
                        "Debe incluir el origen correctamente formateado"),
                () -> assertFalse(resultado.contains("Destino:"),
                        "No debe incluir el campo destino cuando está vacío"),
                () -> assertFalse(resultado.contains("Bicicleta:"),
                        "No debe incluir el campo bicicleta cuando está vacío"),
                () -> assertFalse(resultado.endsWith(" - "),
                        "No debe terminar con el separador de detalles cuando están vacíos")
        );
    }

    @Test
    @DisplayName("Verificación de los métodos equals y hashCode")
    void testEqualsYHashCode() {
        // Preparación
        EventoSistema evento1 = new EventoSistema(TIPO_TEST, "origen1", "detalle");
        EventoSistema evento2 = new EventoSistema(TIPO_TEST, "origen1", "detalle");
        EventoSistema copiaEvento1 = evento1; // Misma referencia

        // Verificación
        assertAll("Los métodos equals y hashCode deben comportarse según el contrato especificado",
                () -> assertNotEquals(evento1, evento2, "Eventos con distintas instancias no deben ser iguales (IDs diferentes)"),
                () -> assertEquals(evento1, copiaEvento1, "El mismo evento debe ser igual a sí mismo"),
                () -> assertNotEquals(evento1.hashCode(), evento2.hashCode(),
                        "Hash codes de eventos diferentes no deben ser iguales"),
                () -> assertEquals(evento1.hashCode(), copiaEvento1.hashCode(),
                        "Hash codes del mismo evento deben ser iguales"),
                () -> assertNotEquals(evento1, null, "Un evento no debe ser igual a null"),
                () -> assertNotEquals(evento1, "NotAnEvent", "Un evento no debe ser igual a un objeto de otro tipo")
        );
    }

    @Test
    @DisplayName("Verificación de excepciones para parámetros requeridos null")
    void testParametrosRequeridosNull() {
        assertAll("Debe lanzar NullPointerException para los parámetros requeridos cuando son null",
                () -> assertThrows(NullPointerException.class,
                        () -> new EventoSistema(null, "origen", "detalle"),
                        "Debe lanzar excepción cuando tipo es null"),
                () -> assertThrows(NullPointerException.class,
                        () -> new EventoSistema(TIPO_TEST, null, "detalle"),
                        "Debe lanzar excepción cuando origen es null")
        );
    }

    @Test
    @DisplayName("Verificación con valores extremos para campos de texto")
    void testValoresExtremos() {
        // Preparación
        String textoLargo = "a".repeat(10000); // Texto muy largo
        String textoEspecial = "!@#$%^&*()_+{}[]|\\:;\"'<>,.?/~`"; // Caracteres especiales

        // Ejecución
        EventoSistema eventoTextoLargo = new EventoSistema(TIPO_TEST, "origen", textoLargo);
        EventoSistema eventoTextoEspecial = new EventoSistema(TIPO_TEST, "origen", textoEspecial);

        // Verificación
        assertAll("Debe manejar correctamente valores extremos en los campos de texto",
                () -> assertEquals(textoLargo, eventoTextoLargo.getDetalles(),
                        "Debe almacenar correctamente textos muy largos"),
                () -> assertEquals(textoEspecial, eventoTextoEspecial.getDetalles(),
                        "Debe almacenar correctamente textos con caracteres especiales"),
                () -> assertTrue(eventoTextoLargo.formatearEvento().contains(textoLargo),
                        "El formato debe incluir correctamente textos muy largos"),
                () -> assertTrue(eventoTextoEspecial.formatearEvento().contains(textoEspecial),
                        "El formato debe incluir correctamente textos con caracteres especiales")
        );
    }

    @Test
    @DisplayName("Verificación del comportamiento del método toString")
    void testToString() {
        // Preparación
        EventoSistema evento = new EventoSistema(TIPO_TEST, "origen", "detalle");

        // Ejecución
        String resultadoToString = evento.toString();
        String resultadoFormatearEvento = evento.formatearEvento();

        // Verificación
        assertEquals(resultadoFormatearEvento, resultadoToString,
                "El método toString debe devolver el mismo resultado que formatearEvento");
    }
}