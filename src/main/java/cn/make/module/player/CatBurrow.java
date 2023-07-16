package cn.make.module.player;

import chad.phobos.api.center.Module;
import chad.phobos.api.setting.Setting;
import cn.make.util.skid.EntityUtil;
import cn.make.util.skid.InventoryUtil;
import cn.make.util.skid.RotationUtil;
import cn.make.util.skid.Timer;
import cn.make.util.skid.two.BlockUtil;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;


public class CatBurrow
	extends Module {
	public final Setting<Boolean> packet;
	public final Setting<Boolean> onlyGround;
	private final Setting<Boolean> breakCrystal;
	public final Setting<Boolean> rotate = this.register(new Setting<>("Rotate", true));
	private final Setting<Boolean> multiPlace;
	public final Setting<Boolean> debug;
	static final Timer breakTimer = new Timer();

	public void attackCrystal() {
		if (!breakTimer.passedMs(250L)) {
			return;
		}
		breakTimer.reset();
		for (Entity entity : CatBurrow.mc.world.loadedEntityList) {
			if (!(entity instanceof EntityEnderCrystal)) continue;
			if (entity.getDistance(CatBurrow.mc.player.posX, CatBurrow.mc.player.posY, CatBurrow.mc.player.posZ) > 2.5) {
				continue;
			}
			CatBurrow.mc.player.connection.sendPacket(new CPacketUseEntity(entity));
			CatBurrow.mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
			BlockPos blockPos = new BlockPos(entity.posX, entity.posY, entity.posZ);
			if (!this.rotate.getValue()) break;
			RotationUtil.facePos(blockPos);
			break;
		}
	}

	@Override
	public void onTick() {
		int n = CatBurrow.mc.player.inventory.currentItem;
		BlockPos blockPos = new BlockPos(CatBurrow.mc.player.posX, CatBurrow.mc.player.posY + 0.5, CatBurrow.mc.player.posZ);
		if (InventoryUtil.getItemHotbar(Item.getItemFromBlock(Blocks.OBSIDIAN)) == -1 && InventoryUtil.getItemHotbar(Item.getItemFromBlock(Blocks.ENDER_CHEST)) == -1) {
			sendModuleMessage(ChatFormatting.RED + "Obsidian/Ender Chest ?");
			this.disable();
			return;
		}
		if (this.breakCrystal.getValue()) {
			this.attackCrystal();
		}
		if (this.onlyGround.getValue() && !CatBurrow.mc.player.onGround) {
			return;
		}
		if (!CatBurrow.mc.world.getBlockState(blockPos.offset(EnumFacing.UP, 2)).getBlock().equals(Blocks.AIR) || CatBurrow.mc.world.getBlockState(blockPos.add(1, 2, 0)).getBlock() != Blocks.AIR && CatBurrow.checkSelf(blockPos.add(1, 0, 0)) != null || CatBurrow.mc.world.getBlockState(blockPos.add(-1, 2, 0)).getBlock() != Blocks.AIR && CatBurrow.checkSelf(blockPos.add(-1, 0, 0)) != null || CatBurrow.mc.world.getBlockState(blockPos.add(0, 2, 1)).getBlock() != Blocks.AIR && CatBurrow.checkSelf(blockPos.add(0, 0, 1)) != null || CatBurrow.mc.world.getBlockState(blockPos.add(0, 2, -1)).getBlock() != Blocks.AIR && CatBurrow.checkSelf(blockPos.add(0, 0, -1)) != null || CatBurrow.mc.world.getBlockState(blockPos.add(1, 2, 1)).getBlock() != Blocks.AIR && CatBurrow.checkSelf(blockPos.add(1, 0, 1)) != null || CatBurrow.mc.world.getBlockState(blockPos.add(1, 2, -1)).getBlock() != Blocks.AIR && CatBurrow.checkSelf(blockPos.add(1, 0, -1)) != null || CatBurrow.mc.world.getBlockState(blockPos.add(-1, 2, 1)).getBlock() != Blocks.AIR && CatBurrow.checkSelf(blockPos.add(-1, 0, 1)) != null || CatBurrow.mc.world.getBlockState(blockPos.add(-1, 2, -1)).getBlock() != Blocks.AIR && CatBurrow.checkSelf(blockPos.add(-1, 0, -1)) != null) {
			boolean bl = false;
			boolean bl2 = false;
			BlockPos blockPos2 = blockPos;
			if (CatBurrow.checkSelf(blockPos2) != null && !CatBurrow.isAir(blockPos2)) {
				CatBurrow.mc.player.connection.sendPacket(new CPacketPlayer.Position(CatBurrow.mc.player.posX + ((double)blockPos2.getX() + 0.5 - CatBurrow.mc.player.posX) / 2.0, CatBurrow.mc.player.posY + 0.2, CatBurrow.mc.player.posZ + ((double)blockPos2.getZ() + 0.5 - CatBurrow.mc.player.posZ) / 2.0, false));
				CatBurrow.mc.player.connection.sendPacket(new CPacketPlayer.Position(CatBurrow.mc.player.posX + ((double)blockPos2.getX() + 0.5 - CatBurrow.mc.player.posX), CatBurrow.mc.player.posY + 0.2, CatBurrow.mc.player.posZ + ((double)blockPos2.getZ() + 0.5 - CatBurrow.mc.player.posZ), false));
				if (this.debug.getValue()) {
					sendModuleMessage("autochthonous " + ((double) blockPos2.getX() + 0.5 - CatBurrow.mc.player.posX) + " " + ((double) blockPos2.getZ() + 0.5 - CatBurrow.mc.player.posZ));
				}
			} else {
				for (EnumFacing enumFacing : EnumFacing.VALUES) {
					if (enumFacing == EnumFacing.UP) continue;
					if (enumFacing == EnumFacing.DOWN) {
						continue;
					}
					blockPos2 = blockPos.offset(enumFacing);
					if (CatBurrow.checkSelf(blockPos2) == null || CatBurrow.isAir(blockPos2)) continue;
					CatBurrow.mc.player.connection.sendPacket(new CPacketPlayer.Position(CatBurrow.mc.player.posX + ((double)blockPos2.getX() + 0.5 - CatBurrow.mc.player.posX) / 2.0, CatBurrow.mc.player.posY + 0.2, CatBurrow.mc.player.posZ + ((double)blockPos2.getZ() + 0.5 - CatBurrow.mc.player.posZ) / 2.0, false));
					CatBurrow.mc.player.connection.sendPacket(new CPacketPlayer.Position(CatBurrow.mc.player.posX + ((double)blockPos2.getX() + 0.5 - CatBurrow.mc.player.posX), CatBurrow.mc.player.posY + 0.2, CatBurrow.mc.player.posZ + ((double)blockPos2.getZ() + 0.5 - CatBurrow.mc.player.posZ), false));
					bl = true;
					if (!this.debug.getValue()) break;
					sendModuleMessage("no air " + ((double) blockPos2.getX() + 0.5 - CatBurrow.mc.player.posX) + " " + ((double) blockPos2.getZ() + 0.5 - CatBurrow.mc.player.posZ));
					break;
				}
				if (!bl) {
					for (EnumFacing enumFacing : EnumFacing.VALUES) {
						if (enumFacing == EnumFacing.UP) continue;
						if (enumFacing == EnumFacing.DOWN) {
							continue;
						}
						blockPos2 = blockPos.offset(enumFacing);
						if (CatBurrow.checkSelf(blockPos2) == null) continue;
						CatBurrow.mc.player.connection.sendPacket(new CPacketPlayer.Position(CatBurrow.mc.player.posX + ((double)blockPos2.getX() + 0.5 - CatBurrow.mc.player.posX) / 2.0, CatBurrow.mc.player.posY + 0.2, CatBurrow.mc.player.posZ + ((double)blockPos2.getZ() + 0.5 - CatBurrow.mc.player.posZ) / 2.0, false));
						CatBurrow.mc.player.connection.sendPacket(new CPacketPlayer.Position(CatBurrow.mc.player.posX + ((double)blockPos2.getX() + 0.5 - CatBurrow.mc.player.posX), CatBurrow.mc.player.posY + 0.2, CatBurrow.mc.player.posZ + ((double)blockPos2.getZ() + 0.5 - CatBurrow.mc.player.posZ), false));
						bl2 = true;
						if (!this.debug.getValue()) break;
						sendModuleMessage("entity " + ((double) blockPos2.getX() + 0.5 - CatBurrow.mc.player.posX) + " " + ((double) blockPos2.getZ() + 0.5 - CatBurrow.mc.player.posZ));
						break;
					}
					if (!bl2) {
						for (EnumFacing enumFacing : EnumFacing.VALUES) {
							if (enumFacing == EnumFacing.UP) continue;
							if (enumFacing == EnumFacing.DOWN) {
								continue;
							}
							blockPos2 = blockPos.offset(enumFacing);
							if (!CatBurrow.isAir(blockPos2) || !CatBurrow.isAir(blockPos2.offset(EnumFacing.UP))) continue;
							CatBurrow.mc.player.connection.sendPacket(new CPacketPlayer.Position(CatBurrow.mc.player.posX + ((double)blockPos2.getX() + 0.5 - CatBurrow.mc.player.posX) / 2.0, CatBurrow.mc.player.posY + 0.2, CatBurrow.mc.player.posZ + ((double)blockPos2.getZ() + 0.5 - CatBurrow.mc.player.posZ) / 2.0, false));
							CatBurrow.mc.player.connection.sendPacket(new CPacketPlayer.Position(CatBurrow.mc.player.posX + ((double)blockPos2.getX() + 0.5 - CatBurrow.mc.player.posX), CatBurrow.mc.player.posY + 0.2, CatBurrow.mc.player.posZ + ((double)blockPos2.getZ() + 0.5 - CatBurrow.mc.player.posZ), false));
							if (!this.debug.getValue()) break;
							sendModuleMessage("air " + ((double) blockPos2.getX() + 0.5 - CatBurrow.mc.player.posX) + " " + ((double) blockPos2.getZ() + 0.5 - CatBurrow.mc.player.posZ));
							break;
						}
					}
				}
			}
		} else {
			if (this.debug.getValue()) {
				sendModuleMessage("fake jump");
			}
			CatBurrow.mc.player.connection.sendPacket(new CPacketPlayer.Position(CatBurrow.mc.player.posX, CatBurrow.mc.player.posY + 0.4199999868869781, CatBurrow.mc.player.posZ, false));
			CatBurrow.mc.player.connection.sendPacket(new CPacketPlayer.Position(CatBurrow.mc.player.posX, CatBurrow.mc.player.posY + 0.7531999805212017, CatBurrow.mc.player.posZ, false));
			CatBurrow.mc.player.connection.sendPacket(new CPacketPlayer.Position(CatBurrow.mc.player.posX, CatBurrow.mc.player.posY + 0.9999957640154541, CatBurrow.mc.player.posZ, false));
			CatBurrow.mc.player.connection.sendPacket(new CPacketPlayer.Position(CatBurrow.mc.player.posX, CatBurrow.mc.player.posY + 1.1661092609382138, CatBurrow.mc.player.posZ, false));
		}
		CatBurrow.mc.player.inventory.currentItem = InventoryUtil.getItemHotbar(Item.getItemFromBlock(Blocks.OBSIDIAN)) != -1 ? InventoryUtil.getItemHotbar(Item.getItemFromBlock(Blocks.OBSIDIAN)) : InventoryUtil.getItemHotbar(Item.getItemFromBlock(Blocks.ENDER_CHEST));
		CatBurrow.mc.playerController.updateController();
		if (this.multiPlace.getValue()) {
			if (CatBurrow.isAir(new BlockPos(CatBurrow.mc.player.posX + 0.3, CatBurrow.mc.player.posY, CatBurrow.mc.player.posZ + 0.3))) {
				try {
					BlockUtil.placeBlock(new BlockPos(CatBurrow.mc.player.posX + 0.3, CatBurrow.mc.player.posY, CatBurrow.mc.player.posZ + 0.3), EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue());
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			if (CatBurrow.isAir(new BlockPos(CatBurrow.mc.player.posX + 0.3, CatBurrow.mc.player.posY, CatBurrow.mc.player.posZ - 0.3))) {
				try {
					BlockUtil.placeBlock(new BlockPos(CatBurrow.mc.player.posX + 0.3, CatBurrow.mc.player.posY, CatBurrow.mc.player.posZ - 0.3), EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue());
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			if (CatBurrow.isAir(new BlockPos(CatBurrow.mc.player.posX - 0.3, CatBurrow.mc.player.posY, CatBurrow.mc.player.posZ + 0.3))) {
				try {
					BlockUtil.placeBlock(new BlockPos(CatBurrow.mc.player.posX - 0.3, CatBurrow.mc.player.posY, CatBurrow.mc.player.posZ + 0.3), EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue());
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			if (CatBurrow.isAir(new BlockPos(CatBurrow.mc.player.posX - 0.3, CatBurrow.mc.player.posY, CatBurrow.mc.player.posZ - 0.3))) {
				try {
					BlockUtil.placeBlock(new BlockPos(CatBurrow.mc.player.posX - 0.3, CatBurrow.mc.player.posY, CatBurrow.mc.player.posZ - 0.3), EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue());
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		} else if (CatBurrow.isAir(blockPos)) {
			try {
				BlockUtil.placeBlock(blockPos, EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else if (CatBurrow.isAir(new BlockPos(CatBurrow.mc.player.posX + 0.3, CatBurrow.mc.player.posY, CatBurrow.mc.player.posZ + 0.3))) {
			try {
				BlockUtil.placeBlock(new BlockPos(CatBurrow.mc.player.posX + 0.3, CatBurrow.mc.player.posY, CatBurrow.mc.player.posZ + 0.3), EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else if (CatBurrow.isAir(new BlockPos(CatBurrow.mc.player.posX + 0.3, CatBurrow.mc.player.posY, CatBurrow.mc.player.posZ - 0.3))) {
			try {
				BlockUtil.placeBlock(new BlockPos(CatBurrow.mc.player.posX + 0.3, CatBurrow.mc.player.posY, CatBurrow.mc.player.posZ - 0.3), EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else if (CatBurrow.isAir(new BlockPos(CatBurrow.mc.player.posX - 0.3, CatBurrow.mc.player.posY, CatBurrow.mc.player.posZ + 0.3))) {
			try {
				BlockUtil.placeBlock(new BlockPos(CatBurrow.mc.player.posX - 0.3, CatBurrow.mc.player.posY, CatBurrow.mc.player.posZ + 0.3), EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else if (CatBurrow.isAir(new BlockPos(CatBurrow.mc.player.posX - 0.3, CatBurrow.mc.player.posY, CatBurrow.mc.player.posZ - 0.3))) {
			try {
				BlockUtil.placeBlock(new BlockPos(CatBurrow.mc.player.posX - 0.3, CatBurrow.mc.player.posY, CatBurrow.mc.player.posZ - 0.3), EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		CatBurrow.mc.player.inventory.currentItem = n;
		CatBurrow.mc.playerController.updateController();
		CatBurrow.mc.player.connection.sendPacket(new CPacketPlayer.Position(CatBurrow.mc.player.posX, -7.0, CatBurrow.mc.player.posZ, false));
		this.disable();
	}

	static Entity checkSelf(BlockPos blockPos) {
		Vec3d[] vec3dArray;
		Entity entity = null;
		for (Vec3d vec3d : vec3dArray = EntityUtil.getVarOffsets(0, 0, 0)) {
			BlockPos blockPos2 = new BlockPos(blockPos).add(vec3d.x, vec3d.y, vec3d.z);
			for (Entity entity2 : CatBurrow.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(blockPos2))) {
				if (entity2 != CatBurrow.mc.player) continue;
				if (entity != null) {
					continue;
				}
				entity = entity2;
			}
		}
		return entity;
	}

	public CatBurrow() {
		super("CatBurrow", "unknown", Category.PLAYER);
		this.packet = this.register(new Setting<>("Packet", true));
		this.onlyGround = this.register(new Setting<>("onlyGround", true));
		this.debug = this.register(new Setting<>("Debug", false));
		this.breakCrystal = this.register(new Setting<>("BreakCrystal", true));
		this.multiPlace = this.register(new Setting<>("MultiPlace", true));
	}

	public static boolean isAir(BlockPos blockPos) {
		Block block = CatBurrow.mc.world.getBlockState(blockPos).getBlock();
		return block instanceof BlockAir;
	}
}
