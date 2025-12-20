package gui;

import java.net.URL;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import Server.ServerUI;

public class ServerPortFrameController  {
	
	String temp="";
	
	@FXML
	private Button btnExit = null;
	@FXML
	private Button btnDone = null;
	@FXML
	private Label lbllist;
	
	@FXML
	private TextField portxt;
	ObservableList<String> list;
	
	private String getport() {
		return portxt.getText();			
	}
	
	public void Done(ActionEvent event) throws Exception {
	    String p = getport();

	    if(p.trim().isEmpty()) {
	        System.out.println("You must enter a port number");
	    }
	    else {
	        // ===== FIRST load the GUI =====
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/ServerGUI.fxml"));
	        Parent root = loader.load();

	        Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
	        stage.setScene(new Scene(root));
	        stage.setTitle("Server GUI");
	        stage.show();

	        // we MUST get controller to activate redirect
	        ServerGUI controller = loader.getController();

	        // ===== NOW START SERVER =====
	        ServerUI.runServer(p);
	    }
	}



	public void start(Stage primaryStage) throws Exception {	
		Parent root = FXMLLoader.load(getClass().getResource("/gui/ServerPort.fxml"));
				
		Scene scene = new Scene(root);
		primaryStage.setTitle("Server");
		primaryStage.setScene(scene);
		
		primaryStage.show();		
	}
	
	public void getExitBtn(ActionEvent event) throws Exception {
		System.out.println("exit Academic Tool");
		System.exit(0);			
	}

}