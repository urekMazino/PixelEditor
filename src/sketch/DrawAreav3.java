package sketch;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import docks.SwingCanvas;
import javafx.embed.swing.SwingNode;
import javafx.scene.control.TabPane;
import jdk.nashorn.internal.runtime.regexp.joni.MatcherFactory;
import misc.ColorsUI;

public class DrawAreav3 extends JPanel{

	private static final long serialVersionUID = 1L;
	// Image in which we're going to draw
	  private BufferedImage image;
	  private TexturePaint textureBackground;
	  // Graphics2D object ==> used to draw on
	  private Graphics2D g2;
	  	  
	  private ToolManager TM;
	  
	  private double[] zoomArray = {.12,.25,.5,1,2,3,4,5,6,7,8,9,10,12,14,16,18,20,25,30,35,40,50,60,80,100,120};
	  
	  private int zoomIndex = 3;
	  
	  private boolean zooming = true;
	  
	  private int alphaBlockSize=8,originalWidth,originalHeight,currentWidth, currentHeight,paddingx,paddingy, lastWidth=0,lastHeight=0;
	  
	  private double zoom = 1,inverseZoom = 1;
	  
	  private Point oldP,currentP,oldMP,currentMP;
	  
	  private Dimension currentPaneSize=new Dimension(),lastPaneSize = new Dimension();
	  
	  private Rectangle vp= new Rectangle();
	  
	  private boolean fixedPadding=false;
	  	  
	  private Timer recalculateTimer,resizeTimer;
	  
	  private SwingCanvas SWC;
	  
	  // Mouse coordinates
	  public DrawAreav3(int w,int h,ToolManager TM,SwingCanvas SWC) {

	    //resizeTimer.setRepeats(false);
		//recalculateTimer.setRepeats( false );
		//this.setBackground(Color.RED);
		this.TM = TM;
		this.SWC = SWC;
		
		originalWidth = currentWidth = w;
		originalHeight = currentHeight = h;
		//UPDATE LABELS
		changeLabelText(SWC.getSizeLabel(),"Size: "+w+" , "+h);
				
		//setDoubleBuffered(false);
	    
		addComponentListener(new ComponentAdapter(){
	    	@Override
            public void componentResized(ComponentEvent e) {
	    		updateSize();
            }
	    });
		 
		 addMouseListener(new MouseAdapter() {
	      public void mousePressed(MouseEvent e) {
	        oldMP = e.getPoint();
	    	oldP = new Point((int)((e.getX()-paddingx+vp.x)*inverseZoom),(int)((e.getY()-paddingy+vp.y)*inverseZoom));
	        if (e.getButton()==1){
		    	if (TM.getTool().equals("Pencil")){
					drawLine(oldP,oldP);
					// refresh draw area to repaint
					repaint();
					// store current coords x,y as olds x,y
		        }
	        } 
	 		if (TM.getTool().equals("Zoom")){
	        	if (e.getButton()==1){
	        		zoom(1,oldP,e.getPoint());
	        	} else if (e.getButton()==3){
	        		zoom(-1,oldP,e.getPoint());
	        	}
	        }
	      }
	    });
	    
	    this.addMouseWheelListener(new MouseWheelListener(){
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) { 
				 if (!ShortcutManager.getZoomEnabled())//mouse wheel was rotated down/ towards the user
                 {
					 /*JScrollBar currentScrollBar;
					 if (!ShortcutManager.getHorizontalEnabled()){
						 currentScrollBar = JSP.getVerticalScrollBar();
					 } else {
						 currentScrollBar = JSP.getHorizontalScrollBar();
					 }
					 int iScrollAmount = e.getScrollAmount();
					 int iNewValue = currentScrollBar.getValue() + currentScrollBar.getBlockIncrement() * iScrollAmount *e.getWheelRotation();
					 iNewValue = Math.max(0, Math.min(iNewValue, currentScrollBar.getMaximum()));
					 currentScrollBar.setValue(iNewValue);*/
                 } else {
     				oldP = new Point((int)(e.getX()*inverseZoom),(int)(e.getY()*inverseZoom));
    				int rotations = e.getWheelRotation();
    				if (rotations >0){
    					zoom(-1,oldP,e.getPoint());
    				} else {
    					zoom(1,oldP,e.getPoint());
    				}
                 }
			}
	    });
	    
