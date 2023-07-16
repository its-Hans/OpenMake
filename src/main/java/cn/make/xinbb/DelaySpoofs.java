package cn.make.xinbb;

import chad.phobos.api.center.Util;
import cn.make.util.PacketCenter;
import cn.make.util.skid.Timer;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.concurrent.LinkedBlockingDeque;

public class DelaySpoofs implements Util {
	final LinkedBlockingDeque<SPOOF> list;
	final Timer timer;
	final Integer delay;
	final CPacketPlayerDigging releasePacket;
	protected boolean start;

	public DelaySpoofs(LinkedBlockingDeque<SPOOF> spoofs, int delay, CPacketPlayerDigging packet) {
		this.list = spoofs;
		this.timer = new Timer();
		this.delay = delay;
		this.releasePacket = packet;
		start = true;
		MinecraftForge.EVENT_BUS.register(this);
	}
	public boolean getStarted() {
		return start;
	}

	@SubscribeEvent
	public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
		if (!getStarted()) {
			return;
		}
		if (list.isEmpty()) {
			start = false;
			MinecraftForge.EVENT_BUS.unregister(this);
			PacketCenter.sP(this.releasePacket);
			return;
		}

		while (!timer.passedMs(delay)) {continue;}
		try {
			spoof(list.take());
			timer.reset();
		} catch (Exception ignore) {}

	}
	private synchronized void spoof(final SPOOF sp) {
		sp.send1();
		sp.send2();
	}
	static class SPOOF {
		public Boolean[] ground;
		public Vec3d[] spoofXYZ;
		public SPOOF(boolean Ground1, Vec3d spoofxyz1, boolean Ground2, Vec3d spoofxyz2) {
			this.ground = new Boolean[] { Ground1, Ground2 };
			this.spoofXYZ = new Vec3d[] { spoofxyz1, spoofxyz2 };
		}
		public void send1() {
			Vec3d s = spoofXYZ[1];
			double x = s.x;
			double y = s.y;
			double z = s.z;
			mc.player.connection.sendPacket(new CPacketPlayer.Position(x,y,z, ground[1]));
		}
		public void send2() {
			Vec3d s = spoofXYZ[2];
			double x = s.x;
			double y = s.y;
			double z = s.z;
			mc.player.connection.sendPacket(new CPacketPlayer.Position(x,y,z, ground[2]));
		}
	}
}
