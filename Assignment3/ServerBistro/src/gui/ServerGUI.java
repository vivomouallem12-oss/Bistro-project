package gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

import java.io.OutputStream;
import java.io.PrintStream;

public class ServerGUI {

    @FXML
    private TextArea console;

    @FXML
    public void initialize() {
        redirectConsoleToGUI();
        appendLog("Server GUI Loaded...\n");
    }

    public void appendLog(String msg) {
        Platform.runLater(() -> console.appendText(msg));
    }

    private void redirectConsoleToGUI() {

        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) {
                Platform.runLater(() ->
                        console.appendText(String.valueOf((char) b)));
            }
        };

        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(out, true));
    }
}
