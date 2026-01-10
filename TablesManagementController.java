package gui;

import client.ClientUI;          // ✅ הוספה
import logic.Request;            // ✅ הוספה
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import logic.Tables;

public class TablesManagementController {

    public static TablesManagementController activeController;

    @FXML
    private TableView<Tables> tablesTable;

    @FXML
    private TableColumn<Tables, Integer> tableIdCol;

    @FXML
    private TableColumn<Tables, Integer> capacityCol;

    @FXML
    private TextField capacityField;

    @FXML
    private Label statusLabel;

    private final ObservableList<Tables> tableList =
            FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        activeController = this;

        tableIdCol.setCellValueFactory(
                new PropertyValueFactory<>("table_num")
        );
        capacityCol.setCellValueFactory(
                new PropertyValueFactory<>("places")
        );

        tablesTable.setItems(tableList);

        // 🔥 בקשה לשרת
        ClientUI.chat.sendToServer(
                new Request("GET_TABLES", null)
        );
    }

    public void setTables(java.util.List<Tables> tables) {
        tableList.clear();
        tableList.addAll(tables);
    }

    @FXML
    private void addTable() {

        String text = capacityField.getText().trim();

        if (text.isEmpty()) {
            statusLabel.setText("Enter capacity");
            return;
        }

        int capacity;
        try {
            capacity = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            statusLabel.setText("Capacity must be a number");
            return;
        }

        ClientUI.chat.sendToServer(
                new Request("ADD_TABLE", capacity)
        );

        capacityField.clear();
        statusLabel.setText("Adding table...");
    }


    @FXML
    private void updateTable() {

        Tables selected = tablesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Please select a table");
            return;
        }

        int newCapacity;
        try {
            newCapacity = Integer.parseInt(capacityField.getText());
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid capacity");
            return;
        }

        selected.setPlaces(newCapacity);

        ClientUI.chat.sendToServer(
                new Request("UPDATE_TABLE", selected)
        );

        statusLabel.setText("Updating table...");
    }


    @FXML
    private void deleteTable() {

        Tables selected = tablesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Please select a table");
            return;
        }

        ClientUI.chat.sendToServer(
                new Request("DELETE_TABLE", selected.getTable_num())
        );

        statusLabel.setText("Deleting table...");
    }

}
