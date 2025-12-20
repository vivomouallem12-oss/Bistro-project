package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import gui.AllOrdersFrameController;

public class ClientUI extends Application {

    public static ClientController chat;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // connect client to server
        chat = new ClientController("Localhost", 5555);

        // load GUI
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/AllOrders.fxml"));
        Parent root = loader.load();

        // send client to controller
        AllOrdersFrameController controller = loader.getController();
        controller.setClient(chat);

        // show window
        primaryStage.setTitle("All Orders");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        // 🔥 VERY IMPORTANT — ensure server sees disconnect
        primaryStage.setOnCloseRequest(event -> {
            try {
                if (chat != null) {
                    chat.disconnect(); // notify server
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.exit(0);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
