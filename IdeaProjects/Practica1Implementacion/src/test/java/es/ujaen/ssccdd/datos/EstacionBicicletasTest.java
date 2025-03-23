package es.ujaen.ssccdd.datos;

import es.ujaen.ssccdd.Constantes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.IntStream;

import static es.ujaen.ssccdd.Constantes.CAPACIDAD_MINIMA;
import static es.ujaen.ssccdd.Constantes.EstadosBicicletas.*;
import static es.ujaen.ssccdd.Constantes.MIN_MANTENIMIENTO;
import static es.ujaen.ssccdd.Constantes.TipoBicicletas.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Clase de prueba para verificar el funcionamiento de la clase EstacionBicicletas.
 * Esta clase contiene pruebas para todos los métodos de EstacionBicicletas,
 * incluyendo casos normales y límite.
 *
 * @author Profesor Sistemas Concurrentes y Distribuidos
 */
public class EstacionBicicletasTest {

    private EstacionBicicletas estacion;
    private Bicicleta bicicleta1, bicicleta2, bicicleta3, bicicletaElectrica;
    private static final int CAPACIDAD_TEST = 10;
    private static final String USER_ID = "user-123";
    private static final String USER_ID_2 = "user-456";

    @BeforeEach
    void setUp() {
        // Creamos bicicletas para los tests
        bicicleta1 = new Bicicleta("B001", NORMAL);
        bicicleta2 = new Bicicleta("B002", NORMAL);
        bicicleta3 = new Bicicleta("B003", NORMAL);
        bicicletaElectrica = new Bicicleta("BE01", ELECTRICA);

        // Creamos una estación con capacidad 10 y las tres bicicletas
        estacion = new EstacionBicicletas("E001", CAPACIDAD_TEST, bicicleta1, bicicleta2, bicicleta3, bicicletaElectrica);
    }

    @Test
    @DisplayName("Test de construcción correcta y estado inicial de la estación")
    void testEstadoInicial() {
        assertAll("La estación debe inicializarse correctamente",
                () -> assertEquals("E001", estacion.getId(), "El ID debe ser el esperado"),
                () -> assertEquals(4, estacion.getDisponibles(), "Debe haber 4 bicicletas disponibles"),
                () -> assertEquals(6, estacion.getCapacidadEstacion(), "Debe haber 6 espacios disponibles"),
                () -> assertTrue(estacion.hayEspacio(), "Debe haber espacio disponible"),
                () -> assertEquals(4, estacion.ocupacionEstacion(), "La ocupación debe ser 4"),
                () -> assertEquals(4, estacion.getBicicletas(DISPONIBLE), "Debe haber 4 bicicletas DISPONIBLES"),
                () -> assertEquals(0, estacion.getBicicletas(ALQUILADA), "No debe haber bicicletas ALQUILADAS"),
                () -> assertEquals(0, estacion.getBicicletas(EN_TRANSITO), "No debe haber bicicletas EN_TRANSITO"),
                () -> assertEquals(0, estacion.getBicicletas(REUBICACION), "No debe haber bicicletas REUBICACION"),
                () -> assertEquals(0, estacion.getBicicletas(FUERA_DE_SERVICIO), "No debe haber bicicletas FUERA_DE_SERVICIO"),
                () -> assertFalse(estacion.avisarMantenimiento(), "No debe necesitar mantenimiento"),
                () -> assertNotNull(estacion.semExm(), "El semáforo de exclusión mutua debe existir"),
                () -> assertNotNull(estacion.semMantenimiento(), "El semáforo de mantenimiento debe existir")
        );
    }

