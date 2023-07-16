package cn.make.module.combat;

import chad.phobos.Client;
import chad.phobos.api.center.Module;
import chad.phobos.api.events.network.PacketEvent;
import chad.phobos.api.events.player.UpdateWalkingPlayerEvent;
import chad.phobos.api.setting.Setting;
import cn.make.NotifyModule;
import cn.make.Targets;
import cn.make.util.skid.EntityUtil;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.function.Predicate;

public class SmartBowBomb extends Module {
	static EntityPlayer target;
	public SmartBowBomb() {
		super("SmartBowBomb", "SMART", Category.COMBAT);
		target = null;
	}
	enum Page {
		General,
		EzBow,
		SuperBow
	}
	public Setting<Page> page = rother("Page", Page.General);

	public Setting<Float> targetHP = rfloa("SuperBowMaxHP", 20.0f, 1.0f, 36.0f, general());
	public Setting<Double> targetRange = rdoub("targetMaxRange", 50.0, 1.0, 100.0, general());
	public Setting<Boolean> groundOnly = rbool("GroundOnly", true, general());
	public Setting<Boolean> lookAtEntity = rbool("LookEntity", false, general());
	public Setting<Boolean> release = rbool("AutoRelease", false, general());
	public Setting<Integer> activeTime = rinte("MinActiveTime", 4, 0, 20, general());
	public Setting<Boolean> debug = rbool("Debug", false, general());

	public Setting<Integer> spoofs = rinte("ShakeFreq", 80, 1, 100, ezbow());
	public Setting<Boolean> bypass = rbool("shakeUpFirst", false, ezbow());

	public Setting<Float> factor = rfloa("Factor", 14.8f, 1f, 20f, superbow());
	public Setting<Boolean> minimize = rbool("minimize", true, superbow());


	private Predicate general() {
		return v -> page.getValue() == Page.General;
	}

	private Predicate ezbow() {
		return v -> page.getValue() == Page.EzBow;
	}

	private Predicate superbow() {
		return v -> page.getValue() == Page.SuperBow;
	}

	@Override
	public String getDisplayInfo() {
		return genInfo();
	}

	@SubscribeEvent
	public void onWalkUpdate(UpdateWalkingPlayerEvent event) {
		if (fullNullCheck()) {
			target = null;
			return;
		}
		target = Targets.getTargetByRange(targetRange.getValue());
		if (target != null) {
			if(
				mc.player.getHeldItemMainhand().getItem() instanceof ItemBow
					&& mc.player.isHandActive()
			) {
				if (mc.player.getItemInUseMaxCount() > activeTime.getValue()) {
					if (lookAtEntity.getValue()) lookEntity(target);
					if (release.getValue()) mc.playerController.onStoppedUsingItem(mc.player);
				}
			}
		}

	}
	@Override
	public void onDisable() {
		target = null;
	}
	@SubscribeEvent
	public void onPacketSend(PacketEvent.Send event) {
		if (fullNullCheck()) {
			return;
		}
		if (groundOnly.getValue() && !mc.player.onGround) return;
		if (target == null) return;
		if (doEZBow(target)) {
			EZBOW.onSendDo(event, bypass.getValue(), spoofs.getValue(), activeTime.getValue());
		} else {
			SUPERBOW.onSendDo(event, factor.getValue(), minimize.getValue(), activeTime.getValue());
		}
	}
	public boolean doEZBow(EntityPlayer player) {
		return EntityUtil.getHealth(player) > targetHP.getValue();
	}

	private static class EZBOW {

