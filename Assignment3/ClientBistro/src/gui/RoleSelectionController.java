package gui;

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

public class RoleSelectionController {

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private StackPane guestCard;
    @FXML
    private StackPane subscriberCard;
    @FXML
    private StackPane agentCard;
    @FXML
    private StackPane managerCard;

    @FXML
    public void initialize() {

        // Click logic
        guestCard.setOnMouseClicked(e -> openScreen("GuestView.fxml"));
        subscriberCard.setOnMouseClicked(e -> openScreen("SubscriberLogin.fxml"));
        agentCard.setOnMouseClicked(e -> openScreen("AgentView.fxml"));
        managerCard.setOnMouseClicked(e -> openScreen("ManagerView.fxml"));

        // Hover Animation
        applyHoverEffect(guestCard);
        applyHoverEffect(subscriberCard);
        applyHoverEffect(agentCard);
        applyHoverEffect(managerCard);
    }

    private void applyHoverEffect(StackPane card) {

        card.setOnMouseEntered(e -> {
            card.setScaleX(1.05);
            card.setScaleY(1.05);
            card.setStyle(card.getStyle()
                    + "-fx-effect:dropshadow(gaussian, rgba(0,255,255,0.4),45,0.4,0,10);");
        });

        card.setOnMouseExited(e -> {
            card.setScaleX(1);
            card.setScaleY(1);
            card.setStyle(card.getStyle()
                    + "-fx-effect:dropshadow(gaussian, rgba(0,0,0,0.5),35,0.3,0,10);");
        });
    }

    private void openScreen(String fxmlName) {
        try {
            javafx.fxml.FXMLLoader loader =
                    new javafx.fxml.FXMLLoader(getClass().getResource(fxmlName));

            javafx.scene.Parent root = loader.load();
            anchorPane.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
