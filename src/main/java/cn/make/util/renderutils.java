package cn.make.util;

import cn.make.util.skid.RenderUtil;
import chad.phobos.api.center.Command;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Objects;


public class renderutils {
	public static String simpleColorString(Color color) {
		return (color.getRed() + " " + color.getGreen() + " " + color.getBlue() + " " + color.getAlpha());
	}

	// 定义一个方法，用来根据进度渲染一个渐变色的方块高亮
	public static void BreakRenderGradient(int progress, BlockPos drawPos, Color top, Color bottom, int alpha, boolean debug) {
		// 检查进度是否在0~100之间，如果不是，直接返回
		if (progress < 0 || progress > 100) {
			if (debug) Command.sendMessage("Progress ?");
			return;
		}

		// 根据方块坐标创建一个AxisAlignedBB对象，表示方块的边界
		AxisAlignedBB box = new AxisAlignedBB(drawPos);

		// 根据进度计算高亮的高度，从0到1之间
		double height = progress / 100.0;

		// 定义两个颜色对象，表示方块的上下颜色
		Color topColor = new Color(top.getRed(), top.getGreen(), top.getBlue(), alpha); 
		Color bottomColor = new Color(bottom.getRed(), bottom.getGreen(), bottom.getBlue(), alpha);
		if (debug) Command.sendMessage("drawing pos " + BlockChecker.simpleXYZString(drawPos) + ", progress " + progress + ", topColor " + simpleColorString(topColor) + ", bottomColor " + simpleColorString(bottomColor));

		// 获取颜色的RGBA分量，并转换为0-1之间的浮点数
		float topRed = (float) topColor.getRed() / 255.0f;
		float topGreen = (float) topColor.getGreen() / 255.0f;
		float topBlue = (float) topColor.getBlue() / 255.0f;
		float topAlpha = (float) topColor.getAlpha() / 255.0f;
		float bottomRed = (float) bottomColor.getRed() / 255.0f;
		float bottomGreen = (float) bottomColor.getGreen() / 255.0f;
		float bottomBlue = (float) bottomColor.getBlue() / 255.0f;
		float bottomAlpha = (float) bottomColor.getAlpha() / 255.0f;

		// 保存当前的渲染状态
		GlStateManager.pushMatrix();
		// 开启混合模式，让颜色能够透明
		GlStateManager.enableBlend();
		// 关闭深度测试，让高亮能够覆盖方块
		GlStateManager.disableDepth();
		// 设置混合函数，让源颜色和目标颜色按照一定比例混合
		GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
		// 关闭纹理映射，让颜色能够均匀填充
		GlStateManager.disableTexture2D();
		// 关闭深度缓冲，让高亮能够显示在最前面
		GlStateManager.depthMask(false);
		// 开启抗锯齿，让高亮的边缘更加平滑
		GL11.glEnable(2848);
		// 设置抗锯齿的质量，越高越平滑
		GL11.glHint(3154, 4354);

		// 获取一个Tessellator对象，用来绘制图形
		Tessellator tessellator = Tessellator.getInstance();
		// 获取一个BufferBuilder对象，用来存储顶点数据
		BufferBuilder bufferBuilder = tessellator.getBuffer();
		// 开始绘制一个三角形带（GL_TRIANGLE_STRIP）类型的图形，使用位置和颜色两种属性
		bufferBuilder.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);

		// 添加顶点数据，每个顶点包含位置和颜色信息
		// 按照方块的八个顶点的顺序添加数据，每次添加两个相邻的顶点，形成一个三角形带
		// 上面四个顶点使用上面的颜色，下面四个顶点使用下面的颜色
		// 根据高度调整上面四个顶点的y坐标，让高亮能够根据进度渐渐覆盖方块
		bufferBuilder.pos(box.minX, box.minY, box.minZ).color(bottomRed, bottomGreen, bottomBlue, bottomAlpha).endVertex();
		bufferBuilder.pos(box.minX, box.minY + height, box.minZ).color(topRed, topGreen, topBlue, topAlpha).endVertex();

		bufferBuilder.pos(box.minX, box.minY, box.maxZ).color(bottomRed, bottomGreen, bottomBlue, bottomAlpha).endVertex();
		bufferBuilder.pos(box.minX, box.minY + height, box.maxZ).color(topRed, topGreen, topBlue, topAlpha).endVertex();

		bufferBuilder.pos(box.maxX, box.minY, box.maxZ).color(bottomRed, bottomGreen, bottomBlue, bottomAlpha).endVertex();
		bufferBuilder.pos(box.maxX, box.minY + height, box.maxZ).color(topRed, topGreen, topBlue, topAlpha).endVertex();

