# Sistema de Gestión Turística - San Antonio de Arredondo

Aplicación Java 17 + JavaFX + MySQL para la gestión turística municipal.

## Requisitos

- JDK 17 o superior
- MySQL Server 8.0 o compatible
- Maven 3.8 o superior

## Conexión a MySQL

La aplicación crea automáticamente la base de datos `turismo`, sus tablas, índices, configuraciones y datos iniciales.

Valores predeterminados:

- Host: `localhost`
- Puerto: `3306`
- Base de datos: `turismo`
- Usuario: `root`
- Contraseña: vacía

También se pueden definir:

- `MYSQL_HOST`
- `MYSQL_PORT`
- `MYSQL_DATABASE`
- `MYSQL_USER`
- `MYSQL_PASSWORD`

Como propiedades Java también se aceptan opciones como `-DMYSQL_USER=root -DMYSQL_PASSWORD=miclave`.

## Ejecución

```text
mvn clean javafx:run
```

Al iniciar:

1. Se conecta al servidor MySQL.
2. Crea la base de datos si no existe.
3. Crea y actualiza las tablas necesarias.
4. Inserta los datos iniciales si corresponden.
5. Muestra el login.

## Usuarios iniciales

| Usuario | Contraseña | Rol | Estado |
|---|---|---|---|
| admin | admin123 | Administrador | Activo |
| mgomez | 123456 | Operador | Activo |
| jperez | 123456 | Inspector | Activo |
| slopez | 123456 | Administrador | Activo |
| nlezcano | 123456 | Director | Activo |
| cruiz | 123456 | Operador | Inactivo |

## Roles y permisos

### Administrador

Puede administrar usuarios, turistas, alojamientos, catálogo y restauraciones de todas las categorías.

Puede acceder a toda la Papelera y utilizar la restauración individual o Restaurar todo.

### Operador

Puede crear y editar turistas.

En la Papelera solo visualiza y restaura turistas.

No puede modificar usuarios, alojamientos ni catálogo.

### Inspector

Puede editar alojamientos y catálogo.

En la Papelera solo visualiza y restaura alojamientos y catálogo.

No puede restaurar turistas ni usuarios.

### Director

Puede consultar el sistema según las restricciones de su rol, sin permisos de modificación de usuarios.

## Módulos

- Inicio y dashboard
- Turistas
- Actividades
- Atractivos
- Alojamientos
- Gastronomía
- Servicios
- Eventos
- Reportes
- Papelera
- Usuarios
- Configuración

## Turistas

Incluye búsqueda, creación, edición, consulta, eliminación lógica, restauración y paginación de 8 registros.

El tipo de documento puede ser DNI, Libreta Cívica, Cédula o Pasaporte y cada tipo utiliza su propia validación y formato.

El correo utiliza una parte local y un dominio seleccionado desde una lista de proveedores disponibles.

## Alojamientos

Incluye búsqueda, creación, edición, consulta, eliminación lógica, restauración y paginación de 8 registros.

## Catálogo

El catálogo contiene Actividades, Atractivos, Gastronomía, Servicios y Eventos.

Gastronomía posee una lista amplia de tipos de establecimientos y puestos de venta. Incluye Restaurante, Cafetería, Bar, Parrilla, Pizzería, Hamburguesería, Comida rápida, Panadería, Pastelería, Heladería, Puesto de helados, Pollería, Carnicería, Pescadería, Verdulería, Food truck, Puesto de shawarma, Puesto de choripán, Puesto de empanadas, Puesto de tacos, Puesto de panchos, Puesto de arepas, Puesto de crepes, Puesto de waffles, Casa de té, Cervecería, Vinoteca, Restobar, Mercado gastronómico, Patio de comidas, Comedor, Catering, Servicio de delivery, restaurantes especializados y muchas otras categorías.

La opción Otros permite añadir nuevas categorías de gastronomía desde el ABM. Las nuevas opciones se guardan en la configuración de MySQL y quedan disponibles en futuras altas y modificaciones.

## Papelera

La Papelera contiene registros eliminados lógicamente de usuarios, turistas, alojamientos y catálogo.

La vista muestra nombre, tipo, hora de eliminación, estado y detalle.

La fecha de eliminación se utiliza únicamente para la Papelera y no aparece en ningún ABM de lectura, creación o modificación.

Los administradores pueden restaurar cualquier registro y utilizar Restaurar todo. Los operadores solo acceden a turistas. Los inspectores solo acceden a alojamientos y catálogo.

## Reportes

Incluye un gráfico de turistas registrados por período.

Las barras y el indicador de la leyenda se adaptan al modo de accesibilidad activo.

## Accesibilidad

Incluye:

- Modo normal
- Mayor contraste
- Protanopía
- Deuteranopía
- Tritanopía
- Acromatopsia
- Modo claro y oscuro
- Tamaño de fuente configurable
- Español e inglés

Acromatopsia utiliza exclusivamente una escala de blanco, gris y negro.

Tritanopía utiliza tonos rosados para botones y selecciones y mantiene azules en los fondos de aplicación y grillas.

Deuteranopía utiliza tonos anaranjados para botones y selecciones y mantiene azules en los fondos de aplicación y grillas.

Protanopía utiliza tonos amarillentos para botones y selecciones y mantiene azules en los fondos de aplicación y grillas.

Los colores de reportes, campos obligatorios, notificaciones y elementos de selección se adaptan al modo activo.

## Interfaz

La aplicación utiliza una única fuente, Inter, en todo el programa.

La ventana principal inicia maximizada y centrada.

El login mantiene tamaño fijo y no se maximiza ni redimensiona.

El dashboard incluye reloj, calendario, resumen del sistema y accesos laterales.

Las grillas muestran hasta 8 registros por página y no utilizan barra vertical interna.

La búsqueda se adapta al ancho de la ventana y solo incorpora desplazamiento horizontal cuando una tabla realmente lo necesita.

## Idioma

La interfaz puede cambiar entre español e inglés.

Los nombres de categorías, tipos de documentos, alojamientos, gastronomía, calificaciones y demás valores conocidos se traducen al cambiar de idioma.

## Persistencia

Los cambios de usuarios, turistas, alojamientos, catálogo, estados, restauraciones, configuraciones y opciones de gastronomía se almacenan en MySQL.

La versión integrada unifica los módulos entregados en los ZIP dentro de la base `turismo`. El módulo de turistas utiliza las tablas `paises`, `provincias` y `tipos_documento`; los módulos de catálogo utilizan `actividades`, `puntos_turisticos`, `gastronomia`, `servicios`, `eventos` y `tipo_establecimiento`. Los datos existentes del catálogo general se migran automáticamente a las tablas específicas cuando la aplicación inicializa la base.

La aplicación usa una única conexión configurable mediante `MYSQL_HOST`, `MYSQL_PORT`, `MYSQL_DATABASE`, `MYSQL_USER` y `MYSQL_PASSWORD`, evitando las conexiones separadas y las bases `turismo_db`, `practica_turismo` y `turismo_arredondo` que utilizaban los proyectos originales de los módulos.

## Seguridad del prototipo

Las contraseñas del prototipo se mantienen en texto plano para conservar las credenciales de prueba originales. Para un sistema real se recomienda utilizar un hash seguro como BCrypt o Argon2.
