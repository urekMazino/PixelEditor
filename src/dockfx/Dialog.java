package dockfx;

import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

public class Dialog extends DockNode{
	
	private int defWidth = 500;
	
	private int defHeight = 300;
	
	public Dialog(Node contents) {
		this(contents,"");
		// TODO Auto-generated constructor stub
	}
	
	public Dialog(Node contents,String title){
		super(contents,false);
		this.setTitle(title);
	}

	public void Show(Window s){
		  Show(s,defWidth,defHeight);
	 }
	  public void Show(Window s,double width,double height){
		  this.setWidth(width);
		  this.setHeight(height);
		  double halfStageX = (s.getWidth()/2)+s.getX();
		  double halfStageY = (s.getHeight()/2)+s.getY();
		  Point2D centerLC = new Point2D(halfStageX-(width/2),halfStageY-(height/2));
		  //Point2D centerLC = new Point2D(0,0);
		  Show(s,centerLC);
	  }
	  
	  private void Show(Window s,Point2D leftCorner){
		  if (!this.isFloating()) {
			  setDockable(false);
			  
		      dockTitleBar.setVisible(this.isCustomTitleBar());
		      dockTitleBar.setManaged(this.isCustomTitleBar());
		      
		      stage = new Stage();
		      stage.titleProperty().bind(titleProperty);
		      
		      stage.initOwner(s);

		      stage.initStyle(stageStyle);
		      
		      borderPane = new BorderPane();
		      borderPane.getStyleClass().add("dock-node-border");
		      borderPane.setCenter(this);

		      Scene scene = new Scene(borderPane);
		      
		      this.floatingProperty.set(true);
		      this.applyCss();
		      borderPane.applyCss();
		      Insets insetsDelta = borderPane.getInsets();

		      double insetsWidth = insetsDelta.getLeft() + insetsDelta.getRight();
		      double insetsHeight = insetsDelta.getTop() + insetsDelta.getBottom();

		      stage.setX(leftCorner.getX() - insetsDelta.getLeft());
		      stage.setY(leftCorner.getY() - insetsDelta.getTop());

		      stage.setMinWidth(borderPane.minWidth(this.getHeight()) + insetsWidth);
		      stage.setMinHeight(borderPane.minHeight(this.getWidth()) + insetsHeight);

		      borderPane.setPrefSize(this.getWidth() + insetsWidth, this.getHeight() + insetsHeight);

		      stage.setScene(scene);

		      if (stageStyle == StageStyle.TRANSPARENT) {
		        scene.setFill(null);
		      }

		      stage.setResizable(this.isStageResizable());
		      if (this.isStageResizable()) {
		        stage.addEventFilter(MouseEvent.MOUSE_PRESSED, this);
		        stage.addEventFilter(MouseEvent.MOUSE_MOVED, this);
		        stage.addEventFilter(MouseEvent.MOUSE_DRAGGED, this);
		      }

		      // we want to set the client area size
		      // without this it subtracts the native border sizes from the scene
		      // size
		      stage.sizeToScene();

		      stage.show();
		  }
	  }
	
}
