package dockfx;

import com.sun.glass.events.MouseEvent;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;

public class DockSplit extends SplitPane{
	
	BooleanProperty hovered = new SimpleBooleanProperty(false);
	
	double maxD = 0;
	
	public DockSplit(){
		this.setOnMouseMoved((e)->{
			if (hovered.getValue() == false){
				hovered.set(true);
				ReleaseDividers(this);
			}
		});

	}
	void ReleaseDividers(DockSplit s){
		this.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		this.setMinSize(0,0);
		ObservableList<Node> children = s.getItems();
		for (Node child: children){
			if (child  instanceof DockNode){
				DockNode node = (DockNode)child;
				if (node.dockedPos == DockPos.LEFT || node.dockedPos == DockPos.RIGHT){
					node.setMaxWidth(Double.MAX_VALUE);
					node.setMinWidth(0);
				} else {
					node.setMaxHeight(Double.MAX_VALUE);
					node.setMinHeight(0);
				}
			} else {
				ReleaseDividers((DockSplit)child);
			} 
		}
	}
	public void lock(){
		if (maxD <= 0){
			return;
		}
		if (this.getOrientation() == Orientation.HORIZONTAL){
			this.setMaxHeight(maxD);
			this.setMinHeight(maxD);
		} else {
			this.setMaxWidth(maxD);
			this.setMinWidth(maxD);
		}
	}
	public void setMaxD(double d){
		maxD = d;
	}
	public void setHovered(Boolean b){
		hovered.setValue(b);
	}
	public Boolean getHovered(){
		return hovered.getValue();
	}
}
