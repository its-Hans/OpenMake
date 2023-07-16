package cn.make.module.player;

import cn.make.module.combat.RebirthPullCrystal;
import cn.make.util.skid.RebirthUtil;
import cn.make.util.skid.RenderUtil;
import cn.make.util.skid.Timer;
import cn.make.util.skid.two.BlockUtil;
import chad.phobos.api.events.block.BlockEvent;
import chad.phobos.api.events.client.ClientEvent;
import chad.phobos.api.events.network.PacketEvent;
import chad.phobos.api.events.render.Render3DEvent;
import chad.phobos.api.center.Command;
import chad.phobos.api.center.Module;
import chad.phobos.api.setting.Bind;
import chad.phobos.api.setting.Setting;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemAir;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerDigging.Action;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

public class RebirthMine extends Module {
	public static final List<Block> godBlocks = Arrays.asList(
		Blocks.COMMAND_BLOCK, Blocks.FLOWING_LAVA, Blocks.LAVA, Blocks.FLOWING_WATER, Blocks.WATER, Blocks.BEDROCK, Blocks.BARRIER
	);
	public static RebirthMine INSTANCE;
	public static BlockPos breakPos;
	private final Timer delayTimer;
	public Setting<page> pageSetting;
	private final Setting<Float> damage;
	private final Setting<Float> range;
	private final Setting<Boolean> rotate;
	private final Setting<Boolean> wait;
	private final Setting<Boolean> mineAir;
	private final Setting<Boolean> instant;

	private final Setting<Integer> delay;
	private final Setting<Integer> maxBreak;
	private final Setting<Boolean> restart;
	public final Setting<Boolean> godCancel;
	private final Setting<Bind> enderChest;

	private final Setting<Boolean> swing;
	private final Setting<Boolean> debug;
	public final Setting<Boolean> hotBar;
	private final Setting<Boolean> allowWeb;
	private final Setting<Boolean> switchReset;

	public Setting<Boolean> render;
	public Setting<Boolean> rendertext;
	public Setting<Integer> redColor;
	public Setting<Integer> blueColor;
	public Setting<Integer> greenColor;
	public Setting<Integer> alpha;

	private final Timer mineTimer;
	private final Timer firstTimer;
	Color color;
	boolean resetcolor = true;
	int nowalpha;
	int lastSlot;
	private int breakNumber;
	private boolean startMine;
	private boolean first;

	public RebirthMine() {
		super("PacketMine", "1", Category.PLAYER);
		this.pageSetting = this.register(new Setting("Page", page.General));
		this.damage = this.register(new Setting<>("Damage", 1.1F, 0.0F, 2.0F, v -> pageSetting.getValue() == page.General));
		this.range = this.register(new Setting<>("Range", 7.0F, 3.0F, 10.0F, v -> pageSetting.getValue() == page.General));
		this.rotate = this.register(new Setting<>("Rotate", true, v -> pageSetting.getValue() == page.General));
		this.wait = this.register(new Setting<>("Wait", false, v -> pageSetting.getValue() == page.General));
		this.mineAir = this.register(new Setting<>("MineAir", false, v -> pageSetting.getValue() == page.General));
		this.instant = this.register(new Setting<>("Instant", false, v -> pageSetting.getValue() == page.General));

		this.delay = this.register(new Setting<>("Delay", 60, 0, 1000, v -> pageSetting.getValue() == page.Control));
		this.maxBreak = this.register(new Setting<>("MaxBreak", 2, 1, 20, v -> pageSetting.getValue() == page.Control));
		this.restart = this.register(new Setting<>("ReStart", true, v -> pageSetting.getValue() == page.Control));
		this.godCancel = this.register(new Setting<>("GodCancel", true, v -> pageSetting.getValue() == page.Control));
		this.enderChest = this.register(new Setting<>("EnderChest", new Bind(-1), v -> pageSetting.getValue() == page.Control));

		this.swing = this.register(new Setting<>("Swing", true, v -> pageSetting.getValue() == page.Dev));
		this.debug = this.register(new Setting<>("Debug", false, v -> pageSetting.getValue() == page.Dev));
		this.hotBar = this.register(new Setting<>("HotBar", false, v -> pageSetting.getValue() == page.Dev));
		this.allowWeb = this.register(new Setting<>("AllowWeb", true, v -> pageSetting.getValue() == page.Dev));
		this.switchReset = this.register(new Setting<>("SwitchReset", false, v -> pageSetting.getValue() == page.Dev));

		this.render = rbool("Render", true, v -> pageSetting.getValue() == page.Render);
		this.rendertext = this.register(new Setting<>("RenderText", true, v -> pageSetting.getValue() == page.Render));
		this.redColor = this.register(new Setting("Red", 255, 0, 255, v -> (pageSetting.getValue() == page.Render && render.getValue())));
		this.greenColor = this.register(new Setting("Green", 255, 0, 255, v -> (pageSetting.getValue() == page.Render && render.getValue())));
		this.blueColor = this.register(new Setting("Blue", 255, 0, 255, v -> (pageSetting.getValue() == page.Render && render.getValue())));
		this.alpha = this.register(new Setting("Alpha", 120, 0, 255, v -> (pageSetting.getValue() == page.Render && render.getValue())));

		this.mineTimer = new Timer();
		this.delayTimer = new Timer();
		this.firstTimer = new Timer();
		this.startMine = false;
		this.first = false;
		this.breakNumber = 0;
		this.lastSlot = -1;
		INSTANCE = this;
	}

