package ru.he3hauka.toolvisuals.client.animation;

import ru.he3hauka.toolvisuals.animation.Emote;
import ru.he3hauka.toolvisuals.animation.PairEmote;

public final class EmotePose {
    public record Rotation(float x, float y, float z) {
        public static final Rotation ZERO = new Rotation(0, 0, 0);
    }

    public static final EmotePose NEUTRAL = new EmotePose(Rotation.ZERO, Rotation.ZERO, Rotation.ZERO,
            Rotation.ZERO, Rotation.ZERO, Rotation.ZERO, Rotation.ZERO, 0, 0);
    public final Rotation head, body, rightArm, leftArm, rightLeg, leftLeg, root;
    public final float lift, weight;

    private EmotePose(Rotation head, Rotation body, Rotation rightArm, Rotation leftArm, Rotation rightLeg,
                      Rotation leftLeg, Rotation root, float lift, float weight) {
        this.head = head;
        this.body = body;
        this.rightArm = rightArm;
        this.leftArm = leftArm;
        this.rightLeg = rightLeg;
        this.leftLeg = leftLeg;
        this.root = root;
        this.lift = lift;
        this.weight = weight;
    }

    private static Rotation rotation(double x, double y, double z) {
        return new Rotation((float) Math.toRadians(x), (float) Math.toRadians(y), (float) Math.toRadians(z));
    }

