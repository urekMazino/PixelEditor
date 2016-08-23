package misc;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.*;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Polygon;
import javafx.util.converter.NumberStringConverter;

public class MyCustomColorPicker extends VBox {

    private final ObjectProperty<Color> currentColorProperty = 
        new SimpleObjectProperty<>(Color.WHITE);
    private final ObjectProperty<Color> customColorProperty = 
        new SimpleObjectProperty<>(Color.TRANSPARENT);

    private Pane colorRect;
    private final Pane colorBar;
    private final Pane colorRectOverlayOne;
    private final Pane colorRectOverlayTwo;
    private Region colorRectIndicator;
    private final Polygon colorBarIndicator;
    private Pane newColorRect;
    private Pane oldColorRect;
    
    private NumberTextField rInput =  new NumberTextField();;
    private NumberTextField gInput =  new NumberTextField();;
    private NumberTextField bInput =  new NumberTextField();;
    private NumberTextField HInput =  new NumberTextField();;
    private NumberTextField SInput =  new NumberTextField();;
    private NumberTextField BInput =  new NumberTextField();;
    
    final StringProperty rtext = new SimpleStringProperty(rInput.getText());
    final StringProperty gtext = new SimpleStringProperty(gInput.getText());
    final StringProperty btext = new SimpleStringProperty(bInput.getText());
    
    private DoubleProperty hue = new SimpleDoubleProperty(-1);
    private DoubleProperty sat = new SimpleDoubleProperty(-1);
    private DoubleProperty bright = new SimpleDoubleProperty(-1);
    private DoubleProperty blue = new SimpleDoubleProperty(0);
    private DoubleProperty red = new SimpleDoubleProperty(0);
    private DoubleProperty green = new SimpleDoubleProperty(0);

    private DoubleProperty alpha = new SimpleDoubleProperty(100) {
        @Override protected void invalidated() {
            setCustomColor(new Color(getCustomColor().getRed(), getCustomColor().getGreen(), 
                    getCustomColor().getBlue(), clamp(alpha.get() / 100)));
        }
    };

    public MyCustomColorPicker() {
    	
    	getStyleClass().add("my-custom-color");

        VBox box = new VBox();

        box.getStyleClass().add("color-rect-pane");
        customColorProperty().addListener((ov, t, t1) -> colorChanged());

        colorRectIndicator = new Region();
        colorRectIndicator.setId("color-rect-indicator");
        colorRectIndicator.setManaged(false);
        colorRectIndicator.setMouseTransparent(true);
        colorRectIndicator.setCache(true);

        final Pane colorRectOpacityContainer = new StackPane();

        colorRect = new StackPane();
        colorRect.getStyleClass().addAll("color-rect", "transparent-pattern");

        Pane colorRectHue = new Pane();
        colorRectHue.backgroundProperty().bind(new ObjectBinding<Background>() {
            {
                bind(hue);
            }
            @Override protected Background computeValue() {
                return new Background(new BackgroundFill(
                        Color.hsb(hue.getValue(), 1.0, 1.0), 
                        CornerRadii.EMPTY, Insets.EMPTY));
            }
        });            

        colorRectOverlayOne = new Pane();
        colorRectOverlayOne.getStyleClass().add("color-rect");
        colorRectOverlayOne.setBackground(new Background(new BackgroundFill(
        		new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, 
        	                new Stop(0, Color.rgb(255, 255, 255, 1)), 
        	                new Stop(1, Color.rgb(255, 255, 255, 0))), 
                CornerRadii.EMPTY, Insets.EMPTY)));

        EventHandler<MouseEvent> rectMouseHandler = event -> {
            final double x = event.getX();
            final double y = event.getY();
            sat.set(clamp(x / colorRect.getWidth()) * 100);
            bright.set(100 - (clamp(y / colorRect.getHeight()) * 100));
            updateHSBColor();
        };

