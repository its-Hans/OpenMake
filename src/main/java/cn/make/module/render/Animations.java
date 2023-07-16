//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Jorge\Desktop\Minecraft-Deobfuscator3000-master\1.12 stable mappings"!

/*
 * Decompiled with CFR 0.150.
 *
 * Could not load the following classes:
 *  net.minecraft.init.MobEffects
 *  net.minecraft.network.play.client.CPacketAnimation
 *  net.minecraft.potion.PotionEffect
 *  net.minecraft.util.EnumHand
 *  net.minecraftforge.fml.common.eventhandler.SubscribeEvent
 */
package cn.make.module.render;

import chad.phobos.api.center.Module;
import chad.phobos.api.events.network.PacketEvent;
import chad.phobos.api.setting.Setting;
import net.minecraft.network.play.client.*;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Animations
    extends Module {
    private final Setting<Boolean> cancelSwing = rbool("NoServerSwing", false);
    private final Setting<Boolean> offSwing = rbool("OffhandSwing", false);
    private final Setting<Boolean> oldAnimation = rbool("OldAnimation", false);

    public Animations() {
        super("Animation", "Change animations.", Category.RENDER);
    }

    @Override
    public void onUpdate() {
        if (this.offSwing.getValue()) {
            Animations.mc.player.swingingHand = EnumHand.OFF_HAND;
        }
        if (this.oldAnimation.getValue()) {
            Animations.mc.entityRenderer.itemRenderer.equippedProgressMainHand = 1.0f;
            Animations.mc.entityRenderer.itemRenderer.itemStackMainHand = Animations.mc.player.getHeldItemMainhand();
        }
    }
    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (
            event.getPacket() instanceof CPacketAnimation
                && this.cancelSwing.getValue()
        ) event.setCanceled(true);
    }
}

