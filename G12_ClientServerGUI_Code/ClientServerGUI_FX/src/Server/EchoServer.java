// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 
package Server;

import java.io.*;
import logic.Order;
import logic.Subscriber;
import ocsf.server.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This class overrides some of the methods in the abstract 
 * superclass in order to give more functionality to the server.
 */

public class EchoServer extends AbstractServer
{
  // Constructors ****************************************************
  
  /**
   * Constructs an instance of the echo server.
   *
   * @param port The port number to connect on.
   */
  public EchoServer(int port) {
    super(port);
  }

  // Instance methods ************************************************
  
  /**
   * This method handles any messages received from the client.
   *
   * @param msg The message received from the client.
   * @param client The connection from which the message originated.
   */
  @Override
  public void handleMessageFromClient(Object msg, ConnectionToClient client) {
    String ip = client.getInetAddress().getHostAddress();
    String host = client.getInetAddress().getHostName();

    System.out.println("Message from client IP: " + ip + 
                       ", Host: " + host + 
                       " , Status: connected");

    try (Connection conn = MySQLConnection.getInstance().getConnection()) {
      System.out.println("SQL connection success");

      if ("getOrders".equals(msg)) {
        String sql = "SELECT * FROM `order`";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

          while (rs.next()) {
            Subscriber sub = new Subscriber(rs.getInt("subscriber_id"), null);
            Order o = new Order(
                    rs.getInt("order_number"),
                    rs.getString("order_date"),
                    rs.getInt("number_of_guests"),
                    rs.getInt("confirmation_code"),
                    sub.getSubscriber_id(),
                    rs.getString("date_of_placing_order")
            );
            System.out.println("Sending order to client: " + o);
            this.sendToAllClients(o);
          }
        }

      } else {
        int orderNum;
        try {
          orderNum = Integer.parseInt(msg.toString());
        } catch (NumberFormatException e) {
          System.out.println("Invalid order number from client: " + msg);
          Order o = new Order(-1, null, 0, 0, 0, null);
          this.sendToAllClients(o);
          return;
        }

        String sql = "SELECT * FROM `order` WHERE order_number = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
          stmt.setInt(1, orderNum);

          try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
              Subscriber sub = new Subscriber(rs.getInt("subscriber_id"), null);
              Order o = new Order(
                      rs.getInt("order_number"),
                      rs.getString("order_date"),
                      rs.getInt("number_of_guests"),
                      rs.getInt("confirmation_code"),
                      sub.getSubscriber_id(),
                      rs.getString("date_of_placing_order")
              );
              System.out.println("Order found: " + o);
              this.sendToAllClients(o);
            } else {
              Order o = new Order(-1, null, 0, 0, 0, null);
              System.out.println("Order not found for order_number: " + orderNum);
              this.sendToAllClients(o);
            }
          }
        }
      }

    } catch (SQLException ex) {
      System.out.println("SQLException: " + ex.getMessage());
      System.out.println("SQLState: " + ex.getSQLState());
      System.out.println("VendorError: " + ex.getErrorCode());
    }
  }

  /**
   * Called when the server starts listening for connections.
   */
  @Override
  protected void serverStarted() {
    MySQLConnection.getInstance();
    System.out.println("Server listening for connections on port " + getPort());
  }

  /**
   * Called when the server stops listening for connections.
   */
  @Override
  protected void serverStopped() {
    System.out.println("Server has stopped listening for connections.");
  }

  /**
   * Called when a client connects.
   */
  @Override
  protected void clientConnected(ConnectionToClient client) {
    String ip = client.getInetAddress().getHostAddress();
    String host = client.getInetAddress().getHostName();
    System.out.println("Client connected - IP: " + ip + ", Host: " + host + " , Status: connected");
  }

  /**
   * Called when a client disconnects.
   */
  @Override
  synchronized protected void clientDisconnected(ConnectionToClient client) {
    String ip = client.getInetAddress().getHostAddress();
    String host = client.getInetAddress().getHostName();
    System.out.println("Client disconnected - IP: " + ip + ", Host: " + host + " , Status: disconnected");
  }
}
// End of EchoServer class
