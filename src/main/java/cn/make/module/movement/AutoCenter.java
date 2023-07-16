package cn.make.module.movement;

import cn.make.util.skid.EntityUtil;
import chad.phobos.Client;
import chad.phobos.api.center.Module;
import chad.phobos.modules.combat.Surround;
import chad.phobos.api.setting.Setting;
import chad.phobos.api.managers.CommandManager;
import net.minecraft.util.math.BlockPos;

public class AutoCenter extends Module {
	public AutoCenter() {
		super("AutoCenter", "CENTER", Category.MOVEMENT);
	}
	enum Mode {
		ONE,
		TWO
	}
	Setting<Mode> mode = rother("Mode", Mode.TWO);
	@Override
	public void onEnable() {
		if (mode.getValue() == Mode.ONE) {
			BlockPos startPos = EntityUtil.getRoundedBlockPos(Surround.mc.player);
			Client.positionManager.setPositionPacket(
				(double) startPos.getX() + 0.5,
				startPos.getY(),
				(double) startPos.getZ() + 0.5,
				true,
				true,
				true
			);
		}
		if (mode.getValue() == Mode.TWO) {
			BlockPos startPos = EntityUtil.getRoundedBlockPos(mc.player);
			CommandManager.setPositionPacket(
				(double) startPos.getX() + 0.5,
				startPos.getY(),
				(double) startPos.getZ() + 0.5,
				true,
				true,
				true
			);
		}
		this.disable();
	}
}
