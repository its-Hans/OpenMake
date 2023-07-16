package cn.make.module.render;

import chad.phobos.api.center.Module;
import chad.phobos.api.events.block.DamageBlockEvent;
import chad.phobos.api.events.render.Render3DEvent;
import chad.phobos.api.setting.Setting;
import cn.make.module.movement.HoleSnap;
import cn.make.util.skid.FadeUtils;
import cn.make.util.skid.RenderUtil;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class BreakESP
	extends Module {
    public static BreakESP INSTANCE = new BreakESP();
	static final HashMap<EntityPlayer, MinePosition> MineMap = new HashMap<>();
	Setting<Boolean> renderAir = rbool("RenderAir", true);
	Setting<Boolean> renderUnknown = rbool("RenderUnknown", true);
    Setting<Boolean> notiSafety = rbool("NotiSafetyOnBreak", true);
	Setting<Double> range = rdoub("Range", 15.0, 0.0, 50.0);
	Setting<Mode> animationMode = rother("AnimationMode", Mode.Up);
	Setting<Integer> animationTime = rinte("AnimationTime", 1000, 0, 5000);
	Setting<Boolean> box = rbool("Box", true);
	Setting<Integer> boxAlpha = rinte("BoxAlpha", 30, 0, 255, v -> this.box.getValue());
	Setting<Boolean> outline = rbool("Outline", true);
	Setting<Integer> outlineAlpha = rinte("OutlineAlpha", 100, 0, 255, v -> this.outline.getValue());
	Setting<Integer> color1r = rinte("ColorRed", 255, 0, 255);
	Setting<Integer> color1g = rinte("ColorGreen", 255, 0, 255);
	Setting<Integer> color1b = rinte("ColorBlue", 255, 0, 255);
	Setting<Boolean> doubleRender = rbool("DoubleRender", false);
	Setting<Integer> color2r = rinte("SecondRed", 255, 0, 255, v -> doubleRender.getValue());
	Setting<Integer> color2g = rinte("SecondGreen", 255, 0, 255, v -> doubleRender.getValue());
	Setting<Integer> color2b = rinte("SecondBlue", 255, 0, 255, v -> doubleRender.getValue());

	public BreakESP() {
		super("BreakESP", "Show mine postion", Category.RENDER);
		INSTANCE = this;
	}

	@Override
	public void onDisable() {
		MineMap.clear();
	}

	@SubscribeEvent
	public void BlockBreak(DamageBlockEvent event) {
		if (event.getPosition().getY() == -1) {
			return;
		}
		EntityPlayer breaker = (EntityPlayer) mc.world.getEntityByID(event.getBreakerId());
		if (breaker == null || breaker.getDistance((double) event.getPosition().getX() + 0.5, event.getPosition().getY(), (double) event.getPosition().getZ() + 0.5) > 7.0) {
			return;
		}
		if (!MineMap.containsKey(breaker)) {
			MineMap.put(breaker, new MinePosition(breaker));
		}
		MineMap.get(breaker).update(event.getPosition());
	}

	@Override
	public void onRender3D(Render3DEvent event) {
		EntityPlayer[] array;
		for (EntityPlayer entityPlayer : array = MineMap.keySet().toArray(new EntityPlayer[0])) {
			if (entityPlayer == null || entityPlayer.isEntityAlive()) continue;
			MineMap.remove(entityPlayer);
		}
        for (MinePosition miner : MineMap.values()) {
            if (
                !this.renderAir.getValue() //不允许渲染空气
                    && mc.world.isAirBlock(miner.first) //挖掘块是空气
            ) {
                miner.finishFirst(); //完成第一次
            }
            if (!(
                (miner.firstFinished && !this.renderAir.getValue()) //第一次完成并且不允许渲染空气
                    || miner.miner == mc.player //挖掘者是自己
                    || miner.first.getDistance(
                        (int) mc.player.posX,
                        (int) mc.player.posY,
                        (int) mc.player.posZ
                ) > this.range.getValue() //挖掘块离玩家的距离大于range
                    || (miner.miner == null && !this.renderUnknown.getValue()) //未知挖掘者且不允许渲染未知挖掘者
            )) {
                this.draw(miner.first, miner.firstFade.easeOutQuad(), getFirstColor());
            }
            if ((miner.miner != mc.player) && !miner.secondFinished && miner.second != null) {
                if (mc.world.isAirBlock(miner.second)) {
                    miner.finishSecond();
                } else if (
                    !miner.second.equals(miner.first)
                        && miner.second.getDistance(
                        (int) mc.player.posX,
                        (int) mc.player.posY,
                        (int) mc.player.posZ
                    ) < this.range.getValue()
                        && (miner.miner != null || this.renderUnknown.getValue())
                        && this.doubleRender.getValue()
                ) {
                    this.draw(miner.second, miner.secondFade.easeOutQuad(), getSecondColor());
                }
            }
        }
    }

	public void draw(BlockPos pos, double size, Color color) {
        notiBeBreakingIfPosAreSelfSafety(pos);
		if (this.animationMode.getValue() != Mode.Both) {
			AxisAlignedBB axisAlignedBB;
			if (this.animationMode.getValue() == Mode.InToOut) {
				axisAlignedBB = mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos).grow(size / 2.0 - 0.5);
			} else if (this.animationMode.getValue() == Mode.Up) {
				AxisAlignedBB bb = mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos);
				axisAlignedBB = new AxisAlignedBB(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.minY + (bb.maxY - bb.minY) * size, bb.maxZ);
			} else if (this.animationMode.getValue() == Mode.Down) {
				AxisAlignedBB bb = mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos);
				axisAlignedBB = new AxisAlignedBB(bb.minX, bb.maxY - (bb.maxY - bb.minY) * size, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
			} else if (this.animationMode.getValue() == Mode.None) {
				axisAlignedBB = mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos);
			} else {
				AxisAlignedBB bb = mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos).grow(size / 2.0 - 0.5);
				AxisAlignedBB bb2 = mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos);
				axisAlignedBB = this.animationMode.getValue() == Mode.Horizontal ? new AxisAlignedBB(bb2.minX, bb.minY, bb2.minZ, bb2.maxX, bb.maxY, bb2.maxZ) : new AxisAlignedBB(bb.minX, bb2.minY, bb.minZ, bb.maxX, bb2.maxY, bb.maxZ);
			}
			if (this.outline.getValue()) {
				RenderUtil.drawBBBox(axisAlignedBB, color, this.outlineAlpha.getValue());
			}
			if (this.box.getValue()) {
				RenderUtil.drawBBFill(axisAlignedBB, color, this.boxAlpha.getValue());
			}
		} else {
			AxisAlignedBB axisAlignedBB = mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos).grow(size / 2.0 - 0.5);
			if (this.outline.getValue()) {
				RenderUtil.drawBBBox(axisAlignedBB, color, this.outlineAlpha.getValue());
			}
			if (this.box.getValue()) {
				RenderUtil.drawBBFill(axisAlignedBB, color, this.boxAlpha.getValue());
			}
			axisAlignedBB = mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos).grow(-Math.abs(size / 2.0 - 1.0));
			if (this.outline.getValue()) {
				RenderUtil.drawBBBox(axisAlignedBB, color, this.outlineAlpha.getValue());
			}
			if (this.box.getValue()) {
				RenderUtil.drawBBFill(axisAlignedBB, color, this.boxAlpha.getValue());
			}
		}
	}

	public Color getFirstColor() {
		return new Color(color1r.getValue(), color1g.getValue(), color1b.getValue());
	}

	public Color getSecondColor() {
		return new Color(color2r.getValue(), color2g.getValue(), color2b.getValue());
	}

	public enum Mode {
		Down,
		Up,
		InToOut,
		Both,
		Vertical,
		Horizontal,
		None
	}

	private static class MinePosition {
		public final EntityPlayer miner;
		public FadeUtils firstFade;
		public FadeUtils secondFade;
		public BlockPos first;
		public BlockPos second;
		public boolean secondFinished;
		public boolean firstFinished;

		public MinePosition(final EntityPlayer player) {
			this.firstFade = new FadeUtils(BreakESP.INSTANCE.animationTime.getValue());
			this.secondFade = new FadeUtils(BreakESP.INSTANCE.animationTime.getValue());
			this.miner = player;
			this.secondFinished = true;
		}

		public void finishSecond() {
			this.secondFinished = true;
		}

		public void finishFirst() {
			this.firstFinished = true;
		}

		public void update(BlockPos pos) {
			if (this.first != null && this.first.equals(pos) && INSTANCE.renderAir.getValue()) {
				return;
			}
			if (this.secondFinished || this.second == null) {
				this.second = pos;
				this.secondFinished = false;
				this.secondFade = new FadeUtils(INSTANCE.animationTime.getValue());
			}
			if (this.first == null || !this.first.equals(pos) || this.firstFinished) {
				this.firstFade = new FadeUtils(INSTANCE.animationTime.getValue());
			}
			this.firstFinished = false;
			this.first = pos;
		}
	}
    public void notiBeBreakingIfPosAreSelfSafety(BlockPos pos) {
        if (!notiSafety.getValue()) return;
        List<HoleSnap.Pair<Vec3i, String>> Safetys = Arrays.asList(
            new HoleSnap.Pair<>(new Vec3i(0, 0, 0), "Bur"),
            new HoleSnap.Pair<>(new Vec3i(1,0,0), "Sur"),
            new HoleSnap.Pair<>(new Vec3i(-1,0,0), "Sur"),
            new HoleSnap.Pair<>(new Vec3i(0,0,1), "Sur"),
            new HoleSnap.Pair<>(new Vec3i(0,0,-1), "Sur")
        );
        for (HoleSnap.Pair<Vec3i, String> pair: Safetys) {
            if (mc.player.getPosition().add(pair.getLeft()).toLong() == pos.toLong()) {
                mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new TextComponentString(ChatFormatting.RED + pair.getRight() + " onBreaking!"), 3);
                break;
            }
        }
    }
}

