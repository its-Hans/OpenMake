package cn.make.module.combat;

import cn.make.util.UtilsRewrite;
import cn.make.util.skid.RebirthUtil;
import cn.make.util.skid.Timer;
import chad.phobos.api.events.block.BlockEvent;
import chad.phobos.api.events.render.Render3DEvent;
import chad.phobos.api.center.Module;
import chad.phobos.api.setting.Setting;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockObsidian;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Objects;

public class CivSelect extends Module {
	BlockPos dmgPos = null;
	int progress = 0;
	Timer timer = new Timer();
	public Setting<Integer> delay = rinte("delay", 0, 0, 2000);
	public Setting<Boolean> once = rbool("Once", true);
	public Setting<Boolean> stopClick = rbool("ResetOnClick", true);
	public Setting<Boolean> render = rbool("Render", true);
	public Setting<Integer> red = rinte("Red", 0, 0, 255, v -> render.getValue());
	public Setting<Integer> green = rinte("Green", 0, 0, 255, v -> render.getValue());
	public Setting<Integer> blue = rinte("Blue", 150, 0, 255, v -> render.getValue());
	public Setting<Integer> alpha = rinte("Alpha", 240, 0, 255, v -> render.getValue());
	public Setting<Double> range = rdoub("Range", 4.4, 1.0, 8.0);

	public CivSelect() {
		super("CivSelect", "description", Category.COMBAT);
	}
	public Color getColor() {
		return new Color(red.getValue(), green.getValue(), blue.getValue());
	}
	@Override
	public void onEnable() {
		resets();
	}
	@Override
	public void onDisable() {
		resets();
	}

	@SubscribeEvent
	public void OnDamageBlock(final BlockEvent event) {
		if (nullCheck()) return;
		if (event == null) return;
		if (UtilsRewrite.uBlock.getBlock(event.pos) != Blocks.OBSIDIAN) return;
		if (dmgPos == event.pos) {
			if (stopClick.getValue()) {
				resets();
			}
			return;
		}
		dmgPos = event.pos;
	}
	@Override
	public void onUpdate() {
		if (fullNullCheck()) {
			resets();
			return;
		}
		if (dmgPos == null) return;
		if (UtilsRewrite.uBlock.getRange(dmgPos) > range.getValue()) {
			return;
		}
		if (
			UtilsRewrite.uInventory.itemSlot(Items.END_CRYSTAL) == -1
			|| UtilsRewrite.uInventory.itemSlot(Item.getItemFromBlock(Blocks.OBSIDIAN)) == -1
		) {
			resets();
			return;
		}
		BlockPos cPos = dmgPos.up();
		if (UtilsRewrite.uBlock.getBlock(dmgPos) instanceof BlockAir) {
			onAirDoSome(cPos);
		}
		if (UtilsRewrite.uBlock.getBlock(dmgPos) instanceof BlockObsidian) {
			placecrystal(cPos);
		}
		if (once.getValue()) {
			resets();
		}
	}
	synchronized void onAirDoSome(BlockPos pos) {
		if (RebirthUtil.posHasCrystal(pos)) {
			if (timer.passedMs(delay.getValue())) {
				timer.reset();
				RebirthUtil.attackCrystal(pos, true, true);
			}
		} else {
			UtilsRewrite.uBlock.doPlace2(dmgPos, null, true, true, Blocks.OBSIDIAN);
		}

	}
	synchronized void placecrystal(BlockPos pos) {
		if (RebirthUtil.canPlaceCrystal(pos)) {
			RebirthUtil.facePosFacing(pos.down(), EnumFacing.UP);
			int old = mc.player.inventory.currentItem;
			UtilsRewrite.uInventory.heldItemChange(UtilsRewrite.uInventory.itemSlot(Items.END_CRYSTAL), true, false, true);
			RebirthUtil.placeCrystal(pos, true, false);
			UtilsRewrite.uInventory.heldItemChange(old, true, false, true);
		}
	}// 定义一个boolean变量来控制反向渲染
	boolean reverse = false;
	@Override
	public void onRender3D(Render3DEvent event) {
		if (!render.getValue()) return;
		if (dmgPos != null) {
			renderc.drawProgressBB(
				progress,
				dmgPos,
				getColor(),
				alpha.getValue(),
				reverse // 使用reverse变量来控制反向渲染
			);
		}
	}
	@Override
	public void onTick() {
		if (dmgPos != null) {
			if (reverse) { // 如果反向渲染为true
				if (progress > 0) { // 如果progress大于0
					progress--; // 减少progress
				} else { // 如果progress等于0
					reverse = false; // 反转reverse变量
				}
			} else { // 如果反向渲染为false
				if (progress < 100) { // 如果progress小于100
					progress++; // 增加progress
				} else { // 如果progress等于100
					reverse = true; // 反转reverse变量
				}
			}
		} else {
			resets();
		}
	}

