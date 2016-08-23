package controllers;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import dockfx.DockNode;
import dockfx.DockPane;
import dockfx.DockPos;
import docks.SwingCanvas;
import docks.Toolbar;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import misc.NumberTextField;
import misc.Pair;
import sketch.DrawArea;
import sketch.ShortcutManager;
import sketch.ToolManager;
import src.MainClass;
public class MainController implements Initializable {

	@FXML
	HBox titleBarHB;
	@FXML
	MenuBar mainMenuBar;
	@FXML
	VBox borderPaneTopVB;
	@FXML
	BorderPane borderPane;
	
	Stage mainStage;
	ToolManager toolManager;
	public DockPane dockPane;
	DockNode tabNode;
	TabPane tabPane;
	Point2D dragStart;
	//restore info
	Rectangle2D restore;
	boolean maximized = false;
	Toolbar toolbar;
	DockNode bottomTools;
	DockNode topTools;
	DockNode toolboxNode;
	DockNode rightTools;
	ShortcutManager SCManager;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		toolManager = new ToolManager();

		borderPane.setMinSize(200, 200);
		dockPane = new DockPane();
		dockPane.setDockSize(MainClass.getWidth(),MainClass.getHeight()-27);
		tabPane = new TabPane();
		
		tabNode = new DockNode(tabPane,false);
		tabNode.titleProperty().set("TabNode");

		tabNode.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
		tabNode.setScaleable(true);
		tabNode.setTakeDocks(false);
		
		bottomTools = new DockNode(new VBox(), "bottom");
		bottomTools.setDockSize(100);
		
		
		rightTools = new DockNode(new VBox(),"right");
		rightTools.setDockSize(200);
		
		
		tabNode.dock(dockPane, DockPos.TOP); 


		bottomTools.dock(dockPane, DockPos.BOTTOM,tabNode);
		
		rightTools.dock(dockPane, DockPos.RIGHT);
		
		openToolbox();
		
		borderPane.setCenter(dockPane);
		DockPane.initializeDefaultUserAgentStylesheet();
		mainStage = MainClass.getMainStage();
		