	public static float getDestroySpeed(IBlockState var0, ItemStack var1) {
		float var2 = 1.0F;
		if (var1 != null && !var1.isEmpty()) {
			var2 *= var1.getDestroySpeed(var0);
		}

		return var2;
	}
	public static float getDigSpeed(IBlockState var0, ItemStack var1) {
		float var2 = getDestroySpeed(var0, var1);
		if (var2 > 1.0F) {
			int var3 = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, var1);
			if (var3 > 0 && !var1.isEmpty()) {
				var2 = (float) ((double) var2 + StrictMath.pow(var3, 2.0) + 1.0);
			}
		}

		if (mc.player.isPotionActive(MobEffects.HASTE)) {
			var2 *= 1.0F + (float) (mc.player.getActivePotionEffect(MobEffects.HASTE).getAmplifier() + 1) * 0.2F;
		}

		if (mc.player.isPotionActive(MobEffects.MINING_FATIGUE)) {
			float var4;
			switch (mc.player.getActivePotionEffect(MobEffects.MINING_FATIGUE).getAmplifier()) {
				case 0:
					var4 = 0.3F;
					boolean var6 = false;
					break;
				case 1:
					var4 = 0.09F;
					boolean var5 = false;
					break;
				case 2:
					var4 = 0.0027F;
					boolean var10000 = false;
					break;
				case 3:
				default:
					var4 = 8.1E-4F;
			}

			var2 *= var4;
		}

		if (mc.player.isInsideOfMaterial(Material.WATER) && !EnchantmentHelper.getAquaAffinityModifier(mc.player)) {
			var2 /= 5.0F;
		}

		float var7;
		if (var2 < 0.0F) {
			var7 = 0.0F;
			boolean var10001 = false;
		} else {
			var7 = var2;
		}

