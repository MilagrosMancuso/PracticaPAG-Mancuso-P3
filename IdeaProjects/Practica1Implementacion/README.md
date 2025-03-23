# Gestión de un Sistema de Transporte de Bicicletas Compartidas
## Primera práctica: Semáforos [![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

## Descripción del Problema
Se desea implementar un sistema de bicicletas compartidas en una ciudad. El sistema debe permitir a los usuarios tomar y devolver bicicletas en diferentes estaciones distribuidas por la ciudad. Además de los usuarios, habrá técnicos de mantenimiento y camiones de redistribución que interactuarán en el sistema. También se introducen estaciones de carga para bicicletas eléctricas y un punto de reparación. Las bicicletas, estaciones, estaciones de carga y punto de reparación deben ser gestionados de manera concurrente para asegurar que los recursos sean utilizados de manera eficiente y segura. Se utilizarán semáforos como herramienta de solución para coordinar los diferentes procesos así como el acceso seguro a los recursos compartidos. Además, habrá un proceso gestor que coordinará a los diferentes procesos en el sistema para garantizar un uso eficiente de los recursos.

## Objetivos
- Diseñar un sistema concurrente utilizando semáforos para gestionar el acceso a los recursos.
- Simular el comportamiento de usuarios, técnicos de mantenimiento y camiones de redistribución en un sistema de bicicletas compartidas.
- Diseñar un proceso gestor que coordine los diferentes procesos del sistema para un uso eficiente de los recursos compartidos.

------
## Soluciones

En los siguientes documentos se encuentran las soluciones propuestas para esta práctica

- [**Solución de Análisis y Diseño**](SolucionAnalisis.md)
- [**Insturcciones para la Implementación**](Implementacion.md)

-----

## Requisitos
1. **Estaciones de Bicicletas**:
   - Cada estación tiene un número limitado de bicicletas y un número limitado de espacios para bicicletas.
   - Las bicicletas que no estén disponibles para el préstamo también están ocupando uno de esos espacios disponibles de la estación.
   - Cada vez que una bicicleta sea devuelta a la estación por parte de un usuario deberá comprobarse si necesita mantenimiento.
   - Las bicicletas de la estación estarán al 100% de su carga.

2. **Estaciones de Carga**:
   - Las estaciones de carga permiten cargar bicicletas eléctricas.
   - Cada estación de carga tiene un número limitado de puntos de carga.
   - Las bicicletas deben estar conectadas a un punto de carga para ser cargadas.

3. **Punto de Reparación**
	- Una vez que los técnicos de mantenimiento reparan las bicicletas serán devueltas a este punto.
	- Los camiones de redistribución serán los encargados de distribuir las bicicletas de vuelta a las estaciones.

5. **Usuarios**:
   - Los usuarios tienen que hacer una propuesta al gestor para la solicitud de una bicicleta.
	   - Deberán indicar la estación de origen y destino del viaje para que el gestor confirme la disponibilidad del viaje.
   - Mientras no reciba la confirmación tendrá que estar esperando.
   - Se dirige a la estación establecida para recoger la bicicleta y comenzará la simulación del viaje. 
   - Deberá dirigirse a una estación de carga si la bicicleta lo necesitara para poder completar el viaje.

6. **Técnicos de Mantenimiento**:
   - Los técnicos de mantenimiento estarán a la espera de recibir órdenes de mantenimiento por parte del gestor.
   - Se dirigirán a la estación de bicicletas establecida en la órden para recogerlas.
   - Entregará las bicicletas en el punto de reparación.

7. **Camiones de Redistribución**:
   - Los camiones pueden mover bicicletas entre estaciones para balancear la disponibilidad, así como del punto de reparación.
   - Cada camión puede transportar un número limitado de bicicletas.
   - Los camiones deben esperar la solicitud por parte del gestor para saber el lugar donde recoger las bicicletas y donde deberán entregarlas.

8. **Proceso Gestor**:
   - El proceso gestor se encarga de coordinar el trabajo de todos los procesos incluidos en el sistema.
   - El gestor verifica el estado de las estaciones, punto de reparación y estaciones de carga, para generar las órdenes de reparación y redistribución correspondientes.
   - El gestor asegura que los técnicos y camiones no estén esperando en lugares donde no podrán realizar su trabajo.
   - El gestor también resolverá las peticiones de transporte solicitadas por los usuarios asegurándose que puede completar su viaje porque garantizará el espacio de devolución de la bicicleta.

9. **Semáforos**:
   - Se deberán definir los semáforos necesarios para garantizar las operaciones de todos los procesos del sistema.
   - Es la única herramienta que podrá utilizarse para la solución concurrente del problema.
   - Es importante indicar el valor de inicialización del semáforo.

