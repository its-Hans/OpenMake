package cn.make.module.player;

import cn.make.util.skid.EntityUtil;
import chad.phobos.api.center.Module;
import chad.phobos.api.setting.Setting;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.MathHelper;

public class Shake
extends Module {
    private int packets;
    public static Shake INSTANCE;
    Setting<String> input1 = this.register(new Setting<>("i1", "0.301"));
    Setting<String> input2 = this.register(new Setting<>("i2", "0.699"));
    Setting<String> input3 = this.register(new Setting<>("i3", "0.23"));
    Setting<String> input4 = this.register(new Setting<>("i4", "0.77"));

    private double roundToClosest(double d, double d2, double d3) {
        double d4 = d3 - d;
        double d5 = d - d2;
        if (d4 > d5) {
            return d2;
        }
        return d3;
    }

    public Shake() {
        super("Shake", "idk", Category.PLAYER);
        INSTANCE = this;
    }

    @Override
    public void onUpdate() {
        if (EntityUtil.isMoving()) return;
        Double pp1 = stringToDouble(input1.getValue());
        Double pp2 = stringToDouble(input2.getValue());
        Double pp3 = stringToDouble(input3.getValue());
        Double pp4 = stringToDouble(input4.getValue());
        if (pp1 == null || pp2 == null || pp3 == null || pp4 == null) {
            sendModuleMessage("not a number input!");
            return;
        }
        
        if (Shake.mc.world.getCollisionBoxes(Shake.mc.player, Shake.mc.player.getEntityBoundingBox().grow(0.01, 0.0, 0.01)).size() < 2) {
            Shake.mc.player.setPosition(
                this.roundToClosest(
                    Shake.mc.player.posX,
                    Math.floor(Shake.mc.player.posX) + pp1,
                    Math.floor(Shake.mc.player.posX) + pp2
                ),
                Shake.mc.player.posY,
                this.roundToClosest(
                    Shake.mc.player.posZ,
                    Math.floor(Shake.mc.player.posZ) + pp1,
                    Math.floor(Shake.mc.player.posZ) + pp2
                )
            );
            this.packets = 0;
        } else if (Shake.mc.player.ticksExisted % 2 == 0) {
            Shake.mc.player.setPosition(Shake.mc.player.posX + MathHelper.clamp(this.roundToClosest(Shake.mc.player.posX, Math.floor(Shake.mc.player.posX) + 0.241, Math.floor(Shake.mc.player.posX) + 0.759) - Shake.mc.player.posX, -0.03, 0.03), Shake.mc.player.posY, Shake.mc.player.posZ + MathHelper.clamp(this.roundToClosest(Shake.mc.player.posZ, Math.floor(Shake.mc.player.posZ) + 0.241, Math.floor(Shake.mc.player.posZ) + 0.759) - Shake.mc.player.posZ, -0.03, 0.03));
            Shake.mc.player.connection.sendPacket(new CPacketPlayer.Position(Shake.mc.player.posX, Shake.mc.player.posY, Shake.mc.player.posZ, true));
            Shake.mc.player.connection.sendPacket(
                new CPacketPlayer.Position(
                    this.roundToClosest(
                        Shake.mc.player.posX,
                        Math.floor(Shake.mc.player.posX) + pp3,
                        Math.floor(Shake.mc.player.posX) + pp4
                    ),
                    Shake.mc.player.posY, this.roundToClosest(
                        Shake.mc.player.posZ,
                    Math.floor(Shake.mc.player.posZ) + pp3,
                    Math.floor(Shake.mc.player.posZ) + pp4
                ),
                    true
                )
            );
            ++this.packets;
        }
    }

    @Override
    public void onDisable() {
        this.packets = 0;
    }

    @Override
    public String getDisplayInfo() {
        return String.valueOf(this.packets);
    }


    public static Double stringToDouble(String input) {
        if (input == null || input.isEmpty()) return null;

        String put = input.trim();
        if (!put.matches("\\d+\\.?\\d*")) return null;

        return Double.parseDouble(put);
    }
}

