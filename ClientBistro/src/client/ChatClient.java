// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com

package client;

import ocsf.client.AbstractClient;
import common.ChatIF;
import gui.AllOrdersFrameController;
import gui.SubscriberMainController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import logic.Order;
import logic.Subscriber;

import java.io.IOException;
import java.util.function.Consumer;

public class ChatClient extends AbstractClient {

    // ================= INSTANCE VARIABLES =================
    ChatIF clientUI;

    public static Order o1 = new Order(0, null, 0, 0, 0, null);
    public static boolean awaitResponse = false;
    public static Consumer<Order> orderCallback = null;

    // ================== CONSTRUCTOR =======================
    public ChatClient(String host, int port, ChatIF clientUI) throws IOException {
        super(host, port);
        this.clientUI = clientUI;
    }

    // ================== SERVER MESSAGES ===================
    @Override
    public void handleMessageFromServer(Object msg) {

        // ---------- LOGIN SUCCESS → SUBSCRIBER OBJECT ----------
        if (msg instanceof Subscriber sub) {

            System.out.println("Login success → Subscriber: " + sub.getUsername());

            Platform.runLater(() -> {
                try {
                    FXMLLoader loader =
                            new FXMLLoader(getClass().getResource("/gui/SubscriberMain.fxml"));

                    Parent root = loader.load();

                    // Pass subscriber data
                    SubscriberMainController ctrl = loader.getController();
                    ctrl.initSubscriber(sub.getSubscriberId(), sub.getUsername());

                    // === OPEN SUBSCRIBER MAIN WINDOW ===
                    Stage newStage = new Stage();
                    newStage.setTitle("Subscriber Main");
                    newStage.setScene(new Scene(root));
                    newStage.show();

                    // === CLOSE ALL OTHER WINDOWS (Login + Role Selection) ===
                    javafx.stage.Window.getWindows().forEach(w -> {
                        if (w instanceof Stage s && s != newStage) {
                            s.close();
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            return;
        }

        // ---------------- STRING RESPONSES ----------------
        if (msg instanceof String response) {

            System.out.println("[Client] Server: " + response);

            switch (response) {

                case "LOGIN_FAIL":
                    System.out.println("Subscriber login failed.");
                    gui.SubscriberLoginController.showServerError(
                            "Invalid Subscriber ID or Name"
                    );
                    break;

                case "ORDER_UPDATED":
                    System.out.println("Order updated successfully.");
                    break;

                case "ORDER_UPDATE_FAIL":
                    System.out.println("Failed to update order.");
                    break;

                default:
                    clientUI.display(response);
            }

            return;
        }

        // ---------------- ORDER OBJECTS ----------------
        if (msg instanceof Order order) {

            if (orderCallback != null) {
                ChatClient.o1 = order;
                Platform.runLater(() -> orderCallback.accept(order));
            } else {
                AllOrdersFrameController.addOrder(order);
            }
        }
    }

    // ============= SEND MESSAGE TO SERVER =================
    public void handleMessageFromClientUI(String message) {
        try {
            openConnection();
            awaitResponse = true;
            sendToServer(message);

            while (awaitResponse) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {}
            }

        } catch (IOException e) {
            e.printStackTrace();
            clientUI.display("Could not send message to server: Terminating client." + e);
            quit();
        }
    }

    // ==================== QUIT CLIENT =====================
    public void quit() {
        try {
            clientUI.display("Disconnecting from server...");
            closeConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.exit(0);
    }
}
