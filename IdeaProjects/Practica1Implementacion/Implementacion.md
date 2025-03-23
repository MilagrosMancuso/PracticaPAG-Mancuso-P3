# Implementación de la práctica

Para la implementación de la práctica el alumno deberá utilizar las clases descritas dentro de los siguientes paquetes de Java:

- `es.ujaen.ssccdd.datos` : Aquí se encuentran los datos compartidos que se utilizan dentro de las tareas del proyecto para la implementación del problema descrito. Estas clases no pueden sufrir cambios porque son imprescindibles para el diseño de las pruebas para la corrección de la práctica.
- `es.ujaen.ssccdd.tareas` : Aquí se encuentran las tareas que componen la solución para la implementación. El alumno deberá completar estas clases para demostrar que han resuelto el problema. La definición de las clases no puede cambiarse por el mismo motivo que en el paquete anterior.
- `Constantes.java` : Aquí el alumno deberá incluir las constantes que estime oportunas para la implementación de la práctica. La interface incluye elementos que se utilizan en las clases ya incluidas en el proyecto. Las definiciones de los elementos que ya están presentes, y que se utilizan, no podrán alterarse. El valor de las constantes sí puede adaptarse para las necesidades del alumno en su implementación.

## Evaluación de la Práctica

La evaluación de esta práctica se realizará mediante pruebas JUnit que verificarán el correcto funcionamiento del sistema de transporte de bicicletas compartidas. Las pruebas no estarán disponibles hasta que no concluya el plazo de entrega de la implementación de la práctica. Pero para que el alumno tenga claro los criterios de evaluación que se comprobarán por los test se detallan a continuación:

## Criterios de Evaluación

### Aprobado (5-6)

Para obtener un aprobado, el alumno deberá demostrar que:

-   Las tareas concurrentes (`UsuarioTask`, `TecnicoMantenimientoTask`, `CamionRedistribucionTask`, `GestorTransporteTask`) se ejecutan correctamente de forma individual.
-   Se utilizan correctamente los semáforos básicos para el control de acceso a recursos compartidos.
-   Los usuarios pueden realizar peticiones de transporte y esperan correctamente hasta recibir confirmación.
-   Los técnicos responden a las órdenes de mantenimiento y recogen bicicletas de sus estaciones asignadas.
-   Los camiones pueden transportar bicicletas entre estaciones cuando reciben órdenes.
-   El gestor puede procesar peticiones de transporte simples y coordinar operaciones básicas.
-   No se producen interbloqueos (deadlocks) en situaciones básicas.

### Notable (7-8)

Para obtener un notable, además de lo anterior, el alumno deberá demostrar que:

-   El sistema maneja correctamente la concurrencia entre múltiples usuarios, técnicos y camiones simultáneamente.
-   Se implementa correctamente el manejo de bicicletas eléctricas, incluyendo la necesidad de recarga durante los viajes.
-   Las subtareas del gestor (`GestionTransporte`, `GestionMantenimiento`, `GestionRedistribucion`) funcionan coordinadamente.
-   El sistema responde adecuadamente a situaciones de carga como:
    -   Estaciones con alta demanda de bicicletas
    -   Estaciones sin espacio disponible para devolver bicicletas
    -   Múltiples peticiones de mantenimiento simultáneas
-   Se garantiza la exclusión mutua en el acceso a recursos compartidos (estaciones, zona de mantenimiento, punto de recarga).
-   El sistema es capaz de redistribuir bicicletas eficientemente entre estaciones con muchas y pocas bicicletas.
-   Se implementa correctamente la generación y gestión de eventos del sistema.

### Sobresaliente (9-10)

Para obtener un sobresaliente, además de todo lo anterior, el alumno deberá demostrar que:

-   El sistema es robusto ante situaciones complejas como:
    -   Escenarios de alta concurrencia con múltiples usuarios solicitando bicicletas simultáneamente
    -   Estaciones que se quedan sin bicicletas mientras hay usuarios esperando
    -   Necesidad de redistribución dinámica cuando las estaciones están desbalanceadas
    -   Gestión eficiente de bicicletas averiadas durante el uso
-   Se implementa correctamente la reserva anticipada de espacios en las estaciones de destino para garantizar que los usuarios siempre pueden finalizar sus viajes.
-   El sistema optimiza el uso de los recursos (camiones, técnicos) evitando que estén esperando en lugares donde no pueden realizar su trabajo.
-   Se manejan correctamente las condiciones de competencia en todos los puntos críticos del sistema.
-   La solución implementa un mecanismo eficiente para la finalización ordenada de todas las tareas al terminar la simulación.
-   El sistema mantiene un registro coherente de eventos que permite reconstruir la secuencia de operaciones realizadas.
-   No se producen inaniciones (starvation) de ninguna tarea en el sistema bajo ninguna circunstancia.

## Nota importante

Se valorará especialmente la correcta sincronización de los procesos concurrentes y la prevención de condiciones de carrera, interbloqueos e inanición. Además se tendrá en cuenta un estilo adecuado en la generación del código así como un correcto diseño de las actividades que deben realizar cada una de las tareas mediante métodos apropiados.

Para obtener la máxima calificación, es fundamental que el alumno implemente todas las funcionalidades descritas en los requisitos, prestando especial atención a la gestión de la concurrencia mediante semáforos y al correcto funcionamiento del sistema en su conjunto. 

La definición de las tareas no deberá alterarse para que la práctica pueda evaluarse adecuadamente por parte de los profesores. Si el alumno detecta alguna deficiencia deberá comunicarlo lo antes posible para que el profesorado comprobarlo y realizar las modificaciones necesarias y que estén comunicada a tiempo para no interferir con el desarrollo de la implementación de la práctica.