		return var7;
	}
	public static float getBlockStrength(BlockPos var0, ItemStack var1) {
		IBlockState var2 = mc.world.getBlockState(var0);
		float var3 = var2.getBlockHardness(mc.world, var0);
		if (var3 < 0.0F) {
			return 0.0F;
		} else {
			return !canBreak(var0) ? getDigSpeed(var2, var1) / var3 / 100.0F : getDigSpeed(var2, var1) / var3 / 30.0F;
		}
	}
	private static boolean canBreak(BlockPos var0) {
		IBlockState var1 = mc.world.getBlockState(var0);
		Block var2 = var1.getBlock();
		boolean var10000;
		if (var2.getBlockHardness(var1, mc.world, var0) != -1.0F) {
			var10000 = true;
			boolean var10001 = false;
		} else {
			var10000 = false;
		}

		return var10000;
	}
	@Override
	public void onTick() {
		if (breakPos == null) {
			this.breakNumber = 0;
			this.startMine = false;
		} else if (!mc.player.isCreative()
			&& !(
			mc.player.getDistance((double) breakPos.getX() + 0.5, (double) breakPos.getY() + 0.5, (double) breakPos.getZ() + 0.5)
				> (double) this.range.getValue().floatValue()
		)
			&& this.breakNumber <= this.maxBreak.getValue() - 1
			&& (this.wait.getValue() || !mc.world.isAirBlock(breakPos) || this.instant.getValue())) {
			if (godBlocks.contains(mc.world.getBlockState(breakPos).getBlock())) {
				if (this.godCancel.getValue()) {
					breakPos = null;
					this.startMine = false;
				}
			} else {
				if (mc.world.isAirBlock(breakPos)) {
					if (this.enderChest.getValue().isDown() && RebirthUtil.canPlace(breakPos)) {
						int var1 = RebirthUtil.findHotbarBlock(Blocks.ENDER_CHEST);
						if (var1 != -1) {
							int var2 = mc.player.inventory.currentItem;
							RebirthUtil.doSwap(var1);
							BlockUtil.placeBlock(breakPos, EnumHand.MAIN_HAND, this.rotate.getValue(), true);
							RebirthUtil.doSwap(var2);
						}
					}

					this.breakNumber = 0;
				}

				if (this.first) {
					if (this.firstTimer.passedMs(300L)) {
						mc.player.connection.sendPacket(new CPacketPlayerDigging(Action.STOP_DESTROY_BLOCK, breakPos, BlockUtil.getRayTraceFacing(breakPos)));
						this.first = false;
					}
				} else if (this.delayTimer.passedMs(this.delay.getValue().intValue())) {
					if (this.startMine) {
						if (mc.world.isAirBlock(breakPos)) {
							return;
						}

						if (!mc.player.onGround && (!this.allowWeb.getValue() || !mc.player.isInWeb)) {
							return;
						}

						if (RebirthPullCrystal.INSTANCE.isOn()
							&& breakPos.equals(RebirthPullCrystal.powerPos)
							&& RebirthPullCrystal.crystalPos != null
							&& !RebirthUtil.posHasCrystal(RebirthPullCrystal.crystalPos)) {
							return;
						}

						int var4 = this.getTool(breakPos);
						if (var4 == -1) {
							var4 = mc.player.inventory.currentItem + 36;
						}

						if (this.mineTimer
							.passedMs(
								(long) (
									1.0F
										/ getBlockStrength(breakPos, mc.player.inventoryContainer.getInventory().get(var4))
										/ 20.0F
										* 1000.0F
										* this.damage.getValue()
								)
							)) {
							int var6 = mc.player.inventory.currentItem;
							boolean var10000;
							if (var6 + 36 != var4) {
								var10000 = true;
								boolean var10001 = false;
							} else {
								var10000 = false;
							}

							boolean var3 = var10000;
							if (var3) {
								if (this.hotBar.getValue()) {
									RebirthUtil.doSwap(var4 - 36);
								} else {
									mc.playerController.windowClick(0, var4, var6, ClickType.SWAP, mc.player);
								}
								var10000 = false;
							}

							if (this.rotate.getValue()) {
								RebirthUtil.facePosFacing(breakPos, BlockUtil.getRayTraceFacing(breakPos));
							}

							if (this.swing.getValue()) {
								mc.player.swingArm(EnumHand.MAIN_HAND);
							}

							mc.player.connection.sendPacket(new CPacketPlayerDigging(Action.STOP_DESTROY_BLOCK, breakPos, BlockUtil.getRayTraceFacing(breakPos)));
							if (var3) {
								if (this.hotBar.getValue()) {
									RebirthUtil.doSwap(var6);
									var10000 = false;
								} else {
									mc.playerController.windowClick(0, var4, var6, ClickType.SWAP, mc.player);
									var10000 = false;
								}
							}

							++this.breakNumber;
							this.delayTimer.reset();
							var10000 = false;
						}

						boolean var12 = false;
					} else {
						if (!this.mineAir.getValue() && mc.world.isAirBlock(breakPos)) {
							return;
						}

						int var5 = this.getTool(breakPos);
						if (var5 == -1) {
							var5 = mc.player.inventory.currentItem + 36;
						}

						this.mineTimer.reset();
						boolean var13 = false;
						if (this.swing.getValue()) {
							mc.player.swingArm(EnumHand.MAIN_HAND);
						}

						mc.player.connection.sendPacket(new CPacketPlayerDigging(Action.START_DESTROY_BLOCK, breakPos, BlockUtil.getRayTraceFacing(breakPos)));
						this.delayTimer.reset();
						var13 = false;
					}
				}
			}
		} else {
			this.startMine = false;
			this.breakNumber = 0;
			breakPos = null;
		}
	}
	@Override
	public void onDisable() {
		this.startMine = false;
		breakPos = null;
	}
	@Override
	public void onEnable() {
		resetcolor = true;
	}
	@Override
	public void onRender3D(Render3DEvent var1) {
		if (!mc.player.isCreative() && breakPos != null) {
			if (this.debug.getValue()) {
				EnumFacing fac = BlockUtil.getRayTraceFacing(breakPos);
				RenderUtil.drawBBFill(new AxisAlignedBB(breakPos.offset(fac)), new Color(240, 240, 240), 40);
				RenderUtil.drawText(new AxisAlignedBB(breakPos.offset(fac)), "facing " + fac.getName());
			}
			if (rendertext.getValue()) RenderUtil.drawText(breakPos, "onBreak");
			if (render.getValue()) {
				if (breakPos != null && (!mc.world.isAirBlock(breakPos) || wait.getValue())) {
					Color olC = new Color(240, 240, 240, 60);
					Color bxC = new Color(redColor.getValue(), greenColor.getValue(), blueColor.getValue(), alpha.getValue());
					RenderUtil.drawBlockOutline(breakPos, olC, 2.4F, false);
					RenderUtil.drawBox(breakPos, bxC);
				}
			}
		}
	}
	@SubscribeEvent
	public void onSettingChange(ClientEvent event) {
		if (event.getStage() == 2 && event.getSetting().getFeature().equals(this)) {
			resetcolor = true;
		}
	}

	@SubscribeEvent(
		priority = EventPriority.LOWEST
	)
	public void onSend(PacketEvent.Send var1) {
		if (
			!fullNullCheck()
				&& !mc.player.isCreative()
				&& this.debug.getValue()
				&& var1.getPacket() instanceof CPacketPlayerDigging
		) {
			Command.sendMessage(((CPacketPlayerDigging) var1.getPacket()).getAction().name());
		}
	}

	@SubscribeEvent
	public void onPacketSend(PacketEvent.Send var1) {
		if (!fullNullCheck() && !mc.player.isCreative()) {
			if (var1.getPacket() instanceof CPacketHeldItemChange) {
				if (((CPacketHeldItemChange) var1.getPacket()).getSlotId() != this.lastSlot) {
					this.lastSlot = ((CPacketHeldItemChange) var1.getPacket()).getSlotId();
					if (this.switchReset.getValue()) {
						this.startMine = false;
						this.mineTimer.reset();
						boolean var2 = false;
					}
				}
			} else if (var1.getPacket() instanceof CPacketPlayerDigging) {
				if (((CPacketPlayerDigging) var1.getPacket()).getAction() == Action.START_DESTROY_BLOCK) {
					if (breakPos == null || !((CPacketPlayerDigging) var1.getPacket()).getPosition().equals(breakPos)) {
						var1.setCanceled(true);
						return;
					}

					this.startMine = true;
					boolean var10000 = false;
				} else if (((CPacketPlayerDigging) var1.getPacket()).getAction() == Action.STOP_DESTROY_BLOCK) {
					if (breakPos == null || !((CPacketPlayerDigging) var1.getPacket()).getPosition().equals(breakPos)) {
						var1.setCanceled(true);
						return;
					}

					if (!this.instant.getValue()) {
						this.startMine = false;
					}
				}
			}
		}
	}

	private int getTool(BlockPos var1) {
		if (this.hotBar.getValue()) {
			int var8 = -1;
			float var9 = 1.0F;

			for (int var10 = 0; var10 < 9; ++var10) {
				ItemStack var11 = mc.player.inventory.getStackInSlot(var10);
				if (var11 != ItemStack.EMPTY) {
					float var12 = (float) EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, var11);
					float var13 = var11.getDestroySpeed(mc.world.getBlockState(var1));
					if (var12 + var13 > var9) {
						var9 = var12 + var13;
						var8 = 36 + var10;
					}
				}

				boolean var14 = false;
			}

			return var8;
		} else {
			AtomicInteger var2 = new AtomicInteger();
			var2.set(-1);
			float var3 = 1.0F;

			for (Entry var5 : RebirthUtil.getInventoryAndHotbarSlots().entrySet()) {
				if (!(((ItemStack) var5.getValue()).getItem() instanceof ItemAir)) {
					float var6 = (float) EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, (ItemStack) var5.getValue());
					float var7 = ((ItemStack) var5.getValue()).getDestroySpeed(mc.world.getBlockState(var1));
					if (var6 + var7 > var3) {
						var3 = var6 + var7;
						var2.set((Integer) var5.getKey());
					}
				}

				boolean var10000 = false;
			}

			return var2.get();
		}
	}

	@SubscribeEvent
	public void onClickBlock(BlockEvent var1) {
		if (!fullNullCheck() && !mc.player.isCreative()) {
			var1.setCanceled(true);
			if (!godBlocks.contains(mc.world.getBlockState(var1.pos).getBlock()) || !this.godCancel.getValue()) {
				if (!var1.pos.equals(breakPos)) {
					breakPos = var1.pos;
					this.mineTimer.reset();
					boolean var10000 = false;
					if (!godBlocks.contains(mc.world.getBlockState(var1.pos).getBlock())) {
						if (this.restart.getValue() && !this.instant.getValue()) {
							this.first = true;
						}

						this.firstTimer.reset();
						var10000 = false;
						mc.player.connection.sendPacket(new CPacketPlayerDigging(Action.START_DESTROY_BLOCK, breakPos, BlockUtil.getRayTraceFacing(breakPos)));

						if (this.swing.getValue()) {
							mc.player.swingArm(EnumHand.MAIN_HAND);
						}

						this.breakNumber = 0;
					}
				}
			}
		}
	}
	enum page{General, Control, Dev, Render}
	enum rendermode{dont, randomwb, randomalpha}

}
