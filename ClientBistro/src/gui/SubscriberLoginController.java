package gui;

import client.ClientUI;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import logic.SubscriberLoginRequest;

public class SubscriberLoginController {

    @FXML
    private TextField subscriberIdField;

    @FXML
    private TextField username; // subscriber name

    @FXML
    private Label errorLabel;

    /** =====================
     *        LOGIN
     * ===================== */
    @FXML
    private void onLogin() {
        String id = subscriberIdField.getText();
        String name = username.getText();

        // Validation
        if (id == null || id.trim().isEmpty()) {
            errorLabel.setText("Subscriber ID is required");
            return;
        }

        if (name == null || name.trim().isEmpty()) {
            errorLabel.setText("Subscriber name is required");
            return;
        }

        try {
            SubscriberLoginRequest req = new SubscriberLoginRequest(id, name);
            ClientUI.chat.sendToServer(req);
            errorLabel.setText(""); // clear error
        } catch (Exception e) {
            errorLabel.setText("Failed to connect to server");
        }
    }

    /** =====================
     *        BACK
     * ===================== */
    @FXML
    private void onBack() {
        try {
            Stage stage = (Stage) subscriberIdField.getScene().getWindow();

            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/gui/RoleSelection.fxml"));

            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.setTitle("Role Selection");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    /** Server error display helper */
    public static void showServerError(String msg) {
        // you can later connect this to a Label in the login screen
        Platform.runLater(() -> System.out.println("Server Error: " + msg));
    }
}
