package cn.make.module.misc;

import chad.phobos.api.center.Module;
import chad.phobos.api.setting.Setting;
import chad.phobos.api.events.network.PacketEvent;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ChatSuffix
extends Module {
    Setting<String> suffixSetting = this.register(new Setting<>("suffix", " | Make"));

    Setting<String> no1 = this.register(new Setting<>("skip1", " "));
    Setting<String> no2 = this.register(new Setting<>("skip2", "*"));
    Setting<String> no3 = this.register(new Setting<>("skip3", "-"));
    Setting<String> no4 = this.register(new Setting<>("skip4", "+"));
    Setting<String> no5 = this.register(new Setting<>("skip5", ";"));
    Setting<String> no6 = this.register(new Setting<>("skip6", ","));
    Setting<String> no7 = this.register(new Setting<>("skip7", "!"));
    Setting<String> no8 = this.register(new Setting<>("skip8", "@"));
    Setting<String> no9 = this.register(new Setting<>("skip9", "$"));
    Setting<String> no0 = this.register(new Setting<>("skip0", "%"));
    
    public ChatSuffix() {
        super("ChatSuffix", "suffix", Category.MISC, true, false, false);
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getStage() == 0 && event.getPacket() instanceof CPacketChatMessage) {
            CPacketChatMessage packet = event.getPacket();
            String message = packet.getMessage();
            if (message.startsWith("/")
                || message.startsWith(no0.getValue())
                || message.startsWith(no1.getValue())
                || message.startsWith(no2.getValue())
                || message.startsWith(no3.getValue())
                || message.startsWith(no4.getValue())
                || message.startsWith(no5.getValue())
                || message.startsWith(no6.getValue())
                || message.startsWith(no7.getValue())
                || message.startsWith(no8.getValue())
                || message.startsWith(no9.getValue())
            ) return;
            
            String suffix = message + suffixSetting.getValue();
            if (suffix.length() >= 256) suffix = suffix.substring(0, 256);
            packet.message = suffix;
        }
    }
}

