package client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import service.ServiceMessages;

public class RegController {
    @FXML
    public PasswordField regPasswordField;
    @FXML
    public TextField regLoginField;
    @FXML
    public TextField regNicknameField;
    @FXML
    public TextArea textAreaRegistration;
    @FXML
    public Button cBtnReg;

    private Controller controller;

    public void setController(Controller controller) {
        this.controller = controller;
    }

    @FXML
    public void clickBtnReg(ActionEvent actionEvent) {
        String login = regLoginField.getText().trim();
        String password = regPasswordField.getText().trim();
        String nickname = regNicknameField.getText().trim();
        controller.tryToReg(login, password, nickname);
    }

    public void regStatus (String result){
        if (result.equals(ServiceMessages.REG_OK)) {
            textAreaRegistration.appendText("Registration successful!\n");
        } else {
            textAreaRegistration.appendText("Registration error. Login or nickname already exist!\n");
        }
    }
}