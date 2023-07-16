package cn.make.util;

import chad.phobos.api.center.Util;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class BlockCheck2 implements Util {
	public static boolean isSafe(EntityPlayer player) {

		int safe = 0;
		BlockPos fW = getPlayerPos.getFeetWestPos(player);
		Block fWt = BlockChecker.getBlockType(fW);
		BlockPos fE = getPlayerPos.getFeetEastPos(player);
		Block fEt = BlockChecker.getBlockType(fE);
		BlockPos fN = getPlayerPos.getFeetNorthPos(player);
		Block fNt = BlockChecker.getBlockType(fN);
		BlockPos fS = getPlayerPos.getFeetSouthPos(player);
		Block fSt = BlockChecker.getBlockType(fS);
		BlockPos bur = getPlayerPos.getBurrowPos(player);
		Block burt = BlockChecker.getBlockType(bur);
		if (fWt != Blocks.AIR) safe++;
		if (fEt != Blocks.AIR) safe++;
		if (fNt != Blocks.AIR) safe++;
		if (fSt != Blocks.AIR) safe++;
		if (burt != Blocks.AIR) safe = safe + 2;

		return safe >= 3;
	}
	public static BlockPos FaceBestBlock(EntityPlayer player) {

		List<BlockPos> airPos_s = new ArrayList<BlockPos>();

		BlockPos East = getPlayerPos.getFaceEastPos(player);
		Block EastType = BlockChecker.getBlockType(East);
		Block EastDownType = BlockChecker.getBlockType(new BlockPos(East.getX(), East.getY() - 1, East.getZ()));
		if (EastType == Blocks.AIR && EastDownType != Blocks.AIR) airPos_s.add(East);

		BlockPos West = getPlayerPos.getFaceWestPos(player);
		Block WestType = BlockChecker.getBlockType(West);
		Block WestDownType = BlockChecker.getBlockType(new BlockPos(West.getX(), West.getY() - 1, West.getZ()));
		if (WestType == Blocks.AIR && WestDownType != Blocks.AIR) airPos_s.add(West);

		BlockPos South = getPlayerPos.getFaceSouthPos(player);
		Block SouthType = BlockChecker.getBlockType(South);
		Block SouthDownType = BlockChecker.getBlockType(new BlockPos(South.getX(), South.getY() - 1, South.getZ()));
		if (SouthType == Blocks.AIR && SouthDownType != Blocks.AIR) airPos_s.add(South);

		BlockPos North = getPlayerPos.getFaceNorthPos(player);
		Block NorthType = BlockChecker.getBlockType(North);
		Block NorthDownType = BlockChecker.getBlockType(new BlockPos(North.getX(), North.getY() - 1, North.getZ()));
		if (NorthType == Blocks.AIR && NorthDownType != Blocks.AIR) airPos_s.add(North);

		if (airPos_s.isEmpty()) return null;

		double minDistanceSq = Double.MAX_VALUE;
		BlockPos nearest = new BlockPos(player);
		for (BlockPos pos : airPos_s) {
			double distanceSq = player.getDistanceSq(pos);
			if (distanceSq < minDistanceSq) {
				minDistanceSq = distanceSq;
				nearest = pos;
			}
		}
		return nearest;
	}
	public static void FaceList() {

		List<BlockPos> faceBlockList = new ArrayList<BlockPos>();
		double xadd = 0;
		double yadd = 0;
		double zadd = 0;

		BlockPos blockpos = new BlockPos(mc.player.posX,mc.player.posY, mc.player.posZ);

		//east face
		xadd = 0;yadd = 0;zadd = 0;
		xadd = 1;yadd = 1;
		BlockPos East_Pos = new BlockPos(blockpos.getX() + xadd, blockpos.getY() + yadd, blockpos.getZ() + zadd);
		Block East_Type = mc.world.getBlockState(East_Pos).getBlock();
		if (East_Type instanceof BlockPistonBase) faceBlockList.add(East_Pos);

		//west face
		xadd = 0;yadd = 0;zadd = 0;
		xadd = -1;yadd = 1;
		BlockPos West_Pos = new BlockPos(blockpos.getX() + xadd, blockpos.getY() + yadd, blockpos.getZ() + zadd);
		Block West_Type = mc.world.getBlockState(West_Pos).getBlock();
		if (West_Type instanceof BlockPistonBase) faceBlockList.add(West_Pos);

		//south face
		xadd = 0;yadd = 0;zadd = 0;
		zadd = 1;yadd = 1;
		BlockPos South_Pos = new BlockPos(blockpos.getX() + xadd, blockpos.getY() + yadd, blockpos.getZ() + zadd);
		Block South_Type = mc.world.getBlockState(South_Pos).getBlock();
		if (South_Type instanceof BlockPistonBase) faceBlockList.add(South_Pos);

		//north face
		xadd = 0;yadd = 0;zadd = 0;
		zadd = -1;yadd = 1;
		BlockPos North_Pos = new BlockPos(blockpos.getX() + xadd, blockpos.getY() + yadd, blockpos.getZ() + zadd);
		Block North_Type = mc.world.getBlockState(North_Pos).getBlock();
		if (North_Type instanceof BlockPistonBase) faceBlockList.add(North_Pos);
	}
}
