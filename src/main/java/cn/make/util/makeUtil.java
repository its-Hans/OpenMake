package cn.make.util;

import cn.make.util.skid.EntityUtil;
import cn.make.util.skid.RebirthUtil;
import cn.make.util.skid.two.BlockUtil;
import chad.phobos.Client;
import chad.phobos.api.center.Command;
import chad.phobos.api.center.Util;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class makeUtil implements Util {

	public static void helpingPlace(BlockPos posOfHelp, Block blockTypePlace, boolean debug, int retryFreq) {
		if (retryFreq > 10 | retryFreq < 1) return; //如果次數不合理就不執行
		int freqs = 0; //創建次數計數器
		while (true) { //雖然是一直循環但裡面有break用來終止循環
			freqs++; //更新次數計時器
			if (BlockChecker.canPlace(posOfHelp)) { //如果可以直接放置方塊
				if (debug) Command.sendMessage("placing given pos" + posOfHelp); //提示
				retryPlace(posOfHelp, blockTypePlace, retryFreq); //嘗試放置
				break; //終止循環
			} else if (debug) Command.sendMessage("cantPlace" + posOfHelp + "trying place helping block"); //否則繼續 提示不能直接放置

			BlockPos helpingPos = null; //建立一個BlockPos用來記錄HelpingBlock的坐標

			//存儲所有HelpingBlock可能的坐標
			BlockPos up = BlockChecker.bOffPosUp(posOfHelp);
			BlockPos down = BlockChecker.bOffPosDown(posOfHelp);
			BlockPos left = BlockChecker.bOffPosLeft(posOfHelp);
			BlockPos right = BlockChecker.bOffPosRight(posOfHelp);
			BlockPos facing = BlockChecker.bOffPosFacing(posOfHelp);
			BlockPos back = BlockChecker.bOffPosBack(posOfHelp);

			//計算是否有可以放置的HelpingBlock
			if (BlockChecker.canPlace(up)) helpingPos = up;
			if (BlockChecker.canPlace(down)) helpingPos = down;
			if (BlockChecker.canPlace(left)) helpingPos = left;
			if (BlockChecker.canPlace(right)) helpingPos = right;
			if (BlockChecker.canPlace(facing)) helpingPos = facing;
			if (BlockChecker.canPlace(back)) helpingPos = back;

			if (helpingPos != null) retryPlace(helpingPos, Blocks.OBSIDIAN, retryFreq); //嘗試放置HelpingBlock
			if (freqs >= retryFreq) break; //如果次數已經達到了不管有沒有放出來都終止循環
			}
	}

	public static BlockPos genBestAirPos(BlockPos pos) {
		double minDist = Double.MAX_VALUE;
		BlockPos nearestAir = null;
		for (EnumFacing facing : EnumFacing.values()) {
			BlockPos neighbor = pos.offset(facing);
			if (makeUtil.mc.world.getBlockState(neighbor).getBlock() == Blocks.AIR) {
				double dist = makeUtil.mc.player.getDistanceSq(neighbor);
				if (dist < minDist) {
					minDist = dist;
					nearestAir = neighbor;
				}
			}
		}
		return nearestAir;
	}

	public static void silentPlace(BlockPos placepos, Block block) {
		if (findHotbarBlock(block) == -1) { //如果快捷欄中沒有想要放的方塊就直接返回
			return;
		}

		int blockslot = findHotbarBlock(block); //要切換到的地方
		int oldslot = makeUtil.mc.player.inventory.currentItem; //當前選中的地方

		//切換到方塊
		makeUtil.mc.player.connection.sendPacket(new CPacketHeldItemChange(blockslot));
		makeUtil.mc.player.inventory.currentItem = blockslot;
		makeUtil.mc.playerController.updateController();

		BlockUtil.placeBlock(placepos, EnumHand.MAIN_HAND, true, true, false); //嘗試放置方塊

		//切換回原來的地方
		makeUtil.mc.player.connection.sendPacket(new CPacketHeldItemChange(oldslot));
		makeUtil.mc.player.inventory.currentItem = oldslot;
		makeUtil.mc.playerController.updateController();
	}
	public static void silentPlace2(BlockPos placepos, Block block) {
		if (findHotbarBlock(block) == -1) { //如果快捷欄中沒有想要放的方塊就直接返回
			return;
		}

		int blockslot = findHotbarBlock(block); //要切換到的地方
		int oldslot = makeUtil.mc.player.inventory.currentItem; //當前選中的地方

		//切換到方塊
		makeUtil.mc.player.connection.sendPacket(new CPacketHeldItemChange(blockslot));
		makeUtil.mc.player.inventory.currentItem = blockslot;
		makeUtil.mc.playerController.updateController();

		BlockUtil.placeBlock(placepos, EnumHand.MAIN_HAND, true, false, true); //嘗試放置方塊

		//切換回原來的地方
		makeUtil.mc.player.connection.sendPacket(new CPacketHeldItemChange(oldslot));
		makeUtil.mc.player.inventory.currentItem = oldslot;
		makeUtil.mc.playerController.updateController();
	}

	public static void damageBlock(BlockPos pos) {
			RayTraceResult rayTraceResult = makeUtil.mc.world.rayTraceBlocks (
				new Vec3d(
					mc.player.posX,
					mc.player.posY + (double) mc.player.getEyeHeight(),
					mc.player.posZ
				),
				new Vec3d(
					(double)pos.getX() + 0.5,
					(double)pos.getX() - 0.5,
					(double)pos.getX() + 0.5
				)
			);

			if (rayTraceResult == null || rayTraceResult.sideHit == null) {
				makeUtil.mc.playerController.onPlayerDamageBlock (
					pos,
					EnumFacing.UP
				);
			} else
				makeUtil.mc.playerController.onPlayerDamageBlock (
					pos,
					rayTraceResult.sideHit
				);
	}

	public static void placeCrystalOnBase(BlockPos basepos) {
		int crystal = getItemHotbar(Items.END_CRYSTAL);
		int oldslot = makeUtil.mc.player.inventory.currentItem;
		if (crystal == -1 | oldslot == -1) return;
		RayTraceResult result = makeUtil.mc.world.rayTraceBlocks(
			new Vec3d(
				makeUtil.mc.player.posX,
				makeUtil.mc.player.posY + (double) makeUtil.mc.player.getEyeHeight(),
				makeUtil.mc.player.posZ
			),
			new Vec3d(
				(double) basepos.getX() + 0.5,
				(double) basepos.getY() - 0.5,
				(double) basepos.getZ() + 0.5)
		);
		EnumFacing facing = result == null || result.sideHit == null ? EnumFacing.UP : result.sideHit;

		makeUtil.mc.player.connection.sendPacket(new CPacketHeldItemChange(crystal));
		makeUtil.mc.player.inventory.currentItem = crystal;
		makeUtil.mc.playerController.updateController();
		makeUtil.mc.player.connection.sendPacket (
			new CPacketPlayerTryUseItemOnBlock(basepos, facing, EnumHand.MAIN_HAND, 0.0f, 0.0f, 0.0f)
		);
		makeUtil.mc.player.connection.sendPacket(new CPacketHeldItemChange(oldslot));
		makeUtil.mc.player.inventory.currentItem = oldslot;
		makeUtil.mc.playerController.updateController();
	}
	public static void placeCrystalNoSwitch(BlockPos basepos) {
		RayTraceResult result = makeUtil.mc.world.rayTraceBlocks(
			new Vec3d(
				makeUtil.mc.player.posX,
				makeUtil.mc.player.posY + (double) makeUtil.mc.player.getEyeHeight(),
				makeUtil.mc.player.posZ
			),
			new Vec3d(
				(double) basepos.getX() + 0.5,
				(double) basepos.getY() - 0.5,
				(double) basepos.getZ() + 0.5)
		);
		EnumFacing facing = result == null || result.sideHit == null ? EnumFacing.UP : result.sideHit;

		makeUtil.mc.player.connection.sendPacket (
			new CPacketPlayerTryUseItemOnBlock(basepos, facing, EnumHand.MAIN_HAND, 0.0f, 0.0f, 0.0f)
		);
	}
	
	public static boolean onBurrowCheck(EntityPlayer player) {
		//獲取玩家burrow坐標
		Block burType = mc.world.getBlockState(getPlayerPos.getBurrowPos(player)).getBlock();
		//如果玩家burrow方塊不是空氣之類的
		return !(burType instanceof BlockAir
			| burType instanceof BlockFlower
			| burType instanceof BlockFire
			| burType instanceof BlockLiquid
			| burType instanceof BlockBed
			| burType instanceof BlockDoor
			| burType instanceof BlockFarmland
			| burType instanceof BlockChest
			| burType instanceof BlockButton
			| burType instanceof BlockHopper
			| burType instanceof BlockSkull
			| burType instanceof BlockBanner
			| burType instanceof BlockSnow
			| burType instanceof BlockPistonExtension
		);
	}

	public static void retryPlace(BlockPos pos,Block blockType, int retrys) {
		if (retrys < 1) {
			Command.sendMessage("cant retryplace dont <0 number");
			return;
		} //如果次數不合理就不執行
		int freqs2 = 0; //創建次數計時器
		while (BlockChecker.airBlocks.contains(BlockChecker.getBlockType(pos))) { //如果那個地方可以放置方塊的話
			if (freqs2 >= retrys) {
				Command.sendMessage("end retry");
				break;
			} //如果次數已經達到則直接終止循環
			silentPlace(pos, blockType); //放置方塊
			Command.sendMessage("retrys " + (freqs2 + 1));
			freqs2++; //更新次數計時器
		}
		Command.sendMessage("ok");
	}
	public static void retryPlace(BlockPos pos, Block blockType, int retrys, boolean debug) {
		if (pos == null) return;
		if (retrys < 1) {
			if (debug) Command.sendMessage("cant retryplace dont <0 number");
			return;
		} //如果次數不合理就不執行
		int freqs2 = 0; //創建次數計時器
		while (BlockChecker.canPlace(pos)) { //如果那個地方可以放置方塊的話
			if (freqs2 >= retrys) {
				if (debug) Command.sendMessage("end retry");
				break;
			} //如果次數已經達到則直接終止循環
			silentPlace(pos, blockType); //放置方塊
			if (debug) Command.sendMessage("retrys " + (freqs2 + 1));
			freqs2++; //更新次數計時器
		}
		if (debug) Command.sendMessage("ok");
	}

	public static int findHotbarBlock(Block blockIn) {
		for (int i = 0; i < 9; ++i) {
			Block block;
			ItemStack stack = makeUtil.mc.player.inventory.getStackInSlot(i);
			if (stack == ItemStack.EMPTY || !(stack.getItem() instanceof ItemBlock) || (block = ((ItemBlock) stack.getItem()).getBlock()) != blockIn)
				continue;
			return i;
		}
		return -1;
	}

	public static int getItemHotbar(Item input) {
		for (int i = 0; i < 9; ++i) {
			Item item = makeUtil.mc.player.inventory.getStackInSlot(i).getItem();
			if (Item.getIdFromItem(item) != Item.getIdFromItem(input)) continue;
			return i;
		}
		return -1;
	}

	public static class pulls {
		private static final List<BlockPos> canPulls = new ArrayList<>();
		private static BlockPos genWest(EntityPlayer target) {
			int masterx = 0;
			int masterz = 1;
			BlockPos qwq = getPlayerPos.getFaceWestPos(target);
			BlockPos crypos = new BlockPos(qwq.getX(), qwq.getY() + 1, qwq.getZ());

			BlockPos check_qwq = BlockChecker.stupidBestPosGen(qwq);

			BlockPos pistonPos1 = new BlockPos(qwq.getX() + masterx, qwq.getY() + 1, qwq.getZ() + masterz);
			BlockPos check_pp1 = BlockChecker.stupidBestPosGen(pistonPos1, crypos);

			BlockPos pistonPos2 = new BlockPos(qwq.getX() + masterx, qwq.getY() + 1, qwq.getZ() - masterz);
			BlockPos check_pp2 = BlockChecker.stupidBestPosGen(pistonPos2, crypos);

			if (!canCrystalPlace(qwq)) return null;
			if (check_qwq == null) return null;
			if (
				check_pp1 != null
					& BlockChecker.airBlocks.contains(BlockChecker.getBlockType(pistonPos1))
			) canPulls.add(pistonPos1);

			if (
				check_pp2 != null
					& BlockChecker.airBlocks.contains(BlockChecker.getBlockType(pistonPos2))
			) canPulls.add(pistonPos2);

			return check_qwq;
		}
		private static BlockPos genEast(EntityPlayer target) {
			int masterx = 0;
			int masterz = -1;
			BlockPos qwq = getPlayerPos.getFaceEastPos(target);
			BlockPos crypos = new BlockPos(qwq.getX(), qwq.getY() + 1, qwq.getZ());

			BlockPos check_qwq = BlockChecker.stupidBestPosGen(qwq);

			BlockPos pistonPos1 = new BlockPos(qwq.getX() + masterx, qwq.getY() + 1, qwq.getZ() + masterz);
			BlockPos check_pp1 = BlockChecker.stupidBestPosGen(pistonPos1, crypos);

			BlockPos pistonPos2 = new BlockPos(qwq.getX() + masterx, qwq.getY() + 1, qwq.getZ() - masterz);
			BlockPos check_pp2 = BlockChecker.stupidBestPosGen(pistonPos2, crypos);

			if (!canCrystalPlace(qwq)) return null;
			if (check_qwq == null) return null;
			if (
				check_pp1 != null
					& BlockChecker.airBlocks.contains(BlockChecker.getBlockType(pistonPos1))
			) canPulls.add(pistonPos1);

			if (
				check_pp2 != null
					& BlockChecker.airBlocks.contains(BlockChecker.getBlockType(pistonPos2))
			) canPulls.add(pistonPos2);

			return check_qwq;
		}
		private static BlockPos genSouth(EntityPlayer target) {
			int masterx = 1;
			int masterz = 0;
			BlockPos qwq = getPlayerPos.getFaceSouthPos(target);
			BlockPos crypos = new BlockPos(qwq.getX(), qwq.getY() + 1, qwq.getZ());

			BlockPos check_qwq = BlockChecker.stupidBestPosGen(qwq);

			BlockPos pistonPos1 = new BlockPos(qwq.getX() + masterx, qwq.getY() + 1, qwq.getZ() + masterz);
			BlockPos check_pp1 = BlockChecker.stupidBestPosGen(pistonPos1, crypos);

			BlockPos pistonPos2 = new BlockPos(qwq.getX() + masterx, qwq.getY() + 1, qwq.getZ() - masterz);
			BlockPos check_pp2 = BlockChecker.stupidBestPosGen(pistonPos2, crypos);

			if (!canCrystalPlace(qwq)) return null;
			if (check_qwq == null) return null;
			if (
				check_pp1 != null
					& BlockChecker.airBlocks.contains(BlockChecker.getBlockType(pistonPos1))
			) canPulls.add(pistonPos1);

			if (
				check_pp2 != null
					& BlockChecker.airBlocks.contains(BlockChecker.getBlockType(pistonPos2))
			) canPulls.add(pistonPos2);

			return check_qwq;
		}
		private static BlockPos genNorth(EntityPlayer target) {
			int masterx = -1;
			int masterz = 0;
			BlockPos qwq = getPlayerPos.getFaceNorthPos(target);
			BlockPos crypos = new BlockPos(qwq.getX(), qwq.getY() + 1, qwq.getZ());

			BlockPos check_qwq = BlockChecker.stupidBestPosGen(qwq);

			BlockPos pistonPos1 = new BlockPos(qwq.getX() + masterx, qwq.getY() + 1, qwq.getZ() + masterz);
			BlockPos check_pp1 = BlockChecker.stupidBestPosGen(pistonPos1, crypos);

			BlockPos pistonPos2 = new BlockPos(qwq.getX() + masterx, qwq.getY() + 1, qwq.getZ() - masterz);
			BlockPos check_pp2 = BlockChecker.stupidBestPosGen(pistonPos2, crypos);

			if (!canCrystalPlace(qwq)) return null;
			if (check_qwq == null) return null;
			if (
				check_pp1 != null
					& BlockChecker.airBlocks.contains(BlockChecker.getBlockType(pistonPos1))
			) canPulls.add(pistonPos1);

			if (
				check_pp2 != null
					& BlockChecker.airBlocks.contains(BlockChecker.getBlockType(pistonPos2))
			) canPulls.add(pistonPos2);

			return check_qwq;
		}
		public static void doPlace(EntityPlayer target) {
			if (target == mc.player | target == null) return;
			canPulls.clear();
			genWest(target);
			genEast(target);
			genSouth(target);
			genNorth(target);
			Command.sendMessage("can Pull List:");
			for (BlockPos pos : canPulls)
				Command.sendMessage(pos.toString());

			BlockPos pistonPos = BlockChecker.getClosestBlockPos(canPulls);
			if (pistonPos == null) {
				Command.sendMessage("NO POS FOUND1");
				return;
			}
			BlockPos rsPos = BlockChecker.stupidBestPosGen(pistonPos);
			if (rsPos == null) {
				Command.sendMessage("NO POS FOUND2");
				return;
			}

			retryPlace(rsPos, Blocks.REDSTONE_BLOCK, 2);
			retryPlace(pistonPos, Blocks.PISTON, 10);
			retryPlace(rsPos, Blocks.REDSTONE_BLOCK, 5);
		}
		public static void ListPoses(EntityPlayer target) {
			if (target == null) return;
			List<BlockPos> rsList = new ArrayList<>();
			List<BlockPos> psList = new ArrayList<>();
			List<BlockPos> pubList = new ArrayList<>();

			//crystal main
			int cm = 1;
			//crystal head
			int ch = 2;
			BlockPos temp;

			//偏移
			BlockPos off010;
			BlockPos off020;

			BlockPos off100;
			BlockPos off110;
			BlockPos offm100;
			BlockPos offm110;
			BlockPos off001;
			BlockPos off011;
			BlockPos off00m1;
			BlockPos off01m1;

			//crystal base West
			BlockPos fAW = getPlayerPos.getFaceWestPos(target);

			//crystal base East
			BlockPos fAE = getPlayerPos.getFaceEastPos(target);

			//crystal base North
			BlockPos fAN = getPlayerPos.getFaceNorthPos(target);

			//crystal base South
			BlockPos fAS = getPlayerPos.getFaceSouthPos(target);

			BlockPos bur = getPlayerPos.getBurrowPos(target);
			Block burt = BlockChecker.getBlockType(bur);

			temp =fAW;
			off010 = offsetBlockPos(temp, 0,1,0);
			off020 = offsetBlockPos(temp, 0,2,0);
			off100 = offsetBlockPos(temp, 1,0,0);
			off110 = offsetBlockPos(temp, 1,1,0);
			offm100 = offsetBlockPos(temp, -1,0,0);
			offm110 = offsetBlockPos(temp, -1,1,0);
			//西面可放置的坐标
			BlockPos WestCrM = off010;
			BlockPos WestCrH = off020;
			BlockPos WestRedstone1 = off100;
			BlockPos WestPiston1 = off110;
			BlockPos WestRedstone2 = offm100;
			BlockPos WestPiston2 = offm110;

			temp =fAE;
			off010 = offsetBlockPos(temp, 0,1,0);
			off020 = offsetBlockPos(temp, 0,2,0);
			off100 = offsetBlockPos(temp, 1,0,0);
			off110 = offsetBlockPos(temp, 1,1,0);
			offm100 = offsetBlockPos(temp, -1,0,0);
			offm110 = offsetBlockPos(temp, -1,1,0);
			//东面可放置的坐标
			BlockPos EastCrM = off010;
			BlockPos EastCrH = off020;
			BlockPos EastRedstone1 = offm100;
			BlockPos EastPiston1 = offm110;
			BlockPos EastRedstone2 =off100;
			BlockPos EastPiston2 = off110;

			temp =fAN;
			off010 = offsetBlockPos(temp, 0,1,0);
			off020 = offsetBlockPos(temp, 0,2,0);
			off001 = offsetBlockPos(temp, 0, 0, 1);
			off011 = offsetBlockPos(temp, 0, 1, 1);
			off00m1 = offsetBlockPos(temp, 0, 0 ,-1);
			off01m1 = offsetBlockPos(temp, 0, 1 ,-1);
			//北面可放置的坐标
			BlockPos NorthCrM = off010;
			BlockPos NorthCrH = off020;
			BlockPos NorthRedstone1 = off001;
			BlockPos NorthPiston1 = off011;
			BlockPos NorthRedstone2 = off00m1;
			BlockPos NorthPiston2 = off01m1;

			temp =fAS;
			off010 = offsetBlockPos(temp, 0,1,0);
			off020 = offsetBlockPos(temp, 0,2,0);
			off001 = offsetBlockPos(temp, 0, 0, 1);
			off011 = offsetBlockPos(temp, 0, 1, 1);
			off00m1 = offsetBlockPos(temp, 0, 0 ,-1);
			off01m1 = offsetBlockPos(temp, 0, 1 ,-1);
			//南面可放置的坐标
			BlockPos SouthCrM = off010;
			BlockPos SouthCrH = off020;
			BlockPos SouthRedstone1 = off00m1;
			BlockPos SouthPiston1 = off01m1;
			BlockPos SouthRedstone2 = off001;
			BlockPos SouthPiston2 = off011;

			//清空临时变量
			temp = null;
			off010 = null;
			off020 = null;
			off100 = null;
			off110 = null;
			offm100 = null;
			offm110 = null;
			off001 = null;
			off011 = null;
			off00m1 = null;
			off01m1 = null;

			//如果西面可以放置水晶
			if (canCrystalPlace(fAW)) {
				//如果可以放置红石..
				if (paCanPlace(WestRedstone1, fAW, true)) rsList.add(WestRedstone1);
				if (paCanPlace(WestRedstone2, fAW, true)) rsList.add(WestRedstone2);
				//如果可以放置活塞..
				if (paCanPlace(WestPiston1, fAW, true)) psList.add(WestPiston1);
				if (paCanPlace(WestPiston2, fAW, true)) psList.add(WestPiston2);
			}
			if (canCrystalPlace(fAE)) {
				if (paCanPlace(EastRedstone1, fAE, true)) rsList.add(EastRedstone1);
				if (paCanPlace(EastRedstone2, fAE, true)) rsList.add(EastRedstone2);
				if (paCanPlace(EastPiston1, fAE, true)) psList.add(EastPiston1);
				if (paCanPlace(EastPiston2, fAE, true)) psList.add(EastPiston2);
			}
			if (canCrystalPlace(fAS)) {
				if (paCanPlace(SouthRedstone1, fAS, true)) rsList.add(SouthRedstone1);
				if (paCanPlace(SouthRedstone2, fAS, true)) rsList.add(SouthRedstone2);
				if (paCanPlace(SouthPiston1, fAS, true)) psList.add(SouthPiston1);
				if (paCanPlace(SouthPiston2, fAS, true)) psList.add(SouthPiston2);
			}
			if (canCrystalPlace(fAN)) {
				if (paCanPlace(NorthRedstone1, fAN, true)) rsList.add(NorthRedstone1);
				if (paCanPlace(NorthRedstone2, fAN, true)) rsList.add(NorthRedstone2);
				if (paCanPlace(NorthPiston1, fAN, true)) psList.add(NorthPiston1);
				if (paCanPlace(NorthPiston2, fAN, true)) psList.add(NorthPiston2);
			}
			//在pubList添加所有可以放置的坐标
			pubList.addAll(rsList);
			pubList.addAll(psList);
			//计算最佳放置坐标赋值到bestPos
			BlockPos bestPos = BlockChecker.bestDist(pubList);
			//建立变量mode并初始化为noPOS
			Mode mode = Mode.noPOS;
			//建立变量type并初始化为null
			Type type = null;
			if (bestPos == null) {//如果没有找到可以放置的地方
				Command.sendMessage("no pos found");
				return;
			}
			//计算最佳坐标属于红石还是活塞并赋值mode
			if (rsList.contains(bestPos)) mode = Mode.rsFirst;
			if (psList.contains(bestPos)) mode = Mode.psFirst;

			if (mode == Mode.rsFirst) { //如果红石
				//计算最佳坐标朝向
				if (bestPos == WestRedstone1 | bestPos == WestRedstone2) type = Type.West;
				if (bestPos == EastRedstone1 | bestPos == EastRedstone2) type = Type.East;
				if (bestPos == NorthRedstone1 | bestPos == NorthRedstone2) type = Type.North;
				if (bestPos == SouthRedstone1 | bestPos == SouthRedstone2) type = Type.South;
				if (type != null) {
					if (type == Type.West) {//如果在北面
						//计算活塞坐标
						BlockPos pistonPos = rsBestPlacePos(WestPiston1, WestPiston2, fAW);
						//如果没有可用坐标则返回
						if (pistonPos == null) return;
						BlockPos redstonePos = bestPos;

						//放置红石和活塞
						retryPlace(redstonePos, Blocks.REDSTONE_BLOCK, 2);
						retryPlace(pistonPos, Blocks.PISTON, 5);
						retryPlace(redstonePos, Blocks.REDSTONE_BLOCK, 10);
						//250 sad
					}
					if (type == Type.East) {
						BlockPos pistonPos = rsBestPlacePos(EastPiston1, EastPiston2, fAE);
						if (pistonPos == null) return;
						BlockPos redstonePos = bestPos;

						retryPlace(redstonePos, Blocks.REDSTONE_BLOCK, 2);
						retryPlace(pistonPos, Blocks.PISTON, 5);
						retryPlace(redstonePos, Blocks.REDSTONE_BLOCK, 10);
						//250 sad
					}
					if (type == Type.North) {
						BlockPos pistonPos = rsBestPlacePos(NorthPiston1, NorthPiston2, fAN);
						if (pistonPos == null) return;
						BlockPos redstonePos = bestPos;

						retryPlace(redstonePos, Blocks.REDSTONE_BLOCK, 2);
						retryPlace(pistonPos, Blocks.PISTON, 5);
						retryPlace(redstonePos, Blocks.REDSTONE_BLOCK, 10);
						//250 sad
					}
					if (type == Type.South) {
						BlockPos pistonPos = rsBestPlacePos(SouthPiston1, SouthPiston2, fAS);
						if (pistonPos == null) return;
						BlockPos redstonePos = bestPos;

						retryPlace(redstonePos, Blocks.REDSTONE_BLOCK, 2);
						retryPlace(pistonPos, Blocks.PISTON, 5);
						retryPlace(redstonePos, Blocks.REDSTONE_BLOCK, 10);
						//250 sad
					}
				}
				type = null;
			}
			if (mode == Mode.psFirst) {
				if (bestPos == WestPiston1 | bestPos == WestPiston2) type = Type.West;
				if (bestPos == EastPiston1 | bestPos == EastPiston2) type = Type.East;
				if (bestPos == NorthPiston1 | bestPos == NorthPiston2) type = Type.North;
				if (bestPos == SouthPiston1 | bestPos == SouthPiston2) type = Type.South;
				if (type != null) {
					if (type == Type.West) {
						BlockPos pistonPos = bestPos;
						BlockPos redstonePos = psBestPlacePos(pistonPos, fAW);
						if (redstonePos == null) return;
						retryPlace(pistonPos, Blocks.PISTON, 2);
						retryPlace(redstonePos, Blocks.REDSTONE_BLOCK, 5);
						retryPlace(pistonPos, Blocks.PISTON, 10);
						//250 sad
					}
					if (type == Type.East) {
						BlockPos pistonPos = bestPos;
						BlockPos redstonePos = psBestPlacePos(pistonPos, fAE);
						if (redstonePos == null) return;
						retryPlace(pistonPos, Blocks.PISTON, 2);
						retryPlace(redstonePos, Blocks.REDSTONE_BLOCK, 5);
						retryPlace(pistonPos, Blocks.PISTON, 10);
						//250 sad
					}
					if (type == Type.North) {
						BlockPos pistonPos = bestPos;
						BlockPos redstonePos = psBestPlacePos(pistonPos, fAN);
						if (redstonePos == null) return;
						retryPlace(pistonPos, Blocks.PISTON, 2);
						retryPlace(redstonePos, Blocks.REDSTONE_BLOCK, 5);
						retryPlace(pistonPos, Blocks.PISTON, 10);
						//250 sad
					}
					if (type == Type.South) {
						BlockPos pistonPos = bestPos;
						BlockPos redstonePos = psBestPlacePos(pistonPos, fAS);
						if (redstonePos == null) return;
						retryPlace(pistonPos, Blocks.PISTON, 2);
						retryPlace(redstonePos, Blocks.REDSTONE_BLOCK, 5);
						retryPlace(pistonPos, Blocks.PISTON, 10);
						//250 sad
					}
				}
			}

		}
		private static List<BlockPos> noPlacePos(
			EntityPlayer target,
			boolean East,
			boolean EastNoPlace_A,
			boolean EastNoPlace_B,
			boolean EastNoPlaceC_A,
			boolean EastNoPlaceC_B,
			boolean West,
			boolean WestNoPlace_A,
			boolean WestNoPlace_B,
			boolean WestNoPlaceC_A,
			boolean WestNoPlaceC_B,
			boolean North,
			boolean NorthNoPlace_A,
			boolean NorthNoPlace_B,
			boolean NorthNoPlaceC_A,
			boolean NorthNoPlaceC_B,
			boolean South,
			boolean SouthNoPlace_A,
			boolean SouthNoPlace_B,
			boolean SouthNoPlaceC_A,
			boolean SouthNoPlaceC_B

		) {
			List<BlockPos> poses = new ArrayList<>();
			BlockPos targetHeadPos = getPlayerPos.getFacePos(target);
			BlockPos E = offsetBlockPos(targetHeadPos,1,0,0);
			BlockPos NoPlace1 = offsetBlockPos(E, 1,1,1);
			BlockPos NoPlace2 = offsetBlockPos(E, 1,1,-1);
			BlockPos NoPlaceC1 = offsetBlockPos(E, 0,1,0);
			BlockPos NoPlaceC2 = offsetBlockPos(E, 0,2,0);

			BlockPos W = offsetBlockPos(targetHeadPos,-1,0,0);
			BlockPos NoPlace3 = offsetBlockPos(E, -1,1,-1);
			BlockPos NoPlace4 = offsetBlockPos(E, -1,1,1);
			BlockPos NoPlaceC3 = offsetBlockPos(W, 0,1,0);
			BlockPos NoPlaceC4 = offsetBlockPos(W, 0,2,0);

			BlockPos N = offsetBlockPos(targetHeadPos,0,0,-1);
			BlockPos NoPlace5 = offsetBlockPos(N, 1,1,-1);
			BlockPos NoPlace6 = offsetBlockPos(N, -1,1,-1);
			BlockPos NoPlaceC5 = offsetBlockPos(N, 0,1,0);
			BlockPos NoPlaceC6 = offsetBlockPos(N, 0,2,0);

			BlockPos S = offsetBlockPos(targetHeadPos,0,0,1);
			BlockPos NoPlace7 = offsetBlockPos(S, -1,1,1);
			BlockPos NoPlace8 = offsetBlockPos(S, 1,1,1);
			BlockPos NoPlaceC7 = offsetBlockPos(S, 0,1,0);
			BlockPos NoPlaceC8 = offsetBlockPos(S, 0,2,0);
			if (East) poses.add(E);
			if (EastNoPlace_A) poses.add(NoPlace1);
			if (EastNoPlace_B) poses.add(NoPlace2);
			if (EastNoPlaceC_A) poses.add(NoPlaceC1);
			if (EastNoPlaceC_B) poses.add(NoPlaceC2);
			if (West) poses.add(W);
			if (WestNoPlace_A) poses.add(NoPlace3);
			if (WestNoPlace_B) poses.add(NoPlace4);
			if (WestNoPlaceC_A) poses.add(NoPlaceC3);
			if (WestNoPlaceC_B) poses.add(NoPlaceC4);
			if (North) poses.add(N);
			if (NorthNoPlace_A) poses.add(NoPlace5);
			if (NorthNoPlace_B) poses.add(NoPlace6);
			if (NorthNoPlaceC_A) poses.add(NoPlaceC5);
			if (NorthNoPlaceC_B) poses.add(NoPlaceC6);
			if (South) poses.add(S);
			if (SouthNoPlace_A) poses.add(NoPlace7);
			if (SouthNoPlace_B) poses.add(NoPlace8);
			if (SouthNoPlaceC_A) poses.add(NoPlaceC7);
			if (SouthNoPlaceC_B) poses.add(NoPlaceC8);

			return poses;
		}
		public static void allCheck(EntityPlayer target, boolean debug) {
			newPistonSimply(target, debug, true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true);
		}
		public static void dontCheck(EntityPlayer target, boolean debug) {
			newPistonSimply(target, debug, false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false);
		}
		private static boolean canBlockPlaceCrystal(final BlockPos crystalBasePos) {
			World world = makeUtil.mc.world;
			IBlockState state = world.getBlockState(crystalBasePos);
			Block block = state.getBlock();
			BlockPos up1 = offsetBlockPos(crystalBasePos, 0, 1, 0);
			BlockPos up2 = offsetBlockPos(crystalBasePos, 0, 2, 0);
			AxisAlignedBB aabb = new AxisAlignedBB(up1, up2.add(1, 1, 1));
			List<Entity> entities = world.getEntitiesWithinAABBExcludingEntity(null, aabb);

			return (block == Blocks.OBSIDIAN || block == Blocks.BEDROCK)
				&& (world.isAirBlock(up1) && world.isAirBlock(up2))
				&& entities.isEmpty();
		}
		public static void newPistonSimply(
			EntityPlayer target,
			boolean debug,
			boolean East,
			boolean EastNoPlace_A,
			boolean EastNoPlace_B,
			boolean EastNoPlaceC_A,
			boolean EastNoPlaceC_B,
			boolean West,
			boolean WestNoPlace_A,
			boolean WestNoPlace_B,
			boolean WestNoPlaceC_A,
			boolean WestNoPlaceC_B,
			boolean North,
			boolean NorthNoPlace_A,
			boolean NorthNoPlace_B,
			boolean NorthNoPlaceC_A,
			boolean NorthNoPlaceC_B,
			boolean South,
			boolean SouthNoPlace_A,
			boolean SouthNoPlace_B,
			boolean SouthNoPlaceC_A,
			boolean SouthNoPlaceC_B
		) {
			List<BlockPos> dontPlace = noPlacePos(target, East, EastNoPlace_A, EastNoPlace_B, EastNoPlaceC_A, EastNoPlaceC_B, West, WestNoPlace_A, WestNoPlace_B, WestNoPlaceC_A, WestNoPlaceC_B, North, NorthNoPlace_A, NorthNoPlace_B, NorthNoPlaceC_A, NorthNoPlaceC_B, South, SouthNoPlace_A, SouthNoPlace_B, SouthNoPlaceC_A, SouthNoPlaceC_B);
			if (debug) Command.sendMessage("some pos set to dont place: ");
			for (BlockPos dppos : dontPlace) Command.sendMessage(dppos.toString());
			List<BlockPos> canplacePos = new ArrayList<>();
			BlockPos targetHeadPos = getPlayerPos.getFacePos(target);
			List<BlockPos> EList = new ArrayList<>();
			List<BlockPos> WList = new ArrayList<>();
			List<BlockPos> NList = new ArrayList<>();
			List<BlockPos> SList = new ArrayList<>();
			boolean canE = false;
			boolean canW = false;
			boolean canN = false;
			boolean canS = false;
			BlockPos E = offsetBlockPos(targetHeadPos,1,0,0);
			BlockPos ERS1 = offsetBlockPos(E, 0,0,1);
			BlockPos ERS2 = offsetBlockPos(E, 0,0,-1);
			BlockPos EP1 = ya1(ERS1);
			BlockPos EP2 = ya1(ERS2);
			EList.add(E);
			EList.add(ERS1);
			EList.add(ERS2);
			EList.add(EP1);
			EList.add(EP2);

			BlockPos W = offsetBlockPos(targetHeadPos,-1,0,0);
			BlockPos WRS1 = offsetBlockPos(W, 0,0,-1);
			BlockPos WRS2 = offsetBlockPos(W, 0,0,1);
			BlockPos WP1 = ya1(WRS1);
			BlockPos WP2 = ya1(WRS2);
			WList.add(W);
			WList.add(WRS1);
			WList.add(WRS2);
			WList.add(WP1);
			WList.add(WP2);


			BlockPos N = offsetBlockPos(targetHeadPos,0,0,-1);
			BlockPos NRS1 = offsetBlockPos(N, -1,0,0);
			BlockPos NRS2 = offsetBlockPos(N, 1,0,0);
			BlockPos NP1 = ya1(NRS1);
			BlockPos NP2 = ya1(NRS2);
			NList.add(N);
			NList.add(NRS1);
			NList.add(NRS2);
			NList.add(NP1);
			NList.add(NP2);

			BlockPos S = offsetBlockPos(targetHeadPos,0,0,1);
			BlockPos SRS1 = offsetBlockPos(S, 1,0,0);
			BlockPos SRS2 = offsetBlockPos(S, -1,0,0);
			BlockPos SP1 = ya1(SRS1);
			BlockPos SP2 = ya1(SRS2);
			SList.add(S);
			SList.add(SRS1);
			SList.add(SRS2);
			SList.add(SP1);
			SList.add(SP2);

			if (canBlockPlaceCrystal(E)) canE = true;
			if (canBlockPlaceCrystal(W)) canW = true;
			if (canBlockPlaceCrystal(N)) canN = true;
			if (canBlockPlaceCrystal(S)) canS = true;
			if (canE) {
				if (debug) Command.sendMessage("found East can place..");
				if (BlockChecker.canPlace(ERS1) & (BlockChecker.isAirb(EP1))) canplacePos.add(ERS1);
				if (BlockChecker.canPlace(ERS2) & BlockChecker.isAirb(EP2)) canplacePos.add(ERS2);
			}
			if (canW) {
				if (debug) Command.sendMessage("found West can place..");
				if (BlockChecker.canPlace(WRS1) & BlockChecker.isAirb(WP1)) canplacePos.add(WRS1);
				if (BlockChecker.canPlace(WRS2) & BlockChecker.isAirb(WP2)) canplacePos.add(WRS2);
			}
			if (canN) {
				if (debug) Command.sendMessage("found North can place..");
				if (BlockChecker.canPlace(NRS1) & BlockChecker.isAirb(NP1)) canplacePos.add(NRS1);
				if (BlockChecker.canPlace(NRS2) & BlockChecker.isAirb(NP2)) canplacePos.add(NRS2);
			}
			if (canS) {
				if (debug) Command.sendMessage("found South can place..");
				if (BlockChecker.canPlace(SRS1) & BlockChecker.isAirb(SP1)) canplacePos.add(SRS1);
				if (BlockChecker.canPlace(SRS2) & BlockChecker.isAirb(SP2)) canplacePos.add(SRS2);
			}
				for (BlockPos noPlaces : dontPlace) {
					if (debug) Command.sendMessage("removed " + noPlaces);
					canplacePos.remove(noPlaces);
					if (canplacePos.isEmpty()) if (debug) Command.sendMessage("now no pos have..");
				}
			for (BlockPos canPlaceList : canplacePos) {
				Command.sendMessage("geted can place list:");
				Command.sendMessage(canPlaceList.toString());
			}

			BlockPos bestPosRS = BlockChecker.bestDist(canplacePos);
			if (bestPosRS != null) {
				BlockPos pistonPos = ya1(bestPosRS);
				if (debug) Command.sendMessage("try place REDSTONE" + bestPosRS);
				retryPlace(bestPosRS, Blocks.REDSTONE_BLOCK, 2, debug);
				if (debug) Command.sendMessage("try place PISTON" + pistonPos);
				silentPlace2(pistonPos, Blocks.PISTON);
				retryPlace(pistonPos, Blocks.PISTON, 2, debug);
				if (BlockChecker.getBlockType(pistonPos) instanceof BlockAir) return;
				if (EList.contains(bestPosRS)) {
					if (debug) Command.sendMessage("try place Crystal" + E);
					BlockUtil.placeCrystalOnBlock(E, EnumHand.MAIN_HAND, false, false, true);
				}
				if (WList.contains(bestPosRS)) {
					if (debug) Command.sendMessage("try place Crystal" + W);
					BlockUtil.placeCrystalOnBlock(W, EnumHand.MAIN_HAND, false, false, true);
				}
				if (NList.contains(bestPosRS)) {
					if (debug) Command.sendMessage("try place Crystal" + N);
					BlockUtil.placeCrystalOnBlock(N, EnumHand.MAIN_HAND, false, false, true);
				}
				if (SList.contains(bestPosRS)) {
					if (debug) Command.sendMessage("try place Crystal" + S);
					BlockUtil.placeCrystalOnBlock(S, EnumHand.MAIN_HAND, false, false, true);
				}
			} else if (debug) Command.sendMessage("no pos found to place");
		}
		private static BlockPos ya1(BlockPos pos) {
			return new BlockPos(pos.getX(), pos.getY()+1, pos.getZ());
		}

		private enum Mode {rsFirst, psFirst, noPOS}
		private enum Type {West,East,North,South}
	}
	public static boolean canCrystalPlace(BlockPos crystalBasePos) {
		BlockPos mainPos = offsetBlockPos(crystalBasePos, 0, 1, 0);
		BlockPos headPos =  offsetBlockPos(crystalBasePos, 0, 2, 0);
		AxisAlignedBB placebox = new AxisAlignedBB(mainPos, headPos);
		return mc.world.getCollisionBoxes(null, placebox).isEmpty();
	}

	public static BlockPos offsetBlockPos(BlockPos pos, int offX, int offY, int offZ) {
		return new BlockPos(pos.getX() + offX, pos.getY() + offY, pos.getZ() + offZ);
	}
	public static boolean paCanPlace(BlockPos pos, BlockPos basePos) {
		List<BlockPos> poses = new ArrayList<>();
		List<BlockPos> canPlacePoses = new ArrayList<>();
		BlockPos mainPos = offsetBlockPos(basePos, 0, 1, 0);
		BlockPos headPos =  offsetBlockPos(basePos, 0, 2, 0);
		BlockPos a1 = offsetBlockPos(pos, 1, 0, 0);
		BlockPos a2 = offsetBlockPos(pos, -1, 0, 0);
		BlockPos a3 = offsetBlockPos(pos, 0, 1, 0);
		BlockPos a4 = offsetBlockPos(pos, 0, -1, 0);
		BlockPos a5 = offsetBlockPos(pos, 0, 0, 1);
		BlockPos a6 = offsetBlockPos(pos, 0, 0, -1);
		poses.add (a1);
		poses.add (a2);
		poses.add (a3);
		poses.add (a4);
		poses.add (a5);
		poses.add (a6);
		poses.remove(mainPos);
		poses.remove(headPos);
		for (BlockPos pos2 : poses)
			if (BlockChecker.getBlockType(pos2) instanceof BlockAir) canPlacePoses.add(pos2);

		return !canPlacePoses.isEmpty();
	}
	public static boolean paCanPlace(BlockPos pos, BlockPos basePos, boolean checkSelfcanPlace) {
		List<BlockPos> poses = new ArrayList<>();
		List<BlockPos> canPlacePoses = new ArrayList<>();
		BlockPos mainPos = offsetBlockPos(basePos, 0, 1, 0);
		BlockPos headPos =  offsetBlockPos(basePos, 0, 2, 0);
		BlockPos a1 = offsetBlockPos(pos, 1, 0, 0);
		BlockPos a2 = offsetBlockPos(pos, -1, 0, 0);
		BlockPos a3 = offsetBlockPos(pos, 0, 1, 0);
		BlockPos a4 = offsetBlockPos(pos, 0, -1, 0);
		BlockPos a5 = offsetBlockPos(pos, 0, 0, 1);
		BlockPos a6 = offsetBlockPos(pos, 0, 0, -1);
		poses.add (a1);
		poses.add (a2);
		poses.add (a3);
		poses.add (a4);
		poses.add (a5);
		poses.add (a6);
		poses.remove(mainPos);
		poses.remove(headPos);
		for (BlockPos pos2 : poses)
			if (BlockChecker.getBlockType(pos2) instanceof BlockAir) canPlacePoses.add(pos2);

		if (checkSelfcanPlace & !BlockChecker.canPlace(pos)) return false;
		return !canPlacePoses.isEmpty();
	}
	public static List<BlockPos> BlockNearPos(BlockPos pos) {
		List<BlockPos> temp = new ArrayList<>();
		BlockPos a1 = offsetBlockPos(pos, 1, 0, 0);
		BlockPos a2 = offsetBlockPos(pos, -1, 0, 0);
		BlockPos a3 = offsetBlockPos(pos, 0, 1, 0);
		BlockPos a4 = offsetBlockPos(pos, 0, -1, 0);
		BlockPos a5 = offsetBlockPos(pos, 0, 0, 1);
		BlockPos a6 = offsetBlockPos(pos, 0, 0, -1);
		temp.add(a1);
		temp.add(a2);
		temp.add(a3);
		temp.add(a4);
		temp.add(a5);
		temp.add(a6);
		return temp;
	}
	public static BlockPos psBestPlacePos(BlockPos psPos, BlockPos crBasePos) {
		BlockPos mainPos = offsetBlockPos(crBasePos, 0, 1, 0);
		BlockPos headPos =  offsetBlockPos(crBasePos, 0, 2, 0);

		List<BlockPos> poses = new ArrayList<>();
		List<BlockPos> canPlacePoses = new ArrayList<>();
		BlockPos a1 = offsetBlockPos(psPos, 1, 0, 0);
		BlockPos a2 = offsetBlockPos(psPos, -1, 0, 0);
		BlockPos a3 = offsetBlockPos(psPos, 0, 1, 0);
		BlockPos a4 = offsetBlockPos(psPos, 0, -1, 0);
		BlockPos a5 = offsetBlockPos(psPos, 0, 0, 1);
		BlockPos a6 = offsetBlockPos(psPos, 0, 0, -1);
		poses.add (a1);
		poses.add (a2);
		poses.add (a3);
		poses.add (a4);
		poses.add (a5);
		poses.add (a6);
		poses.remove(mainPos);
		poses.remove(headPos);
		for (BlockPos pos2 : poses)
			if (BlockChecker.getBlockType(pos2) instanceof BlockAir) canPlacePoses.add(pos2);
		return BlockChecker.bestDist(canPlacePoses);
	}
	public static BlockPos rsBestPlacePos(BlockPos psPos1, BlockPos psPos2, BlockPos crBasePos) {
		BlockPos mainPos = offsetBlockPos(crBasePos, 0, 1, 0);
		BlockPos headPos =  offsetBlockPos(crBasePos, 0, 2, 0);

		List<BlockPos> poses = new ArrayList<>();
		List<BlockPos> canPlacePoses = new ArrayList<>();
		if (!(BlockChecker.getBlockType(psPos1) instanceof BlockAir)) poses.add(psPos1);
		if (!(BlockChecker.getBlockType(psPos2) instanceof BlockAir)) poses.add(psPos2);

		poses.remove(mainPos);
		poses.remove(headPos);
		for (BlockPos pos2 : poses)
			if (BlockChecker.getBlockType(pos2) instanceof BlockAir) canPlacePoses.add(pos2);
		return BlockChecker.bestDist(canPlacePoses);
	}
	public static void clickBlock(BlockPos breakPos, boolean debug) {

		List<Block> GodBlock = Arrays.asList(
			Blocks.AIR,
			Blocks.FLOWING_LAVA,
			Blocks.LAVA,
			Blocks.FLOWING_WATER,
			Blocks.WATER,
			Blocks.BEDROCK
		);
		if (breakPos == null) {
			if (debug) Command.sendMessage("null pos");
			return;
		}

		if (!GodBlock.contains(BlockChecker.getBlockType(breakPos))) {
			if (debug) Command.sendMessage("trying click block on: " + breakPos);
			mc.playerController.onPlayerDamageBlock(breakPos, BlockUtil.getRayTraceFacing(breakPos));
			mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, breakPos, BlockUtil.getRayTraceFacing(breakPos)));
		} else Command.sendMessage("godblock cannot break");

	}
	public static EntityPlayer getTarget(final Double range) {
			EntityPlayer target = null;
			double distance = Math.pow(range, 2.0) + 1.0;
			for (final EntityPlayer player : mc.world.playerEntities) {
				if (!EntityUtil.isntValid(player, range)) {
					if (Client.speedManager.getPlayerSpeed(player) > 10.0) continue;
					if (target != null) if (mc.player.getDistanceSq(player) >= distance) continue;
					target = player;
					distance = mc.player.getDistanceSq(player);
				}
			}
			return target;
	}
	public static void rotateToPosition(BlockPos pos) {
		Client.rotationManager.setRotation(
			BlockChecker.getLegitRotations(pos.add(0.5,0.5,0.5))[0],
			BlockChecker.getLegitRotations(pos.add(0.5,0.5,0.5))[1]
		);
	}
	public static boolean canPlace(BlockPos pos, boolean pfCheck, boolean phCheck, boolean phCheck2, boolean peCheck) {
		boolean posFree = !pfCheck;
		boolean posHasHelp = !phCheck;
		boolean posNoEntity = !peCheck;

		if (isBlockAir(pos)) posFree = true;
		if (phCheck2) {
			if (liteHelpingBlockCheck(pos)) posHasHelp = true;
		} else if (isBlockHasHelpingBlock(pos)) posHasHelp = true;
		if (isPosNoEntity(pos)) posNoEntity = true;
		return posFree && posHasHelp && posNoEntity;
	}
	public static boolean isBlockHasHelpingBlock(BlockPos pos) {
		List<BlockPos> enumList =  Arrays.asList(pos.up(), pos.down(), pos.east(), pos.west(), pos.north(), pos.south());
		for (BlockPos ienum : enumList) if (isBlockFull(ienum)) return true;
		return false;
	}
	public static boolean liteHelpingBlockCheck(BlockPos pos) {
		List<BlockPos> enumList =  Arrays.asList(pos.up(), pos.down(), pos.east(), pos.west(), pos.north(), pos.south());
		for (BlockPos ienum : enumList) if (!isBlockAir(ienum)) return true;
		return false;
	}
	public static boolean isBlockAir(BlockPos pos) {
		IBlockState state = mc.world.getBlockState(pos);
		return state instanceof BlockAir || state instanceof BlockLiquid;
	}
	public static boolean isBlockFull(BlockPos pos) {
		IBlockState state = mc.world.getBlockState(pos);
		return state.isFullBlock();
	}
	public static boolean canBurrowPlace(BlockPos pos) {
		Block self = mc.world.getBlockState(pos).getBlock();
		Block down = mc.world.getBlockState(pos.down()).getBlock();
		boolean nonweb = !mc.player.isInWeb;
		boolean blockFree = false;
		boolean downFull = false;
		List<Block> combatBlocks = Arrays.asList(Blocks.BEDROCK, Blocks.OBSIDIAN, Blocks.REDSTONE_BLOCK, Blocks.PISTON);
		if (self instanceof BlockAir) blockFree = true;
		if (combatBlocks.contains(down)) downFull = true;
		return blockFree && downFull && nonweb;
	}

	public static boolean isPosNoEntity(BlockPos d) {
		return mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(d)).isEmpty();
	}
	public static void safeBurrow(boolean safePlace, BlockPos pos, EnumHand hand, boolean rotate, boolean packet) {
		if (safePlace)
			if (!canBurrowPlace(pos))
				return;
		BlockUtil.burrowPlaceSync(pos, hand, rotate, packet);
	}
	public static void clckBlock2(BlockPos pos, rmode rmode, dormode dormode, boolean rotate) {
		if (rotate) {
			if (dormode == makeUtil.dormode.a || dormode == makeUtil.dormode.both) {
				if (rmode == makeUtil.rmode.both || rmode == makeUtil.rmode.rotateToPosition)
					makeUtil.rotateToPosition(pos);
				if (rmode == makeUtil.rmode.both || rmode == makeUtil.rmode.facePosFacing)
					RebirthUtil.facePosFacing(pos, BlockUtil.getRayTraceFacing(pos));
			}
		}
		mc.world.getBlockState(pos).getBlock();
		mc.playerController.onPlayerDamageBlock(pos, BlockUtil.getRayTraceFacing(pos));
		if (rotate) {
			if (dormode == makeUtil.dormode.b || dormode == makeUtil.dormode.both) {
				if (rmode == makeUtil.rmode.both || rmode == makeUtil.rmode.rotateToPosition)
					makeUtil.rotateToPosition(pos);
				if (rmode == makeUtil.rmode.both || rmode == makeUtil.rmode.facePosFacing)
					RebirthUtil.facePosFacing(pos, BlockUtil.getRayTraceFacing(pos));
			}
		}
	}
	public enum rmode{facePosFacing, rotateToPosition, both}
	public enum dormode{a,b,both}
}

