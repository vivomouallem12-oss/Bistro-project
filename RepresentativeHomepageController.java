package gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class RepresentativeHomepageController {

    private void switchScene(ActionEvent event, String fxml) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource(fxml));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
    

    @FXML
    void onRegisterSubscriber(ActionEvent event) throws Exception {
        switchScene(event, "/gui/RegisterSubscriber.fxml");
    }

    @FXML
    void onManageTables(ActionEvent event) throws Exception {
        switchScene(event, "/gui/TableManagement.fxml");
    }
    @FXML
    void onViewReservations(javafx.event.ActionEvent event) throws Exception {
        switchScene(event, "/gui/ViewReservation.fxml");
    }
    @FXML
    void onOpeningHours(javafx.event.ActionEvent event) throws Exception {
        switchScene(event, "/gui/OpeningHours.fxml");
    }
    @FXML
    void onWaitingList(javafx.event.ActionEvent event) throws Exception {
        switchScene(event, "/gui/WaitingList.fxml");
    }
    @FXML
    private void onLogout(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/gui/RoleSelection.fxml"));
            Scene scene = new Scene(loader.load());

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource())
                    .getScene().getWindow();

            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
