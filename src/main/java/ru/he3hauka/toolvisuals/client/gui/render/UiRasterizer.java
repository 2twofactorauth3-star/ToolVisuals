package ru.he3hauka.toolvisuals.client.gui.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

final class UiRasterizer {
    private UiRasterizer() {}

    static BufferedImage colorWheel(int pixels) {
        var image=new BufferedImage(pixels,pixels,BufferedImage.TYPE_INT_ARGB);
        for(int py=0;py<pixels;py++)for(int px=0;px<pixels;px++){
            double dx=(px+.5-pixels/2d)/(pixels/2d-1),dy=(py+.5-pixels/2d)/(pixels/2d-1),radius=Math.hypot(dx,dy);
            int alpha=(int)Math.round(Math.clamp((1-radius)*(pixels/2d-1)+.5,0,1)*255);
            image.setRGB(px,py,alpha<<24|(java.awt.Color.HSBtoRGB((float)((Math.atan2(dy,dx)/Math.TAU+1)%1),(float)Math.min(1,radius),1)&0xffffff));
        }
        return image;
    }
    static BufferedImage radialSegment(int pixels,float innerRatio,int index,int count) {
        var image=new BufferedImage(pixels,pixels,BufferedImage.TYPE_INT_ARGB);var g=graphics(image);var path=new Path2D.Double();
        double center=pixels/2d,outer=center-1,inner=outer*innerRatio,step=Math.TAU/count,start=-Math.PI/2+(index-.5)*step+.018,end=start+step-.036;
        for(int i=0;i<=32;i++){double angle=start+(end-start)*i/32,x=center+Math.cos(angle)*outer,y=center+Math.sin(angle)*outer;if(i==0)path.moveTo(x,y);else path.lineTo(x,y);}
        for(int i=32;i>=0;i--){double angle=start+(end-start)*i/32;path.lineTo(center+Math.cos(angle)*inner,center+Math.sin(angle)*inner);}
        path.closePath();g.fill(path);g.dispose();return image;
    }

