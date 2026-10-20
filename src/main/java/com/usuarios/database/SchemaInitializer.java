package com.usuarios.database;

import java.sql.*;


public final class SchemaInitializer {
    private SchemaInitializer() {}
    public static void init(DatabaseConnection db) throws SQLException {
        try (Connection c = db.getConnection(); Statement s = c.createStatement()) {
            s.executeUpdate("""
        CREATE TABLE IF NOT EXISTS turistas (
          id_turista INT AUTO_INCREMENT PRIMARY KEY,
          nombre VARCHAR(80) NOT NULL,
          apellido VARCHAR(80) NOT NULL,
          tipo_documento VARCHAR(40) NULL,
          numero_documento VARCHAR(40) NULL,
          fecha_nacimiento DATE NOT NULL,
          provincia VARCHAR(100) NOT NULL,
          pais VARCHAR(100) NOT NULL,
          cantidad_personas INT NOT NULL DEFAULT 1,
          telefono VARCHAR(40) NULL,
          email VARCHAR(160) NULL,
          observaciones TEXT NULL,
          fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
          activo BOOLEAN NOT NULL DEFAULT TRUE,
          fecha_eliminacion DATETIME NULL,

          UNIQUE KEY uq_turista_documento(
              tipo_documento,
              numero_documento
          ),

          INDEX idx_turista_fecha(fecha_registro),
          INDEX idx_turista_activo(activo)

        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
        """);
            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS alojamientos (
                  id INT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(120) NOT NULL,
                  tipo VARCHAR(80) NOT NULL, categoria VARCHAR(80) NOT NULL, direccion VARCHAR(180) NOT NULL,
                  telefono VARCHAR(40), capacidad INT NOT NULL, nombre_dueno VARCHAR(120) NOT NULL,
                  dni_dueno VARCHAR(40) NOT NULL, descripcion TEXT, amenities TEXT, foto_url VARCHAR(500),
                  estado ENUM('Activo','Inactivo') NOT NULL DEFAULT 'Activo',
                  fecha_eliminacion DATETIME NULL,
                  fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                  INDEX idx_aloj_estado(estado)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS catalogo (
                  id INT AUTO_INCREMENT PRIMARY KEY, modulo VARCHAR(40) NOT NULL,
                  nombre VARCHAR(140) NOT NULL, tipo VARCHAR(100) NOT NULL, detalle VARCHAR(255) NOT NULL,
                  direccion VARCHAR(100) NOT NULL DEFAULT '',
                  extra VARCHAR(255), estado ENUM('Activo','Inactivo') NOT NULL DEFAULT 'Activo',
                  fecha_eliminacion DATETIME NULL,
                  fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                  INDEX idx_cat_modulo(modulo), INDEX idx_cat_estado(estado), UNIQUE KEY uq_catalogo_modulo_nombre(modulo,nombre)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS paises (
                  id_pais INT AUTO_INCREMENT PRIMARY KEY,
                  nombre_pais VARCHAR(100) NOT NULL UNIQUE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS provincias (
                  id_provincia INT AUTO_INCREMENT PRIMARY KEY,
                  nombre_provincia VARCHAR(100) NOT NULL,
                  id_pais INT NOT NULL,
                  UNIQUE KEY uq_provincia_pais(nombre_provincia,id_pais),
                  CONSTRAINT fk_provincia_pais FOREIGN KEY(id_pais) REFERENCES paises(id_pais) ON UPDATE CASCADE ON DELETE RESTRICT
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS tipos_documento (
                  id_tipo_documento INT AUTO_INCREMENT PRIMARY KEY,
                  nombre_tipo VARCHAR(60) NOT NULL UNIQUE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS actividades (
                  id INT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(140) NOT NULL, descripcion VARCHAR(255) NOT NULL DEFAULT '',
                  duracion VARCHAR(100) NOT NULL DEFAULT '', precio VARCHAR(100) NOT NULL DEFAULT '', horario VARCHAR(100) NOT NULL DEFAULT '',
                  estado VARCHAR(30) NOT NULL DEFAULT 'Activo', eliminado BOOLEAN NOT NULL DEFAULT FALSE, fecha_eliminacion DATETIME NULL,
                  fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, INDEX idx_act_elim(eliminado)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS puntos_turisticos (
                  id INT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(140) NOT NULL, tipo VARCHAR(100) NOT NULL DEFAULT '', ubicacion VARCHAR(180) NOT NULL DEFAULT '',
                  descripcion VARCHAR(255) NOT NULL DEFAULT '', horario VARCHAR(100) NOT NULL DEFAULT '', estado VARCHAR(30) NOT NULL DEFAULT 'Activo', eliminado BOOLEAN NOT NULL DEFAULT FALSE,
                  fecha_eliminacion DATETIME NULL, fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, INDEX idx_pt_elim(eliminado)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS tipo_establecimiento (
                  id INT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(120) NOT NULL UNIQUE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS gastronomia (
                  id INT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(140) NOT NULL, tipo_cocina VARCHAR(100) NOT NULL DEFAULT '', especialidad VARCHAR(255) NOT NULL DEFAULT '',
                  tipo_id INT NULL, estado VARCHAR(30) NOT NULL DEFAULT 'Activo', eliminado BOOLEAN NOT NULL DEFAULT FALSE, fecha_eliminacion DATETIME NULL,
                  fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, INDEX idx_gast_elim(eliminado)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS servicios (
                  id INT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(140) NOT NULL, tipo VARCHAR(100) NOT NULL DEFAULT '', direccion VARCHAR(180) NOT NULL DEFAULT '',
                  telefono VARCHAR(40) NOT NULL DEFAULT '', horario VARCHAR(100) NOT NULL DEFAULT '', estado VARCHAR(30) NOT NULL DEFAULT 'Activo', eliminado BOOLEAN NOT NULL DEFAULT FALSE,
                  fecha_eliminacion DATETIME NULL, fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, INDEX idx_serv_elim(eliminado)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS eventos (
                  id INT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(140) NOT NULL, fecha VARCHAR(40) NOT NULL DEFAULT '', horario VARCHAR(100) NOT NULL DEFAULT '',
                  lugar VARCHAR(180) NOT NULL DEFAULT '', descripcion VARCHAR(255) NOT NULL DEFAULT '', estado VARCHAR(30) NOT NULL DEFAULT 'Activo', eliminado BOOLEAN NOT NULL DEFAULT FALSE,
                  fecha_eliminacion DATETIME NULL, fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, INDEX idx_event_elim(eliminado)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS configuracion (
                  clave VARCHAR(80) PRIMARY KEY, valor VARCHAR(255) NOT NULL
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
        }
        seed(db);
    }
    private static void seed(DatabaseConnection db) throws SQLException {
        try (Connection c=db.getConnection()) {
            try { c.createStatement().executeUpdate("ALTER TABLE catalogo ADD COLUMN direccion VARCHAR(100) NOT NULL DEFAULT ''"); } catch (SQLException ignored) {}
            try { c.createStatement().executeUpdate("DELETE a FROM catalogo a JOIN catalogo b ON a.modulo=b.modulo AND a.nombre=b.nombre AND a.id>b.id"); } catch (SQLException ignored) {}
            try { c.createStatement().executeUpdate("ALTER TABLE catalogo ADD UNIQUE KEY uq_catalogo_modulo_nombre(modulo,nombre)"); } catch (SQLException ignored) {}
            try { c.createStatement().executeUpdate("ALTER TABLE turistas ADD COLUMN fecha_eliminacion DATETIME NULL"); } catch (SQLException ignored) {}
            try { c.createStatement().executeUpdate("ALTER TABLE turistas ADD COLUMN id_tipo_documento INT NULL"); } catch (SQLException ignored) {}
            try { c.createStatement().executeUpdate("ALTER TABLE turistas ADD COLUMN id_provincia INT NULL"); } catch (SQLException ignored) {}
            try { c.createStatement().executeUpdate("ALTER TABLE turistas ADD COLUMN id_pais INT NULL"); } catch (SQLException ignored) {}
            try { c.createStatement().executeUpdate("CREATE INDEX idx_turista_tipo_doc ON turistas(id_tipo_documento)"); } catch (SQLException ignored) {}
            try { c.createStatement().executeUpdate("CREATE INDEX idx_turista_provincia ON turistas(id_provincia)"); } catch (SQLException ignored) {}
            try { c.createStatement().executeUpdate("CREATE INDEX idx_turista_pais ON turistas(id_pais)"); } catch (SQLException ignored) {}
            try { c.createStatement().executeUpdate("ALTER TABLE turistas ADD CONSTRAINT fk_turista_tipo_doc FOREIGN KEY(id_tipo_documento) REFERENCES tipos_documento(id_tipo_documento) ON UPDATE CASCADE ON DELETE SET NULL"); } catch (SQLException ignored) {}
            try { c.createStatement().executeUpdate("ALTER TABLE turistas ADD CONSTRAINT fk_turista_provincia FOREIGN KEY(id_provincia) REFERENCES provincias(id_provincia) ON UPDATE CASCADE ON DELETE SET NULL"); } catch (SQLException ignored) {}
            try { c.createStatement().executeUpdate("ALTER TABLE turistas ADD CONSTRAINT fk_turista_pais FOREIGN KEY(id_pais) REFERENCES paises(id_pais) ON UPDATE CASCADE ON DELETE SET NULL"); } catch (SQLException ignored) {}
            for(String t:new String[]{"actividades","puntos_turisticos","gastronomia","servicios","eventos"}){try{c.createStatement().executeUpdate("ALTER TABLE "+t+" ADD COLUMN fecha_eliminacion DATETIME NULL");}catch(SQLException ignored){}}
            try { c.createStatement().executeUpdate("ALTER TABLE alojamientos ADD COLUMN fecha_eliminacion DATETIME NULL"); } catch (SQLException ignored) {}
            try { c.createStatement().executeUpdate("ALTER TABLE catalogo ADD COLUMN fecha_eliminacion DATETIME NULL"); } catch (SQLException ignored) {}
            try { c.createStatement().executeUpdate("UPDATE turistas SET fecha_eliminacion=fecha_registro WHERE activo=FALSE AND fecha_eliminacion IS NULL"); } catch (SQLException ignored) {}
            try { c.createStatement().executeUpdate("UPDATE alojamientos SET fecha_eliminacion=fecha_registro WHERE estado='Inactivo' AND fecha_eliminacion IS NULL"); } catch (SQLException ignored) {}
            try { c.createStatement().executeUpdate("UPDATE catalogo SET fecha_eliminacion=fecha_registro WHERE estado='Inactivo' AND fecha_eliminacion IS NULL"); } catch (SQLException ignored) {}
            try (PreparedStatement p=c.prepareStatement("INSERT IGNORE INTO tipos_documento(nombre_tipo) VALUES (?)")) { for(String x:new String[]{"Documento","Libreta Cívica","DNI","Pasaporte","Cédula"}) { p.setString(1,x); p.addBatch(); } p.executeBatch(); }
            try (PreparedStatement p=c.prepareStatement("INSERT IGNORE INTO paises(nombre_pais) VALUES (?)")) { for(String x:new String[]{"Argentina","Brasil","Chile","Uruguay","Paraguay","Bolivia","Perú","España","Italia","Estados Unidos"}) { p.setString(1,x); p.addBatch(); } p.executeBatch(); }
            try (Statement st=c.createStatement()) { st.executeUpdate("INSERT IGNORE INTO provincias(nombre_provincia,id_pais) SELECT 'Córdoba',id_pais FROM paises WHERE nombre_pais='Argentina'"); st.executeUpdate("INSERT IGNORE INTO provincias(nombre_provincia,id_pais) SELECT 'Buenos Aires',id_pais FROM paises WHERE nombre_pais='Argentina'"); st.executeUpdate("INSERT IGNORE INTO provincias(nombre_provincia,id_pais) SELECT 'Santa Fe',id_pais FROM paises WHERE nombre_pais='Argentina'"); st.executeUpdate("INSERT IGNORE INTO provincias(nombre_provincia,id_pais) SELECT 'Mendoza',id_pais FROM paises WHERE nombre_pais='Argentina'"); st.executeUpdate("INSERT IGNORE INTO provincias(nombre_provincia,id_pais) SELECT 'San Luis',id_pais FROM paises WHERE nombre_pais='Argentina'"); st.executeUpdate("UPDATE turistas t JOIN tipos_documento d ON LOWER(d.nombre_tipo)=LOWER(t.tipo_documento) SET t.id_tipo_documento=d.id_tipo_documento WHERE t.id_tipo_documento IS NULL"); st.executeUpdate("UPDATE turistas t JOIN paises p ON LOWER(p.nombre_pais)=LOWER(t.pais) SET t.id_pais=p.id_pais WHERE t.id_pais IS NULL"); st.executeUpdate("UPDATE turistas t JOIN provincias pr ON LOWER(pr.nombre_provincia)=LOWER(t.provincia) JOIN paises p ON p.id_pais=pr.id_pais AND p.id_pais=t.id_pais SET t.id_provincia=pr.id_provincia WHERE t.id_provincia IS NULL"); }
            try (Statement st=c.createStatement()) {
                st.executeUpdate("INSERT INTO actividades(nombre,descripcion,duracion,precio,horario,estado,eliminado,fecha_registro) SELECT nombre,detalle,extra,'','',estado,IF(estado='Inactivo',1,0),fecha_registro FROM catalogo WHERE modulo='Actividades' AND NOT EXISTS (SELECT 1 FROM actividades a WHERE a.nombre=catalogo.nombre)");
                st.executeUpdate("INSERT INTO puntos_turisticos(nombre,tipo,ubicacion,descripcion,horario,estado,eliminado,fecha_registro) SELECT nombre,tipo,direccion,detalle,extra,estado,IF(estado='Inactivo',1,0),fecha_registro FROM catalogo WHERE modulo='Atractivos' AND NOT EXISTS (SELECT 1 FROM puntos_turisticos a WHERE a.nombre=catalogo.nombre)");
                st.executeUpdate("INSERT INTO gastronomia(nombre,tipo_cocina,especialidad,estado,eliminado,fecha_registro) SELECT nombre,tipo,detalle,estado,IF(estado='Inactivo',1,0),fecha_registro FROM catalogo WHERE modulo='Gastronomía' AND NOT EXISTS (SELECT 1 FROM gastronomia a WHERE a.nombre=catalogo.nombre)");
                st.executeUpdate("INSERT INTO servicios(nombre,tipo,direccion,telefono,horario,estado,eliminado,fecha_registro) SELECT nombre,tipo,direccion,extra,detalle,estado,IF(estado='Inactivo',1,0),fecha_registro FROM catalogo WHERE modulo='Servicios' AND NOT EXISTS (SELECT 1 FROM servicios a WHERE a.nombre=catalogo.nombre)");
                st.executeUpdate("INSERT INTO eventos(nombre,fecha,horario,lugar,descripcion,estado,eliminado,fecha_registro) SELECT nombre,extra,'',direccion,detalle,estado,IF(estado='Inactivo',1,0),fecha_registro FROM catalogo WHERE modulo='Eventos' AND NOT EXISTS (SELECT 1 FROM eventos a WHERE a.nombre=catalogo.nombre)");
            }
            try (PreparedStatement p=c.prepareStatement("INSERT IGNORE INTO tipo_establecimiento(nombre) VALUES (?)")) { for(String x:new String[]{"Albergue","Apart Hotel","Cabaña","Camping","Complejo Turístico","Departamento / Casa","Glamping","Hostel","Hostería","Hotel","Posada","Residencial"}) { p.setString(1,x); p.addBatch(); } p.executeBatch(); }
            try (PreparedStatement p=c.prepareStatement("INSERT IGNORE INTO configuracion(clave,valor) VALUES (?,?)")) {
                String[][] vals={{"tema","claro"},{"tamano_fuente","1.0"},{"idioma","es"},{"accesibilidad","normal"}};
                for(String[] v:vals){p.setString(1,v[0]);p.setString(2,v[1]);p.addBatch();} p.executeBatch();
            }
            try (PreparedStatement p=c.prepareStatement("INSERT INTO turistas(nombre,apellido,tipo_documento,numero_documento,fecha_nacimiento,provincia,pais,telefono,email,observaciones,fecha_registro) SELECT ?,?,?,?,?,?,?,?,?,?,? FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM turistas WHERE numero_documento=?)")) {
                String[][] rows={{"Lucía","Fernández","DNI","32111222","1990-03-12","Córdoba","Argentina","3515551001","lucia@example.com","Visita familiar","2026-01-15 10:00:00"},{"Martín","Gómez","DNI","28444555","1984-07-21","Buenos Aires","Argentina","115551002","martin@example.com","Turismo aventura","2026-02-10 11:30:00"},{"Camila","Rodríguez","DNI","35666777","1991-11-05","Santa Fe","Argentina","3425551003","camila@example.com","Consulta gastronómica","2026-03-22 14:00:00"},{"Santiago","Pérez","DNI","30123456","1986-01-18","Mendoza","Argentina","2615551004","santiago@example.com","Alojamiento","2026-04-05 09:15:00"},{"Valentina","López","DNI","40999888","1998-09-27","Córdoba","Argentina","3515551005","valentina@example.com","Evento","2026-05-18 16:20:00"},{"Tomás","Martínez","DNI","33111222","1989-12-02","Buenos Aires","Argentina","115551006","tomas@example.com","Excursión","2026-06-08 12:10:00"},{"Ana","Silva","DNI","29555666","1982-04-14","Río Negro","Argentina","2995551007","ana@example.com","Naturaleza","2026-07-27 15:40:00"},{"Diego","Acosta","DNI","37777888","1993-06-30","San Luis","Argentina","2665551008","diego@example.com","Fin de semana","2026-08-12 13:25:00"},{"Florencia","Molina","DNI","41888999","2000-02-17","Córdoba","Argentina","3515551009","florencia@example.com","Turismo cultural","2026-02-24 10:15:00"},{"Bruno","Castro","DNI","38999111","1995-05-09","Entre Ríos","Argentina","3435551010","bruno@example.com","Consulta alojamiento","2026-03-11 12:45:00"},{"Julieta","Navarro","DNI","40222333","1997-08-26","Córdoba","Argentina","3515551011","julieta@example.com","Gastronomía","2026-04-19 18:10:00"},{"Mateo","Vega","DNI","36555444","1992-10-13","San Juan","Argentina","2645551012","mateo@example.com","Aventura","2026-05-07 09:30:00"},{"Renata","Sosa","DNI","43111555","2001-01-28","Mendoza","Argentina","2615551013","renata@example.com","Eventos","2026-06-21 14:50:00"},{"Nicolás","Ferreyra","DNI","34222666","1988-11-16","Córdoba","Argentina","3515551014","nicolas@example.com","Naturaleza","2026-07-05 11:20:00"},{"Sofía","Herrera","DNI","39777700","1996-03-31","Buenos Aires","Argentina","115551015","sofia@example.com","Fin de semana","2026-08-23 16:05:00"}};
                for(String[] r:rows){for(int i=0;i<11;i++)p.setString(i+1,r[i]);p.setString(12,r[3]);p.addBatch();}p.executeBatch();
            }
            try (PreparedStatement p=c.prepareStatement("INSERT IGNORE INTO catalogo(modulo,nombre,tipo,detalle,direccion,extra,estado) VALUES(?,?,?,?,?,?,?)")) {
                String[][] rows={
                  {"Actividades","Senderismo Cerro Negro","Recreativa","Caminata guiada","Camino al Cerro Negro","3 horas","Activo"},
                  {"Actividades","Paseo en Kayak","Recreativa","Recorrido por el dique","Costanera del Dique","2 horas","Activo"},
                  {"Atractivos","Cerro Negro","Natural","Ruta 5 km 10","Acceso por Ruta Provincial","Punto panorámico","Activo"},
                  {"Atractivos","Dique San Antonio","Natural","Costanera Sur","Costanera Sur","Actividades acuáticas","Activo"},
                  {"Gastronomía","El Asador","Parrilla","Asado criollo","Av. Principal 120","Almuerzo y cena","Activo"},
                  {"Servicios","Cuartel de Bomberos","Emergencia","Atención de emergencias","Calle del Fuego 456","100 - 24h","Activo"},
                  {"Eventos","Fiesta del Dique","Evento","15/12/2026","Costanera Sur","Entrada libre","Activo"}
                };
                for(String[] r:rows){p.setString(1,r[0]);p.setString(2,r[1]);p.setString(3,r[2]);p.setString(4,r[3]);p.setString(5,r[4]);p.setString(6,r[5]);p.setString(7,r[6]);p.addBatch();} p.executeBatch();
            }
            try (PreparedStatement p=c.prepareStatement("INSERT IGNORE INTO alojamientos(nombre,tipo,categoria,direccion,telefono,capacidad,nombre_dueno,dni_dueno,descripcion,amenities,foto_url,estado) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)")) {
                String[][] rows={{"Cabañas Los Aromos","Cabaña","3 estrellas","Camino al Dique 120","3515552001","6","Laura Benítez","30111222","Cabaña familiar con vista a las sierras","WiFi, cochera, parrilla","","Activo"},{"Hotel del Valle","Hotel","4 estrellas","Av. Costanera 450","3515552002","40","Roberto Molina","28999111","Hotel céntrico","Desayuno, WiFi, pileta","","Activo"},{"Camping El Sauce","Camping","2 estrellas","Ruta Provincial km 8","3515552003","80","Marina Torres","32555111","Camping junto al río","Baños, fogones, duchas","","Activo"},{"Hostería Las Sierras","Hostería","3 estrellas","Calle Los Pinos 88","3515552004","18","Gabriel Acuña","27888444","Hostería pequeña","Desayuno, WiFi","","Activo"}};
                for(String[] r:rows){for(int i=0;i<12;i++)p.setString(i+1,r[i]);p.addBatch();}p.executeBatch();
            }
        }
        seedMoreExamples(db);
    }

    private static void seedMoreExamples(DatabaseConnection db) throws SQLException {
        try (Connection c=db.getConnection()) {
            try (PreparedStatement p=c.prepareStatement("INSERT IGNORE INTO turistas(nombre,apellido,tipo_documento,numero_documento,fecha_nacimiento,provincia,pais,telefono,email,observaciones,fecha_registro) VALUES(?,?,?,?,?,?,?,?,?,?,?)")) {
                String[][] rows={
                  {"Agustina","Rossi","Pasaporte","P10001","1994-02-14","Córdoba","Argentina","3515553001","agustina.rossi@example.com","Turismo de naturaleza","2026-01-08 09:20:00"},
                  {"Federico","Suárez","DNI","45001001","1997-06-18","Santa Fe","Argentina","3425553002","federico.suarez@example.com","Escapada de fin de semana","2026-01-19 15:10:00"},
                  {"Carolina","Méndez","DNI","45001002","1990-10-02","Mendoza","Argentina","2615553003","carolina.mendez@example.com","Visita familiar","2026-02-04 10:15:00"},
                  {"Gonzalo","Paz","DNI","45001003","1987-03-27","Buenos Aires","Argentina","1155553004","gonzalo.paz@example.com","Turismo aventura","2026-02-16 12:40:00"},
                  {"Milagros","Acosta","Cédula","45001004","1999-08-09","Entre Ríos","Argentina","3435553005","milagros.acosta@example.com","Consulta gastronómica","2026-02-27 17:05:00"},
                  {"Ramiro","Navarro","DNI","45001005","1985-12-11","San Luis","Argentina","2665553006","ramiro.navarro@example.com","Alojamiento","2026-03-03 09:05:00"},
                  {"Paula","Ferreyra","Pasaporte","P10007","1993-05-22","Córdoba","Argentina","3515553007","paula.ferreyra@example.com","Senderismo","2026-03-14 11:35:00"},
                  {"Leandro","Vega","DNI","45001008","1991-09-30","San Juan","Argentina","2645553008","leandro.vega@example.com","Evento local","2026-03-29 18:20:00"},
                  {"Mariana","Sosa","DNI","45001009","1988-01-17","Río Negro","Argentina","2995553009","mariana.sosa@example.com","Naturaleza","2026-04-02 08:50:00"},
                  {"Ignacio","Luna","DNI","45001010","1996-04-26","Córdoba","Argentina","3515553010","ignacio.luna@example.com","Kayak","2026-04-11 13:15:00"},
                  {"Belén","Quiroga","DNI","45001011","1995-07-05","Buenos Aires","Argentina","1155553011","belen.quiroga@example.com","Gastronomía","2026-04-28 19:10:00"},
                  {"Facundo","Molina","DNI","45001012","1983-11-19","Mendoza","Argentina","2615553012","facundo.molina@example.com","Visita familiar","2026-05-05 10:05:00"},
                  {"Abril","Castro","Pasaporte","P10013","2000-04-12","Santa Fe","Argentina","3425553013","abril.castro@example.com","Turismo cultural","2026-05-16 14:30:00"},
                  {"Joaquín","Herrera","DNI","45001014","1992-02-28","Córdoba","Argentina","3515553014","joaquin.herrera@example.com","Aventura","2026-05-25 16:40:00"},
                  {"Micaela","Torres","DNI","45001015","1998-10-21","Entre Ríos","Argentina","3435553015","micaela.torres@example.com","Fin de semana","2026-06-02 09:45:00"},
                  {"Franco","Benítez","DNI","45001016","1989-06-13","San Luis","Argentina","2665553016","franco.benitez@example.com","Alojamiento","2026-06-12 12:05:00"},
                  {"Lara","Domínguez","DNI","45001017","1997-03-06","Córdoba","Argentina","3515553017","lara.dominguez@example.com","Evento","2026-06-26 17:25:00"},
                  {"Matías","Romero","DNI","45001018","1986-09-14","Buenos Aires","Argentina","1155553018","matias.romero@example.com","Naturaleza","2026-07-01 10:30:00"},
                  {"Josefina","Silva","Cédula","45001019","1994-12-20","Mendoza","Argentina","2615553019","josefina.silva@example.com","Gastronomía","2026-07-13 13:50:00"},
                  {"Esteban","Ponce","DNI","45001020","1981-05-08","Córdoba","Argentina","3515553020","esteban.ponce@example.com","Senderismo","2026-07-24 15:15:00"},
                  {"Victoria","Campos","DNI","45001021","1999-01-31","Río Negro","Argentina","2995553021","victoria.campos@example.com","Kayak","2026-08-03 09:25:00"},
                  {"Nicolás","Ibarra","DNI","45001022","1990-08-16","Santa Fe","Argentina","3425553022","nicolas.ibarra@example.com","Alojamiento","2026-08-09 11:45:00"},
                  {"Elena","Ríos","Pasaporte","P10023","1987-12-04","Buenos Aires","Argentina","1155553023","elena.rios@example.com","Turismo cultural","2026-08-17 14:20:00"},
                  {"Agustín","Salas","DNI","45001024","1995-10-29","Córdoba","Argentina","3515553024","agustin.salas@example.com","Fin de semana","2026-08-29 18:05:00"},
                  {"Pilar","Suárez","DNI","45001025","2001-06-07","San Juan","Argentina","2645553025","pilar.suarez@example.com","Naturaleza","2026-09-02 10:10:00"},
                  {"Damián","Correa","DNI","45001026","1984-04-25","Córdoba","Argentina","3515553026","damian.correa@example.com","Gastronomía","2026-09-06 12:35:00"},
                  {"Cecilia","Varela","DNI","45001027","1993-09-18","Mendoza","Argentina","2615553027","cecilia.varela@example.com","Aventura","2026-09-11 16:10:00"},
                  {"Lisandro","Rey","DNI","45001028","1998-02-03","Entre Ríos","Argentina","3435553028","lisandro.rey@example.com","Eventos","2026-09-15 19:00:00"},
                  {"Natalia","Giménez","Cédula","45001029","1989-07-12","Córdoba","Argentina","3515553029","natalia.gimenez@example.com","Visita familiar","2026-09-20 09:35:00"}
                };
                for(String[] r:rows){for(int i=0;i<11;i++)p.setString(i+1,r[i]);p.addBatch();} p.executeBatch();
            }
            try (PreparedStatement p=c.prepareStatement("INSERT IGNORE INTO catalogo(modulo,nombre,tipo,detalle,direccion,extra,estado) VALUES(?,?,?,?,?,?,?)")) {
                String[][] rows={
                  {"Actividades","Circuito de Bicicleta","Recreativa","Recorrido por la costanera","Costanera Norte","2 horas","Activo"},
                  {"Actividades","Cabalgata Serrana","Aventura","Cabalgata guiada","Sendero Las Sierras","3 horas","Activo"},
                  {"Actividades","Avistaje de Aves","Naturaleza","Salida de observación","Reserva Municipal","2 horas","Activo"},
                  {"Actividades","Clase de Fotografía","Cultural","Taller al aire libre","Centro Cultural","90 minutos","Activo"},
                  {"Actividades","Paseo Familiar","Recreativa","Circuito accesible","Parque Municipal","1 hora","Activo"},
                  {"Atractivos","Balneario Municipal","Natural","Río y áreas verdes","Costanera Norte","Guardavidas en temporada","Activo"},
                  {"Atractivos","Mirador del Valle","Natural","Vista panorámica","Camino de las Sierras","Acceso libre","Activo"},
                  {"Atractivos","Reserva Los Algarrobos","Natural","Senderos interpretativos","Ruta Provincial","Fauna autóctona","Activo"},
                  {"Atractivos","Plaza Central","Urbano","Espacio recreativo","Centro","Juegos y feria","Activo"},
                  {"Atractivos","Centro Cultural","Cultural","Muestras y talleres","Av. Principal 250","Agenda mensual","Activo"},
                  {"Gastronomía","La Esquina Serrana","Restaurante","Comida regional","San Martín 85","Menú familiar","Activo"},
                  {"Gastronomía","Sabores del Valle","Cafetería","Meriendas artesanales","Belgrano 140","Pastelería","Activo"},
                  {"Gastronomía","Rincón Criollo","Parrilla","Carnes y empanadas","Mitre 210","Reservas","Activo"},
                  {"Servicios","Centro de Información Turística","Información","Asesoramiento al visitante","Av. Costanera 50","Lun-Dom 09:00-18:00","Activo"},
                  {"Servicios","Farmacia del Valle","Salud","Atención farmacéutica","San Martín 330","Turno extendido","Activo"},
                  {"Servicios","Terminal de Ómnibus","Transporte","Información de viajes","Ruta 5","Horarios diarios","Activo"},
                  {"Servicios","Policía Turística","Seguridad","Asistencia al visitante","Sarmiento 120","101","Activo"},
                  {"Eventos","Feria de Artesanos","Feria","Productos regionales","Plaza Central","Sábados","Activo"},
                  {"Eventos","Festival de Sabores","Gastronómico","Cocina local","Costanera Sur","Octubre","Activo"},
                  {"Eventos","Semana del Turismo","Cultural","Actividades especiales","Varios espacios","Septiembre","Activo"},
                  {"Eventos","Maratón Serrana","Deportivo","Carrera recreativa","Costanera Norte","Inscripción previa","Activo"}
                };
                for(String[] r:rows){for(int i=0;i<7;i++)p.setString(i+1,r[i]);p.addBatch();} p.executeBatch();
            }
            try (PreparedStatement p=c.prepareStatement("INSERT INTO alojamientos(nombre,tipo,categoria,direccion,telefono,capacidad,nombre_dueno,dni_dueno,descripcion,amenities,foto_url,estado) SELECT ?,?,?,?,?,?,?,?,?,?,?,? FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM alojamientos WHERE nombre=?)")) {
                String[][] rows={
                  {"Posada del Río","Posada","3 estrellas","Costanera Norte 75","3515554001","22","Claudia Roldán","30999101","Posada tranquila junto al río","WiFi, desayuno, cochera","","Activo"},
                  {"Cabañas El Mirador","Cabaña","4 estrellas","Camino del Mirador 44","3515554002","12","Sergio Peralta","28777102","Cabañas con vista a las sierras","WiFi, parrilla, pileta","","Activo"},
                  {"Hostel Punto Joven","Hostel","2 estrellas","Belgrano 312","3515554003","28","Daniela Vera","33988103","Hostel céntrico para jóvenes","WiFi, cocina compartida","","Activo"},
                  {"Complejo Las Acacias","Apart","4 estrellas","Ruta Provincial km 6","3515554004","30","Martín Acuña","29877104","Apartamentos equipados","WiFi, cochera, desayuno","","Activo"},
                  {"Refugio La Montaña","Refugio","2 estrellas","Sendero Serrano 18","3515554005","14","Patricia Luna","31555105","Refugio para turismo aventura","Fogón, cocina, lockers","","Activo"},
                  {"Hotel Plaza","Hotel","3 estrellas","Sarmiento 90","3515554006","35","Eduardo Funes","27666106","Hotel familiar en zona céntrica","WiFi, desayuno, aire acondicionado","","Activo"},
                  {"Camping Los Álamos","Camping","1 estrella","Costanera Sur 210","3515554007","90","Verónica Gil","32777107","Predio amplio para carpas y motorhomes","Baños, duchas, fogones","","Activo"},
                  {"Cabañas El Sauce","Cabaña","3 estrellas","Camino al Dique 205","3515554008","8","Pablo Núñez","30222108","Cabañas para parejas y familias","Parrilla, calefacción, WiFi","","Activo"}
                };
                for(String[] r:rows){for(int i=0;i<12;i++)p.setString(i+1,r[i]);p.setString(13,r[0]);p.addBatch();} p.executeBatch();
            }
            try (PreparedStatement p=c.prepareStatement("INSERT INTO turistas(nombre,apellido,tipo_documento,numero_documento,fecha_nacimiento,provincia,pais,telefono,email,observaciones,fecha_registro) SELECT ?,?,?,?,?,?,?,?,?,?,? FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM turistas WHERE numero_documento=?)")) {
                String[][] rows={
                  {"Elena","Martínez","DNI","H2021001","1985-02-11","Córdoba","Argentina","3515555101","elena.2021@example.com","Turismo de naturaleza","2021-01-18 10:20:00"},
                  {"Pablo","Serrano","Pasaporte","H2021002","1982-06-23","Mendoza","Argentina","2615555102","pablo.2021@example.com","Aventura","2021-04-09 12:15:00"},
                  {"Marina","Ledesma","DNI","H2021003","1990-09-17","Santa Fe","Argentina","3425555103","marina.2021@example.com","Gastronomía","2021-07-21 15:40:00"},
                  {"Jorge","Rivas","DNI","H2021004","1979-11-28","Buenos Aires","Argentina","1155555104","jorge.2021@example.com","Alojamiento","2021-11-06 09:35:00"},
                  {"Clara","Ponce","DNI","H2022001","1988-01-20","Córdoba","Argentina","3515555201","clara.2022@example.com","Senderismo","2022-02-14 11:10:00"},
                  {"Ramón","Vega","Pasaporte","H2022002","1984-05-15","San Luis","Argentina","2665555202","ramon.2022@example.com","Turismo cultural","2022-05-27 16:25:00"},
                  {"Irene","Sosa","DNI","H2022003","1992-08-04","Entre Ríos","Argentina","3435555203","irene.2022@example.com","Eventos","2022-08-19 13:45:00"},
                  {"Marcelo","Núñez","DNI","H2022004","1978-12-09","Córdoba","Argentina","3515555204","marcelo.2022@example.com","Fin de semana","2022-12-03 10:05:00"},
                  {"Rocío","Ferrer","DNI","H2023001","1991-03-12","Córdoba","Argentina","3515555301","rocio.2023@example.com","Naturaleza","2023-01-22 09:50:00"},
                  {"Hernán","Molina","DNI","H2023002","1986-07-18","Mendoza","Argentina","2615555302","hernan.2023@example.com","Aventura","2023-04-13 14:10:00"},
                  {"Laura","Cabrera","Pasaporte","H2023003","1995-10-01","Buenos Aires","Argentina","1155555303","laura.2023@example.com","Gastronomía","2023-08-07 17:30:00"},
                  {"Gabriel","Torres","DNI","H2023004","1980-12-22","Santa Fe","Argentina","3425555304","gabriel.2023@example.com","Alojamiento","2023-12-16 11:45:00"},
                  {"Nadia","Roldán","DNI","H2024001","1993-02-25","Córdoba","Argentina","3515555401","nadia.2024@example.com","Kayak","2024-01-15 10:00:00"},
                  {"Sebastián","Acuña","DNI","H2024002","1987-06-08","San Juan","Argentina","2645555402","sebastian.2024@example.com","Senderismo","2024-03-28 12:35:00"},
                  {"Mara","Varela","Pasaporte","H2024003","1990-09-29","Buenos Aires","Argentina","1155555403","mara.2024@example.com","Eventos","2024-07-12 15:20:00"},
                  {"Oscar","Benítez","DNI","H2024004","1983-11-14","Córdoba","Argentina","3515555404","oscar.2024@example.com","Turismo cultural","2024-10-21 18:05:00"},
                  {"Ailén","Giménez","DNI","H2025001","1996-04-16","Córdoba","Argentina","3515555501","ailen.2025@example.com","Naturaleza","2025-02-08 09:15:00"},
                  {"Federico","Luna","DNI","H2025002","1989-08-27","Mendoza","Argentina","2615555502","federico.2025@example.com","Aventura","2025-05-19 13:40:00"},
                  {"Noelia","Salas","Pasaporte","H2025003","1994-10-30","Santa Fe","Argentina","3425555503","noelia.2025@example.com","Gastronomía","2025-08-23 16:55:00"},
                  {"Diego","Coronel","DNI","H2025004","1981-12-03","Córdoba","Argentina","3515555504","diego.2025@example.com","Alojamiento","2025-11-14 10:30:00"}
                };
                for(String[] r:rows){for(int i=0;i<11;i++)p.setString(i+1,r[i]);p.setString(12,r[3]);p.addBatch();}p.executeBatch();
            }
            try (PreparedStatement p=c.prepareStatement("INSERT INTO busquedas_actividades(termino,fecha) SELECT ?,? FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM busquedas_actividades WHERE termino=? AND fecha=?)")) {
                String[][] rows={
                  {"Senderismo Cerro Negro","2021-01-20 11:00:00"},{"Paseo en Kayak","2021-04-12 14:00:00"},{"Circuito de Bicicleta","2021-07-24 16:00:00"},{"Cabalgata Serrana","2021-11-10 10:00:00"},
                  {"Avistaje de Aves","2022-02-17 12:00:00"},{"Paseo Familiar","2022-05-30 15:00:00"},{"Senderismo Cerro Negro","2022-08-22 11:30:00"},{"Paseo en Kayak","2022-12-06 17:00:00"},
                  {"Circuito de Bicicleta","2023-01-25 09:30:00"},{"Cabalgata Serrana","2023-04-16 13:00:00"},{"Avistaje de Aves","2023-08-10 15:30:00"},{"Clase de Fotografía","2023-12-20 11:15:00"},
                  {"Paseo Familiar","2024-01-18 10:45:00"},{"Senderismo Cerro Negro","2024-03-31 14:20:00"},{"Paseo en Kayak","2024-07-15 16:10:00"},{"Cabalgata Serrana","2024-10-24 12:00:00"},
                  {"Avistaje de Aves","2025-02-11 09:50:00"},{"Circuito de Bicicleta","2025-05-22 13:30:00"},{"Senderismo Cerro Negro","2025-08-26 17:00:00"},{"Paseo Familiar","2025-11-17 10:20:00"}
                };
                for(String[] r:rows){p.setString(1,r[0]);p.setTimestamp(2,Timestamp.valueOf(r[1]));p.setString(3,r[0]);p.setTimestamp(4,Timestamp.valueOf(r[1]));p.addBatch();}p.executeBatch();
            }
            try { c.createStatement().executeUpdate("ALTER TABLE eventos ADD COLUMN telefono VARCHAR(40) NOT NULL DEFAULT ''"); } catch (SQLException ignored) {}
            try { c.createStatement().executeUpdate("ALTER TABLE actividades ADD COLUMN horario VARCHAR(100) NOT NULL DEFAULT ''"); } catch (SQLException ignored) {}
        }
    }
}
