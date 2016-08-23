package sketch;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
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

public class DrawAreav2 extends JPanel implements Scrollable{

	private static final long serialVersionUID = 1L;
	// Image in which we're going to draw
	  private BufferedImage image;
	  // Graphics2D object ==> used to draw on
	  private Graphics2D g2;
	  
	  private JScrollPane JSP;
	  
	  private ToolManager TM;
	  
	  private double[] zoomArray = {.12,.25,.5,1,2,3,4,5,6,7,8,9,10,12,14,16,18,20,25,30,35,40,50,60};
	  
	  private int zoomIndex = 3;
	  
	  private boolean zooming = true;
	  
	  private int originalWidth,originalHeight,currentWidth, currentHeight,paddingx,paddingy, lastWidth=0;
	  
	  private int lastHeight=0;
	  
	  private double zoom = 1;
	  
	  private double inverseZoom = 1;
	  
	  private Point oldP,currentP,oldMP,currentMP,vp=new Point(0,0);
	  
	  private Dimension currentPaneSize=new Dimension(),lastPaneSize = new Dimension();
	  
	  private boolean fixedPadding=false;
	  	  
	  private Timer recalculateTimer,resizeTimer;
	  
	  private SwingCanvas SWC;
	  
	  // Mouse coordinates
	  public DrawAreav2(int w,int h,ToolManager TM,Dimension container,SwingCanvas SWC) {
	    recalculateTimer =  new Timer( 150,  new ActionListener(){
	    
		@Override
		public void actionPerformed(ActionEvent e) {
	  		JSP.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	  		JSP.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			
		}});
	    resizeTimer =  new Timer( 50,  new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
		  		JSP.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		  		JSP.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
				
			}});
	    resizeTimer.setRepeats(false);
		recalculateTimer.setRepeats( false );

		this.TM = TM;
		this.SWC = SWC;
		
		originalWidth = currentWidth = w;
		originalHeight = currentHeight = h;
		//UPDATE LABELS
		changeLabelText(SWC.getSizeLabel(),"Size: "+w+" , "+h);
		
		currentPaneSize.setSize(container.width,container.height-15);
		lastPaneSize = currentPaneSize;
		
		setDoubleBuffered(false);
		paddingx = Math.max(0,(int)((currentPaneSize.getWidth()-currentWidth)*.5));
		 paddingy = Math.max(0,(int)((currentPaneSize.getHeight()-currentHeight)*.5));
	    
		 addMouseListener(new MouseAdapter() {
	      public void mousePressed(MouseEvent e) {
	        oldMP = e.getPoint();
	    	oldP = new Point((int)((e.getX()-paddingx)*inverseZoom),(int)((e.getY()-paddingy)*inverseZoom));
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
					 JScrollBar currentScrollBar;
					 if (!ShortcutManager.getHorizontalEnabled()){
						 currentScrollBar = JSP.getVerticalScrollBar();
					 } else {
						 currentScrollBar = JSP.getHorizontalScrollBar();
					 }
					 int iScrollAmount = e.getScrollAmount();
					 int iNewValue = currentScrollBar.getValue() + currentScrollBar.getBlockIncrement() * iScrollAmount *e.getWheelRotation();
					 iNewValue = Math.max(0, Math.min(iNewValue, currentScrollBar.getMaximum()));
					 currentScrollBar.setValue(iNewValue);
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
	    	currentP =  new Point((int)((e.getX()-paddingx)*inverseZoom),(int)((e.getY()-paddingy)*inverseZoom));
	        if (e.getButton()==1){
		    	if (TM.getTool().equals("Pencil")){
					drawLine(oldP,currentP);
					// refresh draw area to repaint
					repaint();
					// store current coords x,y as olds x,y
			        oldP = currentP;
		        }
	        } else if (e.getButton()==2){
	        	vp = JSP.getViewport().getViewPosition();
	            vp.translate(oldMP.x-currentMP.x, oldMP.y-currentMP.y);
	            DrawAreav2.this.scrollRectToVisible(new Rectangle(vp, JSP.getViewport().getSize()));
	        }
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
	  	  if ((zoomIndex+dScale >= zoomArray.length)||(zoomIndex+dScale<0)) {
			  return;
		  } else if (!this.getSize().equals(currentPaneSize)){
			  return;
		  }
	  	  zooming = true;
	  	  zoomIndex += dScale;
	  	  
	  	  zoom =  zoomArray[zoomIndex];
	  	  inverseZoom = 1/zoom;
	  	  
	  	  changeLabelText(SWC.getZoomLabel(),"Zoom: "+((int)(zoom*100))+"%");
		  
		  currentWidth= (int)(originalWidth*zoom);
		  currentHeight= (int)(originalHeight*zoom);
		  
		  Dimension bounds = JSP.getViewport().getSize();
		  currentPaneSize = bounds;

		  vp = JSP.getViewport().getViewPosition();

		  double offsetx = ((MP.getX()-paddingx)/lastWidth)*(currentWidth- lastWidth);
		  double offsety = ((MP.getY()-paddingy)/lastHeight)*(currentHeight-lastHeight);
		  vp.translate((int)offsetx, (int)offsety);
		  
		  if (currentWidth>bounds.width || currentWidth>bounds.height){
			  if (!fixedPadding){
				  fixedPadding = true;
				  vp.setLocation(new Point((int)((currentWidth*.5)+(bounds.getWidth()*.4))+9,(int)((currentHeight*.5)+(bounds.getHeight()*.4))+9));
			  } 
			  paddingx = (int)(bounds.getWidth()*.9);
			  paddingy = (int)(bounds.getHeight()*.9);
			  currentPaneSize.setSize((int)(currentWidth+bounds.getWidth()*1.8),(int)(currentHeight+bounds.getHeight()*1.8));
			  
		  } else {
			  fixedPadding = false;
			  paddingx = Math.max(0,(int)((currentPaneSize.getWidth()-currentWidth)*.5));
			  paddingy = Math.max(0,(int)((currentPaneSize.getHeight()-currentHeight)*.5));
			  JSP.getHorizontalScrollBar().setEnabled(true);
		  }
	  	  
	  	  repaint();

	  }
	  public void resizedListener(){
		  if (!this.getSize().equals(currentPaneSize)){
			  return;
		  }
		  zooming=true;
		  
		  TabPane tp = SWC.getTabPane();
		  Dimension bounds = new Dimension((int)tp.getWidth()-20,(int)tp.getHeight()-41);
		  
		  
		  currentPaneSize = bounds;
		  
		  vp = JSP.getViewport().getViewPosition();
		  
		  if (currentWidth>bounds.width || currentWidth>bounds.height){
			  if (!fixedPadding){
				  fixedPadding = true;
				  vp.setLocation(new Point((int)((currentWidth*.5)+(bounds.getWidth()*.4))+9,(int)((currentHeight*.5)+(bounds.getHeight()*.4))+9));
			  } 
			  paddingx = (int)(bounds.getWidth()*.9);
			  paddingy = (int)(bounds.getHeight()*.9);
			  currentPaneSize.setSize((int)(currentWidth+bounds.getWidth()*1.8),(int)(currentHeight+bounds.getHeight()*1.8));
		  } else {
			  fixedPadding = false;
			  paddingx = Math.max(0,(int)((currentPaneSize.getWidth()-currentWidth)*.5));
			  paddingy = Math.max(0,(int)((currentPaneSize.getHeight()-currentHeight)*.5));
		  }
		  
		  
		  repaint();
	  }
	  public void resizedSpane(){
	  		JSP.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	  		JSP.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
			 if ( recalculateTimer.isRunning() ){
				    recalculateTimer.restart();
				  } else {
				    recalculateTimer.start();
			}
			resizedListener();
	  }
	  
	  static BufferedImage deepCopy(BufferedImage bi) {
		  ColorModel cm = bi.getColorModel();
		  boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		  WritableRaster raster = bi.copyData(null);
		  return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
		 }
	  public void setSPane(JScrollPane jsp){
		  JSP = jsp;
	  }
	  protected void paintComponent(Graphics g) {
	    if (image == null) {
	      // image to draw null ==> we create
	      image = new BufferedImage(originalWidth, originalHeight,BufferedImage.TYPE_INT_RGB);
	      g2 = (Graphics2D) image.getGraphics();
	      // enable antialiasing
	      g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
	      // clear draw area
	      clear();
	    }	    
	    if (zooming==true){

	    	setPreferredSize(currentPaneSize);
	    	scrollRectToVisible(new Rectangle(vp, JSP.getViewport().getSize()));
	    	revalidate();
	    	scrollRectToVisible(new Rectangle(vp, JSP.getViewport().getSize()));

	    }
	    g.setColor(ColorsUI.getPalette().darkPasiveColor);
	    g.fillRect(0, 0, (int)currentPaneSize.getWidth(), (int)currentPaneSize.getHeight()); 
	    	    
	    g.drawImage(image, paddingx, paddingy,currentWidth,currentHeight, null);
	    
	    paintPixelGrid(g);
	    
	    lastWidth = currentWidth;
	    lastHeight = currentHeight;
	  }
	  public void paintAlphaBackground(Graphics g){
		  g.setColor(ColorsUI.AlphaDark);
		  
		  for(int i=0;i<currentWidth;i+=8){
			  int offset = (16%i);
			  for (int j=0;i<currentHeight;i+=16){
				  int currentx = offset + paddingx+i;
				  int currenty = paddingy + j;
				  g.fillRect(currentx,currenty, currentx+8,currenty+8);
			  }
		  }
		  
	  }
	 public void paintPixelGrid(Graphics g){
		 if (zoom<12){
			 System.out.println("not enough zoom");
			 return;
		 }
		 //BufferedImage grid = new BufferedImage(currentWidth,currentHeight,BufferedImage.TYPE_INT_ARGB);
		 //Graphics2D gridG2 = (Graphics2D) grid.getGraphics();
		 
		 int pixelGap = (int)zoom;
		 System.out.println(pixelGap);
		 int endingx  =paddingx+ currentWidth;
		 int endingy = paddingy+currentHeight;
		 
		 //gridG2.setColor(new Color(255,0,255,100));
		 g.setColor(Color.BLACK);
		 
		 for(int i=1;i<originalWidth;i++){ 
			 int currentx = paddingx+(pixelGap*i);
			 g.drawLine(currentx,paddingy, currentx, endingy);
		 }
		 for(int j=1;j<originalHeight;j++){ 
			 int currenty = paddingy+(pixelGap*j);
			 g.drawLine(paddingx, currenty, endingx, currenty);
		 }
		 //g.drawImage(grid, paddingx, paddingy,currentWidth,currentHeight, null);
	 }
	  // now we create exposed methods
	  public void clear() {
		int leftoverx = (int)((getSize().getWidth()-currentWidth)*.5);
	    g2.setPaint(Color.white);
	    
	    // draw white on entire draw area to clear
	    g2.fillRect(0,0,currentWidth,currentHeight);
	    g2.setPaint(Color.black);
	    repaint();
	  }
	  public void red() {
	    // apply red color on g2 context
	    g2.setPaint(Color.red);
	  }
	 
	  public void black() {
	    g2.setPaint(Color.black);
	  }
	 
	  public void magenta() {
	    g2.setPaint(Color.magenta);
	  }
	 
	  public void green() {
	    g2.setPaint(Color.green);
	  }
	 
	  public void blue() {
	    g2.setPaint(Color.blue);
	  }
	@Override
	public Dimension getPreferredScrollableViewportSize() {
		// TODO Auto-generated method stub
		return currentPaneSize;
	}
	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public boolean getScrollableTracksViewportWidth() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean getScrollableTracksViewportHeight() {
		// TODO Auto-generated method stub
		return false;
	}
	
}
