package chad.phobos.modules.player;

import cn.make.util.skid.BlockUtil;
import cn.make.util.skid.EntityUtil;
import cn.make.util.skid.InventoryUtil;
import cn.make.util.skid.two.SeijaBlockUtil;
import com.mojang.realmsclient.gui.ChatFormatting;
import chad.phobos.api.events.render.Render3DEvent;
import chad.phobos.api.center.Module;
import chad.phobos.api.setting.Setting;
import chad.phobos.api.managers.CommandManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.util.Comparator;
import java.util.stream.Collectors;

public class stayBurrow extends Module {
    public stayBurrow() {
        super("Burrow", "qwq", Category.PLAYER);
    }
    private boolean isSneaking = false;

    private final Setting<Boolean> breakCrystal = rbool("BreakCrystal", true);
    private final Setting<Boolean> onlyOnGround = rbool("OnlyOnGround", true);
    private final Setting<Boolean> rotate = rbool("Rotate", true);
    private final Setting<Boolean> smartOffset = rbool("SmartOffset", true);
    public Setting<Boolean> tpcenter = rbool("TPCenter", false);

    private final Setting<Double> offsetX =rdoub("OffsetX", -7.0D, -10.0D, 10.0D);
    private final Setting<Double> offsetY =rdoub("OffsetY", -7.0D, -10.0D, 10.0D);
    private final Setting<Double> offsetZ =rdoub("OffsetZ", -7.0D, -10.0D, 10.0D);

    private final Setting<Boolean> fristObby = rbool("FristObby", true);


    public void onDisable() {
        this.isSneaking = EntityUtil.stopSneaking(this.isSneaking);
    }

