package ru.he3hauka.toolvisuals.cosmetic.accessory;

import java.util.Objects;

import ru.he3hauka.toolvisuals.cosmetic.style.ColorPalette;

public record AccessoryLook(Back back,Head head,ColorPalette backColors,ColorPalette headColors,float backScale,float headScale,float speed,boolean animated){
    public enum Back {
        NONE(0),ANGEL(1),DEMON(2),BUTTERFLY(3),CRYSTAL(4),PHOENIX(5),DRAGON(6),DRAGONFLY(7),MECHANICAL(8),ORBITAL(9),SWORDS(10);
        public final int id;Back(int id){this.id=id;}
        public static Back byId(int id){return switch(id){case 0->NONE;case 1->ANGEL;case 2->DEMON;case 3->BUTTERFLY;case 4->CRYSTAL;case 5->PHOENIX;case 6->DRAGON;case 7->DRAGONFLY;case 8->MECHANICAL;case 9->ORBITAL;case 10->SWORDS;default->throw new IllegalArgumentException("Unknown back cosmetic");};}
    }
    public enum Head {
        NONE(0),HORNS(1),CROWN(2),HALO(3),CAT_EARS(4),FOX_EARS(5),ANTLERS(6),WITCH_HAT(7),HEADPHONES(8),FLOWER_CROWN(9),ASTROLABE(10);
        public final int id;Head(int id){this.id=id;}
        public static Head byId(int id){return switch(id){case 0->NONE;case 1->HORNS;case 2->CROWN;case 3->HALO;case 4->CAT_EARS;case 5->FOX_EARS;case 6->ANTLERS;case 7->WITCH_HAT;case 8->HEADPHONES;case 9->FLOWER_CROWN;case 10->ASTROLABE;default->throw new IllegalArgumentException("Unknown head cosmetic");};}
    }
    public static final AccessoryLook EMPTY=new AccessoryLook(Back.NONE,Head.NONE,ColorPalette.solid(0xb69aff),ColorPalette.solid(0xff738d),1,1,1,true);
    public AccessoryLook{
        Objects.requireNonNull(back);Objects.requireNonNull(head);Objects.requireNonNull(backColors);Objects.requireNonNull(headColors);
        if(!Float.isFinite(backScale)||backScale<.65f||backScale>1.35f||!Float.isFinite(headScale)||headScale<.65f||headScale>1.35f||!Float.isFinite(speed)||speed<.5f||speed>2)throw new IllegalArgumentException("Invalid cosmetic dimensions");
    }
    public boolean empty(){return back==Back.NONE&&head==Head.NONE;}
    public AccessoryLook legacy(){
        if(back.id<=4&&head.id<=4)return this;
        return new AccessoryLook(back.id<=4?back:Back.NONE,head.id<=4?head:Head.NONE,backColors,headColors,backScale,headScale,speed,animated);
    }
}