    public static EmotePose sample(Emote emote, double ticks) {
        if (!Double.isFinite(ticks)) ticks = 0;
        ticks = Math.clamp(ticks, 0, emote.duration);
        double t = ticks / emote.duration;
        double fade = ease(Math.min(ticks / 8, (emote.duration - ticks) / 8));
        double wave = Math.sin(t * Math.TAU * 4), beat = Math.sin(t * Math.TAU * 6);
        Rotation head = Rotation.ZERO, body = Rotation.ZERO;
        Rotation right = rotation(0, 0, 3), left = rotation(0, 0, -3);
        Rotation rightLeg = Rotation.ZERO, leftLeg = Rotation.ZERO, root = Rotation.ZERO;
        float lift = 0;
        boolean fullTurn = emote == Emote.BACKFLIP || emote == Emote.FRONTFLIP || emote == Emote.SPIN;
        switch (emote) {
            case BACKFLIP, FRONTFLIP -> {
                double phase = ease((t - .16) / .68);
                double tuck = Math.pow(Math.sin(phase * Math.PI), 2);
                double prepare = Math.pow(Math.sin(Math.PI * ease(t / .2)), 2);
                double land = Math.pow(Math.sin(Math.PI * ease((t - .8) / .2)), 2);
                root = rotation((emote == Emote.BACKFLIP ? 360 : -360) * phase, 0, 0);
                lift = (float) (1.05 * tuck - .07 * (prepare + land));
                right = rotation(-90 * tuck + 25 * prepare, 0, 15);
                left = rotation(-90 * tuck + 25 * prepare, 0, -15);
                rightLeg = rotation(-70 * tuck + 12 * (prepare + land), 0, 0);
                leftLeg = rightLeg;
            }
            case WAVE -> {
                right = rotation(-150, 0, -25 + 16 * Math.sin(t * Math.TAU * 3));
                head = rotation(0, -8, 6);
            }
            case CLAP -> {
                double close = .5 - .5 * Math.cos(t * Math.TAU * 4);
                right = rotation(-78, -40 + 35 * close, 5);
                left = rotation(-78, 40 - 35 * close, -5);
                head = rotation(3 + 2 * close, 0, 0);
            }
            case DAB -> {
                right = rotation(-125, -35, 35);
                left = rotation(-12, 0, -128);
                head = rotation(30, -20, 25);
                root = rotation(0, 0, 8);
            }
            case BOW -> {
                double bend = ease(t / .32) * (1 - ease((t - .58) / .42));
                root = rotation(-38 * bend, 0, 0);
                head = rotation(10 * bend, 0, 0);
                rightLeg = rotation(-38 * bend, 0, 0);
                leftLeg = rightLeg;
            }
            case SALUTE -> {
                right = rotation(-140, 0, 8);
                head = rotation(-5, 0, 0);
            }
            case DANCE -> {
                root = rotation(0, 14 * wave, 4 * wave);
                right = rotation(-70 + 35 * wave, 0, 25 + 20 * beat);
                left = rotation(-70 - 35 * wave, 0, -25 + 20 * beat);
                rightLeg = rotation(24 * wave, 0, 0);
                leftLeg = rotation(-24 * wave, 0, 0);
                head = rotation(0, -12 * wave, 0);
                lift = (float) (.025 * (1 - Math.cos(t * Math.TAU * 6)));
            }
            case SPIN -> {
                root = rotation(0, 360 * ease(t), 0);
                right = rotation(0, 0, 85);
                left = rotation(0, 0, -85);
                rightLeg = rotation(0, 0, 5);
                leftLeg = rotation(0, 0, -5);
            }
            case CELEBRATE -> {
                double jump = .5 - .5 * Math.cos(t * Math.TAU * 2);
                lift = (float) (.4 * jump);
                right = rotation(-165, 0, -30);
                left = rotation(-165, 0, 30);
                rightLeg = rotation(16 * jump, 0, 8 * jump);
                leftLeg = rotation(-16 * jump, 0, -8 * jump);
            }
            case FACEPALM -> {
                right = rotation(-142, -20, 35);
                head = rotation(16, 6 * Math.sin(t * Math.TAU * 2), 0);
            }
            case SIT -> {
                lift = (float) (-.72 * fade);
                rightLeg = rotation(-90, 0, 4);
                leftLeg = rotation(-90, 0, -4);
                right = rotation(-12, 0, 8);
                left = rotation(-12, 0, -8);
                head = rotation(5, 0, 0);
            }
            case HANDSHAKE -> { right=rotation(-62+8*Math.sin(t*Math.TAU*3),-12,8);head=rotation(4,0,0); }
            case POINT -> { right=rotation(-92,-24,-6);head=rotation(-3,-18,0);left=rotation(-8,0,-8); }
            case THINK -> { right=rotation(-136,26,18);head=rotation(12,-8,9);root=rotation(0,-4,0); }
            case SHRUG -> { right=rotation(-18,-12,65);left=rotation(-18,12,-65);head=rotation(-6,0,4*Math.sin(t*Math.TAU)); }
            case YES -> { head=rotation(15*Math.sin(t*Math.TAU*2),0,0);right=rotation(0,0,5); }
            case NO -> { head=rotation(0,25*Math.sin(t*Math.TAU*2),0);left=rotation(0,0,-5); }
            case SHUSH -> { right=rotation(-141,-14,19);head=rotation(-3,6,0); }
            case CHEER -> { right=rotation(-156,0,-18);left=rotation(-145,0,24);root=rotation(0,6*wave,3*wave);head=rotation(-8,0,0); }
            case DISCO -> { right=rotation(-105-55*wave,0,30);left=rotation(-105+55*wave,0,-30);root=rotation(0,16*wave,7*wave);rightLeg=rotation(20*wave,0,0);leftLeg=rotation(-20*wave,0,0); }
            case SHUFFLE -> { rightLeg=rotation(32*beat,0,6);leftLeg=rotation(-32*beat,0,-6);right=rotation(-35-20*beat,0,12);left=rotation(-35+20*beat,0,-12);root=rotation(0,10*wave,0);lift=(float)(.035*(1-Math.cos(t*Math.TAU*6))); }
            case GROOVE -> { root=rotation(4*wave,18*wave,9*Math.sin(t*Math.TAU*3));right=rotation(-55+22*wave,15,35);left=rotation(-55-22*wave,-15,-35);head=rotation(0,-14*wave,0); }
            case CLAP_DANCE -> { double close=.5-.5*Math.cos(t*Math.TAU*6);right=rotation(-82,-42+35*close,5);left=rotation(-82,42-35*close,-5);rightLeg=rotation(20*wave,0,0);leftLeg=rotation(-20*wave,0,0);root=rotation(0,0,5*wave); }
            case SIDE_STEP -> { rightLeg=rotation(0,0,14+12*wave);leftLeg=rotation(0,0,-14+12*wave);root=rotation(0,8*wave,7*wave);right=rotation(-35,0,30+18*wave);left=rotation(-35,0,-30+18*wave);lift=(float)(.025*(1-Math.cos(t*Math.TAU*4))); }
            case WAVE_DANCE -> { right=rotation(-90-65*wave,0,35+15*beat);left=rotation(-90+65*wave,0,-35+15*beat);root=rotation(0,10*beat,6*wave);head=rotation(-5,0,-5*wave); }
            case BOUNCE -> { double bounce=.5-.5*Math.cos(t*Math.TAU*4);lift=(float)(.18*bounce);right=rotation(-55-35*bounce,0,25);left=rotation(-55-35*bounce,0,-25);rightLeg=rotation(-18*bounce,0,4);leftLeg=rotation(-18*bounce,0,-4); }
            case VICTORY -> { right=rotation(-170,-12,-15);left=rotation(-25,28,-30);head=rotation(-12,-12,0);root=rotation(0,12,5);rightLeg=rotation(0,0,8); }
            case HAPPY_SPIN -> { root=rotation(0,360*ease(t),0);right=rotation(-145,0,-35);left=rotation(-145,0,35);lift=(float)(.06*Math.pow(Math.sin(Math.PI*t),2));fullTurn=true; }
            case HEART -> { right=rotation(-105,-35,25);left=rotation(-105,35,-25);head=rotation(4,0,8); }
            case BLOW_KISS -> { double reach=ease((t-.35)/.3)*(1-ease((t-.78)/.22));right=rotation(-145+55*reach,-12+12*reach,20-15*reach);head=rotation(-4,0,4);left=rotation(0,0,-10); }
            case HUG -> { double close=.5-.5*Math.cos(t*Math.TAU);right=rotation(-88,-50+15*close,12);left=rotation(-88,50-15*close,-12);head=rotation(6,0,5); }
            case SHY -> { head=rotation(24,-12,8);right=rotation(-20,-8,7);left=rotation(-20,8,-7);root=rotation(0,8,3); }
            case THANK_YOU -> { double bend=12*Math.pow(Math.sin(t*Math.PI),2);root=rotation(-bend,0,0);right=rotation(-101,-33,12);left=rotation(-101,33,-12);rightLeg=rotation(-bend,0,0);leftLeg=rightLeg;head=rotation(8,0,0); }
            case FLATTERED -> { right=rotation(-80,-35,22);left=rotation(-135,15,-18);head=rotation(7,-6,12);root=rotation(0,0,3); }
            case DEVOTION -> { right=rotation(-82,-32,20);left=rotation(-48,25,-28);head=rotation(9,8,0); }
            case HAND_HEART -> { right=rotation(-155,-18,25);left=rotation(-155,18,-25);head=rotation(-7,0,0); }
            case HEART_PULSE -> { double pulse=Math.sin(t*Math.TAU*3);right=rotation(-98-6*pulse,-32,20);left=rotation(-98-6*pulse,32,-20);head=rotation(3,0,6);lift=(float)(.012*(1-Math.cos(t*Math.TAU*3))); }
            case WELCOME -> { right=rotation(-45,-20,48);left=rotation(-45,20,-48);head=rotation(-3,0,0);root=rotation(0,0,2*Math.sin(t*Math.PI)); }
            case WARM_WAVE -> { right=rotation(-122,0,-38+12*Math.sin(t*Math.TAU*3));head=rotation(5,0,12);root=rotation(0,-8,4); }
            case BASHFUL_BOW -> { double bend=19*Math.pow(Math.sin(t*Math.PI),2);root=rotation(-bend,5,0);head=rotation(17,-12,8);right=rotation(-18,-10,7);left=rotation(-18,10,-7);rightLeg=rotation(-bend,0,0);leftLeg=rightLeg; }
            case RELAX -> { right=rotation(-156,-35,42);left=rotation(-156,35,-42);head=rotation(-9,0,0);root=rotation(-4,0,0);rightLeg=rotation(-4,0,6);leftLeg=rotation(-4,0,-6); }
            case CONFUSED -> { head=rotation(8,10*Math.sin(t*Math.TAU),14*Math.sin(t*Math.TAU*2));right=rotation(-24,-12,35);left=rotation(-4,0,-8); }
            case SURPRISED -> { head=rotation(-15,0,0);right=rotation(-146,-8,13);left=rotation(-146,8,-13);root=rotation(-5,0,0); }
            case ANGRY -> { right=rotation(-98,-58,3);left=rotation(-98,58,-3);head=rotation(14,0,0);root=rotation(3,0,0); }
            case SAD -> { head=rotation(28,0,5);root=rotation(6,0,0);right=rotation(8,0,7);left=rotation(8,0,-7);rightLeg=rotation(6,0,0);leftLeg=rightLeg; }
            case SCARED -> { right=rotation(-138,-15,28);left=rotation(-138,15,-28);head=rotation(-10,8*wave,0);root=rotation(-6,0,2*beat); }
            case SLEEPY -> { head=rotation(20+10*Math.sin(t*Math.TAU),0,7);root=rotation(0,0,4*Math.sin(t*Math.TAU));right=rotation(4,0,6); }
            case IMPATIENT -> { head=rotation(0,-15,0);right=rotation(-20,-25,25);left=rotation(-20,25,-25);rightLeg=rotation(10*(.5-.5*Math.cos(t*Math.TAU*5)),0,5);leftLeg=rotation(0,0,-5); }
            case PROUD -> { right=rotation(-22,-35,32);left=rotation(-22,35,-32);head=rotation(-12,0,0);root=rotation(-3,0,0); }
            case DISAPPOINTED -> { head=rotation(30,-8,0);right=rotation(-28,0,15);left=rotation(-28,0,-15);root=rotation(5,0,0); }
            case BORED -> { right=rotation(-137,22,20);head=rotation(16,0,18);root=rotation(0,0,6);left=rotation(3,0,-8); }
            case CARTWHEEL -> { double arc=Math.pow(Math.sin(t*Math.PI),2);root=rotation(0,0,360*ease(t));lift=(float)(.25*arc);right=rotation(-175,0,0);left=right;rightLeg=rotation(0,0,28*arc);leftLeg=rotation(0,0,-28*arc);fullTurn=true; }
            case HANDSTAND -> { double hold=ease(t/.3)*(1-ease((t-.7)/.3));root=rotation(180*hold,0,0);right=rotation(-178*hold,0,5);left=rotation(-178*hold,0,-5);rightLeg=rotation(0,0,9*hold);leftLeg=rotation(0,0,-9*hold); }
            case SIDEFLIP -> { double phase=ease((t-.15)/.7),arc=Math.pow(Math.sin(phase*Math.PI),2);root=rotation(0,0,360*phase);lift=(float)(.9*arc);right=rotation(-65*arc,0,35);left=rotation(-65*arc,0,-35);rightLeg=rotation(-45*arc,0,0);leftLeg=rightLeg;fullTurn=true; }
            case HEEL_CLICK -> { double jump=Math.pow(Math.sin(t*Math.PI),2);lift=(float)(.4*jump);right=rotation(-12,0,75*jump);left=rotation(-12,0,-75*jump);rightLeg=rotation(-15*jump,0,-16*jump);leftLeg=rotation(15*jump,0,16*jump); }
            case WINDMILL -> { root=rotation(0,360*ease(t),0);right=rotation(65*Math.sin(t*Math.TAU*3),0,90);left=rotation(-65*Math.sin(t*Math.TAU*3),0,-90);head=rotation(0,0,4*wave);fullTurn=true; }
            case ROBOT -> { double step=Math.tanh(Math.sin(t*Math.TAU*3)*2);right=rotation(-75-30*step,-25,35);left=rotation(-75+30*step,25,-35);head=rotation(0,20*step,0);root=rotation(0,-12*step,0);rightLeg=rotation(12*step,0,0);leftLeg=rotation(-12*step,0,0); }
            case LEAN -> { double lean=18*Math.pow(Math.sin(t*Math.PI),2);root=rotation(0,0,lean);rightLeg=rotation(0,0,-lean);leftLeg=rightLeg;right=rotation(-18,0,35);left=rotation(-18,0,-15);head=rotation(0,0,-6); }
            case STAR_JUMP -> { double jump=Math.pow(Math.sin(t*Math.PI),2);lift=(float)(.6*jump);right=rotation(0,0,115*jump);left=rotation(0,0,-115*jump);rightLeg=rotation(0,0,28*jump);leftLeg=rotation(0,0,-28*jump);head=rotation(-8*jump,0,0); }
        }
        if (!fullTurn) {
            root = scale(root, (float) fade);
            if (emote != Emote.SIT) lift *= fade;
        }
        return new EmotePose(head, body, right, left, rightLeg, leftLeg, root, lift, (float) fade);
    }

