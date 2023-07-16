package cn.make.module.misc;

import cn.make.Targets;
import cn.make.util.UtilsRewrite;
import cn.make.util.rotateutils;
import cn.make.util.skid.RebirthUtil;
import chad.phobos.api.center.Module;
import chad.phobos.api.setting.Setting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SimpleCev extends Module {
	public SimpleCev() {
		super("SimpleCev", "description", Category.MISC);
	}
	EntityPlayer target;
	enum Mode{Civ,Cev,Smart}
	public Setting<Mode> cevMode = rother("CevMode", Mode.Smart);
	public Setting<Double> range = rdoub("Range", 5.6, 2.0, 6.0);
	public Setting<Boolean> xin = rbool("xin", false);
	public BlockPos genCiv(EntityPlayer target) {
		BlockPos face = target.getPosition().up();
		List<BlockPos> canCrystalList = new ArrayList<>();
		for (BlockPos civPos: Arrays.asList(face.west(), face.south(), face.north(), face.east())) {
			BlockPos crystalPos = civPos.up();
			if (RebirthUtil.canPlaceCrystal(crystalPos)) canCrystalList.add(crystalPos);
		}
		return UtilsRewrite.uBlock.bestDistance(canCrystalList);
	}
	public BlockPos genCev(EntityPlayer target) {
		BlockPos cevPos = target.getPosition().up(3);
		if (RebirthUtil.canPlaceCrystal(cevPos)) return cevPos;
		return null;
	}
	public BlockPos smart(EntityPlayer target) {
		List<BlockPos> cevList = Arrays.asList(genCiv(target), genCev(target));
		return UtilsRewrite.uBlock.bestDistance(cevList);
	}
	@Override
	public void onUpdate() {
		if (fullNullCheck()) return;
		target = Targets.getTarget();
		if (target == null) return;
		BlockPos crystalPos = null;
		switch (cevMode.getValue()) {
			case Smart: {
				crystalPos = smart(target);
				break;
			}
			case Cev: {
				crystalPos = genCev(target);
				break;
			}
			case Civ: {
				crystalPos = genCiv(target);
				break;
			}
		}
		if (crystalPos == null) return;
		if (xin.getValue()) {
			rotateutils.clckBlockXIN(crystalPos.down(), false);
		} else {
			mc.world.getBlockState(crystalPos.down()).getBlock();
			mc.playerController.onPlayerDamageBlock(crystalPos.down(), EnumFacing.UP);
		}
	}
}
