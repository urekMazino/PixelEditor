package misc;

import com.sun.xml.internal.ws.util.StringUtils;

import javafx.scene.control.TextField;

public class NumberTextField extends TextField
{
	private int min=0;
	private int max=Integer.MAX_VALUE;
	
    @Override
    public void replaceText(int start, int end, String text)
    {
        if (validate(text))
        {
       		super.replaceText(start, end, text);
       		String newText = getText();
       		String fixedText = Integer.toString(getInt(newText));
       		super.replaceText(0,newText.length(),fixedText);
       		
       		positionCaret(((newText.length()-fixedText.length())!=0)?fixedText.length():end+1);
        }
    }

    @Override
    public void replaceSelection(String text)
    {
        if (validate(text))
        {
            super.replaceSelection(text);
        }
    }

    private boolean validate(String text)
    {	
        return text.matches("[0-9]*");
    }
    

    public int getInt(String value){
    	return !value.isEmpty() ? Math.min(max,Math.max(min,Integer.parseInt(value))) : min;
    }
    public int getInt(){
    	return getInt(getText());
    }
    public void setMin(int min){
    	this.min = min;
    }
    public void setMax(int max){
    	this.max = max;
    }
}