module com.example.sistema_municipalidad {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.sistema_municipalidad to javafx.fxml;
    exports com.example.sistema_municipalidad;
    exports com.example.sistema_municipalidad.model;
    opens com.example.sistema_municipalidad.model to javafx.fxml;
    exports com.example.sistema_municipalidad.dao;
    opens com.example.sistema_municipalidad.dao to javafx.fxml;
}