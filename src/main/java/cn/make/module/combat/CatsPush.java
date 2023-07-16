/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.state.IBlockState
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.init.Blocks
 *  net.minecraft.network.Packet
 *  net.minecraft.network.play.client.CPacketHeldItemChange
 *  net.minecraft.util.EnumHand
 *  net.minecraft.util.math.BlockPos
 */
package cn.make.module.combat;

import cn.make.Targets;
import cn.make.util.UtilsRewrite;
import chad.phobos.api.events.render.Render2DEvent;
import chad.phobos.api.center.Module;
import chad.phobos.api.setting.Setting;


import cn.make.util.skid.BlockUtil;
import cn.make.util.skid.InventoryUtil;
import cn.make.util.skid.MathUtil;

import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;


public class CatsPush
        extends Module {
    public static EntityPlayer target;
    public static String debugText2;

    public CatsPush() {
        super("simplePush", "233", Category.COMBAT, true, false, false);
    }

    private final Setting<Boolean> rotate = this.register(new Setting<Boolean>("Rotate", true));
    private final Setting<Boolean> mineRedstone = this.register(new Setting<Boolean>("mineRS", true));
    private final Setting<Boolean> disable = this.register(new Setting<Boolean>("Disable", true));
    private final Setting<Boolean> debug = this.register(new Setting<Boolean>("Debug", true));

    @Override
    public void onUpdate() {
        if (InventoryUtil.findHotbarBlock(BlockPistonBase.class) == -1) {
            debugText2 = " ";
            if (disable.getValue()) {
                this.toggle();
            }
            return;
        }
        if ((InventoryUtil.findHotbarBlock(Blocks.REDSTONE_BLOCK) == -1)) {
            debugText2 = " ";
            if (disable.getValue()) {
                this.toggle();
            }
            return;
        }
        target = Targets.getTarget();
        if (target == null) {
            debugText2 = " ";
            if (disable.getValue()) {
                this.toggle();
            }
            return;
        }
        BlockPos pos = new BlockPos(target.posX, target.posY, target.posZ);
        boolean isBurrow = mc.world.getBlockState(pos).getBlock() != Blocks.AIR;
        float[] angle = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), new Vec3d((float) pos.getX() + 0.5f, (float) pos.getY() + 0.5f, (float) pos.getZ() + 0.5f));
        if (
            angle[1] >= -71 && angle[1] <= 71 && angle[0] >= -51 && angle[0] <= 51
                && (
                    getBlock(pos.add(0, 1, 1)).getBlock() == Blocks.AIR
                        | getBlock(pos.add(0, 1, 1)).getBlock() == Blocks.PISTON
            )
        ) {
            this.perform(pos.add(0, 1, 1));
            if (getBlock(pos.add(0, 2, 1)).getBlock() == Blocks.AIR) {
                this.perform1(pos.add(0, 2, 1));
            } else if (getBlock(pos.add(0, 2, 1)).getBlock() == Blocks.REDSTONE_BLOCK) {
            } else if (getBlock(pos.add(1, 1, 1)).getBlock() == Blocks.AIR) {
                this.perform1(pos.add(1, 1, 1));
            } else if (getBlock(pos.add(1, 1, 1)).getBlock() == Blocks.REDSTONE_BLOCK) {
            } else if (getBlock(pos.add(0, 1, 2)).getBlock() == Blocks.AIR) {
                this.perform1(pos.add(0, 1, 2));
            } else if (getBlock(pos.add(0, 1, 2)).getBlock() == Blocks.REDSTONE_BLOCK) {
            } else if (getBlock(pos.add(-1, 1, 1)).getBlock() == Blocks.AIR) {
                this.perform1(pos.add(-1, 1, 1));
            }
        } else if (
            angle[1] >= -71 && angle[1] <= 71 && (angle[0] >= 129 | angle[0] <= -129)
                && (
                    getBlock(pos.add(0, 1, -1)).getBlock() == Blocks.AIR
                        | getBlock(pos.add(0, 1, -1)).getBlock() == Blocks.PISTON
            )
        ) {
            this.perform(pos.add(0, 1, -1));
            if (getBlock(pos.add(0, 2, -1)).getBlock() == Blocks.AIR) {
                this.perform1(pos.add(0, 2, -1));
            } else if (getBlock(pos.add(0, 2, -1)).getBlock() == Blocks.REDSTONE_BLOCK) {
            } else if (getBlock(pos.add(1, 1, -1)).getBlock() == Blocks.AIR) {
                this.perform1(pos.add(1, 1, -1));
            } else if (getBlock(pos.add(1, 1, -1)).getBlock() == Blocks.REDSTONE_BLOCK) {
            } else if (getBlock(pos.add(0, 1, -2)).getBlock() == Blocks.AIR) {
                this.perform1(pos.add(0, 1, -2));
            } else if (getBlock(pos.add(0, 1, -2)).getBlock() == Blocks.REDSTONE_BLOCK) {
            } else if (getBlock(pos.add(-1, 1, -1)).getBlock() == Blocks.AIR) {
                this.perform1(pos.add(-1, 1, -1));
            }
        } else if (
            angle[1] >= -71 && angle[1] <= 71 && angle[0] <= -51 && angle[0] >= -129
                && (
                    getBlock(pos.add(1, 1, 0)).getBlock() == Blocks.AIR
                        | getBlock(pos.add(1, 1, 0)).getBlock() == Blocks.PISTON
            )
        ) {
            this.perform(pos.add(1, 1, 0));
            if (getBlock(pos.add(1, 2, 0)).getBlock() == Blocks.AIR) {
                this.perform1(pos.add(1, 2, 0));
            } else if (getBlock(pos.add(1, 2, 0)).getBlock() == Blocks.REDSTONE_BLOCK) {
            } else if (getBlock(pos.add(1, 1, 1)).getBlock() == Blocks.AIR) {
                this.perform1(pos.add(1, 1, 1));
            } else if (getBlock(pos.add(1, 1, 1)).getBlock() == Blocks.REDSTONE_BLOCK) {
            } else if (getBlock(pos.add(2, 1, 0)).getBlock() == Blocks.AIR) {
                this.perform1(pos.add(2, 1, 0));
            } else if (getBlock(pos.add(2, 1, 0)).getBlock() == Blocks.REDSTONE_BLOCK) {
            } else if (getBlock(pos.add(1, 1, -1)).getBlock() == Blocks.AIR) {
                this.perform1(pos.add(1, 1, -1));
            }
        } else if (
            angle[1] >= -71 && angle[1] <= 71 && angle[0] >= 51 && angle[0] <= 129
                && (
                    getBlock(pos.add(-1, 1, 0)).getBlock() == Blocks.AIR
                        | getBlock(pos.add(-1, 1, 0)).getBlock() == Blocks.PISTON
            )
        ) {
            this.perform(pos.add(-1, 1, 0));
            if (getBlock(pos.add(-1, 2, 0)).getBlock() == Blocks.AIR) {
                this.perform1(pos.add(-1, 2, 0));
            } else if (getBlock(pos.add(-1, 2, 0)).getBlock() == Blocks.REDSTONE_BLOCK) {
            } else if (getBlock(pos.add(-1, 1, 1)).getBlock() == Blocks.AIR) {
                this.perform1(pos.add(-1, 1, 1));
            } else if (getBlock(pos.add(-1, 1, 1)).getBlock() == Blocks.REDSTONE_BLOCK) {
            } else if (getBlock(pos.add(-2, 1, 0)).getBlock() == Blocks.AIR) {
                this.perform1(pos.add(-2, 1, 0));
            } else if (getBlock(pos.add(-2, 1, 0)).getBlock() == Blocks.REDSTONE_BLOCK) {
            } else if (getBlock(pos.add(-1, 1, -1)).getBlock() == Blocks.AIR) {
                this.perform1(pos.add(-1, 1, -1));
            }
        } else {
            if (disable.getValue()) {
                this.toggle();
            }
        }
    }

    public String getDisplayInfo() {
        if (target != null) {
            return target.getName();
        }
        return null;
    }

    @Override
    public void onRender2D(Render2DEvent event) {
        if (!debug.getValue()) return;
        if (InventoryUtil.findHotbarBlock(BlockPistonBase.class) == -1) return;
        if ((InventoryUtil.findHotbarBlock(Blocks.REDSTONE_BLOCK) == -1)) return;
        target = Targets.getTarget();
        if (target == null)
            return;
        BlockPos pos = new BlockPos(target.posX, target.posY, target.posZ);
        float[] angle = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), new Vec3d((float) pos.getX() + 0.5f, (float) pos.getY() + 0.5f, (float) pos.getZ() + 0.5f));
        mc.fontRenderer.drawString(angle[0] + "   " + angle[1], 200, 200, 255, true);
        mc.fontRenderer.drawString(debugText2, 200, 240, 255, true);
    }

    private IBlockState getBlock(BlockPos block) {
        return mc.world.getBlockState(block);
    }

    private void perform(BlockPos pos) {
        int old = CatsPush.mc.player.inventory.currentItem;
        if (mc.world.getBlockState(pos).getBlock() == Blocks.AIR) {
            debugText2 = "trying place piston" + pos;
            mc.player.inventory.currentItem = InventoryUtil.findHotbarBlock(BlockPistonBase.class);
            mc.playerController.updateController();
            BlockUtil.placeBlock(pos, EnumHand.MAIN_HAND, this.rotate.getValue(), true, false);
            mc.player.inventory.currentItem = old;
            mc.playerController.updateController();
        }
    }

    private void perform1(BlockPos pos) {
        int old = CatsPush.mc.player.inventory.currentItem;
        if (mc.world.getBlockState(pos).getBlock() == Blocks.AIR) {
            debugText2 = "trying place rs" + pos;
            mc.player.inventory.currentItem = InventoryUtil.findHotbarBlock(Blocks.REDSTONE_BLOCK);
            mc.playerController.updateController();
            BlockUtil.placeBlock(pos, EnumHand.MAIN_HAND, this.rotate.getValue(), true, false);
            if (mineRedstone.getValue()) {
                debugText2 = "trying break rs" + pos;
                UtilsRewrite.uBlock.clickBlock(pos);
            }
            mc.player.inventory.currentItem = old;
            mc.playerController.updateController();
        }
    }

    @Override
    public void onEnable() {
        debugText2 = " ";
    }
    @Override
    public void onDisable() {
        debugText2 = " ";
    }

}

