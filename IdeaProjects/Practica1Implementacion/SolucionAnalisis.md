# Solución Propuesta

## Análisis
Se tiene que identificar los tipos de datos que componen la solución del problema así como las variables compartidas con su inicialización y los semáforos necesarios con su inicialización y una descripción de los mismos.

### Tipos de datos necesarios

#### TDA List<T>
Representa una estructura de datos que permite almacenar un elemento de un tipo definido
```
void add(T)     // Añade un elemento a la lista
T poll()        // Obtiene y elimina el primer elemento de la lista
```

#### TDA Bicicleta
Representa la bicicleta en la que se basa el sistema de transporte de la práctica. 
```
TipoBicicleta {NORMAL, ELECTRICA}

boolena necesitaCarga()
```

#### TDA Peticion
Representa a una petición que debe ser resuelta en el sistema
```
String      id
String      origen
String      destino
int         numBicicletas
Semaforo    resolucion(0)     // Es un semáforo de sincronización con la petición
 
Los getter asociados a las variables
```

#### TDA PuntoRecarga
Representa el punto de carga para las bicicletas eléctricas que lo necesiten
```
List<Bicicleta> listaBicicletas
int             capacidad

int getCapacidad()
Bicicleta cargar(Bicicleta)
```

#### TDA ZonaMantenimiento
Representa la zona donde se depositan las bicicletas una vez reparadas
```
String          id
List<Bicicleta> listaBicicletas

int getNumBicicletas()
void depositarBicicletas(List<Bicicletas>)
List<Bicicletas> recogerBicicletas(int numBicicletas)
```

#### TDA EstacionBicicletas
Representa una estación de bicletas donde están depositadas las bicicletas para el uso en el sistema de transporte
```
String          id
List<Bicicleta> listaBicicletas

int getNumBicicletas()
Bicicleta recogerBicicleta()
void depositarBicicleta(Bicicleta)
void depositarBicicletas(List<Bicicleta>)
List<Bicicleta> recogerBicicletas(int numBicicletas)
List<Bicicleta> mantenimientoBicicletas() 
```

### Variables compartidas
Las variables compartidas del sistema son las siguientes:

- `EstacionBicicletas[]  bicicletasMap` 							// Las estaciones de bicicletas del sistema de transporte
- `ZonaMantenimiento     zonaMantenimiento`					// Lugar para depositar las bicicletas reparadas
- `PuntoRecarga          puntoRecarga`								// Punto para cargar las bicicletas que lo necesiten
- `List<Peticion>        peticionesTransporte`				// Buffer para las peticiones de transporte de los usuarios
- `List<Peticion>        peticionesRedistribucion`		// Buffer para las peticiones a los camiones de redistribución 	

### Semáforos
Los semáforos necesarios son los siguientes

- Semaforo[]   exmEstaciones 							: semáforos de exclusión mutua para las estaciones inicializados a 1
- Semaforo[]   semMantenimiento					: semáforo de sincronización para el mantenimiento inicializado a 0
- Semaforo     exmTransporte							: semáforo de exclusión mutua inicializado a 1
- Semaforo     exmRedistribucion				 	: semáforo de exclusión mutua inicializado a 1
- Semaforo		exmPuntoRecarga						: semáforo de exclusión mutua inicializado a 1
- Semaforo     semCapacidadPuntoRegarga	: semáforo inicializado al número de espacios en el PuntoRecarga
- Semaforo     exmZonaMantenimiento			: semáforo de exclusión mutua inicializado a 1
- Semaforo     semCamiones							: semáforo de sincronización para los camiones inicializado a 0
- Semaforo     semGestorTransporte				: semáforo de sincronización inicializado a 0

## Diseño
Se presenta el diseño para cada uno de los procesos de la solución de la práctica. Para cada proceso indicaré primero las acciones principales que cada uno de los procesos deberá realizar y luego se describirá en detalle esas acciones de cada uno de los procesos.

### Proceso Usuario(id)
Este proceso representa al usuario del sistema que intenta realizar un viaje en bicicleta dentro de la ciudad. Para ello deberá realizar una petición y hasta que no se resuelva no podrá iniciar el viaje. Durante el viaje es posible que tenga que realizar una parada en el punto de recarga para poder completar el viaje antes de depositar la bicicleta en el destino.

