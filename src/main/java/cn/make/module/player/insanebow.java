package cn.make.module.player;

import chad.phobos.api.events.network.PacketEvent;
import chad.phobos.api.center.Command;
import chad.phobos.api.center.Module;
import chad.phobos.api.setting.Setting;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.*;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class insanebow extends Module {
    public static insanebow instance;
    private final Setting<Integer> fastbow = register(new Setting("fastbow", 3, 0, 20, "autorelease, if 0 dont do"));
    private final Setting<Integer> bowbomb = register(new Setting("bowbomb", 0, 0, 100, "32kbow, if 0 dont do"));
    private final Setting<Boolean> quiver = register(new Setting("quiver", true, "shoot arrow at sky"));

    public insanebow() {
        super("insaneBow", "holy", Category.PLAYER, true, false, false);
        instance = this;
    }

    @Override
    public void onUpdate() {
        if (fullNullCheck()) return;
        if (
            mc.player.inventory.getCurrentItem().getItem() instanceof ItemBow
                && mc.player.isHandActive()
                && mc.player.getItemInUseMaxCount() >= fastbow.getValue()
        ) {
            if (quiver.getValue()) mc.player.connection.sendPacket(new CPacketPlayer.Rotation(mc.player.cameraYaw, -90f, mc.player.onGround));
            if (fastbow.getValue() != 0) mc.playerController.onStoppedUsingItem(mc.player);
            Command.sendMessage("shoot bow");
        }
    }

    boolean onlyground = true;

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (bowbomb.getValue() == 0) return;
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
        if (onlyground & !mc.player.onGround) return;
        mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
        for (int index = 0; index < bowbomb.getValue(); ++index) {
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY - 1e-10, mc.player.posZ, true));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1e-10, mc.player.posZ, false));
        }
    }
}
