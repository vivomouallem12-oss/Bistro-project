package gui;

import Server.ServerUI;
import client.ChatClient;
import client.ClientController;
import client.ClientUI;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import logic.Order;

public class AllOrdersFrameController {

    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, Integer> colOrderNumber;
    @FXML private TableColumn<Order, String> colOrderDate;
    @FXML private TableColumn<Order, Integer> colGuests;
    @FXML private TableColumn<Order, Integer> colConfirmation;
    @FXML private TableColumn<Order, String> colSubscriber;
    @FXML private TableColumn<Order, String> colPlacedOn;
    @FXML private TextField orderNumberInput;
    @FXML private Button editBtn;
 

    // Shared list (static if accessed by client listener)
    public static ObservableList<Order> ordersList = FXCollections.observableArrayList();

    // Reference to your ChatClient
    private ClientController client;


    // Method to set client from main app
    public void setClient(ClientController client) {
        this.client = client;
       
        // Request orders in a new thread
        new Thread(() -> {
            System.out.println("Requesting orders...");
            client.accept("getOrders");
        }).start();
    }
    
    public void setClientAfterEdit(ClientController client) {
    	ordersList.clear();
    	setClient(client);
    }


    @FXML
    public void initialize() {
        colOrderNumber.setCellValueFactory(new PropertyValueFactory<>("order_number"));
        colOrderDate.setCellValueFactory(new PropertyValueFactory<>("order_date"));
        colGuests.setCellValueFactory(new PropertyValueFactory<>("number_of_guests"));
        colConfirmation.setCellValueFactory(new PropertyValueFactory<>("confirmation_code"));
        colSubscriber.setCellValueFactory(new PropertyValueFactory<>("subscriber_id"));
        colPlacedOn.setCellValueFactory(new PropertyValueFactory<>("date_of_placing_order"));
        ordersTable.setItems(ordersList);
    }

    public static void addOrder(Order o) {
    	ordersList.add(o);
      //  Platform.runLater(() -> ordersList.add(o));
    }

    
    public void editOrder(ActionEvent event) {
        String oNum = orderNumberInput.getText();//read the number in the text field (what order to edit)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/EditOrder.fxml"));//prepare the editorder gui

        ChatClient.orderCallback = order -> {
            if (order.getOrder_number() == -1) {//check if the number that user input is for order in the list
                System.out.println("Order Not Found!");
            } else {
                System.out.println("Order Found!");
                try {//if the order found open the editorder gui for the order so client can edit
                    Stage primaryStage = new Stage();
                    Pane root = loader.load();
                    EditOrderFrameController editOrder = loader.getController();
                    editOrder.setClient(client);
                    editOrder.loadOrder(order);

                    primaryStage.setTitle("Order " + order.getOrder_number());
                    primaryStage.setScene(new Scene(root));
                    primaryStage.show();

                    ((Node) event.getSource()).getScene().getWindow().hide();//close the allorderframe gui
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            ChatClient.orderCallback = null;
        };
        new Thread(() -> ClientUI.chat.accept(oNum)).start();
    }




    
    
  
}