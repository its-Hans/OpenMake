package cn.make.module.player;

import cn.make.util.BlockChecker;
import cn.make.util.PacketCenter;
import cn.make.util.UtilsRewrite;
import cn.make.util.makeUtil;
import cn.make.util.skid.MathUtil;
import cn.make.util.skid.RebirthUtil;
import cn.make.util.skid.Timer;
import com.mojang.realmsclient.gui.ChatFormatting;
import chad.phobos.api.events.block.BlockEvent;
import chad.phobos.api.events.render.Render3DEvent;
import chad.phobos.api.center.Module;
import chad.phobos.api.setting.Setting;
import chad.phobos.api.utils.RenderUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;


public class MakePacketMiner extends Module {
	private final Setting<Boolean> silent = register(new Setting("Silent", true));
	private final Setting<Boolean> debug = register(new Setting("Debug", true));
	private final Setting<Boolean> autoRetry = register(new Setting<>("AutoRetry", true));
	private final Setting<Double> resetRange = register(new Setting("resetRange", 5.4, 1.0, 60));

	public BlockPos renderPos;
	public BlockPos breakPos;
	public EnumFacing breakFace;
	public Timer timer;
	public Timer instaTimer;
	public boolean readyToMine;
	public int oldSlot;
	long start;
	boolean swapBack;

	public MakePacketMiner() {
		super("idkMine", "Mine", Category.PLAYER);
		this.renderPos = null;
		this.breakPos = null;
		this.breakFace = null;
		this.readyToMine = false;
		this.timer = new Timer();
		this.instaTimer = new Timer();
		this.oldSlot = -1;
		this.start = -1L;
	}

