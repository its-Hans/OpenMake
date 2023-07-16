package cn.make.module.player;


import chad.phobos.Client;
import chad.phobos.api.center.Module;
import chad.phobos.api.setting.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBow;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;


public class BowAim extends Module {


	private final Setting<Float> range = rfloa("Range", 60.0f, 0.0f, 200f);
	private final Setting<Float> fov = rfloa("fov", 60.0f, 0.0f, 180f);
	private final Setting<Boolean> ignoreWalls = rbool("IgnoreWalls", false);
	private final Setting<Boolean> noVertical = rbool("NoVertical", false);
	Entity target;

    public BowAim() {
		super("BowAimBot", "AimBot", Category.COMBAT);
	}

	@Override
	public void onUpdate() {
		if (
            mc.player.getHeldItemMainhand().getItem() instanceof ItemBow && mc.player.isHandActive()
                && mc.player.getItemInUseMaxCount() > 0
        ) {
			target = findTarget();
			if (target == null) return;

			double xPos = target.posX;
			double yPos = target.posY;
			double zPos = target.posZ;
            double upMultiplier = (mc.player.getDistance(target) / 320) * 1.1;
            Vec3d predict = new Vec3d(xPos, yPos + upMultiplier, zPos);
			float[] rotation = lookAtPredict(predict);

			mc.player.rotationYaw = rotation[0];
			if (noVertical.getValue()) mc.player.rotationPitch = rotation[1];
			target = null;
		}
	}

	private float[] lookAtPredict(Vec3d vec) {
		double diffX = vec.x + 0.5 - mc.player.posX;
		double diffY = vec.y + 0.5 - (mc.player.posY + mc.player.getEyeHeight());
		double diffZ = vec.z + 0.5 - mc.player.posZ;
		double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);
		float yaw = (float) (Math.atan2(diffZ, diffX) * 180.0D / Math.PI) - 90.0F;
		float pitch = (float) -(Math.atan2(diffY, dist) * 180.0D / Math.PI);
		return new float[] {
            mc.player.rotationYaw + MathHelper.wrapDegrees(yaw - mc.player.rotationYaw),
            mc.player.rotationPitch + MathHelper.wrapDegrees(pitch - mc.player.rotationPitch)
        };
	}

	public EntityPlayer findTarget() {
		EntityPlayer target = null;
		double distance = range.getValue() * range.getValue();
		for (EntityPlayer entity : mc.world.playerEntities) {
			if (entity == mc.player) {
				continue;
			}
			if (Client.friendManager.isFriend(entity)) {
				continue;
			}
			if (eu.canEntityBeSeen(entity) && !ignoreWalls.getValue()) {
				continue;
			}

			if (!eu.canSeeEntityAtFov(entity, fov.getValue())) {
				continue;
			}
			if (mc.player.getDistanceSq(entity) <= distance) {
				target = entity;
				distance = mc.player.getDistanceSq(entity);
			}
		}
		return target;
	}

	@Override
	public String getDisplayInfo() {

		if (BowAim.mc.player.getHeldItemMainhand().getItem() instanceof ItemBow && target != null) {
			return target.getName();
		} else {
			return null;
		}
	}

	protected static class eu {
		public static double angleDifference(final float oldYaw, final float newYaw) {
			float yaw = Math.abs(oldYaw - newYaw) % 360.0f;
			if (yaw > 180.0f) {
				yaw = 360.0f - yaw;
			}
			return yaw;
		}

		public static boolean canSeeEntityAtFov(final Entity entityLiving, final float scope) {
			final double diffX = entityLiving.posX - mc.player.posX;
			final double diffZ = entityLiving.posZ - mc.player.posZ;
			final float yaw = (float) (Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0);
			final double difference = angleDifference(yaw, mc.player.rotationYaw);
			return difference <= scope;
		}

		public static boolean canEntityBeSeen(Entity entityIn) {
			return mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posX + (double) mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(entityIn.posX, entityIn.posY + (double) entityIn.getEyeHeight(), entityIn.posZ), false, true, false) == null;
		}
	}
}
