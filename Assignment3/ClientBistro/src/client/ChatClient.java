// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

package client;
import ocsf.client.AbstractClient;
import ocsf.server.AbstractServer;

import common.ChatIF;
import gui.AllOrdersFrameController;
import javafx.application.Platform;
import logic.Order;
import logic.Subscriber;

import java.io.*;
import java.util.function.Consumer;

/**
 * This class overrides some of the methods defined in the abstract
 * superclass in order to give more functionality to the client.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;
 * @author Fran&ccedil;ois B&eacute;langer
 * @version July 2000
 */
public class ChatClient extends AbstractClient
{
  //Instance variables **********************************************
  
  /**
   * The interface type variable.  It allows the implementation of 
   * the display method in the client.
   */
  ChatIF clientUI; 
  public static Order o1 = new Order(0,null,0,0,0,null);
  public static boolean awaitResponse = false;
  public static Consumer<Order> orderCallback = null;

  //Constructors ****************************************************
  
  /**
   * Constructs an instance of the chat client.
   *
   * @param host The server to connect to.
   * @param port The port number to connect on.
   * @param clientUI The interface type variable.
   */
	 
  public ChatClient(String host, int port, ChatIF clientUI) 
    throws IOException 
  {
    super(host, port); //Call the superclass constructor
    this.clientUI = clientUI;
    //openConnection();
  }

  //Instance methods ************************************************
    
  /**
   * This method handles all data that comes in from the server.
   *
   * @param msg The message from the server.
   */

  @Override
  public void handleMessageFromServer(Object msg) {

      // ----------- STRING RESPONSES -----------
      if (msg instanceof String response) {
          System.out.println("[Client] Server: " + response);

          switch (response) {

              case "LOGIN_OK":
                  System.out.println("Subscriber login success!");

                  Platform.runLater(() -> {
                      try {
                          javafx.fxml.FXMLLoader loader =
                                  new javafx.fxml.FXMLLoader(getClass().getResource("/gui/SubscriberHome.fxml"));

                          javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
                          javafx.stage.Stage stage = new javafx.stage.Stage();
                          stage.setTitle("Subscriber Home");
                          stage.setScene(scene);
                          stage.show();
                      } catch (Exception e) {
                          e.printStackTrace();
                      }
                  });
                  break;

              case "LOGIN_FAIL":
                  System.out.println("Subscriber login failed.");
                  gui.SubscriberLoginController.showServerError(
                          "Invalid Subscriber ID or Confirmation Code"
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

      // ----------- ORDER OBJECTS -----------
      if (msg instanceof Order order) {

          if (orderCallback != null) {
              ChatClient.o1 = order;
              Platform.runLater(() -> orderCallback.accept(order));
          } else {
              AllOrdersFrameController.addOrder(order);
          }
      }
  }



  /**
   * This method handles all data coming from the UI            
   *
   * @param message The message from the UI.    
   */
  
  public void handleMessageFromClientUI(String message)  
  {
    try
    {
    	openConnection();//in order to send more than one message
       	awaitResponse = true;
    	sendToServer(message);
		// wait for response
		while (awaitResponse) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
    }
    catch(IOException e)
    {
    	e.printStackTrace();
      clientUI.display("Could not send message to server: Terminating client."+ e);
      quit();
    }
  }

  
  /**
   * This method terminates the client.
   */
  public void quit() {
	    try {
	        clientUI.display("Disconnecting from server...");
	        closeConnection(); // important!
	    } catch(IOException e) {
	        e.printStackTrace();
	    }
	    System.exit(0);
	}
}
//End of ChatClient class
