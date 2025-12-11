module dk.easv.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.sql;
    requires java.naming;
    requires jdk.crypto.ec;
    requires com.microsoft.sqlserver.jdbc;

    opens dk.easv.demo to javafx.fxml;
    opens dk.easv.demo.GUI.Controller to javafx.fxml;
    opens dk.easv.demo.BE to javafx.base;
    opens dk.easv.demo.BLL to javafx.base;
    opens dk.easv.demo.DAL to javafx.base;
    opens dk.easv.demo.GUI.Model to javafx.base;

    exports dk.easv.demo;
    exports dk.easv.demo.GUI.Controller;
    exports dk.easv.demo.BE;
}