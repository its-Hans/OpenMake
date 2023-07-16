package chad.phobos.api.center;

import chad.phobos.modules.client.HUD;
import chad.phobos.api.setting.Bind;
import chad.phobos.api.setting.Setting;
import cn.make.Notifiction;
import cn.make.NotifyModule;
import com.mojang.realmsclient.gui.ChatFormatting;
import chad.phobos.Client;
import chad.phobos.api.events.client.ClientEvent;
import chad.phobos.api.events.render.Render2DEvent;
import chad.phobos.api.events.render.Render3DEvent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;

import java.util.function.Predicate;

public class Module
        extends Feature {
    private final String description;
    private final Category category;
    public Setting<Boolean> enabled = this.register(new Setting<Boolean>("Enabled", false));
    public Setting<Boolean> drawn = this.register(new Setting<Boolean>("Drawn", true));
    public Setting<Bind> bind = this.register(new Setting<Bind>("Keybind", new Bind(-1)));
    public Setting<String> displayName;
    public boolean hasListener;
    public boolean alwaysListening;
    public boolean hidden;
    public float arrayListOffset = 0.0f;
    public float arrayListVOffset = 0.0f;
    public float offset;
    public float vOffset;
    public boolean sliding;

    public Module(String name, String description, Category category, boolean hasListener, boolean hidden, boolean alwaysListening) {
        super(name);
        this.displayName = this.register(new Setting<>("DisplayName", name));
        this.description = description;
        this.category = category;
        this.hasListener = hasListener;
        this.hidden = hidden;
        this.alwaysListening = alwaysListening;
    }
    public Module(String name, String description, Category category) {
        super(name);
        this.displayName = this.register(new Setting<>("DisplayName", name));
        this.description = description;
        this.category = category;
        this.hasListener = true;
        this.hidden = false;
        this.alwaysListening = false;
    }

    public Setting rother(String settingname, Object defaultVal) {
        return register(new Setting(settingname, defaultVal));
    }
    public Setting rother(String settingname, Object defaultVal, Predicate<?> visibleat) {
        return register(new Setting(settingname, defaultVal, visibleat));
    }
    public Setting<Bind> rbind(String settingname, Bind defaultVal) {
        return register(new Setting<Bind>(settingname, defaultVal));
    }
    public Setting<Bind> rbind(String settingname, Bind defaultVal, Predicate<Bind> visibleat) {
        return register(new Setting<Bind>(settingname, defaultVal, visibleat));
    }

    public Setting<Boolean> rbool(String settingname, Boolean defaultVal) {
        return register(new Setting(settingname, defaultVal));
    }
    public Setting<Boolean> rbool(String settingname, Boolean defaultVal, Predicate<?> visibleat) {
        return register(new Setting(settingname, defaultVal, visibleat));
    }

    public Setting<String> rstri(String settingname, String defaultVal) {
        return register(new Setting(settingname, defaultVal));
    }
    public Setting<String> rstri(String settingname, String defaultVal, Predicate<?> visibleat) {
        return register(new Setting(settingname, defaultVal, visibleat));
    }

    public Setting<Integer> rinte(String settingname, Integer defaultVal, Integer min, Integer max) {
        return register(new Setting(settingname, defaultVal, min, max));
    }
    public Setting<Integer> rinte(String settingname, Integer defaultVal, Integer min, Integer max, Predicate<?> visibleat) {
        return register(new Setting(settingname, defaultVal, min, max, visibleat));
    }

    public Setting<Double> rdoub(String settingname, Double defaultVal, Double min, Double max) {
        return register(new Setting(settingname, defaultVal, min, max));
    }
    public Setting<Double> rdoub(String settingname, Double defaultVal, Double min, Double max, Predicate<?> visibleat) {
        return register(new Setting(settingname, defaultVal, min, max, visibleat));
    }

    public Setting<Float> rfloa(String settingname, Float defaultVal, Float min, Float max) {
        return register(new Setting(settingname, defaultVal, min, max));
    }
    public Setting<Float> rfloa(String settingname, Float defaultVal, Float min, Float max, Predicate<?> visibleat) {
        return register(new Setting(settingname, defaultVal, min, max, visibleat));
    }

    public boolean isSliding() {
        return this.sliding;
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public void onToggle() {
    }

    public void onLoad() {
    }

    public void onTick() {
    }

    public void onLogin() {
    }

    public void onLogout() {
    }

    public void onUpdate() {
    }

    public void onRender2D(Render2DEvent event) {
    }

    public void onRender3D(Render3DEvent event) {
    }

    public void onUnload() {
    }

    public String getDisplayInfo() {
        return null;
    }

    public boolean isOn() {
        return this.enabled.getValue();
    }

    public boolean isOff() {
        return this.enabled.getValue() == false;
    }

    public void setEnabled(boolean enabled) {
        if (enabled) {
            this.enable();
        } else {
            this.disable();
        }
    }

    public void enable() {
        this.enabled.setValue(Boolean.TRUE);
        this.onToggle();
        this.onEnable();
        if (HUD.getInstance().notifyToggles.getValue().booleanValue()) {
            TextComponentString text = new TextComponentString(Client.commandManager.getClientMessage() + " " + ChatFormatting.GREEN + this.getDisplayName() + " toggled on.");
            Module.mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(text, 1);
        }
        Notifiction noti = Notifiction.getNoti();
        if (noti.isEnabled()) {
            if (noti.listenModuleToggle.getValue()) {
                noti.putModuleToggle(true, this.getDisplayName());
            }
        }
        NotifyModule noti2 = NotifyModule.getNoti();
        if (noti2.isEnabled()) {
            noti2.pushEnable(this.getDisplayName());
        }
        if (this.isOn() && this.hasListener && !this.alwaysListening) {
            MinecraftForge.EVENT_BUS.register(this);
        }
    }

    public void disable() {
        if (this.hasListener && !this.alwaysListening) {
            MinecraftForge.EVENT_BUS.unregister(this);
        }
        this.enabled.setValue(false);
        if (HUD.getInstance().notifyToggles.getValue().booleanValue()) {
            TextComponentString text = new TextComponentString(Client.commandManager.getClientMessage() + " " + ChatFormatting.RED + this.getDisplayName() + " toggled off.");
            Module.mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(text, 1);
        }
        Notifiction noti = Notifiction.getNoti();
        if (noti.isEnabled()) {
            if (noti.listenModuleToggle.getValue()) {
                noti.putModuleToggle(false, this.getDisplayName());
            }
        }
        NotifyModule noti2 = NotifyModule.getNoti();
        if (noti2.isEnabled()) {
            noti2.pushDisable(this.getDisplayName());
        }
        this.onToggle();
        this.onDisable();
    }

    public void toggle() {
        ClientEvent event = new ClientEvent(!this.isEnabled() ? 1 : 0, this);
        MinecraftForge.EVENT_BUS.post(event);
        if (!event.isCanceled()) {
            this.setEnabled(!this.isEnabled());
        }
    }

    public String getDisplayName() {
        return this.displayName.getValue();
    }
    public void sendModuleMessage(String message) {
        Command.sendSilentMessage(ChatFormatting.GRAY + "[" + ChatFormatting.RESET + getDisplayName() + ChatFormatting.GRAY + "] " + message);
    }

    public void setDisplayName(String name) {
        Module module = Client.moduleManager.getModuleByDisplayName(name);
        Module originalModule = Client.moduleManager.getModuleByName(name);
        if (module == null && originalModule == null) {
            Command.sendMessage(this.getDisplayName() + ", name: " + this.getName() + ", has been renamed to: " + name);
            this.displayName.setValue(name);
            return;
        }
        Command.sendMessage(ChatFormatting.RED + "A module of this name already exists.");
    }

    public String getDescription() {
        return this.description;
    }

    public boolean isDrawn() {
        return this.drawn.getValue();
    }

    public void setDrawn(boolean drawn) {
        this.drawn.setValue(drawn);
    }

    public Category getCategory() {
        return this.category;
    }

    public String getInfo() {
        return null;
    }

    public Bind getBind() {
        return this.bind.getValue();
    }

    public void setBind(int key) {
        this.bind.setValue(new Bind(key));
    }

    public boolean listening() {
        return this.hasListener && this.isOn() || this.alwaysListening;
    }

    public String getFullArrayString() {
        return this.getDisplayName() + ChatFormatting.GRAY + (this.getDisplayInfo() != null ? " [" + ChatFormatting.WHITE + this.getDisplayInfo() + ChatFormatting.GRAY + "]" : "");
    }

    public void notiMessage(String message) {
        NotifyModule.getNoti().pushString(message);
    }

	public enum Category {
        COMBAT("Combat", "F"),
        MISC("Misc", "B"),
        RENDER("Render", "J"),
        MOVEMENT("Movement", "C"),
        PLAYER("Player", "H"),
        CLIENT("Client", "E");

        private final String name;
        private final String icon;

        Category(String name, String icon) {
            this.name = name;
            this.icon = icon;
        }

        public String getName() {
            return this.name;
        }

        public String getIcon() {
            return this.icon;
        }
    }
}

