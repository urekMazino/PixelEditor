package docks;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseWheelEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneLayout;
import javax.swing.SwingUtilities;

import javafx.embed.swing.SwingNode;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Screen;
import sketch.DrawAreav2;
import sketch.DrawAreav3;
import sketch.ToolManager;
import misc.ColorsUI;
import misc.MyScrollBarUI;
public class SwingCanvas {
	
	SwingNode swingContainer;
	JPanel container;
	DrawAreav3 drawArea;
	TabPane tabPane;
	Tab tab;
	JLabel zoomLabel;
	JLabel sizeLabel;
	JLabel mousePosLabel;
	
	
	public SwingCanvas(TabPane tabPane,int w,int h,ToolManager TM){
    	swingContainer = new SwingNode();
    	this.tabPane = tabPane;
    	createAndSetSwingContent(w,h,TM);
    	System.out.println(tabPane.getParent().getParent().getParent());
    	tab = new Tab();
		tab.setText("newTab");
		tab.setContent(swingContainer);
		
		tabPane.getTabs().add(tab);
		
		SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
		selectionModel.select(tab);
	}

	public DrawAreav3 getDrawArea(){
		return drawArea;	
	}
	public Tab getTab(){
		return tab;
	}
	public void removeSwingNode(){
		swingContainer.setContent(null);		
	}
	public void addSwingNode(){
		swingContainer.setContent(container);
	}
	public TabPane getTabPane(){
		return tabPane;
	}
	public JLabel getZoomLabel(){
		return zoomLabel;
	}
	public JLabel getSizeLabel(){
		return sizeLabel;
	}
	public JLabel getMousePosLabel(){
		return mousePosLabel;
	}
	
	public JPanel createFooter(){
		FlowLayout flayout = new FlowLayout(FlowLayout.LEFT);
		flayout.setVgap(0);
		flayout.setHgap(20);
		
		JPanel footer = new JPanel(flayout);
    	footer.setBackground(ColorsUI.getPalette().normPasiveColor);
    	footer.setPreferredSize(new Dimension(0,18));
    	footer.setMinimumSize(new Dimension(0,18));
    	
    	zoomLabel = new JLabel("Zoom: 100%");
    	sizeLabel = new JLabel("Size: 0,0");
    	mousePosLabel = new JLabel("MousePos: 0,0");
    	
    	zoomLabel.setForeground(ColorsUI.getPalette().darkActiveColor);
    	sizeLabel.setForeground(ColorsUI.getPalette().darkActiveColor);
    	mousePosLabel.setForeground(ColorsUI.getPalette().darkActiveColor);
    	
    	footer.add(zoomLabel);
    	footer.add(sizeLabel);
    	footer.add(mousePosLabel);
    	
    	return footer;
    	
	}
	private void createAndSetSwingContent(int w,int h,ToolManager TM){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
            	//container
            	container = new JPanel();
            	container.setLayout(new BorderLayout());
            	container.setMinimumSize(new Dimension(0,0));
            	
            	JPanel footer = createFooter();
            	
        		drawArea = new DrawAreav3(w,h,TM,SwingCanvas.this);
        		drawArea.setBackground(ColorsUI.getPalette().darkPasiveColor);
        	    
        	    JPanel cornerFill = new JPanel();
        	    cornerFill.setBackground(ColorsUI.getPalette().darkPasiveColor);
        	    
        	    container.add(drawArea,BorderLayout.CENTER);
        	    container.add(footer,BorderLayout.SOUTH);
        	    
                swingContainer.setContent(container);
            }
        });
	}
	/*private void createAndSetSwingContent(int w,int h,ToolManager TM){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
            	//container
            	container = new JPanel();
            	container.setLayout(new BorderLayout());
            	container.setMinimumSize(new Dimension(0,0));
            	
            	JPanel footer = createFooter();
            	
        		drawArea = new DrawAreav2(w,h,TM,new Dimension((int)tabPane.getWidth()-20,(int)tabPane.getHeight()-45),SwingCanvas.this);
        		drawArea.setBackground(ColorsUI.getPalette().darkPasiveColor);
        		
        		spane = new JScrollPane(drawArea){
					private static final long serialVersionUID = 1L;

					@Override 
        			  public Dimension getPreferredSize(){
        				  return this.getSize();
        			  }
        		};
        		spane.setLayout(new ScrollPaneLayout());

        		spane.setWheelScrollingEnabled(false);
        		
    	  		spane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    	  		spane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    	  		
        		spane.setComponentZOrder(spane.getVerticalScrollBar(), 0);
        		spane.setComponentZOrder(spane.getHorizontalScrollBar(), 1);
        	    spane.setComponentZOrder(spane.getViewport(), 2);
        	    
         		spane.getVerticalScrollBar().setBlockIncrement((int)(Screen.getPrimary().getBounds().getHeight()*.05));
        		spane.getHorizontalScrollBar().setBlockIncrement((int)(Screen.getPrimary().getBounds().getWidth()*.05));
        		
        		spane.setBorder(BorderFactory.createEmptyBorder());
    
        		spane.getVerticalScrollBar().setUI(new MyScrollBarUI(JScrollBar.VERTICAL));
        	    spane.getVerticalScrollBar().setBackground(ColorsUI.getPalette().darkPasiveColor);
        	    spane.getHorizontalScrollBar().setUI(new MyScrollBarUI(JScrollBar.HORIZONTAL));
        	    spane.getHorizontalScrollBar().setBackground(ColorsUI.getPalette().darkPasiveColor);
        	    
        	    JPanel cornerFill = new JPanel();
        	    cornerFill.setBackground(ColorsUI.getPalette().darkPasiveColor);
        	    
        	    
        	    spane.setCorner(JScrollPane.LOWER_RIGHT_CORNER, cornerFill);
        	    
        	    drawArea.setSPane(spane); 
        	    
        	    container.add(spane,BorderLayout.CENTER);
        	    container.add(footer,BorderLayout.SOUTH);
        	    
        		spane.getViewport().setBackground(ColorsUI.getPalette().darkPasiveColor);
                swingContainer.setContent(container);
            }
        });
	}*/
	
	
}
