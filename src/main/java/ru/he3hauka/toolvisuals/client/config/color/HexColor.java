package ru.he3hauka.toolvisuals.client.config.color;

import java.util.Locale;

public final class HexColor {
    private HexColor(){}
    public static Integer parse(String value){return parse(value,true);}
    public static Integer parse(String value,boolean prefix){
        if(value==null)return null;
        int offset=prefix&&value.startsWith("#")?1:0;
        if(value.length()-offset!=6)return null;
        int color=0;
        for(int i=offset;i<value.length();i++){
            char c=value.charAt(i);
            int digit=c>='0'&&c<='9'?c-'0':c>='a'&&c<='f'?c-'a'+10:c>='A'&&c<='F'?c-'A'+10:-1;
            if(digit<0)return null;
            color=color*16+digit;
        }
        return color;
    }
    public static int require(String value){var color=parse(value);if(color==null)throw new NumberFormatException("Expected six HEX digits");return color;}
    public static String format(int color,boolean prefix){return String.format(Locale.ROOT,prefix?"#%06X":"%06X",color&0xffffff);}
}
