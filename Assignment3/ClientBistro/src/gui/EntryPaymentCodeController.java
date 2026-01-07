package gui;

import client.ClientUI;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import logic.Request;
import logic.Subscriber;

public class EntryPaymentCodeController {

    @FXML
    private TextField txtConfirmationCode;

    private Subscriber subscriber;

    /* ================= SETTERS ================= */

    public void setSubscriber(Subscriber subscriber) {
        this.subscriber = subscriber;
    }

    /* ================= CONTINUE ================= */

    @FXML
    private void onContinue() {

        if (subscriber == null) {
            showError("Subscriber not loaded.");
            return;
        }

        String codeStr = txtConfirmationCode.getText();

        if (codeStr == null || codeStr.isBlank()) {
            showError("Please enter confirmation code.");
            return;
        }

        int code;
        try {
            code = Integer.parseInt(codeStr.trim());
        } catch (NumberFormatException e) {
            showError("Confirmation code must contain numbers only.");
            return;
        }

        // ✅ ONLY CHECK if order can be paid
        ClientUI.chat.sendToServer(
                new Request("CHECK_ORDER_FOR_PAYMENT", code)
        );

        // ❌ DO NOT close window here
        // ❌ DO NOT pay here
    }

    /* ================= CALLED FROM ChatClient ================= */

    public void closeWindow() {
        Stage stage = (Stage) txtConfirmationCode.getScene().getWindow();
        stage.close();
    }

    /* ================= CANCEL ================= */

    @FXML
    private void onCancel() {
        closeWindow();
    }

    /* ================= HELPERS ================= */

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
