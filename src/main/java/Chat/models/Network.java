package Chat.models;

import Chat.controllers.ChatController;
import javafx.application.Platform;

import java.io.*;
import java.net.Socket;

public class Network {

    private static final String AUTH_CMD_PREFIX = "/auth"; // + login + password
    private static final String AUTHOK_CMD_PREFIX = "/authok"; // + username
    private static final String AUTHERR_CMD_PREFIX = "/autherr"; // + error message
    private static final String CLIENT_MSG_CMD_PREFIX = "/cm"; // + msg
    private static final String SERVER_MSG_CMD_PREFIX = "/sm"; // + msg
    private static final String PRIVATE_MSG_CMD_PREFIX = "/pm"; // + msg
    private static final String STOP_SERVER_CMD_PREFIX = "/stop";
    private static final String END_CLIENT_CMD_PREFIX = "/end";
    private static final String GET_CLIENTS_CMD_PREFIX = "/getclients";//+ username

    private static final String REG_CMD_PREFIX = "/reg"; //+ login + pass + username
    private static final String REGOK_CMD_PREFIX = "/regok"; //
    private static final String REGERR_CMD_PREFIX = "/regerr"; // + error message

    public static final String CHANGE_USERNAME_PREFIX = "/chngname"; // + login + username


    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 8186;
    private DataInputStream in;
    private DataOutputStream out;

    private final String host;
    private final int port;
    private String username;

    public Network(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public Network() {
        this.host = DEFAULT_HOST;
        this.port = DEFAULT_PORT;
    }


    public void connect() {
        try {
            Socket socket = new Socket(host, port);

            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Соединение не установлено");
        }
    }

    public DataOutputStream getOut() {
        return out;
    }

    public void waitMessage(ChatController chatController) {
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    String message = in.readUTF();

                    if (message.startsWith(CLIENT_MSG_CMD_PREFIX)) {
                        String[] parts = message.split("\\s+", 3);
                        String sender = parts[1];
                        String messageFromSender = parts[2];

                        Platform.runLater(() -> chatController.appendMessage(String.format("%s: %s", sender, messageFromSender)));
                    } else if (message.startsWith(SERVER_MSG_CMD_PREFIX)) {
                        String[] parts = message.split("\\s+", 2);
                        String serverMessage = parts[1];

                        Platform.runLater(() -> chatController.appendServerMessage(serverMessage));
                    } else if (message.startsWith(GET_CLIENTS_CMD_PREFIX)) {
                        message = message.substring(message.indexOf('[') + 1, message.indexOf(']'));
                        String[] users = message.split(", ");
                        Platform.runLater(() -> chatController.updateUsersList(users));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        t.setDaemon(true);
        t.start();

    }

    public String sendAuthMessage(String login, String password) {
        try {
            out.writeUTF(String.format("%s %s %s", AUTH_CMD_PREFIX, login, password));
            String response = in.readUTF();
            if (response.startsWith(AUTHOK_CMD_PREFIX)) {
                this.username = response.split("\\s+", 2)[1];
                return null;
            } else {
                return response.split("\\s+", 2)[1];
            }
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }

    }

    public String getUsername() {
        return username;
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Ошибка при отправке сообщения");
        }
    }

    public void sendPrivateMessage(String selectedRecipient, String message) {
        sendMessage(String.format("%s %s %s", PRIVATE_MSG_CMD_PREFIX, selectedRecipient, message));
    }

    public String sendSignUpMessage(String login, String password, String username) {
        try {
            out.writeUTF(String.format("%s %s %s %s", REG_CMD_PREFIX, login, password, username));
            String response = in.readUTF();
            if (response.startsWith(REGOK_CMD_PREFIX)) {
                return null;
            } else {
                return response.split("\\s+", 2)[1];
            }
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }

    }

    public void sendChangeUsernameMessage(String message) {
        String newUsername = message.split("\s+")[1];
        sendMessage(String.format("%s %s %s", CHANGE_USERNAME_PREFIX, username, newUsername));
    }

}
