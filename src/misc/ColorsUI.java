package misc;

import java.awt.Color;

public final class ColorsUI {
	
	public static Color DARKNIAGARA =  createHex("006644");
	public static Color NIAGARA1 = createHex("#009966");
	public static Color TURQUOISE = new Color(54,215,183);

	public static Color GREY = new Color(43,43,43);  /*panel background*/
	public static Color DARKGREY = new Color(26,26,26);  /*behind panel */
	public static Color LIGHTGREY = createHex("#666666");  /*highlight panel background*/

	public static Color AlphaDark = new Color(202,202,202);
	public static Color AlphaLight = new Color(252,252,252);
	
	static ColorPalette currentPalette = new ColorPalette(DARKNIAGARA,NIAGARA1,TURQUOISE,DARKGREY,GREY,LIGHTGREY);
	
	private ColorsUI(){}

	public static javafx.scene.paint.Color transToJavafxColor(Color col){
		int r = col.getRed();
		int g = col.getGreen();
		int b = col.getBlue();
		int a = col.getAlpha();
		double opacity = a / 255.0 ;
		return javafx.scene.paint.Color.rgb(r, g, b, opacity);

	}
	
	public static Color transToAwtColor(javafx.scene.paint.Color col){
		return new Color((int)col.getRed()*255,(int)col.getGreen()*255,(int)col.getBlue()*255);
	}
	
	public static Color createHex(String hex){
		int start = (hex.length()==6)?0:1;
		 return new Color(
		            Integer.valueOf( hex.substring( start, start+2 ), 16 ),
		            Integer.valueOf( hex.substring( start+2, start+4 ), 16 ),
		            Integer.valueOf( hex.substring( start+4, start+6 ), 16 ) );
	}
	
	public static ColorPalette getPalette(){
		return currentPalette;
	}
	
}

