package gui;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import client.ClientController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import logic.Order;
import Server.MySQLConnection; // <-- added import

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

    @FXML
    public void saveOrder(ActionEvent event) throws Exception {

        Connection conn = null;
        PreparedStatement psOrder = null;

        try {
            // 🔥 Use Singleton instead of DriverManager
            conn = MySQLConnection.getInstance().getConnection();
            System.out.println("SQL connection success");

            String sqlOrder =
                    "UPDATE `order` SET order_date = ?, number_of_guests = ? WHERE order_number = ?";

            psOrder = conn.prepareStatement(sqlOrder);

            psOrder.setString(1, txtOrderDate.getText());
            psOrder.setInt(2, Integer.parseInt(txtGuests.getText()));
            psOrder.setInt(3, Integer.parseInt(txtOrderNumber.getText()));

            psOrder.executeUpdate();


            FXMLLoader loader = new FXMLLoader();
            ((Node) event.getSource()).getScene().getWindow().hide();
            Stage primaryStage = new Stage();
            Pane root = loader.load(getClass().getResource("/gui/AllOrders.fxml").openStream());
            AllOrdersFrameController afc = loader.getController();
            afc.setClientAfterEdit(client);
            Scene scene = new Scene(root);
            primaryStage.setTitle("All orders");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        } finally {
            try {
                if (psOrder != null) psOrder.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
            }
        }
    }

    public void returns(ActionEvent event) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        ((Node) event.getSource()).getScene().getWindow().hide();
        Stage primaryStage = new Stage();
        Pane root = loader.load(getClass().getResource("/gui/AllOrders.fxml").openStream());
        AllOrdersFrameController afc = loader.getController();
        Scene scene = new Scene(root);
        primaryStage.setTitle("All orders");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
