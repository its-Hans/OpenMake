package cn.make.module.misc;

import chad.phobos.api.center.Module;
import chad.phobos.api.setting.Setting;
import cn.make.tweaksClient;
import com.mojang.realmsclient.gui.ChatFormatting;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;

public class TPCoordLog
extends Module {
    private final Setting<Boolean> saveCoords = this.register(new Setting<>("SaveToFile", true));
    private final HashMap<Entity, Vec3d> knownPlayers = new HashMap();
    private final HashMap<String, Vec3d> tpdPlayers = new HashMap();
    private int numTicks;
    private int numForgetTicks;

    public TPCoordLog() {
        super("TPCoordLog", "New exploit", Category.MISC);
    }

    @Override
    public void onUpdate() {
        if (this.numTicks >= 50) {
            this.numTicks = 0;
            for (Entity entity : TPCoordLog.mc.world.loadedEntityList) {
                if (!(entity instanceof EntityPlayer) || entity.getName().equals(TPCoordLog.mc.player.getName())) continue;
                Vec3d playerPos = new Vec3d(entity.posX, entity.posY, entity.posZ);
                if (this.knownPlayers.containsKey(entity) && Math.abs(this.knownPlayers.get(entity).distanceTo(playerPos)) > 50.0 && Math.abs(TPCoordLog.mc.player.getPositionVector().distanceTo(playerPos)) > 100.0 && (!this.tpdPlayers.containsKey(entity.getName()) || this.tpdPlayers.get(entity.getName()) != playerPos)) {
                    notiMessage(ChatFormatting.WHITE + entity.getName() + ChatFormatting.GRAY + " has TP'd to " + ChatFormatting.WHITE + this.vectorToString(playerPos));
                    this.saveFile(this.vectorToString(playerPos), entity.getName());
                    this.knownPlayers.remove(entity);
                    this.tpdPlayers.put(entity.getName(), playerPos);
                }
                this.knownPlayers.put(entity, playerPos);
            }
        }
        if (this.numForgetTicks >= 9000000) {
            this.tpdPlayers.clear();
        }
        ++this.numTicks;
        ++this.numForgetTicks;
    }

    private String vectorToString(Vec3d vector) {
        return "(" + (int)Math.floor(vector.x) + ", " + (int)Math.floor(vector.z) + ")";
    }

    private void saveFile(String pos, String name) {
        if (this.saveCoords.getValue()) {
            try {
                File file = new File("./" + tweaksClient.simplecfgpath + "/coordsLog.txt");
                file.getParentFile().mkdirs();
                PrintWriter writer = new PrintWriter(new FileWriter(file, true));
                String ip = !mc.isSingleplayer() ? TPCoordLog.mc.currentServerData.serverIP : "singleplayer";
                writer.println("(Teleport) IGN: " + name + " Pos: " + pos + "Server: " + ip);
                writer.close();
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }
}

