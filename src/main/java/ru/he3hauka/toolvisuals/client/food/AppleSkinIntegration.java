package ru.he3hauka.toolvisuals.client.food;

import squeek.appleskin.api.AppleSkinApi;
import squeek.appleskin.api.event.*;

import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;
import static ru.he3hauka.toolvisuals.client.render.filter.RenderOption.*;

public final class AppleSkinIntegration implements AppleSkinApi {
    @Override public void registerEvents(){
        HUDOverlayEvent.Saturation.EVENT.register(event->FoodVisuals.foodOverlay(event,APPLE_SATURATION));
        HUDOverlayEvent.Exhaustion.EVENT.register(event->FoodVisuals.foodOverlay(event,APPLE_EXHAUSTION));
        HUDOverlayEvent.HungerRestored.EVENT.register(event->FoodVisuals.foodOverlay(event,APPLE_HUNGER_PREVIEW));
        HUDOverlayEvent.HealthRestored.EVENT.register(event->{if(!FoodVisuals.hud(APPLE_HEALTH_PREVIEW,HEALTH))event.isCanceled=true;});
        TooltipOverlayEvent.Pre.EVENT.register(event->{if(!FoodVisuals.tooltips())event.isCanceled=true;});
        TooltipOverlayEvent.Render.EVENT.register(event->{if(!FoodVisuals.tooltips())event.isCanceled=true;});
    }
}