    @Override
    public void onTick() {

        if (onlyOnGround.getValue() & (!mc.player.onGround | mc.world.getBlockState(SeijaBlockUtil.getFlooredPosition(mc.player).offset(EnumFacing.DOWN)).getBlock().equals(Blocks.AIR)))
            return;
        this.isSneaking = EntityUtil.stopSneaking(this.isSneaking);

        if (this.breakCrystal.getValue()) {
            if (breakCrystal.getValue())back();
        }
        if (!mc.world.isBlockLoaded(mc.player.getPosition())) {
            return;
        }
        if (InventoryUtil.getItemHotbar(Item.getItemFromBlock(Blocks.OBSIDIAN)) == -1) {
            if (InventoryUtil.getItemHotbar(Item.getItemFromBlock(Blocks.ENDER_CHEST)) == -1) {
                sendModuleMessage(ChatFormatting.RED + "Obsidian/Ender Chest ?");
                this.disable();
                return;
            }
        }
        if (tpcenter.getValue()) {
            BlockPos startPos = EntityUtil.getRoundedBlockPos(mc.player);
            CommandManager.setPositionPacket((double) startPos.getX() + 0.5, startPos.getY(), (double) startPos.getZ() + 0.5, true, true, true);
        }
        if (!SeijaBlockUtil.fakeBBoxCheck(mc.player, new Vec3d(0, 0, 0), true)) {

            BlockPos pos = getOffsetBlock(mc.player);
            if (!mc.world.getBlockState(SeijaBlockUtil.getFlooredPosition(mc.player).offset(EnumFacing.UP,2)).getBlock().equals(Blocks.AIR)){
                for (EnumFacing facing:EnumFacing.VALUES) {
                    if (facing==EnumFacing.UP || facing==EnumFacing.DOWN)continue;
                    BlockPos offPos = SeijaBlockUtil.getFlooredPosition(mc.player).offset(facing);
                    if (BlockUtil.isAir(offPos) && BlockUtil.isAir(offPos.offset(EnumFacing.UP))){
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX+(offPos.getX()+0.5-mc.player.posX)/2, mc.player.posY +0.188383748, mc.player.posZ+(offPos.getZ()+0.5-mc.player.posZ)/2, false));
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX+(offPos.getX()+0.5-mc.player.posX), mc.player.posY+0.123232, mc.player.posZ+(offPos.getZ()+0.5-mc.player.posZ), false));

                    }
                }
            }else {
                if (pos != null && mc.world.getBlockState(SeijaBlockUtil.getFlooredPosition(mc.player).offset(EnumFacing.UP, 2)).getBlock().equals(Blocks.AIR)) {
                    double offX = pos.getX() + 0.5 - mc.player.posX;
                    double offZ = pos.getZ() + 0.5 - mc.player.posZ;
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX + offX * 0.25, mc.player.posY + 0.419999986886978, mc.player.posZ + offZ * 0.25, false));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX + offX * 0.5, mc.player.posY + 0.7531999805212015, mc.player.posZ + offZ * 0.5, false));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX + offX * 0.75, mc.player.posY + 1.001335979112147, mc.player.posZ + offZ * 0.75, false));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(pos.getX() + 0.5, mc.player.posY + 1.166109260938214, pos.getZ() + 0.5, false));
                } else {
                    disable();
                    return;
                }
            }
        } else {
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.419999986886978, mc.player.posZ, false));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.7531999805212015, mc.player.posZ, false));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.001335979112147, mc.player.posZ, false));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.166109260938214, mc.player.posZ, false));
        }
        final int a = mc.player.inventory.currentItem;
        if (fristObby.getValue()) {
            if (InventoryUtil.getItemHotbar(Item.getItemFromBlock(Blocks.OBSIDIAN)) != -1) {
                InventoryUtil.switchToHotbarSlot(InventoryUtil.getItemHotbar(Item.getItemFromBlock(Blocks.OBSIDIAN)), false);
            } else if (InventoryUtil.getItemHotbar(Item.getItemFromBlock(Blocks.ENDER_CHEST)) != -1) {
                InventoryUtil.switchToHotbarSlot(InventoryUtil.getItemHotbar(Item.getItemFromBlock(Blocks.ENDER_CHEST)), false);
            }
        } else {
            if (InventoryUtil.getItemHotbar(Item.getItemFromBlock(Blocks.ENDER_CHEST)) != -1) {
                InventoryUtil.switchToHotbarSlot(InventoryUtil.getItemHotbar(Item.getItemFromBlock(Blocks.ENDER_CHEST)), false);
            } else if (InventoryUtil.getItemHotbar(Item.getItemFromBlock(Blocks.OBSIDIAN)) != -1) {
                InventoryUtil.switchToHotbarSlot(InventoryUtil.getItemHotbar(Item.getItemFromBlock(Blocks.OBSIDIAN)), false);
            }
        }
        this.isSneaking = BlockUtil.placeBlock(new BlockPos(getPlayerPosFixY(mc.player)), EnumHand.MAIN_HAND, this.rotate.getValue(), true, this.isSneaking);
        mc.playerController.updateController();
        mc.player.connection.sendPacket(new CPacketHeldItemChange(a));
        mc.player.inventory.currentItem = a;
        mc.playerController.updateController();
        this.mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(new BlockPos(this.mc.player.posX, this.mc.player.posY - 1.0, this.mc.player.posZ), EnumFacing.UP, EnumHand.MAIN_HAND, 0.0f, 0.0f, 0.0f));

        if (smartOffset.getValue()) {
            boolean defaultOffset = true;
            if (mc.player.posY >= 3) {
                for (int i = -10; i < 10; i++) {
                    if (i == -1)
                        i = 4;
                    if (mc.world.getBlockState(SeijaBlockUtil.getFlooredPosition(mc.player).add(0, i, 0)).getBlock().equals(Blocks.AIR)
                            && mc.world.getBlockState(SeijaBlockUtil.getFlooredPosition(mc.player).add(0, i + 1, 0)).getBlock().equals(Blocks.AIR)
                    ) {
                        BlockPos pos = SeijaBlockUtil.getFlooredPosition(mc.player).add(0, i, 0);
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(pos.getX() + 0.3, pos.getY(), pos.getZ() + 0.3, false));
                        defaultOffset = false;
                        break;
                    }
                }
            }

            if (defaultOffset) {
                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX + this.offsetX.getValue(), mc.player.posY + this.offsetY.getValue(), mc.player.posZ + offsetZ.getValue(), false));
            }

        } else {
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX + this.offsetX.getValue(), mc.player.posY + this.offsetY.getValue(), mc.player.posZ + offsetZ.getValue(), false));
        }
        this.disable();
    }

    public static BlockPos getPlayerPosFixY(EntityPlayer player) {
        return new BlockPos(Math.floor(player.posX), Math.round(player.posY), Math.floor(player.posZ));
    }

    public BlockPos getOffsetBlock(EntityPlayer player) {
        Vec3d vec3d1 = new Vec3d(player.boundingBox.minX, player.boundingBox.minY, player.boundingBox.minZ);
        if (canBur(vec3d1)) return SeijaBlockUtil.vec3toBlockPos(vec3d1);
        Vec3d vec3d2 = new Vec3d(player.boundingBox.maxX, player.boundingBox.minY, player.boundingBox.minZ);
        if (canBur(vec3d2)) return SeijaBlockUtil.vec3toBlockPos(vec3d2);
        Vec3d vec3d3 = new Vec3d(player.boundingBox.minX, player.boundingBox.minY, player.boundingBox.maxZ);
        if (canBur(vec3d3)) return SeijaBlockUtil.vec3toBlockPos(vec3d3);
        Vec3d vec3d4 = new Vec3d(player.boundingBox.maxX, player.boundingBox.minY, player.boundingBox.maxZ);
        if (canBur(vec3d4)) return SeijaBlockUtil.vec3toBlockPos(vec3d4);
        return null;
    }

    public boolean canBur(Vec3d vec3d) {
        BlockPos pos = SeijaBlockUtil.vec3toBlockPos(vec3d);
        return BlockUtil.isAir(pos) && BlockUtil.isAir(pos.offset(EnumFacing.UP)) && BlockUtil.isAir(pos.offset(EnumFacing.UP, 2));
    }
    @Override
    public void onRender3D(Render3DEvent event) {
        if (breakCrystal.getValue())back();
    }


    public static void back() {
        for (Entity crystal : mc.world.loadedEntityList.stream().filter(e -> e instanceof EntityEnderCrystal && !e.isDead).sorted(Comparator.comparing(e -> mc.player.getDistance(e))).collect(Collectors.toList())) {
            if (crystal instanceof EntityEnderCrystal) {
                mc.player.connection.sendPacket(new CPacketUseEntity(crystal));
                mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.OFF_HAND));
            }
        }
    }

}

