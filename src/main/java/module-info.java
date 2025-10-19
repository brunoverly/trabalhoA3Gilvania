module com.example.trabalhoA3Gilvania {
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
   // requires mysql.connector.java;

    requires com.dlsc.formsfx;
    requires java.desktop;
    requires javafx.base;
    requires static lombok;
    requires org.apache.poi.poi;
    requires org.apache.commons.collections4;
    requires org.apache.poi.ooxml;
    requires mysql.connector.j;

    opens com.example.trabalhoA3Gilvania to javafx.fxml;
    exports com.example.trabalhoA3Gilvania;
    exports com.example.trabalhoA3Gilvania.screen;
    opens com.example.trabalhoA3Gilvania.screen to javafx.fxml;
    exports com.example.trabalhoA3Gilvania.controller;
    opens com.example.trabalhoA3Gilvania.controller to javafx.fxml;
}