```
Semaforo semUsuario(0)		// Semaforo para sincronizar las peticiones del usuario

ejecución de la tarea() {
	realizarPeticionTransporte(origen, destino)
	iniciarViaje(origen)
	completarViaje(destino)
}
```
Ahora se describirán cada una de las tareas que debe completar el usuario para realizar un viaje

#### realizarPeticionTransporte(origen, destino)
Para resolver una petición vamos a modificar un poco el problema del **productor/consumidor**. En este caso cuando se realiza la producción hay que esperar hasta que el usuario se asegure que el gestor ha atendido su petición (producto)
```
generar(origen, destino)
peticion = crearPeticion(id, origen, destino, semUsuario)
exmTransporte.wait()
peticionesTransporte.add(peticion)
exmTransporte.signal()
semGestorTransporte.signal()

// Hasta que no se resuelve la petición no se puede continuar
semUsuario.wait()
```

#### iniciarViaje(origen)
El usuario debe recoger la bicicleta de la estación de origen y en el viaje puede que tenga que parar en una estación de recarga antes de depositar la bicicleta en la estación de destino. En el punto de recarga debemos asegurarnos que hay espacio para depositar la bicicleta o esperamos hasta conseguir dejar la bicicleta.

```
exmEstaciones[origen].wait()
bicicleta = estacionesMap[origen].recogerBicicleta()
exmEstaciones[origen].signal()

inicioViaje()
if( bicicleta.necesitaCarga() ) {
	semCapacidadPuntoRecarga.wait()
	exmPuntoRecarga.wait()
	puntoRecarga.cargar(bicicleta)
	exmPuntoRecarga.signal()

	tiempoCarga() // Simulamos el tiempo necesario para la recarga de la bicicleta

	exmPuntoRecarga.wait()
	bicicleta = recogerBicicleta(bicicleta)
	exmPuntoRecarga.signal()
	semCapacidadPuntoRecarga.signal()
}
```

#### completarViaje(destino)
Para completar el viaje solo hay que entregar la bicicleta en la estación de destino

```
finalizarViaje()
exmEstaciones[destino].wait()
estacionesMap[destino].depositarBicicleta(bicicleta)
exmEstaciones[destino].signal()
```

### Proceso TecnicoMantenimiento(idEstacion)
Para la solución se va a considerar que hay un técnico de mantenimiento asociado que recogerá las bicicletas cuando sea avisado por el gestor y que al finalizar su reparación las entregará en la zona de mantenimiento.

```
ejecución de la tarea() {
	while( !finJornada() )
		recogerBicicletas()
		entregarBicicletas()
}
```

#### recogerBicicletas()
Hay que esperar hasta que sea avisado para recoger las bicicletas de la estación de mantenimiento asignada.

```
semMantenimiento[idEstacion].wait()
exmEstaciones[idEstacion].wait()
bicicletasParaMantenimiento = estacionesMap[idEstacion].mantenimientoBicicletas()
exmEstacion[idEstacion].signal()
```

#### entregarBicicletas()
Una vez finalizado el mantenimiento se entregan las bicicletas reparadas en la zona de mantenimiento asignada.

```
realizarMantenimiento()
exmZonaMantenimiento.wait()
zonaMantenimiento.depositarBicicletas(bicicletasReparadas)
exmZonaMantenimiento.signal()
```

### Proceso CamionRedistribucion(id)
Este proceso está a la espera para poder realizar una orden de redistribución. Cuando completa la redistribución se avisa al gestor para que pueda revisar el sistema y generar la siguiente orden de redistribución si fuera necesario.

```
ejecución de la tarea() {
	while( !finJornada() )
		peticion = obtenerPeticion()
		resolverPeticion(peticion)
}
```

#### peticion obtenerPeticion()
Espera que haya una petición disponible, para esta solución se considera que todos los camiones son iguales y podrán atender cualquier petición de transporte. Si se desea resolver este problema de forma general los semáforos asociados a los camiones son únicos y la petición deberá estar identificada por el camión que debe atenderla, para poder obtenerla del buffer de peticiones. 

```
semCamiones.wait()
exmRedistribucion.wait()
peticion = peticionesRedistribucion.poll()
exmRedistribucion.signal()
```

#### resolverPeticion(peticion)
Debe recoger las bicicletas en el punto de origen para transportarlas al punto de destino. 

