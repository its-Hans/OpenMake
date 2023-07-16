package cn.make.util;

import chad.phobos.api.center.Feature;
import chad.phobos.api.center.Util;
import net.minecraft.block.BlockAir;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.List;

public class DelayBlockPlace implements Util {
	List<BlockPos> list = null;
	int tick = 0;
	int counter = 0;
	int switchto = 0;


	public void startPlace(List<BlockPos> placeOn, int delay, int slot) {
		list = placeOn;
		tick = delay;
		counter = 0;
		switchto = slot;
		MinecraftForge.EVENT_BUS.register(this);
	}
	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if (Feature.fullNullCheck()) {
			clear();
		}
		if (list == null || tick == 0 || switchto == 0) {
			return;
		}
		if (counter >= tick) {
			counter = 0;
			place();
		} else {
			counter++;
		}
	}
	public void clear() {
		list = null;
		tick = 0;
		counter = 0;
		switchto = 0;
		MinecraftForge.EVENT_BUS.unregister(this);
	}
	private void place() {
		for (BlockPos pos : list) {
			if (pos != null) {
				if (UtilsRewrite.uBlock.getBlock(pos) instanceof BlockAir)
					UtilsRewrite.uBlock.placeUseSlot(pos, null, true, true, switchto);
				list.remove(pos);
				break;
			}
		}
		if (list.isEmpty()) {
			clear();
		}
	}
}
