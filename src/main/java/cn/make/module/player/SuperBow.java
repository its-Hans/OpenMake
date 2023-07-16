package cn.make.module.player;

//KONAS NOT REBIRTH!!
import cn.make.util.skid.Timer;
import chad.phobos.api.events.network.PacketEvent;
import chad.phobos.api.center.Module;
import chad.phobos.api.setting.Setting;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;

public class SuperBow extends Module {
    public static Timer delayTimer = new Timer();
    public Setting<Boolean> rotation = rbool("Rotation", false);
    public Setting<ModeEn> Mode = rother("Mode", ModeEn.Maximum);
    public Setting<exploitEn> exploit = rother("Exploit", exploitEn.Strong);
    public Setting<Float> factor = rfloa("Factor", 1f, 1f, 20f);
    public Setting<Boolean> minimize = rbool("Minimize", false);
    public Setting<Float> delay = rfloa("Delay", 5f, 0f, 10f);
    private final Random rnd = new Random();
    public SuperBow() {
        super("SuperSuperBow", "cwc", Category.PLAYER);
    }

    @SubscribeEvent
    protected void onPacketSend(PacketEvent.Send event) {
        if (fullNullCheck() || !delayTimer.passedMs((long) (delay.getValue() * 1000))) return;
        if (event.getPacket() instanceof CPacketPlayerDigging
            && ((CPacketPlayerDigging) event.getPacket()).getAction() == CPacketPlayerDigging.Action.RELEASE_USE_ITEM
            && mc.player.getActiveItemStack().getItem() == Items.BOW) {

            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
            double[] strict_direction = new double[] {
                100f * -Math.sin(Math.toRadians(mc.player.rotationYaw)),
                100f * Math.cos(Math.toRadians(mc.player.rotationYaw))
            };

            if (exploit.getValue() == exploitEn.Fast) {
                for (int i = 0; i < getRuns(); i++) {
                    spoof(mc.player.posX, minimize.getValue() ? mc.player.posY : mc.player.posY - 1e-10, mc.player.posZ, true);
                    spoof(mc.player.posX, mc.player.posY + 1e-10, mc.player.posZ, false);
                }
            }
            if (exploit.getValue() == exploitEn.Strong) {
                for (int i = 0; i < getRuns(); i++) {
                    spoof(mc.player.posX, mc.player.posY + 1e-10, mc.player.posZ, false);
                    spoof(mc.player.posX, minimize.getValue() ? mc.player.posY : mc.player.posY - 1e-10, mc.player.posZ, true);
                }
            }
            if (exploit.getValue() == exploitEn.Phobos) {
                for (int i = 0; i < getRuns(); i++) {
                    spoof(mc.player.posX, mc.player.posY + 0.00000000000013, mc.player.posZ, true);
                    spoof(mc.player.posX, mc.player.posY + 0.00000000000027, mc.player.posZ, false);
                }
            }
            if (exploit.getValue() == exploitEn.Strict) {
                for (int i = 0; i < getRuns(); i++) {
                    if (rnd.nextBoolean()) {
                        spoof(mc.player.posX - strict_direction[0], mc.player.posY, mc.player.posZ - strict_direction[1], false);
                    } else {
                        spoof(mc.player.posX + strict_direction[0], mc.player.posY, mc.player.posZ + strict_direction[1], true);
                    }
                }
            }

            delayTimer.reset();
        } else {
            if (event.getPacket() instanceof CPacketPlayerTryUseItem && ((CPacketPlayerTryUseItem) event.getPacket()).getHand() == EnumHand.MAIN_HAND) {
                mc.player.getHeldItemMainhand().getItem();
            }
        }
    }

    private void spoof(double x, double y, double z, boolean ground) {
        if (rotation.getValue()) {
            mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(x, y, z, mc.player.rotationYaw, mc.player.rotationPitch, ground));
        } else {
            mc.player.connection.sendPacket(new CPacketPlayer.Position(x, y, z, ground));
        }
    }

    private int getRuns() {
        if (Mode.getValue() == ModeEn.Factorised) {
            return 10 + (int) ((factor.getValue() - 1));
        }
        if (Mode.getValue() == ModeEn.Normal) {
            return (int) Math.floor(factor.getValue());
        }
        if (Mode.getValue() == ModeEn.Maximum) {
            return (int) (30f * factor.getValue());
        }
        return 1;
    }

    private enum exploitEn {
        Strong, Fast, Strict, Phobos
    }

    private enum ModeEn {
        Normal, Maximum, Factorised
    }
}