package ru.he3hauka.toolvisuals.client.hand;

import java.util.EnumMap;
import java.util.Map;
import ru.he3hauka.toolvisuals.client.config.ClientConfig;
import ru.he3hauka.toolvisuals.client.module.VisualSetting;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public enum HandProperty {
    X(HAND_X,LEFT_HAND_X),Y(HAND_Y,LEFT_HAND_Y),Z(HAND_Z,LEFT_HAND_Z),SCALE(HAND_SCALE,LEFT_HAND_SCALE),
    ROTATION_X(HAND_ROTATION_X,LEFT_HAND_ROTATION_X),ROTATION_Y(HAND_ROTATION_Y,LEFT_HAND_ROTATION_Y),ROTATION_Z(HAND_ROTATION_Z,LEFT_HAND_ROTATION_Z),
    STRETCH_X(HAND_STRETCH_X,LEFT_HAND_STRETCH_X),STRETCH_Y(HAND_STRETCH_Y,LEFT_HAND_STRETCH_Y),STRETCH_Z(HAND_STRETCH_Z,LEFT_HAND_STRETCH_Z),
    HIDE(HAND_HIDE,LEFT_HAND_HIDE),NO_EQUIP(HAND_NO_EQUIP,LEFT_HAND_NO_EQUIP),NO_SWING(HAND_NO_SWING,LEFT_HAND_NO_SWING);

    public final VisualSetting right,left;
    HandProperty(VisualSetting right,VisualSetting left){this.right=right;this.left=left;}
    public VisualSetting setting(HandSide side){return side==HandSide.RIGHT?right:left;}

    public static void change(ClientConfig config,HandSide side,Map<HandProperty,Double> changes) {
        var values=new EnumMap<VisualSetting,Double>(VisualSetting.class);
        changes.forEach((property,value)->values.put(property.setting(side),value));
        config.setValues(values,"custom_hand:"+side.name());
    }

    public static void reset(ClientConfig config,HandSide side) {
        var values=new EnumMap<VisualSetting,Double>(VisualSetting.class);
        for(var property:values()) { var setting=property.setting(side);values.put(setting,setting.initial); }
        config.setValues(values,"custom_hand:reset");
    }

    public static void copy(ClientConfig config,HandSide source,boolean mirror) {
        var values=new EnumMap<VisualSetting,Double>(VisualSetting.class);
        for(var property:values()) {
            double value=config.value(property.setting(source));
            if(mirror&&(property==X||property==ROTATION_Y||property==ROTATION_Z))value=-value;
            values.put(property.setting(source.opposite()),value);
        }
        config.setValues(values,"custom_hand:copy");
    }
}