	public void resets() {
		timer.reset();
		dmgPos = null;
		progress = 0;
	}

	public static class renderc {
		public static ICamera camera = new Frustum();
		public static void drawProgressBB(int progress, BlockPos pos, Color color, int alpha, boolean reverse) {
			// check progress
			if (progress < 0 || progress > 100) {
				return;
			}
			// check alpha
			if (alpha < 0 || alpha > 255) {
				return;
			}
			// 计算包围盒的高度比例
			double ratio = progress / 100.0;
			// 创建一个AxisAlignedBB对象，表示包围盒
			AxisAlignedBB bb;
			// 根据reverse的值，确定包围盒的上下边界
			if (reverse) {
				// 如果reverse为true，则从顶部开始渲染
				bb = new AxisAlignedBB(pos.getX(), pos.getY() + (1 - ratio), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
			} else {
				// 如果reverse为false，则从底部开始渲染
				bb = new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + ratio, pos.getZ() + 1);
			}
			// 调用drawBBFill方法，渲染填充的包围盒
			drawBBFill(bb, color, alpha);
		}

		public static void drawBBFill(AxisAlignedBB BB, Color color, int alpha) {
			AxisAlignedBB bb = new AxisAlignedBB(BB.minX - mc.getRenderManager().viewerPosX, BB.minY - mc.getRenderManager().viewerPosY, BB.minZ - mc.getRenderManager().viewerPosZ, BB.maxX - mc.getRenderManager().viewerPosX, BB.maxY - mc.getRenderManager().viewerPosY, BB.maxZ - mc.getRenderManager().viewerPosZ);
			camera.setPosition(Objects.requireNonNull(mc.getRenderViewEntity()).posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);
			if (camera.isBoundingBoxInFrustum(new AxisAlignedBB(bb.minX + mc.getRenderManager().viewerPosX, bb.minY + mc.getRenderManager().viewerPosY, bb.minZ + mc.getRenderManager().viewerPosZ, bb.maxX + mc.getRenderManager().viewerPosX, bb.maxY + mc.getRenderManager().viewerPosY, bb.maxZ + mc.getRenderManager().viewerPosZ))) {
				GlStateManager.pushMatrix();
				GlStateManager.enableBlend();
				GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
				GlStateManager.disableDepth();
				GlStateManager.disableAlpha();
				GlStateManager.disableCull();
				GL11.glHint(3154, 4354);
				GlStateManager.shadeModel(7425);
				GlStateManager.disableTexture2D();
				GlStateManager.disableLighting();
				GlStateManager.depthMask(false);
				RenderGlobal.renderFilledBox(bb, color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, (float) alpha / 255.0f);
				GlStateManager.depthMask(true);
				GlStateManager.enableLighting();
				GlStateManager.enableTexture2D();
				GlStateManager.shadeModel(7424);
				GlStateManager.enableAlpha();
				GlStateManager.enableCull();
				GlStateManager.enableDepth();
				GlStateManager.disableBlend();
				GlStateManager.glLineWidth(1.0f);
				GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
				GlStateManager.popMatrix();
			}
		}
	}
}
