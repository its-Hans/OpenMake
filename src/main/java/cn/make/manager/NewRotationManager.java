package cn.make.manager;

import cn.make.util.skid.MathUtil;
import chad.phobos.api.center.Feature;
import chad.phobos.asm.accessors.IEntityPlayerSP;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class NewRotationManager
extends Feature {
    private float yaw;
    private float pitch;

    public float[] getAngle(Vec3d vec3d) {
        Vec3d vec3d2 = new Vec3d(NewRotationManager.mc.player.posX, NewRotationManager.mc.player.posY + (double) NewRotationManager.mc.player.getEyeHeight(), NewRotationManager.mc.player.posZ);
        double d = vec3d.x - vec3d2.x;
        double d2 = vec3d.y - vec3d2.y;
        double d3 = vec3d.z - vec3d2.z;
        double d4 = Math.sqrt(d * d + d3 * d3);
        float f = (float)Math.toDegrees(Math.atan2(d3, d)) - 90.0f;
        float f2 = (float)(-Math.toDegrees(Math.atan2(d2, d4)));
        return new float[]{NewRotationManager.mc.player.rotationYaw + MathHelper.wrapDegrees(f - NewRotationManager.mc.player.rotationYaw), NewRotationManager.mc.player.rotationPitch + MathHelper.wrapDegrees(f2 - NewRotationManager.mc.player.rotationPitch)};
    }

    public void lookAtVec3d(Vec3d vec3d) {
        float[] fArray = MathUtil.calcAngle(NewRotationManager.mc.player.getPositionEyes(mc.getRenderPartialTicks()), new Vec3d(vec3d.x, vec3d.y, vec3d.z));
        this.setPlayerRotations(fArray[0], fArray[1]);
    }

    public void lookAtVec3dPacket(Vec3d vec3d, boolean bl, boolean bl2) {
        float[] fArray = this.getAngle(vec3d);
        NewRotationManager.mc.player.connection.sendPacket(new CPacketPlayer.Rotation(fArray[0], bl ? (float)MathHelper.normalizeAngle((int)fArray[1], 360) : fArray[1], NewRotationManager.mc.player.onGround));
        if (bl2) {
            ((IEntityPlayerSP) NewRotationManager.mc.player).setLastReportedYaw(fArray[0]);
            ((IEntityPlayerSP) NewRotationManager.mc.player).setLastReportedPitch(fArray[1]);
        }
    }

    public void setPlayerRotations(float f, float f2) {
        NewRotationManager.mc.player.rotationYaw = f;
        NewRotationManager.mc.player.rotationYawHead = f;
        NewRotationManager.mc.player.rotationPitch = f2;
    }

    public void setRotation(float yaw, float pitch) {
        if (NewRotationManager.mc.player != null) {
            NewRotationManager.mc.player.rotationYawHead = yaw;
            NewRotationManager.mc.player.renderYawOffset = yaw;
        }
        setYaw(yaw);
        setPitch(pitch);
    }

    public void setYaw(float f) {
        this.yaw = f;
    }

    public void setPitch(float f) {
        this.pitch = f;
    }

    public void lookAtVec3d(double d, double d2, double d3) {
        Vec3d vec3d = new Vec3d(d, d2, d3);
        this.lookAtVec3d(vec3d);
    }

    public boolean isInFov(BlockPos blockPos) {
        int n = this.getYaw4D();
        if (n == 0 && (double)blockPos.getZ() - NewRotationManager.mc.player.getPositionVector().z < 0.0) {
            return false;
        }
        if (n == 1 && (double)blockPos.getX() - NewRotationManager.mc.player.getPositionVector().x > 0.0) {
            return false;
        }
        if (n == 2 && (double)blockPos.getZ() - NewRotationManager.mc.player.getPositionVector().z > 0.0) {
            return false;
        }
        return n != 3 || (double)blockPos.getX() - NewRotationManager.mc.player.getPositionVector().x >= 0.0;
    }

    public void restoreRotations() {
        NewRotationManager.mc.player.rotationYaw = this.yaw;
        NewRotationManager.mc.player.rotationYawHead = this.yaw;
        NewRotationManager.mc.player.rotationPitch = this.pitch;
    }

    public float getYaw() {
        return this.yaw;
    }

    public int getYaw4D() {
        return MathHelper.floor((double)(NewRotationManager.mc.player.rotationYaw * 4.0f / 360.0f) + 0.5) & 3;
    }

    public void lookAtVec3dPacket(Vec3d vec3d, boolean bl) {
        float[] fArray = this.getAngle(vec3d);
        NewRotationManager.mc.player.connection.sendPacket(new CPacketPlayer.Rotation(fArray[0], bl ? (float)MathHelper.normalizeAngle((int)fArray[1], 360) : fArray[1], NewRotationManager.mc.player.onGround));
    }

    public void lookAtPos(BlockPos blockPos) {
        float[] fArray = MathUtil.calcAngle(NewRotationManager.mc.player.getPositionEyes(mc.getRenderPartialTicks()), new Vec3d((float)blockPos.getX() + 0.5f, (float)blockPos.getY() + 0.5f, (float)blockPos.getZ() + 0.5f));
        this.setPlayerRotations(fArray[0], fArray[1]);
    }

    public float getPitch() {
        return this.pitch;
    }

    public void updateRotations() {
        this.yaw = NewRotationManager.mc.player.rotationYaw;
        this.pitch = NewRotationManager.mc.player.rotationPitch;
    }
}

