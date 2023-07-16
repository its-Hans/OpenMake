package cn.make.module.movement;


import chad.phobos.api.center.Module;
import chad.phobos.api.events.player.MoveEvent;
import chad.phobos.api.events.player.UpdateWalkingPlayerEvent;
import chad.phobos.api.setting.Setting;
import net.minecraft.init.MobEffects;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Objects;

public class StrafeM
	extends Module {
	public Setting<Mode> mode = rother("Mode", Mode.NORMAL);
	private static StrafeM INSTANCE = new StrafeM();
	private double lastDist;
	private double moveSpeed;
	int stage;

	public StrafeM() {
		super("StrafeM", "mobility more flexible.", Category.MOVEMENT, true, false, false);
		INSTANCE = this;
	}


	public static StrafeM getInstance() {
		if (INSTANCE != null) return INSTANCE;
		INSTANCE = new StrafeM();
		return INSTANCE;
	}

	@SubscribeEvent
	public void onUpdateWalkingPlayerEvent(UpdateWalkingPlayerEvent event) {
		if (event.getStage() == 1 && StrafeM.fullNullCheck()) {
			return;
		}
		this.lastDist = Math.sqrt((StrafeM.mc.player.posX - StrafeM.mc.player.prevPosX) * (StrafeM.mc.player.posX - StrafeM.mc.player.prevPosX) + (StrafeM.mc.player.posZ - StrafeM.mc.player.prevPosZ) * (StrafeM.mc.player.posZ - StrafeM.mc.player.prevPosZ));
	}

	@SubscribeEvent
	public void onStrafe(MoveEvent event) {
		if (StrafeM.mc.player.onGround) {
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
				break;
			}
			case 3: {
				this.moveSpeed = this.lastDist - (this.mode.getValue() == Mode.NORMAL ? 0.6896 : 0.795) * (this.lastDist - this.getBaseMoveSpeed());
				break;
			}
			default: {
				if ((StrafeM.mc.world.getCollisionBoxes(StrafeM.mc.player, StrafeM.mc.player.getEntityBoundingBox().offset(0.0, StrafeM.mc.player.motionY, 0.0)).size() > 0 || StrafeM.mc.player.collidedVertically) && this.stage > 0) {
					this.stage = StrafeM.mc.player.moveForward != 0.0f || StrafeM.mc.player.moveStrafing != 0.0f ? 1 : 0;
				}
				this.moveSpeed = this.lastDist - this.lastDist / (this.mode.getValue() == Mode.NORMAL ? 730.0 : 159.0);
			}
		}
		StrafeM.mc.gameSettings.keyBindJump.isKeyDown();
		this.moveSpeed = StrafeM.mc.player.onGround ? this.getBaseMoveSpeed() : Math.max(this.moveSpeed, this.getBaseMoveSpeed());
		double n = StrafeM.mc.player.movementInput.moveForward;
		double n2 = StrafeM.mc.player.movementInput.moveStrafe;
		double n3 = StrafeM.mc.player.rotationYaw;
		if (n == 0.0 && n2 == 0.0) {
			event.setX(0.0);
			event.setZ(0.0);
		} else if (n != 0.0 && n2 != 0.0) {
			n *= Math.sin(0.7853981633974483);
			n2 *= Math.cos(0.7853981633974483);
		}
		double n4 = this.mode.getValue() == Mode.NORMAL ? 0.993 : 0.99;
		event.setX((n * this.moveSpeed * -Math.sin(Math.toRadians(n3)) + n2 * this.moveSpeed * Math.cos(Math.toRadians(n3))) * n4);
		event.setZ((n * this.moveSpeed * Math.cos(Math.toRadians(n3)) - n2 * this.moveSpeed * -Math.sin(Math.toRadians(n3))) * n4);
		++this.stage;
		event.setCanceled(true);
	}

	public double getBaseMoveSpeed() {
		double n = 0.2873;
		if (!StrafeM.mc.player.isPotionActive(MobEffects.SPEED)) return n;
		n *= 1.0 + 0.2 * (double) (Objects.requireNonNull(StrafeM.mc.player.getActivePotionEffect(MobEffects.SPEED)).getAmplifier() + 1);
		return n;
	}

	@Override
	public String getDisplayInfo() {
		return this.mode.currentEnumName();
	}

	public enum Mode {
		NORMAL,
		Strict

	}
}


