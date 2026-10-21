package com.usuarios.controllers;

import com.usuarios.database.DatabaseConnection;
import com.usuarios.model.Usuario;
import com.usuarios.services.*;
import com.usuarios.utils.CountryProvinceService;
import com.usuarios.utils.NotificationManager;
import com.usuarios.utils.SessionManager;
import com.usuarios.utils.ValidationUtils;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.stage.Window;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DashboardController {
    private final Stage stage;
    private final BorderPane root = new BorderPane();
    private final VBox content = new VBox(12);
    private final ToggleGroup group = new ToggleGroup();
    private boolean dark=false; private double fontScale=1.0; private String idioma="es"; private String access="normal";
    private Timeline clock;

    public DashboardController(Stage stage){
        this.stage=stage; content.setPadding(new Insets(18));
        try { var db=DatabaseConnection.getInstance(); dark="oscuro".equals(db.leerConfiguracion("tema","claro")); idioma=db.leerConfiguracion("idioma","es"); access=db.leerConfiguracion("accesibilidad","normal"); fontScale=Double.parseDouble(db.leerConfiguracion("tamano_fuente","1.0")); } catch(Exception ignored){}
    }
    public void mostrar(){
        root.setTop(top()); root.setLeft(sidebarScroll());
        ScrollPane sp=new ScrollPane(content); sp.setFitToWidth(true); sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); sp.setPannable(true); sp.setStyle("-fx-background:transparent;-fx-background-color:transparent;");sp.setPannable(true);sp.addEventFilter(javafx.scene.input.ScrollEvent.SCROLL,e->{if(Math.abs(e.getDeltaY())>0){sp.setVvalue(Math.max(0,Math.min(1,sp.getVvalue()-e.getDeltaY()*0.004)));e.consume();}}); root.setCenter(sp);
        SceneUtil.set(root,stage); applyTheme(); mostrarInicio(); startClock();
    }
    private HBox top(){
        HBox b=new HBox(12); b.setAlignment(Pos.CENTER_LEFT); b.setPadding(new Insets(10,18,10,18)); b.getStyleClass().add("topbar");
        Label t=new Label("🏛 "+tr("Sistema de Gestión Turística","Tourism Management System")); t.getStyleClass().add("title"); Region r=new Region(); HBox.setHgrow(r,Priority.ALWAYS);
        Usuario u=SessionManager.getInstance().getUsuarioActual(); Label user=new Label("👤 "+(u==null?"":u.getNombre()+" "+u.getApellido())+" · "+(u==null?"":rolTr(u.getRol()))); user.getStyleClass().add("muted");
        Button out=new Button(tr("Cerrar sesión","Log out")); out.setOnAction(e->logout()); b.getChildren().addAll(t,r,user,out); return b;
    }
    private void logout(){
        Alert a=new Alert(Alert.AlertType.CONFIRMATION,tr("¿Cerrar sesión?","Log out?"),ButtonType.OK,ButtonType.CANCEL); if(a.showAndWait().orElse(ButtonType.CANCEL)==ButtonType.OK){
            if(clock!=null)clock.stop(); SessionManager.getInstance().cerrarSesion(); com.usuarios.MainApp.getInstance().mostrarLogin();
        }
    }
    private ScrollPane sidebarScroll(){ VBox side=sidebar(); ScrollPane nav=new ScrollPane(side); nav.setFitToWidth(true); nav.setFitToHeight(true); nav.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); nav.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); nav.setPrefWidth(215); nav.setMinWidth(180); nav.getStyleClass().add("sidebar-scroll"); return nav; }
    private VBox sidebar(){
        VBox s=new VBox(5); s.setPrefWidth(215); s.setPadding(new Insets(14,10,10,12)); s.getStyleClass().add("sidebar");
        Label a=new Label(tr("Municipalidad","Municipality")); a.setFont(Font.font("Inter",FontWeight.BOLD,16)); Label z=new Label("San Antonio de Arredondo"); z.getStyleClass().add("muted"); s.getChildren().addAll(a,z,new Separator());
        List<String> items=new ArrayList<>(List.of("🏠 "+tr("Inicio","Home"),"👥 "+tr("Turistas","Tourists"),"🏃 "+tr("Actividades","Activities"),"🏛 "+tr("Atractivos","Attractions"),"🏨 "+tr("Alojamientos","Accommodations"),"🍽 "+tr("Gastronomía","Gastronomy"),"🔧 "+tr("Servicios","Services"),"🎪 "+tr("Eventos","Events"),"📊 "+tr("Reportes","Reports"),"🗑 "+tr("Papelera","Trash")));
        if(SessionManager.getInstance().esAdministrador()) items.add("👤 "+tr("Usuarios","Users")); items.add("⚙ "+tr("Configuración","Settings"));
        for(String n:items){ToggleButton b=new ToggleButton(n); b.setMaxWidth(Double.MAX_VALUE); b.setAlignment(Pos.CENTER_LEFT); b.setToggleGroup(group); b.getStyleClass().add("menu"); b.setOnAction(e->abrir(n)); s.getChildren().add(b);}
        Region spacer=new Region(); VBox.setVgrow(spacer,Priority.ALWAYS); s.getChildren().add(spacer); return s;
    }
    private String tr(String es,String en){return idioma.equals("en")?en:es;}
    private String rolTr(String r){return idioma.equals("en")?switch(r){case "Operador"->"Operator";case "Director"->"Director";case "Administrador"->"Administrator";default->"Inspector";}:r;}
    private void abrir(String n){
        try{String x=n.replaceFirst("^[^ ]+ ",""); if(x.equals("Inicio")||x.equals("Home"))mostrarInicio(); else if(x.equals("Turistas")||x.equals("Tourists"))mostrarTuristas(); else if(x.equals("Actividades")||x.equals("Activities"))mostrarCatalogo("Actividades"); else if(x.equals("Atractivos")||x.equals("Attractions"))mostrarCatalogo("Atractivos"); else if(x.equals("Alojamientos")||x.equals("Accommodations"))mostrarAlojamientos(); else if(x.equals("Gastronomía")||x.equals("Gastronomy"))mostrarCatalogo("Gastronomía"); else if(x.equals("Servicios")||x.equals("Services"))mostrarCatalogo("Servicios"); else if(x.equals("Eventos")||x.equals("Events"))mostrarCatalogo("Eventos"); else if(x.equals("Reportes")||x.equals("Reports"))mostrarReportes(); else if(x.equals("Papelera")||x.equals("Trash")||x.equals("Eliminados recientemente")||x.equals("Recently deleted"))mostrarPapelera(); else if(x.equals("Usuarios")||x.equals("Users")){if(SessionManager.getInstance().esAdministrador())new UsuariosController(content).mostrar();} else mostrarConfiguracion(); applyTheme(); translateTree(content);}catch(Exception e){error(e.getMessage());}
    }
    private void translateTree(Node n){
        if(idioma.equals("es"))return; Map<String,String>m=Map.ofEntries(Map.entry("Editar","Edit"),Map.entry("Ver información","View information"),Map.entry("Enviar a papelera","Move to trash"),Map.entry("Guardar","Save"),Map.entry("Cancelar","Cancel"),Map.entry("Nuevo","New"),Map.entry("Nuevo turista","New tourist"),Map.entry("Editar turista","Edit tourist"),Map.entry("Actualizar","Refresh"),Map.entry("Buscar...","Search..."),Map.entry("Buscar alojamiento...","Search accommodation..."),Map.entry("Papelera","Trash"),Map.entry("Eliminados recientemente","Recently deleted"),Map.entry("Restaurar","Restore"),Map.entry("Nombre","Name"),Map.entry("Apellido","Surname"),Map.entry("Tipo","Type"),Map.entry("Detalle","Detail"),Map.entry("Información","Information"),Map.entry("Dirección","Address"),Map.entry("Categoría","Category"),Map.entry("Prestador","Provider"),Map.entry("Capacidad","Capacity"),Map.entry("Email","Email"),Map.entry("Nacimiento","Birth date"));
        if(n instanceof Labeled l && l.getText()!=null && m.containsKey(l.getText()))l.setText(m.get(l.getText())); if(n instanceof Parent p)for(Node c:p.getChildrenUnmodifiable())translateTree(c);
    }
    private void base(String title,String sub){content.getChildren().clear();content.getStyleClass().remove("report-page");root.setStyle("-fx-font-size:"+(13*fontScale)+"px;");Label t=new Label(title);t.setFont(Font.font("Inter",FontWeight.BOLD,22*fontScale));Label s=new Label(sub);s.getStyleClass().add("muted");content.getChildren().addAll(t,s);}
    private void mostrarInicio(){
        if(clock!=null) clock.stop();
        Usuario u=SessionManager.getInstance().getUsuarioActual(); String nombre=u==null?"":u.getNombre()+" "+u.getApellido();
        base(tr("Bienvenido, "+nombre,"Welcome, "+nombre),tr("Panel principal · ","Main dashboard · ")+LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        ((Label)content.getChildren().get(0)).getStyleClass().add("welcome-title");((Label)content.getChildren().get(0)).setFont(Font.font("Inter",FontWeight.BOLD,30*fontScale));
        ((Label)content.getChildren().get(1)).getStyleClass().add("welcome-subtitle");((Label)content.getChildren().get(1)).setFont(Font.font("Inter",FontWeight.BOLD,16*fontScale));

        HBox cards=new HBox(14); cards.setFillHeight(true);
        cards.getChildren().addAll(
                card("👥 "+tr("Turistas activos","Active tourists"),safeCount(true),tr("Registros actuales","Current records")),
                card("🏨 "+tr("Alojamientos","Accommodations"),safeAlojCount(),tr("Opciones registradas","Registered options")),
                card("📚 "+tr("Catálogo","Catalog"),safeCatalogCount(),tr("Módulos turísticos","Tourism modules")),
                card("🟢 MySQL","Conectado",tr("Base inicializada automáticamente","Database initialized automatically"))
        );
        for(Node n:cards.getChildren()) HBox.setHgrow(n,Priority.ALWAYS);
        content.getChildren().add(cards);

        BorderPane home=new BorderPane(); home.setPadding(new Insets(20)); home.setPrefHeight(430); home.getStyleClass().add("hero-card");

        Label time=new Label(); time.getStyleClass().add("clock"); time.setFont(Font.font("Inter",FontWeight.BOLD,54*fontScale));
        time.setMinHeight(76*fontScale);
        Label timeHint=new Label(tr("Hora actual del sistema","Current system time")); timeHint.getStyleClass().add("muted");
        VBox calendar=buildCalendar(LocalDate.now());
        VBox left=new VBox(10,time,timeHint,calendar); left.setPrefWidth(390); left.setMaxWidth(420); left.setAlignment(Pos.TOP_LEFT);
        home.setLeft(left); BorderPane.setMargin(left,new Insets(0,10,0,0));

        VBox summary=new VBox(14); summary.getStyleClass().add("summary-panel");
        Label st=new Label("📊 "+tr("Resumen del sistema","System summary")); st.getStyleClass().add("section-title");
        summary.getChildren().add(st);
        summary.getChildren().add(summaryLine("Turistas activos",safeCount(true),"👥"));
        summary.getChildren().add(summaryLine("Alojamientos",safeAlojCount(),"🏨"));
        summary.getChildren().add(summaryLine("Actividades y atractivos",safeCatalogModuloCount("Actividades")+" / "+safeCatalogModuloCount("Atractivos"),"🗺"));
        summary.getChildren().add(summaryLine("Servicios y gastronomía",safeCatalogModuloCount("Servicios")+" / "+safeCatalogModuloCount("Gastronomía"),"🍽"));
        summary.getChildren().add(summaryLine("Eventos",safeCatalogModuloCount("Eventos"),"🎪"));
        Label tip=new Label(tr("Consejo: utiliza el menú lateral para administrar cada módulo y la sección Reportes para revisar los ingresos mensuales de turistas.","Tip: use the sidebar to manage each module and Reports to review monthly tourist registrations."));
        tip.setWrapText(true); tip.getStyleClass().add("muted"); summary.getChildren().add(tip);
        home.setRight(summary); BorderPane.setMargin(summary,new Insets(0,0,0,10));
        BorderPane.setAlignment(summary,Pos.TOP_LEFT);
        content.getChildren().add(home);

        clock=new Timeline(new KeyFrame(Duration.ZERO,e->time.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")))),new KeyFrame(Duration.seconds(1),e->time.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")))));
        clock.setCycleCount(Timeline.INDEFINITE); clock.play();
    }

    private VBox buildCalendar(LocalDate date){
        VBox box=new VBox(9); box.setPadding(new Insets(13)); box.getStyleClass().add("calendar-card");
        Label title=new Label("📅 "+date.format(DateTimeFormatter.ofPattern("MMMM yyyy",idioma.equals("en")?Locale.ENGLISH:Locale.forLanguageTag("es"))));
        title.setFont(Font.font("Inter",FontWeight.BOLD,17*fontScale));
        GridPane g=new GridPane(); g.setHgap(5); g.setVgap(5); g.setMaxWidth(Double.MAX_VALUE);
        for(int i=0;i<7;i++){Label d=new Label((idioma.equals("en")?new String[]{"Mo","Tu","We","Th","Fr","Sa","Su"}:new String[]{"Lu","Ma","Mi","Ju","Vi","Sá","Do"})[i]); d.setMaxWidth(Double.MAX_VALUE); d.setAlignment(Pos.CENTER); d.getStyleClass().add("calendar-day-name"); g.add(d,i,0); GridPane.setHgrow(d,Priority.ALWAYS);}
        LocalDate first=date.withDayOfMonth(1); int shift=first.getDayOfWeek().getValue()-1;
        for(int day=1;day<=date.lengthOfMonth();day++){
            int idx=shift+day-1; Label n=new Label(String.valueOf(day)); n.setMaxWidth(Double.MAX_VALUE); n.setMinHeight(29); n.setAlignment(Pos.CENTER); n.getStyleClass().add("calendar-day");
            if(day==date.getDayOfMonth()) n.getStyleClass().add("today");
            g.add(n,idx%7,1+idx/7); GridPane.setHgrow(n,Priority.ALWAYS);
        }
        box.getChildren().addAll(title,g); return box;
    }

    private void startClock(){ if(clock!=null)clock.play(); }
    private String safeCount(boolean a){try{return String.valueOf(TuristaService.get().listar(a,"").size());}catch(Exception e){return "—";}}
    private String safeAlojCount(){try{return String.valueOf(AlojamientoService.get().listar(true,"").size());}catch(Exception e){return "—";}}
    private String safeCatalogCount(){try{return String.valueOf(CatalogService.get().listar("Actividades",true,"").size()+CatalogService.get().listar("Atractivos",true,"").size()+CatalogService.get().listar("Gastronomía",true,"").size()+CatalogService.get().listar("Servicios",true,"").size()+CatalogService.get().listar("Eventos",true,"").size());}catch(Exception e){return "—";}}
    private String safeCatalogModuloCount(String modulo){try{return String.valueOf(CatalogService.get().listar(modulo,true,"").size());}catch(Exception e){return "—";}}
    private VBox card(String t,String v,String d){VBox c=new VBox(5);c.setPadding(new Insets(15));c.setMaxWidth(Double.MAX_VALUE);c.getStyleClass().add("stat-card");Label a=new Label(t),b=new Label(v),z=new Label(d);a.getStyleClass().add("stat-title");b.setFont(Font.font("Inter",FontWeight.BOLD,25*fontScale));z.getStyleClass().add("muted");c.getChildren().addAll(a,b,z);return c;}
    private HBox summaryLine(String title,String value,String icon){Label i=new Label(icon);i.getStyleClass().add("summary-icon");Label t=new Label(title);t.getStyleClass().add("summary-label");Label v=new Label(value);v.getStyleClass().add("summary-value");Region r=new Region();HBox.setHgrow(r,Priority.ALWAYS);HBox row=new HBox(10,i,t,r,v);row.setAlignment(Pos.CENTER_LEFT);row.getStyleClass().add("summary-row");return row;}

    private void mostrarTuristas(){
        base(tr("Gestión de turistas","Tourist management"),tr("Administración y registro de turistas","Tourist registration and management"));
        VBox card=new VBox(12);card.getStyleClass().add("module-card");
        HBox filter=new HBox(10);filter.setAlignment(Pos.CENTER_LEFT);
        TextField search=new TextField();search.setPromptText(tr("Buscar","Search"));search.setPrefWidth(280);
        Button refresh=new Button(tr("Actualizar","Refresh"));refresh.getStyleClass().add("btn-secondary");
        Button add=new Button("+ "+tr("Nuevo","New"));add.getStyleClass().add("btn-primary");add.setDisable(!SessionManager.getInstance().puedeCrearTuristas());
        Region spc=new Region();HBox.setHgrow(spc,Priority.ALWAYS);filter.getChildren().addAll(search,refresh,spc,add);
        TableView<Map<String,Object>> table=new TableView<>();table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);table.setFixedCellSize(Math.max(34,42*fontScale));table.setPrefHeight(42+8*42+8);
        Pager<Map<String,Object>> pager=new Pager<>(table,8);
        addMapColumn(table,"nombre",tr("Nombre","Name"));addMapColumn(table,"apellido",tr("Apellido","Surname"));addMapColumn(table,"pais",tr("País","Country"));addMapColumn(table,"telefono",tr("Teléfono","Phone"));addMapColumn(table,"email",tr("Email","Email"));
        Runnable load=()->{try{pager.setData(TuristaService.get().listar(true,search.getText()));applyFontScale(table);}catch(Exception e){error(e.getMessage());}};
        load.run();search.textProperty().addListener((o,a,b)->load.run());refresh.setOnAction(e->load.run());add.setOnAction(e->formTurista(table,null,load));
        TableColumn<Map<String,Object>,Void> actions=new TableColumn<>(tr("Acciones","Actions"));actions.setPrefWidth(145);actions.setCellFactory(col->new TableCell<>(){
            final Button view=actionButton("◉","action-view"),edit=actionButton("✎","action-edit"),del=actionButton("🗑","action-delete");final HBox box=new HBox(5,view,edit,del);
            {view.setOnAction(e->{var m=getTableView().getItems().get(getIndex());detalle(m);});edit.setOnAction(e->{var m=getTableView().getItems().get(getIndex());formTurista(table,((Number)m.get("id")).intValue(),load);});del.setOnAction(e->{var m=getTableView().getItems().get(getIndex());try{TuristaService.get().cambiarEstado(((Number)m.get("id")).intValue(),false);load.run();}catch(Exception x){error(x.getMessage());}});edit.setDisable(!SessionManager.getInstance().puedeEditarTuristas());del.setDisable(!SessionManager.getInstance().esAdministrador());}
            protected void updateItem(Void v,boolean empty){super.updateItem(v,empty);setGraphic(empty?null:box);}
        });table.getColumns().add(actions);
        card.getChildren().addAll(filter,table,pager.controls());content.getChildren().add(card);
        try{List<Map<String,Object>> all=TuristaService.get().listar(true,"");content.getChildren().add(metricCardsTuristas(all));content.getChildren().add(recentCard(all,"Turistas"));}catch(Exception e){error(e.getMessage());}
    }

    private void formTurista(TableView<Map<String,Object>> table,Integer id,Runnable reload){
        Dialog<Map<String,String>> d=new Dialog<>();d.setTitle(id==null?tr("Nuevo turista","New tourist"):tr("Editar turista","Edit tourist"));
        VBox page=new VBox(12);page.setPadding(new Insets(16));Map<String,Control> f=new LinkedHashMap<>();
        TextField nombre=new TextField(),apellido=new TextField(),numeroDoc=new TextField(),emailLocal=new TextField(),telefono=new TextField(),observaciones=new TextField();
        ComboBox<String> tipoDoc=new ComboBox<>(FXCollections.observableArrayList(trValue("DNI"),trValue("Pasaporte"),trValue("Cédula"),trValue("Libreta Cívica")));tipoDoc.setMaxWidth(Double.MAX_VALUE);
        DatePicker fecha=new DatePicker();fecha.setMaxWidth(Double.MAX_VALUE);fecha.setDayCellFactory(dp->new DateCell(){@Override public void updateItem(LocalDate item,boolean empty){super.updateItem(item,empty);setDisable(empty||item.isAfter(LocalDate.now().minusYears(18)));}});
        ComboBox<String> pais=new ComboBox<>(FXCollections.observableArrayList(countryOptions()));pais.setMaxWidth(Double.MAX_VALUE);
        ComboBox<String> provincia=new ComboBox<>();provincia.setMaxWidth(Double.MAX_VALUE);
        ComboBox<String> emailDomain=emailDomains();emailInput(emailLocal,emailDomain);
        f.put("nombre",nombre);f.put("apellido",apellido);f.put("tipo_documento",tipoDoc);f.put("numero_documento",numeroDoc);f.put("fecha_nacimiento",fecha);f.put("pais",pais);f.put("provincia",provincia);f.put("telefono",telefono);f.put("email",emailLocal);f.put("email_domain",emailDomain);f.put("observaciones",observaciones);
        GridPane g=formGrid();g.getStyleClass().add("user-abm-grid");page.getChildren().add(g);
        addFormField(g,0,0,nombre,tr("Nombre *","Name *"),50);
        addFormField(g,0,1,apellido,tr("Apellido *","Surname *"),50);
        addFormField(g,0,2,tipoDoc,tr("Tipo de documento *","Document type *"),50);
        addFormField(g,0,3,numeroDoc,tr("Número de documento *","Document number *"),40);
        addFormField(g,0,4,fecha,tr("Fecha de nacimiento *","Birth date *"),20);
        addComboField(g,5,tr("País *","Country *"),pais,()->agregarOpcion(pais,"paises_extra",tr("Nuevo país","New country")));
        addComboField(g,6,tr("Provincia","Province"),provincia,()->agregarOpcionProvincia(provincia,pais));
        addFormField(g,0,7,telefono,tr("Teléfono","Phone"),40);
        addEmailField(g,8,tr("Email *","Email *"),emailLocal,emailDomain);
        addFormField(g,0,9,observaciones,tr("Observaciones","Notes"),300);
        d.getDialogPane().setContent(new ScrollPane(page));
        ScrollPane sp=(ScrollPane)d.getDialogPane().getContent();sp.setFitToWidth(true);sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        Runnable loadProvincias=()->{String p=pais.getValue();provincia.getItems().setAll(provinceOptions(p));provincia.setDisable(provincia.getItems().isEmpty());};pais.valueProperty().addListener((o,a,b)->loadProvincias.run());
        if(id!=null)try{var m=TuristaService.get().listar(true,"").stream().filter(x->Objects.equals(x.get("id"),id)).findFirst().orElse(null);if(m!=null){nombre.setText(Objects.toString(m.get("nombre"),""));apellido.setText(Objects.toString(m.get("apellido"),""));tipoDoc.setValue(trValue(Objects.toString(m.get("tipo_documento"),"")));numeroDoc.setText(Objects.toString(m.get("numero_documento"),""));if(m.get("fecha_nacimiento")!=null)fecha.setValue(LocalDate.parse(Objects.toString(m.get("fecha_nacimiento")).substring(0,10)));pais.setValue(Objects.toString(m.get("pais"),""));loadProvincias.run();provincia.setValue(Objects.toString(m.get("provincia"),""));telefono.setText(Objects.toString(m.get("telefono"),""));splitEmail(Objects.toString(m.get("email"),""),emailLocal,emailDomain);observaciones.setText(Objects.toString(m.get("observaciones"),""));}}catch(Exception e){error(e.getMessage());}else{pais.setValue(countryOptions().contains("Argentina")?"Argentina":pais.getItems().stream().findFirst().orElse(null));loadProvincias.run();}
        ButtonType ok=new ButtonType(tr("Guardar","Save"),ButtonBar.ButtonData.OK_DONE);d.getDialogPane().getButtonTypes().addAll(ok,ButtonType.CANCEL);Button save=(Button)d.getDialogPane().lookupButton(ok);save.getStyleClass().add("btn-primary");
        save.addEventFilter(javafx.event.ActionEvent.ACTION,e->{
            Map<String,String>m=collect(f);m.put("email",combineEmail(emailLocal,emailDomain));m.put("tipo_documento",internalDocumentType(tipoDoc.getValue()));List<String>er=new ArrayList<>();
            for(String k:new String[]{"nombre","apellido","tipo_documento","numero_documento","fecha_nacimiento","pais"})if(m.getOrDefault(k,"").isBlank())er.add(label(k)+" "+tr("es obligatorio.","is required."));
            if("Argentina".equals(pais.getValue())&&m.getOrDefault("provincia","").isBlank())er.add(tr("La provincia es obligatoria para Argentina.","Province is required for Argentina."));
            if(!m.get("email").matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))er.add(tr("El email no es válido.","Email is invalid."));
            if(!er.isEmpty()){e.consume();error(tr("Corrige los siguientes datos antes de guardar:","Correct the following data before saving:")+"\n\n• "+String.join("\n• ",er));}
        });
        d.setResultConverter(b->{if(b!=ok)return null;Map<String,String>m=collect(f);m.put("email",combineEmail(emailLocal,emailDomain));m.put("tipo_documento",internalDocumentType(tipoDoc.getValue()));return m;});d.getDialogPane().setMinWidth(560);d.getDialogPane().setPrefWidth(620);d.getDialogPane().setPrefHeight(620);styleDialog(d);
        d.showAndWait().ifPresent(m->{try{TuristaService.get().guardar(id,m);reload.run();NotificationManager.getInstance().mostrarNotificacion(tr("Turista guardado correctamente","Tourist saved successfully"),"exito");}catch(Exception e){error(e.getMessage());}});
    }

    private Button actionButton(String text,String style){
        Button b=new Button(text); b.getStyleClass().add(style); b.setFocusTraversable(false); return b;
    }

    private void addMapColumn(TableView<Map<String,Object>> table,String key,String title){TableColumn<Map<String,Object>,Object>col=new TableColumn<>(title);col.setCellValueFactory(cell->{Map<String,Object>row=cell.getValue();Object value=row==null?null:row.get(key);return new javafx.beans.property.ReadOnlyObjectWrapper<>(value==null?"":value);});col.setCellFactory(tc->new TableCell<>(){protected void updateItem(Object item,boolean empty){super.updateItem(item,empty);if(empty){setText(null);return;}String v=Objects.toString(item,"");if(item instanceof String||item instanceof java.sql.Date||item instanceof java.sql.Timestamp)v=trValue(v);setText(v);setStyle("-fx-font-size:"+(13*fontScale)+"px;");}});table.getColumns().add(col);}

    private void mostrarCatalogo(String modulo){
        base(tr(modulo,eng(modulo)),tr("Administración y registro de información turística","Tourism information management"));
        VBox card=new VBox(12);card.getStyleClass().add("module-card");HBox filter=new HBox(10);filter.setAlignment(Pos.CENTER_LEFT);
        TextField search=new TextField();search.setPromptText(tr("Buscar","Search"));search.setPrefWidth(280);Button refresh=new Button(tr("Actualizar","Refresh"));refresh.getStyleClass().add("btn-secondary");Button add=new Button("+ "+tr("Nuevo","New"));add.getStyleClass().add("btn-primary");add.setDisable(!SessionManager.getInstance().puedeEditarCatalogo());Region spc=new Region();HBox.setHgrow(spc,Priority.ALWAYS);filter.getChildren().addAll(search,refresh,spc,add);
        TableView<Map<String,Object>> t=new TableView<>();t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);t.setFixedCellSize(Math.max(34,42*fontScale));t.setPrefHeight(42+8*42+8);Pager<Map<String,Object>> pager=new Pager<>(t,8);
        if(modulo.equals("Actividades")){addMapColumn(t,"nombre",tr("Nombre","Name"));addMapColumn(t,"detalle",tr("Descripción","Description"));addMapColumn(t,"precio",tr("Precio","Price"));addMapColumn(t,"horario",tr("Horario","Schedule"));}
        else if(modulo.equals("Atractivos")){addMapColumn(t,"nombre",tr("Nombre","Name"));addMapColumn(t,"tipo",tr("Tipo","Type"));addMapColumn(t,"direccion",tr("Ubicación","Location"));addMapColumn(t,"detalle",tr("Descripción","Description"));addMapColumn(t,"horario",tr("Horario","Schedule"));}
        else if(modulo.equals("Gastronomía")){addMapColumn(t,"nombre",tr("Nombre","Name"));addMapColumn(t,"tipo",tr("Tipo","Type"));addMapColumn(t,"detalle",tr("Especialidad","Specialty"));addMapColumn(t,"horario",tr("Horario","Schedule"));}
        else if(modulo.equals("Servicios")){addMapColumn(t,"nombre",tr("Nombre","Name"));addMapColumn(t,"tipo",tr("Tipo","Type"));addMapColumn(t,"direccion",tr("Dirección","Address"));addMapColumn(t,"extra",tr("Teléfono","Phone"));addMapColumn(t,"detalle",tr("Horario","Schedule"));}
        else {addMapColumn(t,"nombre",tr("Nombre","Name"));addMapColumn(t,"fecha",tr("Fecha","Date"));addMapColumn(t,"direccion",tr("Lugar","Place"));addMapColumn(t,"telefono",tr("Teléfono","Phone"));addMapColumn(t,"detalle",tr("Descripción","Description"));}
        Runnable load=()->{try{pager.setData(CatalogService.get().listar(modulo,true,search.getText()));applyFontScale(t);}catch(Exception e){error(e.getMessage());}};load.run();search.textProperty().addListener((o,a,b)->load.run());refresh.setOnAction(e->load.run());add.setOnAction(e->formCatalogo(modulo,null,load));
        TableColumn<Map<String,Object>,Void> actions=new TableColumn<>(tr("Acciones","Actions"));actions.setPrefWidth(145);actions.setCellFactory(col->new TableCell<>(){final Button view=actionButton("◉","action-view"),edit=actionButton("✎","action-edit"),del=actionButton("🗑","action-delete");final HBox box=new HBox(5,view,edit,del);{view.setOnAction(e->{var m=getTableView().getItems().get(getIndex());detalle(m);});edit.setOnAction(e->{var m=getTableView().getItems().get(getIndex());formCatalogo(modulo,((Number)m.get("id")).intValue(),load);});del.setOnAction(e->{var m=getTableView().getItems().get(getIndex());try{CatalogService.get().estado(modulo,((Number)m.get("id")).intValue(),false);load.run();}catch(Exception x){error(x.getMessage());}});edit.setDisable(!SessionManager.getInstance().puedeEditarCatalogo());del.setDisable(!SessionManager.getInstance().puedeEditarCatalogo());}protected void updateItem(Void v,boolean empty){super.updateItem(v,empty);setGraphic(empty?null:box);}});t.getColumns().add(actions);card.getChildren().addAll(filter,t,pager.controls());content.getChildren().add(card);
        try{List<Map<String,Object>> all=CatalogService.get().listar(modulo,true,"");content.getChildren().addAll(metricCardsCatalogo(all,modulo),recentCard(all,modulo));}catch(Exception e){error(e.getMessage());}
    }

    private void formCatalogo(String modulo,Integer id,Runnable reload){
        Dialog<Map<String,String>> d=new Dialog<>();d.setTitle(id==null?tr("Nuevo registro · "+modulo,"New record · "+eng(modulo)):tr("Modificar registro · "+modulo,"Edit record · "+eng(modulo)));
        VBox page=new VBox(10);page.setPadding(new Insets(14));GridPane g=formGrid();g.getStyleClass().add("user-abm-grid");page.getChildren().add(g);Map<String,Control> f=new LinkedHashMap<>();
        TextField nombre=new TextField();f.put("nombre",nombre);addFormField(g,0,0,nombre,tr("Nombre *","Name *"),140);
        if(modulo.equals("Actividades")){
            TextField descripcion=new TextField(),precio=new TextField(),horario=new TextField();f.put("detalle",descripcion);f.put("precio",precio);f.put("horario",horario);addFormField(g,0,1,descripcion,tr("Descripción *","Description *"),255);addFormField(g,0,2,precio,tr("Precio","Price"),100);addScheduleField(g,3,tr("Horario","Schedule"),horario);
        }else if(modulo.equals("Atractivos")){
            ComboBox<String>tipo=comboPlus("Natural","Cultural","Histórico","Religioso","Deportivo","Recreativo","Mirador","Arqueológico","Otros");TextField ubicacion=new TextField(),descripcion=new TextField(),horario=new TextField();f.put("tipo",tipo);f.put("direccion",ubicacion);f.put("detalle",descripcion);f.put("horario",horario);addComboField(g,1,tr("Tipo *","Type *"),tipo,()->agregarOpcion(tipo,"atractivos_tipos_extra",tr("Nuevo tipo","New type")));addFormField(g,0,2,ubicacion,tr("Ubicación *","Location *"),180);addFormField(g,0,3,descripcion,tr("Descripción *","Description *"),255);addScheduleField(g,4,tr("Horario","Schedule"),horario);
        }else if(modulo.equals("Gastronomía")){
            ComboBox<String>tipo=comboPlus(gastronomiaTipos().toArray(String[]::new));TextField especialidad=new TextField(),horario=new TextField();f.put("tipo",tipo);f.put("detalle",especialidad);f.put("horario",horario);addComboField(g,1,tr("Tipo de gastronomía *","Gastronomy type *"),tipo,()->agregarOpcion(tipo,"gastronomia_tipos_extra",tr("Nuevo tipo","New type")));addFormField(g,0,2,especialidad,tr("Especialidad *","Specialty *"),255);addScheduleField(g,3,tr("Horario","Schedule"),horario);
        }else if(modulo.equals("Servicios")){
            ComboBox<String>tipo=comboPlus("Policía","Bomberos","Hospital","Defensa Civil","Protección Civil","Emergencia","Otros");TextField direccion=new TextField(),telefono=new TextField(),horario=new TextField();f.put("tipo",tipo);f.put("direccion",direccion);f.put("extra",telefono);f.put("detalle",horario);addComboField(g,1,tr("Tipo *","Type *"),tipo,()->agregarOpcion(tipo,"servicios_tipos_extra",tr("Nuevo tipo","New type")));addFormField(g,0,2,direccion,tr("Dirección *","Address *"),180);addFormField(g,0,3,telefono,tr("Teléfono","Phone"),40);addScheduleField(g,4,tr("Horario","Schedule"),horario);
        }else{
            DatePicker fecha=new DatePicker();fecha.setMaxWidth(Double.MAX_VALUE);TextField lugar=new TextField(),descripcion=new TextField(),telefono=new TextField();f.put("fecha",fecha);f.put("direccion",lugar);f.put("detalle",descripcion);f.put("telefono",telefono);addFormField(g,0,1,fecha,tr("Fecha *","Date *"),20);addFormField(g,0,2,lugar,tr("Lugar *","Place *"),180);addFormField(g,0,3,descripcion,tr("Descripción *","Description *"),255);addFormField(g,0,4,telefono,tr("Teléfono","Phone"),40);
        }
        if(id!=null)try{var m=CatalogService.get().listar(modulo,true,"").stream().filter(x->Objects.equals(x.get("id"),id)).findFirst().orElse(null);if(m!=null)for(String k:f.keySet())setControlFromValue(f.get(k),Objects.toString(m.get(k),""));}catch(Exception e){error(e.getMessage());}
        boolean noScroll=modulo.equals("Servicios")||modulo.equals("Gastronomía");if(noScroll)d.getDialogPane().setContent(page);else{ScrollPane sp=new ScrollPane(page);sp.setFitToWidth(true);sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);d.getDialogPane().setContent(sp);}
        ButtonType ok=new ButtonType(tr("Guardar","Save"),ButtonBar.ButtonData.OK_DONE);d.getDialogPane().getButtonTypes().addAll(ok,ButtonType.CANCEL);Button save=(Button)d.getDialogPane().lookupButton(ok);save.getStyleClass().add("btn-primary");
        save.addEventFilter(javafx.event.ActionEvent.ACTION,e->{
            Map<String,String>m=collect(f);List<String>er=new ArrayList<>();
            for(String k:f.keySet()){
                boolean req=k.equals("nombre")||k.equals("tipo")||k.equals("direccion")||k.equals("fecha");
                if(req&&m.getOrDefault(k,"").isBlank())er.add(label(k)+" "+tr("es obligatorio.","is required."));
            }
            if(modulo.equals("Eventos")&&m.getOrDefault("fecha","").isBlank())er.add(tr("La fecha es obligatoria.","Date is required."));
            if(modulo.equals("Actividades")&&!m.getOrDefault("precio","").isBlank()&&!m.get("precio").matches("[0-9]+([.,][0-9]{1,2})?"))er.add(tr("El precio debe ser numérico.","Price must be numeric."));
            if(!er.isEmpty()){e.consume();error(tr("Corrige los siguientes datos antes de guardar:","Correct the following data before saving:")+"\n\n• "+String.join("\n• ",er));}
        });
        d.setResultConverter(b->b==ok?collect(f):null);d.getDialogPane().setMinWidth(560);d.getDialogPane().setPrefWidth(620);d.getDialogPane().setPrefHeight(noScroll?420:560);styleDialog(d);d.showAndWait().ifPresent(m->{try{CatalogService.get().guardar(id,modulo,m);reload.run();NotificationManager.getInstance().mostrarNotificacion(tr("Registro guardado correctamente","Record saved successfully"),"exito");}catch(Exception e){error(e.getMessage());}});
    }

    private void addFormField(GridPane g,int col,int row,Control c,String title,int max){Label l=new Label(title);l.setWrapText(true);if(title.endsWith("*")){l.getStyleClass().add("required");requiredMark(l,c,1);}GridPane.setHgrow(c,Priority.ALWAYS);c.setMaxWidth(Double.MAX_VALUE);c.setMinHeight(38);g.add(l,0,row);g.add(c,1,row);}
    private void addComboField(GridPane g,int row,String title,ComboBox<String> combo,Runnable plus){Label l=new Label(title);l.setWrapText(true);if(title.endsWith("*")){l.getStyleClass().add("required");requiredMark(l,combo,1);}Button b=new Button("+");b.getStyleClass().add("abm-plus");b.setMinSize(32,32);b.setPrefSize(32,32);b.setOnAction(e->plus.run());HBox box=new HBox(6,combo,b);HBox.setHgrow(combo,Priority.ALWAYS);box.setMaxWidth(Double.MAX_VALUE);g.add(l,0,row);g.add(box,1,row);}
    private void addEmailField(GridPane g,int row,String title,TextField local,ComboBox<String> domain){Label l=new Label(title);l.getStyleClass().add("required");requiredMark(l,local,1);Button addDomain=new Button("+");addDomain.getStyleClass().add("email-add-domain");addDomain.setMinSize(32,32);addDomain.setOnAction(e->agregarDominioEmail(domain));HBox box=new HBox(6,local,domain,addDomain);HBox.setHgrow(local,Priority.ALWAYS);domain.setPrefWidth(135);g.add(l,0,row);g.add(box,1,row);}
    private void addScheduleField(GridPane g,int row,String title,TextField value){Label l=new Label(title);ComboBox<String>from=combo(timeOptions()),to=combo(timeOptions());from.setPrefWidth(120);to.setPrefWidth(120);Label fromLabel=new Label(tr("Desde","From")),toLabel=new Label(tr("Hasta","To"));fromLabel.setMinWidth(42);toLabel.setMinWidth(42);String initial=value.getText();if(initial.contains(" - ")){String[]p=initial.split(" - ",2);from.setValue(p[0]);to.setValue(p[1]);}from.setPromptText(tr("Seleccionar hora","Select time"));to.setPromptText(tr("Seleccionar hora","Select time"));from.valueProperty().addListener((o,a,b)->value.setText(scheduleText(from,to)));to.valueProperty().addListener((o,a,b)->value.setText(scheduleText(from,to)));HBox box=new HBox(7,fromLabel,from,toLabel,to);g.add(l,0,row);g.add(box,1,row);value.getProperties().put("scheduleFrom",from);value.getProperties().put("scheduleTo",to);value.setVisible(false);}
    private String scheduleText(ComboBox<String>from,ComboBox<String>to){return from.getValue()==null&&to.getValue()==null?"":Objects.toString(from.getValue(),"")+" - "+Objects.toString(to.getValue(),"");}
    private ComboBox<String> combo(String...items){ComboBox<String>c=new ComboBox<>(FXCollections.observableArrayList(items));c.setMaxWidth(Double.MAX_VALUE);return c;}
    private ComboBox<String> combo(List<String>items){ComboBox<String>c=new ComboBox<>(FXCollections.observableArrayList(items));c.setMaxWidth(Double.MAX_VALUE);return c;}
    private ComboBox<String> comboPlus(String...items){return comboPlus(Arrays.asList(items));}
    private ComboBox<String> comboPlus(List<String>items){return combo(items);}
    private List<String>timeOptions(){List<String>v=new ArrayList<>();for(int h=0;h<24;h++)for(int m:new int[]{0,30})v.add(String.format("%02d:%02d",h,m));return v;}
    private List<String>capacityDefaults(){List<String>v=new ArrayList<>();for(int i=1;i<=200;i++)v.add(String.valueOf(i));return v;}
    private List<String>configList(String key,String[]defaults){List<String>v=new ArrayList<>(Arrays.asList(defaults));try{String x=DatabaseConnection.getInstance().leerConfiguracion(key,"");for(String a:x.split("\\n"))if(!a.isBlank()&&!v.contains(a.trim()))v.add(a.trim());}catch(Exception ignored){}return v;}
    private List<String>countryOptions(){return configList("paises_extra",CountryProvinceService.countries().toArray(String[]::new));}
    private List<String>provinceOptions(String country){if(country==null||country.isBlank())return List.of();List<String>v=new ArrayList<>(CountryProvinceService.provinces(country));try{String x=DatabaseConnection.getInstance().leerConfiguracion("provincias_"+country," ");for(String a:x.split("\\n"))if(!a.isBlank()&&!v.contains(a.trim()))v.add(a.trim());}catch(Exception ignored){}return v;}
    private void agregarOpcion(ComboBox<String>combo,String key,String title){TextInputDialog td=new TextInputDialog();td.setTitle(title);td.setHeaderText(null);td.setContentText(tr("Nombre:","Name:"));td.showAndWait().ifPresent(v->{String x=v.trim();if(x.isBlank()||combo.getItems().contains(x))return;combo.getItems().add(x);combo.setValue(x);try{String old=DatabaseConnection.getInstance().leerConfiguracion(key,"");DatabaseConnection.getInstance().guardarConfiguracion(key,old.isBlank()?x:old+"\n"+x);}catch(Exception e){error(e.getMessage());}});}
    private void agregarOpcionProvincia(ComboBox<String>provincia,ComboBox<String>pais){if(pais.getValue()==null||pais.getValue().isBlank()){error(tr("Primero selecciona un país.","Select a country first."));return;}TextInputDialog td=new TextInputDialog();td.setTitle(tr("Nueva provincia","New province"));td.setHeaderText(null);td.setContentText(tr("Nombre:","Name:"));td.showAndWait().ifPresent(v->{String x=v.trim();if(x.isBlank()||provincia.getItems().contains(x))return;provincia.getItems().add(x);provincia.setValue(x);try{String key="provincias_"+pais.getValue();String old=DatabaseConnection.getInstance().leerConfiguracion(key,"");DatabaseConnection.getInstance().guardarConfiguracion(key,old.isBlank()?x:old+"\n"+x);}catch(Exception e){error(e.getMessage());}});}
    private void agregarDominioEmail(ComboBox<String>domain){TextInputDialog td=new TextInputDialog("@ejemplo.com");td.setTitle(tr("Nuevo dominio de correo","New email domain"));td.setHeaderText(null);td.setContentText(tr("Dominio:","Domain:"));td.showAndWait().ifPresent(v->{String x=v.trim().toLowerCase();if(!x.startsWith("@"))x="@"+x;if(!domain.getItems().contains(x))domain.getItems().add(x);domain.setValue(x);});}
    private String internalDocumentType(String v){if(v==null)return "";return switch(v){case "National ID"->"DNI";case "Passport"->"Pasaporte";case "Identity Card"->"Cédula";case "Civic Booklet"->"Libreta Cívica";default->v;};}
    private void setControlFromValue(Control c,String v){if(c instanceof TextInputControl ti){ti.setText(v);Object a=ti.getProperties().get("scheduleFrom"),b=ti.getProperties().get("scheduleTo");if(a instanceof ComboBox<?> from&&b instanceof ComboBox<?> to&&v.contains(" - ")){String[]p=v.split(" - ",2);((ComboBox<String>)from).setValue(p[0]);((ComboBox<String>)to).setValue(p[1]);}}else if(c instanceof DatePicker dp&&!v.isBlank())try{dp.setValue(LocalDate.parse(v.substring(0,10)));}catch(Exception ignored){}else if(c instanceof ComboBox<?> cb){@SuppressWarnings("unchecked") ComboBox<String>x=(ComboBox<String>)cb;if(!x.getItems().contains(v)&&!v.isBlank())x.getItems().add(v);x.setValue(v);}}
    private List<String>gastronomiaTipos(){String[]es={"Restaurante","Cafetería","Bar","Bar de tapas","Bodegón","Parrilla","Asador","Pizzería","Hamburguesería","Comida rápida","Comida al paso","Sandwichería","Rotisería","Fábrica de pastas","Casa de pastas","Panadería","Confitería","Pastelería","Heladería","Chocolatería","Dulcería","Kiosco","Despensa","Almacén","Supermercado","Minimercado","Pollería","Carnicería","Pescadería","Marisquería","Verdulería","Frutería","Dietética","Casa de comidas","Viandas","Catering","Food truck","Puesto de comida","Puesto de choripán","Puesto de empanadas","Puesto de shawarma","Puesto de tacos","Puesto de panchos","Puesto de arepas","Puesto de crepes","Puesto de waffles","Puesto de jugos","Juguería","Bar de jugos","Casa de té","Tetería","Cervecería","Vinoteca","Restobar","Parador gastronómico","Mercado gastronómico","Patio de comidas","Comedor","Comedor escolar","Comedor comunitario","Servicio de delivery","Rotisería saludable","Restaurante vegetariano","Restaurante vegano","Restaurante de comida étnica","Restaurante de cocina internacional","Restaurante de cocina regional","Casa de empanadas","Casa de sushi","Sushi bar","Parrilla vegetariana","Fábrica de cerveza artesanal","Fábrica de helados","Puesto de helados","Otros"};return idioma.equals("en")?Arrays.stream(es).map(this::tipoGastronomiaDisplay).toList():Arrays.asList(es);}
    private String tipoGastronomiaDisplay(String v){if(!idioma.equals("en"))return v;return switch(v){case "Restaurante"->"Restaurant";case "Cafetería"->"Cafeteria";case "Parrilla"->"Steakhouse";case "Pollería"->"Chicken shop";case "Verdulería"->"Greengrocer";case "Heladería"->"Ice cream shop";case "Comida rápida"->"Fast food";case "Puesto de venta de shawarma","Puesto de shawarma"->"Shawarma stand";case "Pizzería"->"Pizzeria";case "Hamburguesería"->"Burger restaurant";case "Panadería"->"Bakery";case "Pastelería"->"Pastry shop";case "Carnicería"->"Butcher shop";case "Pescadería"->"Fishmonger";case "Frutería"->"Fruit shop";case "Food truck"->"Food truck";case "Catering"->"Catering";case "Cervecería"->"Brewery";case "Vinoteca"->"Wine shop";case "Restobar"->"Restobar";case "Casa de té"->"Tea house";case "Tetería"->"Tea shop";case "Kiosco"->"Kiosk";case "Despensa"->"Grocery store";case "Almacén"->"Grocery";case "Supermercado"->"Supermarket";case "Minimercado"->"Mini market";case "Casa de sushi"->"Sushi restaurant";case "Sushi bar"->"Sushi bar";case "Patio de comidas"->"Food court";case "Mercado gastronómico"->"Food market";case "Parador gastronómico"->"Food stop";case "Puesto de comida"->"Food stand";case "Comedor"->"Dining hall";case "Servicio de delivery"->"Delivery service";case "Restaurante vegetariano"->"Vegetarian restaurant";case "Restaurante vegano"->"Vegan restaurant";case "Restaurante de comida étnica"->"Ethnic restaurant";case "Restaurante de cocina internacional"->"International cuisine restaurant";case "Restaurante de cocina regional"->"Regional cuisine restaurant";case "Casa de empanadas"->"Empanada shop";case "Puesto de choripán"->"Choripan stand";case "Puesto de empanadas"->"Empanada stand";case "Puesto de tacos"->"Taco stand";case "Puesto de panchos"->"Hot dog stand";case "Puesto de arepas"->"Arepa stand";case "Puesto de crepes"->"Crepe stand";case "Puesto de waffles"->"Waffle stand";case "Puesto de jugos"->"Juice stand";case "Juguería"->"Juice bar";case "Bar de jugos"->"Juice bar";case "Bar de tapas"->"Tapas bar";case "Bodegón"->"Traditional restaurant";case "Asador"->"Grill restaurant";case "Rotisería"->"Takeaway deli";case "Fábrica de pastas"->"Pasta shop";case "Casa de pastas"->"Pasta restaurant";case "Confitería"->"Confectionery";case "Chocolatería"->"Chocolate shop";case "Dulcería"->"Candy shop";case "Dietética"->"Health food store";case "Casa de comidas"->"Takeaway restaurant";case "Viandas"->"Meal service";case "Rotisería saludable"->"Healthy takeaway deli";case "Parrilla vegetariana"->"Vegetarian grill";case "Fábrica de cerveza artesanal"->"Craft brewery";case "Fábrica de helados"->"Ice cream factory";case "Puesto de helados"->"Ice cream stand";case "Otros"->"Other";default->v;};}
    private String tipoGastronomiaInterno(String v){if(!idioma.equals("en"))return v;for(String es:gastronomiaTiposInternos())if(tipoGastronomiaDisplay(es).equals(v))return es;return v;}
    private List<String>gastronomiaTiposInternos(){List<String>base=new ArrayList<>(List.of("Restaurante","Cafetería","Bar","Bar de tapas","Bodegón","Parrilla","Asador","Pizzería","Hamburguesería","Comida rápida","Comida al paso","Sandwichería","Rotisería","Fábrica de pastas","Casa de pastas","Panadería","Confitería","Pastelería","Heladería","Chocolatería","Dulcería","Kiosco","Despensa","Almacén","Supermercado","Minimercado","Pollería","Carnicería","Pescadería","Marisquería","Verdulería","Frutería","Dietética","Casa de comidas","Viandas","Catering","Food truck","Puesto de comida","Puesto de choripán","Puesto de empanadas","Puesto de shawarma","Puesto de tacos","Puesto de panchos","Puesto de arepas","Puesto de crepes","Puesto de waffles","Puesto de jugos","Juguería","Bar de jugos","Casa de té","Tetería","Cervecería","Vinoteca","Restobar","Parador gastronómico","Mercado gastronómico","Patio de comidas","Comedor","Comedor escolar","Comedor comunitario","Servicio de delivery","Rotisería saludable","Restaurante vegetariano","Restaurante vegano","Restaurante de comida étnica","Restaurante de cocina internacional","Restaurante de cocina regional","Casa de empanadas","Casa de sushi","Sushi bar","Parrilla vegetariana","Fábrica de cerveza artesanal","Fábrica de helados","Puesto de helados","Otros"));try{String extra=DatabaseConnection.getInstance().leerConfiguracion("gastronomia_tipos_extra","");if(!extra.isBlank())for(String x:extra.split("\\n"))if(!x.isBlank()&&!base.contains(x))base.add(x);}catch(Exception ignored){}return base;}

    // ==========================================
    // 1. DASHBOARD DE ALOJAMIENTOS CON FILTROS Y ESTADO
    // ==========================================
    private void mostrarAlojamientos(){
        base(tr("Gestión de Alojamientos","Accommodation management"),tr("Administración y registro de establecimientos turísticos","Administration and registration of tourist establishments"));
        VBox card=new VBox(12);card.getStyleClass().add("module-card");

        // ESTILO PARA LAS ETIQUETAS SUPERIORES
        String labelStyle = "-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #475569;";

        // 1. CAMPO DE BÚSQUEDA CON TÍTULO
        Label lblBuscar = new Label(tr("Buscar", "Search"));
        lblBuscar.setStyle(labelStyle);
        TextField q = new TextField();
        q.setPromptText(tr("Buscar alojamiento...", "Search accommodation..."));
        q.setPrefWidth(220);
        VBox boxBuscar = new VBox(3, lblBuscar, q);

        // 2. FILTRO TIPO CON TÍTULO Y LISTA DINÁMICA
        Label lblTipo = new Label(tr("Tipo", "Type"));
        lblTipo.setStyle(labelStyle);
        List<String> listaTipos = new ArrayList<>(List.of("Todos"));
        listaTipos.addAll(configList("alojamiento_tipos_extra", new String[]{
                "Albergue", "Apart Hotel", "Cabaña", "Camping", "Complejo Turístico",
                "Departamento / Casa", "Glamping", "Hostel", "Hostería", "Hotel",
                "Posada", "Residencial", "Otros"
        }));
        ComboBox<String> cbTipo = combo(listaTipos);
        cbTipo.setValue("Todos");
        VBox boxTipo = new VBox(3, lblTipo, cbTipo);

        // 3. FILTRO CATEGORÍA CON TÍTULO Y LISTA DINÁMICA
        Label lblCat = new Label(tr("Categoría", "Category"));
        lblCat.setStyle(labelStyle);
        List<String> listaCat = new ArrayList<>(List.of("Todas"));
        listaCat.addAll(configList("alojamiento_categorias_extra", new String[]{
                "Sin categoría", "1 estrella", "2 estrellas", "3 estrellas", "4 estrellas",
                "5 estrellas", "Superior", "Premium", "Boutique", "Otros"
        }));
        ComboBox<String> cbCategoria = combo(listaCat);
        cbCategoria.setValue("Todas");
        VBox boxCat = new VBox(3, lblCat, cbCategoria);

        // 4. FILTRO ESTADO CON TÍTULO (TODOS / ACTIVO / INACTIVO)
        Label lblEstado = new Label(tr("Estado", "Status"));
        lblEstado.setStyle(labelStyle);
        ComboBox<String> cbEstado = combo(new String[]{"Todos", "Activo", "Inactivo"});
        cbEstado.setValue("Todos");
        VBox boxEstado = new VBox(3, lblEstado, cbEstado);

        // BOTONES DE ACCIÓN
        Button ref = new Button(tr("Actualizar", "Refresh"));
        ref.getStyleClass().add("btn-secondary");

        // BOTÓN NUEVO
        Button add = new Button(tr("Nuevo", "New"));
        add.getStyleClass().add("btn-primary");
        add.setDisable(!SessionManager.getInstance().puedeEditarAlojamientos());

        Region spc = new Region();
        HBox.setHgrow(spc, Priority.ALWAYS);

        // BARRA DE FILTROS ALINEADA EN LA BASE
        HBox filter = new HBox(10);
        filter.setAlignment(Pos.BOTTOM_LEFT);
        filter.getChildren().addAll(boxBuscar, boxTipo, boxCat, boxEstado, ref, spc, add);

        // TABLA PRINCIPAL
        TableView<Map<String,Object>> t = new TableView<>();
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        t.setFixedCellSize(Math.max(34, 42 * fontScale));
        t.setPrefHeight(42 + 8 * 42 + 8);
        Pager<Map<String,Object>> pager = new Pager<>(t, 8);

        // COLUMNAS
        addMapColumn(t, "nombre", tr("Nombre", "Name"));
        addMapColumn(t, "tipo", tr("Tipo", "Type"));
        addMapColumn(t, "categoria", tr("Categoría", "Category"));
        addMapColumn(t, "direccion", tr("Dirección", "Address"));
        addMapColumn(t, "capacidad", tr("Plazas", "Places"));
        addMapColumn(t, "telefono", tr("Teléfono", "Phone"));

        // COLUMNA ESTADO CON BADGES (ACTIVO / INACTIVO)
        TableColumn<Map<String,Object>,Object> colEstado = new TableColumn<>(tr("Estado", "Status"));
        colEstado.setPrefWidth(110);
        colEstado.setCellValueFactory(cell -> new javafx.beans.property.ReadOnlyObjectWrapper<>(cell.getValue() == null ? "" : cell.getValue().get("estado")));
        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    String est = Objects.toString(item, "Activo");
                    boolean esActivo = est.equalsIgnoreCase("Activo") || est.equalsIgnoreCase("Active");
                    Label badge = new Label(tr(esActivo ? "Activo" : "Inactivo", esActivo ? "Active" : "Inactive"));
                    badge.getStyleClass().add(esActivo ? "badge-activo" : "badge-inactivo");
                    if (esActivo) {
                        badge.setStyle("-fx-background-color: rgba(34, 197, 94, 0.2); -fx-text-fill: #22c55e; -fx-padding: 3 9 3 9; -fx-background-radius: 12; -fx-font-weight: bold;");
                    } else {
                        badge.setStyle("-fx-background-color: rgba(239, 68, 68, 0.2); -fx-text-fill: #ef4444; -fx-padding: 3 9 3 9; -fx-background-radius: 12; -fx-font-weight: bold;");
                    }
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        t.getColumns().add(colEstado);

        // CARGA Y FILTRADO COMBINADO CON ORDEN ALFABÉTICO
        Runnable load = () -> {
            try {
                List<Map<String,Object>> all = new ArrayList<>();
                all.addAll(AlojamientoService.get().listar(true, ""));  // Activos
                all.addAll(AlojamientoService.get().listar(false, "")); // Inactivos

                // Orden alfabético para ver activos e inactivos juntos desde la primera página
                all.sort((m1, m2) -> Objects.toString(m1.get("nombre"), "")
                        .compareToIgnoreCase(Objects.toString(m2.get("nombre"), "")));

                String texto = q.getText() != null ? q.getText().toLowerCase().trim() : "";
                String tipoSel = cbTipo.getValue();
                String catSel = cbCategoria.getValue();
                String estSel = cbEstado.getValue();

                List<Map<String,Object>> filtrados = all.stream().filter(m -> {
                    String nom = Objects.toString(m.get("nombre"), "").toLowerCase();
                    String dir = Objects.toString(m.get("direccion"), "").toLowerCase();
                    boolean matchTxt = texto.isEmpty() || nom.contains(texto) || dir.contains(texto);

                    String tVal = Objects.toString(m.get("tipo"), "");
                    boolean matchTipo = tipoSel == null || tipoSel.equals("Todos") || tVal.equalsIgnoreCase(tipoSel);

                    String cVal = Objects.toString(m.get("categoria"), "");
                    boolean matchCat = catSel == null || catSel.equals("Todas") || cVal.equalsIgnoreCase(catSel);

                    String eVal = Objects.toString(m.get("estado"), "Activo");
                    boolean matchEst = estSel == null || estSel.equals("Todos") || eVal.equalsIgnoreCase(estSel);

                    return matchTxt && matchTipo && matchCat && matchEst;
                }).toList();

                pager.setData(filtrados);
                applyFontScale(t);
            } catch (Exception e) {
                error(e.getMessage());
            }
        };

        // ACCIONES CON BINDING SEGURO (LOS BOTONES NUNCA DESAPARECEN)
        TableColumn<Map<String,Object>, Map<String,Object>> actions = new TableColumn<>(tr("Acciones", "Actions"));
        actions.setPrefWidth(145);
        actions.setCellValueFactory(cellData -> new javafx.beans.property.ReadOnlyObjectWrapper<>(cellData.getValue()));
        actions.setCellFactory(col -> new TableCell<>() {
            final Button view = actionButton("◉", "action-view");
            final Button edit = actionButton("✎", "action-edit");
            final Button del = actionButton("🗑", "action-delete");
            final HBox box = new HBox(5, view, edit, del);
            {
                box.setAlignment(Pos.CENTER);
                view.setOnAction(e -> {
                    Map<String, Object> m = getItem();
                    if (m != null) mostrarDetalleAlojamiento(m);
                });
                edit.setOnAction(e -> {
                    Map<String, Object> m = getItem();
                    if (m != null) formAlojamiento(load, ((Number)m.get("id")).intValue());
                });
                del.setOnAction(e -> {
                    Map<String, Object> m = getItem();
                    if (m != null) {
                        String nom = Objects.toString(m.get("nombre"), "");

                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle(tr("Baja de Establecimiento", "Deactivate Accommodation"));
                        alert.setHeaderText(null);
                        alert.setContentText(tr(
                                "¿Está seguro de que desea dar de baja '" + nom + "'?\n\nEl establecimiento pasará a estado 'Inactivo' y se mantendrá en el historial.",
                                "Are you sure you want to deactivate '" + nom + "'?\n\nThe accommodation will become 'Inactive'."
                        ));
                        ButtonType btnConfirmar = new ButtonType(tr("Dar de baja", "Deactivate"), ButtonBar.ButtonData.OK_DONE);
                        ButtonType btnCancelar = new ButtonType(tr("Cancelar", "Cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
                        alert.getButtonTypes().setAll(btnConfirmar, btnCancelar);
                        styleAlert(alert);

                        alert.showAndWait().ifPresent(resp -> {
                            if (resp == btnConfirmar) {
                                try {
                                    AlojamientoService.get().estado(((Number)m.get("id")).intValue(), false);
                                    NotificationManager.getInstance().mostrarNotificacion(tr("Alojamiento dado de baja correctamente", "Accommodation deactivated"), "exito");
                                    load.run();
                                } catch (Exception x) {
                                    error(x.getMessage());
                                }
                            }
                        });
                    }
                });
                edit.setDisable(!SessionManager.getInstance().puedeEditarAlojamientos());
                del.setDisable(!SessionManager.getInstance().puedeEditarAlojamientos());
            }

            @Override
            protected void updateItem(Map<String, Object> item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    setGraphic(box);
                }
            }
        });
        t.getColumns().add(actions);

        // LISTENERS REACTIVOS
        q.textProperty().addListener((o, a, b) -> load.run());
        cbTipo.valueProperty().addListener((o, a, b) -> load.run());
        cbCategoria.valueProperty().addListener((o, a, b) -> load.run());
        cbEstado.valueProperty().addListener((o, a, b) -> load.run());
        ref.setOnAction(e -> load.run());
        add.setOnAction(e -> formAlojamiento(load, null));

        load.run();
        card.getChildren().addAll(filter, t, pager.controls());
        content.getChildren().add(card);

        try {
            List<Map<String,Object>> all = AlojamientoService.get().listar(true, "");
            content.getChildren().addAll(metricCardsAlojamientos(all), recentCard(all, "Alojamientos"));
        } catch (Exception e) {
            error(e.getMessage());
        }
    }

    // ==========================================
    // 2. FORMULARIO NUEVO / EDITAR CON AMENITIES COMPLETAS
    // ==========================================
    private void formAlojamiento(Runnable reload,Integer id){
        Dialog<Map<String,String>> d=new Dialog<>();
        d.setTitle(id==null?tr("Nuevo alojamiento","New accommodation"):tr("Modificar alojamiento","Edit accommodation"));
        VBox page=new VBox(12);page.setPadding(new Insets(16));
        GridPane g=formGrid();g.getStyleClass().add("user-abm-grid");
        page.getChildren().add(g);

        // CONTROLES CORREGIDOS
        TextField nombre=new TextField();
        nombre.setPromptText("Ej: Posada Serrana");

        ComboBox<String>tipo=comboPlus(configList("alojamiento_tipos_extra",new String[]{"Albergue","Apart Hotel","Cabaña","Camping","Complejo Turístico","Departamento / Casa","Glamping","Hostel","Hostería","Hotel","Posada","Residencial","Otros"}).toArray(String[]::new));
        ComboBox<String>categoria=comboPlus(configList("alojamiento_categorias_extra",new String[]{"Sin categoría","1 estrella","2 estrellas","3 estrellas","4 estrellas","5 estrellas","Superior","Premium","Boutique","Otros"}).toArray(String[]::new));

        TextField capacidad=new TextField();
        capacidad.setPromptText("Ej: 6");

        TextField direccion=new TextField();
        direccion.setPromptText("Ej: Av. San Martín 450");

        TextField telefono=new TextField();
        telefono.setPromptText("Ej: 3541-558899");

        TextField dueno=new TextField();
        dueno.setPromptText("Nombre y Apellido");

        TextField dni=new TextField();
        dni.setPromptText("Sin puntos");

        // CHECKBOXES DE AMENITIES SIN TRUNCAR (NO SE CORTAN NUNCA)
        CheckBox chkWifi = new CheckBox("Wifi");
        CheckBox chkCochera = new CheckBox("Cochera");
        CheckBox chkPileta = new CheckBox("Pileta");
        CheckBox chkDesayuno = new CheckBox("Desayuno");
        CheckBox chkClima = new CheckBox("Climatización");

        for (CheckBox cb : new CheckBox[]{chkWifi, chkCochera, chkPileta, chkDesayuno, chkClima}) {
            cb.setMinWidth(Region.USE_PREF_SIZE);
        }
        FlowPane boxAmenities = new FlowPane(12, 8, chkWifi, chkCochera, chkPileta, chkDesayuno, chkClima);
        boxAmenities.setAlignment(Pos.CENTER_LEFT);

        TextArea descripcion=new TextArea();
        descripcion.setPrefRowCount(2);
        descripcion.setPrefHeight(60);
        descripcion.setWrapText(true);
        descripcion.setPromptText("Breve descripción u observaciones del establecimiento...");

        TextField foto=new TextField();
        foto.setPromptText("https://...");

        // DISTRIBUCIÓN EN EL GRID
        addFormField(g,0,0,nombre,tr("Nombre del Establecimiento *","Name *"),120);
        addComboField(g,1,tr("Tipo *","Type *"),tipo,()->agregarOpcion(tipo,"alojamiento_tipos_extra",tr("Nuevo tipo","New type")));
        addComboField(g,2,tr("Categoría *","Category *"),categoria,()->agregarOpcion(categoria,"alojamiento_categorias_extra",tr("Nueva categoría","New category")));
        addFormField(g,0,3,capacidad,tr("Capacidad (Plazas) *","Capacity *"),10);
        addFormField(g,0,4,direccion,tr("Dirección *","Address *"),180);
        addFormField(g,0,5,telefono,tr("Teléfono de Contacto *","Phone *"),40);
        addFormField(g,0,6,dueno,tr("Nombre del prestador (Opcional)","Provider name"),120);
        addFormField(g,0,7,dni,tr("DNI del prestador (Opcional)","Provider ID"),40);

        Label lblAm = new Label(tr("Servicios y Comodidades","Amenities"));
        g.add(lblAm, 0, 8);
        g.add(boxAmenities, 1, 8);

        Label lblDesc = new Label(tr("Descripción","Description"));
        g.add(lblDesc, 0, 9);
        g.add(descripcion, 1, 9);

        addFormField(g,0,10,foto,tr("URL de foto (Opcional)","Photo URL"),500);

        // PRECARGA SI ES EDICIÓN
        if(id!=null) {
            try {
                List<Map<String,Object>> all = new ArrayList<>();
                all.addAll(AlojamientoService.get().listar(true, ""));
                all.addAll(AlojamientoService.get().listar(false, ""));
                var m = all.stream().filter(x->Objects.equals(x.get("id"),id)).findFirst().orElse(null);

                if(m!=null){
                    nombre.setText(Objects.toString(m.get("nombre"),""));
                    tipo.setValue(Objects.toString(m.get("tipo"),""));
                    categoria.setValue(Objects.toString(m.get("categoria"),""));
                    capacidad.setText(Objects.toString(m.get("capacidad"),""));
                    direccion.setText(Objects.toString(m.get("direccion"),""));
                    telefono.setText(Objects.toString(m.get("telefono"),""));
                    dueno.setText(Objects.toString(m.get("dueno"),""));
                    dni.setText(Objects.toString(m.get("dni"),""));
                    descripcion.setText(Objects.toString(m.get("descripcion"),""));
                    foto.setText(Objects.toString(m.get("foto"),""));

                    String am = Objects.toString(m.get("amenities"),"");
                    chkWifi.setSelected(am.contains("Wifi"));
                    chkCochera.setSelected(am.contains("Cochera"));
                    chkPileta.setSelected(am.contains("Pileta"));
                    chkDesayuno.setSelected(am.contains("Desayuno"));
                    chkClima.setSelected(am.contains("Climatización") || am.contains("Climatizacion"));
                }
            } catch(Exception e){
                error(e.getMessage());
            }
        }

        ScrollPane sp=new ScrollPane(page);sp.setFitToWidth(true);sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        d.getDialogPane().setContent(sp);

        ButtonType ok=new ButtonType(tr("Guardar","Save"),ButtonBar.ButtonData.OK_DONE);
        d.getDialogPane().getButtonTypes().addAll(ok,ButtonType.CANCEL);
        Button save=(Button)d.getDialogPane().lookupButton(ok);save.getStyleClass().add("btn-primary");

        // VALIDACIONES OBLIGATORIAS
        save.addEventFilter(javafx.event.ActionEvent.ACTION,e->{
            List<String>er=new ArrayList<>();
            if(nombre.getText().trim().isEmpty()) er.add(tr("El nombre del establecimiento es obligatorio.","Name is required."));
            if(tipo.getValue()==null || tipo.getValue().trim().isEmpty()) er.add(tr("El tipo es obligatorio.","Type is required."));
            if(categoria.getValue()==null || categoria.getValue().trim().isEmpty()) er.add(tr("La categoría es obligatoria.","Category is required."));
            if(direccion.getText().trim().isEmpty()) er.add(tr("La dirección es obligatoria.","Address is required."));
            if(telefono.getText().trim().isEmpty()) er.add(tr("El teléfono de contacto es obligatorio.","Phone is required."));

            if(capacidad.getText().trim().isEmpty()){
                er.add(tr("La capacidad de plazas es obligatoria.","Capacity is required."));
            } else {
                try {
                    int cap = Integer.parseInt(capacidad.getText().trim());
                    if(cap <= 0) er.add(tr("La capacidad debe ser mayor a 0.","Capacity must be greater than 0."));
                } catch(NumberFormatException ex){
                    er.add(tr("La capacidad debe ser un número entero válido.","Capacity must be a valid integer."));
                }
            }

            if(!er.isEmpty()){
                e.consume();
                error(tr("Corrige los siguientes datos antes de guardar:","Correct the following data before saving:")+"\n\n• "+String.join("\n• ",er));
            }
        });

        d.setResultConverter(b->{
            if(b!=ok) return null;
            Map<String,String> m = new LinkedHashMap<>();
            m.put("nombre", nombre.getText().trim());
            m.put("tipo", Objects.toString(tipo.getValue(),"").trim());
            m.put("categoria", Objects.toString(categoria.getValue(),"").trim());
            m.put("capacidad", capacidad.getText().trim());
            m.put("direccion", direccion.getText().trim());
            m.put("telefono", telefono.getText().trim());
            m.put("dueno", dueno.getText().trim());
            m.put("dni", dni.getText().trim());
            m.put("descripcion", descripcion.getText().trim());
            m.put("foto", foto.getText().trim());

            List<String> amList = new ArrayList<>();
            if(chkWifi.isSelected()) amList.add("Wifi");
            if(chkCochera.isSelected()) amList.add("Cochera");
            if(chkPileta.isSelected()) amList.add("Pileta");
            if(chkDesayuno.isSelected()) amList.add("Desayuno");
            if(chkClima.isSelected()) amList.add("Climatización");
            m.put("amenities", String.join(", ", amList));

            return m;
        });

        d.getDialogPane().setMinWidth(600);d.getDialogPane().setPrefWidth(640);d.getDialogPane().setPrefHeight(580);
        styleDialog(d);

        d.showAndWait().ifPresent(m->{
            try{
                AlojamientoService.get().guardar(id,m);
                if(reload!=null) reload.run();
                NotificationManager.getInstance().mostrarNotificacion(tr("Alojamiento guardado correctamente","Accommodation saved successfully"),"exito");
            }catch(Exception e){
                error(e.getMessage());
            }
        });
    }

    // ==========================================
    // 3. DETALLE DE ALOJAMIENTO (FICHA TÉCNICA LIMPIA)
    // ==========================================
    private void mostrarDetalleAlojamiento(Map<String,Object> m) {
        String nom = Objects.toString(m.get("nombre"), "Alojamiento");
        String tipo = Objects.toString(m.get("tipo"), "-");
        String cat = Objects.toString(m.get("categoria"), "-");
        String cap = Objects.toString(m.get("capacidad"), "0");
        String dir = Objects.toString(m.get("direccion"), "-");
        String tel = Objects.toString(m.get("telefono"), "No registrado");
        String am = Objects.toString(m.get("amenities"), "");
        if (am.isBlank()) am = "Sin servicios declarados";

        String dueno = Objects.toString(m.get("dueno"), "");
        String dni = Objects.toString(m.get("dni"), "");
        String titular = (!dueno.isBlank()) ? dueno + (!dni.isBlank() ? " (DNI: " + dni + ")" : "") : "No especificado";

        String est = Objects.toString(m.get("estado"), "Activo");
        String desc = Objects.toString(m.get("descripcion"), "");
        if (desc.isBlank()) desc = "Sin observaciones adicionales.";

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(tr("Detalle del Alojamiento", "Accommodation Detail"));
        alert.setHeaderText(nom);
        alert.setContentText(
                "• Tipo y Categoría: " + tipo + " (" + cat + ")\n" +
                        "• Capacidad: " + cap + " plazas\n" +
                        "• Dirección: " + dir + "\n" +
                        "• Teléfono: " + tel + "\n" +
                        "• Servicios: " + am + "\n" +
                        "• Titular: " + titular + "\n" +
                        "• Estado: " + est + "\n\n" +
                        "Descripción:\n" + desc
        );
        styleAlert(alert);
        alert.showAndWait();
    }

    private VBox formSection(String title,Node contentNode){
        VBox box=new VBox(10); box.getStyleClass().add("form-section"); Label titleLabel=new Label(title); titleLabel.getStyleClass().add("form-section-title"); box.getChildren().addAll(titleLabel,contentNode); return box;
    }

    private HBox metricCardsTuristas(List<Map<String,Object>> data){
        long month=data.stream().filter(x->isThisMonth(x.get("fecha_registro"))).count();long prov=data.stream().map(x->Objects.toString(x.get("provincia"),"")).filter(v->!v.isBlank()).distinct().count();long pais=data.stream().map(x->Objects.toString(x.get("pais"),"")).filter(v->!v.isBlank()).distinct().count();
        return metricRow(new String[][]{{tr("Total de turistas","Total tourists"),String.valueOf(data.size()),tr("Turistas registrados","Registered tourists")},{tr("Registrados este mes","Registered this month"),String.valueOf(month),tr("Durante el mes actual","During current month")},{tr("Procedencias distintas","Distinct origins"),String.valueOf(prov),tr("De diferentes provincias","From different provinces")},{tr("Países distintos","Distinct countries"),String.valueOf(pais),tr("Países registrados","Registered countries")}});
    }
    private HBox metricCardsAlojamientos(List<Map<String,Object>> data){
        long month=data.stream().filter(x->isThisMonth(x.get("fecha_registro"))).count();long cap=data.stream().mapToLong(x->toLong(x.get("capacidad"))).sum();
        return metricRow(new String[][]{{tr("Total de alojamientos","Total accommodations"),String.valueOf(data.size()),tr("Alojamientos registrados","Registered accommodations")},{tr("Nuevos este mes","New this month"),String.valueOf(month),tr("Durante el mes actual","During current month")},{tr("Capacidad total (Plazas)","Total capacity"),String.valueOf(cap),tr("Plazas totales del municipio","Total municipal capacity")},{tr("Alojamientos activos","Active accommodations"),String.valueOf(data.size()),tr("Operando normalmente","Operating normally")}});
    }
    private HBox metricCardsCatalogo(List<Map<String,Object>> data,String modulo){long month=data.stream().filter(x->isThisMonth(x.get("fecha_registro"))).count();long types=data.stream().map(x->Objects.toString(x.get("tipo"),"")).filter(v->!v.isBlank()).distinct().count();return metricRow(new String[][]{{tr("Total de "+modulo,"Total "+eng(modulo)),String.valueOf(data.size()),tr("Registros activos","Active records")},{tr("Nuevos este mes","New this month"),String.valueOf(month),tr("Durante el mes actual","During current month")},{tr("Tipos distintos","Distinct types"),String.valueOf(types),tr("Categorías registradas","Registered categories")},{tr("Registros activos","Active records"),String.valueOf(data.size()),tr("Disponibles actualmente","Currently available")}});}
    private HBox metricRow(String[][] vals){HBox row=new HBox(15);row.setFillHeight(true);for(String[]v:vals){VBox c=new VBox(4);c.setMaxWidth(Double.MAX_VALUE);c.setPadding(new Insets(15));c.getStyleClass().add("card-metric");Label a=new Label(v[0]),b=new Label(v[1]),z=new Label(v[2]);a.getStyleClass().add("metric-label");b.getStyleClass().add("metric-value");z.getStyleClass().add("metric-detail");c.getChildren().addAll(a,b,z);row.getChildren().add(c);HBox.setHgrow(c,Priority.ALWAYS);}return row;}
    private VBox recentCard(List<Map<String,Object>> data,String titulo){VBox box=new VBox(10);box.getStyleClass().add("card");Label title=new Label("🕒 "+tr("Últimos "+titulo.toLowerCase()+" registrados","Latest "+eng(titulo).toLowerCase()+" registered"));title.getStyleClass().add("recent-title");TableView<Map<String,Object>>t=new TableView<>();t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);t.setFixedCellSize(Math.max(34,40*fontScale));t.setPrefHeight(8*40+38);t.setMinHeight(8*40+38);addMapColumn(t,"nombre",tr("Nombre","Name"));if(!data.isEmpty()&&data.get(0).containsKey("apellido"))addMapColumn(t,"apellido",tr("Apellido","Surname"));if(!data.isEmpty()&&data.get(0).containsKey("tipo"))addMapColumn(t,"tipo",tr("Tipo","Type"));if(!data.isEmpty()&&data.get(0).containsKey("direccion"))addMapColumn(t,"direccion",tr("Dirección","Address"));if(!data.isEmpty()&&data.get(0).containsKey("fecha_registro"))addMapColumn(t,"fecha_registro",tr("Fecha de registro","Registration date"));List<Map<String,Object>>sorted=new ArrayList<>(data);sorted.sort((a,b)->dateValue(b.get("fecha_registro")).compareTo(dateValue(a.get("fecha_registro"))));t.setItems(FXCollections.observableArrayList(sorted.subList(0,Math.min(8,sorted.size()))));box.getChildren().addAll(title,t);applyFontScale(box);return box;}
    private java.time.LocalDateTime dateValue(Object o){if(o instanceof java.sql.Timestamp x)return x.toLocalDateTime();if(o instanceof java.sql.Date x)return x.toLocalDate().atStartOfDay();if(o instanceof java.time.LocalDateTime x)return x;try{return java.time.LocalDateTime.parse(Objects.toString(o,"").replace(" ","T"));}catch(Exception e){return java.time.LocalDateTime.MIN;}}
    private boolean isThisMonth(Object o){java.time.LocalDateTime d=dateValue(o);java.time.LocalDate n=java.time.LocalDate.now();return d.getYear()==n.getYear()&&d.getMonthValue()==n.getMonthValue();}
    private long toLong(Object o){try{return Long.parseLong(Objects.toString(o,"0"));}catch(Exception e){return 0;}}

    private void mostrarPapelera(){
        base(tr("Papelera","Trash"),tr("Registros inactivos disponibles para restaurar.","Inactive records available for restoration."));
        List<String>opciones=new ArrayList<>();opciones.add("Todo");
        SessionManager sm=SessionManager.getInstance();
        if(sm.esAdministrador()){opciones.addAll(List.of("Usuarios","Turistas","Actividades","Atractivos","Alojamientos","Gastronomía","Servicios","Eventos"));}
        else if(sm.esOperador()){opciones.add("Turistas");}
        else if(sm.esInspector()){opciones.addAll(List.of("Actividades","Atractivos","Alojamientos","Gastronomía","Servicios","Eventos"));}
        ComboBox<String>tipo=new ComboBox<>(FXCollections.observableArrayList(opciones));
        if(!sm.esAdministrador())tipo.getItems().remove("Todo");
        tipo.getSelectionModel().selectFirst();
        TextField search=new TextField();search.setPromptText(tr("Buscar","Search"));
        TableView<Map<String,Object>>t=new TableView<>();t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);t.setFixedCellSize(Math.max(34,46*fontScale));t.setPrefHeight(42+8*46+8);t.setMinHeight(42+8*46+8);
        Pager<Map<String,Object>>pager=new Pager<>(t,8);
        Button restore=new Button(tr("Restaurar","Restore"));Button restoreAll=new Button(tr("Restaurar todo","Restore all"));restoreAll.setDisable(!sm.esAdministrador());
        Runnable r=()->{try{String x=tipo.getValue(),q=search.getText();configureTrashColumns(t,x);List<Map<String,Object>>rows=new ArrayList<>();
            if(sm.esAdministrador()&&(x.equals("Usuarios")||x.equals("Todo"))){List<Usuario>us=new com.usuarios.dao.UsuarioDAO().buscarUsuarios(q,"Todos","Inactivo","Todas");for(Usuario u:us){Map<String,Object>m=new LinkedHashMap<>();m.put("id",u.getId());m.put("nombre",u.getNombre()+" "+u.getApellido());m.put("tipo",tr("Usuario","User"));m.put("detalle",u.getUsuario());m.put("fecha_eliminacion",u.getFechaEliminacion());m.put("estado",u.getEstado());m.put("categoriaInterna","Usuarios");rows.add(m);}}
            if(sm.puedeRestaurar("Turistas")&&(x.equals("Turistas")||x.equals("Todo"))){for(Map<String,Object>m:TuristaService.get().listar(false,q)){m.put("tipo",tr("Turista","Tourist"));m.put("categoriaInterna","Turistas");rows.add(m);}}
            if(sm.puedeRestaurar("Alojamientos")&&(x.equals("Alojamientos")||x.equals("Todo"))){for(Map<String,Object>m:AlojamientoService.get().listar(false,q)){m.put("tipo",tr("Alojamiento","Accommodation"));m.put("categoriaInterna","Alojamientos");rows.add(m);}}
            for(String cat:new String[]{"Actividades","Atractivos","Gastronomía","Servicios","Eventos"})if(sm.puedeRestaurar(cat)&&(x.equals(cat)||x.equals("Todo"))){for(Map<String,Object>m:CatalogService.get().listar(cat,false,q)){m.put("tipo",tr(cat,eng(cat)));m.put("categoriaInterna",cat);rows.add(m);}}
            pager.setData(rows);restoreAll.setDisable(!sm.esAdministrador()||rows.isEmpty());restore.setDisable(true);
        }catch(Exception e){error(e.getMessage());}};
        tipo.setOnAction(e->r.run());search.textProperty().addListener((o,a,b)->r.run());
        t.getSelectionModel().selectedItemProperty().addListener((o,a,b)->{if(b==null){restore.setDisable(true);return;}String cat=Objects.toString(b.get("categoriaInterna"),"");restore.setDisable(!sm.puedeRestaurar(cat));});
        restore.setOnAction(e->{Map<String,Object>m=t.getSelectionModel().getSelectedItem();if(m==null)return;try{restaurarRegistro(m);r.run();NotificationManager.getInstance().mostrarNotificacion(tr("Registro restaurado","Record restored"),"exito");}catch(Exception ex){error(ex.getMessage());}});
        restoreAll.setOnAction(e->{if(!sm.esAdministrador())return;try{restaurarTodos();r.run();NotificationManager.getInstance().mostrarNotificacion(tr("Todos los registros fueron restaurados","All records were restored"),"exito");}catch(Exception ex){error(ex.getMessage());}});
        content.getChildren().addAll(new HBox(8,tipo,search),t,pager.controls(),new HBox(8,restore,restoreAll));VBox.setVgrow(t,Priority.ALWAYS);r.run();
    }
    private void restaurarRegistro(Map<String,Object>m)throws Exception{String x=Objects.toString(m.get("categoriaInterna"),"");int id=((Number)m.get("id")).intValue();if(x.equals("Usuarios"))new com.usuarios.dao.UsuarioDAO().restaurar(id);else if(x.equals("Turistas"))TuristaService.get().cambiarEstado(id,true);else if(x.equals("Alojamientos"))AlojamientoService.get().estado(id,true);else CatalogService.get().estado(x,id,true);}
    private void restaurarTodos()throws Exception{if(!SessionManager.getInstance().esAdministrador())throw new IllegalStateException("Permiso denegado.");new com.usuarios.dao.UsuarioDAO().restaurarTodos();TuristaService.get().restaurarTodos();AlojamientoService.get().restaurarTodos();CatalogService.get().restaurarTodos();}
    private void configureTrashColumns(TableView<Map<String,Object>>t,String x){t.getColumns().clear();addMapColumn(t,"nombre",tr("Nombre","Name"));addMapColumn(t,"tipo",tr("Tipo","Type"));addMapColumn(t,"fecha_eliminacion",tr("Hora de eliminación","Deletion time"));addMapColumn(t,"estado",tr("Estado","Status"));addMapColumn(t,"detalle",tr("Detalle","Detail"));}

    private String eng(String value){return switch(value){case "Turistas"->"Tourists";case "Alojamientos"->"Accommodations";case "Actividades"->"Activities";case "Atractivos"->"Attractions";case "Gastronomía"->"Gastronomy";case "Servicios"->"Services";case "Eventos"->"Events";default->value;};}
    private String trValue(String value){if(value==null)return "";if(idioma.equals("es"))return value;String gast=tipoGastronomiaDisplay(value);if(!gast.equals(value))return gast;return switch(value){case "Cabaña"->"Cabin";case "Hotel"->"Hotel";case "Camping"->"Camping";case "Hostería"->"Inn";case "Hostel"->"Hostel";case "Hostal"->"Hostel";case "Lodge"->"Lodge";case "Parrilla"->"Steakhouse";case "Restaurante"->"Restaurant";case "Cafetería"->"Cafeteria";case "Bar"->"Bar";case "Emergencia"->"Emergency";case "Recreativa"->"Recreational";case "Cultural"->"Cultural";case "Deportiva"->"Sports";case "Natural"->"Natural";case "Histórico"->"Historic";case "Mirador"->"Viewpoint";case "Evento"->"Event";case "Calificación"->"Rating";case "DNI"->"National ID";case "Pasaporte"->"Passport";case "Cédula"->"Identity Card";case "Libreta Cívica"->"Civic Booklet";case "3 estrellas"->"3 stars";case "4 estrellas"->"4 stars";case "2 estrellas"->"2 stars";case "Activo"->"Active";case "Inactivo"->"Inactive";default->eng(value);};}
    private ComboBox<String>emailDomains(){return new ComboBox<>(FXCollections.observableArrayList("@gmail.com","@outlook.com","@hotmail.com","@yahoo.com","@icloud.com","@proton.me","@live.com","@municipalidad.gob.ar","@sannantonio.gob.ar"));}
    private void emailInput(TextField local,ComboBox<String>domain){
        local.setPromptText(tr("usuario","user"));
        local.setMaxWidth(Double.MAX_VALUE);
        domain.setMaxWidth(Double.MAX_VALUE);
        domain.getSelectionModel().selectFirst();
    }
    private void emailField(GridPane grid,TextField local,ComboBox<String>domain,int row){
        local.setMaxWidth(Double.MAX_VALUE);
        domain.setMaxWidth(Double.MAX_VALUE);
        HBox emailBox=new HBox(6,local,domain);
        emailBox.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(local,Priority.ALWAYS);
        grid.add(emailBox,1,row);
    }
    private String combineEmail(TextField local,ComboBox<String>domain){return local.getText().trim()+Objects.toString(domain.getValue(),"");}
    private void splitEmail(String email,TextField local,ComboBox<String>domain){String v=email==null?"":email.trim();int at=v.lastIndexOf('@');if(at>0){local.setText(v.substring(0,at));String suffix=v.substring(at);if(domain.getItems().contains(suffix))domain.setValue(suffix);else {domain.getItems().add(suffix);domain.setValue(suffix);}}else local.setText(v);}
    private void mostrarReportes(){
        base(tr("Reportes","Reports"),tr("Estadísticas de turistas y actividades más buscadas","Tourist statistics and most searched activities"));content.getStyleClass().add("report-page");
        HBox filters=new HBox(10);filters.setAlignment(Pos.CENTER_LEFT);
        ComboBox<String>period=new ComboBox<>(FXCollections.observableArrayList(tr("Año","Year"),tr("Mes","Month"),tr("Temporada","Season"),tr("Trimestre","Quarter")));period.setValue(tr("Año","Year"));
        int currentYear=LocalDate.now().getYear();ComboBox<Integer>year=new ComboBox<>(FXCollections.observableArrayList(currentYear-5,currentYear-4,currentYear-3,currentYear-2,currentYear-1,currentYear));year.setValue(currentYear);
        Button refresh=new Button(tr("Actualizar","Refresh")),bars=new Button(tr("Barras","Bars")),pie=new Button(tr("Torta","Pie")),pdf=new Button("PDF"),excel=new Button("Excel");refresh.getStyleClass().add("btn-secondary");bars.getStyleClass().add("btn-primary");pie.getStyleClass().add("btn-secondary");pdf.getStyleClass().add("btn-secondary");excel.getStyleClass().add("btn-secondary");filters.getChildren().addAll(new Label(tr("Período","Period")),period,new Label(tr("Año","Year")),year,refresh,bars,pie,pdf,excel);
        HBox stats=new HBox(10);stats.setFillHeight(true);content.getChildren().addAll(filters,stats);
        VBox chartBox=new VBox(8);chartBox.getStyleClass().add("card");Label chartTitle=new Label();chartTitle.getStyleClass().add("section-title");chartBox.getChildren().add(chartTitle);
        VBox activityBox=new VBox(8);activityBox.getStyleClass().add("card");Label activityTitle=new Label("🔎 "+tr("Actividades más buscadas","Most searched activities"));activityTitle.getStyleClass().add("section-title");TableView<Map.Entry<String,Integer>>activityTable=new TableView<>();activityTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);activityTable.setFixedCellSize(Math.max(34,42*fontScale));TableColumn<Map.Entry<String,Integer>,String>termCol=new TableColumn<>(tr("Actividad / búsqueda","Activity / search"));termCol.setCellValueFactory(c->new javafx.beans.property.SimpleStringProperty(trValue(c.getValue().getKey())));TableColumn<Map.Entry<String,Integer>,Number>countCol=new TableColumn<>(tr("Búsquedas","Searches"));countCol.setCellValueFactory(c->new javafx.beans.property.SimpleIntegerProperty(c.getValue().getValue()));activityTable.getColumns().addAll(termCol,countCol);activityTable.setPrefHeight(280);activityBox.getChildren().addAll(activityTitle,activityTable);
        Runnable render=()->{try{
            String p=period.getValue();String internal=p.equals(tr("Mes","Month"))?"Mes":p.equals(tr("Temporada","Season"))?"Temporada":p.equals(tr("Trimestre","Quarter"))?"Trimestre":"Año";
            Map<String,Integer>data=internal.equals("Año")?TuristaService.get().reporteAnualComparativo(year.getValue()-5,year.getValue()):TuristaService.get().reportePorPeriodo(internal,year.getValue());
            Map<String,Integer>searches;try{searches=TuristaService.get().actividadesMasBuscadas(internal,year.getValue());}catch(Exception ignored){searches=new LinkedHashMap<>();}
            int total=data.values().stream().mapToInt(Integer::intValue).sum();int max=data.values().stream().mapToInt(Integer::intValue).max().orElse(0);double avg=data.isEmpty()?0:data.values().stream().mapToInt(Integer::intValue).average().orElse(0);int searchTotal=searches.values().stream().mapToInt(Integer::intValue).sum();
            stats.getChildren().clear();addReportStat(stats,tr("Total","Total"),String.valueOf(total),tr("turistas en el gráfico","tourists in chart"));addReportStat(stats,tr("Máximo","Maximum"),String.valueOf(max),tr("mayor cantidad en un período","highest period count"));addReportStat(stats,tr("Promedio","Average"),String.format(Locale.US,"%.1f",avg),tr("turistas por período","tourists per period"));addReportStat(stats,tr("Búsquedas","Searches"),String.valueOf(searchTotal),tr("actividades registradas","activities recorded"));
            activityTable.setItems(FXCollections.observableArrayList(searches.entrySet()));chartBox.getChildren().removeIf(n->n!=chartTitle);chartTitle.setText("📊 "+tr("Turistas por ","Tourists by ")+p+(internal.equals("Año")?"": " · "+year.getValue()));
            if("pie".equals(chartBox.getProperties().get("chartMode"))){PieChart chart=new PieChart();chart.setPrefHeight(400);chart.setLegendVisible(true);for(var e:data.entrySet())if(e.getValue()>0)chart.getData().add(new PieChart.Data(e.getKey(),e.getValue()));chartBox.getChildren().add(chart);Platform.runLater(()->applyPieTheme(chart));}
            else{CategoryAxis x=new CategoryAxis();NumberAxis y=new NumberAxis();x.setLabel(tr("Período","Period"));y.setLabel(tr("Cantidad de turistas","Number of tourists"));y.setForceZeroInRange(true);BarChart<String,Number>chart=new BarChart<>(x,y);chart.setLegendVisible(false);chart.setPrefHeight(400);XYChart.Series<String,Number>serie=new XYChart.Series<>();for(var e:data.entrySet())serie.getData().add(new XYChart.Data<>(e.getKey(),e.getValue()));chart.getData().add(serie);chartBox.getChildren().add(chart);applyChartTheme(chart);Platform.runLater(()->{chart.applyCss();chart.layout();String color=chartColor();for(XYChart.Data<String,Number>d:serie.getData())if(d.getNode()!=null)d.getNode().setStyle("-fx-bar-fill:"+color+";"+"-fx-background-color:"+color+";");});}
            applyFontScale(stats);applyFontScale(chartBox);applyFontScale(activityTable);
        }catch(Exception e){error(e.getMessage());}};
        chartBox.getProperties().put("chartMode","bar");setChartButtons(bars,pie,true);bars.setOnAction(e->{chartBox.getProperties().put("chartMode","bar");setChartButtons(bars,pie,true);render.run();});pie.setOnAction(e->{chartBox.getProperties().put("chartMode","pie");setChartButtons(bars,pie,false);render.run();});refresh.setOnAction(e->render.run());period.setOnAction(e->render.run());year.setOnAction(e->render.run());pdf.setOnAction(e->exportReportPdf(period,year,chartBox));excel.setOnAction(e->exportReportExcel(period,year));
        content.getChildren().addAll(chartBox,activityBox);render.run();
    }
    private void addReportStat(HBox row,String title,String value,String detail){VBox box=new VBox(3);box.getStyleClass().add("card-metric");Label a=new Label(title),b=new Label(value),c=new Label(detail);a.getStyleClass().add("metric-label");b.getStyleClass().add("metric-value");c.getStyleClass().add("metric-detail");box.getChildren().addAll(a,b,c);HBox.setHgrow(box,Priority.ALWAYS);row.getChildren().add(box);}
    private void applyPieTheme(PieChart chart){
        String[] palette=switch(access){
            case "Protanopía"->new String[]{"#D6B74A","#E5A64A","#B89A36","#F0C96A","#A98528","#E9D58D","#C7B45A","#D9C06A"};
            case "Deuteranopía"->new String[]{"#D9873D","#C76B3A","#E2A45D","#B95732","#E8B77A","#9D4D2C","#DFA06A","#B86F45"};
            case "Tritanopía"->new String[]{"#D77FA8","#C85F92","#E59ABC","#B54F82","#EDB2C9","#A83D73","#D98BB0","#B96A9B"};
            case "Acromatopsia"->dark?new String[]{"#FFFFFF","#D9D9D9","#BDBDBD","#999999","#777777","#555555","#333333","#EEEEEE"}:new String[]{"#111111","#333333","#555555","#777777","#999999","#BBBBBB","#DDDDDD","#222222"};
            case "contraste"->dark?new String[]{"#FFFFFF","#BDBDBD","#999999","#777777","#555555","#D9D9D9","#EEEEEE","#AAAAAA"}:new String[]{"#000000","#333333","#555555","#777777","#999999","#BBBBBB","#DDDDDD","#222222"};
            default->new String[]{"#238DB5","#4FA8C6","#78BDD4","#2E9A7B","#D49A3A","#8A6BBE","#C85F7A","#5D8FA8"};
        };
        javafx.application.Platform.runLater(()->{
            for(int i=0;i<chart.getData().size();i++){
                Node n=chart.getData().get(i).getNode();
                if(n!=null)n.setStyle("-fx-pie-color:"+palette[i%palette.length]+";");
            }
        });
    }

    private void exportReportPdf(ComboBox<String>period,ComboBox<Integer>year,Pane chartBox){
        try{
            String p=period.getValue();
            String internal=p.equals(tr("Mes","Month"))?"Mes":p.equals(tr("Temporada","Season"))?"Temporada":p.equals(tr("Trimestre","Quarter"))?"Trimestre":"Año";
            Map<String,Integer> data=internal.equals("Año")?TuristaService.get().reporteAnualComparativo(year.getValue()-5,year.getValue()):TuristaService.get().reportePorPeriodo(internal,year.getValue());
            Map<String,Integer> searches;try{searches=TuristaService.get().actividadesMasBuscadas(internal,year.getValue());}catch(Exception ignored){searches=new LinkedHashMap<>();}
            FileChooser fc=new FileChooser();fc.setTitle(tr("Guardar reporte PDF","Save PDF report"));fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF","*.pdf"));fc.setInitialFileName("reporte_turistas_"+periodKey(internal)+"_"+year.getValue()+".pdf");
            File file=fc.showSaveDialog(stage);if(file==null)return;
            writeSimplePdf(file,tr("Reporte de turistas","Tourist report"),p+" · "+year.getValue(),data,searches);
            NotificationManager.getInstance().mostrarNotificacion(tr("PDF exportado correctamente","PDF exported successfully"),"exito");
        }catch(Exception e){error(e.getMessage());}
    }

    private void exportReportExcel(ComboBox<String>period,ComboBox<Integer>year){
        try{
            String p=period.getValue();
            String internal=p.equals(tr("Mes","Month"))?"Mes":p.equals(tr("Temporada","Season"))?"Temporada":p.equals(tr("Trimestre","Quarter"))?"Trimestre":"Año";
            Map<String,Integer> data=internal.equals("Año")?TuristaService.get().reporteAnualComparativo(year.getValue()-5,year.getValue()):TuristaService.get().reportePorPeriodo(internal,year.getValue());
            Map<String,Integer> searches;try{searches=TuristaService.get().actividadesMasBuscadas(internal,year.getValue());}catch(Exception ignored){searches=new LinkedHashMap<>();}
            FileChooser fc=new FileChooser();fc.setTitle(tr("Guardar reporte Excel","Save Excel report"));fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel","*.xlsx"));fc.setInitialFileName("reporte_turistas_"+periodKey(internal)+"_"+year.getValue()+".xlsx");
            File file=fc.showSaveDialog(stage);if(file==null)return;
            writeSimpleXlsx(file,p+" · "+year.getValue(),data,searches);
            NotificationManager.getInstance().mostrarNotificacion(tr("Excel exportado correctamente","Excel exported successfully"),"exito");
        }catch(Exception e){error(e.getMessage());}
    }

    private String periodKey(String p){return switch(p){case "Año"->"anual";case "Mes"->"mensual";case "Temporada"->"temporada";case "Trimestre"->"trimestre";default->"reporte";};}
    private void setChartButtons(Button bars,Button pie,boolean barSelected){bars.getStyleClass().removeAll("btn-primary","btn-secondary");pie.getStyleClass().removeAll("btn-primary","btn-secondary");bars.getStyleClass().add(barSelected?"btn-primary":"btn-secondary");pie.getStyleClass().add(barSelected?"btn-secondary":"btn-primary");}
    private String pdfText(String x){return x==null?"":x.replace("\\","\\\\").replace("(","\\(").replace(")","\\)").replace("\n"," ");}
    private String pdfColor(double r,double g,double b){return String.format(Locale.US,"%.3f %.3f %.3f rg ",r,g,b);}
    private String pdfFill(double x,double y,double w,double h,double r,double g,double b){return String.format(Locale.US,"q %.3f %.3f %.3f rg %.1f %.1f %.1f %.1f re f Q ",r,g,b,x,y,w,h);}
    private String pdfTextAt(String text,float size,float x,float y,boolean bold,double r,double g,double b){return "BT /F"+(bold?"2":"1")+" "+String.format(Locale.US,"%.1f",size)+" Tf "+pdfColor(r,g,b)+String.format(Locale.US,"%.1f %.1f Td (",x,y)+pdfText(text)+") Tj ET ";}
    private void writeSimplePdf(File file,String title,String subtitle,Map<String,Integer> data,Map<String,Integer> searches)throws Exception{
        int total=data.values().stream().mapToInt(Integer::intValue).sum();
        int max=data.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        double avg=data.isEmpty()?0:data.values().stream().mapToInt(Integer::intValue).average().orElse(0);
        int searchTotal=searches.values().stream().mapToInt(Integer::intValue).sum();
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        List<Integer> offsets=new ArrayList<>();
        List<byte[]> objects=new ArrayList<>();
        objects.add("<< /Type /Catalog /Pages 2 0 R >>".getBytes(StandardCharsets.ISO_8859_1));
        objects.add("<< /Type /Pages /Kids [3 0 R] /Count 1 >>".getBytes(StandardCharsets.ISO_8859_1));
        int pageObj=3,contentObj=4,fontObj=5,boldObj=6;
        StringBuilder c=new StringBuilder();
        c.append(pdfFill(0,772,595,70,0.08,0.35,0.52));
        c.append(pdfTextAt(title,20,38,798,true,1,1,1));
        c.append(pdfTextAt(subtitle,9,38,780,false,0.88,0.95,0.98));
        String[][] cards={{tr("TOTAL","TOTAL"),String.valueOf(total)},{tr("MÁXIMO","MAXIMUM"),String.valueOf(max)},{tr("PROMEDIO","AVERAGE"),String.format(Locale.US,"%.1f",avg)},{tr("BÚSQUEDAS","SEARCHES"),String.valueOf(searchTotal)}};
        double[] xs={38,168,298,428};
        for(int i=0;i<4;i++){
            c.append(pdfFill(xs[i],686,120,64,0.94,0.97,0.985));
            c.append(pdfTextAt(cards[i][0],8,(float)xs[i]+10,731,true,0.08,0.35,0.52));
            c.append(pdfTextAt(cards[i][1],20,(float)xs[i]+10,702,true,0.08,0.35,0.52));
        }
        c.append(pdfTextAt(tr("Turistas por período","Tourists by period"),13,38,658,true,0.08,0.35,0.52));
        c.append(pdfFill(38,620,519,24,0.08,0.35,0.52));
        c.append(pdfTextAt(tr("Período","Period"),9,50,629,true,1,1,1));
        c.append(pdfTextAt(tr("Cantidad","Count"),9,455,629,true,1,1,1));
        int row=0;
        double maxBar=Math.max(1,max);
        for(var e:data.entrySet()){
            double y=596-row*27;
            if(y<335)break;
            if(row%2==0)c.append(pdfFill(38,y-7,519,27,0.96,0.98,0.99));
            c.append(pdfTextAt(e.getKey(),8,50,(float)y+3,false,0.18,0.23,0.27));
            c.append(pdfFill(300,y-2,140,8,0.86,0.93,0.97));
            c.append(pdfFill(300,y-2,140*(e.getValue()/maxBar),8,0.08,0.35,0.52));
            c.append(pdfTextAt(String.valueOf(e.getValue()),9,462,(float)y+1,true,0.08,0.35,0.52));
            row++;
        }
        double searchY=300;
        c.append(pdfTextAt(tr("Actividades más buscadas","Most searched activities"),13,38,(float)searchY,true,0.08,0.35,0.52));
        c.append(pdfFill(38,searchY-38,519,24,0.08,0.35,0.52));
        c.append(pdfTextAt(tr("Actividad / búsqueda","Activity / search"),9,50,(float)searchY-29,true,1,1,1));
        c.append(pdfTextAt(tr("Búsquedas","Searches"),9,455,(float)searchY-29,true,1,1,1));
        int sr=0;
        for(var e:searches.entrySet()){
            double y=searchY-65-sr*21;
            if(y<75)break;
            if(sr%2==0)c.append(pdfFill(38,y-6,519,21,0.96,0.98,0.99));
            c.append(pdfTextAt(e.getKey(),8,50,(float)y,false,0.18,0.23,0.27));
            c.append(pdfTextAt(String.valueOf(e.getValue()),9,462,(float)y,true,0.08,0.35,0.52));
            sr++;
        }
        c.append(pdfTextAt("Municipalidad de San Antonio de Arredondo · "+LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),7,38,35,false,0.45,0.50,0.53));
        String stream=c.toString();
        String page="<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 "+fontObj+" 0 R /F2 "+boldObj+" 0 R >> >> /Contents "+contentObj+" 0 R >>";
        objects.add(page.getBytes(StandardCharsets.ISO_8859_1));
        objects.add(("<< /Length "+stream.getBytes(StandardCharsets.ISO_8859_1).length+" >>\nstream\n"+stream+"\nendstream").getBytes(StandardCharsets.ISO_8859_1));
        objects.add("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>".getBytes(StandardCharsets.ISO_8859_1));
        objects.add("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>".getBytes(StandardCharsets.ISO_8859_1));
        out.write("%PDF-1.4\n".getBytes(StandardCharsets.ISO_8859_1));
        offsets.add(0);
        for(int i=0;i<objects.size();i++){offsets.add(out.size());out.write((i+1+" 0 obj\n").getBytes(StandardCharsets.ISO_8859_1));out.write(objects.get(i));out.write("\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));}
        int xref=out.size();
        out.write(("xref\n0 "+(objects.size()+1)+"\n0000000000 65535 f \n").getBytes(StandardCharsets.ISO_8859_1));
        for(int i=1;i<offsets.size();i++)out.write(String.format("%010d 00000 n \n",offsets.get(i)).getBytes(StandardCharsets.ISO_8859_1));
        out.write(("trailer\n<< /Size "+(objects.size()+1)+" /Root 1 0 R >>\nstartxref\n"+xref+"\n%%EOF").getBytes(StandardCharsets.ISO_8859_1));
        Files.write(file.toPath(),out.toByteArray());
    }
    private String xml(String s){return s==null?"":s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;").replace("'","&apos;");}
    private String xlsxCell(int row,String col,String value,int style){return "<c r=\""+col+row+"\" t=\"inlineStr\" s=\""+style+"\"><is><t>"+xml(value)+"</t></is></c>";}
    private String xlsxNumber(int row,String col,int value,int style){return "<c r=\""+col+row+"\" s=\""+style+"\"><v>"+value+"</v></c>";}
    private String xlsxRow(int row,String a,String b,int styleA,int styleB){return "<row r=\""+row+"\">"+xlsxCell(row,"A",a,styleA)+xlsxCell(row,"B",b,styleB)+"</row>";}
    private void writeSimpleXlsx(File file,String title,Map<String,Integer> data,Map<String,Integer> searches)throws Exception{
        int total=data.values().stream().mapToInt(Integer::intValue).sum();int max=data.values().stream().mapToInt(Integer::intValue).max().orElse(0);double avg=data.isEmpty()?0:data.values().stream().mapToInt(Integer::intValue).average().orElse(0);int searchTotal=searches.values().stream().mapToInt(Integer::intValue).sum();
        StringBuilder s1=new StringBuilder();int r=1;s1.append("<row r=\"1\" ht=\"30\" customHeight=\"1\">").append(xlsxCell(r,"A",tr("REPORTE DE TURISTAS","TOURIST REPORT"),1)).append(xlsxCell(r,"B","",1)).append("</row>");r++;s1.append(xlsxRow(r++,"Período",title,2,3));r++;s1.append(xlsxRow(r++,tr("Indicador","Metric"),tr("Valor","Value"),4,4));s1.append(xlsxRow(r++,tr("Total de turistas","Total tourists"),String.valueOf(total),5,6));s1.append(xlsxRow(r++,tr("Máximo por período","Maximum per period"),String.valueOf(max),5,6));s1.append(xlsxRow(r++,tr("Promedio por período","Average per period"),String.format(Locale.US,"%.1f",avg),5,7));s1.append(xlsxRow(r++,tr("Búsquedas de actividades","Activity searches"),String.valueOf(searchTotal),5,6));r+=2;s1.append(xlsxRow(r++,tr("Turistas por período","Tourists by period"),"",2,2));s1.append(xlsxRow(r++,tr("Período","Period"),tr("Cantidad","Count"),4,4));int firstData=r;for(var e:data.entrySet()){s1.append(xlsxRow(r++,e.getKey(),String.valueOf(e.getValue()),8,(r%2==0)?9:8));}int lastData=r-1;
        StringBuilder s2=new StringBuilder();r=1;s2.append(xlsxRow(r++,tr("ACTIVIDADES MÁS BUSCADAS","MOST SEARCHED ACTIVITIES"),"",1,1));s2.append(xlsxRow(r++,tr("Actividad / búsqueda","Activity / search"),tr("Búsquedas","Searches"),4,4));for(var e:searches.entrySet())s2.append(xlsxRow(r++,e.getKey(),String.valueOf(e.getValue()),8,6));
        String sheet1="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><sheetViews><sheetView workbookViewId=\"0\" showGridLines=\"0\"><pane ySplit=\"3\" topLeftCell=\"A4\" activePane=\"bottomLeft\" state=\"frozen\"/></sheetView></sheetViews><sheetFormatPr defaultRowHeight=\"20\"/><cols><col min=\"1\" max=\"1\" width=\"34\" customWidth=\"1\"/><col min=\"2\" max=\"2\" width=\"24\" customWidth=\"1\"/></cols><sheetData>"+s1+"</sheetData><mergeCells count=\"2\"><mergeCell ref=\"A1:B1\"/><mergeCell ref=\"A8:B8\"/></mergeCells><autoFilter ref=\"A"+firstData+":B"+lastData+"\"/><pageMargins left=\"0.3\" right=\"0.3\" top=\"0.5\" bottom=\"0.5\" header=\"0.2\" footer=\"0.2\"/><pageSetup orientation=\"portrait\" fitToWidth=\"1\" fitToHeight=\"0\"/></worksheet>";
        String sheet2="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><sheetViews><sheetView workbookViewId=\"0\" showGridLines=\"0\"/></sheetViews><sheetFormatPr defaultRowHeight=\"20\"/><cols><col min=\"1\" max=\"1\" width=\"42\" customWidth=\"1\"/><col min=\"2\" max=\"2\" width=\"18\" customWidth=\"1\"/></cols><sheetData>"+s2+"</sheetData><mergeCells count=\"1\"><mergeCell ref=\"A1:B1\"/></mergeCells><pageMargins left=\"0.3\" right=\"0.3\" top=\"0.5\" bottom=\"0.5\" header=\"0.2\" footer=\"0.2\"/><pageSetup orientation=\"portrait\" fitToWidth=\"1\" fitToHeight=\"0\"/></worksheet>";
        String styles="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><fonts count=\"3\"><font><sz val=\"11\"/><name val=\"Aptos\"/></font><font><b/><sz val=\"16\"/><color rgb=\"FFFFFFFF\"/><name val=\"Aptos Display\"/></font><font><b/><sz val=\"11\"/><name val=\"Aptos\"/></font></fonts><fills count=\"5\"><fill><patternFill patternType=\"none\"/></fill><fill><patternFill patternType=\"gray125\"/></fill><fill><patternFill patternType=\"solid\"><fgColor rgb=\"FF176B88\"/></patternFill></fill><fill><patternFill patternType=\"solid\"><fgColor rgb=\"FFDFF3FB\"/></patternFill></fill><fill><patternFill patternType=\"solid\"><fgColor rgb=\"FFF3F7FA\"/></patternFill></fill></fills><borders count=\"2\"><border><left/><right/><top/><bottom/></border><border><left style=\"thin\"/><right style=\"thin\"/><top style=\"thin\"/><bottom style=\"thin\"/></border></borders><cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs><cellXfs count=\"10\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/><xf numFmtId=\"0\" fontId=\"1\" fillId=\"2\" borderId=\"0\"/><xf numFmtId=\"0\" fontId=\"2\" fillId=\"3\" borderId=\"0\"/><xf numFmtId=\"0\" fontId=\"0\" fillId=\"4\" borderId=\"1\"/><xf numFmtId=\"0\" fontId=\"2\" fillId=\"2\" borderId=\"1\"/><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\"/><xf numFmtId=\"0\" fontId=\"2\" fillId=\"0\" borderId=\"1\"/><xf numFmtId=\"0\" fontId=\"0\" fillId=\"4\" borderId=\"1\"/><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\"/><xf numFmtId=\"0\" fontId=\"0\" fillId=\"3\" borderId=\"1\"/></cellXfs></styleSheet>";
        String types="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\"><Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/><Default Extension=\"xml\" ContentType=\"application/xml\"/><Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/><Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/><Override PartName=\"/xl/worksheets/sheet2.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/><Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/></Types>";
        String rels="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/></Relationships>";
        String wbrels="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/><Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet2.xml\"/><Relationship Id=\"rId3\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/></Relationships>";
        String wb="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"><sheets><sheet name=\"Resumen\" sheetId=\"1\" r:id=\"rId1\"/><sheet name=\"Actividades\" sheetId=\"2\" r:id=\"rId2\"/></sheets></workbook>";
        try(ZipOutputStream z=new ZipOutputStream(new FileOutputStream(file))){zip(z,"[Content_Types].xml",types);zip(z,"_rels/.rels",rels);zip(z,"xl/workbook.xml",wb);zip(z,"xl/_rels/workbook.xml.rels",wbrels);zip(z,"xl/styles.xml",styles);zip(z,"xl/worksheets/sheet1.xml",sheet1);zip(z,"xl/worksheets/sheet2.xml",sheet2);}
    }
    private void zip(ZipOutputStream z,String name,String text)throws IOException{z.putNextEntry(new ZipEntry(name));z.write(text.getBytes(StandardCharsets.UTF_8));z.closeEntry();}

    private void mostrarConfiguracion(){base(tr("Configuración","Settings"),tr("Preferencias de accesibilidad y presentación.","Accessibility and display preferences."));CheckBox darkBox=new CheckBox(tr("Modo oscuro","Dark mode"));darkBox.getStyleClass().add("settings-check");Slider size=new Slider(.85,1.35,fontScale);size.setShowTickLabels(true);size.setShowTickMarks(true);size.valueProperty().addListener((o,a,b)->{fontScale=b.doubleValue();root.setStyle("-fx-font-size:"+(13*fontScale)+"px;");applyFontScale(root);});ComboBox<String>lang=new ComboBox<>(FXCollections.observableArrayList("Español","English"));lang.getSelectionModel().select(idioma.equals("es")?0:1);ComboBox<String>acc=new ComboBox<>(FXCollections.observableArrayList("Normal","Mayor contraste","Protanopía","Deuteranopía","Tritanopía","Acromatopsia"));acc.setValue(access.equals("normal")?"Normal":access.equals("contraste")?"Mayor contraste":access);Label sizeL=new Label(tr("Tamaño de letra","Font size"));Button save=new Button(tr("Guardar preferencias","Save preferences"));darkBox.setSelected(dark);darkBox.setOnAction(e->{dark=darkBox.isSelected();applyTheme();});lang.setOnAction(e->{idioma=lang.getValue().equals("Español")?"es":"en";root.setTop(top());root.setLeft(sidebarScroll());mostrarConfiguracion();});acc.setOnAction(e->{access=acc.getValue().equals("Normal")?"normal":acc.getValue().equals("Mayor contraste")?"contraste":acc.getValue();applyTheme();});content.getChildren().addAll(darkBox,new HBox(10,sizeL,size),new HBox(10,new Label(tr("Idioma","Language")),lang),new HBox(10,new Label(tr("Accesibilidad","Accessibility")),acc),save);save.setOnAction(e->{try{var db=DatabaseConnection.getInstance();db.guardarConfiguracion("tema",dark?"oscuro":"claro");db.guardarConfiguracion("tamano_fuente",String.valueOf(fontScale));db.guardarConfiguracion("idioma",idioma);db.guardarConfiguracion("accesibilidad",access);NotificationManager.getInstance().mostrarNotificacion(tr("Preferencias guardadas","Preferences saved"),"exito");applyTheme();}catch(Exception ex){error(ex.getMessage());}});applyTheme();}

    private void applyFontScale(Node n){
        double px=13*fontScale;
        if(n instanceof TableView<?> tv){
            limpiarColumnasSobrantes(tv);
            tv.setStyle(tv.getStyle()+";-fx-font-size:"+px+"px;");
            tv.setFixedCellSize(Math.max(34,42*fontScale));
            tv.setMinHeight(120*fontScale);
            for(TableColumn<?,?> c:tv.getColumns()){
                c.setMinWidth(Math.max(c.getMinWidth(),90*fontScale));
                c.setStyle(c.getStyle()+";-fx-font-size:"+px+"px;");
            }
            ajustarResizeTabla(tv);
            for(Node cell:tv.lookupAll(".table-cell"))cell.setStyle(cell.getStyle()+";-fx-font-size:"+px+"px;");
            for(Node header:tv.lookupAll(".column-header .label"))header.setStyle(header.getStyle()+";-fx-font-size:"+px+"px;");
            for(Node sb:tv.lookupAll(".scroll-bar:vertical"))sb.setStyle(sb.getStyle()+";-fx-pref-width:"+Math.max(10,10*fontScale)+"px;");
            for(Node sb:tv.lookupAll(".scroll-bar:horizontal"))sb.setStyle(sb.getStyle()+";-fx-pref-height:"+Math.max(10,10*fontScale)+"px;");
        }else if(n instanceof Labeled l){
            if(l.getStyleClass().contains("clock"))px=54*fontScale;
            else if(l.getStyleClass().contains("welcome-title"))px=30*fontScale;
            else if(l.getStyleClass().contains("welcome-subtitle"))px=16*fontScale;
            l.setStyle(l.getStyle()+";-fx-font-size:"+px+"px;");
        }else if(n instanceof TextInputControl ti){
            ti.setStyle(ti.getStyle()+";-fx-font-size:"+px+"px;");
        }else if(n instanceof ComboBox<?> cb){
            cb.setStyle(cb.getStyle()+";-fx-font-size:"+px+"px;");
        }
        if(n instanceof Parent p)for(Node c:p.getChildrenUnmodifiable())applyFontScale(c);
    }

    private void ajustarResizeTabla(TableView<?> tv){Runnable r=()->{double total=0;for(TableColumn<?,?> c:tv.getColumns())total+=Math.max(c.getMinWidth(),90*fontScale);tv.setColumnResizePolicy(total>Math.max(0,tv.getWidth()-18)?TableView.UNCONSTRAINED_RESIZE_POLICY:TableView.CONSTRAINED_RESIZE_POLICY);};if(!Boolean.TRUE.equals(tv.getProperties().get("resizeListener"))){tv.widthProperty().addListener((o,a,b)->r.run());tv.getProperties().put("resizeListener",Boolean.TRUE);}r.run();}
    private void limpiarColumnasSobrantes(TableView<?> tv){
        tv.getColumns().removeIf(c->{String t=c.getText();return t==null||t.isBlank()||"MASIVO".equalsIgnoreCase(t.trim());});
    }

    private void applyTheme(){root.getStyleClass().removeAll("dark","contrast","protanopia","deuteranopia","tritanopia","achromatopsia");if(dark)root.getStyleClass().add("dark");if(access.equals("contraste"))root.getStyleClass().add("contrast");if(access.equals("Protanopía"))root.getStyleClass().add("protanopia");if(access.equals("Deuteranopía"))root.getStyleClass().add("deuteranopia");if(access.equals("Tritanopía"))root.getStyleClass().add("tritanopia");if(access.equals("Acromatopsia"))root.getStyleClass().add("achromatopsia"); applyFontScale(root);}
    private String chartColor(){return switch(access){case "Protanopía"->"#d6b74a";case "Deuteranopía"->"#d9873d";case "Tritanopía"->"#d77fa8";case "Acromatopsia"->dark?"#ffffff":"#222222";case "contraste"->dark?"#ffffff":"#000000";default->dark?"#66b8d6":"#238db5";};}
    private void applyChartTheme(BarChart<String,Number>c){String cls=switch(access){case "contraste"->"contraste";case "Protanopía"->"protanopia";case "Deuteranopía"->"deuteranopia";case "Tritanopía"->"tritanopia";case "Acromatopsia"->"acromatopsia";default->"normal";};c.getStyleClass().add("chart-access-"+cls);}
    private GridPane formGrid(){GridPane g=new GridPane();g.setHgap(10);g.setVgap(8);g.setPadding(new Insets(4));ColumnConstraints label=new ColumnConstraints();label.setMinWidth(155);label.setPrefWidth(155);ColumnConstraints field=new ColumnConstraints();field.setHgrow(Priority.ALWAYS);field.setMinWidth(320);g.getColumnConstraints().addAll(label,field);return g;}
    private Map<String,String>collect(Map<String,Control>f){Map<String,String>m=new LinkedHashMap<>();for(String k:f.keySet()){Control c=f.get(k); if(c instanceof TextInputControl) m.put(k,((TextInputControl)c).getText().trim()); else if(c instanceof DatePicker dp) m.put(k,dp.getValue()==null?"":dp.getValue().toString()); else m.put(k,Objects.toString(((ComboBox<?>)c).getValue(),"").trim());}return m;}
    private Map<String,String>collectTF(Map<String,TextField>f){Map<String,String>m=new LinkedHashMap<>();f.forEach((k,v)->m.put(k,v.getText().trim()));return m;}
    private void tset(TextField t,String v){t.setText(v==null?"":v);}
    private String label(String k){return switch(k){case "nombre"->tr("Nombre","Name");case "apellido"->tr("Apellido","Surname");case "tipo"->tr("Tipo","Type");case "detalle"->tr("Detalle","Detail");case "direccion"->tr("Dirección","Address");case "categoria"->tr("Categoría","Category");case "dueno"->tr("Prestador","Provider");case "dni"->"DNI";case "capacidad"->tr("Capacidad","Capacity");default->k;};}
    private int maxFor(String k,boolean desc){return desc?300:switch(k){case "nombre","apellido","tipo","categoria"->50;case "pais","provincia"->0;case "direccion","detalle","extra","telefono","dni","capacidad","amenities","foto"->100;case "descripcion","observaciones"->300;default->100;};}
    private String max(String k,boolean desc){return "";}
    private void requiredMark(Label label,Control c,int min){Runnable r=()->{String v=c instanceof TextInputControl ti?ti.getText():c instanceof DatePicker dp?(dp.getValue()==null?"":dp.getValue().toString()):Objects.toString(((ComboBox<?>)c).getValue(),"");boolean ok=!v.isBlank()&&v.trim().length()>=min;String base=label.getText().replace(" *","");label.setText(ok?base:base+" *");if(ok)label.getStyleClass().remove("required");else if(!label.getStyleClass().contains("required"))label.getStyleClass().add("required");};if(c instanceof TextInputControl ti)ti.textProperty().addListener((o,a,b)->r.run());else if(c instanceof DatePicker dp)dp.valueProperty().addListener((o,a,b)->r.run());else if(c instanceof ComboBox<?> cb)cb.valueProperty().addListener((o,a,b)->r.run());r.run();}
    private void counter(Control c,Label l,int max){if(max<=0)return; if(c instanceof TextInputControl ti){Label count=new Label();count.getStyleClass().add("char-counter");ti.textProperty().addListener((o,a,b)->{if(b.length()>max){ti.setText(b.substring(0,max));return;}int left=max-ti.getText().length();count.setText(left<=10&&left>0?tr("Quedan "+left+" caracteres",""+left+" characters left"):"");});((GridPane)l.getParent()).add(count,2,GridPane.getRowIndex(l));}}
    private void detalle(Map<String,Object>m){
        Dialog<Void>d=new Dialog<>(); d.setTitle(tr("Información del registro","Record information"));
        GridPane g=formGrid(); g.getStyleClass().add("user-abm-grid"); int r=0;
        for(var e:m.entrySet()){
            if(e.getKey().equals("id")||e.getKey().equals("fecha_eliminacion"))continue;
            Label l=new Label(label(e.getKey())); TextField v=new TextField(Objects.toString(e.getValue(),"")); v.setEditable(false); v.setPrefHeight(40); v.setMaxWidth(Double.MAX_VALUE);
            g.add(l,0,r); g.add(v,1,r++);
        }
        ScrollPane sp=new ScrollPane(g); sp.setFitToWidth(true); sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); sp.setPannable(true);
        d.getDialogPane().setContent(sp); d.getDialogPane().getButtonTypes().add(ButtonType.OK); d.getDialogPane().setMinWidth(560); d.getDialogPane().setPrefWidth(620); d.getDialogPane().setPrefHeight(470); styleDialog(d); centerDialog(d); d.showAndWait();
    }
    private void styleDialog(Dialog<?>d){d.getDialogPane().getStyleClass().add("app-dialog");d.getDialogPane().setStyle("-fx-font-size:"+(13*fontScale)+"px;");d.setOnShown(e->{var sc=d.getDialogPane().getScene();if(sc!=null){sc.getStylesheets().add(DashboardController.class.getResource("/dashboard.css").toExternalForm());var r=sc.getRoot();r.getStyleClass().addAll(root.getStyleClass());d.getDialogPane().getStyleClass().addAll(root.getStyleClass());}centerDialog(d);});}
    private void centerDialog(Dialog<?> d){Window owner=stage; if(d.getDialogPane().getScene()!=null){Window w=d.getDialogPane().getScene().getWindow(); if(w!=null){w.centerOnScreen(); if(owner!=null)w.setX(owner.getX()+(owner.getWidth()-w.getWidth())/2); if(owner!=null)w.setY(owner.getY()+(owner.getHeight()-w.getHeight())/2);}}}
    private void styleAlert(Alert a){a.getDialogPane().getStyleClass().add("app-dialog");a.getDialogPane().setStyle("-fx-font-size:"+(13*fontScale)+"px;");a.setOnShown(e->{var sc=a.getDialogPane().getScene();if(sc!=null){sc.getStylesheets().add(DashboardController.class.getResource("/dashboard.css").toExternalForm());sc.getRoot().getStyleClass().addAll(root.getStyleClass());}});}
    private void error(String s){Alert a=new Alert(Alert.AlertType.ERROR,tr("No se pudo completar la operación.","The operation could not be completed.")+"\n\n"+(s==null?"Error":s),ButtonType.OK);styleAlert(a);a.showAndWait();}
    private static final class Pager<T>{
        private final TableView<T> table; private final int pageSize; private final Button prev=new Button("‹"); private final Button next=new Button("›"); private final Label info=new Label(); private List<T> data=List.of(); private int page=0;
        Pager(TableView<T> table,int pageSize){this.table=table;this.pageSize=pageSize;prev.setOnAction(e->{if(page>0){page--;refresh();}});next.setOnAction(e->{if((page+1)*pageSize<data.size()){page++;refresh();}});}
        void setData(List<T> d){data=d==null?List.of():new ArrayList<>(d);page=0;refresh();}
        private void refresh(){int pages=Math.max(1,(data.size()+pageSize-1)/pageSize);int from=Math.min(page*pageSize,data.size());int to=Math.min(from+pageSize,data.size());table.setItems(FXCollections.observableArrayList(data.subList(from,to)));prev.setDisable(page==0);next.setDisable(page>=pages-1);info.setText((page+1)+" / "+pages+" · "+data.size());}
        HBox controls(){HBox h=new HBox(8,prev,info,next);h.setAlignment(Pos.CENTER);h.setMaxWidth(Double.MAX_VALUE);h.getStyleClass().add("pager-controls");return h;}
    }
    private static class SceneUtil{static void set(Node n,Stage s){javafx.scene.Scene sc=s.getScene();if(sc==null)sc=new javafx.scene.Scene((Parent)n,1400,850);else sc.setRoot((Parent)n);if(sc.getStylesheets().isEmpty())sc.getStylesheets().add(DashboardController.class.getResource("/dashboard.css").toExternalForm());s.setTitle("Sistema de Gestión Turística - San Antonio de Arredondo");s.setScene(sc);s.setMinWidth(1100);s.setMinHeight(700);s.centerOnScreen();s.show();s.setMaximized(true);s.toFront();}}
}