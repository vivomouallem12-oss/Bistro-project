package gui;

import client.ClientController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

public class SubscriberMainController {

    @FXML
    private AnchorPane rootPane;

    @FXML
    private Label lblWelcome;

    @FXML
    private Button btnMakeReservation;

    @FXML
    private Button btnJoinWaitingList;

    @FXML
    private Button btnPayBill;

    @FXML
    private Button btnMyAccount;

    @FXML
    private Button btnLogout;

    // ===== DATA FROM LOGIN =====
    private ClientController client;
    private int subscriberId;
    private String subscriberName;

    public void setClient(ClientController client) {
        this.client = client;
    }

    public void initSubscriber(int id, String name) {
        this.subscriberId = id;
        this.subscriberName = name;

        lblWelcome.setText("Welcome, " + name + "  (ID: " + id + ")");
    }


    // ===== BUTTON HANDLERS =====

    @FXML
    private void onMakeReservation(ActionEvent event) {
        openScreen("/gui/SubscriberReservation.fxml", event);
    }

    @FXML
    private void onJoinWaitingList(ActionEvent event) {
        openScreen("/gui/SubscriberWaitingList.fxml", event);
    }

    @FXML
    private void onPayBill(ActionEvent event) {
        openScreen("/gui/SubscriberPayment.fxml", event);
    }

    @FXML
    private void onMyAccount(ActionEvent event) {
        openScreen("/gui/SubscriberAccount.fxml", event);
    }

    @FXML
    private void onLogout(ActionEvent event) {
        openScreen("/gui/RoleSelection.fxml", event);
    }

    // ===== SCREEN SWITCHER =====
    private void openScreen(String fxmlPath, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            javafx.stage.Stage newStage = new javafx.stage.Stage();
            newStage.setScene(new javafx.scene.Scene(root));
            newStage.show();

            // ---- CLOSE CURRENT WINDOW ----
            javafx.stage.Stage currentStage =
                    (javafx.stage.Stage)((javafx.scene.Node)event.getSource())
                            .getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
