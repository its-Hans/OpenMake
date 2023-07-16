package cn.make.util;

import chad.phobos.api.center.Util;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

public class pushHelper implements Util {
	public static class piston {
		BlockPos piston;
		BlockPos redstone;
		boolean pull;
		public piston(BlockPos piston, BlockPos redstone, boolean pull) {
			this.piston = piston;
			this.redstone = redstone;
			this.pull = pull;
		}
		public BlockPos getPistonPos() {
			return this.piston;
		}
		public BlockPos getRedstonePos() {
			return this.redstone;
		}
		public boolean getIsPull() {
			return this.pull;
		}
	}
	public static BlockPos[][] pistonList(EntityPlayer player) {
		BlockPos face = player.getPosition().up();
		return new BlockPos[][] {
			new BlockPos[] {
				face.west(), face.north(), face.east(), face.south()
			},
			new BlockPos[] {
				face.east(), face.south(), face.west(), face.north()
			}
		};
	}/*
	public static piston[] canPushList(EntityPlayer player) {
		boolean onBurrow = !(blocktype(player.getPosition()) instanceof BlockAir);
		boolean westFree = false;
		boolean northFree = false;
		boolean eastFree = false;
		boolean southFree = false;
		BlockPos[][] poslist = pistonList(player);
		BlockPos[] listone = poslist[1];
		BlockPos[] listtwo = poslist[2];
		if (blocktype(listone[1]) instanceof BlockAir) westFree = true;
		if (blocktype(listone[2]) instanceof BlockAir) northFree = true;
		if (blocktype(listone[3]) instanceof BlockAir) eastFree = true;
		if (blocktype(listone[4]) instanceof BlockAir) southFree = true;
	}*/
	public static Block blocktype(BlockPos pos) {
		return BlockChecker.getBlockType(pos);
	}
}
