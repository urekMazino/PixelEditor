package src;
import controllers.MainController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import sketch.ShortcutManager;

public class MainClass  extends Application{
	static Stage mainStage;
	static int sceneWidth=1200;
	static int sceneHeight=800;
	
	public static void main(String[] args){
		launch(args);
	}
	@Override
	public void start(Stage primaryStage) throws Exception {
		try {
			mainStage = primaryStage;
			Application.setUserAgentStylesheet(Application.STYLESHEET_MODENA);
			FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxmls/Main.fxml"));
			Font.loadFont(getClass().getResourceAsStream("../fonts/untitled-font-1.ttf"), 14);
			Parent root = (Parent)loader.load();
			MainController controller = (MainController)loader.getController();
			
			Scene scene = new Scene(root,sceneWidth,sceneHeight);
			scene.getStylesheets().add(getClass().getResource("../css/Main.css").toExternalForm());
			
			
			primaryStage.setScene(scene);
			//primaryStage.initStyle(StageStyle.UNDECORATED);
			
			controller.SetMainStage(primaryStage);
			
			
			primaryStage.show();
			
			
				
			
		} catch(Exception e) {
			e.printStackTrace();
		}	
	}
	
	public static Stage getMainStage(){
		return mainStage;
	}
	
	public static int getWidth(){
		return sceneWidth;
	}
	public static int getHeight(){
		return sceneWidth;
	}
}
