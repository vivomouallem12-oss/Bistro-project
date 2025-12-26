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

    /** Load an order into the edit form */
    public void loadOrder(Order o) {
        this.currentOrder = o;

        txtOrderNumber.setText(String.valueOf(o.getOrderNumber()));
        txtOrderDate.setText(o.getOrderDate());
        txtGuests.setText(String.valueOf(o.getNumberOfGuests()));
        txtConfirmationCode.setText(o.getConfirmationCode());
        txtSubscriberId.setText(String.valueOf(o.getSubscriberId()));
        txtPlacedOn.setText(o.getDateOfPlacingOrder());

        txtOrderNumber.setDisable(true);
        txtConfirmationCode.setDisable(true);
        txtSubscriberId.setDisable(true);
        txtPlacedOn.setDisable(true);

        txtOrderDate.setDisable(false);
        txtGuests.setDisable(false);
    }

    /** Fetch order by order number */
    @FXML
    public void fetchOrder(ActionEvent event) {
        try {
            int orderNum = Integer.parseInt(txtOrderNumber.getText());
            client.sendToServer(orderNum);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input",
                    "Please enter a valid order number.");
        }
    }

    /** Save changes */
    @FXML
    public void saveOrder(ActionEvent event) {
        try {
            if (currentOrder == null) {
                showAlert(Alert.AlertType.ERROR, "No Order Loaded",
                        "Please load an order first.");
                return;
            }

            // update fields
            currentOrder.setOrderDate(txtOrderDate.getText());
            currentOrder.setNumberOfGuests(Integer.parseInt(txtGuests.getText()));

            // send to server
            client.sendToServer(currentOrder);

            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "Order update sent to server.");

            // return to All Orders screen
            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/gui/AllOrders.fxml"));
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
            showAlert(Alert.AlertType.ERROR, "Invalid Input",
                    "Number of guests must be a number.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Unexpected error: " + e.getMessage());
        }
    }

    /** Return without saving */
    @FXML
    public void returns(ActionEvent event) throws Exception {
        FXMLLoader loader =
                new FXMLLoader(getClass().getResource("/gui/AllOrders.fxml"));
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

    /** Alert helper */
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
