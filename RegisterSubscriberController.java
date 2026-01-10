package gui;

import client.ClientUI;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import logic.Request;
import logic.Subscriber;

public class RegisterSubscriberController {

    public static RegisterSubscriberController activeController;

    @FXML
    private TextField nameField;

    @FXML
    private TextField phoneField;

    @FXML
    private TextField emailField;

    @FXML
    private Label resultLabel;

    @FXML
    public void initialize() {
        activeController = this;
    }

    @FXML
    private void createSubscriber() {

        String name = nameField.getText();
        String phone = phoneField.getText();
        String email = emailField.getText();

        if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            resultLabel.setText("Please fill all fields");
            return;
        }

        Subscriber sub = new Subscriber(
                0,
                name,
                email,
                phone
        );

        ClientUI.chat.sendToServer(
                new Request("REGISTER_SUBSCRIBER", sub)
        );

        resultLabel.setText("Creating subscriber...");
    }

    public void showSuccess(String displayId) {
        resultLabel.setText(
                "Subscriber created successfully!\nSubscriber ID: " + displayId
        );
    }
}
