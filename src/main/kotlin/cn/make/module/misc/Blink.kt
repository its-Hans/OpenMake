/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package cn.make.module.misc


import chad.phobos.api.center.Module
import chad.phobos.api.events.network.PacketEvent
import cn.make.util.PacketCenter
import cn.make.util.skid.Timer
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.network.Packet
import net.minecraft.network.play.client.*
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*
import java.util.concurrent.LinkedBlockingQueue

class Blink : Module("Blink", "OpenSkyrim", Category.MISC) {
    private val packets = LinkedBlockingQueue<Packet<*>>()
    private var fakePlayer: EntityOtherPlayerMP? = null
    private var disableLogger = false
    private val positions = LinkedList<DoubleArray>()
    private val pulseValue = rbool("Pulse", false)
    private val pulseDelayValue = rinte("PulseDelay", 1000, 500, 5000)
    private val pulseTimer = Timer()
    private val cancelC0f = rbool("AntiCheat", true)

    override fun onEnable() {
        val thePlayer = mc.player ?: return

        if (!pulseValue.value) {
            val faker = EntityOtherPlayerMP(mc.world!!, thePlayer.gameProfile)


            faker.rotationYawHead = thePlayer.rotationYawHead
            faker.renderYawOffset = thePlayer.renderYawOffset
            faker.copyLocationAndAnglesFrom(thePlayer)
            faker.rotationYawHead = thePlayer.rotationYawHead
            mc.world!!.addEntityToWorld(-1337, faker)


            fakePlayer = faker
        }
        synchronized(positions) {
            positions.add(
                doubleArrayOf(
                    thePlayer.posX,
                    thePlayer.entityBoundingBox.minY + thePlayer.eyeHeight / 2,
                    thePlayer.posZ
                )
            )
            positions.add(doubleArrayOf(thePlayer.posX, thePlayer.entityBoundingBox.minY, thePlayer.posZ))
        }
        pulseTimer.reset()
    }

    override fun onDisable() {
        if (mc.player == null)
            return

        blink()

        val faker = fakePlayer

        if (faker != null) {
            mc.world?.removeEntityFromWorld(faker.entityId)
            fakePlayer = null
        }
    }

    @SubscribeEvent
    fun onPacket(event: PacketEvent) {
        val packet = event.getPacket<Packet<*>>()

        if (mc.player == null || disableLogger)
            return

        if (packet is CPacketPlayer) // Cancel all movement stuff
            event.isCanceled = true

        if (
            packet is CPacketPlayer.Position
            || packet is CPacketPlayer.PositionRotation
            || packet is CPacketPlayerTryUseItemOnBlock
            || packet is CPacketAnimation
            || packet is CPacketEntityAction
            || packet is CPacketUseEntity
        ) {
            event.isCanceled = true
            packets.add(packet)
        }
        if (packet is CPacketConfirmTransaction && cancelC0f.value) {
            event.isCanceled = true
            packets.add(packet)
        }//取消C0F然后加到packet中,等到关闭的时候发出去//
        //fixed by potatochipscn//
    }

    override fun onUpdate() {
        val thePlayer = mc.player ?: return

        synchronized(positions) {
            positions.add(
                doubleArrayOf(
                    thePlayer.posX,
                    thePlayer.entityBoundingBox.minY,
                    thePlayer.posZ
                )
            )
        }
        if (pulseValue.value && pulseTimer.passedMs(pulseDelayValue.value.toLong())) {
            blink()
            pulseTimer.reset()
        }
    }

    override fun getDisplayInfo(): String {
        return packets.size.toString()
    }

    private fun blink() {
        try {
            disableLogger = true

            while (!packets.isEmpty()) {
                //mc.networkManager?.sendPacket(packets.take())
                mc.player.connection.sendPacket(packets.take())
            }

            disableLogger = false
        } catch (e: Exception) {
            e.printStackTrace()
            disableLogger = false
        }
        synchronized(positions) { positions.clear() }
    }
}