/**
 * @file DockPane.java
 * @brief Class implementing a generic dock pane for the layout of dock nodes.
 *
 * @section License
 *
 *          This file is a part of the DockFX Library. Copyright (C) 2015 Robert B. Colton
 *
 *          This program is free software: you can redistribute it and/or modify it under the terms
 *          of the GNU Lesser General Public License as published by the Free Software Foundation,
 *          either version 3 of the License, or (at your option) any later version.
 *
 *          This program is distributed in the hope that it will be useful, but WITHOUT ANY
 *          WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 *          PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 *          You should have received a copy of the GNU Lesser General Public License along with this
 *          program. If not, see <http://www.gnu.org/licenses/>.
 **/

package dockfx;

import java.util.Stack;

import com.sun.javafx.css.StyleManager;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Popup;
import javafx.util.Duration;

/**
 * Base class for a dock pane that provides the layout of the dock nodes. Stacking the dock nodes to
 * the center in a TabPane will be added in a future release. For now the DockPane uses the relative
 * sizes of the dock nodes and lays them out in a tree of DockSplits.
 * 
 * @since DockFX 0.1
 */
public class DockPane extends StackPane implements EventHandler<DockEvent> {
  
	DoubleProperty dockWidth = new SimpleDoubleProperty(0);
	
	DoubleProperty dockHeight = new SimpleDoubleProperty(0);
	
	private DockSplit rootSplit;
	/**
   * The current root node of this dock pane's layout.
   */
  Boolean useInit = true;
  /**
   * Whether a DOCK_ENTER event has been received by this dock pane since the last DOCK_EXIT event
   * was received.
   */
  private boolean receivedEnter = false;

  /**
   * The current node in this dock pane that we may be dragging over.
   */
  private Node dockNodeDrag;
  /**
   * The docking area of the current dock indicator button if any is selected. This is either the
   * root or equal to dock node drag.
   */
  private Node dockAreaDrag;
  /**
   * The docking position of the current dock indicator button if any is selected.
   */
  private DockPos dockPosDrag;

  /**
   * The docking area shape with a dotted animated border on the indicator overlay popup.
   */
  private Rectangle dockAreaIndicator;
  /**
   * The timeline used to animate the borer of the docking area indicator shape. Because JavaFX has
   * no CSS styling for timelines/animations yet we will make this private and offer an accessor for
   * the user to programmatically modify the animation or disable it.
   */
  private Timeline dockAreaStrokeTimeline;
  /**
   * The popup used to display the root dock indicator buttons and the docking area indicator.
   */
  private Popup dockIndicatorOverlay;

  /**
   * The grid pane used to lay out the local dock indicator buttons. This is the grid used to lay
   * out the buttons in the circular indicator.
   */
  private GridPane dockPosIndicator;
  /**
   * The popup used to display the local dock indicator buttons. This allows these indicator buttons
   * to be displayed outside the window of this dock pane.
   */
  private Popup dockIndicatorPopup;

  /**
   * Base class for a dock indicator button that allows it to be displayed during a dock event and
   * continue to receive input.
   * 
   * @since DockFX 0.1
   */
  public class DockPosButton extends Button {
    /**
     * Whether this dock indicator button is used for docking a node relative to the root of the
     * dock pane.
     */
    private boolean dockRoot = true;
    /**
     * The docking position indicated by this button.
     */
    private DockPos dockPos = DockPos.CENTER;

    /**
     * Creates a new dock indicator button.
     */
    public DockPosButton(boolean dockRoot, DockPos dockPos) {
      super();
      this.dockRoot = dockRoot;
      this.dockPos = dockPos;
    }

    /**
     * Whether this dock indicator button is used for docking a node relative to the root of the
     * dock pane.
     * 
     * @param dockRoot Whether this indicator button is used for docking a node relative to the root
     *        of the dock pane.
     */
    public final void setDockRoot(boolean dockRoot) {
      this.dockRoot = dockRoot;
    }

    /**
     * The docking position indicated by this button.
     * 
     * @param dockPos The docking position indicated by this button.
     */
    public final void setDockPos(DockPos dockPos) {
      this.dockPos = dockPos;
    }

    /**
     * The docking position indicated by this button.
     * 
     * @return The docking position indicated by this button.
     */
    public final DockPos getDockPos() {
      return dockPos;
    }

