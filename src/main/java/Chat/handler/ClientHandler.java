package Chat.handler;

import Chat.MyServer;
import Chat.authentication.AuthenticationService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class ClientHandler {
    private static final String AUTH_CMD_PREFIX = "/auth"; // + login + password
    private static final String AUTHOK_CMD_PREFIX = "/authok"; // + username
    private static final String AUTHERR_CMD_PREFIX = "/autherr"; // + error message
    private static final String CLIENT_MSG_CMD_PREFIX = "/cm"; // + msg
    private static final String SERVER_MSG_CMD_PREFIX = "/sm"; // + msg
    private static final String PRIVATE_MSG_CMD_PREFIX = "/pm"; // + client + msg
    private static final String STOP_SERVER_CMD_PREFIX = "/stop";
    private static final String END_CLIENT_CMD_PREFIX = "/end";
    private static final String GET_CLIENTS_CMD_PREFIX = "/getclients";//+ username

    private static final String REG_CMD_PREFIX = "/reg"; //+ login + pass + username
    private static final String REGOK_CMD_PREFIX = "/regok"; //
    private static final String REGERR_CMD_PREFIX = "/regerr"; // + error message

    private static final String CHANGE_USERNAME_PREFIX = "/chngname"; // + login + username

    private MyServer myServer;
    private Socket clientSocket;
    private DataOutputStream out;
    private DataInputStream in;
    private String username;

    public ClientHandler(MyServer myServer, Socket socket) {

        this.myServer = myServer;
        clientSocket = socket;
    }

    public void handle() throws IOException {
        out = new DataOutputStream(clientSocket.getOutputStream());
        in = new DataInputStream(clientSocket.getInputStream());

        new Thread(() -> {
            try {
                sign();
                readMessage();
            } catch (IOException e) {
                e.printStackTrace();
                myServer.unSubscribe(this);
                try {
                    myServer.broadcastClientDisconnected(this);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
    }

    private void sign() throws IOException {
        while (true) {
            String message = in.readUTF();
            if (message.startsWith(AUTH_CMD_PREFIX)) {
                boolean isSuccessAuth = processAuthentication(message);
                if (isSuccessAuth) {
                    break;
                }

            } else if (message.startsWith(REG_CMD_PREFIX)) {
                processSignUp(message);

            } else {
                out.writeUTF(AUTHERR_CMD_PREFIX + " Ошибка аутентификации");
                System.out.println("Неудачная попытка аутентификации");
            }
        }
    }

    private boolean processAuthentication(String message) throws IOException {
        String[] parts = message.split("\\s+");
        if (parts.length != 3) {
            out.writeUTF(AUTHERR_CMD_PREFIX + " Ошибка аутентификации");
        }
        String login = parts[1];
        String password = parts[2];

        AuthenticationService auth = myServer.getAuthenticationService();

        username = auth.getUsernameByLoginAndPassword(login, password);

        if (username != null) {
            if (myServer.isUsernameBusy(username)) {
                out.writeUTF(AUTHERR_CMD_PREFIX + " Логин уже используется");
                return false;
            }

            out.writeUTF(AUTHOK_CMD_PREFIX + " " + username);

            connectUser(username);

            return true;
        } else {
            out.writeUTF(AUTHERR_CMD_PREFIX + " Логин или пароль не соответствуют действительности");
            return false;
        }
    }

    private boolean processSignUp(String message) throws IOException {
        String[] parts = message.split("\\s+");
        if (parts.length != 4) {
            out.writeUTF(REGERR_CMD_PREFIX + " Ошибка регистрации. Неверный запрос");
        }
        String login = parts[1];
        String password = parts[2];
        String username = parts[3];

        AuthenticationService auth = myServer.getAuthenticationService();


        if (auth.checkLoginByFree(login)) {
            auth.createUser(login, password, username);
            out.writeUTF(REGOK_CMD_PREFIX);
            return true;
        } else {
            out.writeUTF(REGERR_CMD_PREFIX + " Пользователь с таким логином уже существует");
            return false;
        }
    }

    private void connectUser(String username) throws IOException {
        myServer.subscribe(this);
        System.out.println("Пользователь " + username + " подключился к чату");
        myServer.broadcastClients(this);
    }

    private void readMessage() throws IOException {
        while (true) {
            String message = in.readUTF();
            System.out.println("message | " + username + ": " + message);
            if (message.startsWith(STOP_SERVER_CMD_PREFIX)) {
                System.exit(0);
            } else if (message.startsWith(END_CLIENT_CMD_PREFIX)) {
                return;
            } else if (message.startsWith(PRIVATE_MSG_CMD_PREFIX)) {
                String[] parts = message.split("\\s+", 3);
                String recipient = parts[1];
                String privateMessage = parts[2];

                myServer.sendPrivateMessage(this, recipient, privateMessage);
            } else if(message.startsWith(CHANGE_USERNAME_PREFIX)) {
                String[] parts = message.split("\\s+", 3);
                String username = parts[1];
                String newUsername = parts[2];
                myServer.changeUsername(this, newUsername);
            }else {
                myServer.broadcastMessage(message, this);
            }

        }
    }

    public void sendMessage(String sender, String message) throws IOException {
        if (sender != null) {
            sendClientMessage(sender, message);
        } else {
            sendServerMessage(message);
        }
    }

    public void sendServerMessage(String message) throws IOException {
        out.writeUTF(String.format("%s %s", SERVER_MSG_CMD_PREFIX, message));
    }

    public void sendClientMessage(String sender, String message) throws IOException {
        out.writeUTF(String.format("%s %s %s", CLIENT_MSG_CMD_PREFIX, sender, message));
    }

    public String getUsername() {
        return username;
    }


    public void sendClientsList(List<ClientHandler> clients) throws IOException {
        String msg = String.format("%s %s", GET_CLIENTS_CMD_PREFIX, clients.toString());
        out.writeUTF(msg);
    }

    @Override
    public String toString() {
        return username;
    }
}
