package cn.make.util.skid;

import chad.phobos.api.events.player.MoveEvent;
import cn.make.module.render.PlaceRender;
import chad.phobos.Client;
import chad.phobos.api.center.Command;
import chad.phobos.api.center.Util;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.Slot;
import net.minecraft.item.*;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.Explosion;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.*;
import java.util.List;

public class RebirthUtil implements Util {

	static final Timer breakTimer = new Timer();

	public static final List<Block> canUseList = Arrays.asList(
		Blocks.ENDER_CHEST,
		Blocks.CHEST,
		Blocks.TRAPPED_CHEST,
		Blocks.CRAFTING_TABLE,
		Blocks.ANVIL,
		Blocks.BREWING_STAND,
		Blocks.HOPPER,
		Blocks.DROPPER,
		Blocks.DISPENSER,
		Blocks.TRAPDOOR,
		Blocks.ENCHANTING_TABLE
	);
	public static final List<Block> shulkerList = Arrays.asList(
		Blocks.WHITE_SHULKER_BOX,
		Blocks.ORANGE_SHULKER_BOX,
		Blocks.MAGENTA_SHULKER_BOX,
		Blocks.LIGHT_BLUE_SHULKER_BOX,
		Blocks.YELLOW_SHULKER_BOX,
		Blocks.LIME_SHULKER_BOX,
		Blocks.PINK_SHULKER_BOX,
		Blocks.GRAY_SHULKER_BOX,
		Blocks.SILVER_SHULKER_BOX,
		Blocks.CYAN_SHULKER_BOX,
		Blocks.PURPLE_SHULKER_BOX,
		Blocks.BLUE_SHULKER_BOX,
		Blocks.BROWN_SHULKER_BOX,
		Blocks.GREEN_SHULKER_BOX,
		Blocks.RED_SHULKER_BOX,
		Blocks.BLACK_SHULKER_BOX
	);


	public static int getItemHotbar(Item input) {
		for (int i = 0; i < 9; ++i) {
			Item item = InventoryUtil.mc.player.inventory.getStackInSlot(i).getItem();
			if (Item.getIdFromItem(item) != Item.getIdFromItem(input)) continue;
			return i;
		}
		return -1;
	}

	public static boolean isEating() {
		boolean var10000;
		if ((!mc.player.isHandActive() || !(mc.player.getActiveItemStack().getItem() instanceof ItemFood))
			&& (!(mc.player.getHeldItemMainhand().getItem() instanceof ItemFood) || !Mouse.isButtonDown(1))) {
			var10000 = false;
		} else {
			var10000 = true;
			boolean var10001 = false;
		}

		return var10000;
	}
	public static EntityPlayer getTarget(double var0) {
		EntityPlayer var2 = null;
		double var3 = var0;

		for(EntityPlayer var6 : mc.world.playerEntities) {
			if (invalid(var6, var0)) {
				boolean var10000 = false;
			} else if (var2 == null) {
				var2 = var6;
				var3 = mc.player.getDistanceSq(var6);
				boolean var7 = false;
			} else if (mc.player.getDistanceSq(var6) >= var3) {
				boolean var8 = false;
			} else {
				var2 = var6;
				var3 = mc.player.getDistanceSq(var6);
				boolean var9 = false;
			}
		}

		return var2;
	}
	public static boolean invalid(Entity var0, double var1) {
		boolean var10000;
		if (var0 != null
			&& !isDead(var0)
			&& !var0.equals(mc.player)
			&& (!(var0 instanceof EntityPlayer) || !Client.friendManager.isFriend(var0.getName()))
			&& !(mc.player.getDistanceSq(var0) > MathUtil.square(var1))) {
			var10000 = false;
		} else {
			var10000 = true;
			boolean var10001 = false;
		}

		return var10000;
	}

	public static boolean isDead(Entity var0) {
		boolean var10000;
		if (!isAlive(var0)) {
			var10000 = true;
			boolean var10001 = false;
		} else {
			var10000 = false;
		}

		return var10000;
	}
	public static boolean isAlive(Entity var0) {
		boolean var10000;
		if (isLiving(var0) && !var0.isDead && ((EntityLivingBase)var0).getHealth() > 0.0F) {
			var10000 = true;
			boolean var10001 = false;
		} else {
			var10000 = false;
		}

		return var10000;
	}
	public static boolean isLiving(Entity var0) {
		return var0 instanceof EntityLivingBase;
	}

	public static BlockPos getEntityPos(Entity var0) {
		return new BlockPos(var0.posX, var0.posY + 0.5, var0.posZ);
	}