    /**
     * Whether this dock indicator button is used for docking a node relative to the root of the
     * dock pane.
     * 
     * @return Whether this indicator button is used for docking a node relative to the root of the
     *         dock pane.
     */
    public final boolean isDockRoot() {
      return dockRoot;
    }
  }

  /**
   * A collection used to manage the indicator buttons and automate hit detection during DOCK_OVER
   * events.
   */
  private ObservableList<DockPosButton> dockPosButtons;


  /**
   * Creates a new DockPane adding event handlers for dock events and creating the indicator
   * overlays.
   */
  public DockPane() {
    super();
    
    this.widthProperty().addListener((v,oldV,newV) -> {
		setDockWidth((double)newV);
		useInit = false;
	});
	this.heightProperty().addListener((v,oldV,newV) -> {
		setDockHeight((double)newV);
	});
    rootSplit = new DockSplit();
    this.addEventHandler(DockEvent.ANY, this);
    this.addEventFilter(DockEvent.ANY, new EventHandler<DockEvent>() {

      @Override
      public void handle(DockEvent event) {

        if (event.getEventType() == DockEvent.DOCK_ENTER) {
          DockPane.this.receivedEnter = true;
        } else if (event.getEventType() == DockEvent.DOCK_OVER) {
          DockPane.this.dockNodeDrag = null;
        }
      }

    });

    dockIndicatorPopup = new Popup();
    dockIndicatorPopup.setAutoFix(false);

    dockIndicatorOverlay = new Popup();
    dockIndicatorOverlay.setAutoFix(false);

    StackPane dockRootPane = new StackPane();
    dockRootPane.prefWidthProperty().bind(this.widthProperty());
    dockRootPane.prefHeightProperty().bind(this.heightProperty());

    dockAreaIndicator = new Rectangle();
    dockAreaIndicator.setManaged(false);
    dockAreaIndicator.setMouseTransparent(true);

    dockAreaStrokeTimeline = new Timeline();
    dockAreaStrokeTimeline.setCycleCount(Timeline.INDEFINITE);
    // 12 is the cumulative offset of the stroke dash array in the default.css style sheet
    // RFE filed for CSS styled timelines/animations:
    // https://bugs.openjdk.java.net/browse/JDK-8133837
    KeyValue kv = new KeyValue(dockAreaIndicator.strokeDashOffsetProperty(), 12);
    KeyFrame kf = new KeyFrame(Duration.millis(500), kv);
    dockAreaStrokeTimeline.getKeyFrames().add(kf);
    dockAreaStrokeTimeline.play();

    DockPosButton dockCenter = new DockPosButton(false, DockPos.CENTER);
    dockCenter.getStyleClass().add("dock-center");

    DockPosButton dockTop = new DockPosButton(false, DockPos.TOP);
    dockTop.getStyleClass().add("dock-top");
    DockPosButton dockRight = new DockPosButton(false, DockPos.RIGHT);
    dockRight.getStyleClass().add("dock-right");
    DockPosButton dockBottom = new DockPosButton(false, DockPos.BOTTOM);
    dockBottom.getStyleClass().add("dock-bottom");
    DockPosButton dockLeft = new DockPosButton(false, DockPos.LEFT);
    dockLeft.getStyleClass().add("dock-left");

    DockPosButton dockTopRoot = new DockPosButton(true, DockPos.TOP);
    StackPane.setAlignment(dockTopRoot, Pos.TOP_CENTER);
    dockTopRoot.getStyleClass().add("dock-top-root");

    DockPosButton dockRightRoot = new DockPosButton(true, DockPos.RIGHT);
    StackPane.setAlignment(dockRightRoot, Pos.CENTER_RIGHT);
    dockRightRoot.getStyleClass().add("dock-right-root");

    DockPosButton dockBottomRoot = new DockPosButton(true, DockPos.BOTTOM);
    StackPane.setAlignment(dockBottomRoot, Pos.BOTTOM_CENTER);
    dockBottomRoot.getStyleClass().add("dock-bottom-root");

    DockPosButton dockLeftRoot = new DockPosButton(true, DockPos.LEFT);
    StackPane.setAlignment(dockLeftRoot, Pos.CENTER_LEFT);
    dockLeftRoot.getStyleClass().add("dock-left-root");

    //dockCenter goes first when tabs are added in a future version
    dockPosButtons = FXCollections.observableArrayList(dockTop, dockRight, dockBottom, dockLeft,
        dockTopRoot, dockRightRoot, dockBottomRoot, dockLeftRoot);

    dockPosIndicator = new GridPane();
    dockPosIndicator.add(dockTop, 1, 0);
    dockPosIndicator.add(dockRight, 2, 1);
    dockPosIndicator.add(dockBottom, 1, 2);
    dockPosIndicator.add(dockLeft, 0, 1);
    // dockPosIndicator.add(dockCenter, 1, 1);

    dockRootPane.getChildren().addAll(dockAreaIndicator, dockTopRoot, dockRightRoot, dockBottomRoot,
        dockLeftRoot);

    dockIndicatorOverlay.getContent().add(dockRootPane);
    dockIndicatorPopup.getContent().addAll(dockPosIndicator);

    this.getStyleClass().add("dock-pane");
    dockRootPane.getStyleClass().add("dock-root-pane");
    dockPosIndicator.getStyleClass().add("dock-pos-indicator");
    dockAreaIndicator.getStyleClass().add("dock-area-indicator");
  }

