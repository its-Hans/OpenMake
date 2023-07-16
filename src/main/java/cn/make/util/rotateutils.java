package cn.make.util;

import cn.make.util.skid.RebirthUtil;
import cn.make.util.skid.two.BlockUtil;
import chad.phobos.api.center.Command;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.Arrays;
import java.util.Comparator;

import static chad.phobos.api.center.Util.mc;

public class rotateutils {
	public static void clckBlockXIN(BlockPos pos, boolean debug) {
		rotateutils _this = new rotateutils();
		final EnumFacing legitFace = _this.getBestFacing(pos);
		if (debug) Command.sendMessage("damage facing: " + legitFace.getName());
		RebirthUtil.facePosFacing(pos, legitFace);
		mc.world.getBlockState(pos).getBlock();
		mc.playerController.onPlayerDamageBlock(pos, legitFace);
	}
	public EnumFacing getBestFacing(BlockPos pos) {
		idk[] idks = offsetsBlockPos(pos);
		final EntityPlayer player = mc.player;
		return Arrays.stream(idks)
			.filter(idk -> idk._pos != null)
			.min(Comparator.comparingDouble(
				idk -> idk._pos.distanceSqToCenter(
					player.posX,
					player.posY + player.getEyeHeight(),
					player.posZ
				)
			))
			.map(idk -> idk._facing)
			// 返回结果
			.orElse(null);
	}

	public static idk[] offsetsBlockPos(BlockPos pos) {
		return new idk[] {
			new idk(pos.up(), EnumFacing.UP),
			new idk(pos.down(), EnumFacing.DOWN),
			new idk(pos.east(), EnumFacing.EAST),
			new idk(pos.west(), EnumFacing.WEST),
			new idk(pos.south(), EnumFacing.SOUTH),
			new idk(pos.north(), EnumFacing.NORTH)
		};
	}
	public static class idk {
		BlockPos _pos;
		EnumFacing _facing;
		public idk(BlockPos pos, EnumFacing facing) {
			_pos = pos;
			_facing = facing;
		}
	}

	public static void placeblockFacing(BlockPos pos, EnumFacing facing, boolean packet) {
		if (facing == EnumFacing.EAST) {
			doPlace(pos, -90F, packet);
			return;
		}
		if (facing == EnumFacing.WEST) {
			doPlace(pos, 90F, packet);
			return;
		}
		if (facing == EnumFacing.NORTH) {
			doPlace(pos, 180F, packet);
			return;
		}
		if (facing == EnumFacing.SOUTH) {
			doPlace(pos, 0.0F, packet);
		}
	}
	static void doPlace(BlockPos pos, float yaw, boolean packet) {
		mc.player.connection.sendPacket(new CPacketPlayer.Rotation(yaw, 5.0F, mc.player.onGround));
		BlockUtil.placeBlock(pos, EnumHand.MAIN_HAND, false, packet);
		RebirthUtil.facePlacePos(pos);
	}
	//一个方法，接受一个BlockPos，以一个合理的EnumFacing点击这个BlockPos
	public static EnumFacing clickBlockXIN2(BlockPos pos) {
		//获取玩家的位置
		EntityPlayerSP player = mc.player;
		double x = player.posX;
		double y = player.posY + player.getEyeHeight();
		double z = player.posZ;
		//获取方块的中心位置
		double bx = pos.getX() + 0.5;
		double by = pos.getY() + 0.5;
		double bz = pos.getZ() + 0.5;
		//计算玩家到方块的向量
		double dx = bx - x;
		double dy = by - y;
		double dz = bz - z;
		//计算玩家到方块的距离
		double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
		//计算玩家到方块的单位向量
		double ux = dx / dist;
		double uy = dy / dist;
		double uz = dz / dist;
		//计算玩家到方块的夹角
		float yaw = (float) Math.toDegrees(Math.atan2(uz, ux)) - 90.0F;
		float pitch = (float) -Math.toDegrees(Math.asin(uy));
		//根据夹角确定合理的EnumFacing
		EnumFacing facing;
		if (pitch > 45.0F) {
			facing = EnumFacing.DOWN; //如果夹角大于45度，说明玩家在方块上方，使用DOWN
		} else if (pitch < -45.0F) {
			facing = EnumFacing.UP; //如果夹角小于-45度，说明玩家在方块下方，使用UP
		} else {
			facing = EnumFacing.byHorizontalIndex(MathHelper.floor((yaw * 4.0F / 360.0F) + 0.5D) & 3); //否则，根据yaw确定水平方向
		}
		//点击方块
		mc.playerController.onPlayerDamageBlock(pos, facing);
		return facing;
	}



	public static EnumFacing getReverse(EnumFacing old) {
		if (old == EnumFacing.NORTH) return EnumFacing.SOUTH;
		if (old == EnumFacing.SOUTH) return EnumFacing.NORTH;
		if (old == EnumFacing.EAST) return EnumFacing.WEST;
		if (old == EnumFacing.WEST) return EnumFacing.EAST;
		if (old == EnumFacing.DOWN) return EnumFacing.UP;
		return EnumFacing.DOWN;
	}

}
