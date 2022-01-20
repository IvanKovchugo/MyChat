package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

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

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private final int PORT = 8253;
    private final String ADDRESS = "localhost";
    private Stage stage;

    private boolean authenticated;
    private String nickname;
    private String toWhom;

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
        authPanel.setVisible(!authenticated);
        authPanel.setManaged(!authenticated);
        anchorMsg.setVisible(authenticated);
        anchorMsg.setManaged(authenticated);

        if(!authenticated){
            nickname = "";
        }

        setTitle(nickname);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Platform.runLater(() -> {
            stage = (Stage)textField.getScene().getWindow();
            stage.setOnCloseRequest(event -> {
                if (socket != null  && !socket.isClosed()) {
                    try {
                        out.writeUTF("/end");
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

                        if (str.startsWith("/")) {
                            if (str.equals("/end")) {
                                break;
                            }
                        } else {
                            chatTextField.appendText(getTime() + str + "\n");
                        }
                        if (str.startsWith("/authok")) {
                            nickname = str.split(" ") [1];
                            setAuthenticated(true);
                            break;
                        }
                    }

                    // work
                    while (authenticated) {
                        String str = in.readUTF();

                        if (str.equals("/end")) {
                            setAuthenticated(false);
                            break;
                        }
                        if (str.startsWith("/w")) {
                            nickname = str.split(" ")[1];
                            toWhom = str.split(" ")[2];

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
            String msg = String.format("/auth %s %s", loginField.getText().trim(), passwordField.getText().trim());
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
}