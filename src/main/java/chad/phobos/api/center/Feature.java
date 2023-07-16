package chad.phobos.api.center;

import chad.phobos.features.gui.OyVeyGui;
import chad.phobos.api.setting.Setting;
import chad.phobos.Client;
import chad.phobos.api.managers.TextManager;
import cn.make.newgui.PhobosGui;

import java.util.ArrayList;
import java.util.List;

public class Feature
        implements Util {
    public List<Setting> settings = new ArrayList<Setting>();
    public TextManager renderer = Client.textManager;
    private String name;

    public Feature() {
    }

    public Feature(String name) {
        this.name = name;
    }

    public static boolean nullCheck() {
        return Feature.mc.player == null;
    }

    public static boolean fullNullCheck() {
        return Feature.mc.player == null || Feature.mc.world == null;
    }

    public String getName() {
        return this.name;
    }

    public List<Setting> getSettings() {
        return this.settings;
    }

    public boolean hasSettings() {
        return !this.settings.isEmpty();
    }

    public boolean isEnabled() {
        if (this instanceof Module) {
            return ((Module) this).isOn();
        }
        return false;
    }

    public boolean isDisabled() {
        return !this.isEnabled();
    }

    public Setting register(Setting setting) {
        setting.setFeature(this);
        this.settings.add(setting);
        if (this instanceof Module) {
            Module m = (Module) this;
            if (Feature.mc.currentScreen instanceof OyVeyGui) {
                OyVeyGui.getInstance().updateModule(m);
            }
            if (Feature.mc.currentScreen instanceof PhobosGui) {
                PhobosGui.getInstance().updateModule(m);
            }
        }
        return setting;
    }

    public void unregister(Setting settingIn) {
        ArrayList<Setting> removeList = new ArrayList<Setting>();
        for (Setting setting : this.settings) {
            if (!setting.equals(settingIn)) continue;
            removeList.add(setting);
        }
        if (!removeList.isEmpty()) {
            this.settings.removeAll(removeList);
        }
        if (this instanceof Module) {
            Module m = (Module) this;
            if (Feature.mc.currentScreen instanceof OyVeyGui) {
                OyVeyGui.getInstance().updateModule(m);
            }
            if (Feature.mc.currentScreen instanceof PhobosGui) {
                PhobosGui.getInstance().updateModule(m);
            }
        }
    }

    public Setting getSettingByName(String name) {
        for (Setting setting : this.settings) {
            if (!setting.getName().equalsIgnoreCase(name)) continue;
            return setting;
        }
        return null;
    }

    public void reset() {
        for (Setting setting : this.settings) {
            setting.setValue(setting.getDefaultValue());
        }
    }

    public void clearSettings() {
        this.settings = new ArrayList<Setting>();
    }
}