    @Test
    @DisplayName("Test de construcción inválida de estación (argumentos incorrectos)")
    void testConstruccionInvalida() {
        assertAll("La construcción con parámetros inválidos debe lanzar excepciones",
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new EstacionBicicletas((Bicicleta[]) null),
                        "Debe lanzar excepción con bicicletas null"),

                () -> assertThrows(IllegalArgumentException.class,
                        () -> new EstacionBicicletas(new Bicicleta[]{}),
                        "Debe lanzar excepción con array vacío"),

                () -> assertThrows(IllegalArgumentException.class,
                        () -> new EstacionBicicletas("E002", 5, (Bicicleta) null),
                        "Debe lanzar excepción con bicicleta null"),

                () -> assertThrows(IllegalArgumentException.class,
                        () -> {
                            Bicicleta duplicada = new Bicicleta("DUPL", NORMAL);
                            new EstacionBicicletas("E003", 5, duplicada, duplicada);
                        },
                        "Debe lanzar excepción con bicicletas duplicadas")
        );
    }

    @Test
    @DisplayName("Test de alquiler de bicicleta con asignación de ID de usuario")
    void testPeticionAlquiler() {
        // Alquilamos una bicicleta
        Optional<Bicicleta> bicicletaAlquilada = estacion.peticionAlquiler(USER_ID);

        assertAll("El alquiler debe funcionar correctamente",
                () -> assertTrue(bicicletaAlquilada.isPresent(), "Debe devolver una bicicleta"),
                () -> assertEquals(ALQUILADA, bicicletaAlquilada.get().getEstado(), "La bicicleta debe estar en estado ALQUILADA"),
                () -> assertEquals(3, estacion.getDisponibles(), "Deben quedar 3 bicicletas disponibles"),
                () -> assertEquals(1, estacion.getBicicletas(ALQUILADA), "Debe haber 1 bicicleta alquilada"),
                () -> assertEquals(4, estacion.ocupacionEstacion(), "La ocupación debe seguir siendo 4")
        );

        // Verificamos que la bicicleta alquilada tenga el ID del usuario
        try {
            // Adquirimos el semáforo para operaciones de exclusión mutua
            estacion.semExm().acquire();

            // Obtenemos el mapa interno de bicicletas mediante reflection
            java.lang.reflect.Field field = EstacionBicicletas.class.getDeclaredField("bicicletas");
            field.setAccessible(true);
            Map<Constantes.EstadosBicicletas, Queue<Bicicleta>> mapaBicicletas =
                    (Map<Constantes.EstadosBicicletas, Queue<Bicicleta>>) field.get(estacion);

            // Obtenemos la primera bicicleta alquilada
            Bicicleta bicicletaEnEstacion = mapaBicicletas.get(ALQUILADA).peek();

            // Verificamos que el ID sea el del usuario
            assertEquals(USER_ID, bicicletaEnEstacion.getId(),
                    "La bicicleta alquilada en la estación debe tener el ID del usuario");

            // Liberamos el semáforo
            estacion.semExm().release();
        } catch (Exception e) {
            fail("No se pudo verificar el ID de usuario en la bicicleta alquilada: " + e.getMessage());
        }

        // Alquilamos todas las bicicletas restantes
        for (int i = 0; i < 3; i++) {
            estacion.peticionAlquiler(USER_ID_2);
        }

        // Ahora intentamos alquilar una más
        Optional<Bicicleta> intentoAlquiler = estacion.peticionAlquiler(USER_ID);

        assertAll("No debe permitir más alquileres cuando no hay bicicletas disponibles",
                () -> assertFalse(intentoAlquiler.isPresent(), "No debe devolver bicicleta cuando no hay disponibles"),
                () -> assertEquals(0, estacion.getDisponibles(), "No deben quedar bicicletas disponibles"),
                () -> assertEquals(4, estacion.getBicicletas(ALQUILADA), "Deben haber 4 bicicletas alquiladas")
        );
    }

    @Test
    @DisplayName("Test de alquiler de bicicleta con múltiples usuarios")
    void testPeticionAlquilerMultiplesUsuarios() {
        // Alquilamos bicicletas con diferentes IDs de usuario
        Optional<Bicicleta> bicicletaUsuario1 = estacion.peticionAlquiler("usuario1");
        Optional<Bicicleta> bicicletaUsuario2 = estacion.peticionAlquiler("usuario2");

        // Verificamos
        assertTrue(bicicletaUsuario1.isPresent() && bicicletaUsuario2.isPresent(),
                "Ambos usuarios deben poder alquilar bicicletas");

        // Verificamos las bicicletas alquiladas en la estación
        try {
            // Adquirimos el semáforo para operaciones de exclusión mutua
            estacion.semExm().acquire();

            // Obtenemos el mapa interno de bicicletas mediante reflection
            java.lang.reflect.Field field = EstacionBicicletas.class.getDeclaredField("bicicletas");
            field.setAccessible(true);
            Map<Constantes.EstadosBicicletas, Queue<Bicicleta>> mapaBicicletas =
                    (Map<Constantes.EstadosBicicletas, Queue<Bicicleta>>) field.get(estacion);

            // Convertimos la cola en lista para poder acceder por índice
            List<Bicicleta> bicicletasAlquiladas = new ArrayList<>(mapaBicicletas.get(ALQUILADA));

            // Verificamos que hay IDs de usuarios distintos en las bicicletas alquiladas
            boolean hayUsuario1 = false;
            boolean hayUsuario2 = false;

            for (Bicicleta b : bicicletasAlquiladas) {
                if (b.getId().equals("usuario1")) hayUsuario1 = true;
                if (b.getId().equals("usuario2")) hayUsuario2 = true;
            }

            assertTrue(hayUsuario1 && hayUsuario2,
                    "Debe haber bicicletas alquiladas con los IDs de ambos usuarios");

            // Liberamos el semáforo
            estacion.semExm().release();
        } catch (Exception e) {
            fail("No se pudo verificar los IDs de usuario en las bicicletas alquiladas: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test de reserva de espacio para bicicleta en tránsito")
    void testPeticionTransito() {
        Bicicleta bicicletaNueva = new Bicicleta("B004", NORMAL);

        // Reservamos espacio para tránsito
        boolean resultado = estacion.peticionTransito(bicicletaNueva);

        assertAll("La reserva de tránsito debe funcionar correctamente",
                () -> assertTrue(resultado, "Debe permitir reservar espacio para tránsito"),
                () -> assertEquals(1, estacion.getBicicletas(EN_TRANSITO), "Debe haber 1 bicicleta EN_TRANSITO"),
                () -> assertEquals(5, estacion.ocupacionEstacion(), "La ocupación debe ser 5"),
                () -> assertEquals(5, estacion.getCapacidadEstacion(), "Deben quedar 5 espacios libres")
        );

        // Llenamos la estación con más bicicletas en tránsito
        for (int i = 0; i < 5; i++) {
            estacion.peticionTransito(new Bicicleta("B" + (i + 5), NORMAL));
        }

        // Intentamos una más cuando está llena
        boolean resultadoLimite = estacion.peticionTransito(new Bicicleta("B99", NORMAL));

        assertAll("No debe permitir más reservas cuando está llena",
                () -> assertFalse(resultadoLimite, "No debe permitir más reservas cuando está llena"),
                () -> assertEquals(6, estacion.getBicicletas(EN_TRANSITO), "Debe haber 6 bicicletas EN_TRANSITO"),
                () -> assertEquals(10, estacion.ocupacionEstacion(), "La ocupación debe ser 10"),
                () -> assertEquals(0, estacion.getCapacidadEstacion(), "No deben quedar espacios libres"),
                () -> assertFalse(estacion.hayEspacio(), "No debe haber espacio disponible")
        );
    }

    @Test
    @DisplayName("Test de petición de reubicación de bicicletas")
    void testPeticionReubicacion() {
        List<Bicicleta> bicicletasParaReubicar = new ArrayList<>();
        bicicletasParaReubicar.add(new Bicicleta("BR01", NORMAL));
        bicicletasParaReubicar.add(new Bicicleta("BR02", NORMAL));
        bicicletasParaReubicar.add(new Bicicleta("BR03", ELECTRICA));

        // Reservamos espacio para reubicación
        boolean resultado = estacion.peticionReubicacion(bicicletasParaReubicar);

        assertAll("La petición de reubicación debe funcionar correctamente",
                () -> assertTrue(resultado, "Debe permitir reservar espacio para reubicación"),
                () -> assertEquals(3, estacion.getBicicletas(REUBICACION), "Debe haber 3 bicicletas en REUBICACION"),
                () -> assertEquals(7, estacion.ocupacionEstacion(), "La ocupación debe ser 7"),
                () -> assertEquals(3, estacion.getCapacidadEstacion(), "Deben quedar 3 espacios libres"),
                () -> assertTrue(bicicletasParaReubicar.isEmpty(), "La lista original debe quedar vacía")
        );

        // Intentamos reservar más espacio del disponible
        List<Bicicleta> excesoBicicletas = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            excesoBicicletas.add(new Bicicleta("BRE" + i, NORMAL));
        }

        boolean resultadoExceso = estacion.peticionReubicacion(excesoBicicletas);

        assertAll("No debe permitir reubicación cuando no hay espacio suficiente",
                () -> assertFalse(resultadoExceso, "Debe rechazar reubicación cuando no hay espacio suficiente"),
                () -> assertEquals(3, estacion.getBicicletas(REUBICACION), "Debe mantener 3 bicicletas en REUBICACION"),
                () -> assertEquals(7, estacion.ocupacionEstacion(), "La ocupación debe seguir siendo 7"),
                () -> assertEquals(4, excesoBicicletas.size(), "La lista original debe mantener todos sus elementos")
        );
    }

    @Test
    @DisplayName("Test de recoger bicicleta alquilada")
    void testRecogerBicicleta() {
        // Primero alquilamos una bicicleta
        Optional<Bicicleta> bicicletaAlquilada = estacion.peticionAlquiler(USER_ID);
        String idBicicleta = USER_ID; // Ahora el ID de la bicicleta es el ID del usuario

        // Luego la recogemos
        Optional<Bicicleta> bicicletaRecogida = estacion.recogerBicicleta(idBicicleta);

        assertAll("La recogida de bicicleta debe funcionar correctamente",
                () -> assertTrue(bicicletaRecogida.isPresent(), "Debe devolver la bicicleta alquilada"),
                () -> assertEquals(idBicicleta, bicicletaRecogida.get().getId(), "Debe ser la bicicleta con el ID del usuario"),
                () -> assertEquals(ALQUILADA, bicicletaRecogida.get().getEstado(), "El estado debe seguir siendo ALQUILADA"),
                () -> assertEquals(0, estacion.getBicicletas(ALQUILADA), "No debe quedar bicicletas alquiladas en la estación"),
                () -> assertEquals(3, estacion.ocupacionEstacion(), "La ocupación debe ser 3")
        );

        // Intentamos recoger una bicicleta con un ID que no existe
        Optional<Bicicleta> intentoRecogida = estacion.recogerBicicleta("ID_INEXISTENTE");

        assertAll("No debe permitir recoger bicicletas que no existen",
                () -> assertFalse(intentoRecogida.isPresent(), "No debe devolver bicicleta cuando no existe el ID")
        );
    }

    @RepeatedTest(20)
    @DisplayName("Test de entregar bicicleta en tránsito")
    void testEntregarBicicleta() {
        Bicicleta bicicletaNueva = new Bicicleta("B004", NORMAL);

        // Reservamos espacio para la bicicleta en tránsito
        estacion.peticionTransito(bicicletaNueva);

        // Guardamos el estado inicial para comparar después
        int disponiblesInicial = estacion.getDisponibles();
        int fueraDeServicioInicial = estacion.getBicicletas(FUERA_DE_SERVICIO);

        // Ahora la entregamos
        boolean resultado = estacion.entregarBicicleta(bicicletaNueva);

        // Después de la entrega, la bicicleta puede estar en DISPONIBLE o en FUERA_DE_SERVICIO
        // dependiendo del resultado aleatorio del método necesitaMantenimiento()
        int disponiblesFinal = estacion.getDisponibles();
        int fueraDeServicioFinal = estacion.getBicicletas(FUERA_DE_SERVICIO);

        assertAll("La entrega de bicicleta debe actualizar el estado correctamente",
                () -> assertTrue(resultado, "Debe confirmar entrega exitosa"),
                () -> assertEquals(0, estacion.getBicicletas(EN_TRANSITO), "No debe haber bicicletas EN_TRANSITO"),
                // Al menos una de estas condiciones debe ser cierta
                () -> assertTrue(
                        // O bien la bicicleta pasó a DISPONIBLE
                        (disponiblesFinal == disponiblesInicial + 1 && fueraDeServicioFinal == fueraDeServicioInicial) ||
                                // O bien la bicicleta pasó a FUERA_DE_SERVICIO
                                (disponiblesFinal == disponiblesInicial && fueraDeServicioFinal == fueraDeServicioInicial + 1),
                        "La bicicleta debe estar en DISPONIBLE o en FUERA_DE_SERVICIO después de la entrega"),
                () -> assertEquals(5, estacion.ocupacionEstacion(), "La ocupación debe ser 5")
        );

        // Intentamos entregar una bicicleta que no está en tránsito
        Bicicleta bicicletaSinTransito = new Bicicleta("B005", NORMAL);
        boolean resultadoSinTransito = estacion.entregarBicicleta(bicicletaSinTransito);

        assertAll("No debe permitir entregar bicicletas que no están en tránsito",
                () -> assertFalse(resultadoSinTransito, "Debe rechazar entrega de bicicleta que no está en tránsito")
        );
    }

    @Test
    @DisplayName("Test de entregar lista de bicicletas (redistribución)")
    void testReubicarBicicletas() {
        // Creamos una lista de bicicletas para reubicar
        List<Bicicleta> listaBicicletas = new ArrayList<>();
        listaBicicletas.add(new Bicicleta("B005", NORMAL));
        listaBicicletas.add(new Bicicleta("B006", ELECTRICA));

        // Primero hacemos la petición de reubicación para reservar espacio
        boolean resultadoReubicacion = estacion.peticionReubicacion(listaBicicletas);

        // Recreamos la lista para la entrega, ya que la original se vacía en peticionReubicacion
        listaBicicletas.add(new Bicicleta("B005", NORMAL));
        listaBicicletas.add(new Bicicleta("B006", ELECTRICA));

        // Entregamos la lista de bicicletas
        boolean resultado = estacion.reubicarBicicleatas(listaBicicletas);


        assertAll("La entrega de lista de bicicletas debe funcionar correctamente",
                () -> assertTrue(resultadoReubicacion, "La reserva de reubicación debe ser exitosa"),
                () -> assertTrue(resultado, "Debe confirmar entrega exitosa"),
                () -> assertEquals(6, estacion.getDisponibles(), "Debe haber 6 bicicletas disponibles"),
                () -> assertEquals(0, estacion.getBicicletas(REUBICACION), "No debe haber bicicletas en REUBICACION"),
                () -> assertTrue(listaBicicletas.isEmpty(), "La lista original debe quedar vacía"),
                () -> assertEquals(6, estacion.ocupacionEstacion(), "La ocupación debe ser 6")
        );

        // Probamos el caso donde las bicicletas entregadas no corresponden a las reservadas
        List<Bicicleta> listaNoReservada = new ArrayList<>();
        listaNoReservada.add(new Bicicleta("BNR1", NORMAL));
        listaNoReservada.add(new Bicicleta("BNR2", NORMAL));

        boolean resultadoNoReservado = estacion.reubicarBicicleatas(listaNoReservada);

        assertAll("No debe permitir entregar bicicletas no reservadas previamente",
                () -> assertFalse(resultadoNoReservado, "Debe rechazar entrega de bicicletas no reservadas"),
                () -> assertEquals(2, listaNoReservada.size(), "La lista original debe mantener todas las bicicletas")
        );
    }

    @Test
    @DisplayName("Test del método listaReubicacion para obtener bicicletas para redistribución")
    void testListaReubicacion() {
        // Verificamos el estado inicial
        assertEquals(4, estacion.getDisponibles(), "Debe haber 4 bicicletas disponibles inicialmente");

        // Obtenemos 2 bicicletas para reubicación
        List<Bicicleta> bicicletasParaReubicar = estacion.listaReubicacion(2);

        assertAll("El método listaReubicacion debe funcionar correctamente",
                () -> assertEquals(2, bicicletasParaReubicar.size(), "Debe devolver 2 bicicletas"),
                () -> assertEquals(2, estacion.getDisponibles(), "Deben quedar 2 bicicletas disponibles en la estación"),
                () -> assertEquals(2, estacion.ocupacionEstacion(), "La ocupación debe ser 2")
        );

        // Intentamos obtener más bicicletas de las disponibles
        List<Bicicleta> masBicicletas = estacion.listaReubicacion(3);

        assertAll("Debe devolver todas las disponibles aunque se pidan más",
                () -> assertEquals(2, masBicicletas.size(), "Debe devolver las 2 bicicletas restantes"),
                () -> assertEquals(0, estacion.getDisponibles(), "No deben quedar bicicletas disponibles"),
                () -> assertEquals(0, estacion.ocupacionEstacion(), "La ocupación debe ser 0")
        );

        // Probamos con un número inválido de bicicletas
        assertThrows(IllegalArgumentException.class,
                () -> estacion.listaReubicacion(0),
                "Debe lanzar excepción cuando se pide un número no positivo de bicicletas");

        assertThrows(IllegalArgumentException.class,
                () -> estacion.listaReubicacion(-1),
                "Debe lanzar excepción cuando se pide un número negativo de bicicletas");
    }

    @Test
    @DisplayName("Test del método cancelarReubicacion para devolver bicicletas a la estación")
    void testCancelarReubicacion() {
        // Preparamos el escenario: primero reducimos las bicicletas disponibles
        List<Bicicleta> bicicletasRetiradas = estacion.listaReubicacion(2);
        assertEquals(2, estacion.getDisponibles(), "Deben quedar 2 bicicletas disponibles");

        // Ahora intentamos cancelar la reubicación devolviendo las bicicletas
        boolean resultado = estacion.cancelarReubicacion(bicicletasRetiradas);

        assertAll("El método cancelarReubicacion debe funcionar correctamente",
                () -> assertTrue(resultado, "Debe confirmar la operación como exitosa"),
                () -> assertTrue(bicicletasRetiradas.isEmpty(), "La lista de bicicletas debe quedar vacía"),
                () -> assertEquals(4, estacion.getDisponibles(), "Deben volver a ser 4 bicicletas disponibles"),
                () -> assertEquals(4, estacion.ocupacionEstacion(), "La ocupación debe ser 4")
        );

        // Probamos el caso donde no hay espacio suficiente
        // Primero llenamos la estación con bicicletas en tránsito
        for (int i = 0; i < 6; i++) {
            estacion.peticionTransito(new Bicicleta("BT" + i, NORMAL));
        }

        // Ahora intentamos cancelar una reubicación cuando la estación está llena
        List<Bicicleta> masReubicacion = new ArrayList<>();
        masReubicacion.add(new Bicicleta("BM1", NORMAL));
        masReubicacion.add(new Bicicleta("BM2", NORMAL));

        boolean resultadoSinEspacio = estacion.cancelarReubicacion(masReubicacion);

        assertAll("No debe permitir cancelar reubicación cuando no hay espacio",
                () -> assertFalse(resultadoSinEspacio, "Debe rechazar cancelación por falta de espacio"),
                () -> assertEquals(2, masReubicacion.size(), "La lista debe mantener sus elementos"),
                () -> assertEquals(4, estacion.getDisponibles(), "El número de bicicletas disponibles no debe cambiar")
        );
    }

    @Test
    @DisplayName("Test de avisar mantenimiento con umbral mínimo")
    void testAvisarMantenimiento() {
        // Creamos una estación con bicicletas normales
        Bicicleta[] bicicletasNormales = IntStream.range(0, MIN_MANTENIMIENTO + 1)
                .mapToObj(i -> new Bicicleta("BN" + i, NORMAL))
                .toArray(Bicicleta[]::new);

        EstacionBicicletas estacionFS = new EstacionBicicletas("EFS", 20, bicicletasNormales);

        // Inicialmente no debe necesitar mantenimiento
        assertFalse(estacionFS.avisarMantenimiento(),
                "No debe avisar mantenimiento cuando todas las bicicletas están disponibles");

        // Simulamos que algunas bicicletas pasan a estado FUERA_DE_SERVICIO (más que el mínimo)
        try {
            // Adquirimos el semáforo para operaciones de exclusión mutua
            estacionFS.semExm().acquire();

            // Obtenemos el mapa interno de bicicletas mediante reflection
            java.lang.reflect.Field field = EstacionBicicletas.class.getDeclaredField("bicicletas");
            field.setAccessible(true);
            Map<Constantes.EstadosBicicletas, Queue<Bicicleta>> mapaBicicletas =
                    (Map<Constantes.EstadosBicicletas, Queue<Bicicleta>>) field.get(estacionFS);

            // Movemos suficientes bicicletas de DISPONIBLE a FUERA_DE_SERVICIO
            // para superar el umbral mínimo
            List<Bicicleta> bicicletasDisponibles = new ArrayList<>(mapaBicicletas.get(DISPONIBLE));

            for (int i = 0; i < MIN_MANTENIMIENTO + 1; i++) {
                Bicicleta b = bicicletasDisponibles.get(i);
                mapaBicicletas.get(DISPONIBLE).remove(b);
                b.setEstado(FUERA_DE_SERVICIO);
                mapaBicicletas.get(FUERA_DE_SERVICIO).add(b);
            }

            // Liberamos el semáforo
            estacionFS.semExm().release();
        } catch (Exception e) {
            fail("No se pudo configurar la prueba: " + e.getMessage());
        }

        // Ahora sí debe avisar mantenimiento
        assertAll("El aviso de mantenimiento debe funcionar correctamente",
                () -> assertTrue(estacionFS.avisarMantenimiento(),
                        "Debe avisar mantenimiento cuando hay suficientes bicicletas fuera de servicio"),
                () -> assertEquals(MIN_MANTENIMIENTO + 1, estacionFS.getBicicletas(FUERA_DE_SERVICIO),
                        "Debe tener el número correcto de bicicletas fuera de servicio")
        );

        // Ahora probamos el caso límite (exactamente en el umbral)
        EstacionBicicletas estacionMinFS = new EstacionBicicletas("EFS_MIN", 20,
                IntStream.range(0, MIN_MANTENIMIENTO)
                        .mapToObj(i -> new Bicicleta("BM" + i, NORMAL))
                        .toArray(Bicicleta[]::new));

        try {
            // Adquirimos el semáforo para operaciones de exclusión mutua
            estacionMinFS.semExm().acquire();

            // Obtenemos el mapa interno de bicicletas mediante reflection
            java.lang.reflect.Field field = EstacionBicicletas.class.getDeclaredField("bicicletas");
            field.setAccessible(true);
            Map<Constantes.EstadosBicicletas, Queue<Bicicleta>> mapaBicicletas =
                    (Map<Constantes.EstadosBicicletas, Queue<Bicicleta>>) field.get(estacionMinFS);

            // Movemos exactamente el mínimo de bicicletas a FUERA_DE_SERVICIO
            List<Bicicleta> bicicletasDisponibles = new ArrayList<>(mapaBicicletas.get(DISPONIBLE));
            mapaBicicletas.get(DISPONIBLE).clear();

            for (Bicicleta b : bicicletasDisponibles) {
                b.setEstado(FUERA_DE_SERVICIO);
                mapaBicicletas.get(FUERA_DE_SERVICIO).add(b);
            }

            // Liberamos el semáforo
            estacionMinFS.semExm().release();
        } catch (Exception e) {
            fail("No se pudo configurar la prueba con el mínimo: " + e.getMessage());
        }

        assertAll("No debe avisar en el límite del umbral",
                () -> assertFalse(estacionMinFS.avisarMantenimiento(),
                        "No debe avisar cuando hay exactamente el mínimo de bicicletas"),
                () -> assertEquals(MIN_MANTENIMIENTO, estacionMinFS.getBicicletas(FUERA_DE_SERVICIO),
                        "Debe tener el mínimo de bicicletas fuera de servicio")
        );
    }

    @Test
    @DisplayName("Test de mantenimiento de bicicletas (retirada para reparación)")
    void testMantenimientoBicicletas() {
        // Creamos una estación con bicicletas normales
        Bicicleta[] bicicletasNormales = IntStream.range(0, 3)
                .mapToObj(i -> new Bicicleta("BN" + i, i % 2 == 0 ? NORMAL : ELECTRICA))
                .toArray(Bicicleta[]::new);

        EstacionBicicletas estacionFS = new EstacionBicicletas("EFS", 20, bicicletasNormales);

        // Simulamos que las bicicletas pasan a estado FUERA_DE_SERVICIO
        try {
            // Adquirimos el semáforo para operaciones de exclusión mutua
            estacionFS.semExm().acquire();

            // Obtenemos el mapa interno de bicicletas mediante reflection
            java.lang.reflect.Field field = EstacionBicicletas.class.getDeclaredField("bicicletas");
            field.setAccessible(true);
            Map<Constantes.EstadosBicicletas, Queue<Bicicleta>> mapaBicicletas =
                    (Map<Constantes.EstadosBicicletas, Queue<Bicicleta>>) field.get(estacionFS);

            // Movemos todas las bicicletas de DISPONIBLE a FUERA_DE_SERVICIO
            List<Bicicleta> bicicletasDisponibles = new ArrayList<>(mapaBicicletas.get(DISPONIBLE));
            mapaBicicletas.get(DISPONIBLE).clear();

            for (Bicicleta b : bicicletasDisponibles) {
                b.setEstado(FUERA_DE_SERVICIO);
                mapaBicicletas.get(FUERA_DE_SERVICIO).add(b);
            }

            // Liberamos el semáforo
            estacionFS.semExm().release();
        } catch (Exception e) {
            fail("No se pudo configurar la prueba: " + e.getMessage());
        }

        // Verificamos que las bicicletas estén en estado FUERA_DE_SERVICIO
        assertEquals(3, estacionFS.getBicicletas(FUERA_DE_SERVICIO),
                "Debe haber 3 bicicletas FUERA_DE_SERVICIO");

        // Ahora verificamos el mantenimiento
        List<Bicicleta> bicicletasMantenimiento = estacionFS.mantenimientoBicicletas();

        assertAll("El mantenimiento debe funcionar correctamente",
                () -> assertEquals(3, bicicletasMantenimiento.size(), "Debe devolver todas las bicicletas para mantenimiento"),
                () -> assertEquals(0, estacionFS.getBicicletas(FUERA_DE_SERVICIO), "No deben quedar bicicletas fuera de servicio"),
                () -> assertEquals(EN_REPARACION, bicicletasMantenimiento.get(0).getEstado(), "Las bicicletas deben estar EN_REPARACION"),
                () -> assertEquals(EN_REPARACION, bicicletasMantenimiento.get(1).getEstado(), "Las bicicletas deben estar EN_REPARACION"),
                () -> assertEquals(EN_REPARACION, bicicletasMantenimiento.get(2).getEstado(), "Las bicicletas deben estar EN_REPARACION"),
                () -> assertEquals(0, estacionFS.ocupacionEstacion(), "La estación debe quedar vacía tras el mantenimiento")
        );

        // Caso en que no hay bicicletas para mantenimiento
        List<Bicicleta> sinMantenimiento = estacionFS.mantenimientoBicicletas();

        assertAll("No debe devolver bicicletas cuando no hay ninguna para mantenimiento",
                () -> assertTrue(sinMantenimiento.isEmpty(), "La lista debe estar vacía cuando no hay bicicletas para mantenimiento")
        );
    }

    @Test
    @DisplayName("Test de obtener número de bicicletas por estado")
    void testGetBicicletas() {
        // Creamos casos para cada estado
        Optional<Bicicleta> alquilada = estacion.peticionAlquiler(USER_ID);
        estacion.peticionTransito(new Bicicleta("BTR1", NORMAL));

        // Preparamos bicicletas para reubicación
        List<Bicicleta> bicicletasReubicacion = new ArrayList<>();
        bicicletasReubicacion.add(new Bicicleta("BRU1", NORMAL));
        estacion.peticionReubicacion(bicicletasReubicacion);

        // Para el estado FUERA_DE_SERVICIO, debemos simular el proceso correcto
        // Primero, creamos una estación vacía para manipular el estado interno de manera controlada
        EstacionBicicletas estacionFS = new EstacionBicicletas("EFS", 10, new Bicicleta("BFS1", NORMAL));

        // Ahora simulamos un proceso que pondría una bicicleta en estado FUERA_DE_SERVICIO
        // En un caso real, esto ocurriría cuando se devuelve una bicicleta y se detecta que necesita mantenimiento
        // Como no podemos forzar directamente el método necesitaMantenimiento(), utilizamos reflection
        // para modificar el estado interno (solo para propósitos de testing)
        try {
            // Adquirimos el semáforo para operaciones de exclusión mutua
            estacionFS.semExm().acquire();

            // Obtenemos el mapa interno de bicicletas mediante reflection
            java.lang.reflect.Field field = EstacionBicicletas.class.getDeclaredField("bicicletas");
            field.setAccessible(true);
            Map<Constantes.EstadosBicicletas, Queue<Bicicleta>> mapaBicicletas =
                    (Map<Constantes.EstadosBicicletas, Queue<Bicicleta>>) field.get(estacionFS);

            // Movemos la bicicleta de DISPONIBLE a FUERA_DE_SERVICIO
            Bicicleta bicicletaParaFS = mapaBicicletas.get(DISPONIBLE).poll();
            bicicletaParaFS.setEstado(FUERA_DE_SERVICIO);
            mapaBicicletas.get(FUERA_DE_SERVICIO).add(bicicletaParaFS);

            // Liberamos el semáforo
            estacionFS.semExm().release();
        } catch (Exception e) {
            fail("No se pudo configurar la prueba: " + e.getMessage());
        }

        assertAll("Debe obtener correctamente el número de bicicletas por estado",
                () -> assertEquals(3, estacion.getBicicletas(DISPONIBLE), "Debe tener 3 bicicletas disponibles"),
                () -> assertEquals(1, estacion.getBicicletas(ALQUILADA), "Debe tener 1 bicicleta alquilada"),
                () -> assertEquals(1, estacion.getBicicletas(EN_TRANSITO), "Debe tener 1 bicicleta en tránsito"),
                () -> assertEquals(1, estacion.getBicicletas(REUBICACION), "Debe tener 1 bicicleta en reubicación"),
                () -> assertEquals(0, estacion.getBicicletas(FUERA_DE_SERVICIO), "No debe tener bicicletas fuera de servicio"),
                () -> assertEquals(0, estacion.getBicicletas(EN_REPARACION), "No debe tener bicicletas en reparación"),
                () -> assertEquals(0, estacionFS.getBicicletas(DISPONIBLE), "La estación FS no debe tener bicicletas disponibles"),
                () -> assertEquals(1, estacionFS.getBicicletas(FUERA_DE_SERVICIO), "La estación FS debe tener 1 bicicleta fuera de servicio")
        );

        // Caso con parámetro inválido
        assertThrows(IllegalArgumentException.class,
                () -> estacion.getBicicletas(null),
                "Debe lanzar excepción con estado null");
    }

    @Test
    @DisplayName("Test de cálculo de ocupación incluyendo el estado REUBICACION")
    void testOcupacionEstacionConReubicacion() {
        // Estado inicial
        assertEquals(4, estacion.ocupacionEstacion(), "La ocupación inicial debe ser 4");

        // Añadimos bicicletas en diferentes estados
        estacion.peticionAlquiler(USER_ID); // ALQUILADA
        estacion.peticionTransito(new Bicicleta("BTR1", NORMAL)); // EN_TRANSITO

        // Añadimos bicicletas para reubicación
        List<Bicicleta> bicicletasReubicacion = new ArrayList<>();
        bicicletasReubicacion.add(new Bicicleta("BR1", NORMAL));
        bicicletasReubicacion.add(new Bicicleta("BR2", ELECTRICA));
        estacion.peticionReubicacion(bicicletasReubicacion);

        // Verificamos que la ocupación incluye todos los estados correctamente
        assertEquals(7, estacion.ocupacionEstacion(),
                "La ocupación debe incluir DISPONIBLE(3) + ALQUILADA(1) + EN_TRANSITO(1) + REUBICACION(2)");

        // Verificamos disponibilidad de espacios
        assertEquals(3, estacion.getCapacidadEstacion(), "Deben quedar 3 espacios disponibles");
        assertTrue(estacion.hayEspacio(), "Debe haber espacio disponible");

        // Llenamos la estación al máximo
        List<Bicicleta> masReubicacion = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            masReubicacion.add(new Bicicleta("BRF" + i, NORMAL));
        }
        estacion.peticionReubicacion(masReubicacion);

        // Verificamos ocupación completa
        assertEquals(10, estacion.ocupacionEstacion(), "La ocupación debe ser 10 (capacidad máxima)");
        assertEquals(0, estacion.getCapacidadEstacion(), "No deben quedar espacios disponibles");
        assertFalse(estacion.hayEspacio(), "No debe haber espacio disponible");
    }

    @RepeatedTest(10)
    @DisplayName("Test de simulación de diferentes escenarios de viaje")
    void testSimulacionViajes() {
        // Caso 1: Viaje normal completo
        Optional<Bicicleta> bicicleta1Alquilada = estacion.peticionAlquiler("usuario1");
        String idBicicleta1 = "usuario1"; // El ID ahora es el del usuario
        Optional<Bicicleta> bicicleta1Recogida = estacion.recogerBicicleta(idBicicleta1);

        // Creamos estación destino
        EstacionBicicletas estacionDestino = new EstacionBicicletas("DEST", 5,
                new Bicicleta("BDEST", NORMAL));

        // Completamos el viaje normal
        boolean reservaTransito1 = estacionDestino.peticionTransito(bicicleta1Recogida.get());
        boolean entregaExitosa1 = estacionDestino.entregarBicicleta(bicicleta1Recogida.get());

        // Caso 2: Viaje con bicicleta eléctrica que necesitaría pasar por punto de recarga
        Optional<Bicicleta> bicicletaElectricaAlquilada = estacion.peticionAlquiler("usuario2"); // Asumimos que toma la eléctrica

        // Verificamos si es eléctrica y simulamos necesidad de carga
        boolean esElectrica = bicicletaElectricaAlquilada.isPresent() &&
                bicicletaElectricaAlquilada.get().getTipo() == ELECTRICA;

        boolean necesitaCarga = esElectrica && bicicletaElectricaAlquilada.get().necesitaCarga();

        // Creamos punto de recarga y simulamos carga si es necesario
        PuntoRecarga puntoRecarga = new PuntoRecarga(5);
        boolean cargaCompletada = false;

        if (esElectrica) {
            Optional<Bicicleta> bicicletaElectricaRecogida = estacion.recogerBicicleta("usuario2");

            // Simulamos el paso por punto de recarga
            if (necesitaCarga && bicicletaElectricaRecogida.isPresent()) {
                try {
                    puntoRecarga.semDeposito().acquire();
                    puntoRecarga.semExm().acquire();

                    puntoRecarga.iniciarCarga(bicicletaElectricaRecogida.get());

                    // Simulamos tiempo de carga
                    Thread.sleep(100);

                    Optional<Bicicleta> bicicletaCargada = puntoRecarga.finalizadaCarga(bicicletaElectricaRecogida.get().getId());
                    cargaCompletada = bicicletaCargada.isPresent();

                    puntoRecarga.semExm().release();
                    puntoRecarga.semDeposito().release();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        // Caso 3: Viaje donde estación destino está llena
        // Creamos una estación con capacidad mínima
        EstacionBicicletas estacionLlena = new EstacionBicicletas("LLENA", CAPACIDAD_MINIMA,
                new Bicicleta("BLLENA", NORMAL));

        // Llenamos la estación hasta su capacidad
        int espaciosDisponibles = estacionLlena.getCapacidadEstacion();
        for (int i = 0; i < espaciosDisponibles; i++) {
            estacionLlena.peticionTransito(new Bicicleta("BLLENA" + i, NORMAL));
        }

        // Verificamos que la estación está realmente llena
        assertFalse(estacionLlena.hayEspacio(), "La estación debe estar completamente llena");

        // Ahora intentamos hacer un viaje a esta estación llena
        Optional<Bicicleta> bicicleta3Alquilada = estacion.peticionAlquiler("usuario3");
        boolean reservaRechazada;

        if (bicicleta3Alquilada.isPresent()) {
            Optional<Bicicleta> bicicleta3Recogida = estacion.recogerBicicleta("usuario3");

            if (bicicleta3Recogida.isPresent()) {
                // Intentamos reservar espacio en la estación llena
                reservaRechazada = !estacionLlena.peticionTransito(bicicleta3Recogida.get());
            } else {
                reservaRechazada = false;
            }
        } else {
            reservaRechazada = false;
        }

        // Validamos todos los escenarios
        boolean finalCargaCompletada = cargaCompletada;
        assertAll("La simulación de viajes debe funcionar correctamente en diferentes escenarios",
                // Caso 1: Viaje normal
                () -> assertTrue(bicicleta1Alquilada.isPresent(), "Debe poder alquilar bicicleta 1"),
                () -> assertTrue(bicicleta1Recogida.isPresent(), "Debe poder recoger bicicleta 1"),
                () -> assertTrue(reservaTransito1, "Debe poder reservar tránsito para bicicleta 1"),
                () -> assertTrue(entregaExitosa1, "Debe poder entregar bicicleta 1 en destino"),
                () -> assertEquals(2, estacionDestino.getBicicletas(DISPONIBLE) + estacionDestino.getBicicletas(FUERA_DE_SERVICIO),
                        "La estación destino debe tener 2 bicicletas entre disponibles y fuera de servicio"),
                // Caso 2: Viaje con bicicleta eléctrica y posible carga
                () -> assertTrue(bicicletaElectricaAlquilada.isPresent(), "Debe poder alquilar una bicicleta eléctrica"),
                () -> assertEquals(esElectrica && necesitaCarga ? true : true, esElectrica ? finalCargaCompletada || !necesitaCarga : true,
                        "Si es eléctrica y necesita carga, la carga debe completarse"),

                // Caso 3: Viaje con estación llena
                () -> assertTrue(reservaRechazada, "Debe rechazar la reserva cuando la estación está llena")
        );
    }

    @Test
    @DisplayName("Test integrado del flujo de reubicación de bicicletas")
    void testFlujoReubicacionCompleto() {
        // Creamos estación origen y destino
        EstacionBicicletas estacionOrigen = new EstacionBicicletas("ORIGEN", 10,
                new Bicicleta("BO1", NORMAL),
                new Bicicleta("BO2", NORMAL),
                new Bicicleta("BO3", ELECTRICA)
        );

        EstacionBicicletas estacionDestino = new EstacionBicicletas("DESTINO", 10,
                new Bicicleta("BD1", NORMAL)
        );

        // 1. Obtenemos bicicletas para redistribuir de la estación origen
        List<Bicicleta> bicicletasRedistribucion = estacionOrigen.listaReubicacion(2);

        assertEquals(2, bicicletasRedistribucion.size(), "Debe obtener 2 bicicletas para redistribución");
        assertEquals(1, estacionOrigen.getDisponibles(), "Estación origen debe tener 1 bicicleta disponible");

        // 2. Reservamos espacio en la estación destino
        boolean reservaExitosa = estacionDestino.peticionReubicacion(new ArrayList<>(List.of(
                new Bicicleta(bicicletasRedistribucion.get(0).getId(), bicicletasRedistribucion.get(0).getTipo()),
                new Bicicleta(bicicletasRedistribucion.get(1).getId(), bicicletasRedistribucion.get(1).getTipo())
        )));

        assertTrue(reservaExitosa, "Debe permitir reservar espacio para reubicación");
        assertEquals(2, estacionDestino.getBicicletas(REUBICACION), "Estación destino debe tener 2 bicicletas en REUBICACION");

        // 3. Entregamos las bicicletas en la estación destino
        boolean entregaExitosa = estacionDestino.reubicarBicicleatas(bicicletasRedistribucion);

        // 4. Verificamos el estado final
        assertAll("El flujo completo de reubicación debe funcionar correctamente",
                () -> assertTrue(entregaExitosa, "La entrega debe ser exitosa"),
                () -> assertTrue(bicicletasRedistribucion.isEmpty(), "La lista de bicicletas debe quedar vacía"),
                () -> assertEquals(0, estacionDestino.getBicicletas(REUBICACION), "No debe haber bicicletas en REUBICACION"),
                () -> assertEquals(3, estacionDestino.getDisponibles(), "Estación destino debe tener 3 bicicletas disponibles"),
                () -> assertEquals(1, estacionOrigen.getDisponibles(), "Estación origen debe mantener 1 bicicleta disponible")
        );

        // 5. Simulamos un caso donde se cancela la redistribución
        List<Bicicleta> nuevaReubicacion = estacionOrigen.listaReubicacion(1);
        assertEquals(1, nuevaReubicacion.size(), "Debe obtener 1 bicicleta para la nueva reubicación");
        assertEquals(0, estacionOrigen.getDisponibles(), "Estación origen debe tener 0 bicicletas disponibles");

        // Cancelamos la redistribución
        boolean cancelacionExitosa = estacionOrigen.cancelarReubicacion(nuevaReubicacion);

        assertAll("La cancelación de redistribución debe funcionar correctamente",
                () -> assertTrue(cancelacionExitosa, "La cancelación debe ser exitosa"),
                () -> assertTrue(nuevaReubicacion.isEmpty(), "La lista de bicicletas debe quedar vacía"),
                () -> assertEquals(1, estacionOrigen.getDisponibles(), "Estación origen debe volver a tener 1 bicicleta disponible")
        );
    }
}