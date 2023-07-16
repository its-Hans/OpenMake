package cn.make.module.combat;

import chad.phobos.api.center.Module;
import chad.phobos.api.setting.Setting;
import cn.make.Targets;
import cn.make.util.DelayBlockPlace;
import cn.make.util.UtilsRewrite;
import javafx.util.Pair;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.ArrayList;
import java.util.List;

public class AntiPushMake extends Module {
	static final PistonOffset[] pistonOffsets = new PistonOffset[]{ //静态final字段，只初始化一次
		new PistonOffset(
			new Vec3i(1, 1, 0),
			new Vec3i(-1, 1, 0)
		),
		new PistonOffset(
			new Vec3i(-1, 1, 0),
			new Vec3i(1, 1, 0)
		),
		new PistonOffset(
			new Vec3i(0, 1, 1),
			new Vec3i(0, 1, -1)
		),
		new PistonOffset(
			new Vec3i(0, 1, -1),
			new Vec3i(0, 1, 1)
		),
	};
	public Setting<APMode> antiPull = rother("AntiPull", APMode.Smart);
	public Setting<Boolean> antiRSFirst = rbool("AntiRedstoneFirst", false);
	public Setting<Double> rangeCheck = rdoub("FindPlayer", 8.0, 0.0, 12.0);
	public Setting<Integer> delay = rinte("Delay", 0, 0, 10);
	public Setting<Boolean> swing = rbool("swing", true, v -> delay.getValue() == 0);
	DelayBlockPlace place = new DelayBlockPlace();

	public AntiPushMake() {
		super("AntiPushRewrite", "description", Category.COMBAT);
	}

	@Override
	public void onUpdate() {
		if (fullNullCheck()) return;
		int ob = UtilsRewrite.uInventory.itemSlot(Item.getItemFromBlock(Blocks.OBSIDIAN));
		if (ob == -1) return;
		double range = rangeCheck.getValue();
		if (range > 0.2) {
			if (Targets.getTargetByRange(range) == null) {
				return;
			}
		}
		List<BlockPos> placeOn = findNeedPlace();
		if (placeOn.isEmpty()) return;
		if (delay.getValue() == 0) {
			for (BlockPos pos : placeOn) {
				if (UtilsRewrite.uBlock.getState(pos) instanceof BlockAir) {
					if (swing.getValue()) {
						UtilsRewrite.uBlock.placeUseSlot(pos, null, true, true, ob);
					} else {
						UtilsRewrite.uBlock.placeUseSlotNoSwing(pos, null, true, true, ob);
					}
				}
			}
		} else {
			place.startPlace(placeOn, delay.getValue(), ob);
		}
	}

	public List<BlockPos> findNeedPlace() {
		BlockPos ppos = mc.player.getPosition();
		boolean onburrow = UtilsRewrite.uBlock.getState(ppos) instanceof BlockObsidian;
		List<Pair<BlockPos, BlockPos>> offsets = new ArrayList<>(); //使用List<Pair>来存储偏移量
		for (PistonOffset p : pistonOffsets) {
			offsets.add(new Pair<>(ppos.add(p.face), ppos.add(p.opposite)));
		}
		List<BlockPos> needPlace = new ArrayList<>();
		for (Pair<BlockPos, BlockPos> data : offsets) {
			if (UtilsRewrite.uBlock.getState(data.getKey()) instanceof BlockPistonBase) {
				needPlace.add(data.getValue());
				if (onburrow) { //antipull
					antiPull.getValue().handleAntiPull(needPlace, data.getKey(), ppos); //使用枚举类的抽象方法
				}
			} else {//find rs
				BlockPos piston = data.getKey();
				for (EnumFacing facing : EnumFacing.values()) {
					if (facing.getDirectionVec().equals(data.getValue().subtract(piston))) continue; //跳过活塞面对的方向
					BlockPos redstone = piston.add(facing.getDirectionVec());
					if (UtilsRewrite.uBlock.getBlock(redstone) == Blocks.REDSTONE_BLOCK) { //找到红石块
						if (antiRSFirst.getValue()) { //将判断放在这里
							needPlace.add(data.getValue());
							if (onburrow) { //antipull
								antiPull.getValue().handleAntiPull(needPlace, data.getKey(), ppos); //使用枚举类的抽象方法
							}
						}
						break;
					}
				}
			}
		}
		return needPlace;
	}

	enum APMode {
		TrapHead {
			@Override
			public void handleAntiPull(List<BlockPos> needPlace, BlockPos piston, BlockPos ppos) {
				needPlace.add(ppos.up(2));
			}
		},
		PistonUpPlace {
			@Override
			public void handleAntiPull(List<BlockPos> needPlace, BlockPos piston, BlockPos ppos) {
				needPlace.add(piston.up());
			}
		},
		Smart {
			@Override
			public void handleAntiPull(List<BlockPos> needPlace, BlockPos piston, BlockPos ppos) {
				if (UtilsRewrite.uBlock.getBlock(piston.up()) == Blocks.REDSTONE_BLOCK) {
					needPlace.add(ppos.up(2));
				} else {
					needPlace.add(piston.up());
				}
			}
		},
		NO {
			@Override
			public void handleAntiPull(List<BlockPos> needPlace, BlockPos piston, BlockPos ppos) {
				// do nothing
			}
		};

		public abstract void handleAntiPull(List<BlockPos> needPlace, BlockPos piston, BlockPos ppos);
	}

	static class PistonOffset {
		public Vec3i face;
		public Vec3i opposite;

		public PistonOffset(Vec3i face, Vec3i opposite) {
			this.face = face; //直接使用参数本身来赋值给字段
			this.opposite = opposite; //直接使用参数本身来赋值给字段
		}
	}


}
