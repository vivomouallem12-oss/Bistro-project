package gui;

import client.ClientUI;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import logic.Request;

public class ManagementInfoController {

    public static ManagementInfoController activeController;

    @FXML private Label todayReservationsLabel;
    @FXML private Label monthlyReservationsLabel;
    @FXML private Label canceledReservationsLabel;
    @FXML private Label subscribersLabel;
    @FXML private Label currentCustomersLabel;

    @FXML
    public void initialize() {
        activeController = this;
        ClientUI.chat.sendToServer(new Request("GET_MANAGEMENT_INFO", null));
    }

    public void updateData(
            int today,
            int month,
            int canceled,
            int subscribers,
            int inside) {

        todayReservationsLabel.setText(String.valueOf(today));
        monthlyReservationsLabel.setText(String.valueOf(month));
        canceledReservationsLabel.setText(String.valueOf(canceled));
        subscribersLabel.setText(String.valueOf(subscribers));
        currentCustomersLabel.setText(String.valueOf(inside));
    }
}
