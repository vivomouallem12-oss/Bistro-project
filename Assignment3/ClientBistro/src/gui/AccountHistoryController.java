package gui;

import client.ClientController;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import logic.*;

import java.util.List;

public class AccountHistoryController implements SubscriberChildScreen {

    /* ================= TABLES ================= */

    @FXML private TableView<Order> reservationsTable;
    @FXML private TableView<Order> visitsTable;

    /* ===== RESERVATIONS COLUMNS ===== */
    @FXML private TableColumn<Order, Integer> colResId;
    @FXML private TableColumn<Order, String> colResDate;
    @FXML private TableColumn<Order, Integer> colGuests;
    @FXML private TableColumn<Order, String> colResStatus;

    /* ===== VISITS COLUMNS ===== */
    @FXML private TableColumn<Order, String> colVisitDate;
    @FXML private TableColumn<Order, Integer> colVisitPeople;
    @FXML private TableColumn<Order, String> colVisitStatus;

    @FXML private Button btnBack;

    private ClientController client;
    private Subscriber subscriber;

    private static AccountHistoryController ACTIVE;

    /* ================= INIT ================= */

    @FXML
    public void initialize() {
        ACTIVE = this;

        /* ---------- RESERVATIONS ---------- */
        colResId.setCellValueFactory(d ->
                new SimpleIntegerProperty(d.getValue().getOrder_number()).asObject());

        colResDate.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getOrder_date()));

        colGuests.setCellValueFactory(d ->
                new SimpleIntegerProperty(d.getValue().getNumber_of_guests()).asObject());

        colResStatus.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getOrder_status()));

        /* ---------- VISITS ---------- */
        colVisitDate.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getOrder_date()));

        colVisitPeople.setCellValueFactory(d ->
                new SimpleIntegerProperty(d.getValue().getNumber_of_guests()).asObject());

        colVisitStatus.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getOrder_status()));

        btnBack.setOnAction(e -> goBack());
    }

    public static AccountHistoryController getActive() {
        return ACTIVE;
    }

    /* ================= CONTEXT ================= */

    @Override
    public void setClient(ClientController client) {
        this.client = client;
    }

    @Override
    public void setSubscriber(Subscriber subscriber) {
        this.subscriber = subscriber;

        if (client != null && subscriber != null) {
            // 🔥 request BOTH histories
            client.sendToServer(
                    new Request("ACCOUNT_RESERVATIONS", subscriber.getSubscriberId())
            );

            client.sendToServer(
                    new Request("ACCOUNT_VISITS", subscriber.getSubscriberId())
            );
        }
    }

    /* ================= SERVER CALLBACKS ================= */

    public void handleReservationHistory(List<Order> reservations) {
        Platform.runLater(() ->
                reservationsTable.setItems(
                        FXCollections.observableArrayList(reservations)
                )
        );
    }

    public void handleVisitHistory(List<Order> visits) {
        Platform.runLater(() ->
                visitsTable.setItems(
                        FXCollections.observableArrayList(visits)
                )
        );
    }

    /* ================= NAVIGATION ================= */

    private void goBack() {
        try {
            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/gui/SubscriberMain.fxml"));

            Parent root = loader.load();

            SubscriberMainController ctrl = loader.getController();
            ctrl.setClient(client);
            ctrl.setSubscriber(subscriber);

            Stage stage = (Stage) btnBack.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
