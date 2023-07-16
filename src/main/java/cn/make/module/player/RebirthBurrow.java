package cn.make.module.player;


import chad.phobos.api.center.Module;
import chad.phobos.api.setting.Setting;
import cn.make.util.skid.RebirthUtil;
import cn.make.util.skid.Timer;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class RebirthBurrow
	extends Module {
	private final Setting<Boolean> placeDisable = rbool("PlaceDisable", false);
	private final Setting<Boolean> wait = rbool("Wait", true);
	private final Setting<Boolean> switchBypass = rbool("SwitchBypass", false);
	private final Setting<Boolean> rotate = rbool("Rotate", true);
	private final Setting<Boolean> onlyGround = rbool("OnlyGround", true);
	private final Setting<Boolean> airCheck = rbool("OnlyGround-AirCheck", true, v -> onlyGround.getValue());
	private final Setting<Boolean> aboveHead = rbool("AboveHead", true);
	private final Setting<Boolean> center = rbool("AboveHead-Center", false, v -> aboveHead.getValue());
	private final Setting<Boolean> breakCrystal = rbool("BreakCrystal", true);
	public final Setting<Float> safeHealth = rfloa("BreakCrystal-SafeHealth", 16.0f, 0.0f, 36.0f, v -> breakCrystal.getValue());
	private final Setting<Integer> multiPlace = rinte("MultiPlace", 1, 1, 4);
	private final Setting<Integer> timeOut = rinte("TimeOut", 10, 0, 2000);
	private final Setting<Integer> delay = rinte("delay", 300, 0, 1000);
	private final Setting<Boolean> smartOffset = rbool("SmartOffset", true);
	private final Setting<Double> offsetX = rdoub("OffsetX", -7.0, -14.0, 14.0, v -> !this.smartOffset.getValue());
	private final Setting<Double> offsetY = rdoub("OffsetY", -7.0, -14.0, 14.0, v -> !this.smartOffset.getValue());
	private final Setting<Double> offsetZ = rdoub("OffsetZ", -7.0, -14.0, 14.0, v -> !this.smartOffset.getValue());
	private final Setting<Boolean> debug = rbool("Debug", false);
	int progress = 0;
	private final Timer timer = new Timer();
	private final Timer timedOut = new Timer();
	public static RebirthBurrow INSTANCE;
	private boolean shouldWait = false;

	public RebirthBurrow() {
		super("BurrowPlus", "COOL", Category.PLAYER);
		INSTANCE = this;
	}

	private static boolean checkSelf(BlockPos pos) {
		for (Vec3d vec3d : RebirthUtil.getVarOffsets(0, 0, 0)) {
			BlockPos position = new BlockPos(pos).add(vec3d.x, vec3d.y, vec3d.z);
			for (Entity entity : RebirthBurrow.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(position))) {
				if (entity != RebirthBurrow.mc.player) continue;
				return true;
			}
		}
		return false;
	}

	private static boolean isAir(BlockPos pos) {
		return RebirthBurrow.mc.world.isAirBlock(pos);
	}

	private static boolean Trapped(BlockPos pos) {
		return !RebirthBurrow.mc.world.isAirBlock(pos) && RebirthBurrow.checkSelf(pos.down(2));
	}

	public static boolean canReplace(BlockPos pos) {
		return RebirthBurrow.mc.world.getBlockState(pos).getMaterial().isReplaceable();
	}

	@Override
	public void onEnable() {
		this.timedOut.reset();
		this.shouldWait = this.wait.getValue();
	}

	@Override
	public void onDisable() {
		this.timer.reset();
		this.shouldWait = false;
	}

	@Override
	public void onUpdate() {
		this.progress = 0;
		int blockSlot = !this.switchBypass.getValue() ? (RebirthUtil.getItemHotbar(Item.getItemFromBlock(Blocks.OBSIDIAN)) != -1 ? RebirthUtil.getItemHotbar(Item.getItemFromBlock(Blocks.OBSIDIAN)) : RebirthUtil.getItemHotbar(Item.getItemFromBlock(Blocks.ENDER_CHEST))) : (RebirthUtil.findItemInventorySlot(Item.getItemFromBlock(Blocks.OBSIDIAN), false, true) != -1 ? RebirthUtil.findItemInventorySlot(Item.getItemFromBlock(Blocks.OBSIDIAN), false, true) : RebirthUtil.findItemInventorySlot(Item.getItemFromBlock(Blocks.ENDER_CHEST), false, true));
		if (blockSlot == -1) {
			sendModuleMessage(ChatFormatting.RED + "Obsidian/Ender Chest ?");
			this.disable();
			return;
		}
		if (this.timedOut.passedMs(this.timeOut.getValue())) {
			this.disable();
			return;
		}
		BlockPos originalPos = RebirthUtil.getPlayerPos();
		if (!(this.canPlace(new BlockPos(RebirthBurrow.mc.player.posX + 0.3, RebirthBurrow.mc.player.posY + 0.5, RebirthBurrow.mc.player.posZ + 0.3)) || this.canPlace(new BlockPos(RebirthBurrow.mc.player.posX - 0.3, RebirthBurrow.mc.player.posY + 0.5, RebirthBurrow.mc.player.posZ + 0.3)) || this.canPlace(new BlockPos(RebirthBurrow.mc.player.posX + 0.3, RebirthBurrow.mc.player.posY + 0.5, RebirthBurrow.mc.player.posZ - 0.3)) || this.canPlace(new BlockPos(RebirthBurrow.mc.player.posX - 0.3, RebirthBurrow.mc.player.posY + 0.5, RebirthBurrow.mc.player.posZ - 0.3)))) {
			if (this.debug.getValue()) {
				sendModuleMessage("cant place");
			}
			if (!this.shouldWait) {
				this.disable();
			}
			return;
		}
		if (RebirthBurrow.mc.player.isInLava() || RebirthBurrow.mc.player.isInWater() || RebirthBurrow.mc.player.isInWeb) {
			if (this.debug.getValue()) {
				sendModuleMessage("player stuck");
			}
			return;
		}
		if (this.onlyGround.getValue()) {
			if (!RebirthBurrow.mc.player.onGround) {
				if (this.debug.getValue()) {
					sendModuleMessage("player not on ground");
				}
				return;
			}
			if (this.airCheck.getValue() && RebirthBurrow.isAir(RebirthUtil.getPlayerPos().down())) {
				if (this.debug.getValue()) {
					sendModuleMessage("player in air");
				}
				return;
			}
		}
		if (!this.timer.passedMs(this.delay.getValue())) {
			return;
		}
		if (this.breakCrystal.getValue() && RebirthUtil.getHealth(RebirthBurrow.mc.player) >= this.safeHealth.getValue()) {
			if (this.debug.getValue()) {
				sendModuleMessage("try break crystal");
			}
			RebirthUtil.attackCrystal(originalPos, this.rotate.getValue(), false);
			RebirthUtil.attackCrystal(new BlockPos(RebirthBurrow.mc.player.posX + 0.3, RebirthBurrow.mc.player.posY + 0.5, RebirthBurrow.mc.player.posZ + 0.3), this.rotate.getValue(), false);
			RebirthUtil.attackCrystal(new BlockPos(RebirthBurrow.mc.player.posX + 0.3, RebirthBurrow.mc.player.posY + 0.5, RebirthBurrow.mc.player.posZ - 0.3), this.rotate.getValue(), false);
			RebirthUtil.attackCrystal(new BlockPos(RebirthBurrow.mc.player.posX - 0.3, RebirthBurrow.mc.player.posY + 0.5, RebirthBurrow.mc.player.posZ + 0.3), this.rotate.getValue(), false);
			RebirthUtil.attackCrystal(new BlockPos(RebirthBurrow.mc.player.posX - 0.3, RebirthBurrow.mc.player.posY + 0.5, RebirthBurrow.mc.player.posZ - 0.3), this.rotate.getValue(), false);
		}
		this.timer.reset();
		this.shouldWait = false;
		BlockPos headPos = RebirthUtil.getPlayerPos().up(2);
		if (RebirthBurrow.Trapped(headPos) || RebirthBurrow.Trapped(headPos.add(1, 0, 0)) || RebirthBurrow.Trapped(headPos.add(-1, 0, 0)) || RebirthBurrow.Trapped(headPos.add(0, 0, 1)) || RebirthBurrow.Trapped(headPos.add(0, 0, -1)) || RebirthBurrow.Trapped(headPos.add(1, 0, -1)) || RebirthBurrow.Trapped(headPos.add(-1, 0, -1)) || RebirthBurrow.Trapped(headPos.add(1, 0, 1)) || RebirthBurrow.Trapped(headPos.add(-1, 0, 1))) {
			if (!this.aboveHead.getValue()) {
				if (!this.shouldWait) {
					this.disable();
				}
				return;
			}
			boolean moved = false;
			BlockPos offPos = originalPos;
			if (RebirthBurrow.checkSelf(offPos) && !RebirthBurrow.canReplace(offPos)) {
				this.gotoPos(offPos);
				if (this.debug.getValue()) {
					sendModuleMessage("moved to center " + ((double)offPos.getX() + 0.5 - RebirthBurrow.mc.player.posX) + " " + ((double)offPos.getZ() + 0.5 - RebirthBurrow.mc.player.posZ));
				}
			} else {
				for (EnumFacing facing : EnumFacing.VALUES) {
					if (facing == EnumFacing.UP || facing == EnumFacing.DOWN || !RebirthBurrow.checkSelf(offPos = originalPos.offset(facing)) || RebirthBurrow.canReplace(offPos)) continue;
					this.gotoPos(offPos);
					moved = true;
					if (!this.debug.getValue()) break;
					sendModuleMessage("moved to block " + ((double)offPos.getX() + 0.5 - RebirthBurrow.mc.player.posX) + " " + ((double)offPos.getZ() + 0.5 - RebirthBurrow.mc.player.posZ));
					break;
				}
				if (!moved) {
					for (EnumFacing facing : EnumFacing.VALUES) {
						if (facing == EnumFacing.UP || facing == EnumFacing.DOWN || !RebirthBurrow.checkSelf(offPos = originalPos.offset(facing))) continue;
						this.gotoPos(offPos);
						moved = true;
						if (!this.debug.getValue()) break;
						sendModuleMessage("moved to entity " + ((double)offPos.getX() + 0.5 - RebirthBurrow.mc.player.posX) + " " + ((double)offPos.getZ() + 0.5 - RebirthBurrow.mc.player.posZ));
						break;
					}
					if (!moved) {
						if (!this.center.getValue()) {
							if (!this.shouldWait) {
								this.disable();
							}
							return;
						}
						for (EnumFacing facing : EnumFacing.VALUES) {
							if (facing == EnumFacing.UP || facing == EnumFacing.DOWN || !RebirthBurrow.canReplace(offPos = originalPos.offset(facing)) || !RebirthBurrow.canReplace(offPos.up())) continue;
							this.gotoPos(offPos);
							if (this.debug.getValue()) {
								sendModuleMessage("moved to air " + ((double)offPos.getX() + 0.5 - RebirthBurrow.mc.player.posX) + " " + ((double)offPos.getZ() + 0.5 - RebirthBurrow.mc.player.posZ));
							}
							moved = true;
							break;
						}
						if (!moved) {
							if (!this.shouldWait) {
								this.disable();
							}
							return;
						}
					}
				}
			}
		} else {
			if (this.debug.getValue()) {
				sendModuleMessage("fake jump");
			}
			RebirthBurrow.mc.player.connection.sendPacket(new CPacketPlayer.Position(RebirthBurrow.mc.player.posX, RebirthBurrow.mc.player.posY + 0.4199999868869781, RebirthBurrow.mc.player.posZ, false));
			RebirthBurrow.mc.player.connection.sendPacket(new CPacketPlayer.Position(RebirthBurrow.mc.player.posX, RebirthBurrow.mc.player.posY + 0.7531999805212017, RebirthBurrow.mc.player.posZ, false));
			RebirthBurrow.mc.player.connection.sendPacket(new CPacketPlayer.Position(RebirthBurrow.mc.player.posX, RebirthBurrow.mc.player.posY + 0.9999957640154541, RebirthBurrow.mc.player.posZ, false));
			RebirthBurrow.mc.player.connection.sendPacket(new CPacketPlayer.Position(RebirthBurrow.mc.player.posX, RebirthBurrow.mc.player.posY + 1.1661092609382138, RebirthBurrow.mc.player.posZ, false));
		}
		int oldSlot = RebirthBurrow.mc.player.inventory.currentItem;
		if (!this.switchBypass.getValue()) {
			RebirthUtil.doSwap(blockSlot);
		} else {
			RebirthBurrow.mc.playerController.windowClick(0, blockSlot, oldSlot, ClickType.SWAP, RebirthBurrow.mc.player);
		}
		this.placeBlock(originalPos);
		this.placeBlock(new BlockPos(RebirthBurrow.mc.player.posX + 0.3, RebirthBurrow.mc.player.posY + 0.5, RebirthBurrow.mc.player.posZ + 0.3));
		this.placeBlock(new BlockPos(RebirthBurrow.mc.player.posX + 0.3, RebirthBurrow.mc.player.posY + 0.5, RebirthBurrow.mc.player.posZ - 0.3));
		this.placeBlock(new BlockPos(RebirthBurrow.mc.player.posX - 0.3, RebirthBurrow.mc.player.posY + 0.5, RebirthBurrow.mc.player.posZ + 0.3));
		this.placeBlock(new BlockPos(RebirthBurrow.mc.player.posX - 0.3, RebirthBurrow.mc.player.posY + 0.5, RebirthBurrow.mc.player.posZ - 0.3));
		if (!this.switchBypass.getValue()) {
			RebirthUtil.doSwap(oldSlot);
		} else {
			RebirthBurrow.mc.playerController.windowClick(0, blockSlot, oldSlot, ClickType.SWAP, RebirthBurrow.mc.player);
		}
		if (this.smartOffset.getValue()) {
			double distance = 0.0;
			BlockPos bestPos = null;
			for (BlockPos pos : RebirthUtil.getBox(6.0f)) {
				if (this.cantGoto(pos) || RebirthBurrow.mc.player.getDistance((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5) <= 3.0 || bestPos != null && !(RebirthBurrow.mc.player.getDistance((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5) < distance)) continue;
				bestPos = pos;
				distance = RebirthBurrow.mc.player.getDistance((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5);
			}
			if (bestPos != null) {
				RebirthBurrow.mc.player.connection.sendPacket(new CPacketPlayer.Position((double)bestPos.getX() + 0.5, bestPos.getY(), (double)bestPos.getZ() + 0.5, false));
			} else {
				for (BlockPos pos : RebirthUtil.getBox(6.0f)) {
					if (this.cantGoto(pos) || RebirthBurrow.mc.player.getDistance((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5) <= 2.0 || bestPos != null && !(RebirthBurrow.mc.player.getDistance((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5) < distance)) continue;
					bestPos = pos;
					distance = RebirthBurrow.mc.player.getDistance((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5);
				}
				if (bestPos != null) {
					RebirthBurrow.mc.player.connection.sendPacket(new CPacketPlayer.Position((double)bestPos.getX() + 0.5, bestPos.getY(), (double)bestPos.getZ() + 0.5, false));
				} else {
					RebirthBurrow.mc.player.connection.sendPacket(new CPacketPlayer.Position(RebirthBurrow.mc.player.posX, -7.0, RebirthBurrow.mc.player.posZ, false));
				}
			}
		} else {
			RebirthBurrow.mc.player.connection.sendPacket(new CPacketPlayer.Position(RebirthBurrow.mc.player.posX + this.offsetX.getValue(), RebirthBurrow.mc.player.posY + this.offsetY.getValue(), RebirthBurrow.mc.player.posZ + this.offsetZ.getValue(), false));
		}
		if (this.placeDisable.getValue()) {
			this.disable();
		}
	}

	private void gotoPos(BlockPos offPos) {
		if (Math.abs((double)offPos.getX() + 0.5 - RebirthBurrow.mc.player.posX) < Math.abs((double)offPos.getZ() + 0.5 - RebirthBurrow.mc.player.posZ)) {
			RebirthBurrow.mc.player.connection.sendPacket(new CPacketPlayer.Position(RebirthBurrow.mc.player.posX, RebirthBurrow.mc.player.posY + 0.2, RebirthBurrow.mc.player.posZ + ((double)offPos.getZ() + 0.5 - RebirthBurrow.mc.player.posZ), true));
		} else {
			RebirthBurrow.mc.player.connection.sendPacket(new CPacketPlayer.Position(RebirthBurrow.mc.player.posX + ((double)offPos.getX() + 0.5 - RebirthBurrow.mc.player.posX), RebirthBurrow.mc.player.posY + 0.2, RebirthBurrow.mc.player.posZ, true));
		}
	}

	private boolean cantGoto(BlockPos pos) {
		return !RebirthBurrow.isAir(pos) || !RebirthBurrow.isAir(pos.up());
	}

	private void placeBlock(BlockPos pos) {
		if (this.progress >= this.multiPlace.getValue()) {
			return;
		}
		if (this.canPlace(pos)) {
			RebirthUtil.placeBlock(pos, EnumHand.MAIN_HAND, this.rotate.getValue(), true, this.breakCrystal.getValue(), false);
			++this.progress;
		}
	}

	private boolean canPlace(BlockPos pos) {
		if (!RebirthUtil.canBlockFacing(pos)) {
			return false;
		}
		if (!RebirthBurrow.canReplace(pos)) {
			return false;
		}
		return !this.checkEntity(pos);
	}

	private boolean checkEntity(BlockPos pos) {
		for (Entity entity : RebirthBurrow.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
			if (entity.isDead || entity instanceof EntityItem || entity instanceof EntityXPOrb || entity instanceof EntityExpBottle || entity instanceof EntityArrow || !(entity instanceof EntityEnderCrystal ? !this.breakCrystal.getValue() || RebirthUtil.getHealth(RebirthBurrow.mc.player) < this.safeHealth.getValue() : entity != RebirthBurrow.mc.player)) continue;
			return true;
		}
		return false;
	}
}