    public EmotePose mirrored() {
        return new EmotePose(mirror(head),mirror(body),mirror(leftArm),mirror(rightArm),mirror(leftLeg),mirror(rightLeg),mirror(root),lift,weight);
    }

    public static EmotePose samplePair(PairEmote emote, double ticks, boolean second) {
        if (!Double.isFinite(ticks)) ticks = 0;
        var base = sample(emote.animation, ticks);
        double t = Math.clamp(ticks / emote.animation.duration, 0, 1), wave = Math.sin(t * Math.TAU * 3);
        Rotation head = base.head, right = base.rightArm, left = base.leftArm, root = base.root;
        switch (emote) {
            case HUG -> { right = rotation(-80, -35, 8); left = rotation(-80, 35, -8); head = rotation(4, 0, second ? -9 : 9); }
            case KISS -> { double lean = ease(t / .3) * (1 - ease((t - .7) / .3)); head = rotation(5, 0, second ? -10 : 10); right = rotation(-55, -18, 8); left = rotation(-55, 18, -8); root = rotation(5 * lean, 0, 0); }
            case HANDSHAKE -> { right = rotation(-65 + 4 * wave, -18, 6); left = rotation(0, 0, -3); head = rotation(3, 0, 0); }
            case HIGH_FIVE -> { right = rotation(-128 + 3 * wave, -8, 0); left = rotation(0, 0, -5); head = rotation(-6, 0, 0); }
            case DANCE -> { right = rotation(-65 + 3 * wave, -30, 8); left = rotation(-65 - 3 * wave, 30, -8); root = rotation(0, 3 * wave, 2 * wave); }
            case HEART -> { right = rotation(-100, -35, 12); left = rotation(-100, 35, -12); head = rotation(3, 0, second ? -5 : 5); }
        }
        var pose = new EmotePose(head, base.body, right, left, base.rightLeg, base.leftLeg, scale(root, base.weight), base.lift, base.weight);
        return second && emote != PairEmote.HANDSHAKE ? pose.mirrored() : pose;
    }

