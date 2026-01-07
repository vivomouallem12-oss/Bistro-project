package client;

import ocsf.client.AbstractClient;
import common.ChatIF;
import gui.*;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import logic.*;

import java.io.IOException;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ChatClient extends AbstractClient {

    /* ================= INSTANCE VARIABLES ================= */
    private ChatIF clientUI;
    private static PaymentController activePaymentController;

    /* ================= CALLBACKS ================= */
    public static Consumer<List<Order>> ordersListCallback = null;
    public static Consumer<List<VisitHistory>> visitsListCallback = null;

    public ChatClient(String host, int port, ChatIF clientUI) throws IOException {
        super(host, port);
        this.clientUI = clientUI;
    }

    public static void setActivePaymentController(PaymentController ctrl) {
        activePaymentController = ctrl;
    }

    /* ================= SERVER → CLIENT ================= */
    @Override
    public void handleMessageFromServer(Object msg) {

        System.out.println("[SERVER → CLIENT] " + msg.getClass().getSimpleName());

        /* ================= RESPONSE ================= */
        if (msg instanceof Response r) {

            String status = r.getStatus();
            System.out.println("[RESPONSE] status=" + status);

            switch (status) {

                /* ---------- GUEST RESERVATION ---------- */
                case "FREE_TABLES_FOUND" -> Platform.runLater(() ->
                        ReservationController.getInstance()
                                .handleFreeTables((List<Tables>) r.getData())
                );

                case "FREE_TABLES_NOT_FOUND" -> Platform.runLater(() ->
                        ReservationController.getInstance()
                                .orderFail("No tables found at this time.",
                                        (List<LocalTime>) r.getData())
                );

                case "CODE_FOUND" -> Platform.runLater(() ->
                        ReservationController.getInstance()
                                .codeSuccess((OrderCustomer) r.getData())
                );

                case "CODE_NOT_FOUND" -> Platform.runLater(() ->
                        ReservationController.getInstance()
                                .codeStatus("Reservation code not found.", "red")
                );

                case "CANCEL_SUCCESS" -> Platform.runLater(() ->
                        ReservationController.getInstance()
                                .codeStatus("Order canceled.", "red")
                );

                case "CANCEL_FAILED" -> Platform.runLater(() ->
                        ReservationController.getInstance()
                                .codeStatus("Cancel failed.", "red")
                );

                /* ---------- PAYMENT ---------- */
                case "ORDER_PAYABLE" -> Platform.runLater(() -> openPaymentWindow());

                case "ORDER_NOT_PAYABLE" -> Platform.runLater(() ->
                        showError("Payment not allowed.\nStatus: " + r.getData())
                );

                case "PAYMENT_SUCCESS" -> Platform.runLater(() -> {
                    if (activePaymentController != null)
                        activePaymentController.showPaymentSuccess();
                });

                case "PAYMENT_FAILED" -> Platform.runLater(() -> {
                    if (activePaymentController != null)
                        activePaymentController.showPaymentFailed();
                });

                /* ---------- SUBSCRIBER RESERVATION ---------- */
                case "AVAILABLE" -> Platform.runLater(() -> {
                    SubscriberReservationController ctrl =
                            SubscriberReservationController.getActive();
                    if (ctrl != null) ctrl.sendCreateReservation();
                });

                case "NOT_AVAILABLE" -> Platform.runLater(() -> {
                    SubscriberReservationController ctrl =
                            SubscriberReservationController.getActive();
                    if (ctrl != null)
                        ctrl.showSuggestedTimes((List<String>) r.getData());
                });

                case "RESERVATION_CREATED" -> Platform.runLater(() -> {
                    int code = (int) ((Map<?, ?>) r.getData()).get("confirmationCode");
                    showInfo("Reservation confirmed ✅\nConfirmation code: " + code);
                });

                /* ---------- CHECK-IN ---------- */
                case "CHECKIN_SUCCESS" -> Platform.runLater(() -> {
                    SubscriberReservationController ctrl =
                            SubscriberReservationController.getActive();
                    if (ctrl != null) {
                        Map<?, ?> data = (Map<?, ?>) r.getData();
                        ctrl.handleCheckInSuccess(
                                (int) data.get("table_num"),
                                (int) data.get("confirmation_code")
                        );
                    }
                });

                case "NO_TABLE_AVAILABLE" -> Platform.runLater(() -> {
                    SubscriberReservationController ctrl =
                            SubscriberReservationController.getActive();
                    if (ctrl != null) ctrl.handleCheckInWait();
                });

                case "CHECKIN_CODE_NOT_FOUND" ->
                        Platform.runLater(() -> showError("Confirmation code not found."));

                case "CHECKIN_WRONG_DAY" ->
                        Platform.runLater(() -> showError("This reservation is not for today."));

                case "CHECKIN_TOO_EARLY" ->
                        Platform.runLater(() -> showError("You arrived too early."));

                case "CHECKIN_TOO_LATE" ->
                        Platform.runLater(() -> showError("You arrived too late."));

                case "CHECKIN_NOT_ALLOWED" ->
                        Platform.runLater(() -> showError("Check-in not allowed.\n" + r.getData()));

                /* ---------- WAITING LIST ---------- */
                case "WAITING_SEATED" -> Platform.runLater(() -> {
                    Map<?, ?> data = (Map<?, ?>) r.getData();
                    showInfo(
                            "🎉 Table is ready!\n\n" +
                            "Table Number: " + data.get("table_num") + "\n" +
                            "Confirmation Code: " + data.get("confirmationCode")
                    );
                });

                case "WAITING_JOINED" -> Platform.runLater(() -> {
                    int code = (int) ((Map<?, ?>) r.getData()).get("confirmationCode");
                    showInfo("⏳ Joined waiting list.\nConfirmation Code: " + code);
                });

                case "WAITING_ALREADY_EXISTS" -> Platform.runLater(() -> {
                    int code = (int) ((Map<?, ?>) r.getData()).get("confirmationCode");
                    showInfo("ℹ Already in waiting list.\nCode: " + code);
                });

                case "WAITING_NOT_ALLOWED" ->
                        Platform.runLater(() -> showError((String) r.getData()));

                case "WAITING_LEFT" ->
                        Platform.runLater(() -> showInfo("✅ Left waiting list."));

                case "WAITING_NOT_FOUND" ->
                        Platform.runLater(() -> showError("Waiting entry not found."));

                case "WAITING_JOIN_FAILED" ->
                        Platform.runLater(() -> showError("Failed to join waiting list."));

                /* ---------- ACCOUNT HISTORY ---------- */
                        /* ---------- VISIT HISTORY ---------- */
                case "ACCOUNT_VISITS" -> Platform.runLater(() -> {
                    AccountHistoryController ctrl = AccountHistoryController.getActive();
                    if (ctrl != null)
                        ctrl.handleVisitHistory((List<Order>) r.getData());
                });

                /* ---------- RESERVATION HISTORY ---------- */
                case "ACCOUNT_RESERVATIONS" -> Platform.runLater(() -> {
                    AccountHistoryController ctrl = AccountHistoryController.getActive();
                    if (ctrl != null)
                        ctrl.handleReservationHistory((List<Order>) r.getData());
                });


                default -> System.out.println("[Client] Unhandled Response: " + status);
            }
            return;
        }

        /* ================= LOGIN ================= */
        if (msg instanceof Subscriber sub) {
            Platform.runLater(() -> loadSubscriberMain(sub));
            return;
        }

        /* ================= ERROR ================= */
        if ("SERVER_ERROR".equals(msg)) {
            Platform.runLater(() ->
                    showError("Server error. Please try again later."));
        }
    }

    /* ================= HELPERS ================= */

    private void openPaymentWindow() {
        try {
            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/gui/Payment.fxml"));
            Parent root = loader.load();
            activePaymentController = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Payment");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadSubscriberMain(Subscriber sub) {
        try {
            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/gui/SubscriberMain.fxml"));
            Parent root = loader.load();

            SubscriberMainController ctrl = loader.getController();
            ctrl.setClient(ClientUI.chat);
            ctrl.setSubscriber(sub);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();

            Stage.getWindows().forEach(w -> {
                if (w instanceof Stage s && s != stage) s.close();
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleMessageFromClientUI(Object message) {
        try {
            openConnection();
            sendToServer(message);
        } catch (IOException e) {
            e.printStackTrace();
            quit();
        }
    }

    public void quit() {
        try { closeConnection(); } catch (IOException ignored) {}
        System.exit(0);
    }

    private static void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }

    private static void showInfo(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).showAndWait();
    }
}
