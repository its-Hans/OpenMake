package chad.phobos.modules.client;

import chad.phobos.Client;
import chad.phobos.api.center.Module;
import chad.phobos.api.events.client.ClientEvent;
import chad.phobos.api.setting.Setting;
import chad.phobos.features.gui.OyVeyGui;
import cn.make.newgui.PhobosGui;
import cn.make.tweaksClient;
import cn.make.util.skid.ColorUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.function.Predicate;

public class ClickGui
        extends Module {
    private static ClickGui INSTANCE = new ClickGui();

    enum GuiList{OyVey, Phobos}
    public Setting<GuiList> guiSelect = rother("GuiSelect", GuiList.Phobos);
    public Setting<Boolean> outline = this.register(new Setting("outline", true, v -> (guiSelect.getValue() != GuiList.OyVey)));
    public Setting<Integer> oRed = this.register(new Setting<Integer>("OutlineRed", 0, 0, 255, lambda1()));
    public Setting<Integer> oGreen = this.register(new Setting<Integer>("OutlineGreen", 0, 0, 255, lambda1()));
    public Setting<Integer> oBlue = this.register(new Setting<Integer>("OutlineBlue", 150, 0, 255, lambda1()));
    public Setting<Integer> oAlpha = this.register(new Setting<Integer>("OutlineAlpha", 240, 0, 255, lambda1()));
    public Setting<Integer> topAlpha = this.register(new Setting<Integer>("topAlpha", 150, 0, 255, lambda1()));
    public final Setting<Boolean> cleanGui = rbool("CleanGui", false);
    public Setting<Boolean> openCloseString = this.register(new Setting<Boolean>("openAndClose", true, v -> (guiSelect.getValue() != GuiList.OyVey)));
    public Setting<String> moduleButton = this.register(new Setting<Object>("Buttons:", "", v -> !this.openCloseString.getValue()));
    public Setting<String> openstring = this.register(new Setting("openString", "", lambda2()));
    public Setting<String> closestring = this.register(new Setting("closeString", "-", lambda2()));

    public Setting<Integer> red = this.register(new Setting<Integer>("Red", 0, 0, 255));
    public Setting<Integer> green = this.register(new Setting<Integer>("Green", 0, 0, 255));
    public Setting<Integer> blue = this.register(new Setting<Integer>("Blue", 255, 0, 255));
    public Setting<Integer> hoverAlpha = this.register(new Setting<Integer>("Alpha", 180, 0, 255));
    public Setting<Integer> topRed = this.register(new Setting<Integer>("SecondRed", 0, 0, 255));
    public Setting<Integer> topGreen = this.register(new Setting<Integer>("SecondGreen", 0, 0, 255));
    public Setting<Integer> topBlue = this.register(new Setting<Integer>("SecondBlue", 150, 0, 255));
    public Setting<Integer> alpha = this.register(new Setting<Integer>("HoverAlpha", 240, 0, 255));
    public Setting<Boolean> rainbow = this.register(new Setting<Boolean>("Rainbow", false));
    public Setting<rainbowMode> rainbowModeHud = this.register(new Setting<Object>("HRainbowMode", rainbowMode.Static, v -> this.rainbow.getValue()));
    public Setting<rainbowModeArray> rainbowModeA = this.register(new Setting<Object>("ARainbowMode", rainbowModeArray.Static, v -> this.rainbow.getValue()));
    public Setting<Integer> rainbowHue = this.register(new Setting<Object>("Delay", Integer.valueOf(240), Integer.valueOf(0), Integer.valueOf(600), v -> this.rainbow.getValue()));
    public Setting<Float> rainbowBrightness = this.register(new Setting<Object>("Brightness ", Float.valueOf(150.0f), Float.valueOf(1.0f), Float.valueOf(255.0f), v -> this.rainbow.getValue()));
    public Setting<Float> rainbowSaturation = this.register(new Setting<Object>("Saturation", Float.valueOf(150.0f), Float.valueOf(1.0f), Float.valueOf(255.0f), v -> this.rainbow.getValue()));

    public ClickGui() {
        super("ClickGui", "Opens the ClickGui", Module.Category.CLIENT, true, false, false);
        this.setInstance();
    }

    public static ClickGui getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ClickGui();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    public Predicate lambda1() {
        return v -> (
            guiSelect.getValue() != GuiList.OyVey
            && outline.getValue()
        );
    }
    public Predicate lambda2() {
        return v -> (
            guiSelect.getValue() == GuiList.Phobos
            && openCloseString.getValue()
        );
    }

    @SubscribeEvent
    public void onSettingChange(ClientEvent event) {
        if (event.getStage() == 2 && event.getSetting().getFeature().equals(this)) {
            if (event.getSetting() == this.guiSelect) {
                mc.displayGuiScreen(nowGUI());
            }
            Client.colorManager.setColor(this.red.getPlannedValue(), this.green.getPlannedValue(), this.blue.getPlannedValue(), this.hoverAlpha.getPlannedValue());
        }
    }

    @Override
    public void onEnable() {
        mc.displayGuiScreen(nowGUI());
    }
    @Override
    public void onDisable() {
        if (ClickGui.mc.currentScreen == nowGUI()) {
            mc.displayGuiScreen(null);
        }
    }

    @Override
    public void onLoad() {
        Client.colorManager.setColor(this.red.getValue(), this.green.getValue(), this.blue.getValue(), this.hoverAlpha.getValue());
    }

    @Override
    public void onTick() {
        if (ClickGui.mc.currentScreen != nowGUI()) {
            this.disable();
            if (tweaksClient.getInstance().autoSave.getValue()) {
                Client.configManager.saveCurrentConfig();
            }
        }
    }

    public Color getGuiColor() {
        return new Color(this.red.getValue(), this.green.getValue(), this.blue.getValue(), this.alpha.getValue());
    }

    public int getCurrentColorHex() {
        return ColorUtil.toARGB(this.red.getValue(), this.green.getValue(), this.blue.getValue(), this.alpha.getValue());
    }

    public enum rainbowModeArray {
        Static,
        Up

    }

    public enum rainbowMode {
        Static,
        Sideway

    }
    public GuiScreen nowGUI() {
        GuiScreen gui = null;
        switch (guiSelect.getValue()) {
            case OyVey: {
                gui = OyVeyGui.getClickGui();
                break;
            }
            case Phobos: {
                gui = PhobosGui.getClickGui();
                break;
            }
        }
        return gui;
    }
}

