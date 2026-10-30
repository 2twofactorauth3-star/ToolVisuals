package ru.he3hauka.toolvisuals.client.gui.render;

final class UiRasterSizes {
    private static final int[] MASK={128,256,384,512,640,768};
    private static final int[] ICON={16,24,32,48,64};
    private static final int[] CORNER={4,8,12,16,24,32,48,64,96};
    private UiRasterSizes(){}
    private static int bucket(int requested,int[] sizes){for(int size:sizes)if(requested<=size)return size;return sizes[sizes.length-1];}
    static int mask(int requested){return bucket(requested,MASK);}
    static int icon(int requested){return bucket(requested,ICON);}
    static int corner(int requested){return bucket(requested,CORNER);}
}
