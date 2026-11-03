package ru.he3hauka.toolvisuals.client.hud.players;

import java.util.ArrayList;
import java.util.List;

public final class PlayerPanelLayout {
    public record Cell(int index,int column,float y,float height){}
    public record Layout(List<Cell> cells,int columns,float height,int more){}
    private PlayerPanelLayout(){}
    public static Layout arrange(List<Float> heights,float maximumHeight,int maximumColumns){
        if(!Float.isFinite(maximumHeight)||maximumHeight<23||maximumColumns<1)throw new IllegalArgumentException("Invalid player panel size");
        var cells=new ArrayList<Cell>();int column=0,more=0;float y=0;
        for(int index=0;index<heights.size();index++){
            float requested=heights.get(index);if(!Float.isFinite(requested)||requested<23)throw new IllegalArgumentException("Invalid player row height");
            float height=Math.min(maximumHeight,requested);
            if(y>0&&y+height>maximumHeight){column++;y=0;}
            if(column>=maximumColumns){var old=cells.removeLast();more=heights.size()-old.index;cells.add(new Cell(-1,old.column,old.y,23));break;}
            cells.add(new Cell(index,column,y,height));y+=height;
        }
        float height=23;int columns=1;for(var cell:cells){height=Math.max(height,cell.y+cell.height);columns=Math.max(columns,cell.column+1);}
        return new Layout(List.copyOf(cells),columns,height,more);
    }
}
