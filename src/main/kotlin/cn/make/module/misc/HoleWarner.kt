package cn.make.module.misc

import cn.make.util.UtilsRewrite
import chad.phobos.api.events.network.PacketEvent
import chad.phobos.api.center.Module
import net.minecraft.block.BlockAir
import net.minecraft.block.BlockObsidian
import net.minecraft.network.Packet
import net.minecraft.network.play.server.SPacketBlockBreakAnim
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.concurrent.ConcurrentSkipListSet
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

class HoleWarner : Module("HoleWarner", "hw", Category.MISC) {
    class INFO(var breakPos: BlockPos, var progress: Int, var playerName: String)
    private var listHole = ArrayList<BlockPos>()
    private var breakinSet = ConcurrentSkipListSet<INFO>()
    private var warnINFOs = ArrayList<INFO>()
    override fun onTick() {
        val posHelper = UtilsRewrite.posHelper(mc.player)
        val holeList = ArrayList<BlockPos>()
        holeList.addAll(posHelper.surroundList()) //sur
        holeList.add(posHelper.feetBlock) //bur
        val obiList = ArrayList<BlockPos>()
        for (pos in holeList) {
            if (UtilsRewrite.uBlock.getBlock(pos) is BlockObsidian) {
                obiList.add(pos)
            }
        }
        listHole = obiList
        val it:Iterator<INFO> = breakinSet.iterator()
        while (it.hasNext()) {
            val data = it.next()
            if (listHole.contains(data.breakPos)) {
                warnINFOs.add(data)
            }
        }
    }

    override fun onUpdate() {
        val it:Iterator<INFO> = tempSet.iterator()
        while (it.hasNext()) {
            val data = it.next()
            if (UtilsRewrite.uBlock.getBlock(data.breakPos) !is BlockAir) {
                breakinSet.add(data)
            } else {
                breakinSet.remove(data)
            }
        }
    }

    private var tempSet = HashSet<INFO>()
    @SubscribeEvent
    fun onPacketReceive(send: PacketEvent.Receive) {
        if (send.getPacket<Packet<*>>() is SPacketBlockBreakAnim) {
            val packet = send.getPacket<Packet<*>>() as SPacketBlockBreakAnim
            val pos:BlockPos = packet.position
            val progress:Int = packet.progress
            val breakPlayer:String = mc.world.getEntityByID(packet.breakerId)!!.name
            tempSet.add(INFO(pos, progress, breakPlayer))
        }
    }
}