package cn.make.module.render;

import chad.phobos.api.center.Module;
import chad.phobos.api.setting.Setting;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class fogColor extends Module {
    public fogColor() {
        super("CustomFog", "Change world something.", Category.RENDER, true, false, false);
    }

    Setting<Integer> red = register(new Setting("Red", 255, 0, 255));
    Setting<Integer> green = register(new Setting("Green", 255, 0, 255));
    Setting<Integer> blue = register(new Setting("Blue", 255, 0, 255));

    @SubscribeEvent
    public void fogColors(EntityViewRenderEvent.FogColors event) {
            event.setRed(red.getValue() / 255f);
            event.setGreen(green.getValue() / 255f);
            event.setBlue(blue.getValue() / 255f);
    }

    @SubscribeEvent
    public void fog_density(EntityViewRenderEvent.FogDensity event) {
            event.setDensity(0.0f);
            event.setCanceled(true);
    }

    int registered = 0;

    @Override
    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
        registered = 0;
    }

    @Override
    public void onUpdate() {
        if (registered == 0) {
            MinecraftForge.EVENT_BUS.register(this);
            registered = 1;
        }
    }
}
