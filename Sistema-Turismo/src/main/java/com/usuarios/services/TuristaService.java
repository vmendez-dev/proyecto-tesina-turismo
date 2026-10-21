package com.usuarios.services;

import com.usuarios.database.DatabaseConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public final class TuristaService {
    private static final TuristaService I=new TuristaService();
    private final DatabaseConnection db=DatabaseConnection.getInstance();
    private TuristaService(){}
    public static TuristaService get(){return I;}

    public List<Map<String,Object>> listar(boolean activos,String q)throws SQLException{
        StringBuilder sql=new StringBuilder("SELECT t.id_turista,t.nombre,t.apellido,COALESCE(td.nombre_tipo,t.tipo_documento) tipo_documento,t.numero_documento,t.fecha_nacimiento,COALESCE(pr.nombre_provincia,t.provincia) provincia,COALESCE(pa.nombre_pais,t.pais) pais,t.telefono,t.email,t.observaciones,t.fecha_registro,t.fecha_eliminacion,t.activo FROM turistas t LEFT JOIN tipos_documento td ON td.id_tipo_documento=t.id_tipo_documento LEFT JOIN provincias pr ON pr.id_provincia=t.id_provincia LEFT JOIN paises pa ON pa.id_pais=t.id_pais WHERE t.activo=?");
        List<Object>p=new ArrayList<>(List.of(activos));
        if(q!=null&&!q.isBlank()){sql.append(" AND (LOWER(t.nombre) LIKE ? OR LOWER(t.apellido) LIKE ? OR t.numero_documento LIKE ? OR LOWER(t.email) LIKE ? OR LOWER(COALESCE(pa.nombre_pais,t.pais)) LIKE ?)");String x="%"+q.trim().toLowerCase()+"%";for(int i=0;i<5;i++)p.add(x);}
        sql.append(" ORDER BY t.apellido,t.nombre");
        try(Connection c=db.getConnection();PreparedStatement s=c.prepareStatement(sql.toString())){for(int i=0;i<p.size();i++)s.setObject(i+1,p.get(i));try(ResultSet r=s.executeQuery()){return rows(r);}}
    }

    public void guardar(Integer id,Map<String,String>d)throws SQLException{
        for(String k:new String[]{"nombre","apellido","tipo_documento","numero_documento","fecha_nacimiento","pais"})if(d.getOrDefault(k,"").isBlank())throw new SQLException("Falta completar: "+k);
        LocalDate fecha;try{fecha=LocalDate.parse(d.get("fecha_nacimiento"));}catch(Exception e){throw new SQLException("La fecha de nacimiento no es válida.");}
        if(java.time.Period.between(fecha,LocalDate.now()).getYears()<18)throw new SQLException("El turista debe ser mayor de 18 años.");
        if("Argentina".equalsIgnoreCase(d.getOrDefault("pais", ""))&&d.getOrDefault("provincia","").isBlank())throw new SQLException("La provincia es obligatoria para Argentina.");
        try(Connection c=db.getConnection()){
            int tipo=ensureTipoDocumento(c,d.get("tipo_documento"));
            int pais=ensurePais(c,d.get("pais"));
            int provincia=ensureProvincia(c,d.getOrDefault("provincia",""),pais);
            if(id==null){String sql="INSERT INTO turistas(nombre,apellido,id_tipo_documento,tipo_documento,numero_documento,fecha_nacimiento,id_provincia,provincia,id_pais,pais,telefono,email,observaciones,activo) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,TRUE)";try(PreparedStatement s=c.prepareStatement(sql)){set(s,d,tipo,pais,provincia);s.executeUpdate();}}
            else{String sql="UPDATE turistas SET nombre=?,apellido=?,id_tipo_documento=?,tipo_documento=?,numero_documento=?,fecha_nacimiento=?,id_provincia=?,provincia=?,id_pais=?,pais=?,telefono=?,email=?,observaciones=? WHERE id_turista=?";try(PreparedStatement s=c.prepareStatement(sql)){set(s,d,tipo,pais,provincia);s.setInt(14,id);s.executeUpdate();}}
        }
    }

    private void set(PreparedStatement s,Map<String,String>d,int tipo,int pais,int provincia)throws SQLException{
        s.setString(1,d.get("nombre"));s.setString(2,d.get("apellido"));s.setInt(3,tipo);s.setString(4,d.get("tipo_documento"));s.setString(5,d.get("numero_documento"));s.setDate(6,java.sql.Date.valueOf(d.get("fecha_nacimiento")));if(d.getOrDefault("provincia","").isBlank())s.setNull(7,Types.INTEGER);else s.setInt(7,provincia);s.setString(8,d.getOrDefault("provincia",""));s.setInt(9,pais);s.setString(10,d.get("pais"));s.setString(11,d.getOrDefault("telefono",""));s.setString(12,d.getOrDefault("email",""));s.setString(13,d.getOrDefault("observaciones",""));
    }
    private int ensureTipoDocumento(Connection c,String nombre)throws SQLException{try(PreparedStatement s=c.prepareStatement("SELECT id_tipo_documento FROM tipos_documento WHERE LOWER(nombre_tipo)=LOWER(?)")){s.setString(1,nombre);try(ResultSet r=s.executeQuery()){if(r.next())return r.getInt(1);}}try(PreparedStatement s=c.prepareStatement("INSERT INTO tipos_documento(nombre_tipo) VALUES(?)",Statement.RETURN_GENERATED_KEYS)){s.setString(1,nombre);s.executeUpdate();try(ResultSet r=s.getGeneratedKeys()){r.next();return r.getInt(1);}}}
    private int ensurePais(Connection c,String nombre)throws SQLException{try(PreparedStatement s=c.prepareStatement("SELECT id_pais FROM paises WHERE LOWER(nombre_pais)=LOWER(?)")){s.setString(1,nombre);try(ResultSet r=s.executeQuery()){if(r.next())return r.getInt(1);}}try(PreparedStatement s=c.prepareStatement("INSERT INTO paises(nombre_pais) VALUES(?)",Statement.RETURN_GENERATED_KEYS)){s.setString(1,nombre);s.executeUpdate();try(ResultSet r=s.getGeneratedKeys()){r.next();return r.getInt(1);}}}
    private int ensureProvincia(Connection c,String nombre,int pais)throws SQLException{if(nombre==null||nombre.isBlank())return 0;try(PreparedStatement s=c.prepareStatement("SELECT id_provincia FROM provincias WHERE LOWER(nombre_provincia)=LOWER(?) AND id_pais=?")){s.setString(1,nombre);s.setInt(2,pais);try(ResultSet r=s.executeQuery()){if(r.next())return r.getInt(1);}}try(PreparedStatement s=c.prepareStatement("INSERT INTO provincias(nombre_provincia,id_pais) VALUES(?,?)",Statement.RETURN_GENERATED_KEYS)){s.setString(1,nombre);s.setInt(2,pais);s.executeUpdate();try(ResultSet r=s.getGeneratedKeys()){r.next();return r.getInt(1);}}}

    public void restaurarTodos()throws SQLException{try(Connection c=db.getConnection();Statement s=c.createStatement()){s.executeUpdate("UPDATE turistas SET activo=TRUE,fecha_eliminacion=NULL WHERE activo=FALSE");}}
    public void cambiarEstado(int id,boolean activo)throws SQLException{try(Connection c=db.getConnection();PreparedStatement s=c.prepareStatement("UPDATE turistas SET activo=?,fecha_eliminacion=CASE WHEN ? THEN NULL ELSE CURRENT_TIMESTAMP END WHERE id_turista=?")){s.setBoolean(1,activo);s.setBoolean(2,activo);s.setInt(3,id);s.executeUpdate();}}

    public Map<String,Integer> reporteMensual()throws SQLException{return reportePorPeriodo("Mes",null);}
    public Map<String,Integer> reportePorPeriodo(String periodo,Integer year)throws SQLException{
        int y=year==null?LocalDate.now().getYear():year;Map<String,Integer>m=new LinkedHashMap<>();
        if("Mes".equals(periodo)){
            int limite=y==LocalDate.now().getYear()?LocalDate.now().getMonthValue():12;for(int mes=1;mes<=limite;mes++)m.put(String.format(Locale.ROOT,"%04d-%02d",y,mes),0);
            String sql="SELECT MONTH(fecha_registro) periodo,COUNT(*) cantidad FROM turistas WHERE activo=TRUE AND YEAR(fecha_registro)=? GROUP BY MONTH(fecha_registro) ORDER BY MONTH(fecha_registro)";try(Connection c=db.getConnection();PreparedStatement s=c.prepareStatement(sql)){s.setInt(1,y);try(ResultSet r=s.executeQuery()){while(r.next()){int mes=r.getInt("periodo");if(m.containsKey(String.format(Locale.ROOT,"%04d-%02d",y,mes)))m.put(String.format(Locale.ROOT,"%04d-%02d",y,mes),r.getInt("cantidad"));}}}
        }else if("Trimestre".equals(periodo)){
            int limite=y==LocalDate.now().getYear()?(LocalDate.now().getMonthValue()+2)/3:4;for(int q=1;q<=limite;q++)m.put(y+" T"+q,0);
            String sql="SELECT QUARTER(fecha_registro) periodo,COUNT(*) cantidad FROM turistas WHERE activo=TRUE AND YEAR(fecha_registro)=? GROUP BY QUARTER(fecha_registro) ORDER BY QUARTER(fecha_registro)";try(Connection c=db.getConnection();PreparedStatement s=c.prepareStatement(sql)){s.setInt(1,y);try(ResultSet r=s.executeQuery()){while(r.next()){int q=r.getInt("periodo");if(q<=limite)m.put(y+" T"+q,r.getInt("cantidad"));}}}
        }else if("Temporada".equals(periodo)){
            String[] nombres={"Verano","Otoño","Invierno","Primavera"};int limite=y==LocalDate.now().getYear()?temporadaActual():4;for(int i=0;i<limite;i++)m.put(nombres[i],0);
            String sql="SELECT CASE WHEN MONTH(fecha_registro) IN (12,1,2) THEN 1 WHEN MONTH(fecha_registro) IN (3,4,5) THEN 2 WHEN MONTH(fecha_registro) IN (6,7,8) THEN 3 ELSE 4 END periodo,COUNT(*) cantidad FROM turistas WHERE activo=TRUE AND YEAR(fecha_registro)=? GROUP BY periodo ORDER BY periodo";try(Connection c=db.getConnection();PreparedStatement s=c.prepareStatement(sql)){s.setInt(1,y);try(ResultSet r=s.executeQuery()){while(r.next()){int n=r.getInt("periodo");if(n<=limite)m.put(nombres[n-1],r.getInt("cantidad"));}}}
        }else{
            String sql="SELECT YEAR(fecha_registro) periodo,COUNT(*) cantidad FROM turistas WHERE activo=TRUE AND YEAR(fecha_registro)=? GROUP BY YEAR(fecha_registro)";try(Connection c=db.getConnection();PreparedStatement s=c.prepareStatement(sql)){s.setInt(1,y);try(ResultSet r=s.executeQuery()){if(r.next())m.put(String.valueOf(y),r.getInt("cantidad"));else m.put(String.valueOf(y),0);}}
        }
        return m;
    }
    private int temporadaActual(){int m=LocalDate.now().getMonthValue();return m<=2?1:m<=5?2:m<=8?3:4;}
    public Map<String,Integer> reporteAnualComparativo(int desde,int hasta)throws SQLException{
        int max=Math.min(hasta,LocalDate.now().getYear());int min=Math.min(desde,max);String sql="SELECT YEAR(fecha_registro) periodo,COUNT(*) cantidad FROM turistas WHERE activo=TRUE AND YEAR(fecha_registro) BETWEEN ? AND ? GROUP BY YEAR(fecha_registro) ORDER BY YEAR(fecha_registro)";try(Connection c=db.getConnection();PreparedStatement s=c.prepareStatement(sql)){s.setInt(1,min);s.setInt(2,max);try(ResultSet r=s.executeQuery()){Map<String,Integer>m=new LinkedHashMap<>();for(int y=min;y<=max;y++)m.put(String.valueOf(y),0);while(r.next())m.put(r.getString("periodo"),r.getInt("cantidad"));return m;}}
    }
    public int totalActivos()throws SQLException{try(Connection c=db.getConnection();PreparedStatement s=c.prepareStatement("SELECT COUNT(*) FROM turistas WHERE activo=TRUE");ResultSet r=s.executeQuery()){r.next();return r.getInt(1);}}
    public Map<String,Integer> actividadesMasBuscadas(String periodo,Integer year)throws SQLException{
        StringBuilder sql=new StringBuilder("SELECT termino,COUNT(*) cantidad FROM busquedas_actividades WHERE 1=1");List<Object>p=new ArrayList<>();
        if(year!=null){sql.append(" AND YEAR(fecha)=?");p.add(year);}
        if(periodo!=null&&periodo.equals("Mes")){sql.append(" AND MONTH(fecha)=MONTH(CURDATE())");}
        else if(periodo!=null&&periodo.equals("Temporada")){sql.append(" AND ((MONTH(fecha) IN (12,1,2) AND MONTH(CURDATE()) IN (12,1,2)) OR (MONTH(fecha) IN (3,4,5) AND MONTH(CURDATE()) IN (3,4,5)) OR (MONTH(fecha) IN (6,7,8) AND MONTH(CURDATE()) IN (6,7,8)) OR (MONTH(fecha) IN (9,10,11) AND MONTH(CURDATE()) IN (9,10,11)))");}
        else if(periodo!=null&&periodo.equals("Trimestre")){sql.append(" AND QUARTER(fecha)=QUARTER(CURDATE())");}
        sql.append(" GROUP BY termino ORDER BY cantidad DESC,termino LIMIT 10");
        try(Connection c=db.getConnection();PreparedStatement s=c.prepareStatement(sql.toString())){for(int i=0;i<p.size();i++)s.setObject(i+1,p.get(i));try(ResultSet r=s.executeQuery()){Map<String,Integer>m=new LinkedHashMap<>();while(r.next())m.put(r.getString("termino"),r.getInt("cantidad"));return m;}}
    }
    private List<Map<String,Object>> rows(ResultSet r)throws SQLException{List<Map<String,Object>>out=new ArrayList<>();while(r.next()){Map<String,Object>m=new LinkedHashMap<>();m.put("id",r.getInt("id_turista"));m.put("nombre",r.getString("nombre"));m.put("apellido",r.getString("apellido"));m.put("tipo_documento",r.getString("tipo_documento"));m.put("numero_documento",r.getString("numero_documento"));m.put("fecha_nacimiento",r.getDate("fecha_nacimiento"));m.put("provincia",r.getString("provincia"));m.put("pais",r.getString("pais"));m.put("telefono",r.getString("telefono"));m.put("email",r.getString("email"));m.put("observaciones",r.getString("observaciones"));m.put("fecha_registro",r.getTimestamp("fecha_registro"));m.put("fecha_eliminacion",r.getTimestamp("fecha_eliminacion"));m.put("activo",r.getBoolean("activo"));out.add(m);}return out;}
}
