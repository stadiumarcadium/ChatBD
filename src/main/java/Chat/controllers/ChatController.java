package Chat.controllers;

import Chat.models.Network;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.*;
import java.text.DateFormat;
import java.util.*;

public class ChatController {

    @FXML
    private javafx.scene.control.Button closeButton;

    @FXML
    private ListView<String> usersList;

    @FXML
    private Label usernameTitle;

    @FXML
    private TextArea chatHistory;

    @FXML
    private TextField inputField;

    @FXML
    private Button sendButton;
    private String selectedRecipient;

    @FXML
    public void initialize() {
        sendButton.setOnAction(event -> sendMessage());
        inputField.setOnAction(event -> sendMessage());
        ContextMenu contextMenu = new ContextMenu();
        MenuItem item1 = new MenuItem(" Сменить ник ");
        contextMenu.getItems().add(item1);
        usernameTitle.setOnContextMenuRequested(event -> contextMenu.show(usernameTitle, event.getScreenX(), event.getScreenY()));
        item1.setOnAction(event -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Смена ника");
            dialog.setHeaderText("Ведите новый ник:");
            dialog.showAndWait().ifPresent(name -> {
                network.sendChangeUsernameMessage(Network.CHANGE_USERNAME_PREFIX + " " + name);
                network.setUsername(name);
                setUsernameTitle(name);
            });
        });

        usersList.setCellFactory(lv -> {
            MultipleSelectionModel<String> selectionModel = usersList.getSelectionModel();
            ListCell<String> cell = new ListCell<>();
            cell.textProperty().bind(cell.itemProperty());
            cell.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                usersList.requestFocus();
                if (!cell.isEmpty()) {
                    int index = cell.getIndex();
                    if (selectionModel.getSelectedIndices().contains(index)) {
                        selectionModel.clearSelection(index);
                        selectedRecipient = null;
                    } else {
                        selectionModel.select(index);
                        selectedRecipient = cell.getItem();
                    }
                    event.consume();
                }
            });
            return cell;
        });

    }

    private Network network;

    public void setNetwork(Network network) {
        this.network = network;
    }

    private void sendMessage() {
        String message = inputField.getText().trim();
        inputField.clear();


        if (message.trim().isEmpty()) {
            return;
        }

        if (selectedRecipient != null) {
            network.sendPrivateMessage(selectedRecipient, message);
        } else if (message.startsWith(Network.CHANGE_USERNAME_PREFIX)) {
            network.sendChangeUsernameMessage(message);
        } else {
            network.sendMessage(message);
        }

        appendMessage("Я: " + message);
    }

    public void appendMessage(String message) {
        String timeStamp = DateFormat.getInstance().format(new Date());

        chatHistory.appendText(timeStamp);
        chatHistory.appendText(System.lineSeparator());
        chatHistory.appendText(message);
        chatHistory.appendText(System.lineSeparator());
        chatHistory.appendText(System.lineSeparator());
    }

    public void appendServerMessage(String serverMessage) {
        chatHistory.appendText("ВНИМАНИЕ: " + serverMessage);
        chatHistory.appendText(System.lineSeparator());
        chatHistory.appendText(System.lineSeparator());
    }

    public void setUsernameTitle(String username) {
        this.usernameTitle.setText(username);
    }


    public void updateUsersList(String[] users) {

        Arrays.sort(users);

        for (int i = 0; i < users.length; i++) {
            if (users[i].equals(network.getUsername())) {
                users[i] = ">>> " + users[i];
            }
        }

        usersList.getItems().clear();
        Collections.addAll(usersList.getItems(), users);

    }

    public void saveLog() {
        String logFileName = "history_" + network.getUsername() + ".txt";
        try {
            File log = new File(logFileName);
            if (!log.exists()) {
                log.createNewFile();
            }
            PrintWriter fileWriter = new PrintWriter(new FileWriter(log, true));
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(chatHistory.getText());
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadLog() {
        String logFileName = "history_" + network.getUsername() + ".txt";
        File log = new File(logFileName);
        List<String> logLines = new ArrayList<>();
        if (log.exists()) {
            try {
                FileInputStream in = new FileInputStream(log);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    logLines.add(line + "\n");
                }
                if (logLines.size() > 100) {
                    for (int i = logLines.size() - 100; i <= (logLines.size() - 1); i++) {
                        chatHistory.appendText(logLines.get(i) + "\n");
                    }
                } else {
                    logLines.forEach(name -> chatHistory.appendText(name + "\n"));
                }
                chatHistory.setScrollTop(Double.MAX_VALUE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}