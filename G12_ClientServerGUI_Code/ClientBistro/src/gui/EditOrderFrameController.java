package gui;

import client.ClientController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import logic.Order;

public class EditOrderFrameController {

    private ClientController client;
    private Order currentOrder;

    @FXML private TextField txtOrderNumber;
    @FXML private TextField txtOrderDate;
    @FXML private TextField txtGuests;
    @FXML private TextField txtConfirmationCode;
    @FXML private TextField txtSubscriberId;
    @FXML private TextField txtPlacedOn;

    public void setClient(ClientController client) {
        this.client = client;
    }

    /**
     * Load an order into the edit form.
     */
    public void loadOrder(Order o) {
        this.currentOrder = o;

        txtOrderNumber.setText(String.valueOf(o.getOrder_number()));
        txtOrderDate.setText(o.getOrder_date());
        txtGuests.setText(String.valueOf(o.getNumber_of_guests()));
        txtConfirmationCode.setText(String.valueOf(o.getConfirmation_code()));
        txtSubscriberId.setText(String.valueOf(o.getSubscriber_id()));
        txtPlacedOn.setText(o.getDate_of_placing_order());

        txtOrderNumber.setDisable(true);
        txtConfirmationCode.setDisable(true);
        txtSubscriberId.setDisable(true);
        txtPlacedOn.setDisable(true);

        txtOrderDate.setDisable(false);
        txtGuests.setDisable(false);
    }

    /**
     * Fetch order by order number when user types it and clicks "Edit"
     */
    @FXML
    public void fetchOrder(ActionEvent event) {
        try {
            int orderNum = Integer.parseInt(txtOrderNumber.getText());
            client.sendToServer(Integer.valueOf(orderNum)); // send Integer
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid order number.");
        }
    }

    /**
     * Save the updated order to the server
     */
    @FXML
    public void saveOrder(ActionEvent event) {
        try {
            if (currentOrder == null) {
                showAlert(Alert.AlertType.ERROR, "No Order Loaded", "Please load an order first.");
                return;
            }

            // Update fields in currentOrder
            currentOrder.setOrder_date(txtOrderDate.getText());
            currentOrder.setNumber_of_guests(Integer.parseInt(txtGuests.getText()));

            // Send updated order to server
            client.sendToServer(currentOrder);

            showAlert(Alert.AlertType.INFORMATION, "Success", "Order update sent to server.");

            // Close edit window and return to AllOrders view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/AllOrders.fxml"));
            ((Node) event.getSource()).getScene().getWindow().hide();
            Stage primaryStage = new Stage();
            Pane root = loader.load();
            AllOrdersFrameController afc = loader.getController();
            afc.setClientAfterEdit(client);
            Scene scene = new Scene(root);
            primaryStage.setTitle("All Orders");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Number of guests must be a number.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    /**
     * Return to AllOrders view without saving
     */
    @FXML
    public void returns(ActionEvent event) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/AllOrders.fxml"));
        ((Node) event.getSource()).getScene().getWindow().hide();
        Stage primaryStage = new Stage();
        Pane root = loader.load();
        AllOrdersFrameController afc = loader.getController();
        afc.setClientAfterEdit(client);
        Scene scene = new Scene(root);
        primaryStage.setTitle("All Orders");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Helper method to show alerts
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
