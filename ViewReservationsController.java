package gui;

import client.ClientUI;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import logic.Order;
import logic.Request;

public class ViewReservationsController {

    public static ViewReservationsController activeController;

    @FXML
    private TableView<Order> reservationsTable;

    @FXML
    private TableColumn<Order, String> dateCol;

    @FXML
    private TableColumn<Order, String> timeCol;

    @FXML
    private TableColumn<Order, Integer> guestsCol;

    @FXML
    private TableColumn<Order, String> statusCol;

    private final ObservableList<Order> reservationsList =
            FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        activeController = this;

        dateCol.setCellValueFactory(
                new PropertyValueFactory<>("order_date")
        );
        timeCol.setCellValueFactory(
                new PropertyValueFactory<>("order_time")
        );
        guestsCol.setCellValueFactory(
                new PropertyValueFactory<>("number_of_guests")
        );
        statusCol.setCellValueFactory(
                new PropertyValueFactory<>("order_status")
        );

        reservationsTable.setItems(reservationsList);

        // 🔥 בקשה לשרת
        ClientUI.chat.sendToServer(
                new Request("GET_ALL_RESERVATIONS", null)
        );
    }

    // 🔹 נקרא מה־ChatClient
    public void setReservations(java.util.List<Order> orders) {
        reservationsList.clear();
        reservationsList.addAll(orders);
    }
}
