package cn.make.module.movement;

import chad.phobos.api.center.Module;
import chad.phobos.api.events.player.MoveEvent;
import chad.phobos.api.events.player.UpdateWalkingPlayerEvent;
import chad.phobos.api.setting.Setting;
import net.minecraft.init.MobEffects;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Objects;

public class StrafeR
extends Module {
    public static StrafeR INSTANCE = new StrafeR();
    public final Setting<Mode> mode = register(new Setting<>("Mode", Mode.Normal));
    private final Setting<Boolean> jump = register(new Setting<>("Jump", false));
    private final Setting<Double> jumpMotion = register(new Setting<>("JumpMotion", 0.40123128, 0.1, 1.0));
    private final Setting<Float> multiplier = register(new Setting<>("Factor", 1.67f, 0.0f, 3.0f));
    private final Setting<Float> plier = register(new Setting<>("Factor+", 2.149f, 0.0f, 3.0f));
    private final Setting<Float> Dist = register(new Setting<>("Dist", 0.6896f, 0.1f, 1.0f));
    private final Setting<Float> multiDist = register(new Setting<>("Dist+", 0.795f, 0.1f, 1.0f));
    private final Setting<Float> SPEEDY = register(new Setting<>("SpeedY1", 730.0f, 500.0f, 800.0f));
    private final Setting<Float> SPEEDH = register(new Setting<>("SpeedH1", 159.0f, 100.0f, 300.0f));
    private final Setting<Float> StrafeH = register(new Setting<>("SpeedH2", 0.993f, 0.1f, 1.0f));
    private final Setting<Float> StrafeY = register(new Setting<>("SpeedY2", 0.99f, 0.1f, 1.2f));
    int stage;
    private double lastDist;
    private double moveSpeed;

    public StrafeR() {
        super("StrafeRE", "Modifies sprinting", Category.MOVEMENT);
        INSTANCE = this;
    }

    @Override
    public String getInfo() {
        return this.mode.getValue().name();
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayerEvent(UpdateWalkingPlayerEvent event) {
        if (StrafeR.fullNullCheck()) {
            return;
        }
        this.lastDist = Math.sqrt((StrafeR.mc.player.posX - StrafeR.mc.player.prevPosX) * (StrafeR.mc.player.posX - StrafeR.mc.player.prevPosX) + (StrafeR.mc.player.posZ - StrafeR.mc.player.prevPosZ) * (StrafeR.mc.player.posZ - StrafeR.mc.player.prevPosZ));
    }

    @SubscribeEvent
    public void onStrafe(MoveEvent event) {
        if (StrafeR.fullNullCheck()) {
            return;
        }
        if (HoleSnap.INSTANCE.isOn()) {
            return;
        }
        if (!StrafeR.mc.player.isInWater() && !StrafeR.mc.player.isInLava()) {
            if (StrafeR.mc.player.onGround) {
                this.stage = 2;
            }
            if (this.stage == 0) {
                ++this.stage;
                this.lastDist = 0.0;
            } else if (this.stage == 2) {
                double motionY = this.jumpMotion.getValue();
                if (StrafeR.mc.player.onGround && this.jump.getValue() || StrafeR.mc.gameSettings.keyBindJump.isKeyDown()) {
                    if (StrafeR.mc.player.isPotionActive(MobEffects.JUMP_BOOST)) {
                        motionY += (float)(Objects.requireNonNull(StrafeR.mc.player.getActivePotionEffect(MobEffects.JUMP_BOOST)).getAmplifier() + 1) * 0.1f;
                    }
                    StrafeR.mc.player.motionY = motionY;
                    event.setY(StrafeR.mc.player.motionY);
                    this.moveSpeed *= this.mode.getValue() == Mode.Normal ? this.multiplier.getValue() : this.plier.getValue();
                }
            } else if (this.stage == 3) {
                this.moveSpeed = this.lastDist - (double) (this.mode.getValue() == Mode.Normal ? this.Dist.getValue() : this.multiDist.getValue()) * (this.lastDist - this.getBaseMoveSpeed());
            } else {
                if ((StrafeR.mc.world.getCollisionBoxes(StrafeR.mc.player, StrafeR.mc.player.getEntityBoundingBox().offset(0.0, StrafeR.mc.player.motionY, 0.0)).size() > 0 || StrafeR.mc.player.collidedVertically) && this.stage > 0) {
                    this.stage = StrafeR.mc.player.moveForward != 0.0f || StrafeR.mc.player.moveStrafing != 0.0f ? 1 : 0;
                }
                this.moveSpeed = this.lastDist - this.lastDist / (double) (this.mode.getValue() == Mode.Normal ? this.SPEEDY.getValue() : this.SPEEDH.getValue());
            }
            this.moveSpeed = !StrafeR.mc.gameSettings.keyBindJump.isKeyDown() && StrafeR.mc.player.onGround ? this.getBaseMoveSpeed() : Math.max(this.moveSpeed, this.getBaseMoveSpeed());
            double n = StrafeR.mc.player.movementInput.moveForward;
            double n2 = StrafeR.mc.player.movementInput.moveStrafe;
            double n3 = StrafeR.mc.player.rotationYaw;
            if (n == 0.0 && n2 == 0.0) {
                event.setX(0.0);
                event.setZ(0.0);
            } else if (n != 0.0 && n2 != 0.0) {
                n *= Math.sin(0.7853981633974483);
                n2 *= Math.cos(0.7853981633974483);
            }
            double n4 = this.mode.getValue() == Mode.Normal ? (double) this.StrafeH.getValue() : (double) this.StrafeY.getValue();
            event.setX((n * this.moveSpeed * -Math.sin(Math.toRadians(n3)) + n2 * this.moveSpeed * Math.cos(Math.toRadians(n3))) * n4);
            event.setZ((n * this.moveSpeed * Math.cos(Math.toRadians(n3)) - n2 * this.moveSpeed * -Math.sin(Math.toRadians(n3))) * n4);
            ++this.stage;
            event.setCanceled(false);
        }
    }

    public double getBaseMoveSpeed() {
        double n = 0.2873;
        if (StrafeR.mc.player.isPotionActive(MobEffects.SPEED)) {
            n *= 1.0 + 0.2 * (double)(Objects.requireNonNull(StrafeR.mc.player.getActivePotionEffect(MobEffects.SPEED)).getAmplifier() + 1);
        }
        return n;
    }

    public static enum Mode {
        Normal,
        Strict

    }
}