	    addMouseMotionListener(new MouseMotionAdapter() {
	      public void mouseDragged(MouseEvent e) {
	        // coord x,y when drag mouse
	    	currentMP = e.getPoint();
	    	currentP =  new Point((int)((e.getX()-paddingx+vp.x)*inverseZoom),(int)((e.getY()-paddingy+vp.y)*inverseZoom));
	        if (e.getButton()==1){
		    	if (TM.getTool().equals("Pencil")){
					drawLine(oldP,currentP);
					// refresh draw area to repaint
					repaint();
					// store current coords x,y as olds x,y
			        oldP = currentP;
		        }
	        } else if (e.getButton()==2){
	            moveVP((oldMP.x-currentMP.x), (oldMP.y-currentMP.y));
	            repaint();
	            //DrawAreav3.this.scrollRectToVisible(new Rectangle(vp, JSP.getViewport().getSize()));
	        }
	        oldMP = currentMP;
	       }
	      public void mouseMoved(MouseEvent e){
	    	  Point AdjustedP = new Point((int)((e.getX()-paddingx)*inverseZoom),(int)((e.getY()-paddingy)*inverseZoom));
	    	  //AdjustedP = new Point(Math.min(originalWidth,Math.max(0, AdjustedP.x)),Math.min(originalHeight,Math.max(0, AdjustedP.y)));
	    	  changeLabelText(SWC.getMousePosLabel(),"MousePos: "+(AdjustedP.x)+" , "+(AdjustedP.y));
	      }
	      
	    });
	    
	  }
	  public void changeLabelText(JLabel label, String s){
		  label.setText(s);
	  }
	  public void changeSize(int width, int height){
		  originalWidth = width;
		  originalHeight = height;
		  changeLabelText(SWC.getSizeLabel(),width+" , "+height);
	  }
	  
	  private void drawLine(Point SP, Point EP){
	        if (g2 != null) {
	          // draw line if g2 context not null
	          g2.setPaint(ColorsUI.transToAwtColor(TM.getForegroundColorProperty().get()));
	          g2.drawLine(SP.x,SP.y,EP.x,EP.y);
	          // refresh draw area to repaint
	          repaint();
	        }
	  }
	  public void zoom(int dScale,Point p,Point MP){
		  if (zooming == true){
			  return;
		  }
	  	  if ((zoomIndex+dScale >= zoomArray.length)||(zoomIndex+dScale<0)) {
			  return;
		  } 
	  	  zooming = true;
	  	  zoomIndex += dScale;
	  	 
	  	  zoom =  zoomArray[zoomIndex];
	  	  inverseZoom = 1/zoom;
	  	  
	  	  changeLabelText(SWC.getZoomLabel(),"Zoom: "+((int)(zoom*100))+"%");
	  	  
		  currentWidth= (int)(originalWidth*zoom);
		  currentHeight= (int)(originalHeight*zoom);
		  
		  updateSize();
		  
		  if (currentWidth>vp.getWidth() || currentHeight>vp.getHeight()){
			  double offsetx = ((MP.getX()-paddingx+vp.x)/lastWidth)*(currentWidth-lastWidth);
			  double offsety = ((MP.getY()-paddingy+vp.y)/lastHeight)*(currentHeight-lastHeight);
			  
			  moveVP((int)offsetx, (int)offsety);
		  }

	  	  repaint();

	  }
	  
	  static BufferedImage deepCopy(BufferedImage bi) {
		  ColorModel cm = bi.getColorModel();
		  boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		  WritableRaster raster = bi.copyData(null);
		  return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
		 }
	  
	  protected void paintComponent(Graphics g) {
		  if (image==null){
			  System.out.println("woops");
			  initGraphics();
		  }
	    g.setColor(ColorsUI.getPalette().darkPasiveColor);
	    g.fillRect(0, 0, this.getWidth(), this.getHeight());
	    paintAlphaBackground((Graphics2D)g);
	    g.drawImage(image, paddingx-vp.x, paddingy-vp.y,currentWidth,currentHeight, null);
	   
	    paintPixelGrid(g);
	    
	    lastWidth = currentWidth;
	    lastHeight = currentHeight;
	    
	    zooming = false;
	  }
	  
	  
	  public void paintAlphaBackground(Graphics2D g){
		  g.setPaint(textureBackground);
		  g.fillRect(paddingx-vp.x, paddingy-vp.y,currentWidth,currentHeight);

	  }

	 public void paintPixelGrid(Graphics g){
		 if (zoom<12){
			 return;
		 }
		 //BufferedImage grid = new BufferedImage(currentWidth,currentHeight,BufferedImage.TYPE_INT_ARGB);
		 //Graphics2D gridG2 = (Graphics2D) grid.getGraphics();
		 
		 
		 int pixelGap = (int)zoom;
		 int beginx = 
		 int endingx  =paddingx+currentWidth-vp.x;
		 int endingy = paddingy+currentHeight-vp.y;
		 
		 //gridG2.setColor(new Color(255,0,255,100));
		 g.setColor(Color.WHITE);
		 
		 for(int i=1;i<originalWidth;i++){ 
			 int currentx = paddingx-vp.x+(pixelGap*i);
			 g.drawLine(currentx,paddingy-vp.y, currentx, endingy);
		 }
		 for(int j=1;j<originalHeight;j++){ 
			 int currenty = paddingy-vp.y+(pixelGap*j);
			 g.drawLine(paddingx-vp.x, currenty, endingx, currenty);
		 }
		 //g.drawImage(grid, paddingx, paddingy,currentWidth,currentHeight, null);
	 }
	  // now we create exposed methods
	  public void clear() {
	    g2.setPaint(Color.white);
	    
	    // draw white on entire draw area to clear
	    g2.fillRect(0,0,currentWidth,currentHeight);
	    g2.setPaint(Color.black);
	    repaint();
	  }
	private void setVP(int x,int y){
		x = Math.min((int)(currentPaneSize.getWidth()-vp.getSize().getWidth()),Math.max(0, x));
		y = Math.min((int)(currentPaneSize.getHeight()-vp.getSize().getHeight()),Math.max(0, y));
		vp.setLocation(x,y);
	}
	private void moveVP(int x, int y){
		x = Math.min((int)(currentPaneSize.getWidth()-vp.getSize().getWidth()),Math.max(0, x+vp.x));
		y = Math.min((int)(currentPaneSize.getHeight()-vp.getSize().getHeight()),Math.max(0, y+vp.y));

		vp.setLocation(x,y);
	}
	
	private void updateSize(){		
		vp = new Rectangle(vp.getLocation(),getSize());
		if (currentWidth>vp.getWidth() || currentHeight>vp.getHeight()){
			  paddingx = (int)(vp.getWidth()*.9);
			  paddingy = (int)(vp.getHeight()*.9);
			  currentPaneSize.setSize(vp.getWidth()*1.8+currentWidth, vp.getHeight()*1.8+currentHeight);
			  if (vp.x == 0 && vp.y == 0){
				  setVP((int)((lastWidth*.5)+(vp.getWidth()*.4)),(int)((lastHeight*.5)+(vp.getHeight()*.4)));
			  }
		  } else {
			  paddingx = Math.max(0,(int)((vp.getWidth()-currentWidth)*.5));
			  paddingy = Math.max(0,(int)((vp.getHeight()-currentHeight)*.5));
			  currentPaneSize.setSize(vp.getSize());
			  setVP(0,0);
		  }
	}
	
	private void backgroundTextureInit(){
		BufferedImage textureBackgroundImg = new BufferedImage(alphaBlockSize*2, alphaBlockSize*2,BufferedImage.TYPE_INT_RGB);
		Graphics2D currentContext = textureBackgroundImg.createGraphics();
		currentContext.setPaint(ColorsUI.AlphaDark);
		currentContext.fillRect(0, 0, alphaBlockSize, alphaBlockSize);
		currentContext.fillRect(alphaBlockSize, alphaBlockSize, alphaBlockSize, alphaBlockSize);
		currentContext.setPaint(ColorsUI.AlphaLight);
		currentContext.fillRect(alphaBlockSize, 0, alphaBlockSize, alphaBlockSize);
		currentContext.fillRect(0, alphaBlockSize, alphaBlockSize, alphaBlockSize);
		
		textureBackground = new TexturePaint(textureBackgroundImg,new Rectangle(0,0,alphaBlockSize*2,alphaBlockSize*2));
	}

	private void initImage(){
      image = new BufferedImage(originalWidth, originalHeight,BufferedImage.TYPE_INT_ARGB);
      g2 = (Graphics2D) image.getGraphics();
      g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
  
	}
	
	private void initGraphics(){
		initImage();
		backgroundTextureInit();
	}
	
}
