package cn.make.util;

import chad.phobos.api.center.Util;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

public class getPlayerPos implements Util {

	public static BlockPos getFaceEastPos(EntityPlayer player) {
		BlockPos blockpos = new BlockPos(player.posX,player.posY, player.posZ);
		return new BlockPos(
			blockpos.getX() + 1, //x +1 : east
			blockpos.getY() + 1, //y always 1 : bec face
			blockpos.getZ()
		);
	}
	public static BlockPos getFaceWestPos(EntityPlayer player) {
		BlockPos blockpos = new BlockPos(player.posX,player.posY, player.posZ);
		return new BlockPos(
			blockpos.getX() - 1, //x -1 : west
			blockpos.getY() + 1, //y always 1 : bec face
			blockpos.getZ()
		);
	}

	public static BlockPos getFaceSouthPos(EntityPlayer player) {
		BlockPos blockpos = new BlockPos(player.posX,player.posY, player.posZ);
		return new BlockPos(
			blockpos.getX(),
			blockpos.getY() + 1, //y always 1 : bec face
			blockpos.getZ() + 1 //z +1 : south
		);
	}
	public static BlockPos getFaceNorthPos(EntityPlayer player) {
		BlockPos blockpos = new BlockPos(player.posX,player.posY, player.posZ);
		return new BlockPos(
			blockpos.getX(),
			blockpos.getY() + 1, //y always 1 : bec face
			blockpos.getZ() - 1 //z -1 : north
		);
	}


	public static BlockPos getFeetEastPos(EntityPlayer player) {
		BlockPos blockpos = new BlockPos(player.posX,player.posY, player.posZ);
		return new BlockPos(
			blockpos.getX() + 1, //x +1 : east
			blockpos.getY(),
			blockpos.getZ()
		);
	}
	public static BlockPos getFeetWestPos(EntityPlayer player) {
		BlockPos blockpos = new BlockPos(player.posX,player.posY, player.posZ);
		return new BlockPos(
			blockpos.getX() - 1, //x -1 : west
			blockpos.getY(),
			blockpos.getZ()
		);
	}

	public static BlockPos getFeetSouthPos(EntityPlayer player) {
		BlockPos blockpos = new BlockPos(player.posX,player.posY, player.posZ);
		return new BlockPos(
			blockpos.getX(),
			blockpos.getY(),
			blockpos.getZ() + 1 //z +1 : south
		);
	}
	public static BlockPos getFeetNorthPos(EntityPlayer player) {
		BlockPos blockpos = new BlockPos(player.posX,player.posY, player.posZ);
		return new BlockPos(
			blockpos.getX(),
			blockpos.getY(),
			blockpos.getZ() - 1 //z -1 : north
		);
	}


	public static BlockPos getBurrowPos(EntityPlayer player) {
		BlockPos blockpos = new BlockPos(player.posX,player.posY, player.posZ);
		return new BlockPos(
			blockpos.getX(),
			blockpos.getY(), //burrow
			blockpos.getZ()
		);
	}
	public static BlockPos getFacePos(EntityPlayer player) {
		BlockPos blockpos = new BlockPos(player.posX,player.posY, player.posZ);
		return new BlockPos(
			blockpos.getX(),
			blockpos.getY() + 1, //face
			blockpos.getZ()
		);
	}
	public static BlockPos getHeadPos(EntityPlayer player) {
		BlockPos blockpos = new BlockPos(player.posX,player.posY, player.posZ);
		return new BlockPos(
			blockpos.getX(),
			blockpos.getY() + 2, //head
			blockpos.getZ()
		);
	}
}