		bufferBuilder.pos(box.maxX, box.minY, box.minZ).color(bottomRed, bottomGreen, bottomBlue, bottomAlpha).endVertex();
		bufferBuilder.pos(box.maxX, box.minY + height, box.minZ).color(topRed, topGreen, topBlue, topAlpha).endVertex();

		// 为了闭合方块，再添加前面的两个顶点
		bufferBuilder.pos(box.minX, box.minY, box.minZ).color(bottomRed, bottomGreen, bottomBlue, bottomAlpha).endVertex();
		bufferBuilder.pos(box.minX, box.minY + height, box.minZ).color(topRed, topGreen, topBlue, topAlpha).endVertex();

		// 结束绘制，并渲染图形
		tessellator.draw();

		// 关闭抗锯齿
		GL11.glDisable(2848);
		// 恢复深度缓冲
		GlStateManager.depthMask(true);
		// 开启深度测试
		GlStateManager.enableDepth();
		// 开启纹理映射
		GlStateManager.enableTexture2D();
		// 关闭混合模式
		GlStateManager.disableBlend();
		// 恢复渲染状态
		GlStateManager.popMatrix();
		if (debug) Command.sendMessage("drawed");
	}
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
	public enum DrawMode {
		Down,
		Up,
		InToOut,
		Both,
		Vertical,
		Horizontal,
		None
	}
	public static void rebirthBlockAnimation(BlockPos pos, double size, Color color, int oAlpha, int bAlpha, DrawMode mode, boolean outline, boolean box) {
		// size == progress, maybe 0.0~1.0
		if (mode != DrawMode.Both) {
			AxisAlignedBB axisAlignedBB;
			if (mode == DrawMode.InToOut) {
				axisAlignedBB = mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos).grow(size / 2.0 - 0.5);
			} else if (mode == DrawMode.Up) {
				AxisAlignedBB bb = mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos);
				axisAlignedBB = new AxisAlignedBB(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.minY + (bb.maxY - bb.minY) * size, bb.maxZ);
			} else if (mode == DrawMode.Down) {
				AxisAlignedBB bb = mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos);
				axisAlignedBB = new AxisAlignedBB(bb.minX, bb.maxY - (bb.maxY - bb.minY) * size, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
			} else if (mode == DrawMode.None) {
				axisAlignedBB = mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos);
			} else {
				AxisAlignedBB bb = mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos).grow(size / 2.0 - 0.5);
				AxisAlignedBB bb2 = mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos);
				axisAlignedBB = mode == DrawMode.Horizontal ? new AxisAlignedBB(bb2.minX, bb.minY, bb2.minZ, bb2.maxX, bb.maxY, bb2.maxZ) : new AxisAlignedBB(bb.minX, bb2.minY, bb.minZ, bb.maxX, bb2.maxY, bb.maxZ);
			}
			if (outline) {
				RenderUtil.drawBBBox(axisAlignedBB, color, oAlpha);
			}
			if (box) {
				RenderUtil.drawBBFill(axisAlignedBB, color, bAlpha);
			}
		} else {
			AxisAlignedBB axisAlignedBB = mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos).grow(size / 2.0 - 0.5);
			if (outline) {
				RenderUtil.drawBBBox(axisAlignedBB, color, oAlpha);
			}
			if (box) {
				RenderUtil.drawBBFill(axisAlignedBB, color, bAlpha);
			}
			axisAlignedBB = mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos).grow(-Math.abs(size / 2.0 - 1.0));
			if (outline) {
				RenderUtil.drawBBBox(axisAlignedBB, color, oAlpha);
			}
			if (box) {
				RenderUtil.drawBBFill(axisAlignedBB, color, bAlpha);
			}
		}
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
			RenderGlobal.renderFilledBox(bb, color.getRed() / 255.0f, color.getGreen() / 255.0f, (float)((float)color.getBlue() / 255.0f), (float)((float)alpha / 255.0f));
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

	public static ICamera camera;
	public static Minecraft mc;
	static {
		camera = new Frustum();
		mc = Minecraft.getMinecraft();
	}

	public static void prepareGL3D() {
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.disableDepth();
		GlStateManager.disableAlpha();
		GlStateManager.disableCull();
		GL11.glEnable(2848);
		GL11.glHint(3154, 4354);
		GlStateManager.shadeModel(7425);
		GlStateManager.disableTexture2D();
		GlStateManager.disableLighting();
		GlStateManager.depthMask(false);
	}
	public static void releaseGL3D() {
		GlStateManager.depthMask(true);
		GlStateManager.enableLighting();
		GlStateManager.enableTexture2D();
		GL11.glDisable(2848);
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