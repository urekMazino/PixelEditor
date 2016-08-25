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
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.sun.javafx.geom.Line2D;

import docks.SwingCanvas;
import javafx.embed.swing.SwingNode;
import javafx.scene.control.TabPane;
import jdk.nashorn.internal.runtime.regexp.joni.MatcherFactory;
import misc.ColorsUI;
import sun.java2d.SunGraphics2D;

public class DrawAreav3 extends JPanel{

	private static final long serialVersionUID = 1L;
	// Image in which we're going to draw
	  private BufferedImage image,previewLayer;
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
	  
	  private boolean endlessPencil = true;
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
	          bresenhamDrawLine(g2,SP.x,SP.y,EP.x,EP.y);
	          //g2.drawLine(SP.x,SP.y,EP.x,EP.y);
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
			  initGraphics();
		  }
		

	    g.setColor(ColorsUI.getPalette().darkPasiveColor);
	    g.fillRect(0, 0, this.getWidth(), this.getHeight());
	    paintAlphaBackground((Graphics2D)g);
	    g.drawImage(image, paddingx-vp.x, paddingy-vp.y,currentWidth,currentHeight, null);
	   
	    paintGrid(g,10,10,ColorsUI.getPalette().darkPasiveColor);
	    paintPixelGrid(g);

	    lastWidth = currentWidth;
	    lastHeight = currentHeight;
	    
	    zooming = false;
	  }
	  
	  private void bresenhamDrawLine(Graphics g,int x1, int y1,int x2,int y2){
  		 int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

  		 if (((x2<0 || x2>=originalWidth) && (x1<0 || x1>=originalWidth)) || ((y2<0 || y2>=originalHeight) && (y1<0 || y1>=originalHeight))){
  			 return;
  		 } else {

  		 }
  		 x2 = Math.max(0, Math.min(x2, originalWidth-1));
		 x1 = Math.max(0, Math.min(x1, originalWidth-1));
		 y2 = Math.max(0, Math.min(y2, originalHeight-1));
		 y1 = Math.max(0, Math.min(y1, originalHeight-1));
  			
  		/* boolean callAgainMirror = false;
  		 int mirrorx1=x1,mirrorx2=x2,mirrory1=y1,mirrory2=y2;
  		
		if ((x2<0 || x2>=originalWidth) && (x1>=0 && x1<originalWidth)){
			mirrorx2 = x2%originalWidth;
			mirrorx1 = (x2<0)?originalWidth-1:0;
			x2 = Math.max(0, Math.min(x2, originalWidth-1));
			callAgainMirror = true;
		} else if ((x1<0 || x1>=originalWidth) && (x2>=0 && x2<originalWidth)){
			mirrorx1 = x1%originalWidth;
			mirrorx2 = (x1<0)?originalWidth-1:0;
			x1 = Math.max(0, Math.min(x1, originalWidth-1));
			callAgainMirror = true;
		} else {
			if (endlessPencil){
  		  		x2 = x2%originalWidth;
  		   		x1 = x1%originalWidth;
			} else {
				return;
			}
		}
		
		if ((y2<0 || y2>=originalHeight) && (y1>=0 && y1<originalHeight)){
			mirrory2 = y2%originalHeight;
			mirrory1 = (y2<0)?originalHeight-1:0;
			y2 = Math.max(0, Math.min(y2, originalHeight-1));
			callAgainMirror = true;
		} else if ((y1<0 || y1>=originalHeight) && (y2>=0 && y2<originalHeight)){
			mirrory1 = y1%originalHeight;
			mirrory2 = (y1<0)?originalHeight-1:0;
			y1 = Math.max(0, Math.min(y1, originalHeight-1));
			callAgainMirror = true;
		} else {
			if (endlessPencil){
  		  		y2 = y2%originalHeight;
  		   		y1 = y1%originalHeight;
			} else {
				return;
			}
		} 
		
		if (callAgainMirror){
			bresenhamDrawLine(g,mirrorx1,mirrory1,mirrorx2,mirrory2);
		}
		*/
		 
  	    int Dx = x2 - x1;
  	    int Dy = y2 - y1;

  	    //# Increments
  	    int Sx = Integer.signum(Dx); 
  	    int Sy = Integer.signum(Dy);

  	    //# Segment length
  	    Dx = Math.abs(Dx); 
  	    Dy = Math.abs(Dy); 
  	    int D = Math.max(Dx, Dy);

  	    //# Initial remainder
  	    double R = D / 2;

  	    int X = x1;
  	    int Y = y1;
  	    if(Dx > Dy)
  	    {   
  	        //# Main loop
  	        for(int I=0; I<=D; I++)
  	        {   
  	  			 pixels[(Y*originalWidth)+X]=-16777216;
  	            //# Update (X, Y) and R
  	            X+= Sx; R+= Dy; //# Lateral move
  	            if (R >= Dx)
  	            {
  	                Y+= Sy; 
  	                R-= Dx; //# Diagonal move
  	            }
  	        }
  	    }
  	    else
  	    {   
  	        //# Main loop
  	        for(int I=0; I<=D; I++)
  	        {    
  	        	pixels[(Y*originalWidth)+X]=-16777216;
  	            //# Update (X, Y) and R
  	            Y+= Sy; 
  	            R+= Dx; //# Lateral move
  	            if(R >= Dy)
  	            {    
  	                X+= Sx; 
  	                R-= Dy; //# Diagonal move
  	            }
  	        }
  	    }
	  }
	  

	  public void paintAlphaBackground(Graphics2D g){
		  g.setPaint(textureBackground);
		  g.fillRect(paddingx-vp.x, paddingy-vp.y,currentWidth,currentHeight);

	  }
	  public void paintGrid(Graphics g,int w, int h,Color c){
		double pixelGap = zoom;
		double wIncrements = w*pixelGap;
		double hIncrements = h*pixelGap;
		int beginningx = (int)(paddingx-vp.x<0?(-pixelGap+((paddingx-vp.x)%wIncrements)):paddingx-vp.x);
		int beginningy = (int)(paddingy-vp.y<0?(-pixelGap+((paddingy-vp.y)%hIncrements)):paddingy-vp.y);
		int endingx  =(int)Math.min(paddingx+currentWidth-vp.x,vp.x+vp.getWidth()+pixelGap);
		int endingy = (int)Math.min(paddingy+currentHeight-vp.y,vp.y+vp.getHeight()+pixelGap);

		
		g.setColor(c);
		
		for(int i=beginningx;i<endingx;i+=wIncrements){ 
			g.drawLine(i,beginningy, i, endingy);
		}
		for(int j=beginningy;j<endingy;j+=hIncrements){ 
			g.drawLine(beginningx, j, endingx, j);
		}
	  }
	 public void paintPixelGrid(Graphics g){
		 if (zoom>=14){
			 paintGrid(g,1,1,new Color(255,255,255,100));
		 }
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
	/*private void fillBucket(){
		byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
	    int width = image.getWidth();
	    int height = image.getHeight();
	    boolean hasAlphaChannel = image.getAlphaRaster() != null;

	      int[][] result = new int[height][width];
	      if (hasAlphaChannel) {
	         final int pixelLength = 4;
	         for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
	            int argb = 0;
	            argb += (((int) pixels[pixel] & 0xff) << 24); // alpha
	            argb += ((int) pixels[pixel + 1] & 0xff); // blue
	            argb += (((int) pixels[pixel + 2] & 0xff) << 8); // green
	            argb += (((int) pixels[pixel + 3] & 0xff) << 16); // red
	            result[row][col] = argb;
	            col++;
	            if (col == width) {
	               col = 0;
	               row++;
	            }
	         }
	      } else {
	         final int pixelLength = 3;
	         for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
	            int argb = 0;
	            argb += -16777216; // 255 alpha
	            argb += ((int) pixels[pixel] & 0xff); // blue
	            argb += (((int) pixels[pixel + 1] & 0xff) << 8); // green
	            argb += (((int) pixels[pixel + 2] & 0xff) << 16); // red
	            result[row][col] = argb;
	            col++;
	            if (col == width) {
	               col = 0;
	               row++;
	            }
	         }
	      }

	      return result;
	}*/
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
	private void CreateAction(){
		
	}
	private void backgroundTextureInit(){
		BufferedImage textureBackgroundImg = new BufferedImage(alphaBlockSize*2, alphaBlockSize*2,BufferedImage.TYPE_INT_ARGB);
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
		backgroundTextureInit();
		initImage();
	}
	
}
