package cn.make.util;

import chad.phobos.api.utils.BlockUtil;
import chad.phobos.api.center.Util;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.math.BlockPos;

public class PacketCenter implements Util {
	public static void sP(Packet<?> packet) {
		PacketCenter.mc.player.connection.sendPacket(packet);
	}
	public static CPacketPlayerDigging getDigBlockPacket(BlockPos pos, CPacketPlayerDigging.Action action) {
		return new CPacketPlayerDigging(action, pos, BlockUtil.getRayTraceFacing(pos));
	}
}
