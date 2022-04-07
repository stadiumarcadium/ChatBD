module Chat {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens Chat to javafx.fxml;
    exports Chat;
    exports Chat.controllers;
    opens Chat.controllers to javafx.fxml;
}