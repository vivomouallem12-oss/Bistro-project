package Server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLConnection {

    private static MySQLConnection instance;

    private MySQLConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // load MySQL driver once
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static synchronized MySQLConnection getInstance() {
        if (instance == null) {
            instance = new MySQLConnection();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
    	return DriverManager.getConnection(
    		    "jdbc:mysql://localhost:3306/project?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=Asia/Jerusalem",
    		    "root",
    		    "A212534382b"   
    		);

    }
}
