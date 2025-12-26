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
    private TextField confirmationCodeField;

    @FXML
    private Label errorLabel;

    /** =====================
     *        LOGIN
     * ===================== */
    @FXML
    private void onLogin() {
        String id = subscriberIdField.getText();
        String code = confirmationCodeField.getText();

        // Validation
        if (id == null || id.trim().isEmpty()) {
            errorLabel.setText("Subscriber ID is required");
            return;
        }

        if (code == null || code.trim().isEmpty()) {
            errorLabel.setText("Confirmation code is required");
            return;
        }

        try {
            SubscriberLoginRequest req = new SubscriberLoginRequest(id, code);
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

    /** ==========================================================
     *  This method is called FROM ClientController on success
     *  ========================================================== */
    public void openSubscriberHome() {
        Platform.runLater(() -> {
            try {
                Stage stage =
                        (Stage) subscriberIdField.getScene().getWindow();

                FXMLLoader loader =
                        new FXMLLoader(getClass().getResource("/gui/SubscriberHome.fxml"));

                Scene scene = new Scene(loader.load());
                stage.setScene(scene);
                stage.setTitle("Subscriber Home");
                stage.show();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /** Server error display helper */
    public static void showServerError(String msg) {
        Platform.runLater(() -> System.out.println("Server Error: " + msg));
    }
}
