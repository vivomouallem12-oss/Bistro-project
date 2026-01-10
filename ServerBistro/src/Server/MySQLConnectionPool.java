package Server;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MySQLConnectionPool {

    private static MySQLConnectionPool instance;
    private final List<Connection> availableConnections = new ArrayList<>();
    private final List<Connection> usedConnections = new ArrayList<>();

    private static final int INITIAL_POOL_SIZE = 5;

    private MySQLConnectionPool() throws SQLException {
        for (int i = 0; i < INITIAL_POOL_SIZE; i++) {
            availableConnections.add(MySQLConnection.getInstance().getConnection());
        }
    }

    public static synchronized MySQLConnectionPool getInstance() throws SQLException {
        if (instance == null) {
            instance = new MySQLConnectionPool();
        }
        return instance;
    }

    public synchronized Connection getConnection() throws SQLException {
        if (availableConnections.isEmpty()) {
            // optional: create more or block
            availableConnections.add(MySQLConnection.getInstance().getConnection());
        }

        Connection conn = availableConnections.remove(0);
        usedConnections.add(conn);
        return conn;
    }

    public synchronized void releaseConnection(Connection conn) {
        usedConnections.remove(conn);
        availableConnections.add(conn);
    }
}
