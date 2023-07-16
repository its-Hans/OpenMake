package cn.make.util;

import cn.make.util.skid.EntityUtil;
import cn.make.util.skid.RebirthUtil;
import chad.phobos.Client;
import cn.make.util.skid.two.BlockUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.*;

import java.util.*;

public class UtilsRewrite {
	static Minecraft mc = Minecraft.getMinecraft();

	public static class uBlock {
		public static BlockPos addPos(BlockPos pos1, BlockPos pos2) {
			int x = (pos1.getX() + pos2.getX());
			int y = (pos1.getY() + pos2.getY());
			int z = (pos1.getZ() + pos2.getZ());
			return new BlockPos(x, y, z);
		}

		public static void placeBlock(BlockPos pos, EnumFacing placeFacing, Block blockType, boolean swap, boolean swapback, boolean packet, boolean rotate) {
			if (
				pos == null
				|| blockType == null
			) return;
			int blockSlot = uInventory.itemSlot(Item.getItemFromBlock(blockType));
			int oldslot = mc.player.inventory.currentItem;
			if (swap) {
				uInventory.heldItemChange(blockSlot, true, false, true);
			}
			if (placeFacing != null) {
				float[] fac = uRotation.getFacingPlace(placeFacing);
				RebirthUtil.faceYawAndPitch(fac[0], fac[1]);
			}
			BlockUtil.placeBlock(pos, EnumHand.MAIN_HAND, rotate, packet, false);
			if (swapback) {
				uInventory.heldItemChange(oldslot, true, false, true);
			}
		}

		public static void doPlace2(BlockPos pos, EnumFacing fac, boolean rotate, boolean packet, Block block) {
			if (fac != null) {
				float[] facing = uRotation.getFacingPlace(fac);
				RebirthUtil.faceYawAndPitch(facing[0], facing[1]);
			}
			int var3 = mc.player.inventory.currentItem;
			int swapTo = uInventory.itemSlot(Item.getItemFromBlock(block));
			if (swapTo != -1) RebirthUtil.doSwap(swapTo);
			BlockUtil.placeBlock(pos, EnumHand.MAIN_HAND, false, packet);
			RebirthUtil.doSwap(var3);
			if (rotate) {
				RebirthUtil.facePlacePos(pos);
			}
		}

		public static void doPlaceNoSwing(BlockPos pos, EnumFacing fac, boolean rotate, boolean packet, Block block) {
			if (fac != null) {
				float[] facing = uRotation.getFacingPlace(fac);
				RebirthUtil.faceYawAndPitch(facing[0], facing[1]);
			}
			int var3 = mc.player.inventory.currentItem;
			int swapTo = uInventory.itemSlot(Item.getItemFromBlock(block));
			if (swapTo != -1) RebirthUtil.doSwap(swapTo);
			BlockUtil.noSwingPlace(pos, EnumHand.MAIN_HAND, false, packet);
			RebirthUtil.doSwap(var3);
			if (rotate) {
				RebirthUtil.facePlacePos(pos);
			}
		}
		public static void placeUseSlot(BlockPos pos, EnumFacing fac, boolean rotate, boolean packet, int slot) {
			if (fac != null) {
				float[] facing = uRotation.getFacingPlace(fac);
				RebirthUtil.faceYawAndPitch(facing[0], facing[1]);
			}
			int var3 = mc.player.inventory.currentItem;
			if (slot != -1) RebirthUtil.doSwap(slot);
			BlockUtil.placeBlock(pos, EnumHand.MAIN_HAND, false, packet);
			RebirthUtil.doSwap(var3);
			if (rotate) {
				RebirthUtil.facePlacePos(pos);
			}
		}
		public static void placeUseSlotNoSwing(BlockPos pos, EnumFacing fac, boolean rotate, boolean packet, int slot) {
			if (fac != null) {
				float[] facing = uRotation.getFacingPlace(fac);
				RebirthUtil.faceYawAndPitch(facing[0], facing[1]);
			}
			int var3 = mc.player.inventory.currentItem;
			if (slot != -1) RebirthUtil.doSwap(slot);
			BlockUtil.noSwingPlace(pos, EnumHand.MAIN_HAND, false, packet);
			RebirthUtil.doSwap(var3);
			if (rotate) {
				RebirthUtil.facePlacePos(pos);
			}
		}

