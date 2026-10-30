package ru.he3hauka.toolvisuals.cosmetic.accessory;

import java.util.List;

import ru.he3hauka.toolvisuals.cosmetic.style.ColorPalette;

public final class AccessoryPresets {
    public enum Preset {SERAPH,NIGHTFALL,PRISM,FAIRY,EMBER,STEAMPUNK,STARGAZER,FOREST}
    private AccessoryPresets(){}
    public static AccessoryLook create(Preset preset){
        return switch(preset){
            case SERAPH->look(AccessoryLook.Back.ANGEL,AccessoryLook.Head.HALO,0xfff0cb,0xe3b875,0xffe8a8,0xffffff);
            case NIGHTFALL->look(AccessoryLook.Back.DEMON,AccessoryLook.Head.HORNS,0x5c2447,0xff577e,0x6a224b,0xff7b8e);
            case PRISM->look(AccessoryLook.Back.CRYSTAL,AccessoryLook.Head.CROWN,0x79e3e8,0xbc93ff,0xb69aff,0xeffcff);
            case FAIRY->look(AccessoryLook.Back.BUTTERFLY,AccessoryLook.Head.CAT_EARS,0xffa0d6,0x83e8cb,0xb69aff,0xffb7d8);
            case EMBER->look(AccessoryLook.Back.PHOENIX,AccessoryLook.Head.FOX_EARS,0xff512c,0xffe2a1,0xe88432,0xfff1d3);
            case STEAMPUNK->look(AccessoryLook.Back.MECHANICAL,AccessoryLook.Head.HEADPHONES,0x4a7187,0xe9c28b,0x3e485b,0x77e1f2);
            case STARGAZER->look(AccessoryLook.Back.ORBITAL,AccessoryLook.Head.ASTROLABE,0x7364bc,0xf5dba2,0x758de8,0xffedbc);
            case FOREST->look(AccessoryLook.Back.DRAGONFLY,AccessoryLook.Head.ANTLERS,0x63b7a6,0xcaf8de,0x795b48,0xe0c3a1);
        };
    }
    private static AccessoryLook look(AccessoryLook.Back back,AccessoryLook.Head head,int a,int b,int c,int d){
        return new AccessoryLook(back,head,palette(a,b),palette(c,d),1,1,1,true);
    }
    private static ColorPalette palette(int first,int last){return new ColorPalette(List.of(first,last),ColorPalette.Mode.STATIC,ColorPalette.Space.RGB,4,0,0,1,true,false);}
}
