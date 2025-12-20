package Server;

import java.io.IOException;
import java.sql.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import logic.Order;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;

public class EchoServer extends AbstractServer {

    // Store client IP safely so we can print it even after disconnect
    private final Map<ConnectionToClient, String> clientIPs = new ConcurrentHashMap<>();
    private final Map<ConnectionToClient, String> clientHosts = new ConcurrentHashMap<>();

    public EchoServer(int port) {
        super(port);
    }

    // ================= HANDLE CLIENT MESSAGE =================
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

            // ---------- STRING ----------
            if (msg instanceof String) {
                String s = (String) msg;

                if ("getOrders".equals(s)) {

                    String sql = "SELECT * FROM `order`";
                    try (Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery(sql)) {

                        while (rs.next()) {
                            Order o = new Order(
                                    rs.getInt("order_number"),
                                    rs.getString("order_date"),
                                    rs.getInt("number_of_guests"),
                                    rs.getInt("confirmation_code"),
                                    rs.getInt("subscriber_id"),
                                    rs.getString("date_of_placing_order")
                            );

                            client.sendToClient(o);
                            System.out.println("[Server → Client] Sent order: " + o);
                        }
                    }

                } else {
                    // maybe string is an order number
                    try {
                        int orderNum = Integer.parseInt(s);
                        fetchSingleOrder(conn, orderNum, client);
                    } catch (NumberFormatException e) {
                        System.out.println("[Warning] Unknown string command: " + s);
                    }
                }

            // ---------- INTEGER ----------
            } else if (msg instanceof Integer) {
                fetchSingleOrder(conn, (Integer) msg, client);

            // ---------- ORDER UPDATE ----------
            } else if (msg instanceof Order) {
                Order o = (Order) msg;

                String sql =
                        "UPDATE `order` SET order_date=?, number_of_guests=? WHERE order_number=?";

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, o.getOrder_date());
                    ps.setInt(2, o.getNumber_of_guests());
                    ps.setInt(3, o.getOrder_number());

                    int updated = ps.executeUpdate();

                    if (updated > 0) {
                        System.out.println("[DB] Order updated: " + o);
                        sendToAllClients("Order updated successfully!");
                    } else {
                        System.out.println("[DB] Order not found: " + o.getOrder_number());
                        sendToAllClients("Order not found!");
                    }
                }

            } else {
                System.out.println("[Warning] Unknown message type: " + msg);
            }

        } catch (SQLException | IOException ex) {
            System.out.println("[ERROR] " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            if (pool != null && conn != null) {
                pool.releaseConnection(conn);
                System.out.println("[Pool] Connection returned to pool");
            }
        }
    }

    // ================= FETCH SINGLE ORDER =================
    private void fetchSingleOrder(Connection conn, int orderNum, ConnectionToClient client) throws IOException {

        String sql = "SELECT * FROM `order` WHERE order_number = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderNum);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    Order o = new Order(
                            rs.getInt("order_number"),
                            rs.getString("order_date"),
                            rs.getInt("number_of_guests"),
                            rs.getInt("confirmation_code"),
                            rs.getInt("subscriber_id"),
                            rs.getString("date_of_placing_order")
                    );

                    client.sendToClient(o);
                    System.out.println("[Server → Client] Sent single order: " + o);
                } else {
                    client.sendToClient(new Order(-1, null, 0, 0, 0, null));
                    System.out.println("[DB] Requested order not found: " + orderNum);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ================= SERVER LIFECYCLE =================
    @Override
    protected void serverStarted() {
        System.out.println("[Server] Listening on port " + getPort());

        // OCSF built-in watchdog timeout
        setTimeout(5000); // checks clients every 5 seconds

        try {
            MySQLConnection.getInstance();
            MySQLConnectionPool.getInstance();
            System.out.println("[Pool] Initialized and ready");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void serverStopped() {
        System.out.println("[Server] Stopped listening for connections.");
    }

    // ================= CLIENT CONNECT =================
    @Override
    protected void clientConnected(ConnectionToClient client) {
        try {
            String ip = client.getInetAddress().getHostAddress();
            String host = client.getInetAddress().getHostName();

            clientIPs.put(client, ip);
            clientHosts.put(client, host);

            System.out.println("[Client] Connected - IP: " + ip + ", Host: " + host);

        } catch (Throwable ignored) {}
    }

    // ================= NORMAL DISCONNECT =================
    @Override
    synchronized protected void clientDisconnected(ConnectionToClient client) {
        try {
            if (client == null) return;

            String ip = clientIPs.getOrDefault(client, "UNKNOWN");
            String host = clientHosts.getOrDefault(client, "UNKNOWN");

            System.out.println("[Client] Disconnected normally - IP: " + ip + ", Host: " + host);

            clientIPs.remove(client);
            clientHosts.remove(client);

        } catch (Throwable ignored) {}
    }

    // ================= CRASH / FORCE DISCONNECT =================
    @Override
    synchronized protected void clientException(ConnectionToClient client, Throwable exception) {

        try {
            if (client == null)
                return;

            String ip = clientIPs.getOrDefault(client, "UNKNOWN");
            String host = clientHosts.getOrDefault(client, "UNKNOWN");

            System.out.println("[Client] CRASHED / FORCED DISCONNECT - IP: " + ip + ", Host: " + host);

            // DO NOT call client.close()
            // OCSF will close automatically and then call clientDisconnected()

        } catch (Throwable ignored) {}
    }
}
