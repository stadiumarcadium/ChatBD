package Chat.controllers;

import Chat.StartClient;
import Chat.models.Network;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;


public class SignController {

    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;
    @FXML
    private TextField loginReg;
    @FXML
    private TextField passReg;
    @FXML
    private TextField usernameReg;

    private Network network;
    private StartClient startClient;

    public void checkAuth(String login, String password) {

        if (login == null || password == null) {
            startClient.showErrorAlert("Ошибка ввода при аутентификации", "Поля не заданы");

        }

        if (login.length() == 0 || password.length() == 0) {
            startClient.showErrorAlert("Ошибка ввода при аутентификации", "Поля не должны быть пустыми");

            return;
        }

        String authErrorMessage = network.sendAuthMessage(login, password);

        if (authErrorMessage == null) {
            startClient.openChatDialog();
        } else {
            startClient.showErrorAlert("Ошибка аутентификации", authErrorMessage);
        }
    }

    @FXML
    public void checkAuth() {
        String login = loginField.getText().trim();
        String password = passwordField.getText().trim();

        checkAuth(login, password);
    }

    @FXML
    void signUp() {
        String login = loginReg.getText().trim();
        String password = passReg.getText().trim();
        String username = usernameReg.getText().trim();

        if (login.length() == 0 || password.length() == 0 || username.length() == 0) {
            startClient.showErrorAlert("Ошибка регистрации", "Поля не должны быть пустыми");
            return;
        }

        String signUpErrorMessage = network.sendSignUpMessage(login, password, username);

        if (signUpErrorMessage == null) {
            startClient.showInformationAlert("Поздравляем с регистрацией!", "Добро пожаловать");
            checkAuth(login, password);
        } else {
            startClient.showErrorAlert("Ошибка регистрации", signUpErrorMessage);
        }
    }

    public void setNetwork(Network network) {
        this.network = network;
    }


    public void setStartClient(StartClient startClient) {
        this.startClient = startClient;
    }



}
