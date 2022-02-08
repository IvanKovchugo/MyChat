package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import service.ServiceMessages;

import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    public Button btnReg;
    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextArea chatTextField;

    @FXML
    private Button sendButton;

    @FXML
    private TextField textField;

    @FXML
    void initialize() {
        chatTextField.setEditable(false);

    }

    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public AnchorPane authPanel;
    @FXML
    public AnchorPane anchorMsg;
    @FXML
    public ListView<String> clientList;
    @FXML
    public AnchorPane anchorForList;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private final int PORT = 8253;
    private final String ADDRESS = "localhost";
    private Stage stage;
    private Stage regStage;
    private RegController regController;

    private boolean authenticated;
    private String nickname;
    private String toWhom;

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
        authPanel.setVisible(!authenticated);
        authPanel.setManaged(!authenticated);
        anchorMsg.setVisible(authenticated);
        anchorMsg.setManaged(authenticated);
        anchorForList.setVisible(authenticated);
        anchorForList.setManaged(authenticated);
        clientList.setVisible(authenticated);
        clientList.setManaged(authenticated);


        if(!authenticated){
            nickname = "";
        }

        setTitle(nickname);

        chatTextField.clear();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Platform.runLater(() -> {
            stage = (Stage)textField.getScene().getWindow();
            stage.setOnCloseRequest(event -> {
                if (socket != null  && !socket.isClosed()) {
                    try {
                        out.writeUTF(ServiceMessages.END);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        });
        setAuthenticated(false);
    }

    public void connect() {
        try {
            socket = new Socket(ADDRESS, PORT);

            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    // authy
                    while (true) {
                        String str = in.readUTF();

                        if (str.startsWith(ServiceMessages.SLASH)) {
                            if (str.equals(ServiceMessages.END)) {
                                break;
                            }

                            if (str.startsWith(ServiceMessages.AUTH_OK)) {
                                nickname = str.split(" ")[1];
                                setAuthenticated(true);
                                break;
                            }
                            if (str.startsWith(ServiceMessages.REG)) {
                                regController.regStatus(str);
                            }

                        } else {
                            chatTextField.appendText(getTime() + str + "\n");
                        }
                    }

                    // work
                    while (authenticated) {
                        String str = in.readUTF();

                        if (str.startsWith(ServiceMessages.SLASH)) {
                            if (str.equals(ServiceMessages.END)) {
                                setAuthenticated(false);
                                break;
                            }

                            chatTextField.appendText(str + "\n");
                            if (str.startsWith(ServiceMessages.CLIENTLIST)) {
                                String[] token = str.split(" ", 3);
                                Platform.runLater(() -> {
                                    clientList.getItems().clear();
                                    for (int i = 1; i < token.length; i++) {
                                        clientList.getItems().add(token[i]);
                                    }
                                });
                            }

                            if (str.startsWith("/nicks ")) {
                                nickname = str.split(" ")[1];
                                setTitle(nickname);
                            }

                        } else{
                                chatTextField.appendText(str + "\n");

                                Last100Messages.writeLine(str);
                            }
                        }

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void clickBtnSendText(ActionEvent actionEvent) {
        if (textField.getText().length() > 0) {
            try {
                out.writeUTF(textField.getText());
                textField.clear();
                textField.requestFocus();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getTime() {
        return new SimpleDateFormat("HH:mm:ss ").format(new Date());
    }

    public void clickBtnAuth(ActionEvent actionEvent) {
        if (socket == null || socket.isClosed()) {
            connect();
        }

        try {
            String msg = String.format("%s %s %s", ServiceMessages.AUTH,
                    loginField.getText().trim(), passwordField.getText().trim());
            out.writeUTF(msg);
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void setTitle(String nickname) {
        String title;
        if (nickname.equals("")) {
            title = "MyChat";
        }else {
            title = String.format("MyChat - %s", nickname);
        }
        Platform.runLater(() -> {
            stage.setTitle(title);
        });
    }

    public void clickBtnClientList(MouseEvent mouseEvent) {
        String toWhom = clientList.getSelectionModel().getSelectedItem();
        textField.setText(ServiceMessages.W + " " + toWhom + " ");
    }

    public void btnRegClick(ActionEvent actionEvent) {
        if (regStage == null) {
            createRegWindow();
        }
        regStage.show();
    }

    private void createRegWindow() {

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/RegView.fxml"));
            Parent root = fxmlLoader.load();
            regStage = new Stage();
            regStage.setTitle("MyChat registration");
            regStage.setScene(new Scene(root,700, 410));

            regStage.initModality(Modality.APPLICATION_MODAL);
            regStage.initStyle(StageStyle.UTILITY);

            regController = fxmlLoader.getController();
            regController.setController(this);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToReg(String login, String password, String nickname) {
        if (socket == null || socket.isClosed()) {
            connect();
        }
        String msg = String.format("%s %s %s %s", ServiceMessages.REG ,login, password, nickname);
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}