/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.EnumFacing
 *  net.minecraft.util.math.BlockPos
 *  net.minecraftforge.fml.common.eventhandler.Cancelable
 */
package chad.phobos.api.events.block;

import chad.phobos.api.center.EventStage;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class SelfDamageBlockEvent
extends EventStage {
    public BlockPos pos;
    public EnumFacing facing;

    protected SelfDamageBlockEvent(int stage, BlockPos pos, EnumFacing facing) {
        super(stage);
        this.pos = pos;
        this.facing = facing;
    }

    public final BlockPos getPos() {
        return this.pos;
    }
    public static class port1 extends SelfDamageBlockEvent {
        public port1(int stage, BlockPos pos, EnumFacing facing) {
            super(stage, pos, facing);
        }
    }
    public static class port2 extends SelfDamageBlockEvent {
        public port2(int stage, BlockPos pos, EnumFacing facing) {
            super(stage, pos, facing);
        }
    }
    public static class port3 extends SelfDamageBlockEvent {
        public port3(int stage, BlockPos pos, EnumFacing facing) {
            super(stage, pos, facing);
        }
    }
    public static class port4 extends SelfDamageBlockEvent {
        public port4(int stage, BlockPos pos, EnumFacing facing) {
            super(stage, pos, facing);
        }
    }
}

