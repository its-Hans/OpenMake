package cn.make.module.misc;

import chad.phobos.api.events.network.PacketEvent;
import chad.phobos.api.center.Module;
import chad.phobos.api.setting.Setting;
import chad.phobos.asm.accessors.ICPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class OffGroundSpoof extends Module {
    private boolean isOnGround = false;
    public Setting<Boolean> grPacket = rbool("GroundPacket", true);
    public OffGroundSpoof() {
        super("OffGround", "Packet AntiPistonKick", Category.PLAYER);
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {

        if (event.getPacket() instanceof CPacketPlayer) {
            CPacketPlayer player = event.getPacket();
            boolean ground = mc.player.onGround;
            if (
                isOnGround
                    && ground
                    && player.getY(0.0) == (!mc.player.isSprinting() ? 0.0 : mc.player.posY)
            ) {
                if (grPacket.getValue()) {
                    ((ICPacketPlayer)player).setOnGround(false);
                } else {
                    mc.player.onGround = false;
                }
            }
            isOnGround = ground;
        }
    }
}