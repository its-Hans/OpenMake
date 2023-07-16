package cn.make.module.combat

import cn.make.Targets
import cn.make.util.UtilsRewrite
import chad.phobos.api.center.Module
import chad.phobos.api.setting.Setting
import chad.phobos.api.utils.Timer
import net.minecraft.block.Block
import net.minecraft.block.BlockAir
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos

class PullCrystalMake : Module("PullCrystalNew", "description", Category.COMBAT) {

    companion object {
        fun pHelper(): UtilsRewrite.posHelper {
            return UtilsRewrite.posHelper(null)
        }
        fun pHelper(player: EntityPlayer): UtilsRewrite.posHelper {
            return UtilsRewrite.posHelper(player)
        }
    }
    var timer = Timer()
    val delay: Setting<Int> = rinte("Delay", 60, 10, 100)
    var target: EntityPlayer? = null
    override fun onUpdate() {
        if (fullNullCheck()) return
        if (!timer.passedMs(delay.value.toLong())) {
            return
        } else {
            timer.reset()
        }
        target = Targets.getTarget()
        if (target == null) return
        val pistonList = getPistonList(target!!.position.up())
    }
    fun placeBlock(block: Block, facing: EnumFacing?, pos: BlockPos?) {
        if (UtilsRewrite.uBlock.getBlock(pos) is BlockAir) {
            UtilsRewrite.uBlock.doPlace2(pos, facing, true, true, block)
        }
    }

    private fun getPistonList(playerFace: BlockPos?): Set<BlockPos> {
        val pistonList: MutableSet<BlockPos> = LinkedHashSet()
        for (fac in UtilsRewrite.helperEF.facingHelper.values()) {
            pistonList.add(fac.getOffsetLeftPos(playerFace).up())
            pistonList.add(fac.getOffsetRightPos(playerFace).up())
        }
        pistonList.removeIf { data: BlockPos? -> UtilsRewrite.uBlock.getBlock(data) !is BlockAir }
        pistonList.removeIf { data: BlockPos? -> !UtilsRewrite.uBlock.hasHelping(data) }
        return pistonList
    }
}