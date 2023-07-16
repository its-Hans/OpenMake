package chad.phobos.api.events.player;

import chad.phobos.api.center.EventStage;

public class UpdateWalkingPlayerEvent
        extends EventStage {
    public UpdateWalkingPlayerEvent(int stage) {
        super(stage);
    }
}

