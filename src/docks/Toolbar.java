package docks;

import dockfx.DockNode;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;
import misc.MyCustomColorPicker;
import sketch.ToolManager;

public class Toolbar extends VBox{
	
	Pane colorPickerBtn;
	ToolManager TM;
	DockNode container;
	
	public Toolbar(ToolManager TM){
		this.TM = TM;
		this.getStyleClass().add("toolbar");
		this.alignmentProperty().set(Pos.TOP_CENTER);
		this.spacingProperty().set(5);
		this.setFillWidth(false);
		
		colorPickerBtn = createColorPickerBtn();
		
		Label pencilBtn = new Label("a");
		pencilBtn.setPrefHeight(30);

		Label zoomBtn = new Label("b");
		zoomBtn.setPrefHeight(30);

		getChildren().add(pencilBtn);
		getChildren().add(zoomBtn);
		getChildren().add(colorPickerBtn);
		
		pencilBtn.setOnMouseClicked((e)->{
			TM.setTool("Pencil");
		});
		zoomBtn.setOnMouseClicked((e)->{
			TM.setTool("Zoom");
		});
		
	}
	
	private Pane createColorPickerBtn(){
		Pane container = new Pane();
		container.setPrefSize(32,32);
		
		ObjectProperty<Color> foregroundColorProperty = TM.getForegroundColorProperty();
		ObjectProperty<Color> backgroundColorProperty = TM.getBackgroundColorProperty();
		
		Pane foregroundColorContainer = new Pane();
		foregroundColorContainer.getStyleClass().add("color-sample-container");
		foregroundColorContainer.setPrefSize(22, 22);
		Rectangle foregroundColorSample = new Rectangle(18,18);
		foregroundColorSample.mouseTransparentProperty().set(true);
		foregroundColorSample.setLayoutX(2);
		foregroundColorSample.setLayoutY(2);
		foregroundColorSample.fillProperty().bind(foregroundColorProperty);
		foregroundColorContainer.getChildren().add(foregroundColorSample);
		
		foregroundColorContainer.setOnMouseClicked((e)->{
			openColorPicker(foregroundColorProperty);
		});
		
		Pane backgroundColorContainer = new Pane();
		backgroundColorContainer.getStyleClass().add("color-sample-container");
		backgroundColorContainer.setPrefSize(22, 22);
		backgroundColorContainer.setLayoutX(10);
		backgroundColorContainer.setLayoutY(10);
		Rectangle backgroundColorSample = new Rectangle(18,18);
		backgroundColorSample.setLayoutX(2);
		backgroundColorSample.setLayoutY(2);
		backgroundColorSample.fillProperty().bind(backgroundColorProperty);
		backgroundColorContainer.getChildren().add(backgroundColorSample);
		
		backgroundColorContainer.setOnMouseClicked((e)->{
			openColorPicker(backgroundColorProperty);
		});
		
		container.getChildren().addAll(backgroundColorContainer,foregroundColorContainer);
		
		return container;
	}
	public void setDock(DockNode dn){
		container = dn;
	}
	private void openColorPicker(ObjectProperty<Color> currentColorProperty){
		MyCustomColorPicker myCustomColorPicker = new MyCustomColorPicker();
        myCustomColorPicker.setCurrentColor(currentColorProperty.get());
       
        CustomMenuItem itemColor = new CustomMenuItem(myCustomColorPicker);
        itemColor.setHideOnClick(false);

        currentColorProperty.bind(myCustomColorPicker.customColorProperty());
        ContextMenu contextMenu = new ContextMenu(itemColor);
        contextMenu.getStyleClass().add("context-menu-color-picker");
        contextMenu.setOnHiding(t->currentColorProperty.unbind());
        //Bounds bounds = localToScreen(getBoundsInLocal());
        Bounds bounds = container.localToScreen(container.getBoundsInLocal());
        contextMenu.show(Toolbar.this.getScene().getWindow(),bounds.getMaxX()+4,bounds.getMinY()+2);
	}
	
	
}
