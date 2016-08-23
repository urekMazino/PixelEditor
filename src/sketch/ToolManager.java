package sketch;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.paint.Color;

public class ToolManager {
	
	
    private final ObjectProperty<Color> foregroundColor = new SimpleObjectProperty<>(Color.BLACK);
	
    private final ObjectProperty<Color> backgroundColor = new SimpleObjectProperty<>(Color.WHITE);
    
	private String currentTool = "Pencil";
	
	private int toolSize = 1;
	
	public String getTool(){
		return currentTool;
	}
	public int getToolSize(){
		return toolSize;
	}
	public void setTool(String tool){
		currentTool = tool;
	}
	
	public ObjectProperty<Color> getForegroundColorProperty(){
		return foregroundColor;
	}
	
	public ObjectProperty<Color> getBackgroundColorProperty(){
		return backgroundColor;
	}
	
}
