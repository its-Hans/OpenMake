package cn.make.module.misc;

import cn.make.Targets;
import cn.make.util.BlockChecker;
import cn.make.util.rotateutils;
import cn.make.util.skid.two.BlockUtil;
import chad.phobos.api.center.Module;
import chad.phobos.api.setting.Setting;
import chad.phobos.api.utils.MathUtil;
import net.minecraft.block.BlockObsidian;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class AutoCityBypass extends Module {
    public Setting<Boolean> debug = rbool("debug", false);
    public Setting<Boolean> once = rbool("Once", true);
    public Setting<Boolean> newClicker = rbool("NewClick", true);
    public Setting<Boolean> superBypass = rbool("SuperBypass", false, v -> (!newClicker.getValue()));
    public Setting<Boolean> lookback = rbool("LookBack", true, v -> sb());
    public AutoCityBypass() {
        super("CityPlus", "idk", Category.MISC);
    }
    public boolean sb() {
        return (newClicker.getValue() && superBypass.getValue());
    }

    @Override
    public void onUpdate() {
        if (fullNullCheck()) {
            this.disable();
            return;
        }
        doCity();
        if (once.getValue()) this.disable();
    }
    public void doCity() {
        EntityPlayer target = Targets.getTarget();
        if (target == null) {
            if (debug.getValue()) sendModuleMessage("No target");
            return;
        }
        BlockPos burrow = new BlockPos(target.posX, target.posY, target.posZ);
        BlockPos surround = null;
        BlockPos[] surrounds = {
            burrow.north(),
            burrow.east(),
            burrow.west(),
            burrow.south()
        };
        List<BlockPos> canDamageSurrounds = new ArrayList<>();
        if (BlockChecker.getBlockType(burrow) instanceof BlockObsidian) {
            safeDamage(burrow, debug.getValue());
            if (debug.getValue()) sendModuleMessage("damage on burrow");
            return;
        }
        for (BlockPos surpos : surrounds) {
            if (BlockChecker.getBlockType(surpos) instanceof BlockObsidian) {
                canDamageSurrounds.add(surpos);
                if (debug.getValue()) sendModuleMessage("founded an surround on " + BlockChecker.simpleXYZString(surpos));
            }
        }
        surround = BlockChecker.bestDist(canDamageSurrounds);
        if (surround == null) {
            sendModuleMessage("NO POS founded to damage");
            return;
        }
        safeDamage(surround, debug.getValue());
    }
    public void safeDamage(BlockPos pos, boolean debug) {
        if (newClicker.getValue()) {
            EnumFacing fac = rotateutils.clickBlockXIN2(pos);
            if (debug) sendModuleMessage("ondmg block " + BlockChecker.simpleXYZString(pos) + " facing : " + fac.getName());
            return;
        }
        if (superBypass.getValue()) {
            float o1 = mc.player.rotationYaw;
            float o2 = mc.player.rotationPitch;
            float o3 = mc.player.renderYawOffset;
            float o4 = mc.player.rotationYawHead;
            float[] angle = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), new Vec3d(pos));
            float yaw = angle[0];
            float pitch = angle[1];
            mc.player.rotationYaw = yaw;
            mc.player.rotationPitch = pitch;
            mc.player.renderYawOffset = yaw;
            mc.player.rotationYawHead = yaw;
            mc.world.getBlockState(pos).getBlock();
            mc.playerController.onPlayerDamageBlock(pos, BlockUtil.getRayTraceFacing(pos));
            if (lookback.getValue()) {
                mc.player.rotationYaw = o1;
                mc.player.rotationPitch = o2;
                mc.player.renderYawOffset = o3;
                mc.player.rotationYawHead = o4;
            }
        } else {
            rotateutils.clckBlockXIN(pos, debug);
        }
    }
}