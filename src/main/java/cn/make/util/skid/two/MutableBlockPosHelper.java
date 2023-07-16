package cn.make.util.skid.two;

import net.minecraft.util.math.BlockPos;

public class MutableBlockPosHelper {
    public BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

    public static BlockPos.MutableBlockPos set(BlockPos.MutableBlockPos mutablePos, double x, double y, double z) {
        return mutablePos.setPos(x, y, z);
    }

    public static BlockPos.MutableBlockPos set(BlockPos.MutableBlockPos mutablePos, BlockPos pos) {
        return mutablePos.setPos(pos.getX(), pos.getY(), pos.getZ());
    }

    public static BlockPos.MutableBlockPos set(BlockPos.MutableBlockPos mutablePos, BlockPos pos, double x, double y, double z) {
        return mutablePos.setPos((double)pos.getX() + x, (double)pos.getY() + y, (double)pos.getZ() + z);
    }

    public static BlockPos.MutableBlockPos set(BlockPos.MutableBlockPos mutablePos, BlockPos pos, int x, int y, int z) {
        return mutablePos.setPos(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
    }

    public static BlockPos.MutableBlockPos set(BlockPos.MutableBlockPos mutablePos, int x, int y, int z) {
        return mutablePos.setPos(x, y, z);
    }

    public static BlockPos.MutableBlockPos setAndAdd(BlockPos.MutableBlockPos mutablePos, int x, int y, int z) {
        return mutablePos.setPos(mutablePos.getX() + x, mutablePos.getY() + y, mutablePos.getZ() + z);
    }

    public static BlockPos.MutableBlockPos setAndAdd(BlockPos.MutableBlockPos mutablePos, double x, double y, double z) {
        return mutablePos.setPos((double)mutablePos.getX() + x, (double)mutablePos.getY() + y, (double)mutablePos.getZ() + z);
    }

    public static BlockPos.MutableBlockPos setAndAdd(BlockPos.MutableBlockPos mutablePos, BlockPos pos) {
        return mutablePos.setPos(mutablePos.getX() + pos.getX(), mutablePos.getY() + pos.getY(), mutablePos.getZ() + pos.getZ());
    }

    public static BlockPos.MutableBlockPos setAndAdd(BlockPos.MutableBlockPos mutablePos, BlockPos pos, double x, double y, double z) {
        return mutablePos.setPos((double)(mutablePos.getX() + pos.getX()) + x, (double)(mutablePos.getY() + pos.getY()) + y, (double)(mutablePos.getZ() + pos.getZ()) + z);
    }

    public BlockPos.MutableBlockPos set(double x, double y, double z) {
        return this.mutablePos.setPos(x, y, z);
    }

    public BlockPos.MutableBlockPos set(BlockPos pos) {
        return this.mutablePos.setPos(pos.getX(), pos.getY(), pos.getZ());
    }

    public BlockPos.MutableBlockPos set(BlockPos pos, double x, double y, double z) {
        return this.mutablePos.setPos((double)pos.getX() + x, (double)pos.getY() + y, (double)pos.getZ() + z);
    }

    public BlockPos.MutableBlockPos set(BlockPos pos, int x, int y, int z) {
        return this.mutablePos.setPos(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
    }

    public BlockPos.MutableBlockPos set(int x, int y, int z) {
        return this.mutablePos.setPos(x, y, z);
    }

    public BlockPos.MutableBlockPos setAndAdd(int x, int y, int z) {
        return this.mutablePos.setPos(this.mutablePos.getX() + x, this.mutablePos.getY() + y, this.mutablePos.getZ() + z);
    }

    public BlockPos.MutableBlockPos setAndAdd(double x, double y, double z) {
        return this.mutablePos.setPos((double)this.mutablePos.getX() + x, (double)this.mutablePos.getY() + y, (double)this.mutablePos.getZ() + z);
    }

    public BlockPos.MutableBlockPos setAndAdd(BlockPos pos) {
        return this.mutablePos.setPos(this.mutablePos.getX() + pos.getX(), this.mutablePos.getY() + pos.getY(), this.mutablePos.getZ() + pos.getZ());
    }

    public BlockPos.MutableBlockPos setAndAdd(BlockPos pos, double x, double y, double z) {
        return this.mutablePos.setPos((double)(this.mutablePos.getX() + pos.getX()) + x, (double)(this.mutablePos.getY() + pos.getY()) + y, (double)(this.mutablePos.getZ() + pos.getZ()) + z);
    }
}

