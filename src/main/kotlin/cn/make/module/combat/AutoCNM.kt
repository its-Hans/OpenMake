package cn.make.module.combat

import chad.phobos.api.center.Module
import cn.make.util.skid.Timer
import net.minecraft.entity.player.EntityPlayer

class AutoCNM : Module("4i04Fucker", "NMSL", Category.COMBAT) {
    private var fuckerdelay = rinte("FuckDelay",500,0,10000)
    private var only404 = rbool("Only4i04", true)
    private var fucker2 = mutableListOf<String>()
    private val name404 = mutableListOf("4i04", "鬼牌")

    override fun onEnable() { resetfucker() }
    override fun onDisable() { resetfucker() }

    private var timer = Timer()
    override fun onTick() {
        if (mc.player == null || mc.world == null) return
        val it = fucker2.iterator()
        while (it.hasNext()) {
            if (timer.passedMs(fuckerdelay.value.toLong())) {
                if (canSend()) {
                    mc.player.sendChatMessage(it.next())
                    it.remove()
                }
            }
        }
        if (fucker2.isEmpty()) resetfucker()
    }

    private fun canSend(): Boolean {
        if (!only404.value) return true
        for (players: EntityPlayer in mc.world.playerEntities) {
            if (name404.contains(players.name)) return true
        }
        return false
    }
    private fun resetfucker() {
        fucker2 = mutableListOf(
            "老子操你妈逼的窝囊废辱华玩意",
            "你家祖坟是不是被鬼子内射了",
            "你脑中是不是给狗屎堵塞了",
            "你他妈就好比一条野犬找到了生活的希望",
            "你写的垃圾Moongod就和窝囊废一样",
            "你以为你什么东西啊",
            "老子轻轻松松给你女朋友给你亲妈操了",
            "你懂不懂 你以为你在游戏里面非常牛逼其实现实世界就是一个窝囊废吗",
            "老子看到你就和看到路边的一坨狗屎一样恶心"
        )
    }
}