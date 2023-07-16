package cn.make.xinbb;

import chad.phobos.api.center.Module;
import chad.phobos.api.events.network.PacketEvent;
import chad.phobos.api.setting.Setting;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.concurrent.LinkedBlockingDeque;

public class xinBowBomb extends Module {
    public Setting<ModeEn> Mode = rother("Mode", ModeEn.Maximum);
    public Setting<exploitEn> exploit = rother("Exploit", exploitEn.Strong);
    public Setting<Float> factor = rfloa("Factor", 1f, 1f, 20f);
    public Setting<Boolean> minimize = rbool("Minimize", false);
    public Setting<Integer> delay = rinte("DELAY", 100, 0, 10000);
    public xinBowBomb() {
        super("XINBOWBOMB", "HARD", Category.PLAYER);
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketPlayerDigging
            && ((CPacketPlayerDigging) event.getPacket()).getAction() == CPacketPlayerDigging.Action.RELEASE_USE_ITEM
            && mc.player.getActiveItemStack().getItem() == Items.BOW) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
            getSpoofs(exploit.getDefaultValue(), Mode.getValue(), minimize.getValue(), delay.getValue(), event.getPacket());
            event.setCanceled(true);
            this.disable();
        }
    }

    private int getRuns(ModeEn m) {
        if (m == ModeEn.Factorised) {
            return 10 + (int) ((factor.getValue() - 1));
        }
        if (m == ModeEn.Normal) {
            return (int) Math.floor(factor.getValue());
        }
        if (m == ModeEn.Maximum) {
            return (int) (30f * factor.getValue());
        }
        return 1;
    }
    public DelaySpoofs getSpoofs(exploitEn exploit, ModeEn mode, boolean minimIze, int delay, CPacketPlayerDigging packet) {
        final int runs = getRuns(mode);
        DelaySpoofs ds;
        switch (exploit) {
            case Fast: {
                LinkedBlockingDeque<DelaySpoofs.SPOOF> deque = new LinkedBlockingDeque<>();
                for (int i = 0; i < runs; i++) {
                    deque.add(new DelaySpoofs.SPOOF(true, new Vec3d(mc.player.posX, minimIze ? mc.player.posY : mc.player.posY - 1e-10, mc.player.posZ), false, new Vec3d(mc.player.posX, mc.player.posY + 1e-10, mc.player.posZ)));
                }
                ds = new DelaySpoofs(deque, delay, packet);
                break;
            }
            case Strong: {
                LinkedBlockingDeque<DelaySpoofs.SPOOF> deque = new LinkedBlockingDeque<>();
                for (int i = 0; i < runs; i++) {
                    deque.add(new DelaySpoofs.SPOOF(false, new Vec3d(mc.player.posX, mc.player.posY + 1e-10, mc.player.posZ), true, new Vec3d(mc.player.posX, minimIze ? mc.player.posY : mc.player.posY - 1e-10, mc.player.posZ)));
                }
                ds = new DelaySpoofs(deque, delay, packet);
                break;
            }
            case Phobos: {
                LinkedBlockingDeque<DelaySpoofs.SPOOF> deque = new LinkedBlockingDeque<>();
                for (int i = 0; i < runs; i++) {
                    deque.add(new DelaySpoofs.SPOOF(true, new Vec3d(mc.player.posX, mc.player.posY + 0.00000000000013, mc.player.posZ), false, new Vec3d(mc.player.posX, mc.player.posY + 0.00000000000027, mc.player.posZ)));
                }
                ds = new DelaySpoofs(deque, delay, packet);
                break;
            }
            default: {
                ds = new DelaySpoofs(new LinkedBlockingDeque<>(), 0, packet);
                break;
            }
        }
        return ds;
    }

    public enum exploitEn {
        Strong, Fast, Phobos
    }

    public enum ModeEn {
        Normal, Maximum, Factorised
    }
}