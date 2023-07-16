package cn.make.module.misc;

import chad.phobos.api.center.Command;
import chad.phobos.api.center.Module;
import chad.phobos.api.setting.Setting;
import net.minecraft.network.play.client.CPacketChatMessage;

public class kit
        extends Module {
    private Setting<String> command = this.register(new Setting<String>("Command", "kit"));
    private Setting<String> kitname = this.register(new Setting<String>("KitName", "speedrun"));
    private Setting<Boolean> debug = this.register(new Setting("debug", false));

    public kit() {
        super("Kit", "kit", Category.MISC, true, false, false);
    }

    @Override
    public void onEnable() {
        String message = "/" + this.command.getValue() + " " + this.kitname.getValue();
        kit.mc.player.connection.sendPacket(new CPacketChatMessage(message));
        if (debug.getValue()) Command.sendMessage("send " + message);
        this.disable();
    }
}