```
origen = peticion.getOrigen()
if( origen == ZonaMantenimiento ) {
	exmZonaMantenimiento.wait()
	bicicletasParaReubicar = zonaMantenimiento.recogerBicicletas(peticion.getNumBicicletas())
	exmZonaMantenimiento.signal()
} else {
	exmEstaciones[origen].wait()
	bicicletasParaReubicar = estacionesMap[origen].recogerBicicletas(peticion.getNumBicicletas())
	exmEstaciones[origen].signal()
}

realizarTransporte()

destino = peticion.getDestino()
exmEstaciones[destino].wait()
estacionesMap[destino].depositarBicicletas(bicicletasParaReubicar)
exmEstaciones[destino].signal()

// Se indica que se ha completado la petición al gestor que la ha solicitado
peticion.semPeticion().signal()
```

### Proceso GestorTransporte(id)
Este es el proceso principal que se encarga de coordinar al resto de procesos. Para resolver las peticiones de transporte pendientes las almacena en una lista para que sean tratadas paralelamente con el resto de acciones del gestor.

```
Variables
	List<Peticion> peticionesPendientes			// Peticiones pendientes de resolver
	Semaforo	   semPeticionesPendientes(0)	// Semaforo asociado a las peticiones pendientes
	Semaforo	   semGestor(0)					// Para sincronizar las redistribuciones
	
ejecución de la tarea() {
	inicializacionSistema()
	
	ejecutar en Paralelo{
		recibirPeticionesTransporte()
		resolverPeticionesTransporte()
		gestionMantenimiento()
		gestionRedistribucion()
	} esperar a que finalicen
}
```

#### recibirPeticionesTransporte()
Esta tarea se ejecuta en paralelo con las otras dos tareas del gestor y se encarga de recoger peticiones de transporte de los usuarios

```
while( !finJornada() ) { 
	semGestorTransporte.wait()
	exmTransporte.wait()
	peticion = peticionesTrasporte.poll()
	exmTrasporte.signal()

	peticionesPendientes.add(peticion)
	semPeticionesPendientes.signal()
```

#### resolverPeticionesTransporte()
Explora la lista de peticiones pendientes y resuelve las peticiones posibles. Si una petición no puede resolverse debe quedar pendiente para resolverse más adelante

```
while( !finJornada ) {
	semPeticionesPendientes.wait()
	peticion = peticionesPendientes.poll()
	if( resolverPeticion(peticion) ) {
		// La petición está resuelta
		peticion.semPeticion().signal()
	} else {
		// Queda pendiente para resolverse
		peticionesPendientes.add(peticion)
		semPeticionesPendientes.signal()
}
```

#### gestionMantenimiento()
Comprueba si hay que avisar al tecnico de mantenimiento asociado a la estación

```
while( !finJornada() ) {
	for( EstacionMantenimiento estacion : bicicletasMap )
		exmEstaciones[estacion.getId()].wait()
		if( necesitaMantenimiento(estacion) ) {
			semMantenimiento[estacion.getId()].signal()
		}
		exmEstaciones[estacion.getId()].signal()
}
```

#### gestionRedistribucion()
Explora los diferentes puntos donde hay bicicletas para preparar una petición de redistribución

```
while( !finJornada() ) {
	// Buscamos un origen para la redistribución
	exmZonaMantenimiento.wait()
	if( excesoBicicletas(zonaMantenimiento) ) {
		numBicicletas = zonaMantenimiento.getNumBicicletas()
		origen = idZonaMantenimiento
	}
	exmZonaMantenimiento.signal()

	iterator = bicicletasMap.iterator()
	while( numBicicletas < MINIMO && iterator.hasNext() ) {
		estacion = iterator.next()
		if( excesoBicicletas(estacion) ) {
			numBicicletas = estacion.getNumBicicletas()
			origen = idEstacion
		}
	}

	// Localizamos un destino para la redistribución
	iterator = bicicletasMap.iterator()
	while( destino != null && iterator.hasNext() ) {
		estacion = iterator.next()
		if( necesitaBicicletas(estacion) ) {
			destino = idEstacion
		}
	}

	peticion = crearPeticion(id, origen, destino, semGestor)
	exmRedistribucion.wait()
	peticionesRedistribucion.add(peticion)
	exmRedistribucion.signal()

	// Avisamos a un camión para la redistribución
	semCamion.signal()

	// Esperamos a que se resuelva
	semGestor.wait()
}
```
