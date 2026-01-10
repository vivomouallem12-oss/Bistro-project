package gui;

import javafx.fxml.FXML;
import javafx.scene.chart.*;
import java.util.Map;
import client.ClientUI;
import logic.Request;


public class ReportsAnalysisController {

    public static ReportsAnalysisController activeController;

    @FXML
    private LineChart<String, Number> arrivalChart;

    @FXML
    private BarChart<String, Number> reservationChart;
    
    @FXML
    public void initialize() {
        activeController = this;

        ClientUI.chat.sendToServer(
            new Request("GET_REPORTS_DATA", null)
        );
    }

    

    public void setReportsData(
            Map<String, Map<String, Integer>> data) {

        arrivalChart.getData().clear();
        reservationChart.getData().clear();

        // ===== Arrival / Delays =====
        XYChart.Series<String, Number> arrivalSeries =
                new XYChart.Series<>();
        arrivalSeries.setName("Average Delay");

        if (data.get("arrival") != null) {
            data.get("arrival").forEach((label, value) ->
                    arrivalSeries.getData().add(
                            new XYChart.Data<>(label, value))
            );
        }

        arrivalChart.getData().add(arrivalSeries);

        // ===== Reservations =====
        XYChart.Series<String, Number> reservationsSeries =
                new XYChart.Series<>();
        reservationsSeries.setName("Reservations");

        if (data.get("reservations") != null) {
            data.get("reservations").forEach((label, value) ->
                    reservationsSeries.getData().add(
                            new XYChart.Data<>(label, value))
            );
        }

        // ===== Waiting List =====
        XYChart.Series<String, Number> waitingSeries =
                new XYChart.Series<>();
        waitingSeries.setName("Waiting List");

        if (data.get("waiting") != null) {
            data.get("waiting").forEach((label, value) ->
                    waitingSeries.getData().add(
                            new XYChart.Data<>(label, value))
            );
        }

        reservationChart.getData().addAll(
                reservationsSeries,
                waitingSeries
        );
    }
}
