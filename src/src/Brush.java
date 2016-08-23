package src;

import javafx.scene.canvas.Canvas;

public abstract class Brush {
	
	public abstract void onPress(Canvas c);
	
	public abstract void onDrag(Canvas c);
	
	public abstract void onRelease(Canvas c);
	
	public abstract void indicator(Canvas c);
	
}
