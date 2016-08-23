package sketch;

 
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import javafx.embed.swing.SwingNode;
import javafx.scene.Group;


 
/**
* Component for drawing !
*
* @author sylsau
*
*/
public class DrawArea extends JComponent {
 
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
// Image in which we're going to draw
  private BufferedImage image;
  // Graphics2D object ==> used to draw on
  private Graphics2D g2;
  
  private JScrollPane JSP;
  
  private ToolManager TM;
  
  private double zoomInRate = 1.1;
  
  private double zoomOutRate = 1/zoomInRate;
  
  private double maxZoom = 20;
  
  private double minZoom = .0025;
  
  private int originalWidth,originalHeight,currentWidth, currentHeight;
  
  private int lastWidth=0;
  
  private int lastHeight=0;
  
  private double zoom = 1;
  
  private double inverseZoom = 1;
  
  private Point offset = new Point(0,0),oldP,currentP,focusPoint,oldMP,currentMP,vp=new Point(0,0);
  
  private boolean fixedPadding=false;
  private JPanel parentPanel;
  
  // Mouse coordinates
  SwingNode parent;
  public DrawArea(int w,int h,ToolManager TM) {
	this.TM = TM;
	originalWidth = currentWidth = w;
	originalHeight = currentHeight = h;
	setPreferredSize(new Dimension(w,h));
	SwingUtilities.invokeLater(new Runnable(){

		@Override
		public void run() {
			parentPanel = (JPanel)DrawArea.this.getParent();
		}
		
	});
	setDoubleBuffered(false);
    addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        // save coord x,y when mouse is pressed
    	oldMP = e.getPoint();
    	oldP = new Point((int)(e.getX()*inverseZoom),(int)(e.getY()*inverseZoom));
 		if (TM.getTool().equals("Zoom")){
        	if (e.getButton()==1){
        		zoom(zoomInRate,oldP,e.getPoint());
        	} else if (e.getButton()==3){
        		zoom(zoomOutRate,oldP,e.getPoint());
        	}
        }
      }
    });
    
    this.addMouseWheelListener(new MouseWheelListener(){

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			
			oldP = new Point((int)(e.getX()*inverseZoom),(int)(e.getY()*inverseZoom));
			
			int rotations = e.getWheelRotation();
			if (rotations >0){
				zoom(zoomOutRate,oldP,e.getPoint());
			} else {
				zoom(zoomInRate,oldP,e.getPoint());
			}
			
		}
    	
    });
    
    addMouseMotionListener(new MouseMotionAdapter() {
      public void mouseDragged(MouseEvent e) {
        // coord x,y when drag mouse
    	currentMP = e.getPoint();
    	currentP =  new Point((int)(e.getX()*inverseZoom),(int)(e.getY()*inverseZoom));
        if (e.getButton()==1){
	    	if (TM.getTool().equals("Pencil")){
				// draw line if g2 context not null
				drawLine(oldP,currentP);
				// refresh draw area to repaint
				repaint();
				// store current coords x,y as olds x,y
		        oldP = currentP;
	        }
        } else if (e.getButton()==2){
        	vp = JSP.getViewport().getViewPosition();
            vp.translate(oldMP.x-currentMP.x, oldMP.y-currentMP.y);
            DrawArea.this.parentPanel.scrollRectToVisible(new Rectangle(vp, JSP.getViewport().getSize()));
        }        
      }
    });
    
  }
  private void drawLine(Point SP, Point EP){
        if (g2 != null) {
          // draw line if g2 context not null
          g2.drawLine(SP.x,SP.y, EP.x,EP.y);
          // refresh draw area to repaint
          repaint();
          // store current coords x,y as olds x,y
        }
  }
  public void setSwingNode(SwingNode sn){
	  parent = sn;
  }
  public void changeSize(int w, int h){
	  setSize(w,h);
	  repaint();
  }
  public void zoom(double dScale,Point p,Point MP){
	  zoom = Math.max(Math.min(zoom*dScale, maxZoom),minZoom);
	  //System.out.println(zoom);
	  inverseZoom = 1/zoom;
	  currentWidth= (int)(originalWidth*zoom);
	  currentHeight= (int)(originalHeight*zoom);
	  
	  Dimension bounds = JSP.getViewport().getSize();
	  
	  Dimension currentSize = this.getSize();

	  vp = JSP.getViewport().getViewPosition();
	  double offsetx = (MP.getX()/lastWidth)*(currentWidth- lastWidth);
	  double offsety = (MP.getY()/lastHeight)*(currentHeight-lastHeight);
	  vp.translate((int)offsetx, (int)offsety);
	  if (currentWidth>bounds.width || currentWidth>bounds.height){
		  if (!fixedPadding){
			  fixedPadding = true;
			  vp= new Point((int)((currentWidth*.5)+(bounds.getWidth()*.4))+9,(int)((currentHeight*.5)+(bounds.getHeight()*.4))+9);
		  }
		  

		  currentSize.setSize((int)(currentWidth+bounds.getWidth()*1.8),(int)(currentHeight+bounds.getHeight()*1.8));
		  JSP.getViewport().getView().setPreferredSize(currentSize);
	  } else {
		  fixedPadding = false;
		  JSP.getViewport().getView().setPreferredSize(currentSize);
	  }

	  
	
	  //Point focusPoint = new Point((int)((MP.getX()*dScale)-(bounds.getWidth()*.5)+(bounds.getWidth()*.9)), (int)((MP.getY()*dScale)-(bounds.getHeight()*.5)+(bounds.getHeight()*.9)));
	  
	  red();
	  Point testMP = new Point((int)((MP.getX()*dScale)*inverseZoom),(int)((MP.getY()*dScale)*inverseZoom));
	  g2.setStroke(new BasicStroke(10));
	  drawLine(testMP,testMP);
  	  black();
	  

	  repaint();
	  
  }
  public void resizedSpane(){
	  
	  Dimension currentSize = this.getSize();
	  Dimension bounds = JSP.getViewport().getSize();
	  
	  
	  if (currentWidth>bounds.width || currentWidth>bounds.height){
		  if (!fixedPadding){
			  fixedPadding = true;
		  }
		  currentSize.setSize((int)(currentWidth+bounds.getWidth()*1.8),(int)(currentHeight+bounds.getHeight()*1.8));
		  JSP.getViewport().getView().setPreferredSize(currentSize);
	  } else {
		  fixedPadding = false;
		  JSP.getViewport().getView().setPreferredSize(currentSize);
	  }
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
      image = new BufferedImage(originalWidth, originalHeight,BufferedImage.TYPE_INT_ARGB);
      g2 = (Graphics2D) image.getGraphics();
      // enable antialiasing
      g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
      // clear draw area
      clear();
    }
    if (lastWidth!=currentWidth || lastHeight!=currentHeight){
    	setPreferredSize(new Dimension(currentWidth,currentHeight));
	
	  	  SwingUtilities.invokeLater(new Runnable(){
	  		@Override
	  		public void run() {
	  		  	  JSP.revalidate();
	  		  	  revalidate();
	  			  parentPanel.scrollRectToVisible(new Rectangle(vp, JSP.getViewport().getSize()));
	  		}
	  	  });
    }
    g.setColor(Color.red);
    g.fillRect(0, 0, this.getWidth(), this.getHeight()); 
    g.drawImage(image, (int)offset.getX(), (int)offset.getY(),currentWidth,currentHeight, null);

    lastWidth = currentWidth;
    lastHeight = currentHeight;
  }
 
  // now we create exposed methods
  public void clear() {
    g2.setPaint(Color.white);
    // draw white on entire draw area to clear
    g2.fillRect(0, 0, getSize().width, getSize().height);
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
 
}