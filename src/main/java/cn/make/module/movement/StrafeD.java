package cn.make.module.movement;

import chad.phobos.api.center.Module;
import chad.phobos.api.events.client.ClientEvent;
import chad.phobos.api.events.player.MoveEvent;
import chad.phobos.api.events.player.UpdateWalkingPlayerEvent;
import chad.phobos.api.setting.Setting;
import cn.make.util.skid.EntityUtil;
import net.minecraft.init.MobEffects;
import net.minecraft.util.MovementInput;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Objects;

import static java.lang.Math.*;

public class StrafeD
        extends Module {
    private static StrafeD INSTANCE;
    private final Setting<Mode> mode = rother("Mode", Mode.INSTANT);
    private final Setting<Boolean> limiter = rbool("SetGround", true, v -> this.mode.getValue() == Mode.NCP);
    private final Setting<Float> speed = rfloa("Speed", 2.0f, 1.0f, 5.0f, v -> this.mode.getValue() == Mode.NCP);
    public Setting<Boolean> strafeJump = rbool("Jump", false, v -> this.mode.getValue() == Mode.INSTANT);
    private int stage = 1;
    private double moveSpeed;
    private double lastDist;

    public StrafeD() {
        super("StrafeD", "AirControl etc.", Category.MOVEMENT, true, false, false);
        INSTANCE = this;
    }

    public static StrafeD getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new StrafeD();
        }
        return INSTANCE;
    }

    public static double getBaseMoveSpeed() {
        double baseSpeed = 0.272;
        if (StrafeD.mc.player.isPotionActive(MobEffects.SPEED)) {
            int amplifier = Objects.requireNonNull(StrafeD.mc.player.getActivePotionEffect(MobEffects.SPEED)).getAmplifier();
            baseSpeed *= 1.0 + 0.2 * (double) amplifier;
        }
        return baseSpeed;
    }

    @Override
    public void onEnable() {
        this.moveSpeed = StrafeD.getBaseMoveSpeed();
    }

    @Override
    public void onDisable() {
        this.moveSpeed = 0.0;
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        if (event.getStage() == 0) {
            this.lastDist = sqrt((StrafeD.mc.player.posX - StrafeD.mc.player.prevPosX) * (StrafeD.mc.player.posX - StrafeD.mc.player.prevPosX) + (StrafeD.mc.player.posZ - StrafeD.mc.player.prevPosZ) * (StrafeD.mc.player.posZ - StrafeD.mc.player.prevPosZ));
        }
    }

    @SubscribeEvent
    public void onMove(MoveEvent event) {
        if (this.mode.getValue() == Mode.NCP) {
            this.doNCP(event);
        }
    }

    private void doNCP(MoveEvent event) {
        if (this.shouldReturn()) {
            return;
        }
        if (!this.limiter.getValue() && StrafeD.mc.player.onGround) {
            this.stage = 2;
        }
        switch (this.stage) {
            case 0: {
                ++this.stage;
                this.lastDist = 0.0;
                break;
            }
            case 2: {
                double motionY = 0.40123128;
                if (StrafeD.mc.player.moveForward == 0.0f && StrafeD.mc.player.moveStrafing == 0.0f || !StrafeD.mc.player.onGround)
                    break;
                if (StrafeD.mc.player.isPotionActive(MobEffects.JUMP_BOOST)) {
                    motionY += (float) (Objects.requireNonNull(StrafeD.mc.player.getActivePotionEffect(MobEffects.JUMP_BOOST)).getAmplifier() + 1) * 0.1f;
                }
                StrafeD.mc.player.motionY = motionY;
                event.setY(StrafeD.mc.player.motionY);
                this.moveSpeed *= this.speed.getValue();
                break;
            }
            case 3: {
                this.moveSpeed = this.lastDist - 0.76 * (this.lastDist - StrafeD.getBaseMoveSpeed());
                break;
            }
            default: {
                if (StrafeD.mc.world.getCollisionBoxes(StrafeD.mc.player, StrafeD.mc.player.getEntityBoundingBox().offset(0.0, StrafeD.mc.player.motionY, 0.0)).size() > 0 || StrafeD.mc.player.collidedVertically && this.stage > 0) {
                    this.stage = StrafeD.mc.player.moveForward != 0.0f || StrafeD.mc.player.moveStrafing != 0.0f ? 1 : 0;
                }
                this.moveSpeed = this.lastDist - this.lastDist / 159.0;
            }
        }
        this.moveSpeed = max(this.moveSpeed, StrafeD.getBaseMoveSpeed());
        double forward = StrafeD.mc.player.movementInput.moveForward;
        double strafe = StrafeD.mc.player.movementInput.moveStrafe;
        double yaw = StrafeD.mc.player.rotationYaw;
        if (forward == 0.0 && strafe == 0.0) {
            event.setX(0.0);
            event.setZ(0.0);
        } else if (forward != 0.0 && strafe != 0.0) {
            forward *= sin(0.7853981633974483);
            strafe *= cos(0.7853981633974483);
        }
        event.setX((forward * this.moveSpeed * -sin(toRadians(yaw)) + strafe * this.moveSpeed * cos(toRadians(yaw))) * 0.99);
        event.setZ((forward * this.moveSpeed * cos(toRadians(yaw)) - strafe * this.moveSpeed * -sin(toRadians(yaw))) * 0.99);
        ++this.stage;
    }

    @SubscribeEvent
    public void onMode(MoveEvent event) {
        if (!(this.shouldReturn() || event.getStage() != 0 || this.mode.getValue() != Mode.INSTANT || StrafeD.nullCheck() || StrafeD.mc.player.isSneaking() || StrafeD.mc.player.isInWater() || StrafeD.mc.player.isInLava() || StrafeD.mc.player.movementInput.moveForward == 0.0f && StrafeD.mc.player.movementInput.moveStrafe == 0.0f)) {
            if (StrafeD.mc.player.onGround && this.strafeJump.getValue()) {
                StrafeD.mc.player.motionY = 0.4;
                event.setY(0.4);
            }
            MovementInput movementInput = StrafeD.mc.player.movementInput;
            float moveForward = movementInput.moveForward;
            float moveStrafe = movementInput.moveStrafe;
            float rotationYaw = StrafeD.mc.player.rotationYaw;
            if ((double) moveForward == 0.0 && (double) moveStrafe == 0.0) {
                event.setX(0.0);
                event.setZ(0.0);
            } else {
                if ((double) moveForward != 0.0) {
                    float f;
                    if ((double) moveStrafe > 0.0) {
                        rotationYaw += (float) ((double) moveForward > 0.0 ? -45 : 45);
                    } else if ((double) moveStrafe < 0.0) {
                        rotationYaw += (float) ((double) moveForward > 0.0 ? 45 : -45);
                    }
                    moveStrafe = 0.0f;
                    float f2 = moveForward == 0.0f ? moveForward : (f = (moveForward = (double) moveForward > 0.0 ? 1.0f : -1.0f));
                }
                moveStrafe = moveStrafe == 0.0f ? moveStrafe : ((double) moveStrafe > 0.0 ? 1.0f : -1.0f);
                event.setX((double) moveForward * EntityUtil.getMaxSpeed() * cos(toRadians(rotationYaw + 90.0f)) + (double) moveStrafe * EntityUtil.getMaxSpeed() * sin(toRadians(rotationYaw + 90.0f)));
                event.setZ((double) moveForward * EntityUtil.getMaxSpeed() * sin(toRadians(rotationYaw + 90.0f)) - (double) moveStrafe * EntityUtil.getMaxSpeed() * cos(toRadians(rotationYaw + 90.0f)));
            }
        }
    }

    @SubscribeEvent
    public void onSettingChange(ClientEvent event) {
        if (event.getStage() == 2 && event.getSetting().equals(this.mode) && this.mode.getPlannedValue() == Mode.INSTANT) {
            StrafeD.mc.player.motionY = -0.1;
        }
    }

    private boolean shouldReturn() {
        return false;
    }

    public enum Mode {
        NCP,
        INSTANT

    }
}

