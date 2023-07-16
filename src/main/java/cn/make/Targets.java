package cn.make;

import cn.make.util.skid.EntityUtil;
import chad.phobos.Client;
import chad.phobos.api.center.Module;
import chad.phobos.api.setting.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Comparator;

public class Targets extends Module {
	public Setting<Double> rangeset = rdoub("Range", 5.4, 1.0, 10.0);
	public Setting<Boolean> singleMode = rbool("SingleMode", false);
	static EntityPlayer cache;
	public Targets() {
		super("TargetManager", "Manage COMBAT!!", Category.CLIENT, true, false, true);
		cache = null;
	}
	@Override
	public void onEnable() {
		this.disable();
	}

	@Override
	public void onDisable() {
		cache = null;
	}

	public static Targets _this() {
		return Client.moduleManager.getModuleByClass(Targets.class);
	}
	public static EntityPlayer getTarget() {
		if (mc.world == null) {
			cache = null;
			return null;
		}
		double range = _this().rangeset.getValue();
		if (_this().needRefresh()) {
			cache = mc.world.getPlayers(EntityPlayer.class, _this()::canTarget)
				.stream()
				.filter(player -> mc.player.getDistanceSqToCenter(player.getPosition()) < range * range)
				.min(Comparator.comparingDouble(player -> mc.player.getDistanceSqToCenter(player.getPosition())))
				.orElse(null);
		}
		return cache;
	}
	public static EntityPlayer getTargetByRange(final Double range) {
		if (mc.world == null) {
			return null;
		}
		return mc.world.getPlayers(EntityPlayer.class, _this()::canTarget)
				.stream()
				.filter(player -> mc.player.getDistanceSqToCenter(player.getPosition()) < range * range)
				.min(Comparator.comparingDouble(player -> mc.player.getDistanceSqToCenter(player.getPosition())))
				.orElse(null);
	}

	public boolean canTarget(Entity entity) {
		if (entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) entity;
			return !EntityUtil.isDead(player) //not dead
				&& !player.equals(mc.player) //not self
				&& !Client.friendManager.isFriend(player); //not friend
		}
		return false;
	}
	public boolean needRefresh() {
		if (cache == null) return true;
		if (!singleMode.getValue()) return true;
		double range = rangeset.getValue();
		return mc.player.getDistanceSqToCenter(cache.getPosition()) < range * range;
	}
}
