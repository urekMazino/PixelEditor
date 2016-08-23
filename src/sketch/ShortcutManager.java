package sketch;

import dockfx.DockEvent;
import dockfx.DockTitleBar;
import docks.SwingCanvas;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

public class ShortcutManager {
	
	Scene scene;
	
	KeyCodeCombination pencilSC = new KeyCodeCombination(KeyCode.A);
	
	KeyCodeCombination zoomSC = new KeyCodeCombination(KeyCode.Z);
	
	KeyCodeCombination clearSC = new KeyCodeCombination(KeyCode.SPACE);
	
	KeyCode zoomActivator = KeyCode.ALT;
	KeyCode horizontalActivator = KeyCode.SHIFT;
	
	static boolean zoomActivatorOn = false;
	static boolean  horizontalActivatorOn = false;
	
	ToolManager TM;
	SwingCanvas CC;
	
	public ShortcutManager(Scene scene){
		this.scene = scene;
		init(scene);
	}
	public void setToolManager(ToolManager TM){
		this.TM = TM;
	}
	public void setCurrentCanvas(SwingCanvas CC){
		this.CC = CC;
	}
	public SwingCanvas getCurrentCanvas(){
		return this.CC;
	}
	public static boolean getZoomEnabled(){
		return zoomActivatorOn;
	}
	public static boolean getHorizontalEnabled(){
		return horizontalActivatorOn;
	}
	private void init(Scene scene){
		scene.setOnKeyPressed((e)->{
			if (pencilSC.match(e)){
				System.out.println("PENCIL");
				TM.setTool("Pencil");
			}
			
			if (zoomSC.match(e)){
				System.out.println("ZOOM");
				TM.setTool("Zoom");
				
			}
			if (clearSC.match(e)){
				if (CC!=null){
					System.out.println("ZOOM");
					CC.getDrawArea().clear();
				}
			}
			if (e.getCode() == zoomActivator){
				zoomActivatorOn = true;
			} else if (e.getCode() == horizontalActivator){
				System.out.println("SHIFT");
				horizontalActivatorOn = true;
			} 
		});
		
		scene.setOnKeyReleased((e)->{
			if (e.getCode()==zoomActivator){
				zoomActivatorOn = false;
			}  else if (e.getCode() == horizontalActivator){
				horizontalActivatorOn = false;
			} 
		});
	}
}