	public static int bestSlot(final BlockPos pos) {
		int best = 0;
		double max = 0.0;
		for (int i = 0; i < 9; ++i) {
			final ItemStack stack = MakePacketMiner.mc.player.inventory.getStackInSlot(i);
			if (!stack.isEmpty()) {
				float speed = stack.getDestroySpeed(MakePacketMiner.mc.world.getBlockState(pos));
				if (speed > 1.0f) {
					final int eff;
					speed += (float) (((eff = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, stack)) > 0) ? (Math.pow(eff, 2.0) + 1.0) : 0.0);
					if (speed > max) {
						max = speed;
						best = i;
					}
				}
			}
		}
		return best;
	}

	public static boolean canBlockBeBroken(final BlockPos pos) {
		final IBlockState blockState = MakePacketMiner.mc.world.getBlockState(pos);
		//final Block block = blockState.getBlock();
		//return block.getBlockHardness(blockState, RebirthMine.mc.world, pos) != -1.0f;
		return blockState.getBlockHardness(MakePacketMiner.mc.world, pos) != -1.0f;
	}

	@Override
	public void onRender3D(Render3DEvent event) {
		if (fullNullCheck()) return;
		if (breakPos == null) return;

		RenderUtil.drawBoxESP(
			this.breakPos,
			new Color(100, 100, 240),
			false,
			new Color(240, 100, 100),
			1.0f,
			true,
			true,
			90,
			false
		);

	}
	@Override
	public void onEnable() {
		this.breakPos = null;
		this.instaTimer.reset();
		this.timer.reset();
		this.swapBack = false;
	}

	@Override
	public void onDisable() {
		this.breakPos = null;
	}

	@Override
	public void onTick() {
		if (!nullCheck()) {
			if (this.swapBack) {
				if (this.oldSlot != -1) {
					UtilsRewrite.uInventory.heldItemChange(oldSlot, true, true, false);
					if (debug.getValue()) sendModuleMessage("swapback!");
				}
				this.swapBack = false;
			}
			if (this.breakPos != null) {
				this.oldSlot = MakePacketMiner.mc.player.inventory.currentItem;
				if (
					MakePacketMiner.mc.player.getDistanceSq(this.breakPos) > MathUtil.square(
						this.resetRange.getValue().floatValue()
					) //if ranged
						|| MakePacketMiner.mc.world.getBlockState(this.breakPos).getBlock() == Blocks.AIR //if cantbreak
				) {
					this.breakPos = null;
					this.breakFace = null;
					this.readyToMine = false;
					return; //reset
				}
				if (autoRetry.getValue()) {
					retry();
				}
				final float breakTime = MakePacketMiner.mc.world.getBlockState(this.breakPos)
					.getBlockHardness(MakePacketMiner.mc.world, this.breakPos) * 40.0f;
				if (this.timer.passedMs((long) breakTime)) {
					this.readyToMine = true;
				}
				if (this.timer.passedMs((long) breakTime) && bestSlot(this.breakPos) != -1) {
					int best = bestSlot(this.breakPos);
					UtilsRewrite.uInventory.heldItemChange(best, true, true, false);
				}
				makeUtil.rotateToPosition(breakPos);
				PacketCenter.sP(PacketCenter.getDigBlockPacket(breakPos, CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK));
				if (this.oldSlot != -1) {
					MakePacketMiner.mc.player.inventory.currentItem = this.oldSlot;
					MakePacketMiner.mc.player.connection.sendPacket(new CPacketHeldItemChange(this.oldSlot));
					UtilsRewrite.uInventory.heldItemChange(oldSlot, true, true, false);
				}
			}
		}
	}

	@SubscribeEvent
	public void OnDamageBlock(final BlockEvent event) {
		if (nullCheck()) return;
		//makeUtil.rotateToPosition(event.pos);
		//SilentRotationUtil.lookAtBlock(event.pos);
		RebirthUtil.facePosFacing(event.pos, event.facing);
		if (
			this.breakPos != null
				&& event.pos.toLong() == this.breakPos.toLong()
				&& this.timer.passedMs(
				(long) (MakePacketMiner.mc.world.getBlockState(this.breakPos)
					.getBlockHardness(MakePacketMiner.mc.world, this.breakPos)
					* 40.0f
				)
			)
				&& bestSlot(event.pos) != -1
		) {
			if (!this.silent.getValue()) {
				MakePacketMiner.mc.player.inventory.currentItem = bestSlot(this.breakPos);
			}
			UtilsRewrite.uInventory.heldItemChange(bestSlot(this.breakPos), false, true, false);
			PacketCenter.sP(PacketCenter.getDigBlockPacket(breakPos, CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK));
			event.setCanceled(this.swapBack = true);
			sendModuleMessage("send packet and canceled " + ChatFormatting.RED + "STOP_DESTROY_BLOCK" + ChatFormatting.RESET + " on pos " + BlockChecker.simpleXYZString(breakPos));
			return;
		}
		if (canBlockBeBroken(event.pos)) {
			if (this.breakPos != null) {
				PacketCenter.sP(PacketCenter.getDigBlockPacket(breakPos, CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK));
			}
			this.start = System.currentTimeMillis();
			this.timer.reset();
			this.instaTimer.reset();
			this.breakPos = event.pos;
			this.breakFace = event.facing;
			this.readyToMine = false;
			makeUtil.rotateToPosition(breakPos);
			MakePacketMiner.mc.player.swingArm(EnumHand.MAIN_HAND);
			PacketCenter.sP(PacketCenter.getDigBlockPacket(breakPos, CPacketPlayerDigging.Action.START_DESTROY_BLOCK));
			PacketCenter.sP(PacketCenter.getDigBlockPacket(breakPos, CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK));
			if (debug.getValue()) sendModuleMessage("breakblock " + BlockChecker.simpleXYZString(breakPos));
			event.setCanceled(true);
		}
	}
	public void retry() {
		{
			if (
				this.timer.passedMs(
					(long) (MakePacketMiner.mc.world.getBlockState(this.breakPos)
						.getBlockHardness(MakePacketMiner.mc.world, this.breakPos)
						* 40.0f
					)
				) && bestSlot(breakPos) != -1
			) {
				UtilsRewrite.uInventory.heldItemChange(bestSlot(this.breakPos), false, true, false);
				PacketCenter.sP(PacketCenter.getDigBlockPacket(breakPos, CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK));
			}
		}
	}


}