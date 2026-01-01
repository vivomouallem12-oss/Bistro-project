package client;

import java.io.IOException;
import common.ChatIF;

public class ClientController implements ChatIF {

    public static int DEFAULT_PORT;

    private ChatClient client;

    // ---------------- CONSTRUCTOR ----------------
    public ClientController(String host, int port) {
        try {
            client = new ChatClient(host, port, this);
        } catch (IOException e) {
            System.out.println("Error: Can't setup connection! Terminating client.");
            System.exit(1);
        }
    }

    // ---------------- SEND MESSAGE ----------------
    public void sendToServer(Object msg) {
        try {
            client.sendToServer(msg);
        } catch (IOException e) {
            System.out.println("Failed sending message to server.");
            e.printStackTrace();
        }
    }

    // ---------------- DISCONNECT ----------------
    public void disconnect() {
        try {
            if (client != null && client.isConnected()) {
                client.closeConnection();
                System.out.println("Client disconnected from server.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openConnection() throws Exception {
        client.openConnection();
    }

    public void closeConnection() throws Exception {
        client.closeConnection();
    }

    // ---------------- CHAT INTERFACE ----------------
    @Override
    public void display(String message) {
        System.out.println("> " + message);
    }

    // Console UI (if needed)
    public void accept(String str) {
        client.handleMessageFromClientUI(str);
    }
}
