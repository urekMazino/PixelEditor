package misc;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Scrollbar;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.plaf.basic.BasicScrollBarUI;

public class MyScrollBarUI extends BasicScrollBarUI {
  private final Dimension d = new Dimension(0,0);
  private int sizeDifference = 2;
  private int xDifference = 0;
  private int yDifference = 0;
  private int xOffset = 0;
  private int yOffset = 0; 
  
  public MyScrollBarUI(int orientation){
	  if (orientation==JScrollBar.HORIZONTAL){
		  yDifference = sizeDifference;
		  yOffset = yDifference/2; 
	  } else {
		  xDifference = sizeDifference;
		  xOffset = xDifference/2; 
	  }
  }
  
  @Override
  protected JButton createDecreaseButton(int orientation) {
	  return new JButton() {
	      @Override
	      public Dimension getPreferredSize() {
	        return d;
	      }
	  };
  }

  @Override
  protected JButton createIncreaseButton(int orientation) {
    return new JButton() {
      @Override
      public Dimension getPreferredSize() {
        return d;
      }
    };
  }
 	
  @Override
  protected void paintTrack(Graphics g, JComponent c, Rectangle r) {

  }
  
  @Override
  protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
    Color color = null;
    
    if (isDragging) {
      color = ColorsUI.currentPalette.darkActiveColor;
    } else if (isThumbRollover()) {
      color = ColorsUI.currentPalette.highActiveColor;
    } else {
      color = ColorsUI.currentPalette.normActiveColor; 
    }
    
    g2.setPaint(color);
    g2.fillRoundRect(r.x + xOffset, r.y + yOffset, r.width - xDifference, r.height - yDifference, 15, 15);
    
    g2.dispose();
  }

  @Override
  protected void setThumbBounds(int x, int y, int width, int height) {
    super.setThumbBounds(x, y, width, height);
    scrollbar.repaint();
  }
}