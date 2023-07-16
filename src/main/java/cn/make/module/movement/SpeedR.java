package cn.make.module.movement;

import chad.phobos.api.center.Module;
import chad.phobos.api.events.network.PacketEvent;
import chad.phobos.api.events.player.MoveEvent;
import chad.phobos.api.events.player.PushEvent;
import chad.phobos.api.events.player.UpdateWalkingPlayerEvent;
import chad.phobos.api.setting.Setting;
import cn.make.util.skid.RebirthUtil;
import cn.make.util.skid.Timer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Objects;

public class SpeedR
extends Module {
    public static SpeedR INSTANCE = new SpeedR();
    private final Setting<Boolean> jump = register(new Setting<>("Jump", false));
    private final Setting<Boolean> inWater = register(new Setting<>("InWater", false));
    private final Setting<Double> strafeSpeed = register(new Setting<>("StrafeSpeed", 278.5, 100.0, 1000.0));
    private final Setting<Boolean> explosions = register(new Setting<>("Explosions", true));
    private final Setting<Boolean> velocity = register(new Setting<>("Velocity", true));
    private final Setting<Float> multiplier = register(new Setting<>("H-Factor", 1.0f, 0.0f, 5.0f));
    private final Setting<Float> vertical = register(new Setting<>("V-Factor", 1.0f, 0.0f, 5.0f));
    private final Setting<Integer> coolDown = register(new Setting<>("CoolDown", 400, 0, 5000));
    private final Setting<Integer> pauseTime = register(new Setting<>("PauseTime", 400, 0, 1000));
    private final Setting<Boolean> directional = register(new Setting<>("Directional", false));
    private final Setting<Double> cap = register(new Setting<>("Cap", 10.0, 0.0, 10.0));
    private final Setting<Boolean> scaleCap = register(new Setting<>("ScaleCap", false));
    private final Setting<Boolean> slow = register(new Setting<>("Slowness", true));
    private final Setting<Boolean> modify = register(new Setting<>("Modify", false));
    private final Setting<Double> xzFactor = register(new Setting<>("XZ-Factor", 1.0, 0.0, 5.0, v -> this.modify.getValue()));
    private final Setting<Double> yFactor = register(new Setting<>("Y-Factor", 1.0, 0.0, 5.0, v -> this.modify.getValue()));
    private final Setting<Boolean> debug = register(new Setting<>("Debug", false));
    private final Timer expTimer = new Timer();
    private boolean stop;
    private double speed;
    private double distance;
    private int stage;
    private double lastExp;
    private boolean boost;

    public SpeedR() {
        super("SpeedRE", "3ar", Category.MOVEMENT);
        INSTANCE = this;
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public void onReceivePacket(PacketEvent.Receive event) {
        SPacketExplosion packet;
        BlockPos pos;
        if (SpeedR.fullNullCheck()) {
            return;
        }
        if (event.getPacket() instanceof SPacketEntityVelocity) {
            SPacketEntityVelocity packet2 = event.getPacket();
            if (packet2.getEntityID() == SpeedR.mc.player.getEntityId() && !this.directional.getValue() && this.velocity.getValue()) {
                double speed = Math.sqrt(packet2.getMotionX() * packet2.getMotionX() + packet2.getMotionZ() * packet2.getMotionZ()) / 8000.0;
                double d = this.lastExp = this.expTimer.passedMs(this.coolDown.getValue()) ? speed : speed - this.lastExp;
                if (this.lastExp > 0.0) {
                    if (this.debug.getValue()) {
                        notiMessage("boost");
                    }
                    this.expTimer.reset();
                    this.speed += this.lastExp * (double) this.multiplier.getValue();
                    this.distance += this.lastExp * (double) this.multiplier.getValue();
                    if (SpeedR.mc.player.motionY > 0.0 && this.vertical.getValue() != 0.0f) {
                        SpeedR.mc.player.motionY *= this.vertical.getValue();
                    }
                }
            }
        } else if (event.getPacket() instanceof SPacketPlayerPosLook) {
            this.distance = 0.0;
            this.speed = 0.0;
            this.stage = 4;
        } else if (event.getPacket() instanceof SPacketExplosion && this.explosions.getValue() && RebirthUtil.isMoving() && SpeedR.mc.player.getDistanceSq(pos = new BlockPos((packet = event.getPacket()).getX(), packet.getY(), packet.getZ())) < 100.0 && (!this.directional.getValue() || !RebirthUtil.isInMovementDirection(packet.getX(), packet.getY(), packet.getZ()))) {
            double speed = Math.sqrt(packet.getMotionX() * packet.getMotionX() + packet.getMotionZ() * packet.getMotionZ());
            double d = this.lastExp = this.expTimer.passedMs(this.coolDown.getValue()) ? speed : speed - this.lastExp;
            if (this.lastExp > 0.0) {
                if (this.debug.getValue()) {
                    notiMessage("boost");
                }
                this.expTimer.reset();
                this.speed += this.lastExp * (double) this.multiplier.getValue();
                this.distance += this.lastExp * (double) this.multiplier.getValue();
                if (SpeedR.mc.player.motionY > 0.0) {
                    SpeedR.mc.player.motionY *= this.vertical.getValue();
                }
            }
        }
    }

    @SubscribeEvent
    public void onPush(PushEvent event) {
        if (event.getStage() == 0 && event.entity.equals(SpeedR.mc.player)) {
            event.x = -event.x * 0.0;
            event.y = -event.y * 0.0;
            event.z = -event.z * 0.0;
        } else if (event.getStage() == 1) {
            event.setCanceled(true);
        } else if (event.getStage() == 2 && SpeedR.mc.player != null && SpeedR.mc.player.equals(event.entity)) {
            event.setCanceled(true);
        }
    }

    @Override
    public String getInfo() {
        return "3arthh4ck";
    }

    @Override
    public void onEnable() {
        this.speed = RebirthUtil.getSpeed();
        this.distance = RebirthUtil.getDistance2D();
        this.stage = 4;
    }

    private boolean isFlying(EntityPlayer player) {
        return player.isElytraFlying() || player.capabilities.isFlying;
    }

    @SubscribeEvent
    public void Update(UpdateWalkingPlayerEvent event) {
        if (this.expTimer.passedMs(this.pauseTime.getValue())) {
            this.distance = RebirthUtil.getDistance2D();
        }
    }

    @SubscribeEvent
    public void Move(MoveEvent event) {
        if (SpeedR.fullNullCheck()) {
            return;
        }
        if (this.isFlying(SpeedR.mc.player)) {
            return;
        }
        if (!this.inWater.getValue() && (RebirthUtil.inLiquid() || RebirthUtil.inLiquid(true)) || SpeedR.mc.player.isOnLadder() || SpeedR.mc.player.isEntityInsideOpaqueBlock()) {
            this.stop = true;
            return;
        }
        if (this.stop) {
            this.stop = false;
            return;
        }
        if (!RebirthUtil.isMoving()) {
            SpeedR.mc.player.motionX = 0.0;
            SpeedR.mc.player.motionZ = 0.0;
        }
        this.playerMove(event);
        if (this.modify.getValue()) {
            event.setX(event.getX() * this.xzFactor.getValue());
            event.setY(event.getY() * this.yFactor.getValue());
            event.setZ(event.getZ() * this.xzFactor.getValue());
        }
    }

    public double getCap() {
        int amplifier;
        double ret = this.cap.getValue();
        if (!this.scaleCap.getValue()) {
            return ret;
        }
        if (SpeedR.mc.player.isPotionActive(MobEffects.SPEED)) {
            amplifier = Objects.requireNonNull(SpeedR.mc.player.getActivePotionEffect(MobEffects.SPEED)).getAmplifier();
            ret *= 1.0 + 0.2 * (double)(amplifier + 1);
        }
        if (this.slow.getValue() && SpeedR.mc.player.isPotionActive(MobEffects.SLOWNESS)) {
            amplifier = Objects.requireNonNull(SpeedR.mc.player.getActivePotionEffect(MobEffects.SLOWNESS)).getAmplifier();
            ret /= 1.0 + 0.2 * (double)(amplifier + 1);
        }
        return ret;
    }

    public void playerMove(MoveEvent event) {
        if (!RebirthUtil.isMoving()) {
            return;
        }
        if (LongJump.INSTANCE.isOn()) {
            return;
        }
        if (this.stage == 1) {
            this.speed = 1.35 * RebirthUtil.getSpeed(this.slow.getValue(), this.strafeSpeed.getValue() / 1000.0) - 0.01;
        } else if (this.stage == 2) {
            if (this.jump.getValue() || SpeedR.mc.gameSettings.keyBindJump.isKeyDown()) {
                double yMotion;
                SpeedR.mc.player.motionY = yMotion = 0.3999 + RebirthUtil.getJumpSpeed();
                event.setY(yMotion);
                this.speed *= this.boost ? 1.6835 : 1.395;
            }
        } else if (this.stage == 3) {
            this.speed = this.distance - 0.66 * (this.distance - RebirthUtil.getSpeed(this.slow.getValue(), this.strafeSpeed.getValue() / 1000.0));
            this.boost = !this.boost;
        } else {
            if ((SpeedR.mc.world.getCollisionBoxes(null, SpeedR.mc.player.getEntityBoundingBox().offset(0.0, SpeedR.mc.player.motionY, 0.0)).size() > 0 || SpeedR.mc.player.collidedVertically) && this.stage > 0) {
                this.stage = RebirthUtil.isMoving() ? 1 : 0;
            }
            this.speed = this.distance - this.distance / 159.0;
        }
        this.speed = Math.min(this.speed, this.getCap());
        this.speed = Math.max(this.speed, RebirthUtil.getSpeed(this.slow.getValue(), this.strafeSpeed.getValue() / 1000.0));
        RebirthUtil.strafe(event, this.speed);
        ++this.stage;
    }
}

