package cn.make.module.misc;

import cn.make.util.BlockChecker;
import cn.make.util.renderutils;
import chad.phobos.api.events.block.BlockEvent;
import chad.phobos.api.events.render.Render3DEvent;
import chad.phobos.api.center.Module;
import chad.phobos.api.setting.Setting;
import chad.phobos.api.utils.RenderUtil;
import net.minecraft.block.BlockAir;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class ProgressRender extends Module {
	static final Map<BlockPos, Integer> blockMap = new LinkedHashMap<>();
	public Setting<Integer> renderTime = rinte("RenderTime", 2, 1, 20);
	public Setting<Integer> alpha = rinte("Alpha", 40, 1, 255);

	public Setting<Boolean> mineRender = rbool("RenderDigging", false);
	public Setting<Boolean> reverseRender = rbool("ReverseRender", false);
	public Setting<Boolean> text = rbool("ProgressText", false);
	public Setting<Boolean> waitAir = rbool("AirRender", false);
	public Setting<Boolean> alwaysRender = rbool("alwaysRender", true);
	public Setting<Boolean> debug = rbool("Debug", false);

	public Setting<Integer> red = rinte("Red", 40, 1, 255);
	public Setting<Integer> green = rinte("Green", 40, 1, 255);
	public Setting<Integer> blue = rinte("Blue", 40, 1, 255);
	BlockPos ondmg = null;

	public ProgressRender() {
		super("ProgressRender", "test", Category.MISC);
	}

	@Override
	public void onUpdate() {
		if (fullNullCheck()) return;
		if (waitAir.getValue()) {
			return;
		}
		for (BlockPos blockPos : blockMap.keySet()) {
			if (blockPos == null) return;
			if (BlockChecker.getBlockType(blockPos) instanceof BlockAir) {
				delMap2(blockPos);
				return;
			}
		}
	}

	@Override
	public void onEnable() {
		clearMap();
	}

	@Override
	public void onDisable() {
		clearMap();
	}

	@Override
	public void onLogin() {
		clearMap();
	}

	@Override
	public void onLogout() {
		clearMap();
	}

	@Override
	public void onTick() {
		if (fullNullCheck()) {
			return;
		}

		// 遍历HashMap并更新每个方块位置的进度
		for (Map.Entry<BlockPos, Integer> entry : blockMap.entrySet()) {
			BlockPos blockPos = entry.getKey();
			int progress = entry.getValue();

			// 更新进度
			progress++;

			// 将更新后的进度存回HashMap中
			blockMap.put(blockPos, progress);
			if (debug.getValue()) sendModuleMessage("update progress " + BlockChecker.simpleXYZString(blockPos));
		}
	}

	@Override
	public void onRender3D(Render3DEvent event) {
		if (fullNullCheck()) {
			return;
		}
		Color color = new Color(red.getValue(), green.getValue(), blue.getValue(), alpha.getValue());

		// 使用 Iterator 来遍历 blockProgressMap
		Iterator<Map.Entry<BlockPos, Integer>> iterator = blockMap.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<BlockPos, Integer> entry = iterator.next();
			BlockPos blockPos = entry.getKey();
			int progress = entry.getValue();

			// 计算方块位置的进度百分比
			int progressPercentage = progress * renderTime.getValue();
			if (progressPercentage > 100) {
				if (!alwaysRender.getValue()) {
					// 使用 Iterator 的 remove 方法来安全地删除元素
					iterator.remove();
					return;
				} else {
					// 如果进度超过100且是alwaysRender模式，则将进度百分比设置为100
					progressPercentage = 100;
				}
			}
			if (text.getValue()) RenderUtil.drawText(blockPos, ("break " + progressPercentage));
			renderutils.drawProgressBB(
				progressPercentage,
				blockPos,
				color,
				alpha.getValue(),
				reverseRender.getValue()
			);
		}
	}


	@SubscribeEvent
	public void OnDamageBlock(BlockEvent event) {
		if (fullNullCheck()) {
			return;
		}
		if (ondmg != null) delMap(ondmg);
		ondmg = event.pos;
		if (mineRender.getValue()) {
			putMap(ondmg);
		}
	}

	public void putMap(BlockPos pos) {
		if (this.isOn()) {
			blockMap.put(pos, 0);
			if (debug.getValue()) {
				sendModuleMessage("add pos " + BlockChecker.simpleXYZString(pos));
			}
		}
	}

	public void delMap(BlockPos pos) {
		if (this.isOn()) {
			synchronized (blockMap) {
				blockMap.remove(pos);
			}
			if (debug.getValue()) {
				sendModuleMessage("del pos " + BlockChecker.simpleXYZString(pos));
			}
		}
	}

	private void delMap2(BlockPos pos) {
		if (this.isOn()) {
			blockMap.remove(pos);
			if (debug.getValue()) {
				sendModuleMessage("del pos " + BlockChecker.simpleXYZString(pos));
			}
		}
	}


	public void clearMap() {
		blockMap.clear();
		if (debug.getValue()) sendModuleMessage("clear map");
	}
}