	public static void attackCrystal(BlockPos var0, boolean var1, boolean var2) {
		if (breakTimer.passedMs(300)) {
			if (!var2 || !isEating()) {
				for(Entity var4 : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(var0))) {
					if (var4 instanceof EntityEnderCrystal) {
						breakTimer.reset();
						boolean var5 = false;
						mc.player.connection.sendPacket(new CPacketUseEntity(var4));
						mc.player.swingArm(EnumHand.MAIN_HAND);
						if (var1) {
							faceXYZ(var4.posX, var4.posY + 0.25, var4.posZ);
							var5 = false;
						}
						break;
					}

					boolean var10000 = false;
				}
			}
		}
	}
	public static void attackCrystal(Entity var0, boolean var1, boolean var2) {
		if (breakTimer.passedMs(300)) {
			if (!var2 || isEating()) {
				if (var0 != null) {
					breakTimer.reset();
					boolean var10000 = false;
					mc.player.connection.sendPacket(new CPacketUseEntity(var0));
					mc.player.swingArm(EnumHand.MAIN_HAND);
					if (var1) {
						faceXYZ(var0.posX, var0.posY + 0.25, var0.posZ);
					}
				}
			}
		}
	}
	public static void faceXYZ(double var0, double var2, double var4) {
		faceYawAndPitch(getXYZYaw(var0, var2, var4), getXYZPitch(var0, var2, var4));
	}
	public static void faceYawAndPitch(float var0, float var1) {
		sendPlayerRot(var0, var1, mc.player.onGround);
	}
	public static void sendPlayerRot(float var0, float var1, boolean var2) {
		mc.player.connection.sendPacket(new CPacketPlayer.Rotation(var0, var1, var2));
	}
	public static float getXYZYaw(double var0, double var2, double var4) {
		float[] var6 = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), new Vec3d(var0, var2, var4));
		return var6[0];
	}

	public static float getXYZPitch(double var0, double var2, double var4) {
		float[] var6 = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), new Vec3d(var0, var2, var4));
		return var6[1];
	}
	public static float calculateDamage(Entity var0, Entity var1) {
		return calculateDamage(var0.posX, var0.posY, var0.posZ, var1);
	}

	public static float calculateDamage(double var0, double var2, double var4, Entity var6) {
		float var7 = 12.0F;
		double var8 = var6.getDistance(var0, var2, var4) / (double)var7;
		Vec3d var10 = new Vec3d(var0, var2, var4);
		double var11 = 0.0;
		Entity var10000 = var6;

		label17: {
			try {
				var11 = (double)var10000.world.getBlockDensity(var10, var6.getEntityBoundingBox());
			} catch (Exception var18) {
				break label17;
			}

			boolean var19 = false;
		}

		double var13 = (1.0 - var8) * var11;
		float var15 = (float)((int)((var13 * var13 + var13) / 2.0 * 7.0 * (double)var7 + 1.0));
		double var16 = 1.0;
		if (var6 instanceof EntityLivingBase) {
			var16 = (double)getBlastReduction(
				(EntityLivingBase)var6, getDamageMultiplied(var15), new Explosion(mc.world, null, var0, var2, var4, 6.0F, false, true)
			);
		}

		return (float)var16;
	}
	public static float getDamageMultiplied(float var0) {
		int var1 = mc.world.getDifficulty().getId();
		float var10001;
		if (var1 == 0) {
			var10001 = 0.0F;
			boolean var10002 = false;
		} else if (var1 == 2) {
			var10001 = 1.0F;
			boolean var2 = false;
		} else if (var1 == 1) {
			var10001 = 0.5F;
			boolean var3 = false;
		} else {
			var10001 = 1.5F;
		}

		return var0 * var10001;
	}

	public static float getBlastReduction(EntityLivingBase var0, float var1, Explosion var2) {
		if (var0 instanceof EntityPlayer) {
			EntityPlayer var4 = (EntityPlayer)var0;
			DamageSource var5 = DamageSource.causeExplosionDamage(var2);
			float var3 = CombatRules.getDamageAfterAbsorb(
				var1, (float)var4.getTotalArmorValue(), (float)var4.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue()
			);
			int var6 = 0;
			EntityPlayer var10000 = var4;

			label19: {
				try {
					var6 = EnchantmentHelper.getEnchantmentModifierDamage(var10000.getArmorInventoryList(), var5);
				} catch (Exception var8) {
					break label19;
				}

				boolean var10 = false;
			}

			float var7 = MathHelper.clamp((float)var6, 0.0F, 20.0F);
			var3 *= 1.0F - var7 / 25.0F;
			if (var0.isPotionActive(MobEffects.RESISTANCE)) {
				var3 -= var3 / 4.0F;
			}

			return Math.max(var3, 0.0F);
		} else {
			return CombatRules.getDamageAfterAbsorb(
				var1, (float)var0.getTotalArmorValue(), (float)var0.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue()
			);
		}
	}
	public static boolean isMoving() {
		boolean var10000;
		if ((double)mc.player.moveForward == 0.0 && (double)mc.player.moveStrafing == 0.0) {
			var10000 = false;
		} else {
			var10000 = true;
			boolean var10001 = false;
		}

		return var10000;
	}
	public static int findHotbarBlock(Block var0) {
		for(int var1 = 0; var1 < 9; ++var1) {
			ItemStack var2 = mc.player.inventory.getStackInSlot(var1);
			if (var2 != ItemStack.EMPTY && var2.getItem() instanceof ItemBlock) {
				if (((ItemBlock)var2.getItem()).getBlock() == var0) {
					return var1;
				}

				boolean var10000 = false;
			}

			boolean var3 = false;
		}

		return -1;
	}
	public static int findHotbarClass(Class var0) {
		for(int var1 = 0; var1 < 9; ++var1) {
			ItemStack var2 = mc.player.inventory.getStackInSlot(var1);
			if (var2 == ItemStack.EMPTY) {
				boolean var10000 = false;
			} else {
				if (var0.isInstance(var2.getItem())) {
					return var1;
				}

				if (var2.getItem() instanceof ItemBlock) {
					if (var0.isInstance(((ItemBlock)var2.getItem()).getBlock())) {
						return var1;
					}

					boolean var3 = false;
				}
			}

			boolean var4 = false;
		}

		return -1;
	}
	public static int findItemInHotbar(Item var0) {
		int var1 = -1;

		for(int var2 = 0; var2 < 9; ++var2) {
			ItemStack var3 = mc.player.inventory.getStackInSlot(var2);
			if (var3 == ItemStack.EMPTY) {
				boolean var10000 = false;
			} else {
				var3.getItem();
				boolean var5 = false;
				Item var4 = var3.getItem();
				if (var4.equals(var0)) {
					var1 = var2;
					var5 = false;
					break;
				}
			}

			boolean var6 = false;
		}

		return var1;
	}
	public static boolean canPlaceCrystal(BlockPos var0) {
		BlockPos var1 = var0.down();
		BlockPos var2 = var1.up();
		BlockPos var3 = var1.up(2);
		boolean var10000;
		if ((getBlock(var1) == Blocks.BEDROCK || getBlock(var1) == Blocks.OBSIDIAN)
			&& getBlock(var2) == Blocks.AIR
			&& getBlock(var3) == Blocks.AIR
			&& mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(var2)).isEmpty()
			&& mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(var3)).isEmpty()) {
			var10000 = true;
			boolean var10001 = false;
		} else {
			var10000 = false;
		}

		return var10000;
	}
	public static Block getBlock(BlockPos var0) {
		return getState(var0).getBlock();
	}
	public static IBlockState getState(BlockPos var0) {
		return mc.world.getBlockState(var0);
	}
	public static boolean posHasCrystal(BlockPos var0) {
		for(Entity var2 : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(var0))) {
			if (var2 instanceof EntityEnderCrystal && new BlockPos(var2.posX, var2.posY, var2.posZ).equals(var0)) {
				return true;
			}

			boolean var10000 = false;
		}

		return false;
	}

	public static void doSwap(int var0) {
		mc.player.inventory.currentItem = var0;
		mc.playerController.updateController();
	}
	public static EnumFacing getFirstFacing(BlockPos var0) {
		Iterator var1 = getPossibleSides(var0).iterator();
		if (var1.hasNext()) {
			return (EnumFacing)var1.next();
		}

		boolean var10000 = false;

		return null;
	}
	public static List<EnumFacing> getPossibleSides(BlockPos var0) {
		ArrayList var1 = new ArrayList();

		for(EnumFacing var5 : EnumFacing.values()) {
			BlockPos var6 = var0.offset(var5);
			if (getBlock(var6).canCollideCheck(getState(var6), false)) {
				if (!canReplace(var6)) {
					var1.add(var5);
				}
				boolean var10000 = false;
			}

			boolean var8 = false;
		}

		return var1;
	}
	public static boolean canReplace(BlockPos var0) {
		return getState(var0).getMaterial().isReplaceable();
	}
	public static float[] getLegitRotations(Vec3d var0) {
		Vec3d var1 = RotationUtil.getEyesPos();
		double var2 = var0.x - var1.x;
		double var4 = var0.y - var1.y;
		double var6 = var0.z - var1.z;
		double var8 = Math.sqrt(var2 * var2 + var6 * var6);
		float var10 = (float)Math.toDegrees(Math.atan2(var6, var2)) - 90.0F;
		float var11 = (float)(-Math.toDegrees(Math.atan2(var4, var8)));
		return new float[]{
			mc.player.rotationYaw + MathHelper.wrapDegrees(var10 - mc.player.rotationYaw),
			mc.player.rotationPitch + MathHelper.wrapDegrees(var11 - mc.player.rotationPitch)
		};
	}

	public static void placeCrystal(BlockPos var0, boolean var1, boolean debug) {
		if (debug) Command.sendMessage("crystal pos maybe " + var0);
		boolean var10000;
		if (mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL) {
			var10000 = true;
			boolean var10001 = false;
		} else {
			var10000 = false;
		}

		boolean var2 = var10000;
		BlockPos var3 = var0.down();
		RayTraceResult var4 = mc.world
			.rayTraceBlocks(
				new Vec3d(mc.player.posX, mc.player.posY + (double)mc.player.getEyeHeight(), mc.player.posZ),
				new Vec3d((double)var0.getX() + 0.5, (double)var0.getY() - 0.5, (double)var0.getZ() + 0.5)
			);
		EnumFacing var8;
		if (var4 != null && var4.sideHit != null) {
			var8 = var4.sideHit;
		} else {
			var8 = EnumFacing.UP;
			boolean var11 = false;
		}

		EnumFacing var5 = var8;
		EnumFacing var6 = var5.getOpposite();
		Vec3d var7 = new Vec3d(var3).add(0.5, 0.5, 0.5).add(new Vec3d(var6.getDirectionVec()));
		if (var1) {
			RotationUtil.faceVector(var7, true);
		}

		NetHandlerPlayClient var9 = mc.player.connection;
		CPacketPlayerTryUseItemOnBlock var12 = new CPacketPlayerTryUseItemOnBlock();
		EnumHand var10005;
		if (var2) {
			var10005 = EnumHand.OFF_HAND;
			boolean var10006 = false;
		} else {
			var10005 = EnumHand.MAIN_HAND;
		}

		Command.sendMessage("crystalbase: " + var3 + "facing: " + var5);
		var12 = (new CPacketPlayerTryUseItemOnBlock(var3, var5, var10005, 0.0F, 0.0F, 0.0F));
		var9.sendPacket(var12);
		EntityPlayerSP var10 = mc.player;
		EnumHand var13;
		if (var2) {
			var13 = EnumHand.OFF_HAND;
			boolean var10002 = false;
		} else {
			var13 = EnumHand.MAIN_HAND;
		}

		var10.swingArm(var13);
	}
	public static void placeCrystalFix(BlockPos basePos, boolean var1, boolean debug) {
		RebirthUtil.placeCrystal(basePos.up(), var1, debug);
	}
	public static void mineBlock(BlockPos var0) {
		getBlock(var0);
		mc.playerController.onPlayerDamageBlock(var0, BlockUtil.getRayTraceFacing(var0));
		boolean var10000 = false;
	}
	public static boolean canPlace(BlockPos var0) {
		if (mc.player.getDistance((double)var0.getX() + 0.5, (double)var0.getY() + 0.5, (double)var0.getZ() + 0.5) > 6.0) {
			return false;
		} else if (!canBlockFacing(var0)) {
			return false;
		} else if (!canReplace(var0)) {
			return false;
		} else if (!strictPlaceCheck(var0)) {
			return false;
		} else {
			boolean var10000;
			if (!checkEntity(var0)) {
				var10000 = true;
				boolean var10001 = false;
			} else {
				var10000 = false;
			}

			return var10000;
		}
	}
	public static boolean canBlockFacing(BlockPos var0) {
		boolean var1 = false;

		for(EnumFacing var5 : EnumFacing.values()) {
			if (canClick(var0.offset(var5))) {
				var1 = true;
			}

			boolean var10000 = false;
		}

		return var1;
	}
	public static boolean canClick(BlockPos var0) {
		return mc.world.getBlockState(var0).getBlock().canCollideCheck(mc.world.getBlockState(var0), false);
	}
	public static boolean strictPlaceCheck(BlockPos var0) {
		for (EnumFacing var2 : getPlacableFacings(var0, true, false)) {
			if (canClick(var0.offset(var2))) {
				return true;
			}

			boolean var10000 = false;
		}

		return false;
	}

	public static List<EnumFacing> getPlacableFacings(BlockPos var0, boolean var1, boolean var2) {
		ArrayList var3 = new ArrayList();

		for(EnumFacing var7 : EnumFacing.values()) {
			if (getRaytrace(var0, var7)) {
				boolean var10000 = false;
			} else {
				getPlaceFacing(var0, var1, var3, var7);
			}

			boolean var12 = false;
		}

		for(EnumFacing var11 : EnumFacing.values()) {
			if (var2 && getRaytrace(var0, var11)) {
				boolean var13 = false;
			} else {
				getPlaceFacing(var0, var1, var3, var11);
			}

			boolean var14 = false;
		}

		return var3;
	}
	private static void getPlaceFacing(BlockPos var0, boolean var1, ArrayList<EnumFacing> var2, EnumFacing var3) {
		BlockPos var4 = var0.offset(var3);
		if (var1) {
			Vec3d var5 = mc.player.getPositionEyes(1.0F);
			Vec3d var6 = new Vec3d((double)var4.getX() + 0.5, (double)var4.getY() + 0.5, (double)var4.getZ() + 0.5);
			IBlockState var7 = mc.world.getBlockState(var4);
			boolean var10000;
			if (var7.getBlock() != Blocks.AIR && !var7.isFullBlock()) {
				var10000 = false;
			} else {
				var10000 = true;
				boolean var10001 = false;
			}

			boolean var8 = var10000;
			ArrayList var9 = new ArrayList();
			double var15 = var5.x - var6.x;
			EnumFacing var10002 = EnumFacing.WEST;
			EnumFacing var10003 = EnumFacing.EAST;
			boolean var10004;
			if (!var8) {
				var10004 = true;
				boolean var10005 = false;
			} else {
				var10004 = false;
			}

			var9.addAll(checkAxis(var15, var10002, var10003, var10004));
			var10000 = false;
			var9.addAll(checkAxis(var5.y - var6.y, EnumFacing.DOWN, EnumFacing.UP, true));
			var10000 = false;
			var15 = var5.z - var6.z;
			var10002 = EnumFacing.NORTH;
			var10003 = EnumFacing.SOUTH;
			if (!var8) {
				var10004 = true;
				boolean var20 = false;
			} else {
				var10004 = false;
			}

			var9.addAll(checkAxis(var15, var10002, var10003, var10004));
			var10000 = false;
			if (!var9.contains(var3.getOpposite())) {
				return;
			}
		}

		IBlockState var10 = mc.world.getBlockState(var4);
		if (var10.getBlock().canCollideCheck(var10, false) && !var10.getMaterial().isReplaceable()) {
			var2.add(var3);
			boolean var14 = false;
		}
	}

	public static ArrayList<EnumFacing> checkAxis(double var0, EnumFacing var2, EnumFacing var3, boolean var4) {
		ArrayList var5 = new ArrayList();
		if (var0 < -0.5) {
			var5.add(var2);
			boolean var10000 = false;
		}

		if (var0 > 0.5) {
			var5.add(var3);
			boolean var6 = false;
		}

		if (var4) {
			if (!var5.contains(var2)) {
				var5.add(var2);
				boolean var7 = false;
			}

			if (!var5.contains(var3)) {
				var5.add(var3);
				boolean var8 = false;
			}
		}

		return var5;
	}
	public static boolean checkEntity(BlockPos var0) {
		for(Entity var2 : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(var0))) {
			if (!var2.isDead && !(var2 instanceof EntityItem) && !(var2 instanceof EntityXPOrb) && !(var2 instanceof EntityExpBottle)) {
				if (!(var2 instanceof EntityArrow)) {
					return true;
				}

				boolean var10000 = false;
			}
		}

		return false;
	}
	private static boolean getRaytrace(BlockPos var0, EnumFacing var1) {
		Vec3d var2 = new Vec3d(var0).add(0.5, 0.5, 0.5).add(new Vec3d(var1.getDirectionVec()).scale(0.5));
		RayTraceResult var3 = mc.world.rayTraceBlocks(mc.player.getPositionEyes(1.0F), var2);
		boolean var10000;
		if (var3 != null && var3.typeOfHit != RayTraceResult.Type.MISS) {
			var10000 = true;
			boolean var10001 = false;
		} else {
			var10000 = false;
		}

		return var10000;
	}
	public static double distanceToXZ(double var0, double var2) {
		double var4 = mc.player.posX - var0;
		double var6 = mc.player.posZ - var2;
		return Math.sqrt(var4 * var4 + var6 * var6);
	}
	public static void facePlacePos(BlockPos var0) {
		EnumFacing var1 = BlockUtil.getFirstFacing(var0);
		if (var1 != null) {
			BlockPos var2 = var0.offset(var1);
			EnumFacing var3 = var1.getOpposite();
			Vec3d var4 = new Vec3d(var2).add(0.5, 0.5, 0.5).add(new Vec3d(var3.getDirectionVec()).scale(0.5));
			RotationUtil.faceVector(var4, true);
		}
	}

	public static boolean canPlace2(BlockPos var0) {
		if (mc.player.getDistance((double)var0.getX() + 0.5, (double)var0.getY() + 0.5, (double)var0.getZ() + 0.5) > 6.0) {
			return false;
		} else if (!canReplace(var0)) {
			return false;
		} else {
			boolean var10000;
			if (!checkPlayer(var0)) {
				var10000 = true;
				boolean var10001 = false;
			} else {
				var10000 = false;
			}

			return var10000;
		}
	}
	public static boolean checkPlayer(BlockPos var0) {
		for(Entity var2 : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(var0))) {
			if (!var2.isDead
				&& !(var2 instanceof EntityItem)
				&& !(var2 instanceof EntityXPOrb)
				&& !(var2 instanceof EntityExpBottle)
				&& !(var2 instanceof EntityArrow)) {
				if (!(var2 instanceof EntityEnderCrystal)) {
					return true;
				}

				boolean var10000 = false;
			}
		}

		return false;
	}
	public static boolean canPlace(BlockPos var0, double var1) {
		if (mc.player.getDistance((double)var0.getX() + 0.5, (double)var0.getY() + 0.5, (double)var0.getZ() + 0.5) > var1) {
			return false;
		} else if (!canBlockFacing(var0)) {
			return false;
		} else if (!canReplace(var0)) {
			return false;
		} else if (!strictPlaceCheck(var0)) {
			return false;
		} else {
			boolean var10000;
			if (!checkEntity(var0)) {
				var10000 = true;
				boolean var10001 = false;
			} else {
				var10000 = false;
			}

			return var10000;
		}
	}
	public static EnumFacing getBestNeighboring(BlockPos var0, EnumFacing var1) {
		for(EnumFacing var5 : EnumFacing.VALUES) {
			if (var1 == null || !var0.offset(var5).equals(var0.offset(var1, -1))) {
				if (var5 == EnumFacing.DOWN) {
					boolean var16 = false;
				} else {
					for(EnumFacing var7 : getPlacableFacings(var0.offset(var5), true, true)) {
						if (canClick(var0.offset(var5).offset(var7))) {
							return var5;
						}

						boolean var10000 = false;
					}
				}
			}

			boolean var17 = false;
		}

		EnumFacing var11 = null;
		double var12 = 0.0;

		for(EnumFacing var8 : EnumFacing.VALUES) {
			if (var1 == null || !var0.offset(var8).equals(var0.offset(var1, -1))) {
				if (var8 == EnumFacing.DOWN) {
					boolean var20 = false;
				} else {
					for(EnumFacing var10 : getPlacableFacings(var0.offset(var8), true, false)) {
						if (!canClick(var0.offset(var8).offset(var10))) {
							boolean var19 = false;
						} else {
							if (var11 == null || mc.player.getDistanceSq(var0.offset(var8)) < var12) {
								var11 = var8;
								var12 = mc.player.getDistanceSq(var0.offset(var8));
							}

							boolean var18 = false;
						}
					}
				}
			}

			boolean var21 = false;
		}

		return null;
	}
	public static Vec3d[] getVarOffsets(int var0, int var1, int var2) {
		List<Vec3d> var3 = getVarOffsetList(var0, var1, var2);
		Vec3d[] var4 = var3.toArray(new Vec3d[0]); // 传入一个空的Vec3d数组
		return var4;
	}
	public static List<Vec3d> getVarOffsetList(int var0, int var1, int var2) {
		ArrayList<Vec3d> var3 = new ArrayList<Vec3d>();
		var3.add(new Vec3d(var0, var1, var2));
		boolean var10000 = false;
		return var3;
	}


	public static BlockPos getPlayerPos() {
		return getEntityPos(mc.player);
	}
	public static boolean canPlaceEnum(BlockPos var0) {
		return canBlockFacing(var0) && strictPlaceCheck(var0);
	}

	public static void facePosFacing(BlockPos var0, EnumFacing var1) {
		Vec3d var2 = new Vec3d(var0).add(0.5, 0.5, 0.5)
			.add(new Vec3d(var1.getDirectionVec()).scale(0.5));
		RotationUtil.faceVector(var2, true);
	}


	public static Map<Integer, ItemStack> getInventoryAndHotbarSlots() {
		HashMap var0 = new HashMap();

		for(int var1 = 9; var1 <= 44; ++var1) {
			var0.put(var1, mc.player.inventoryContainer.getInventory().get(var1));
			boolean var10000 = false;
			var10000 = false;
		}

		return var0;
	}
	public static void placeBlock(BlockPos var0, EnumHand var1, boolean var2, boolean var3) {
		EnumFacing var4 = getFirstFacing(var0);
		if (var4 != null) {
			BlockPos var5 = var0.offset(var4);
			EnumFacing var6 = var4.getOpposite();
			Vec3d var7 = new Vec3d(var5).add(0.5, 0.5, 0.5).add(new Vec3d(var6.getDirectionVec()).scale(0.5));
			Block var8 = mc.world.getBlockState(var5).getBlock();
			boolean var9 = false;
			if (!mc.player.isSneaking() && (canUseList.contains(var8) || shulkerList.contains(var8))) {
				mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
				var9 = true;
			}

			if (var2) {
				faceVector(var7);
			}

			boolean var10000 = false;
			rightClickBlock(var5, var7, var1, var6, var3);
			PlaceRender.putMap(var0);
			if (var9) {
				mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
			}
		}
	}
	public static void rightClickBlock(BlockPos var0, Vec3d var1, EnumHand var2, EnumFacing var3, boolean var4) {
		if (var4) {
			float var5 = (float)(var1.x - (double)var0.getX());
			float var6 = (float)(var1.y - (double)var0.getY());
			float var7 = (float)(var1.z - (double)var0.getZ());
			mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(var0, var3, var2, var5, var6, var7));
		} else {
			mc.playerController.processRightClickBlock(mc.player, mc.world, var0, var3, var1, var2);
		}
		boolean var10000 = false;

		mc.player.swingArm(var2);
		mc.rightClickDelayTimer = 4;
	}
	public static void faceVector(Vec3d var0) {
		float[] var1 = getLegitRotations(var0);
		boolean var10000 = false;
		sendPlayerRot(var1[0], var1[1], mc.player.onGround);
	}

	public static int findItemInventorySlot(Item item, boolean offHand, boolean withXCarry) {
		int slot = InventoryUtil.findItemInventorySlot(item, offHand);
		if (slot == -1 && withXCarry) {
			for (int i = 1; i < 5; ++i) {
				Slot craftingSlot = InventoryUtil.mc.player.inventoryContainer.inventorySlots.get(i);
				ItemStack craftingStack = craftingSlot.getStack();
				if (craftingStack.getItem() == Items.AIR || craftingStack.getItem() != item) continue;
				slot = i;
			}
		}
		return slot;
	}

	public static float getHealth(Entity entity) {
		if (EntityUtil.isLiving(entity)) {
			EntityLivingBase livingBase = (EntityLivingBase)entity;
			return livingBase.getHealth() + livingBase.getAbsorptionAmount();
		}
		return 0.0f;
	}

	public static NonNullList<BlockPos> getBox(float range) {
		NonNullList positions = NonNullList.create();
		positions.addAll(BlockUtil.getSphere(new BlockPos(Math.floor(BlockUtil.mc.player.posX), Math.floor(BlockUtil.mc.player.posY), Math.floor(BlockUtil.mc.player.posZ)), range, 0, false, true, 0));
		return positions;
	}

	public static void placeBlock(BlockPos pos, EnumHand hand, boolean rotate, boolean packet, boolean attackEntity, boolean eatingPause) {
		if (attackEntity) {
			attackCrystal(pos, rotate, eatingPause);
		}
		RebirthUtil.placeBlock(pos, hand, rotate, packet);
	}

	public static boolean isInMovementDirection(double x, double y, double z) {
		if (mc.player.motionX != 0.0 || mc.player.motionZ != 0.0) {
			BlockPos movingPos = new BlockPos(mc.player).add(mc.player.motionX * 10000.0, 0.0, mc.player.motionZ * 10000.0);
			BlockPos antiPos = new BlockPos(mc.player).add(mc.player.motionX * -10000.0, 0.0, mc.player.motionY * -10000.0);
			return movingPos.distanceSq(x, y, z) < antiPos.distanceSq(x, y, z);
		}
		return true;
	}

	public static double getDistance2D() {
		double xDist = mc.player.posX - mc.player.prevPosX;
		double zDist = mc.player.posZ - mc.player.prevPosZ;
		return Math.sqrt(xDist * xDist + zDist * zDist);
	}
	public static double getSpeed() {
		return getSpeed(false);
	}

	public static double getSpeed(boolean slowness) {
		int amplifier;
		double defaultSpeed = 0.2873;
		if (mc.player.isPotionActive(MobEffects.SPEED)) {
			amplifier = Objects.requireNonNull(mc.player.getActivePotionEffect(MobEffects.SPEED)).getAmplifier();
			defaultSpeed *= 1.0 + 0.2 * (double)(amplifier + 1);
		}
		if (slowness && mc.player.isPotionActive(MobEffects.SLOWNESS)) {
			amplifier = Objects.requireNonNull(mc.player.getActivePotionEffect(MobEffects.SLOWNESS)).getAmplifier();
			defaultSpeed /= 1.0 + 0.2 * (double)(amplifier + 1);
		}
		if (mc.player.isSneaking()) {
			defaultSpeed /= 5.0;
		}
		return defaultSpeed;
	}

	public static boolean inLiquid() {
		return inLiquid(MathHelper.floor(requirePositionEntity().getEntityBoundingBox().minY + 0.01));
	}

	public static boolean inLiquid(boolean feet) {
		return inLiquid(MathHelper.floor(requirePositionEntity().getEntityBoundingBox().minY - (feet ? 0.03 : 0.2)));
	}

	private static boolean inLiquid(int y) {
		return findState(BlockLiquid.class, y) != null;
	}

	public static Entity getPositionEntity() {
		Entity ridingEntity;
		EntityPlayerSP player = mc.player;
		return player == null ? null : ((ridingEntity = player.getRidingEntity()) != null && !(ridingEntity instanceof EntityBoat) ? ridingEntity : player);
	}

	public static Entity requirePositionEntity() {
		return Objects.requireNonNull(getPositionEntity());
	}
	private static IBlockState findState(Class<? extends Block> block, int y) {
		Entity entity = requirePositionEntity();
		int startX = MathHelper.floor(entity.getEntityBoundingBox().minX);
		int startZ = MathHelper.floor(entity.getEntityBoundingBox().minZ);
		int endX = MathHelper.ceil(entity.getEntityBoundingBox().maxX);
		int endZ = MathHelper.ceil(entity.getEntityBoundingBox().maxZ);
		for (int x = startX; x < endX; ++x) {
			for (int z = startZ; z < endZ; ++z) {
				IBlockState s = mc.world.getBlockState(new BlockPos(x, y, z));
				if (!block.isInstance(s.getBlock())) continue;
				return s;
			}
		}
		return null;
	}

	public static double getSpeed(boolean slowness, double defaultSpeed) {
		int amplifier;
		if (mc.player.isPotionActive(MobEffects.SPEED)) {
			amplifier = Objects.requireNonNull(mc.player.getActivePotionEffect(MobEffects.SPEED)).getAmplifier();
			defaultSpeed *= 1.0 + 0.2 * (double)(amplifier + 1);
		}
		if (slowness && mc.player.isPotionActive(MobEffects.SLOWNESS)) {
			amplifier = Objects.requireNonNull(mc.player.getActivePotionEffect(MobEffects.SLOWNESS)).getAmplifier();
			defaultSpeed /= 1.0 + 0.2 * (double)(amplifier + 1);
		}
		if (mc.player.isSneaking()) {
			defaultSpeed /= 5.0;
		}
		return defaultSpeed;
	}

	public static double getJumpSpeed() {
		double defaultSpeed = 0.0;
		if (mc.player.isPotionActive(MobEffects.JUMP_BOOST)) {
			int amplifier = mc.player.getActivePotionEffect(MobEffects.JUMP_BOOST).getAmplifier();
			defaultSpeed += (double)(amplifier + 1) * 0.1;
		}
		return defaultSpeed;
	}


	public static void strafe(MoveEvent event, double speed) {
		if (isMoving()) {
			double[] strafe = strafe(speed);
			event.setX(strafe[0]);
			event.setZ(strafe[1]);
		} else {
			event.setX(0.0);
			event.setZ(0.0);
		}
	}

	public static double[] strafe(double speed) {
		return strafe(mc.player, speed);
	}

	public static double[] strafe(Entity entity, double speed) {
		return strafe(entity, mc.player.movementInput, speed);
	}

	public static double[] strafe(Entity entity, MovementInput movementInput, double speed) {
		float moveForward = movementInput.moveForward;
		float moveStrafe = movementInput.moveStrafe;
		float rotationYaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * mc.getRenderPartialTicks();
		if (moveForward != 0.0f) {
			if (moveStrafe > 0.0f) {
				rotationYaw += (float)(moveForward > 0.0f ? -45 : 45);
			} else if (moveStrafe < 0.0f) {
				rotationYaw += (float)(moveForward > 0.0f ? 45 : -45);
			}
			moveStrafe = 0.0f;
			if (moveForward > 0.0f) {
				moveForward = 1.0f;
			} else if (moveForward < 0.0f) {
				moveForward = -1.0f;
			}
		}
		double posX = (double)moveForward * speed * -Math.sin(Math.toRadians(rotationYaw)) + (double)moveStrafe * speed * Math.cos(Math.toRadians(rotationYaw));
		double posZ = (double)moveForward * speed * Math.cos(Math.toRadians(rotationYaw)) - (double)moveStrafe * speed * -Math.sin(Math.toRadians(rotationYaw));
		return new double[]{posX, posZ};
	}



	public static class RenderUtil {

		public static void drawBBBox(AxisAlignedBB var0, Color var1, int var2) {
			AxisAlignedBB var3 = chad.phobos.api.utils.RenderUtil.interpolateAxis(new AxisAlignedBB(var0.minX, var0.minY, var0.minZ, var0.maxX, var0.maxY, var0.maxZ));
			float var4 = (float)var1.getRed() / 255.0F;
			float var5 = (float)var1.getGreen() / 255.0F;
			float var6 = (float)var1.getBlue() / 255.0F;
			float var7 = (float)var2 / 255.0F;
			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.disableDepth();
			GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
			GlStateManager.disableTexture2D();
			GlStateManager.depthMask(false);
			GL11.glEnable(2848);
			GL11.glHint(3154, 4354);
			GL11.glLineWidth(1.0F);
			Tessellator var8 = Tessellator.getInstance();
			BufferBuilder var9 = var8.getBuffer();
			var9.begin(3, DefaultVertexFormats.POSITION_COLOR);
			var9.pos(var3.minX, var3.minY, var3.minZ).color(var4, var5, var6, var7).endVertex();
			var9.pos(var3.minX, var3.minY, var3.maxZ).color(var4, var5, var6, var7).endVertex();
			var9.pos(var3.maxX, var3.minY, var3.maxZ).color(var4, var5, var6, var7).endVertex();
			var9.pos(var3.maxX, var3.minY, var3.minZ).color(var4, var5, var6, var7).endVertex();
			var9.pos(var3.minX, var3.minY, var3.minZ).color(var4, var5, var6, var7).endVertex();
			var9.pos(var3.minX, var3.maxY, var3.minZ).color(var4, var5, var6, var7).endVertex();
			var9.pos(var3.minX, var3.maxY, var3.maxZ).color(var4, var5, var6, var7).endVertex();
			var9.pos(var3.minX, var3.minY, var3.maxZ).color(var4, var5, var6, var7).endVertex();
			var9.pos(var3.maxX, var3.minY, var3.maxZ).color(var4, var5, var6, var7).endVertex();
			var9.pos(var3.maxX, var3.maxY, var3.maxZ).color(var4, var5, var6, var7).endVertex();
			var9.pos(var3.minX, var3.maxY, var3.maxZ).color(var4, var5, var6, var7).endVertex();
			var9.pos(var3.maxX, var3.maxY, var3.maxZ).color(var4, var5, var6, var7).endVertex();
			var9.pos(var3.maxX, var3.maxY, var3.minZ).color(var4, var5, var6, var7).endVertex();
			var9.pos(var3.maxX, var3.minY, var3.minZ).color(var4, var5, var6, var7).endVertex();
			var9.pos(var3.maxX, var3.maxY, var3.minZ).color(var4, var5, var6, var7).endVertex();
			var9.pos(var3.minX, var3.maxY, var3.minZ).color(var4, var5, var6, var7).endVertex();
			var8.draw();
			GL11.glDisable(2848);
			GlStateManager.depthMask(true);
			GlStateManager.enableDepth();
			GlStateManager.enableTexture2D();
			GlStateManager.disableBlend();
			GlStateManager.popMatrix();
		}

		public static void drawBoxESP(BlockPos var0, Color var1, int var2) {
			chad.phobos.api.utils.RenderUtil.drawBox(var0, new Color(var1.getRed(), var1.getGreen(), var1.getBlue(), var2));
		}
	}
}
