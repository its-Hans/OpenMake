package cn.make.module.combat

import cn.make.Targets
import cn.make.util.UtilsRewrite
import chad.phobos.api.center.Module
import chad.phobos.api.setting.Bind
import chad.phobos.api.setting.Setting
import chad.phobos.api.utils.Timer
import net.minecraft.block.Block
import net.minecraft.block.BlockAir
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import java.util.*

class KeyPushMake : Module("KeyPush", "description", Category.COMBAT) {

    var timer = Timer()
    private val pushBind: Setting<Bind> = rbind("PushBind",
        Bind(-1)
    )
    var delay: Setting<Int> = rinte("Delay", 60, 10, 100)
    private var mode2: Setting<Boolean> = rbool("Mode2", false)
    var target: EntityPlayer? = null
    private val pushList: MutableList<PushINFO> = ArrayList()
    private fun onPress(): Boolean {
        return pushBind.value.isDown
    }

    class PushINFO(val power: BlockPos?, val piston: BlockPos?, val fac: EnumFacing, val pull: Boolean)
    enum class PistonList(val fac: EnumFacing) {
        N(EnumFacing.NORTH), S(EnumFacing.SOUTH), W(EnumFacing.WEST), E(EnumFacing.EAST);

        private fun blocked(player: EntityPlayer?): Boolean {
            return UtilsRewrite.uBlock.getState(
                UtilsRewrite.uBlock.addPos(
                    UtilsRewrite.posHelper(player).faceBlock,
                    BlockPos(fac.opposite.directionVec)
                )
            ) !is BlockAir
        }

        fun pushable(player: EntityPlayer?): Boolean {
            //trapped
            return if (UtilsRewrite.uBlock.getState(UtilsRewrite.posHelper(player).headBlock) !is BlockAir) false else !blocked(
                player
            )
            //返回false
        }

        fun pullable(player: EntityPlayer?): Boolean {
            //trapped
            if (UtilsRewrite.uBlock.getState(UtilsRewrite.posHelper(player).headBlock) !is BlockAir) return false
            val blocked = blocked(player)
            if (blocked) {
                //piston up hasBlock
                if (UtilsRewrite.uBlock.getState(BlockPos(fac.directionVec).up()) is BlockAir) {
                    //onburrow
                    if (UtilsRewrite.uBlock.isBlockFull(UtilsRewrite.posHelper(player).feetBlock)) {
                        return true
                    }
                }
            }
            return false
        }

        private val offset2: BlockPos
            get() = BlockPos(fac.directionVec)

        fun offsetFrom(from: BlockPos?): BlockPos {
            return UtilsRewrite.uBlock.addPos(from, offset2)
        }
    }

    private fun getPushInfo(p: PistonList, target: EntityPlayer?, range: Double): PushINFO {
        val posHelper = UtilsRewrite.posHelper(target)
        var cpush = false
        var cpull = false
        var power: BlockPos? = null
        var piston: BlockPos? = null
        val fac = p.fac
        var doPull = false
        val pistonTemp = p.offsetFrom(posHelper.faceBlock)
        if (UtilsRewrite.uBlock.customCanPlace(pistonTemp, arrayOf(Blocks.AIR, Blocks.PISTON))) {
            piston = pistonTemp
            cpush = p.pushable(target)
            cpull = p.pullable(target)
        }
        if (!cpush) {
            if (cpull) {
                doPull = true
            }
        }
        if (piston != null) {
            val upPos = piston.up()
            val upv3d = Vec3d(upPos.x + 0.5, upPos.y + 0.5, upPos.z + 0.5)
            UtilsRewrite.uBlock.getBlock(upPos)
            if (UtilsRewrite.uBlock.fullCustomCanPlace(
                    upPos,
                    true,
                    false,
                    true,
                    arrayOf(Blocks.AIR)
                )
            ) {
                if (posHelper.posEye().distanceTo(upv3d) < range) {
                    power = upPos
                }
            }
            if (power == null) {
                val canPlaceOffsets: MutableList<BlockPos> = ArrayList()
                for (tempPos in posHelper.offsetList(piston)) {
                    if (UtilsRewrite.uBlock.fullCustomCanPlace(
                            upPos,
                            true,
                            false,
                            true,
                            arrayOf(Blocks.AIR, Blocks.REDSTONE_BLOCK)
                        )
                    ) {
                        canPlaceOffsets.add(tempPos)
                    }
                }
                val canPlaceArray = canPlaceOffsets.toTypedArray()
                power = UtilsRewrite.uBlock.bestDistance(canPlaceArray)
            }
        }
        return PushINFO(power, piston, fac, doPull)
    }
    override fun onUpdate() {
        if (fullNullCheck()) return
        if (!onPress()) {
            pushList.clear()
            return
        }
        if (!timer.passedMs(delay.value.toLong())) {
            return
        }
        target = Targets.getTarget()
        if (target == null) return
        val infoList: MutableList<PushINFO> = ArrayList()
        for (eValue in PistonList.values()) {
            val tmpInfo = getPushInfo(eValue, target, Targets._this().rangeset.value)
            if (tmpInfo.piston != null && tmpInfo.power != null) {
                var notAir = 0
                if (UtilsRewrite.uBlock.getBlock(tmpInfo.piston) !is BlockAir) notAir++
                if (UtilsRewrite.uBlock.getBlock(tmpInfo.power) !is BlockAir) notAir++

                /*
                when(notAir) {
                    0 -> {
                        infoList.add(tmpInfo)
                        break
                    }
                    1 -> {
                        infoList.add(tmpInfo)
                        break
                    }
                    else -> break
                }
                */
                if (notAir <= 1) {
                    infoList.add(tmpInfo)
                }
            }
        }
        if (infoList.isEmpty()) {
            return
        }
        val tmpPistonList: MutableMap<BlockPos?, PushINFO> = TreeMap()
        for (pushInfo in infoList) {
            tmpPistonList[pushInfo.piston] = pushInfo
        }
        val best = UtilsRewrite.uBlock.bestDistance(tmpPistonList.keys.toTypedArray())
        val iNFO = tmpPistonList[best]
        placeBlock(Blocks.PISTON, iNFO?.fac, iNFO?.piston)
        UtilsRewrite.uBlock.placeBlock(iNFO!!.piston, iNFO.fac, Blocks.PISTON, true, true, false, true)
        if (!timer.passedMs(delay.value.toLong())) {
            return
        }
        timer.reset()
        placeBlock(Blocks.REDSTONE_BLOCK, null, iNFO.power)
        if (iNFO.pull) UtilsRewrite.uBlock.clickBlock(iNFO.power)
    }
    fun placeBlock(block: Block, facing: EnumFacing?, pos: BlockPos?) {
        if (UtilsRewrite.uBlock.getBlock(pos) !is BlockAir) {
            return
        }
        if (mode2.value) {
            UtilsRewrite.uBlock.doPlace2(pos, facing, true, true, block)
        } else {
            UtilsRewrite.uBlock.placeBlock(pos, facing, block, true, true, false, true)
        }
    }
    /*
    companion object {
        fun posHelper(target: EntityPlayer?): UtilsRewrite.posHelper {
            return posHelper(target)
        }
    }
    */
}