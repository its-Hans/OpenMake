package cn.make.module.misc;

import chad.phobos.api.events.network.PacketEvent;
import chad.phobos.api.center.Module;
import net.minecraft.network.play.server.SPacketUnloadChunk;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class FarRender extends Module {
	public FarRender() {
		super("FarRender", "Cancel SPacketUnloadChunk", Category.MISC);
	}
	@SubscribeEvent
	public void onPacketSend(PacketEvent.Send event) {
		if (event.getPacket() instanceof SPacketUnloadChunk) {
			event.setCanceled(true);
		}
	}
}
