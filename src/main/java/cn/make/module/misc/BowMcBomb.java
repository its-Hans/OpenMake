package cn.make.module.misc;

import chad.phobos.api.events.network.PacketEvent;
import chad.phobos.api.center.Module;
import chad.phobos.api.setting.Setting;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BowMcBomb extends Module {

    public BowMcBomb() {
        super("EzBow2", "BowMcBomb", Category.MISC);
    }

    public Setting<Integer> spoofs = this.register(new Setting<Integer>("Spoofs", 10, 1, 300));
    public Setting<Boolean> bypass = this.register(new Setting<Boolean>( "Bypass", false));
    private final Setting<Boolean> fastbow = this.register(new Setting<Boolean>("Release", false));
    public final Setting<Boolean> onlyGround = this.register(new Setting<>("OnlyGround", true));

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (
            fastbow.getValue()
                && mc.player.getHeldItemMainhand().getItem() instanceof ItemBow
                && mc.player.isHandActive() && mc.player.getItemInUseMaxCount() >= 3
        ) {
            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, mc.player.getHorizontalFacing()));
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(mc.player.getActiveHand()));
            mc.player.stopActiveHand();
        }

    }


    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getStage() != 0) return;

        if (event.getPacket() instanceof CPacketPlayerDigging) {
            CPacketPlayerDigging packet = event.getPacket();

            if (packet.getAction() == CPacketPlayerDigging.Action.RELEASE_USE_ITEM) {
                ItemStack handStack = mc.player.getHeldItem(EnumHand.MAIN_HAND);
                if (!handStack.isEmpty() &&handStack.getItem() instanceof ItemBow) doSpoofs();
            }

        } else if (event.getPacket() instanceof CPacketPlayerTryUseItem) event.getPacket();
    }

    private void doSpoofs() {
        if (onlyGround.getValue() & !mc.player.onGround) return;

        mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
            for (int index = 0; index < spoofs.getValue(); ++index) {
                if (bypass.getValue()) {
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1e-10, mc.player.posZ, false));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY - 1e-10, mc.player.posZ, true));
                } else {
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY - 1e-10, mc.player.posZ, true));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1e-10, mc.player.posZ, false));
                }
            }
    }

}