  /**
   * The Timeline used to animate the docking area indicator in the dock indicator overlay for this
   * dock pane.
   * 
   * @return The Timeline used to animate the docking area indicator in the dock indicator overlay
   *         for this dock pane.
   */
  public final Timeline getDockAreaStrokeTimeline() {
    return dockAreaStrokeTimeline;
  }

  /**
   * Helper function to retrieve the URL of the default style sheet used by DockFX.
   * 
   * @return The URL of the default style sheet used by DockFX.
   */
  public final static String getDefaultUserAgentStyleheet() {
	  return DockPane.class.getResource("../css/Main.css").toExternalForm();
  }

  /**
   * Helper function to add the default style sheet of DockFX to the user agent style sheets.
   */
  public final static void initializeDefaultUserAgentStylesheet() {
    StyleManager.getInstance()
        .addUserAgentStylesheet(DockPane.class.getResource("../css/Main.css").toExternalForm());
  }

  /**
   * A cache of all dock node event handlers that we have created for tracking the current docking
   * area.
   */
  private ObservableMap<Node, DockNodeEventHandler> dockNodeEventFilters =
      FXCollections.observableHashMap();

  /**
   * A wrapper to the type parameterized generic EventHandler that allows us to remove it from its
   * listener when the dock node becomes detached. It is specifically used to monitor which dock
   * node in this dock pane's layout we are currently dragging over.
   * 
   * @since DockFX 0.1
   */
  private class DockNodeEventHandler implements EventHandler<DockEvent> {
    /**
     * The node associated with this event handler that reports to the encapsulating dock pane.
     */
    private Node node = null;

    /**
     * Creates a default dock node event handler that will help this dock pane track the current
     * docking area.
     * 
     * @param node The node that is to listen for docking events and report to the encapsulating
     *        docking pane.
     */
    public DockNodeEventHandler(Node node) {
      this.node = node;
    }

    @Override
    public void handle(DockEvent event) {
      DockPane.this.dockNodeDrag = node;
    }
  }

