package misc;

import java.awt.Color;

public class ColorPalette{
	
	public Color darkActiveColor;
	public Color normActiveColor;
	public Color highActiveColor;
	public Color darkPasiveColor;
	public Color normPasiveColor;
	public Color highPasiveColor;
	
	public ColorPalette(Color dac, Color nac, Color hac,Color dpc, Color npc, Color hpc){
		darkActiveColor = dac;
		normActiveColor = nac;
		highActiveColor = hac;
		darkPasiveColor = dpc;
		normPasiveColor = npc;
		highPasiveColor = hpc;
	}
}