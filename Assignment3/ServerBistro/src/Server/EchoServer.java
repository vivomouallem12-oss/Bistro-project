package Server;

import java.io.IOException;
import java.sql.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import logic.Order;
import logic.Subscriber;
import logic.SubscriberLoginRequest;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;

public class EchoServer extends AbstractServer {

    private final Map<ConnectionToClient, String> clientIPs = new ConcurrentHashMap<>();
    private final Map<ConnectionToClient, String> clientHosts = new ConcurrentHashMap<>();

    public EchoServer(int port) {
        super(port);
    }

    @Override
    public void handleMessageFromClient(Object msg, ConnectionToClient client) {

        String ip = clientIPs.getOrDefault(client, "UNKNOWN");
        String host = clientHosts.getOrDefault(client, "UNKNOWN");
        System.out.println("[Client] Message from IP: " + ip + ", Host: " + host);

        MySQLConnectionPool pool = null;
        Connection conn = null;

        try {
            pool = MySQLConnectionPool.getInstance();
            conn = pool.getConnection();
            System.out.println("[DB] Obtained pooled connection");

            // ---------- STRING COMMAND ----------
            if (msg instanceof String s) {

                if (s.equals("getOrders")) {
                    sendAllOrders(conn, client);
                } else {
                    try {
                        int orderNum = Integer.parseInt(s);
                        fetchSingleOrder(conn, orderNum, client);
                    } catch (NumberFormatException e) {
                        System.out.println("[Warning] Unknown string command: " + s);
                    }
                }
            }

            // ---------- FETCH BY INTEGER ----------
            else if (msg instanceof Integer orderNum) {
                fetchSingleOrder(conn, orderNum, client);
            }

            // ---------- UPDATE ORDER ----------
            else if (msg instanceof Order o) {
                updateOrder(conn, o, client);
            }

            // ---------- SUBSCRIBER LOGIN ----------
            else if (msg instanceof SubscriberLoginRequest req) {
                handleSubscriberLogin(conn, req, client);
            }

            else {
                System.out.println("[Warning] Unknown message type: " + msg);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            try { client.sendToClient("SERVER_ERROR"); } catch (Exception ignored) {}
        }

        finally {
            if (pool != null && conn != null) {
                pool.releaseConnection(conn);
                System.out.println("[Pool] Connection returned");
            }
        }
    }

    // =========================================================
    //                   DB OPERATIONS
    // =========================================================
    private void sendAllOrders(Connection conn, ConnectionToClient client) throws Exception {

        String sql = "SELECT * FROM `order`";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {

                Integer subscriberId =
                        (rs.getObject("subscriber_id") == null)
                                ? null
                                : rs.getInt("subscriber_id");

                Order o = new Order(
                        rs.getInt("order_number"),
                        rs.getString("order_date"),
                        rs.getInt("number_of_guests"),
                        rs.getString("confirmation_code"),
                        subscriberId,
                        rs.getString("date_of_placing_order"),
                        "confirmed"
                );

                client.sendToClient(o);
                System.out.println("[Server → Client] Sent: " + o);
            }
        }
    }

    private void fetchSingleOrder(Connection conn, int orderNum, ConnectionToClient client) throws IOException {

        String sql = "SELECT * FROM `order` WHERE order_number = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderNum);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    Integer subscriberId =
                            (rs.getObject("subscriber_id") == null)
                                    ? null
                                    : rs.getInt("subscriber_id");

                    Order o = new Order(
                            rs.getInt("order_number"),
                            rs.getString("order_date"),
                            rs.getInt("number_of_guests"),
                            rs.getString("confirmation_code"),
                            subscriberId,
                            rs.getString("date_of_placing_order"),
                            "confirmed"
                    );

                    client.sendToClient(o);
                    System.out.println("[Server → Client] Sent single order");
                }
                else {
                    client.sendToClient("ORDER_NOT_FOUND");
                    System.out.println("[DB] Order not found: " + orderNum);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateOrder(Connection conn, Order o, ConnectionToClient client) throws Exception {

        String sql = "UPDATE `order` SET order_date=?, number_of_guests=? WHERE order_number=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, o.getOrderDate());
            ps.setInt(2, o.getNumberOfGuests());
            ps.setInt(3, o.getOrderNumber());

            int updated = ps.executeUpdate();

            if (updated > 0) {
                client.sendToClient("ORDER_UPDATED");
                System.out.println("[DB] Order updated: " + o);
            } else {
                client.sendToClient("ORDER_UPDATE_FAIL");
                System.out.println("[DB] Order not found: " + o.getOrderNumber());
            }
        }
    }

    // =========================================================
    //              SUBSCRIBER LOGIN (FIXED)
    // =========================================================
    private void handleSubscriberLogin(Connection conn, SubscriberLoginRequest req, ConnectionToClient client) throws Exception {

        System.out.println("[Login Attempt] Subscriber ID = " +
                req.getSubscriberId() +
                " Name = " +
                req.getSubscriberName());

        String sql =
        	    "SELECT * FROM subscriber " +
        	    "WHERE subscriber_id = ? " +
        	    "AND BINARY subscriber_name = ?";


        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Integer.parseInt(req.getSubscriberId()));
            ps.setString(2, req.getSubscriberName());

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    // ✅ Build Subscriber from REAL DB DATA
                	Subscriber sub = new Subscriber(
                	        rs.getInt("subscriber_id"),
                	        rs.getString("subscriber_name"),
                	        rs.getString("subscriber_email"),
                	        rs.getString("subscriber_phone")
                	);

                	System.out.println("=== SERVER SUBSCRIBER BUILT ===");
                	System.out.println("ID: " + sub.getSubscriberId());
                	System.out.println("USERNAME: " + sub.getUsername());


                    client.sendToClient(sub);
                    System.out.println("[Login] SUCCESS -> Subscriber object sent for " +
                            sub.getUsername() + " (ID: " + sub.getSubscriberId() + ")");
                } else {
                    client.sendToClient("LOGIN_FAIL");
                    System.out.println("[Login] FAIL");
                }
            }
        }
    }


    // =========================================================
    //                   SERVER EVENTS
    // =========================================================
    @Override
    protected void serverStarted() {
        System.out.println("[Server] Listening on port " + getPort());
        try {
            MySQLConnection.getInstance();
            MySQLConnectionPool.getInstance();
            System.out.println("[Pool] Ready");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void serverStopped() {
        System.out.println("[Server] Stopped.");
    }

    @Override
    protected void clientConnected(ConnectionToClient client) {
        try {
            String ip = client.getInetAddress().getHostAddress();
            String host = client.getInetAddress().getHostName();

            clientIPs.put(client, ip);
            clientHosts.put(client, host);

            System.out.println("[Client] Connected - " + ip);
        } catch (Throwable ignored) {}
    }

    @Override
    protected synchronized void clientDisconnected(ConnectionToClient client) {
        if (client == null) return;

        String ip = clientIPs.getOrDefault(client, "UNKNOWN");
        System.out.println("[Client] Disconnected - " + ip);

        clientIPs.remove(client);
        clientHosts.remove(client);
    }

    @Override
    protected synchronized void clientException(ConnectionToClient client, Throwable ex) {
        if (client == null) return;
        System.out.println("[Client] Crashed: " + ex.getMessage());
    }
}