    private static Rotation mirror(Rotation value){return new Rotation(value.x,-value.y,-value.z);}

    static double ease(double value) {
        double t = Math.clamp(value, 0, 1);
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    static Rotation rootOffset(EmotePose from, EmotePose to) {
        return new Rotation(turnOffset(from.root.x, to.root.x), turnOffset(from.root.y, to.root.y), turnOffset(from.root.z, to.root.z));
    }

    private static float turnOffset(float from, float to) { return (float) (Math.rint((from - to) / Math.TAU) * Math.TAU); }

    static EmotePose mix(EmotePose from, EmotePose to, double progress, Rotation offset) {
        float t = (float) ease(progress);
        if (t <= 0) return from;
        if (t >= 1) return to;
        float first = from.weight * (1 - t), second = to.weight * t, weight = first + second;
        return new EmotePose(
                joint(from.head, to.head, first, second, weight), joint(from.body, to.body, first, second, weight),
                joint(from.rightArm, to.rightArm, first, second, weight), joint(from.leftArm, to.leftArm, first, second, weight),
                joint(from.rightLeg, to.rightLeg, first, second, weight), joint(from.leftLeg, to.leftLeg, first, second, weight),
                new Rotation(lerp(from.root.x, to.root.x + offset.x, t), lerp(from.root.y, to.root.y + offset.y, t), lerp(from.root.z, to.root.z + offset.z, t)),
                lerp(from.lift, to.lift, t), weight);
    }

    private static Rotation joint(Rotation from, Rotation to, float first, float second, float weight) {
        if (weight < .000001f) return Rotation.ZERO;
        return new Rotation((from.x * first + to.x * second) / weight, (from.y * first + to.y * second) / weight, (from.z * first + to.z * second) / weight);
    }

    private static Rotation scale(Rotation value, float scale) { return new Rotation(value.x * scale, value.y * scale, value.z * scale); }
    private static float lerp(float from, float to, float t) { return from + (to - from) * t; }
}