        colorRectOverlayTwo = new Pane();
        colorRectOverlayTwo.getStyleClass().addAll("color-rect");
        colorRectOverlayTwo.setBackground(new Background(new BackgroundFill(
                new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, 
                new Stop(0, Color.rgb(0, 0, 0, 0)), new Stop(1, Color.rgb(0, 0, 0, 1))), 
                CornerRadii.EMPTY, Insets.EMPTY)));
        colorRectOverlayTwo.setOnMouseDragged(rectMouseHandler);
        colorRectOverlayTwo.setOnMousePressed(rectMouseHandler);

        Pane colorRectBlackBorder = new Pane();
        colorRectBlackBorder.setMouseTransparent(true);
        colorRectBlackBorder.getStyleClass().addAll("color-rect", "color-rect-border");

        colorBar = new Pane();
        colorBar.getStyleClass().add("color-bar");
        colorBar.setBackground(new Background(new BackgroundFill(createHueGradient(), 
                new CornerRadii(5.0,5.0,0.0,0.0,false), Insets.EMPTY)));

        colorBarIndicator = new Polygon();
        colorBarIndicator.getPoints().addAll(new Double[]{
        		0.0,25.0,
        		4.0,33.0,
        		-4.0,33.0});
        colorBarIndicator.setFill(Color.WHITE);
        colorBarIndicator.setId("color-bar-indicator");
        colorBarIndicator.setMouseTransparent(true);
        colorBarIndicator.setCache(true);
        DropShadow ds = new DropShadow();

        ds.setColor(Color.BLACK);
        colorBarIndicator.setEffect(ds);
        
        colorRectIndicator.layoutXProperty().bind(
            sat.divide(100).multiply(colorRect.widthProperty()));
        colorRectIndicator.layoutYProperty().bind(
            Bindings.subtract(1, bright.divide(100)).multiply(colorRect.heightProperty()));
        colorBarIndicator.layoutXProperty().bind(
            hue.divide(360).multiply(colorBar.widthProperty()));
        colorRectOpacityContainer.opacityProperty().bind(alpha.divide(100));

        EventHandler<MouseEvent> barMouseHandler = event -> {
            final double x = event.getX();
            hue.set(clamp(x / colorRect.getWidth()) * 360);
            updateHSBColor();
        };

        colorBar.setOnMouseDragged(barMouseHandler);
        colorBar.setOnMousePressed(barMouseHandler);

        // RIGHT SIDE PANE
        
        HBox colorPreviews = new HBox();
        //colorPreviews.setSpacing(10);;
        
        newColorRect = new Pane(new Label("new"));
        newColorRect.getStyleClass().add("color-new-rect");
        newColorRect.setId("new-color");
        newColorRect.backgroundProperty().bind(new ObjectBinding<Background>() {
            {
                bind(customColorProperty);
            }
            @Override protected Background computeValue() {
                return new Background(new BackgroundFill(customColorProperty.get(),  new CornerRadii(0.0,0.0,0.0,5.0,false), Insets.EMPTY));
            }
        });
        Label oldLabel = new Label("old");
        oldLabel.setId("fancytext");
        oldColorRect = new Pane(oldLabel);
        oldColorRect.getStyleClass().add("color-new-rect");
       
        
        colorPreviews.getChildren().addAll(newColorRect,oldColorRect);

        colorBar.getChildren().setAll(colorBarIndicator);
        colorRectOpacityContainer.getChildren().setAll(colorRectHue, colorRectOverlayOne, colorRectOverlayTwo);
        colorRect.getChildren().setAll(colorRectOpacityContainer, colorRectBlackBorder, colorRectIndicator);
        VBox.setVgrow(colorRect, Priority.SOMETIMES);
        box.getChildren().addAll(colorBar, colorRect,colorPreviews,makeInputs());
        //box2.getChildren().addAll(colorPreviews);
        getChildren().addAll(box);

        if (currentColorProperty.get() == null) {
            currentColorProperty.set(Color.TRANSPARENT);
        }
        updateValues();
        
    }
    private HBox makeInputs(){
    	HBox container = new HBox();
    	container.setSpacing(10);
    	container.getStyleClass().add("color-properties");
    	VBox containerRGB = new VBox();
    	VBox containerHSB = new VBox(); 
    	containerRGB.setPrefWidth(85);
    	containerHSB.setPrefWidth(85);
    	containerRGB.setFillWidth(true);
    	containerHSB.setFillWidth(true);
    	containerRGB.setSpacing(2);
    	containerHSB.setSpacing(2);
    	
    	HBox rCont = new HBox(new Label("R"));
    	HBox gCont = new HBox(new Label("G"));
    	HBox bCont = new HBox(new Label("B"));
    	
    	HBox HCont = new HBox(new Label("H"));
    	HBox SCont = new HBox(new Label("S"));
    	HBox BCont = new HBox(new Label("B"));
    	
    	rInput.setMin(0);
    	rInput.setMax(255);
    	rCont.getChildren().add(rInput);
    	rInput.textProperty().addListener((observable, oldValue, newValue) -> {
    		if (! newValue.equals(rtext.get())){
	    		red.set((double)rInput.getInt());
	    		green.set((double)gInput.getInt());
	    		blue.set((double)bInput.getInt());
	    		updateRGBColor();
    		}
    	});
    	
    	gInput.setMin(0);
    	gInput.setMax(255);
    	gCont.getChildren().add(gInput);
    	gInput.textProperty().addListener((observable, oldValue, newValue) -> {
    		if (! newValue.equals(gtext.get())){
	    		red.set((double)rInput.getInt());
	    		green.set((double)gInput.getInt());
	    		blue.set((double)bInput.getInt());
	    		updateRGBColor();
    		}
    	});
    	bInput.setMin(0);
    	bInput.setMax(255);
    	bCont.getChildren().add(bInput);
    	bInput.textProperty().addListener((observable, oldValue, newValue) -> {
    		if (! newValue.equals(btext.get())){
	    		red.set((double)rInput.getInt());
	    		green.set((double)gInput.getInt());
	    		blue.set((double)bInput.getInt());
	    		updateRGBColor();
    		}
    	});
    	HInput.textProperty().bindBidirectional(hue,new NumberStringConverter());
    	HInput.setMin(0);
    	HInput.setMax(360);
    	HCont.getChildren().add(HInput);
    	
    	SInput.textProperty().bindBidirectional(sat,new NumberStringConverter());
    	SInput.setMin(0);
    	SInput.setMax(100);
    	SCont.getChildren().add(SInput);
    	
    	BInput.textProperty().bindBidirectional(bright,new NumberStringConverter());
    	BInput.setMin(0);
    	BInput.setMax(100);
    	BCont.getChildren().add(BInput);
    	
    	HBox.setHgrow(rInput, Priority.ALWAYS);
    	HBox.setHgrow(gInput, Priority.ALWAYS);
    	HBox.setHgrow(bInput, Priority.ALWAYS);
    	HBox.setHgrow(HInput, Priority.ALWAYS);
    	HBox.setHgrow(SInput, Priority.ALWAYS);
    	HBox.setHgrow(BInput, Priority.ALWAYS);
    	
    	rtext.addListener((e,o,n)->{
	        rInput.setText(n);
    	});
    	gtext.addListener((e,o,n)->{
	        gInput.setText(n);
    	});
    	btext.addListener((e,o,n)->{
	        bInput.setText(n);
    	});
    	
    	containerRGB.getChildren().addAll(rCont,gCont,bCont);
    	containerHSB.getChildren().addAll(HCont,SCont,BCont);
    	container.getChildren().addAll(containerRGB,containerHSB);
    	return container;
    }
    private void updateValues() {
        sat.set(getCurrentColor().getSaturation()*100);
        bright.set(getCurrentColor().getBrightness()*100);
    	hue.set(getCurrentColor().getHue());
        
        alpha.set(getCurrentColor().getOpacity()*100);

        red.set(getCurrentColor().getRed()*255);
        blue.set(getCurrentColor().getBlue()*255);
        green.set(getCurrentColor().getGreen()*255);
                
        rtext.set(Integer.toString((int)red.get()));
        gtext.set(Integer.toString((int)green.get()));
        btext.set(Integer.toString((int)blue.get()));
        
        setCustomColor(Color.hsb(hue.get(), clamp(sat.get() / 100), 
                clamp(bright.get() / 100), clamp(alpha.get()/100)));
    }

    private void colorChanged() {
        
        sat.set(getCustomColor().getSaturation() * 100);
        bright.set(getCustomColor().getBrightness()*100);
        if  (sat.get()>0 && bright.get()>0){
        	hue.set(getCustomColor().getHue());
        }
        red.set(getCustomColor().getRed()*255);
        blue.set(getCustomColor().getBlue()*255);
        green.set(getCustomColor().getGreen()*255);
        
    }
    private void updateRGBColor(){
    	Color newColor=Color.BLACK;
    	try{
    		newColor = Color.rgb((int)red.get(),(int)green.get(),(int)blue.get());
    	} catch(Exception e){
    		
    	}
        sat.set(newColor.getSaturation() * 100);
        bright.set(newColor.getBrightness() * 100);
        if  (sat.get()>0 && bright.get()>0){
            hue.set(newColor.getHue());
        }

        
    	setCustomColor(newColor);
    }
    private void updateHSBColor() {
    	
    	//double h = (bright.get <= 0)?hue.get();
        Color newColor = Color.hsb(hue.get(), clamp(sat.get() / 100), 
                        clamp(bright.get() / 100), clamp(alpha.get() / 100));
        rtext.set(Integer.toString((int)(newColor.getRed()*255)));
        gtext.set(Integer.toString((int)(newColor.getGreen()*255)));
        btext.set(Integer.toString((int)(newColor.getBlue()*255)));
        setCustomColor(newColor);
    }

    @Override 
    protected void layoutChildren() {
        super.layoutChildren();            
        colorRectIndicator.autosize();
    }

    static double clamp(double value) {
        return value < 0 ? 0 : value > 1 ? 1 : value;
    }

    private static LinearGradient createHueGradient() {
        double offset;
        Stop[] stops = new Stop[255];
        for (int x = 0; x < 255; x++) {
            offset = (double)((1.0 / 255) * x);
            int h = (int)((x / 255.0) * 360);
            stops[x] = new Stop(offset, Color.hsb(h, 1.0, 1.0));
        }
        return new LinearGradient(0f, 0f, 1f, 0f, true, CycleMethod.NO_CYCLE, stops);
    }

    public void setCurrentColor(Color currentColor) {
        this.currentColorProperty.set(currentColor);
        this.oldColorRect.backgroundProperty().set(new Background(new BackgroundFill(currentColor,  new CornerRadii(0.0,0.0,5.0,0.0,false), Insets.EMPTY)));
        updateValues();
    }

    Color getCurrentColor() {
        return currentColorProperty.get();
    }

    public final ObjectProperty<Color> customColorProperty() {
        return customColorProperty;
    }

    void setCustomColor(Color color) {
        customColorProperty.set(color);
    }

    Color getCustomColor() {
        return customColorProperty.get();
    }
}