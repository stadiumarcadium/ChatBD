module Chat {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires log4j;


    opens Chat to javafx.fxml;
    exports Chat;
    exports Chat.controllers;
    opens Chat.controllers to javafx.fxml;
}