		public static void onSendDo(PacketEvent.Send event, boolean bbypass, int sspoofs, int acttime) {
			if (event.getStage() != 0) return;

			if (event.getPacket() instanceof CPacketPlayerDigging) {
				CPacketPlayerDigging packet = event.getPacket();

				if (packet.getAction() == CPacketPlayerDigging.Action.RELEASE_USE_ITEM) {
					ItemStack handStack = mc.player.getHeldItem(EnumHand.MAIN_HAND);
					if (
						!handStack.isEmpty()
							&& handStack.getItem() instanceof ItemBow
						&& mc.player.getItemInUseMaxCount() >= acttime
					) {
						doSpoofs(bbypass, sspoofs);
					}
				}

			} else if (event.getPacket() instanceof CPacketPlayerTryUseItem) event.getPacket();
		}

		private static void doSpoofs(boolean bbypass, int sspoofs) {

			mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));

			for (int index = 0; index < sspoofs; ++index) {
				if (bbypass) {
					mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1e-10, mc.player.posZ, false));
					mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY - 1e-10, mc.player.posZ, true));
				} else {
					mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY - 1e-10, mc.player.posZ, true));
					mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1e-10, mc.player.posZ, false));
				}
			}
			NotifyModule.getNoti().pushString("EzBow to " + target.getName() + " with " + mc.player.getItemInUseMaxCount() + " actives!");
		}
	}

	private static class SUPERBOW {
		private static void spoof(double x, double y, double z, boolean ground) {
			mc.player.connection.sendPacket(new CPacketPlayer.Position(x, y, z, ground));
		}

		private static int getRuns(Float ffactor) {
			return 10 + (int) ((ffactor - 1));
		}

		private static void onSendDo(PacketEvent.Send event, Float ffactor, boolean mminimize, int acttime) {

			if (
				event.getPacket() instanceof CPacketPlayerDigging
					&& ((CPacketPlayerDigging) event.getPacket()).getAction() == CPacketPlayerDigging.Action.RELEASE_USE_ITEM
					&& mc.player.getActiveItemStack().getItem() == Items.BOW
				&& mc.player.getItemInUseMaxCount() >= acttime

			) {
				mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
				int spoofs = getRuns(ffactor);
				for (int i = 0; i < spoofs; i++) {
					spoof(mc.player.posX, mc.player.posY + 1e-10, mc.player.posZ, false);
					spoof(mc.player.posX, mminimize ? mc.player.posY : mc.player.posY - 1e-10, mc.player.posZ, true);
				}
				NotifyModule.getNoti().pushString("SuperBow to " + target.getName() + " with " + mc.player.getItemInUseMaxCount() + " actives!");
			} else {
				if (
					event.getPacket() instanceof CPacketPlayerTryUseItem
						&& ((CPacketPlayerTryUseItem) event.getPacket()).getHand() == EnumHand.MAIN_HAND
				) {
					mc.player.getHeldItemMainhand().getItem();
				}
			}
		}
	}
	public void lookEntity(EntityPlayer entity) {
		Client.rotationManager.lookAtEntity(entity);
		if (debug.getValue()) chatNotify("lookAt " + entity.getName());
	}
	private void chatNotify(String msg) {
		if (debug.getValue()) mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new TextComponentString(msg), 2);
	}

	public String genInfo() {
		if (target == null) return "waiting";
		String HP = getHP(target);
		String NAME = target.getName();
		String ACTIVE = genActiveInfo();
		return ChatFormatting.WHITE + HP + " " + NAME + " | " + ACTIVE;
	}
	public String getHP(EntityPlayer player) {
		return String.format("%.1f", EntityUtil.getHealth(player));
	}
	public ChatFormatting getcolore() {
		return doEZBow(target) ? ChatFormatting.RED : ChatFormatting.GREEN;
	}

	public String genActiveInfo() {
		int count = mc.player.getItemInUseMaxCount();
		if (
			count == 0 || !(mc.player.getHeldItemMainhand().getItem() instanceof ItemBow)
		) return "*";
		if (count < activeTime.getValue()) return getcolore() + "a.. " + ChatFormatting.WHITE + count;
		else return getcolore() + "r! " + ChatFormatting.WHITE + count;
	}
}
