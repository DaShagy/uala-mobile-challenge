# Ualá City Mobile Challenge

Este repositorio contiene la resolución del "Mobile Challenge - Engineer" propuesto por Ualá.

## **Descripción del Desafío y Características Principales**

El objetivo de este desafío es evaluar las habilidades de resolución de problemas, el juicio de experiencia de usuario (UX) y la calidad del código, considerando un enfoque de nivel de producción.

El punto de partida es una lista de aproximadamente 200,000 ciudades en formato JSON, accesible desde un gist, donde cada entrada incluye el país, nombre, ID y coordenadas.

Las **características principales** a implementar fueron:

* **Gestión de Datos:** Descargar y procesar la lista de ciudades.
* **Búsqueda y Filtrado:**
    * Filtrar ciudades por un prefijo dado (ej. "Al" para "Alabama", "Albuquerque").
    * Optimizar para búsquedas rápidas.
    * Búsqueda insensible a mayúsculas y minúsculas.
    * Actualización de la lista al instante con cada caracter ingresado o eliminado en el filtro.
    * Capacidad de filtrar solo las ciudades marcadas como favoritas.
* **Visualización de la Lista:**
    * Mostrar las ciudades en una lista deslizable.
    * Ordenar alfabéticamente (ciudad primero, luego país).
    * Cada celda de ciudad debe mostrar el nombre de la ciudad y el código de país, coordenadas, y una opción para marcar/desmarcar como favorita.
* **Navegación y Detalles:**
    * Al tocar una ciudad, navegar a un mapa que muestre sus coordenadas.
    * Pantalla de información detallada para cada ciudad, incluyendo datos no mostrados en la lista principal y, opcionalmente, de una fuente adicional.
* **Experiencia de Usuario (UX) y Persistencia:**
    * Interfaz de usuario dinámica que se adapte a la orientación del dispositivo (pantallas separadas en vertical, una sola pantalla con dos paneles en horizontal).
    * Las ciudades favoritas deben recordarse entre los lanzamientos de la aplicación.
* **Calidad del Código:**
    * Implementar tests unitarios para el algoritmo de búsqueda.
    * Implementar tests de UI y unitarios para las pantallas desarrolladas.
    * Se prioriza el uso de Kotlin para Android, Jetpack Compose para la UI, y se evita el uso de librerías de terceros para la interfaz de usuario.

---

## **Etapas de Desarrollo del Ualá City Mobile Challenge**

El proyecto se desarrolló en varias etapas lógicas, cada una abordando un conjunto específico de funcionalidades y desafíos arquitectónicos:

### **1. Configuración Inicial y Funcionalidad Básica de Búsqueda y Favoritos**
Esta etapa se centró en sentar las bases del proyecto, establecer la estructura fundamental y desarrollar las funcionalidades esenciales de búsqueda y un primer acercamiento a los favoritos.

* **Commits involucrados:**
    * Kick-off
    * CMC-01
    * CMC-02
    * CMC-03
    * CMC-04
    * CMC-05
    * CMC-06

### **2. Implementación de Persistencia y Optimización Reactiva Inicial**
En esta fase, se introdujo la capacidad de persistir los datos de favoritos y se comenzaron a abordar los desafíos de rendimiento y reactividad en la interfaz de usuario, incluyendo un primer intento de paginación.

* **Commits involucrados:**
    * CMC-07
    * CMC-08
    * CMC-09
    * CMC-10
    * CMC-11

### **3. Integración de Inyección de Dependencias y Consumo de APIs Externas**
Esta etapa se centró en la mejora de la arquitectura con la integración de un framework de inyección de dependencias (Hilt) y en la incorporación de funcionalidades a través del consumo de APIs externas para mapas y detalles.

* **Commits involucrados:**
    * CMC-12
    * CMC-13
    * CMC-14
    * CMC-15
    * CMC-16
    * CMC-17
    * CMC-18

### **4. Diseño de UI Adaptativa y Refinamiento de Modelos de Datos**
En esta fase, se implementó la interfaz de usuario adaptativa para diferentes orientaciones (dos paneles) y se trabajó en la consistencia de los modelos de datos de UI, así como en la integración de información y recursos visuales adicionales.

* **Commits involucrados:**
    * CMC-19
    * CMC-20
    * CMC-21
    * CMC-22
    * CMC-23
    * CMC-24
    * CMC-25
    * CMC-26

### **5. Mejora de Filtros, Estilización y Robustez de APIs**
Esta etapa se enfocó en mejorar las capacidades de ordenamiento y filtrado, aplicar estilos a la interfaz de usuario, refinar el manejo de respuestas de APIs y asegurar la fiabilidad de las funciones de búsqueda y datos.

* **Commits involucrados:**
    * CMC-27
    * CMC-28
    * CMC-29
    * CMC-30
    * CMC-31
    * CMC-32
    * CMC-33
    * CMC-34
    * CMC-35

### **6. Refinamientos Arquitectónicos Finales y Estabilización de Funcionalidades Clave**
La etapa final se dedicó a las últimas refactorizaciones arquitectónicas importantes (especialmente en MVI), la implementación exitosa de la paginación y la garantía de la robustez y atomicidad de las operaciones críticas de la base de datos.

* **Commits involucrados:**
    * CMC-36
    * CMC-37
    * CMC-38
    * CMC-39
    * CMC-40
    * CMC-41
    * CMC-42