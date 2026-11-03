package ru.he3hauka.toolvisuals.client.cape.animation;

import java.util.ArrayList;
import java.util.List;
import java.util.ArrayDeque;
import ru.he3hauka.toolvisuals.client.cape.CapeCanvas;
import ru.he3hauka.toolvisuals.cosmetic.cape.CapeDesign;
import ru.he3hauka.toolvisuals.cosmetic.cape.CapeAnimationPolicy;

public final class CapeAnimationDraft {
    private final List<CapeCanvas> frames=new ArrayList<>();
    private CapeDesign cached;
    private int selected,fps;
    private record Edit(CapeDesign design,int selected){}
    private Edit before;
    private final ArrayDeque<Edit> undo=new ArrayDeque<>(),redo=new ArrayDeque<>();

    public CapeAnimationDraft(CapeDesign design){for(int i=0;i<design.frameCount;i++)frames.add(new CapeCanvas(design.frame(i)));fps=design.fps;cached=design;}
    public CapeCanvas canvas(){return frames.get(selected);}
    public int selected(){return selected;}
    public int size(){return frames.size();}
    public int fps(){return fps;}
    public void begin(){if(before==null)before=new Edit(snapshot(),selected);}
    public void end(){canvas().end();if(before==null)return;if(!snapshot().equals(before.design)){undo.addLast(before);if(undo.size()>50)undo.removeFirst();redo.clear();}before=null;}
    public boolean canUndo(){return !undo.isEmpty();}
    public boolean canRedo(){return !redo.isEmpty();}
    public void undo(){end();if(undo.isEmpty())return;redo.addLast(new Edit(snapshot(),selected));restore(undo.removeLast());}
    public void redo(){end();if(redo.isEmpty())return;undo.addLast(new Edit(snapshot(),selected));restore(redo.removeLast());}
    private void restore(Edit edit){frames.clear();for(int i=0;i<edit.design.frameCount;i++)frames.add(new CapeCanvas(edit.design.frame(i)));fps=edit.design.fps;selected=Math.min(edit.selected,frames.size()-1);cached=edit.design;before=null;}
    public void fps(int value){begin();fps=Math.clamp(value,1,CapeAnimationPolicy.MAX_FPS);end();}
    public void select(int index){end();selected=Math.floorMod(index,frames.size());}
    public boolean duplicate(){if(frames.size()>=CapeAnimationPolicy.MAX_FRAMES)return false;begin();canvas().end();frames.add(selected+1,new CapeCanvas(canvas().snapshot()));fps=Math.min(fps,CapeAnimationPolicy.MAX_FPS);selected++;end();return true;}
    public boolean remove(){if(frames.size()==1)return false;begin();frames.remove(selected);selected=Math.min(selected,frames.size()-1);end();return true;}
    public void replace(CapeDesign design){begin();var previous=before;restore(new Edit(design,0));before=previous;end();}
    public CapeDesign snapshot(){
        boolean changed=cached==null||cached.fps!=fps||cached.frameCount!=frames.size();
        if(!changed)for(int i=0;i<frames.size();i++)if(!frames.get(i).snapshot().equals(cached.frame(i))){changed=true;break;}
        if(changed)cached=new CapeDesign(frames.stream().map(CapeCanvas::snapshot).toList(),fps);return cached;
    }
}