		public static IBlockState getState(BlockPos pos) {
			return mc.world.getBlockState(pos);
		}

		public static Block getBlock(BlockPos pos) {
			return getState(pos).getBlock();
		}

		public static boolean isBlockAir(BlockPos pos) {
			IBlockState state = getState(pos);
			return state instanceof BlockAir || state instanceof BlockLiquid;
		}

		public static boolean isBlockFull(BlockPos pos) {
			IBlockState state = getState(pos);
			return state.isFullBlock();
		}

		public static boolean hasHelping(BlockPos pos) {
			/*
			BlockPos[] offsets = new posHelper(mc.player).offsetList(pos);
			for (BlockPos _pos : offsets) {
				if (isBlockFull(_pos)) return true;
			}
			return false;
			 */
			return findHelping(pos) != null;
		}
		public static BlockPos findHelping(BlockPos main) {
			BlockPos[] offsets = new posHelper(mc.player).offsetList(main);
			List<BlockPos> canHelpingList = new ArrayList<>();
			for (BlockPos _pos : offsets) {
				if (!RebirthUtil.canReplace(_pos)) canHelpingList.add(_pos);
			}
			return uBlock.bestDistance(canHelpingList);
		}

		public static boolean canPlace(BlockPos pos) {
			boolean posNoEntity = noEntity(pos);
			boolean posHasHelp = hasHelping(pos);
			boolean posNoBlock = isBlockAir(pos);
			return posNoEntity && posHasHelp && posNoBlock;
		}
		public static boolean customCanPlace(BlockPos pos, Block[] airBlocks) {
			boolean posNoEntity = noEntity(pos);
			boolean posHasHelp = hasHelping(pos);
			boolean posNoBlock = posOnType(pos, airBlocks);
			return posNoEntity && posHasHelp && posNoBlock;
		}
		public static boolean fullCustomCanPlace(BlockPos pos, boolean pne, boolean phh, boolean pnb, Block[] airBlocks) {
			boolean posNoEntity;
			boolean posHasHelp;
			boolean posNoBlock;
			if (pne) {
				posNoEntity = noEntity(pos);
			} else {
				posNoEntity = true;
			}

			if (phh) {
				posHasHelp = hasHelping(pos);
			} else {
				posHasHelp = true;
			}

			if (pnb) {
				posNoBlock = posOnType(pos, airBlocks);
			} else {
				posNoBlock = true;
			}
			return posNoEntity && posHasHelp && posNoBlock;
		}
		public static boolean posOnType(BlockPos pos, Block[] types) {
			Block block = getBlock(pos);
			List<Block> typelist = new ArrayList<>();
			Collections.addAll(typelist, types);
			return typelist.contains(block);
		}
		public static boolean posOnType(BlockPos pos, List<Block> types) {
			Block block = getBlock(pos);
			return types.contains(block);
		}

