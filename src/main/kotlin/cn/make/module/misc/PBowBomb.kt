package cn.make.module.misc

import chad.phobos.api.center.Module
import chad.phobos.api.events.network.PacketEvent
import cn.make.util.skid.Timer
import net.minecraft.init.Items
import net.minecraft.network.Packet
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*


class PBowBomb : Module("PBowBomb", "Makes bows speedy bois", Category.MISC) {

    private val ticks = rfloa("Ticks", 10f, 1f, 50f)

    private val projectileTimer = Timer()

    @SubscribeEvent
    fun onPacketSend(event: PacketEvent.Send) {
        if (event.getPacket<Packet<*>>() is CPacketPlayerDigging) {
            val packet:CPacketPlayerDigging = event.getPacket()
            if (packet.action == CPacketPlayerDigging.Action.RELEASE_USE_ITEM) {
                if (mc.player.activeItemStack.getItem() == Items.BOW && projectileTimer.passedMs(5000)) {
                    mc.player.connection.sendPacket(
                        CPacketEntityAction(
                            mc.player, CPacketEntityAction.Action.START_SPRINTING
                        )
                    )
                    mc.player.connection.sendPacket(
                        CPacketEntityAction(
                            mc.player, CPacketEntityAction.Action.START_SPRINTING
                        )
                    )

                    val projectileRandom = Random()

                    for (tick in 0 until ticks.value.toInt()) {
                        val sin = -kotlin.math.sin(Math.toRadians(mc.player.rotationYaw.toDouble()))
                        val cos = kotlin.math.cos(Math.toRadians(mc.player.rotationYaw.toDouble()))

                        if (projectileRandom.nextBoolean()) {
                            mc.player.connection.sendPacket(
                                CPacketPlayer.Position(
                                    mc.player.posX + sin * 100, mc.player.posY + 5, mc.player.posZ + cos * 100, false
                                )
                            )
                            mc.player.connection.sendPacket(
                                CPacketPlayer.Position(
                                    mc.player.posX - sin * 100, mc.player.posY, mc.player.posZ - cos * 100, true
                                )
                            )
                        } else {
                            mc.player.connection.sendPacket(
                                CPacketPlayer.Position(
                                    mc.player.posX - sin * 100, mc.player.posY, mc.player.posZ - cos * 100, true
                                )
                            )
                            mc.player.connection.sendPacket(
                                CPacketPlayer.Position(
                                    mc.player.posX + sin * 100, mc.player.posY + 5, mc.player.posZ + cos * 100, false
                                )
                            )
                        }

                        projectileTimer.reset()
                    }
                }
            }
        }
    }

}