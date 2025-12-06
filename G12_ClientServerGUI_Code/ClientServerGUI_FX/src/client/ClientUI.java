package client;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import gui.AllOrdersFrameController;


public class ClientUI extends Application {
	public static ClientController chat;
	 @Override
	    public void start(Stage primaryStage) throws Exception {
		 	chat = new ClientController("localhost",5555);
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/AllOrders.fxml"));
	        Parent root = loader.load();
	        AllOrdersFrameController controller = loader.getController();
	        controller.setClient(chat);

	        // Show scene
	        primaryStage.setTitle("All Orders");
	        primaryStage.setScene(new Scene(root));
	        primaryStage.show();
	       
	    }

	 public static void main(String[] args) {
		    launch(args);
		}
}
