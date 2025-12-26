package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientUI extends Application {

    public static ClientController chat;

    @Override
    public void start(Stage primaryStage) {
        try {
            // Create client
            chat = new ClientController("localhost", 5555);

            // 🔥 FORCE real connection NOW
            chat.openConnection();

        } catch (Exception e) {
            System.out.println("❌ Server is OFF, cannot connect");
            showServerError();
            return;   // stop here
        }

        try {
            Parent root =
                    FXMLLoader.load(getClass().getResource("/gui/RoleSelection.fxml"));

            primaryStage.setTitle("Select Role");
            primaryStage.setScene(new Scene(root));
            primaryStage.show();

            primaryStage.setOnCloseRequest(event -> {
                try {
                    if (chat != null)
                        chat.closeConnection();   // 🔥 Proper disconnect
                } catch (Exception ignored) {}
                System.exit(0);
            });

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ Problem loading RoleSelection.fxml");
        }
    }

    private void showServerError() {
        javafx.scene.control.Alert alert =
                new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Connection Failed");
        alert.setHeaderText("Server Not Available");
        alert.setContentText("Please start the server and try again.");
        alert.showAndWait();
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