		public static boolean noEntity(BlockPos pos) {
			List<Entity> posEntities = mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos));
			for (Entity var2 : posEntities) {
				if (!(
					(var2 instanceof EntityItem)
						|| (var2 instanceof EntityXPOrb)
						|| (var2 instanceof EntityExpBottle)
						|| (var2 instanceof EntityArrow)
				)) {
					return false;
				}
			}
			return true;
		}

		public static double getRange(BlockPos pos) {
			return mc.player.getPositionVector().distanceTo(new Vec3d(pos));
		}

		public enum digType {
			START(CPacketPlayerDigging.Action.START_DESTROY_BLOCK),
			STOP(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK),
			ABORT(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK);
			final CPacketPlayerDigging.Action action;
			digType(CPacketPlayerDigging.Action _action) {
				action = _action;
			}
			public CPacketPlayerDigging.Action getType() {
				return action;
			}
		}

		public static void digBlock(digType type, BlockPos pos, EnumFacing facing) {
			PacketCenter.sP(new CPacketPlayerDigging(type.getType(), pos, facing));
		}

		public static void clickBlock(BlockPos pos) {
			final EnumFacing legitFace = UtilsRewrite.uRotation.getBestFacing(pos);
			RebirthUtil.facePosFacing(pos, legitFace);
			mc.world.getBlockState(pos).getBlock();
			mc.playerController.onPlayerDamageBlock(pos, legitFace);
		}

		public static BlockPos bestDistance(BlockPos[] position) {
			BlockPos playerPos = mc.player.getPosition();
			double minDistance = Double.MAX_VALUE;
			BlockPos closestPos = null;
			List<BlockPos> positions = new ArrayList<>();
			for (BlockPos pos: position) {
				if (pos != null) positions.add(pos);
			}
			for (BlockPos pos : positions) {
				double distance = playerPos.distanceSq(pos);
				if (distance < minDistance) {
					minDistance = distance;
					closestPos = pos;
				}
			}
			return closestPos;
		}
		public static BlockPos bestDistance(List<BlockPos> position) {
			BlockPos playerPos = mc.player.getPosition();
			double minDistance = Double.MAX_VALUE;
			BlockPos closestPos = null;
			List<BlockPos> positions = new ArrayList<>();
			for (BlockPos pos: position) {
				if (pos != null) positions.add(pos);
			}
			for (BlockPos pos : positions) {
				double distance = playerPos.distanceSq(pos);
				if (distance < minDistance) {
					minDistance = distance;
					closestPos = pos;
				}
			}
			return closestPos;
		}
	}
	public static class uRotation {
		public static EnumFacing getBestFacing(BlockPos pos) {
			rotateutils.idk[] idks = rotateutils.offsetsBlockPos(pos);
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
		public static void lookAtVec3d(Vec3d vec3d) {
			float[] angle = calculateAngle(
				mc.player.getPositionEyes(mc.getRenderPartialTicks()),
				new Vec3d(vec3d.x, vec3d.y, vec3d.z)
			);
			mc.player.rotationPitch = angle[1];
			mc.player.rotationYaw = angle[0];
		}
		public static float[] calculateAngle(Vec3d from, Vec3d to) {
			double difX = to.x - from.x;
			double difY = (to.y - from.y) * -1.0;
			double difZ = to.z - from.z;
			double dist = MathHelper.sqrt(difX * difX + difZ * difZ);
			float yD = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difZ, difX)) - 90.0);
			float pD = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difY, dist)));
			if (pD > 90F) {
				pD = 90F;
			} else if (pD < -90F) {
				pD = -90F;
			}
			return new float[]{yD, pD};
		}
		public static float[] getFacingPlace(EnumFacing facing) {
			float[] fac = new float[] {
				60F,
				60F
			};
			if (facing == EnumFacing.EAST) {
				fac = new float[] {-90.0F, 5.0F};
			} else if (facing == EnumFacing.WEST) {
				fac = new float[] {90.0F, 5.0F};
			} else if (facing == EnumFacing.NORTH) {
				fac = new float[] {180.0F, 5.0F};
			} else if (facing == EnumFacing.SOUTH) {
				fac = new float[] {0.0F, 5.0F};
			}
			return fac;
		}

		/**
		 * Finds all the visible sides of a certain position
		 * @param position The position to find all the visible sides of
		 * @return List of visible sides
		 */
		public List<EnumFacing> getVisibleSides(BlockPos position) {
			List<EnumFacing> visibleSides = new ArrayList<>();

			// pos vector
			Vec3d positionVector = new Vec3d(position).add(0.5, 0.5, 0.5);

			// facing
			double facingX = mc.player.getPositionEyes(1).x - positionVector.x;
			double facingY = mc.player.getPositionEyes(1).y - positionVector.y;
			double facingZ = mc.player.getPositionEyes(1).z - positionVector.z;

			// x
			{
				if (facingX < -0.5) {
					visibleSides.add(EnumFacing.WEST);
				}

				else if (facingX > 0.5) {
					visibleSides.add(EnumFacing.EAST);
				}

				else if (!mc.world.getBlockState(position).isFullBlock() || !mc.world.isAirBlock(position)) {
					visibleSides.add(EnumFacing.WEST);
					visibleSides.add(EnumFacing.EAST);
				}
			}

			// y
			{
				if (facingY < -0.5) {
					visibleSides.add(EnumFacing.DOWN);
				}

				else if (facingY > 0.5) {
					visibleSides.add(EnumFacing.UP);
				}

				else {
					visibleSides.add(EnumFacing.DOWN);
					visibleSides.add(EnumFacing.UP);
				}
			}

			// z
			{
				if (facingZ < -0.5) {
					visibleSides.add(EnumFacing.NORTH);
				}

				else if (facingZ > 0.5) {
					visibleSides.add(EnumFacing.SOUTH);
				}

				else if (!mc.world.getBlockState(position).isFullBlock() || !mc.world.isAirBlock(position)) {
					visibleSides.add(EnumFacing.NORTH);
					visibleSides.add(EnumFacing.SOUTH);
				}
			}

			return visibleSides;
		}
	}
	public static class uInventory {
		public static void heldItemChange(int item, boolean setCur, boolean packet, boolean updCon) {
			//int oldslot = mc.player.inventory.currentItem;

			if (setCur) mc.player.inventory.currentItem = item;
			if (packet) mc.player.connection.sendPacket(new CPacketHeldItemChange(item));
			if (updCon) mc.playerController.updateController();

		}

		public static int itemSlot(Item input) {
			for (int i = 0; i < 9; ++i) {
				Item item = mc.player.inventory.getStackInSlot(i).getItem();
				if (Item.getIdFromItem(item) != Item.getIdFromItem(input)) continue;
				return i;
			}
			return -1;
		}
	}
	public static class posHelper {

		EntityPlayer player;
		public posHelper(EntityPlayer _player) {
			player = _player;
		}
		public void a (){
			ArrayList<BlockPos> a = new ArrayList<>();
		}
		public enum offsets {
			NORTH(0, 0, -1),
			EAST(1, 0, 0),
			SOUTH(0, 0, 1),
			WEST(-1, 0, 0),
			DOWN(0, -1, 0),
			UP(0, 1, 0);
			final int x;
			final int y;
			final int z;
			offsets(int offX, int offY, int offZ) {
				x = offX;
				y = offY;
				z = offZ;
			}
			public BlockPos get() {
				return new BlockPos(x, y, z);
			}
			public BlockPos add(BlockPos main) {
				return uBlock.addPos(main, get());
			}
		}
		public Vec3d posEye() {
			return new Vec3d(
				player.posX,
				player.posY + player.getEyeHeight(),
				player.posZ
			);
		}
		public Vec3d posFeet() {
			return new Vec3d(
				player.posX,
				player.posY,
				player.posZ
			);
		}
		public Vec3d posPlayer() {
			return new Vec3d(
				player.posX,
				player.posY,
				player.posZ
			);
		}

		public BlockPos getFeetBlock() {
			Vec3d burrow = posFeet();
			return new BlockPos(burrow);
		}
		public BlockPos getFaceBlock() {
			return getFeetBlock().up();
		}
		public BlockPos getHeadBlock() {
			return getFeetBlock().up(2);
		}
		public BlockPos surroundEast() {
			return offsets.EAST.add(getFeetBlock());
		}
		public BlockPos surroundWest() {
			return offsets.WEST.add(getFeetBlock());
		}
		public BlockPos surroundSouth() {
			return offsets.SOUTH.add(getFeetBlock());
		}
		public BlockPos surroundNorth() {
			return offsets.NORTH.add(getFeetBlock());
		}
		public BlockPos faceEast() {
			return surroundEast().up();
		}
		public BlockPos faceWest() {
			return surroundWest().up();
		}
		public BlockPos faceSouth() {
			return surroundSouth().up();
		}
		public BlockPos faceNorth() {
			return surroundNorth().up();
		}
		public BlockPos[] surroundList() {
			return new BlockPos[] {
				surroundEast(),
				surroundWest(),
				surroundSouth(),
				surroundNorth()
			};
		}
		public BlockPos[] pistonList() {
			return new BlockPos[] {
				faceEast(),
				faceWest(),
				faceSouth(),
				faceNorth()
			};
		}
		public BlockPos[] offsetList(BlockPos main) {
			return new BlockPos[] {
				offsets.NORTH.add(main),
				offsets.SOUTH.add(main),
				offsets.WEST.add(main),
				offsets.EAST.add(main),
				offsets.DOWN.add(main),
				offsets.UP.add(main)
			};
		}
		public BlockPos[] noHoriOffsets(BlockPos main) {
			return new BlockPos[] {
				offsets.NORTH.add(main),
				offsets.SOUTH.add(main),
				offsets.WEST.add(main),
				offsets.EAST.add(main)
			};
		}
	}
	public static class moduleHelper {
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
	}
	public static class helperEF {
		public static helperEF hef;
		public helperEF() {
			hef = new helperEF();
		}

		public Vec3i posAdd(Vec3i pos, Vec3i pos2) {
			return new Vec3i((pos.getX() + pos2.getX()), (pos.getY() + pos2.getY()), (pos.getZ() + pos2.getZ()));
		}
		public BlockPos posAdd(BlockPos pos, BlockPos pos2) {
			return new BlockPos((pos.getX() + pos2.getX()), (pos.getY() + pos2.getY()), (pos.getZ() + pos2.getZ()));
		}
		public Vec3i posMinus(Vec3i pos, Vec3i pos2) {
			return new Vec3i((pos.getX() - pos2.getX()), (pos.getY() - pos2.getY()), (pos.getZ() - pos2.getZ()));
		}
		public BlockPos posMinus(BlockPos pos, BlockPos pos2) {
			return new BlockPos((pos.getX() - pos2.getX()), (pos.getY() - pos2.getY()), (pos.getZ() - pos2.getZ()));
		}
		public enum facingHelper {
			NORTH(EnumFacing.NORTH),
			SOUTH(EnumFacing.SOUTH),
			WEST(EnumFacing.WEST),
			EAST(EnumFacing.EAST);
			final EnumFacing facing;

			facingHelper(EnumFacing _facing) {
				facing =_facing;
			}

			public BlockPos getOffsetFac() {
				return getOffsets()[0];
			}
			public BlockPos getOffsetFacPos(BlockPos main) {
				return hef.posAdd(main, getOffsetFac());
			}
			public BlockPos getOffsetLeft() {
				return getOffsets()[1];
			}
			public BlockPos getOffsetLeftPos(BlockPos main) {
				return hef.posAdd(main, getOffsetLeft());
			}
			public BlockPos getOffsetRight() {
				return getOffsets()[2];
			}
			public BlockPos getOffsetRightPos(BlockPos main) {
				return hef.posAdd(main, getOffsetRight());
			}
			public BlockPos getOffsetForward(int forwards) {
				BlockPos forward;
				switch (facing) {
					case NORTH: {
						forward = new BlockPos(0, 0, (-1 - forwards));
						break;
					}
					case SOUTH: {
						forward = new BlockPos(0, 0, (1 + forwards));
						break;
					}
					case EAST: {
						forward = new BlockPos((1 + forwards), 0, 0);
						break;
					}
					case WEST: {
						forward = new BlockPos((-1 + forwards), 0, 0);
						break;
					}
					default: {
						forward = null;
						break;
					}
				}
				return forward;
			}
			public BlockPos getOffsetForwardPos(BlockPos main, int forwards) {
				return hef.posAdd(main, getOffsetForward(forwards));
			}
			public BlockPos[] getOffsets() {
				BlockPos fac;
				BlockPos left;
				BlockPos right;
				BlockPos oppoFac;
				switch (facing) {
					case NORTH: {
						fac = new BlockPos(0, 0, -1);
						left = new BlockPos(-1, 0, -1);
						right = new BlockPos(1, 0, -1);
						oppoFac = new BlockPos(0, 0, 1);
						break;
					}
					case SOUTH: {
						fac = new BlockPos(0, 0, 1);
						left = new BlockPos(1, 0, 1);
						right = new BlockPos(-1, 0, 1);
						oppoFac = new BlockPos(0, 0, -1);
						break;
					}
					case EAST: {
						fac = new BlockPos(1, 0, 0);
						left = new BlockPos(1, 0, -1);
						right = new BlockPos(1, 0, 1);
						oppoFac = new BlockPos(-1, 0, 0);
						break;
					}
					case WEST: {
						fac = new BlockPos(-1, 0, 0);
						left = new BlockPos(-1, 0, 1);
						right = new BlockPos(-1, 0, -1);
						oppoFac = new BlockPos(1, 0, 0);
						break;
					}
					default: {
						fac = null;
						left = null;
						right = null;
						oppoFac = null;
						break;
					}
				}
				return new BlockPos[] {
					fac,
					left,
					right,
					oppoFac
				};
			}
			public BlockPos[] getOffsetsFacPos(BlockPos main) {
				List<BlockPos> offs = new ArrayList<>();
				for (BlockPos off : getOffsets()) {
					offs.add(hef.posAdd(main, off));
				}
				return offs.toArray(new BlockPos[0]);
			}
		}
	}
}