  /**
   * Dock the node into this dock pane at the given docking position relative to the sibling in the
   * layout. This is used to relatively position the dock nodes to other nodes given their preferred
   * size.
   * 
   * @param node The node that is to be docked into this dock pane.
   * @param dockPos The docking position of the node relative to the sibling.
   * @param sibling The sibling of this node in the layout.
   */
  public void dock(Node node, DockPos dockPos, Node sibling) {
    DockNodeEventHandler dockNodeEventHandler = new DockNodeEventHandler(node);
    dockNodeEventFilters.put(node, dockNodeEventHandler);
    node.addEventFilter(DockEvent.DOCK_OVER, dockNodeEventHandler);

    DockSplit split = (DockSplit) rootSplit;
    if (split.getItems().size()==0) {
      split.getItems().add(node);
      rootSplit = split;
      this.getChildren().add(rootSplit);
      return;
    } 
    // find the parent of the sibling
    if (sibling != null && sibling != rootSplit) {

      Stack<Parent> stack = new Stack<Parent>();
      stack.push((Parent) rootSplit);
      while (!stack.isEmpty()) {
        Parent parent = stack.pop();
        ObservableList<Node> children = parent.getChildrenUnmodifiable();

        if (parent instanceof DockSplit) {
          DockSplit splitPane = (DockSplit) parent;
          children = splitPane.getItems();
        }

        for (int i = 0; i < children.size(); i++) {
          if (children.get(i) == sibling) {
            split = (DockSplit) parent;
          } else if (children.get(i) instanceof Parent) {
            stack.push((Parent) children.get(i));
          }
        }
      }
    }

    Orientation requestedOrientation = (dockPos == DockPos.LEFT || dockPos == DockPos.RIGHT)
        ? Orientation.HORIZONTAL : Orientation.VERTICAL;

    // if the orientation is different then reparent the split pane
    if (split.getOrientation() != requestedOrientation) {
      if (split.getItems().size() > 1) {
        DockSplit splitPane = new DockSplit();
        if (split == rootSplit && sibling == rootSplit) {
          this.getChildren().set(this.getChildren().indexOf(rootSplit), splitPane);
          splitPane.getItems().add(split);
          rootSplit = splitPane;
          
          
          
        } else {
          split.getItems().set(split.getItems().indexOf(sibling), splitPane);
          splitPane.getItems().add(sibling);
          DockNode siblingD = (DockNode)sibling;
          splitPane.setMaxD(siblingD.getDockSize());
        }
        
        split = splitPane;
      }
      split.setOrientation(requestedOrientation);
    }
    ObservableList<Node> children = split.getItems();
    
    int relativeIndex = 0;
	if (dockPos == DockPos.RIGHT || dockPos == DockPos.BOTTOM){
		relativeIndex = children.size();
		if (sibling != null && sibling != rootSplit) {
			relativeIndex = children.indexOf(sibling) + 1;
		    }
		children.add(relativeIndex,node);

	} else {
		if (sibling != null && sibling != rootSplit) {
		      relativeIndex = children.indexOf(sibling);
		    }
		children.add(relativeIndex,node);
	}
    
	if (children.size()>1){
		AdjustDividers(rootSplit);
	}
    

  }
	


  /**
   * Dock the node into this dock pane at the given docking position relative to the root in the
   * layout. This is used to relatively position the dock nodes to other nodes given their preferred
   * size.
   * 
   * @param node The node that is to be docked into this dock pane.
   * @param dockPos The docking position of the node relative to the sibling.
   */
  public void dock(Node node, DockPos dockPos) {
    dock(node, dockPos, rootSplit);
  }

  /**
   * Detach the node from this dock pane removing it from the layout.
   * 
   * @param node The node that is to be removed from this dock pane.
   */
  public void undock(DockNode node) {
    DockNodeEventHandler dockNodeEventHandler = dockNodeEventFilters.get(node);
    node.removeEventFilter(DockEvent.DOCK_OVER, dockNodeEventHandler);
    dockNodeEventFilters.remove(node);
    // depth first search to find the parent of the node
    
    DockNode sibling=null;
    
    Stack<Parent> findStack = new Stack<Parent>();
    findStack.push((Parent) rootSplit);
    while (!findStack.isEmpty()) {
      Parent parent = findStack.pop();

      ObservableList<Node> children = parent.getChildrenUnmodifiable();

      if (parent instanceof DockSplit) {
        DockSplit split = (DockSplit) parent;
        if (parent.getParent() instanceof DockNode){
        	sibling = (DockNode)parent.getParent();
        }
        children = split.getItems();
      }
      for (int i = 0; i < children.size(); i++) {
        if (children.get(i) == node) {
          children.remove(i);

          removeUselessSplit(rootSplit);
          
          AdjustDividers(rootSplit);
          return;
        } else if (children.get(i) instanceof Parent) {
          findStack.push((Parent) children.get(i));
        }
      }
    }
  }	
  
