package chad.phobos.api.events.client;

import chad.phobos.api.center.EventStage;

public class EventUpdate extends EventStage {

    public float yaw;
    public float pitch;
    private boolean onGround;

    public EventUpdate(int stage, float yaw, float pitch, boolean onGround) {
        super(stage);
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }
}
