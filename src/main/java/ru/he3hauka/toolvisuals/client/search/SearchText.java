package ru.he3hauka.toolvisuals.client.search;

import java.text.Normalizer;
import java.util.*;

public final class SearchText {
    private static final String EN="qwertyuiop[]asdfghjkl;'zxcvbnm,.",RU="йцукенгшщзхъфывапролджэячсмитьбю";
    private static final String CYRILLIC="абвгдеёжзийклмнопрстуфхцчшщъыьэюя";
    private static final String[] LATIN={"a","b","v","g","d","e","e","zh","z","i","y","k","l","m","n","o","p","r","s","t","u","f","h","c","ch","sh","sch","","y","","e","yu","ya"};
    private SearchText(){}
    public static String normalize(String text) {
        return Normalizer.normalize(text.replaceAll("([a-z])([A-Z])","$1 $2"),Normalizer.Form.NFKD).toLowerCase(Locale.ROOT).replace('ё','е').replaceAll("\\p{M}","").replaceAll("[^\\p{L}\\p{N}]+"," ").strip().replaceAll(" +"," ");
    }
    public static String transliterate(String text) {
        var output=new StringBuilder();for(char cp:text.toLowerCase(Locale.ROOT).toCharArray()){int index=CYRILLIC.indexOf(cp);output.append(index<0?String.valueOf(cp):LATIN[index]);}return output.toString();
    }
    public static Set<String> variants(String input) {
        input=input.toLowerCase(Locale.ROOT);var result=new LinkedHashSet<String>();
        for(String value:List.of(input,keyboard(input,EN,RU),keyboard(input,RU,EN))){String normalized=normalize(value);if(!normalized.isEmpty()){result.add(normalized);result.add(transliterate(normalized));}}
        return result;
    }
    private static String keyboard(String input,String from,String to){var result=new StringBuilder();for(char cp:input.toCharArray()){int at=from.indexOf(cp);result.append(at<0?cp:to.charAt(at));}return result.toString();}
    public static int distance(String a,String b) {
        if(Math.abs(a.length()-b.length())>2)return 3;
        int[][] grid=new int[a.length()+1][b.length()+1];for(int i=0;i<=a.length();i++)grid[i][0]=i;for(int j=0;j<=b.length();j++)grid[0][j]=j;
        for(int i=1;i<=a.length();i++)for(int j=1;j<=b.length();j++) {
            int cost=a.charAt(i-1)==b.charAt(j-1)?0:1;
            grid[i][j]=Math.min(grid[i-1][j]+1,Math.min(grid[i][j-1]+1,grid[i-1][j-1]+cost));
            if(i>1&&j>1&&a.charAt(i-1)==b.charAt(j-2)&&a.charAt(i-2)==b.charAt(j-1))grid[i][j]=Math.min(grid[i][j],grid[i-2][j-2]+1);
        }
        return grid[a.length()][b.length()];
    }
}
