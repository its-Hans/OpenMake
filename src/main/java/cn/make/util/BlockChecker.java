package cn.make.util;

import cn.make.util.skid.RotationUtil;
import chad.phobos.api.center.Util;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class BlockChecker implements Util {

	public static List<Block> airBlocks =
		java.util.Arrays.asList(
			Blocks.AIR,
			Blocks.FIRE,
			Blocks.LAVA,
			Blocks.WATER,
			Blocks.FLOWING_LAVA,
			Blocks.FLOWING_WATER
		);	public static List<Block> airBlocks2 =
		java.util.Arrays.asList(
			Blocks.AIR,
			Blocks.FIRE,
			Blocks.LAVA,
			Blocks.WATER,
			Blocks.FLOWING_LAVA,
			Blocks.FLOWING_WATER,
			Blocks.PISTON
		);
	public static BlockPos canInteract(BlockPos[] positions, float range) {
		BlockPos playerPos = Minecraft.getMinecraft().player.getPosition();
		double minDistance = Double.MAX_VALUE;
		BlockPos closestPos = null;
		for (BlockPos pos : positions) {
			double distance = playerPos.distanceSq(pos);
			if (distance < minDistance) {
				minDistance = distance;
				closestPos = pos;
			}
		}
		if (Math.sqrt(minDistance) > range) {
			return null;
		}
		return closestPos;
	}
	public static BlockPos bestDist(List<BlockPos> positions) {
		BlockPos playerPos = mc.player.getPosition();
		double minDistance = Double.MAX_VALUE;
		BlockPos closestPos = null;
		for (BlockPos pos : positions) {
			double distance = playerPos.distanceSq(pos);
			if (distance < minDistance) {
				minDistance = distance;
				closestPos = pos;
			}
		}
		return closestPos;
	}
	public static boolean canPlace(BlockPos pos) {
		if (!airBlocks.contains(getBlockType(pos))) return false;

		return !airBlocks.contains(bOffTypeUp(pos))
			| !airBlocks.contains(bOffTypeDown(pos))
			| !airBlocks.contains(bOffTypeLeft(pos))
			| !airBlocks.contains(bOffTypeRight(pos))
			| !airBlocks.contains(bOffTypeFacing(pos))
			| !airBlocks.contains(bOffTypeBack(pos));
	}
	public static boolean isDownBlock(BlockPos pos) {
		return getBlockType(pos.down()) != Blocks.AIR;
	}
	public static boolean isAir(BlockPos pos) {
		return airBlocks.contains(getBlockType(pos));
	}
	public static boolean isAirb(BlockPos pos) {
		return airBlocks2.contains(getBlockType(pos));
	}
	public static BlockPos stupidBestPosGen(BlockPos pos) {
		List<BlockPos> freeLists = new ArrayList<>();
		BlockPos bestPos = null;

		BlockPos upp = bOffPosUp(pos);
		Block upt = bOffTypeUp(pos);

		BlockPos downp = bOffPosDown(pos);
		Block downt = bOffTypeDown(pos);

		BlockPos leftp = bOffPosLeft(pos);
		Block leftt = bOffTypeLeft(pos);

		BlockPos rightp = bOffPosRight(pos);
		Block rightt = bOffTypeRight(pos);

		BlockPos facingp = bOffPosFacing(pos);
		Block facingt = bOffTypeFacing(pos);

		BlockPos backp = bOffPosBack(pos);
		Block backt = bOffTypeBack(pos);

		if (airBlocks.contains(upt)) freeLists.add(upp);
		if (airBlocks.contains(downt)) freeLists.add(downp);
		if (airBlocks.contains(leftt)) freeLists.add(leftp);
		if (airBlocks.contains(rightt)) freeLists.add(rightp);
		if (airBlocks.contains(facingt)) freeLists.add(facingp);
		if (airBlocks.contains(backt)) freeLists.add(backp);
		return getClosestBlockPos(freeLists);
	}
	public static BlockPos stupidBestPosGen(BlockPos pos, BlockPos postwo) {
		List<BlockPos> freeLists = new ArrayList<>();
		BlockPos cryhead = new BlockPos(postwo.getX(), postwo.getY() + 1, postwo.getZ());

		BlockPos upp = bOffPosUp(pos);
		Block upt = bOffTypeUp(pos);

		BlockPos downp = bOffPosDown(pos);
		Block downt = bOffTypeDown(pos);

		BlockPos leftp = bOffPosLeft(pos);
		Block leftt = bOffTypeLeft(pos);

		BlockPos rightp = bOffPosRight(pos);
		Block rightt = bOffTypeRight(pos);

		BlockPos facingp = bOffPosFacing(pos);
		Block facingt = bOffTypeFacing(pos);

		BlockPos backp = bOffPosBack(pos);
		Block backt = bOffTypeBack(pos);

		if (airBlocks.contains(upt)) freeLists.add(upp);
		if (airBlocks.contains(downt)) freeLists.add(downp);
		if (airBlocks.contains(leftt)) freeLists.add(leftp);
		if (airBlocks.contains(rightt)) freeLists.add(rightp);
		if (airBlocks.contains(facingt)) freeLists.add(facingp);
		if (airBlocks.contains(backt)) freeLists.add(backp);
		if (freeLists.contains(postwo)) freeLists.remove(postwo);
		if (freeLists.contains(cryhead)) freeLists.remove(cryhead);
		return getClosestBlockPos(freeLists);
	}
	public static BlockPos getClosestBlockPos(List<BlockPos> list) {

		//如果列表为空，返回null
		if (list == null || list.isEmpty()) {
			return null;
		}
		//初始化最近的距离和位置
		double minDistance = Double.MAX_VALUE;
		BlockPos closest = null;
		//遍历列表中的每个位置
		for (BlockPos pos : list) {
			//计算位置到玩家的距离平方
			double distanceSq = pos.distanceSq(mc.player.posX, mc.player.posY, mc.player.posZ);
			//如果距离小于最近的距离，更新最近的距离和位置
			if (distanceSq < minDistance) {
				minDistance = distanceSq;
				closest = pos;
			}
		}
		//返回最近的位置
		return closest;
	}

	public static BlockPos bOffPosUp(BlockPos pos) {
		return new BlockPos(
			pos.getX(),
			pos.getY() + 1,
			pos.getZ()
		);
	}
	public static Block bOffTypeUp(BlockPos pos) {
		return BlockChecker.mc.world.getBlockState(bOffPosUp(pos)).getBlock();
	}

	public static BlockPos bOffPosDown(BlockPos pos) {
		return new BlockPos(
			pos.getX(),
			pos.getY() - 1,
			pos.getZ()
		);
	}
	public static Block bOffTypeDown(BlockPos pos) {
		return BlockChecker.mc.world.getBlockState(bOffPosDown(pos)).getBlock();
	}

	public static BlockPos bOffPosLeft(BlockPos pos) {
		return new BlockPos(
			pos.getX() + 1,
			pos.getY(),
			pos.getZ()
		);
	}
	public static Block bOffTypeLeft(BlockPos pos) {
		return BlockChecker.mc.world.getBlockState(bOffPosLeft(pos)).getBlock();
	}

	public static BlockPos bOffPosRight(BlockPos pos) {
		return new BlockPos(
			pos.getX() - 1,
			pos.getY(),
			pos.getZ()
		);
	}
	public static Block bOffTypeRight(BlockPos pos) {
		return BlockChecker.mc.world.getBlockState(bOffPosRight(pos)).getBlock();
	}

	public static BlockPos bOffPosFacing(BlockPos pos) {
		return new BlockPos(
			pos.getX(),
			pos.getY(),
			pos.getZ() + 1
		);
	}
	public static Block bOffTypeFacing(BlockPos pos) {
		return BlockChecker.mc.world.getBlockState(bOffPosFacing(pos)).getBlock();
	}

	public static BlockPos bOffPosBack(BlockPos pos) {
		return new BlockPos(
			pos.getX(),
			pos.getY(),
			pos.getZ() - 1
		);
	}
	public static Block bOffTypeBack(BlockPos pos) {
		return BlockChecker.mc.world.getBlockState(bOffPosBack(pos)).getBlock();
	}

	public static Block getBlockType(BlockPos pos) {
		return BlockChecker.mc.world.getBlockState(pos).getBlock();
	}
	public static List<BlockPos> getNSWE(BlockPos mainPos) {
		List<BlockPos> list = new ArrayList<>();

		list.add(makeUtil.offsetBlockPos(mainPos, 1,0,0));
		list.add(makeUtil.offsetBlockPos(mainPos, -1,0,0));
		list.add(makeUtil.offsetBlockPos(mainPos, 0,0,1));
		list.add(makeUtil.offsetBlockPos(mainPos, 0,0,-1));
		return list;
	}
	public static String simpleXYZString(BlockPos pos) {
		if (pos == null) return "illegal pos";
		return pos.getX() + " " + pos.getY() + " " + pos.getZ();
	}

	public static float[] getLegitRotations(BlockPos vec) {
		Vec3d eyesPos = RotationUtil.getEyesPos();

		double diffX = vec.getX() - eyesPos.x;
		double diffY = vec.getY() - eyesPos.y;
		double diffZ = vec.getZ() - eyesPos.z;

		double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

		float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F;
		float pitch = (float) -Math.toDegrees(Math.atan2(diffY, diffXZ));

		return new float[] {
			mc.player.rotationYaw
				+ MathHelper.wrapDegrees(yaw - mc.player.rotationYaw),
			mc.player.rotationPitch + MathHelper
				.wrapDegrees(pitch - mc.player.rotationPitch)
		};
	}

	public static float[] getLegitRotations(Vec3d vec) {
		Vec3d eyesPos = RotationUtil.getEyesPos();

		double diffX = vec.x - eyesPos.x;
		double diffY = vec.y - eyesPos.y;
		double diffZ = vec.z - eyesPos.z;

		double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

		float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F;
		float pitch = (float) -Math.toDegrees(Math.atan2(diffY, diffXZ));

		return new float[]{
			mc.player.rotationYaw
				+ MathHelper.wrapDegrees(yaw - mc.player.rotationYaw),
			mc.player.rotationPitch + MathHelper
				.wrapDegrees(pitch - mc.player.rotationPitch)};
	}

}

