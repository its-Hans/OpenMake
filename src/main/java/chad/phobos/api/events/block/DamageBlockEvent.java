package chad.phobos.api.events.block;

import chad.phobos.api.center.EventStage;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class DamageBlockEvent
extends EventStage {
    final BlockPos pos;
    final int progress;
    final int breakerId;

    public DamageBlockEvent(BlockPos pos, int progress, int breakerId) {
        this.pos = pos;
        this.progress = progress;
        this.breakerId = breakerId;
    }

    public BlockPos getPosition() {
        return this.pos;
    }

    public int getProgress() {
        return this.progress;
    }

    public int getBreakerId() {
        return this.breakerId;
    }
}

