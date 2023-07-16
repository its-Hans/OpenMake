package cn.make.module.misc;

import chad.phobos.api.events.network.PacketEvent;
import chad.phobos.api.center.Command;
import chad.phobos.api.center.Module;
import chad.phobos.api.setting.Setting;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.regex.Pattern;

public class BowGod
extends Module {
    private long lastShootTime;
    public final Setting<Boolean> debug = this.register(new Setting<>("Debug", false));
    public final Setting<Boolean> bypass = this.register(new Setting<>("Bypass", false));
    public final Setting<Integer> Timeout = this.register(new Setting<>("Timeout", 500, 0, 2000));
    private final Setting<String> spoofs = this.register(new Setting<>("Spoofs", "10"));
    public final Setting<Boolean> onlyGround = this.register(new Setting<>("OnlyGround", true));

    public BowGod() {
        super("EzBow", "super bow", Category.MISC);
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send send) {
        ItemStack itemStack;
        CPacketPlayerTryUseItem cPacketPlayerTryUseItem;
        if (BowGod.fullNullCheck()) {
            return;
        }
        if (send.getStage() != 0) {
            return;
        }
        if (send.getPacket() instanceof CPacketPlayerDigging) {
            ItemStack itemStack2;
            CPacketPlayerDigging cPacketPlayerDigging = send.getPacket();
            if (cPacketPlayerDigging.getAction() == CPacketPlayerDigging.Action.RELEASE_USE_ITEM && !(itemStack2 = BowGod.mc.player.getHeldItem(EnumHand.MAIN_HAND)).isEmpty()) {
                itemStack2.getItem();
                if (itemStack2.getItem() instanceof ItemBow) {
                    this.doSpoofs();
                    if (this.debug.getValue()) {
                        Command.sendMessage("trying to spoof");
                    }
                }
            }
        } else if (
            send.getPacket() instanceof CPacketPlayerTryUseItem
                && (cPacketPlayerTryUseItem = send.getPacket()).getHand() == EnumHand.MAIN_HAND
                && !(itemStack = BowGod.mc.player.getHeldItem(EnumHand.MAIN_HAND)).isEmpty()
        ) itemStack.getItem();

    }

    public static boolean isInteger(String string) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(string).matches();
    }

    @Override
    public void onEnable() {
        if (this.isEnabled()) {
            this.lastShootTime = System.currentTimeMillis();
        }
    }

    private void doSpoofs() {
        if (System.currentTimeMillis() - this.lastShootTime >= (long) this.Timeout.getValue()) {
            if (onlyGround.getValue() & !mc.player.onGround) {
                if (this.debug.getValue()) Command.sendMessage("noGround");
                return;
            }
            this.lastShootTime = System.currentTimeMillis();
            BowGod.mc.player.connection.sendPacket(new CPacketEntityAction(BowGod.mc.player, CPacketEntityAction.Action.START_SPRINTING));
            if (BowGod.isInteger(this.spoofs.getValue())) {
                for (int i = 0; i < Integer.parseInt(this.spoofs.getValue()); ++i) {
                    if (this.bypass.getValue()) {
                        BowGod.mc.player.connection.sendPacket(new CPacketPlayer.Position(BowGod.mc.player.posX, BowGod.mc.player.posY + 1.0E-10, BowGod.mc.player.posZ, false));
                        BowGod.mc.player.connection.sendPacket(new CPacketPlayer.Position(BowGod.mc.player.posX, BowGod.mc.player.posY - 1.0E-10, BowGod.mc.player.posZ, true));
                        continue;
                    }
                    BowGod.mc.player.connection.sendPacket(new CPacketPlayer.Position(BowGod.mc.player.posX, BowGod.mc.player.posY - 1.0E-10, BowGod.mc.player.posZ, true));
                    BowGod.mc.player.connection.sendPacket(new CPacketPlayer.Position(BowGod.mc.player.posX, BowGod.mc.player.posY + 1.0E-10, BowGod.mc.player.posZ, false));
                }
            }
            if (this.debug.getValue()) {
                Command.sendMessage("Spoofed");
            }
        }
    }
}