    private static Graphics2D graphics(BufferedImage image) {
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        graphics.setColor(Color.WHITE);
        graphics.setStroke(new BasicStroke(1.25f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        return graphics;
    }

    static BufferedImage circle(int diameter) {
        BufferedImage image = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = graphics(image);
        graphics.fill(new Ellipse2D.Float(0, 0, diameter, diameter));
        graphics.dispose();
        return image;
    }
    static BufferedImage ring(int pixels, int width) {
        var image = new BufferedImage(pixels, pixels, BufferedImage.TYPE_INT_ARGB);
        var g = graphics(image);
        g.setStroke(new BasicStroke(width));
        float inset = width / 2f + 1;
        g.draw(new Ellipse2D.Float(inset, inset, pixels - inset * 2, pixels - inset * 2));
        g.dispose(); return image;
    }

    static BufferedImage icon(UiPaint.Icon icon, int pixels) {
        BufferedImage image = new BufferedImage(pixels, pixels, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = graphics(image);
        g.scale(pixels / 16f, pixels / 16f);
        switch (icon) {
            case SPEAKER, SPEAKER_OFF -> {path(g,2,6,5,6,8,3,8,13,5,10,2,10,2,6);if(icon==UiPaint.Icon.SPEAKER_OFF){line(g,10.5f,6,14,10);line(g,10.5f,10,14,6);}else{path(g,10.5f,5,12,6.5f,12,9.5f,10.5f,11);path(g,12.5f,2.5f,14.5f,5,14.5f,11,12.5f,13.5f);}}
            case MUSIC -> {path(g,6,11,6,3,13,1.8f,13,10);ellipse(g,4,11.5f,2,1.6f);ellipse(g,11,10.5f,2,1.6f);line(g,6,5.5f,13,4.2f);}
            case PLAY -> {var triangle=new Path2D.Float();triangle.moveTo(5,2.8);triangle.lineTo(13,8);triangle.lineTo(5,13.2);triangle.closePath();g.fill(triangle);}
            case PAUSE -> {g.fill(new RoundRectangle2D.Float(4,3,2.8f,10,1,1));g.fill(new RoundRectangle2D.Float(9.2f,3,2.8f,10,1,1));}
            case PREVIOUS, NEXT -> {if(icon==UiPaint.Icon.PREVIOUS){g.translate(16,0);g.scale(-1,1);}var triangle=new Path2D.Float();triangle.moveTo(3,3);triangle.lineTo(11,8);triangle.lineTo(3,13);triangle.closePath();g.fill(triangle);line(g,13,3,13,13);}
            case STOP -> g.fill(new RoundRectangle2D.Float(3.5f,3.5f,9,9,1.5f,1.5f));
            case SHUFFLE -> {path(g,2,4,4.5f,4,11.5f,12,14,12);path(g,2,12,4.5f,12,11.5f,4,14,4);path(g,11.5f,1.5f,14,4,11.5f,6.5f);path(g,11.5f,9.5f,14,12,11.5f,14.5f);}
            case REPEAT -> {path(g,3,6,3,4,13,4,13,6);path(g,13,10,13,12,3,12,3,10);path(g,10.5f,1.5f,13,4,10.5f,6.5f);path(g,5.5f,9.5f,3,12,5.5f,14.5f);}
            case PING -> {line(g,3,12,3,10);line(g,6.3f,12,6.3f,7.5f);line(g,9.6f,12,9.6f,5);line(g,13,12,13,2.5f);}
            case CLOCK -> {ellipse(g,8,8,6,6);path(g,8,4,8,8,11,9.5f);}
            case FPS -> {g.draw(new RoundRectangle2D.Float(2,2.5f,12,9,2,2));line(g,8,11.5f,8,14);line(g,5,14,11,14);path(g,4,8,6.5f,5,9,8,12,4.5f);}
            case CLOSE -> { line(g, 4, 4, 12, 12); line(g, 12, 4, 4, 12); }
            case SEARCH -> { ellipse(g, 6.8f, 6.8f, 4.5f, 4.5f); line(g, 10.2f, 10.2f, 14, 14); }
            case BACK, ARROW -> {
                float f = icon == UiPaint.Icon.BACK ? -1 : 1;
                line(g, 8 - f * 4, 8, 8 + f * 4, 8);
                path(g, 8 + f, 4.5f, 8 + f * 4.5f, 8, 8 + f, 11.5f);
            }
            case CHEVRON -> path(g,6,4,10,8,6,12);
            case WORLD -> { ellipse(g, 8, 8, 6, 6); ellipse(g, 8, 8, 2.8f, 6); line(g, 2, 8, 14, 8); }
            case PLAYER -> {
                ellipse(g, 8, 5, 2.4f, 2.4f);
                path(g, 3, 13.5f, 3, 12, 5.7f, 9.7f, 10.3f, 9.7f, 13, 12, 13, 13.5f);
            }
            case RENDER -> path(g, 8, 1.5f, 10, 6, 14.5f, 8, 10, 10, 8, 14.5f, 6, 10, 1.5f, 8, 6, 6, 8, 1.5f);
            case HUD -> {
                for (int x : new int[]{2, 9}) for (int y : new int[]{2, 9})
                    g.fill(new RoundRectangle2D.Float(x, y, 5, 5, 1.5f, 1.5f));
            }
            case CAMERA -> {
                path(g, 2, 5, 5, 5, 6, 3, 10, 3, 11, 5, 14, 5, 14, 13, 2, 13, 2, 5);
                ellipse(g, 8, 8.5f, 2.5f, 2.5f);
            }
            case EYE -> { ellipse(g, 8, 8, 6.5f, 4); ellipse(g, 8, 8, 2, 2); line(g, 2, 2, 14, 14); }
            case SETTINGS -> {
                ellipse(g, 8, 8, 3.8f, 3.8f); ellipse(g, 8, 8, 1.3f, 1.3f);
                for (int i = 0; i < 8; i++) {
                    double angle = Math.PI * i / 4;
                    line(g, 8 + (float)Math.cos(angle) * 4.5f, 8 + (float)Math.sin(angle) * 4.5f,
                            8 + (float)Math.cos(angle) * 6.2f, 8 + (float)Math.sin(angle) * 6.2f);
                }
            }
            case KEYBOARD -> {
                g.draw(new RoundRectangle2D.Float(1.5f, 3, 13, 10, 3, 3));
                for (int row = 0; row < 2; row++) for (int col = 0; col < 5; col++)
                    line(g, 3.5f + col * 2.2f, 5.5f + row * 2.5f, 3.5f + col * 2.2f, 5.7f + row * 2.5f);
                line(g, 5.5f, 10.5f, 10.5f, 10.5f);
            }
            case SUN -> {
                ellipse(g, 8, 8, 3.1f, 3.1f);
                for (int i = 0; i < 8; i++) {
                    double angle = Math.PI * i / 4;
                    line(g, 8 + (float)Math.cos(angle) * 5, 8 + (float)Math.sin(angle) * 5,
                            8 + (float)Math.cos(angle) * 6.5f, 8 + (float)Math.sin(angle) * 6.5f);
                }
            }
            case MOON -> {
                Path2D.Float moon = new Path2D.Float();
                moon.moveTo(11.5, 3.1);
                moon.curveTo(5, -0.5, 0, 7, 4.5, 11.5);
                moon.curveTo(7, 14, 11, 13, 13, 10.5);
                moon.curveTo(7, 12, 5, 5, 11.5, 3.1);
                g.draw(moon);
            }
            case RESET -> {
                var arc=new Path2D.Float();arc.moveTo(3,8);arc.curveTo(3,10.76,5.24,13,8,13);
                arc.curveTo(10.76,13,13,10.76,13,8);arc.curveTo(13,5.24,10.76,3,8,3);arc.curveTo(6,3,4.3,4.1,3,5.5);g.draw(arc);
                path(g,3,2.5f,3,5.5f,6,5.5f);
            }
            case VERSIONS -> {
                path(g,5,3,13,3,13,11);g.draw(new RoundRectangle2D.Float(2,6,8,7,1.5f,1.5f));
                path(g,4,9.5f,5.5f,11,8,8.5f);
            }
            case REALMS -> {path(g,2,5,5,8,8,3,11,8,14,5,12.5f,12,3.5f,12,2,5);line(g,4,14,12,14);}
            case CHECK -> path(g, 3, 8, 6.5f, 11.5f, 13, 4.5f);
            case PLUS -> {line(g,3,8,13,8);line(g,8,3,8,13);}
            case STAR, STAR_FILLED -> {
                Path2D.Float star=new Path2D.Float();
                for(int i=0;i<10;i++){
                    double angle=-Math.PI/2+i*Math.PI/5;float radius=i%2==0?6.5f:2.9f;
                    float x=8+(float)Math.cos(angle)*radius,y=8+(float)Math.sin(angle)*radius;
                    if(i==0)star.moveTo(x,y);else star.lineTo(x,y);
                }
                star.closePath();
                if (icon == UiPaint.Icon.STAR_FILLED) g.fill(star); else g.draw(star);
            }
        }
        g.dispose();
        return image;
    }

    private static void line(Graphics2D g, float x1, float y1, float x2, float y2) {
        g.draw(new Line2D.Float(x1, y1, x2, y2));
    }

    private static void ellipse(Graphics2D g, float cx, float cy, float rx, float ry) {
        g.draw(new Ellipse2D.Float(cx - rx, cy - ry, rx * 2, ry * 2));
    }

    private static void path(Graphics2D g, float... points) {
        Path2D.Float path = new Path2D.Float();
        path.moveTo(points[0], points[1]);
        for (int i = 2; i < points.length; i += 2) path.lineTo(points[i], points[i + 1]);
        g.draw(path);
    }
}