		//ON WINDOW RENDER LISTENER
		/*
		mainStage.addEventHandler(WindowEvent.WINDOW_SHOWN, new EventHandler<WindowEvent>() {
		    @Override
		    public void handle(WindowEvent window) {
		        Platform.runLater(new Runnable() {
		            @Override
		            public void run() {
		                System.out.println("dafuq");
		            }
		        });
		    }
		});*/
		
	}
	
	public void enter(){
		TreeItem root = new TreeItem<>();
		root.setExpanded(true);
		
		recursive(dockPane,root);
		
		StackPane layout = new StackPane();
		TreeView<String> tree = new TreeView<String>(root);
		tree.getSelectionModel().selectedItemProperty().addListener((v,oldValue,newValue) -> {
			remove(dockPane,newValue.getValue());
		});

		layout.getChildren().add(tree);
		
		Scene scene = new Scene(layout,800,800);
		Stage stage = new Stage();
		stage.setScene(scene);
		stage.show();
	}
	public void recursive(Parent parent,TreeItem<String> treeParent){
		ObservableList<Node> children = parent.getChildrenUnmodifiable();
		for (Node n : children){
			String type = getType(n);
			if (type!="Unknown"){
				TreeItem<String> item = new TreeItem<>(type);
				item.setExpanded(true);

				treeParent.getChildren().add(item);
				if (n instanceof DockNode){
					DockNode dn = (DockNode)n;
					if (dn.getContents() instanceof TabPane){
						recursive((Parent)n,item);
					} 
				} else if (n instanceof SplitPane ) 
					recursive((Parent)n,item);
			} else {
				if (!(n instanceof DockNode))
					recursive((Parent)n,treeParent);
			}
			System.out.println(n);
		}
	}
	public void remove(Parent parent,String stringLook){
		ObservableList<Node> children = parent.getChildrenUnmodifiable();
		for (Node n : children){
			String type = getType(n);
			if (type.equals(stringLook)){
				SplitPane sp = (SplitPane)parent.getParent();
				sp.getItems().remove((DockNode)n);
			} 
			if (!(n instanceof DockNode))
				remove((Parent)n,stringLook);
			
		}	
	}
	public void openToolbox(){
		toolbar = new Toolbar(toolManager);
		toolboxNode= new DockNode(toolbar, "");
		toolbar.setDock(toolboxNode);
		toolboxNode.hideStateButton();
		toolboxNode.setDockSize(40);
		toolboxNode.dock(dockPane, DockPos.LEFT);
		toolboxNode.setTakeDocks(false);
		toolboxNode.setDockInsideOthers(false);
	}
	String getType(Node n){
		String s = "";
		if (n instanceof SplitPane){
			s = "SplitPane";
		} else if (n instanceof DockNode){
			s = "DockNode,";
			DockNode dn =  (DockNode)n;
			s += dn.getTitle();
		} else if (n instanceof TabPane){
			s = "TabPane,";
			TabPane tp = (TabPane)n;
			for (Tab t : tp.getTabs()){
				System.out.println(t.getContent());
			}
		} 
		else {
			s = "Unknown";
		}
		return s;
	}
	
	public void NewFile(){
		int w = 300, h = 300;
		
		Dialog dialog = PromptFile();
		
		Optional<Pair<Integer, Integer>> result = dialog.showAndWait();
		
		if (result.isPresent()){
			w = result.get().getT1();
			h = result.get().getT2();
		}
		
		
		SCManager.setCurrentCanvas(new SwingCanvas(tabPane,w,h,toolManager));
		
		/*Group group = new Group();
		group.prefHeight(800);
		group.prefWidth(800);
		group.setAutoSizeChildren(false);
		group.setStyle("-fx-background-color:red;");*/
		//DrawArea drawArea = new DrawArea(w,h);
		//SwingNode drawContainer = new SwingNode();
		
		
		
	}

	public void SetMainStage(Stage s){
		mainStage = s;
		SCManager = new ShortcutManager(s.getScene());
		SCManager.setToolManager(toolManager);
		/*
		tabPane.widthProperty().addListener((e)->{
			if (SCManager.getCurrentCanvas()!=null){
				SCManager.getCurrentCanvas().getDrawArea().resizedSpane();
			}
		});
		tabPane.heightProperty().addListener((e)->{
			if (SCManager.getCurrentCanvas()!=null){
				SCManager.getCurrentCanvas().getDrawArea().resizedSpane();
			}
		});*/
		tabPane.sceneProperty().addListener((p,o,n)->{
			if (SCManager.getCurrentCanvas()!=null){
				if (n==null){
					SCManager.getCurrentCanvas().removeSwingNode();
				}else{
					SCManager.getCurrentCanvas().addSwingNode();
				}
			}
		});
		

	}
	
	public Dialog PromptFile(){
		Dialog<Pair<Integer,Integer>> dialog = new Dialog<>();
		dialog.setTitle("Create New File");
		dialog.setHeaderText(null);

		ButtonType createBtn = new ButtonType("Create",ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(createBtn,ButtonType.CANCEL);
		
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));

		NumberTextField widthField = new NumberTextField();
		widthField.setText("300");
		widthField.setPromptText("Width");
		widthField.setMin(1);
		NumberTextField heightField = new NumberTextField();
		heightField.setPromptText("Height");
		heightField.setMin(1);
		heightField.setText("300");
		
		grid.add(new Label("Width:"), 0, 0);
		grid.add(widthField, 1, 0);
		grid.add(new Label("Height:"), 0, 1);
		grid.add(heightField, 1, 1);
		
		Node createBtnNode = dialog.getDialogPane().lookupButton(createBtn);
		
		widthField.textProperty().addListener((observable, oldValue, newValue) -> {
			createBtnNode.setDisable(newValue.trim().isEmpty());
		});
		heightField.textProperty().addListener((observable, oldValue, newValue) -> {
			createBtnNode.setDisable(newValue.trim().isEmpty());
		});
		
		dialog.getDialogPane().setContent(grid);
		
		Platform.runLater(() -> widthField.requestFocus());
		
		dialog.setResultConverter(dialogButton -> {
		    if (dialogButton == createBtn) {
		        return new Pair<>(widthField.getInt(), heightField.getInt());
		    }
		    return null;
		});
		
		
		return dialog;
	}
	
	/*
	 //FOR CUSTOM WINDOW
	public void initHandlers(){
		borderPaneTopVB.addEventHandler(MouseEvent.ANY, new EventHandler<MouseEvent>(){
				@Override
		public void handle(MouseEvent event){
			handleMouse(event);
		}});
		mainMenuBar.addEventHandler(MouseEvent.ANY, new EventHandler<MouseEvent>(){
		@Override
		public void handle(MouseEvent event){
			handleMouse(event);
		}});
	}
	public void handleMouse(MouseEvent event){
		if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
			if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
				MaxResApp();
			} else {
				dragStart = new Point2D(event.getX(), event.getY());
			}
		}  else if (event.getEventType() == MouseEvent.DRAG_DETECTED) {
			mainStage.setMaximized(false);

		} else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
		      if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
		            event.setDragDetect(false);
		            event.consume();
		            return;
		       }
		      mainStage.setX(event.getScreenX() - dragStart.getX());
		      mainStage.setY(event.getScreenY() - dragStart.getY());
		}
	}
	
	public void SetMainStage(Stage s){
		mainStage = s;
	}
	
	public void CloseApp(){
		Platform.exit();
	}
	public void MininimizeApp(){
		mainStage.setIconified(true);
	}
	public void  MaxResApp(){
		
		if (maximized == false){
		
		double sx = mainStage.getX();
		double sy = mainStage.getY();
		double smx = sx+mainStage.getWidth();
		double smy = sy+mainStage.getHeight();
		
		restore = new Rectangle2D(sx,sy,smx-sx,smy-sy);
		
		Screen largestInter=Screen.getPrimary();
		double largestArea = 0;
		for (Iterator<Screen> s = Screen.getScreens().iterator();s.hasNext();){
			Screen current = s.next();
			double x = current.getBounds().getMinX();
			double mx = current.getBounds().getMaxX();
			double y = current.getBounds().getMinY();
			double my = current.getBounds().getMaxY();
			double dx = Math.min(smx, mx) - Math.max(sx, x);
			double dy = Math.min(smy, my) - Math.max(sy, y);
			double area = dx*dy;
			if (largestArea<area){
				largestArea = area;
				largestInter =current;
			}
		}
		Rectangle2D bounds = largestInter.getVisualBounds();

		mainStage.setX(bounds.getMinX());
		mainStage.setY(bounds.getMinY());
		mainStage.setWidth(bounds.getWidth());
		mainStage.setHeight(bounds.getHeight());
		maximized = true;
		} else {
			mainStage.setX(restore.getMinX());
			mainStage.setY(restore.getMinY());
			mainStage.setWidth(restore.getWidth());
			mainStage.setHeight(restore.getHeight());
			maximized = false;
		}
		//mainStage.setMaximized(!mainStage.isMaximized());
	}
	 */
	
}


