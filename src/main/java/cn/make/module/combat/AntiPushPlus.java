package cn.make.module.combat;

import chad.phobos.Client;
import chad.phobos.api.center.Module;
import chad.phobos.api.setting.Setting;
import cn.make.util.UtilsRewrite;
import cn.make.util.skid.RebirthUtil;
import cn.make.util.skid.two.BlockUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

public class AntiPushPlus extends Module {
	public static AntiPushPlus INSTANCE;
	public final Setting<Boolean> rotate;
	public final Setting<Boolean> packet;
	public final Setting<TrapMode> trap;
	public final Setting<Boolean> helper;
	private final Setting<Boolean> onlyBurrow;
	private final Setting<Boolean> whenDouble;
	private final Setting<Double> maxSelfSpeed;


	public AntiPushPlus() {
		super("AntiPushPlus", "Trap self when piston kick", Category.COMBAT);
		this.rotate = this.register(new Setting<>("Rotate", true));
		this.packet = this.register(new Setting<>("Packet", true));
		this.trap = this.register(new Setting<>("Trap", TrapMode.SMART));
		this.onlyBurrow = this.register(new Setting<>("OnlyBurrow", true, v -> trap.getValue() != TrapMode.NOTRAP));
		this.whenDouble = this.register(new Setting<>("WhenDouble", true, v -> onlyBurrow.getValue()));
		this.maxSelfSpeed = this.register(new Setting<>("MaxSelfSpeed", 6.0, 1.0, 30.0));
		this.helper = rbool("Helper", true);
		INSTANCE = this;
	}

	public static boolean canPlace(BlockPos var0) {
		if (!RebirthUtil.canBlockFacing(var0)) {
			return false;
		} else if (!RebirthUtil.canReplace(var0)) {
			return false;
		} else {
           return !RebirthUtil.checkEntity(var0);
		}
	}

	private Block getBlock(BlockPos var1) {
		return mc.world.getBlockState(var1).getBlock();
	}

	@Override
	public void onUpdate() {
		if (
			!fullNullCheck()
				&& mc.player.onGround
				&& !(Client.speedManager.getPlayerSpeed(mc.player) > this.maxSelfSpeed.getValue())
		) {
			this.block();
		}
	}

	private void placeBlock(BlockPos var1) {
		if (canPlace(var1)) {
			int var2 = mc.player.inventory.currentItem;
			if (RebirthUtil.findHotbarClass(BlockObsidian.class) != -1) {
				RebirthUtil.doSwap(RebirthUtil.findHotbarClass(BlockObsidian.class));
				BlockUtil.placeBlock(var1, EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue());
				RebirthUtil.doSwap(var2);
			}
		}
	}

	private void block() {
		BlockPos playerPos = RebirthUtil.getPlayerPos(); //burrow here
		if (
			this.getBlock(playerPos.up(2)) != Blocks.OBSIDIAN
				&& this.getBlock(playerPos.up(2)) != Blocks.BEDROCK
		) {
			int var2 = 0;
			if (this.whenDouble.getValue()) {
				for (EnumFacing var6 : EnumFacing.VALUES) {
					if (var6 != EnumFacing.DOWN && var6 != EnumFacing.UP) {
						if (this.getBlock(playerPos.offset(var6).up()) instanceof BlockPistonBase) {
							if (mc.world.getBlockState(playerPos.offset(var6).up())
								.getValue(BlockDirectional.FACING)
								.getOpposite() == var6) {
								++var2;
							}
						}
					}
				}
			}

			for (EnumFacing var14 : EnumFacing.VALUES) {
				if (var14 != EnumFacing.DOWN) {
					if (var14 != EnumFacing.UP) {
						BlockPos pistonpos = playerPos.offset(var14).up();
						if (this.getBlock(pistonpos) instanceof BlockPistonBase) {
							if (
								trap.getValue() == TrapMode.PISTONUP
									&& ( // 如果有burrow或者没有打开onlyBurrow
									this.getBlock(playerPos) != Blocks.AIR
										|| !this.onlyBurrow.getValue()
								)) this.placeBlock(pistonpos.up());

							if (
								trap.getValue() == TrapMode.SMART
									&& ( // 如果有burrow或者没有打开onlyBurrow
									this.getBlock(playerPos) != Blocks.AIR
										|| !this.onlyBurrow.getValue()
								)
							) {
								Block b = UtilsRewrite.uBlock.getBlock(pistonpos.up());
								if (b == Blocks.AIR) {
									this.placeBlock(pistonpos.up());
								} else {
									if (b == Blocks.REDSTONE_BLOCK) {
										this.placeBlock(playerPos.up(2));
										if (!RebirthUtil.canPlaceEnum(playerPos.up(2))) {
											for (EnumFacing var10 : EnumFacing.VALUES) {
												if (canPlace(playerPos.offset(var10).up(2))) {
													this.placeBlock(playerPos.offset(var10).up(2));
													break;
												}
											}
										}

									}
								}
							}
							if (mc.world.getBlockState(playerPos.offset(var14).up())
								.getValue(BlockDirectional.FACING)
								.getOpposite() == var14) {
								this.placeBlock(playerPos.up().offset(var14, -1));
								if (
									this.trap.getValue() == TrapMode.HEAD
										&& (
										this.getBlock(playerPos) != Blocks.AIR
											|| !this.onlyBurrow.getValue()
											|| var2 >= 2
									)
								) {
									this.placeBlock(playerPos.up(2));
									if (!RebirthUtil.canPlaceEnum(playerPos.up(2))) {
										for (EnumFacing var10 : EnumFacing.VALUES) {
											if (canPlace(playerPos.offset(var10).up(2))) {
												this.placeBlock(playerPos.offset(var10).up(2));
												break;
											}
										}
									}
								}
								if (!RebirthUtil.canPlaceEnum(playerPos.up().offset(var14, -1)) && this.helper.getValue()) {
									if (RebirthUtil.canPlaceEnum(playerPos.offset(var14, -1))) {
										this.placeBlock(playerPos.offset(var14, -1));
									} else {
										this.placeBlock(playerPos.offset(var14, -1).down());
									}
								}
							}
						}
					}
				}

			}
		}
	}

	enum TrapMode {HEAD, PISTONUP, SMART, NOTRAP}

}
