package gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import logic.OpenHours;
import logic.Request;
import client.ClientUI;


public class OpeningHoursController {

    @FXML
    private ComboBox<String> dayBox;

    @FXML
    private TextField openField;

    @FXML
    private TextField closeField;

    @FXML
    private Label statusLabel;

    @FXML
    public void initialize() {
        dayBox.getItems().addAll(
                "Sunday",
                "Monday",
                "Tuesday",
                "Wednesday",
                "Thursday",
                "Friday",
                "Saturday",
                "Holiday / Special Day"
        );
    }

    @FXML
    private void saveHours() {
        String dayStr = dayBox.getValue();
        String open = openField.getText();
        String close = closeField.getText();

        if (dayStr == null || open.isEmpty() || close.isEmpty()) {
            statusLabel.setText("Please fill all fields");
            return;
        }

        int day = convertDayToInt(dayStr);

        OpenHours hours = new OpenHours(day, open, close);

        Request req = new Request("SET_OPEN_HOURS", hours);
        ClientUI.chat.sendToServer(req);

        statusLabel.setText("Opening hours sent to server");
    }
    private int convertDayToInt(String day) {
        return switch (day) {
            case "Sunday" -> 1;
            case "Monday" -> 2;
            case "Tuesday" -> 3;
            case "Wednesday" -> 4;
            case "Thursday" -> 5;
            case "Friday" -> 6;
            case "Saturday" -> 7;
            default -> 0; // Holiday / Special Day
        };
    }
    @FXML
    private void onLogout() {
        System.out.println("Logout clicked");

        // כאן אפשר:
        // 1. לחזור למסך login
        // 2. לסגור את החלון
        // 3. לשלוח בקשת logout לשרת (אם יש)

        // דוגמה – סגירת החלון:
        Stage stage = (Stage) dayBox.getScene().getWindow();
        stage.close();
    }



}
