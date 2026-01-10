package gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ManagerHomePageController {

    private void switchScene(ActionEvent event, String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onRegisterSubscriber(ActionEvent event) {
        switchScene(event, "/gui/RegisterSubscriber.fxml");
    }

    @FXML
    private void onManageTables(ActionEvent event) {
        switchScene(event, "/gui/TableManagement.fxml");
    }

    @FXML
    private void onViewReservations(ActionEvent event) {
        switchScene(event, "/gui/ViewReservation.fxml");
    }

    @FXML
    private void onOpeningHours(ActionEvent event) {
        switchScene(event, "/gui/OpeningHours.fxml");
    }

    @FXML
    private void onWaitingList(ActionEvent event) {
        switchScene(event, "/gui/WaitingList.fxml");
    }

    @FXML
    private void onViewingManagementInformation(ActionEvent event) {
        switchScene(event, "/gui/ManagementInfo.fxml");
    }

    @FXML
    private void onReportsAndAnalysis(ActionEvent event) {
        switchScene(event, "/gui/ReportsAnalysis.fxml");
    }

    @FXML
    private void onLogout(ActionEvent event) {
        switchScene(event, "/gui/RoleSelection.fxml");
    }
}