  public void AdjustDividers(DockSplit s){
	s.setHovered(false);
	ObservableList<Node> children = s.getItems();

	for (Node child: children){
		if (child  instanceof DockNode){
			DockNode node = (DockNode)child;
			if (!node.getScaleable()){
				if (node.dockedPos == DockPos.LEFT || node.dockedPos == DockPos.RIGHT){
					node.setMaxWidth(node.getDockSize());
					node.setMinWidth(node.getDockSize());
					node.setPrefHeight(Double.MAX_VALUE);
				} else {
					node.setMaxHeight(node.getDockSize());
					node.setMinHeight(node.getDockSize());
					node.setPrefWidth(Double.MAX_VALUE);
				}
			}
		} else {
			DockSplit currSplit = (DockSplit)child;
			//currSplit.lock();
			AdjustDividers((DockSplit)child);
		} 
	}
	
  }
  void removeUselessSplit(DockSplit s){
	  DockNode toReturn = null;
	  ObservableList<Node> children = s.getItems();

	  for (int i=0;i<children.size();i++){
		  if (children.get(i)  instanceof DockSplit){
				DockSplit childS = (DockSplit)children.get(i);
				if (childS.getOrientation() == s.getOrientation()){
					ObservableList<Node> grandChildren = childS.getItems();
					children.remove(i);
					s.getItems().addAll(i,grandChildren);
					i--;
				}
				if (childS.getItems().size()<= 1){
					if (childS.getItems().size()==0){
						children.remove(i);
						return;
					} else {
						if (childS.getItems().get(0) instanceof DockNode){
							toReturn = (DockNode)childS.getItems().get(0);
							toReturn.setDockPos(((childS.getOrientation()==Orientation.HORIZONTAL)?DockPos.TOP:DockPos.LEFT));
							children.add(i,toReturn);	
						} else {
							children.add(i,childS.getItems().get(0));
						}
						children.remove(i+1);
						i--;
					}
				} else {
					
					removeUselessSplit(childS);
				}
			}
	  }
  }
  @Override
  public void handle(DockEvent event) {
    if (event.getEventType() == DockEvent.DOCK_ENTER) {
      if (!dockIndicatorOverlay.isShowing()) {
        Point2D topLeft = DockPane.this.localToScreen(0, 0);
        System.out.println(topLeft);
        dockIndicatorOverlay.show(DockPane.this, topLeft.getX(), topLeft.getY());
      }
    } else if (event.getEventType() == DockEvent.DOCK_OVER) {
      this.receivedEnter = false;
      Boolean isRoot = false;
      dockPosDrag = null;
      dockAreaDrag = dockNodeDrag;
      DockNode underDockNode = (DockNode)dockNodeDrag;
      DockNode overDockNode = (DockNode)event.getContents();
      
      for (DockPosButton dockIndicatorButton : dockPosButtons) {
        if (dockIndicatorButton
            .contains(dockIndicatorButton.screenToLocal(event.getScreenX(), event.getScreenY()))) {
          dockPosDrag = dockIndicatorButton.getDockPos();
          if (dockIndicatorButton.isDockRoot()) {
            dockAreaDrag = rootSplit;
            isRoot = true;
          }
          dockIndicatorButton.pseudoClassStateChanged(PseudoClass.getPseudoClass("focused"), true);
          break;
        } else {
          dockIndicatorButton.pseudoClassStateChanged(PseudoClass.getPseudoClass("focused"), false);
        }
      }

      if (dockPosDrag != null) {
        Point2D originToScene = dockAreaDrag.localToScene(-this.getLayoutX(),-this.getLayoutY());
        dockAreaIndicator.setVisible(true);
        double overDockSize = overDockNode.getDockSize();
        dockAreaIndicator.relocate(originToScene.getX(), originToScene.getY());
        Boolean underScaleable = underDockNode.getScaleable();
        if (isRoot)
        	underScaleable = true;
        
        if (underDockNode.dockedPos == DockPos.RIGHT || underDockNode.dockedPos == DockPos.LEFT){
	        if (dockPosDrag == DockPos.RIGHT) {
	          dockAreaIndicator.setTranslateX(dockAreaDrag.getLayoutBounds().getWidth()+((underScaleable)?-overDockSize:0));
	        } else if (dockPosDrag == DockPos.LEFT){
	        	dockAreaIndicator.setTranslateX(0+((underScaleable)?0:-overDockSize));
	        } else {
	          dockAreaIndicator.setTranslateX(0);
	        }
        } else {
	        if (dockPosDrag == DockPos.RIGHT) {
		          dockAreaIndicator.setTranslateX(dockAreaDrag.getLayoutBounds().getWidth()-overDockSize);
		        } else {
		          dockAreaIndicator.setTranslateX(0);
		        }
        }
        if (underDockNode.dockedPos == DockPos.TOP || underDockNode.dockedPos == DockPos.BOTTOM){
	        if (dockPosDrag == DockPos.BOTTOM) {
	          dockAreaIndicator.setTranslateY(dockAreaDrag.getLayoutBounds().getHeight()+((underScaleable)?-overDockSize:0));
	        } else if (dockPosDrag == DockPos.TOP) {
	          dockAreaIndicator.setTranslateY((underScaleable)?0:-overDockSize);
	        } else {
	        	dockAreaIndicator.setTranslateY(0);
	        }
        } else {
        	if (dockPosDrag == DockPos.BOTTOM) {
		          dockAreaIndicator.setTranslateY(dockAreaDrag.getLayoutBounds().getHeight()-overDockSize);
		        } else {
		          dockAreaIndicator.setTranslateY(0);
		        }
        }

        double maxY = rootSplit.localToScreen(rootSplit.getLayoutBounds()).getMaxY();
        double maxX = rootSplit.localToScreen(rootSplit.getLayoutBounds()).getMaxX();
        double  currY = dockAreaDrag.localToScreen(dockAreaDrag.getLayoutBounds()).getMinY(); 
        double currX = dockAreaDrag.localToScreen(dockAreaDrag.getLayoutBounds()).getMinX();
        
        dockAreaIndicator.setTranslateX(Math.min(currX+dockAreaIndicator.getTranslateX(),maxX-overDockSize)-currX);
        dockAreaIndicator.setTranslateY(Math.min(currY+dockAreaIndicator.getTranslateY(),maxY-overDockSize)-currY);
        
        if (dockPosDrag == DockPos.LEFT || dockPosDrag == DockPos.RIGHT) {
          dockAreaIndicator.setWidth(overDockSize);
        } else {
          dockAreaIndicator.setWidth(dockAreaDrag.getLayoutBounds().getWidth());
        }
        if (dockPosDrag == DockPos.TOP || dockPosDrag == DockPos.BOTTOM) {
          dockAreaIndicator.setHeight(overDockSize);
        } else {
          dockAreaIndicator.setHeight(dockAreaDrag.getLayoutBounds().getHeight());
        }
      } else {
        dockAreaIndicator.setVisible(false);
      }

      if (dockNodeDrag != null) {
        Point2D originToScreen = dockNodeDrag.localToScreen(0, 0);

        double posX = originToScreen.getX() + dockNodeDrag.getLayoutBounds().getWidth() / 2
            - dockPosIndicator.getWidth() / 2;
        double posY = originToScreen.getY() + dockNodeDrag.getLayoutBounds().getHeight() / 2
            - dockPosIndicator.getHeight() / 2;

        if (!underDockNode.canTakeDocks() || !overDockNode.getDockInsideOthers()){
        	if (dockIndicatorPopup.isShowing()){
        		dockIndicatorPopup.hide();
        	}
        } else {
	        if (!dockIndicatorPopup.isShowing()) {
	        	dockIndicatorPopup.show(DockPane.this, posX, posY);
	        } else {
	          dockIndicatorPopup.setX(posX);
	          dockIndicatorPopup.setY(posY);
	        }
        }
        // set visible after moving the popup
        dockPosIndicator.setVisible(true);
      } else {
        dockPosIndicator.setVisible(false);
      }
    }

    if (event.getEventType() == DockEvent.DOCK_RELEASED && event.getContents() != null) {
      if (dockPosDrag != null && dockIndicatorOverlay.isShowing()) {
        DockNode dockNode = (DockNode) event.getContents();
        dockNode.dock(this, dockPosDrag, dockAreaDrag);
      }
    }

    if ((event.getEventType() == DockEvent.DOCK_EXIT && !this.receivedEnter)
        || event.getEventType() == DockEvent.DOCK_RELEASED) {
      if (dockIndicatorPopup.isShowing()) {
        dockIndicatorPopup.hide();
      }
      if (dockIndicatorOverlay.isShowing()){
    	  dockIndicatorOverlay.hide();
      }
    }
  }
  public void setDockSize(double w, double h){
		setDockWidth(w);
		setDockHeight(h);
	}
	
	public double getDockWidth(){
		return dockWidth.get();
	}
	
	public void setDockWidth(double i){
		dockWidth.set(i);
	}
	
	public double getDockHeight(){
		return dockHeight.get();
	}
	
	public void setDockHeight(double i){
		dockHeight.set(i);
	}
  
  
}
