package ru.he3hauka.toolvisuals.client.cape;

import ru.he3hauka.toolvisuals.cosmetic.cape.CapeDesign;

final class CapeFlags {
    private static final int WHITE=0xffffffff,RED=0xffce1720,GOLD=0xffffd634;
    private CapeFlags() {}

    static int pixel(CapePresets.Kind kind,int x,int y) {
        return switch(kind) {
            case RUSSIA->y<CapeDesign.HEIGHT/3?WHITE:y<CapeDesign.HEIGHT*2/3?0xff0039a6:0xffd52b1e;
            case UKRAINE->y<CapeDesign.HEIGHT/2?0xff0057b7:0xffffd700;
            case BELARUS->belarus(x,y);
            case KAZAKHSTAN->kazakhstan(x,y);
            default->throw new IllegalArgumentException("Not a flag preset: "+kind);
        };
    }

    private static int belarus(int x,int y) {
        if(x<3) {
            int row=y%8;
            return x==1?(row==0||row==4?WHITE:RED):(row==1||row==3||row==5||row==7?WHITE:RED);
        }
        return y<CapeDesign.HEIGHT*2/3?RED:0xff009739;
    }

    private static int kazakhstan(int x,int y) {
        if(x<3) {
            int row=y%8;
            if(x==1&&(row==0||row==3||row==4||row==7)||x!=1&&(row==1||row==2||row==5||row==6))return GOLD;
        }
        int dx=x-11,dy=y-12,distance=dx*dx+dy*dy;
        boolean sun=distance<=8||distance<=26&&(dx==0||dy==0||Math.abs(dx)==Math.abs(dy));
        int span=Math.abs(x-11);
        boolean eagle=y==20&&span<=6||y==21&&span>=2&&span<=5||y==22&&span<=2||y==23&&span<=1;
        return sun||eagle?GOLD:0xff00afca;
    }
}
