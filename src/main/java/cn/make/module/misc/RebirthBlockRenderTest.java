package cn.make.module.misc;

import cn.make.util.renderutils;
import chad.phobos.api.events.block.BlockEvent;
import chad.phobos.api.events.render.Render3DEvent;
import chad.phobos.api.center.Module;
import chad.phobos.api.setting.Setting;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

public class RebirthBlockRenderTest extends Module {
	public RebirthBlockRenderTest() {
		super("RebirthBlockRenderTest", "description", Category.MISC);
	}
	public Setting<renderutils.DrawMode> drawMode = rother("DrawMode", renderutils.DrawMode.None);
	public Setting<Integer> speed = rinte("Speed", 1, 1, 10);
	public BlockPos dmgPos = null;
	public float progress = 0.0f;

	@Override
	public void onRender3D(Render3DEvent event) {
		if (dmgPos != null) {
			renderutils.rebirthBlockAnimation(dmgPos, progress, new Color(240,240,240), 100, 60, drawMode.getValue(), true, true);
		}
	}
	@SubscribeEvent
	public void onDmgBlock(BlockEvent event) {
		dmgPos = event.pos;
		progress = 0.0f;
	}
	@Override
	public void onTick() {
		float s = (0.01f * speed.getValue());
		if (dmgPos == null) {
			progress = 0.0f;
			return;
		}
		if (progress < (1.0 - s)) {
			progress = progress + s;
		} else {
			progress = 1.0f;
		}
	}
}
