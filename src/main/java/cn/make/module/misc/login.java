package cn.make.module.misc;

import chad.phobos.api.center.Command;
import chad.phobos.api.center.Module;
import chad.phobos.api.setting.Setting;
import net.minecraft.network.play.client.CPacketChatMessage;

public class login
        extends Module {
    private Setting<String> command = this.register(new Setting<String>("Command", "/login"));
    private Setting<String> kitname = this.register(new Setting<String>("KitName", "idc"));
    private Setting<Boolean> debug = this.register(new Setting("debug", false));

    public login() {
        super("Login", "L", Category.MISC, true, false, false);
    }

    @Override
    public void onEnable() {
        String message = this.command.getValue() + " " + this.kitname.getValue();
        login.mc.player.connection.sendPacket(new CPacketChatMessage(message));
        if (debug.getValue()) Command.sendMessage("send " + message);
        this.disable();
    }
}

