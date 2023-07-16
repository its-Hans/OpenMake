package cn.make.module.combat;

import cn.make.util.DelayBlockPlace;
import cn.make.util.UtilsRewrite;
import cn.make.util.makeUtil;
import cn.make.util.skid.Timer;
import cn.make.util.skid.two.SeijaBlockUtil;
import com.mojang.realmsclient.gui.ChatFormatting;

import chad.phobos.api.center.Command;
import chad.phobos.api.center.Module;

import cn.make.util.skid.EntityUtil;
import cn.make.util.skid.InventoryUtil;

import chad.phobos.api.setting.Setting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemTool;
import net.minecraft.network.Packet;
//import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.potion.Potion;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SexyBurrow extends Module
{
    EnumHand enumHand = EnumHand.MAIN_HAND;
    private final Setting<Boolean> multiPlace;
    private final Setting<Boolean> newMulti;
    private final Setting<Integer> time;
    private final Setting<Boolean> debug;
    private final Setting<Boolean> rotate;
    private final Setting<Boolean> breakCrystal;
    private final Setting<Boolean> tpcenter;
    private final Setting<Boolean> fix;
    private final Setting<Boolean> safePlace;
    private final Setting<Double> offset;


    private static final SexyBurrow insta = new SexyBurrow();
    
    public SexyBurrow() {
        super("SexyBurrow", "Ovo?", Category.COMBAT, true, false, false);
        this.multiPlace = this.register(new Setting("MultiPlace", true));
        this.newMulti = rbool("NewMultiPlaceTest", false, v -> multiPlace.getValue());
        this.time = rinte("Delay", 0, 2, 10, v -> (newMulti.getValue() && multiPlace.getValue()));
        this.offset = rdoub("Offset", 0.3, 0.0, 0.4);
        this.rotate = this.register(new Setting("Rotate", true));
        this.tpcenter = this.register(new Setting("TPCenter", false));
        this.breakCrystal = rbool("BreakCrystal", true);
        this.debug = rbool("debug", false);
        this.fix = rbool("Fix", false);
        this.safePlace = register(new Setting("Safe", true, "something bypass"));
    }
    
    public static int getSlotByDmg(final Double minDmg) {
        for (int i = 0; i < 9; ++i) {
            if (SexyBurrow.mc.player.inventory.getStackInSlot(i).getItem() instanceof ItemTool) {
                final ItemTool currItemTool = (ItemTool)SexyBurrow.mc.player.inventory.getStackInSlot(i).getItem();
                if (currItemTool.attackDamage >= minDmg) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    public void breakcrystal() {
        final AxisAlignedBB axisAlignedBB = new AxisAlignedBB(SeijaBlockUtil.getFlooredPosition((Entity)SexyBurrow.mc.player));
        final List<Entity> l = (List<Entity>)SexyBurrow.mc.world.getEntitiesWithinAABBExcludingEntity((Entity)null, axisAlignedBB);
        for (final Entity entity : l) {
            if (entity instanceof EntityEnderCrystal) {
                if (SexyBurrow.mc.player.isPotionActive((Potion)Objects.requireNonNull(Potion.getPotionById(18)))) {
                    final int toolSlot = getSlotByDmg(4.0);
                    if (toolSlot != -1) {
                        final int oldSlot = SexyBurrow.mc.player.inventory.currentItem;
                        InventoryUtil.switchToHotbarSlot(toolSlot, false);
                        InventoryUtil.switchToHotbarSlot(oldSlot, false);
                    }
                }
                SexyBurrow.mc.player.connection.sendPacket((Packet)new CPacketUseEntity(entity));
                //SexyBurrow.mc.player.connection.sendPacket((Packet)new CPacketAnimation(EnumHand.OFF_HAND));
            }
        }
    }
    
    @Override
    public void onTick() {
        if((!mc.player.onGround) && fix.getValue()) {
            if (debug.getValue()) Command.sendMessage("fix offground");
            return;
        }
        if((mc.player.isInWeb) && fix.getValue()) {
            if (debug.getValue()) Command.sendMessage("fix inweb");
            return;
        }
        if (this.breakCrystal.getValue()) this.breakcrystal();
        if (this.tpcenter.getValue()) {
            final BlockPos startPos = EntityUtil.getRoundedBlockPos((Entity) SexyBurrow.mc.player);
        }
        final int oldSlot = SexyBurrow.mc.player.inventory.currentItem;
        final BlockPos originalPos = new BlockPos(SexyBurrow.mc.player.posX, SexyBurrow.mc.player.posY + 0.5, SexyBurrow.mc.player.posZ);
        if (InventoryUtil.getItemHotbar(Item.getItemFromBlock(Blocks.OBSIDIAN)) == -1 && InventoryUtil.getItemHotbar(Item.getItemFromBlock(Blocks.ENDER_CHEST)) == -1) {
            Command.sendMessage(ChatFormatting.RED + "No Blocks Found!");
            this.disable();
            return;
        }
        if (!SexyBurrow.mc.world.getBlockState(originalPos.offset(EnumFacing.UP)).getBlock().equals(Blocks.AIR) || (SexyBurrow.mc.world.getBlockState(originalPos.add(1, 2, 0)).getBlock() != Blocks.AIR && checkSelf(originalPos.add(1, 0, 0)) != null) || (SexyBurrow.mc.world.getBlockState(originalPos.add(0, 2, 0)).getBlock() != Blocks.AIR && checkSelf(originalPos.add(-1, 0, 0)) != null) || (SexyBurrow.mc.world.getBlockState(originalPos.add(0, 2, 0)).getBlock() != Blocks.AIR && checkSelf(originalPos.add(1, 0, 0)) != null) || (SexyBurrow.mc.world.getBlockState(originalPos.add(0, 2, 0)).getBlock() != Blocks.AIR && checkSelf(originalPos.add(0, 0, 1)) != null) || (SexyBurrow.mc.world.getBlockState(originalPos.add(0, 2, 0)).getBlock() != Blocks.AIR && checkSelf(originalPos.add(0, 0, -1)) != null) || (SexyBurrow.mc.world.getBlockState(originalPos.add(-1, 2, 0)).getBlock() != Blocks.AIR && checkSelf(originalPos.add(-1, 0, 0)) != null) || (SexyBurrow.mc.world.getBlockState(originalPos.add(0, 2, 1)).getBlock() != Blocks.AIR && checkSelf(originalPos.add(0, 0, 1)) != null) || (SexyBurrow.mc.world.getBlockState(originalPos.add(0, 2, -1)).getBlock() != Blocks.AIR && checkSelf(originalPos.add(0, 0, -1)) != null) || (SexyBurrow.mc.world.getBlockState(originalPos.add(1, 2, 1)).getBlock() != Blocks.AIR && checkSelf(originalPos.add(1, 0, 1)) != null) || (SexyBurrow.mc.world.getBlockState(originalPos.add(1, 2, -1)).getBlock() != Blocks.AIR && checkSelf(originalPos.add(1, 0, -1)) != null) || (SexyBurrow.mc.world.getBlockState(originalPos.add(-1, 2, 1)).getBlock() != Blocks.AIR && checkSelf(originalPos.add(-1, 0, 1)) != null) || (SexyBurrow.mc.world.getBlockState(originalPos.add(-1, 2, -1)).getBlock() != Blocks.AIR && checkSelf(originalPos.add(-1, 0, -1)) != null)) {
            boolean sendPacket = false;
            boolean sendPacket2 = false;
            if (this.debug.getValue()) {
                Command.sendMessage("head,,");
            }
            BlockPos offPos = originalPos;
            if (checkSelf(offPos) != null && !isAir(offPos)) {
                SexyBurrow.mc.player.connection.sendPacket((Packet)new CPacketPlayer.Position(SexyBurrow.mc.player.posX + (offPos.getX() + 0.5 - SexyBurrow.mc.player.posX) / 2.0, SexyBurrow.mc.player.posY + 0.2, SexyBurrow.mc.player.posZ + (offPos.getZ() + 0.5 - SexyBurrow.mc.player.posZ) / 2.0, false));
                SexyBurrow.mc.player.connection.sendPacket((Packet)new CPacketPlayer.Position(SexyBurrow.mc.player.posX + (offPos.getX() + 0.5 - SexyBurrow.mc.player.posX), SexyBurrow.mc.player.posY + 0.2, SexyBurrow.mc.player.posZ + (offPos.getZ() + 0.5 - SexyBurrow.mc.player.posZ), false));
            }
            else {
                for (final EnumFacing facing : EnumFacing.VALUES) {
                    if (facing != EnumFacing.UP && facing != EnumFacing.DOWN) {
                        offPos = originalPos.offset(facing);
                        if (checkSelf(offPos) != null && !isAir(offPos)) {
                            SexyBurrow.mc.player.connection.sendPacket((Packet)new CPacketPlayer.Position(SexyBurrow.mc.player.posX + (offPos.getX() + 0.5 - SexyBurrow.mc.player.posX) / 2.0, SexyBurrow.mc.player.posY + 0.2, SexyBurrow.mc.player.posZ + (offPos.getZ() + 0.5 - SexyBurrow.mc.player.posZ) / 2.0, false));
                            SexyBurrow.mc.player.connection.sendPacket((Packet)new CPacketPlayer.Position(SexyBurrow.mc.player.posX + (offPos.getX() + 0.5 - SexyBurrow.mc.player.posX), SexyBurrow.mc.player.posY + 0.2, SexyBurrow.mc.player.posZ + (offPos.getZ() + 0.5 - SexyBurrow.mc.player.posZ), false));
                            sendPacket = true;
                            break;
                        }
                    }
                }
                if (!sendPacket) {
                    for (final EnumFacing facing : EnumFacing.VALUES) {
                        if (facing != EnumFacing.UP && facing != EnumFacing.DOWN) {
                            offPos = originalPos.offset(facing);
                            if (checkSelf(offPos) != null) {
                                SexyBurrow.mc.player.connection.sendPacket((Packet)new CPacketPlayer.Position(SexyBurrow.mc.player.posX + (offPos.getX() + 0.5 - SexyBurrow.mc.player.posX) / 2.0, SexyBurrow.mc.player.posY + 0.2, SexyBurrow.mc.player.posZ + (offPos.getZ() + 0.5 - SexyBurrow.mc.player.posZ) / 2.0, false));
                                SexyBurrow.mc.player.connection.sendPacket((Packet)new CPacketPlayer.Position(SexyBurrow.mc.player.posX + (offPos.getX() + 0.5 - SexyBurrow.mc.player.posX), SexyBurrow.mc.player.posY + 0.2, SexyBurrow.mc.player.posZ + (offPos.getZ() + 0.5 - SexyBurrow.mc.player.posZ), false));
                                sendPacket2 = true;
                                break;
                            }
                        }
                    }
                    if (!sendPacket2) {
                        for (final EnumFacing facing : EnumFacing.VALUES) {
                            if (facing != EnumFacing.UP && facing != EnumFacing.DOWN) {
                                offPos = originalPos.offset(facing);
                                if (isAir(offPos) && isAir(offPos.offset(EnumFacing.UP))) {
                                    SexyBurrow.mc.player.connection.sendPacket((Packet)new CPacketPlayer.Position(SexyBurrow.mc.player.posX + (offPos.getX() + 0.5 - SexyBurrow.mc.player.posX) / 2.0, SexyBurrow.mc.player.posY + 0.2, SexyBurrow.mc.player.posZ + (offPos.getZ() + 0.5 - SexyBurrow.mc.player.posZ) / 2.0, false));
                                    SexyBurrow.mc.player.connection.sendPacket((Packet)new CPacketPlayer.Position(SexyBurrow.mc.player.posX + (offPos.getX() + 0.5 - SexyBurrow.mc.player.posX), SexyBurrow.mc.player.posY + 0.2, SexyBurrow.mc.player.posZ + (offPos.getZ() + 0.5 - SexyBurrow.mc.player.posZ), false));
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        else {
            SexyBurrow.mc.player.connection.sendPacket((Packet)new CPacketPlayer.Position(SexyBurrow.mc.player.posX, SexyBurrow.mc.player.posY + 0.4199999868869781, SexyBurrow.mc.player.posZ, false));
            SexyBurrow.mc.player.connection.sendPacket((Packet)new CPacketPlayer.Position(SexyBurrow.mc.player.posX, SexyBurrow.mc.player.posY + 0.7531999805212017, SexyBurrow.mc.player.posZ, false));
            SexyBurrow.mc.player.connection.sendPacket((Packet)new CPacketPlayer.Position(SexyBurrow.mc.player.posX, SexyBurrow.mc.player.posY + 0.9999957640154541, SexyBurrow.mc.player.posZ, false));
            SexyBurrow.mc.player.connection.sendPacket((Packet)new CPacketPlayer.Position(SexyBurrow.mc.player.posX, SexyBurrow.mc.player.posY + 1.1661092609382138, SexyBurrow.mc.player.posZ, false));
            if (this.debug.getValue()) {
                Command.sendMessage("ground,.");
            }
        }
        if (InventoryUtil.getItemHotbar(Item.getItemFromBlock(Blocks.OBSIDIAN)) != -1) {
            SexyBurrow.mc.player.inventory.currentItem = InventoryUtil.getItemHotbar(Item.getItemFromBlock(Blocks.OBSIDIAN));
        }
        else {
            SexyBurrow.mc.player.inventory.currentItem = InventoryUtil.getItemHotbar(Item.getItemFromBlock(Blocks.ENDER_CHEST));
        }
        SexyBurrow.mc.playerController.updateController();
        if (this.multiPlace.getValue()) {
            if (newMulti.getValue()) {
                newMulPlace();
            } else {
                BlockPos pos1 = new BlockPos(SexyBurrow.mc.player.posX + offset.getValue(), SexyBurrow.mc.player.posY, SexyBurrow.mc.player.posZ + offset.getValue());
                if (isAir(pos1)) makeUtil.safeBurrow(this.safePlace.getValue(), pos1, enumHand, true, true);

                BlockPos pos2 = new BlockPos(SexyBurrow.mc.player.posX + offset.getValue(), SexyBurrow.mc.player.posY, SexyBurrow.mc.player.posZ - offset.getValue());
                if (isAir(pos2)) makeUtil.safeBurrow(this.safePlace.getValue(), pos2, enumHand, true, true);

                BlockPos pos3 = new BlockPos(SexyBurrow.mc.player.posX - offset.getValue(), SexyBurrow.mc.player.posY, SexyBurrow.mc.player.posZ + offset.getValue());
                if (isAir(pos3)) makeUtil.safeBurrow(this.safePlace.getValue(), pos3, enumHand, true, true);

                BlockPos pos4 = new BlockPos(SexyBurrow.mc.player.posX - offset.getValue(), SexyBurrow.mc.player.posY, SexyBurrow.mc.player.posZ - offset.getValue());
                if (isAir(pos4)) makeUtil.safeBurrow(this.safePlace.getValue(), pos4, enumHand, true, true);
            }
        }
        else if (isAir(originalPos)) {
            makeUtil.safeBurrow(this.safePlace.getValue(), originalPos, enumHand, this.rotate.getValue(), true);
        }
        else if (isAir(new BlockPos(SexyBurrow.mc.player.posX + offset.getValue(), SexyBurrow.mc.player.posY, SexyBurrow.mc.player.posZ + offset.getValue()))) {
            makeUtil.safeBurrow(this.safePlace.getValue(), new BlockPos(SexyBurrow.mc.player.posX + offset.getValue(), SexyBurrow.mc.player.posY, SexyBurrow.mc.player.posZ + offset.getValue()), enumHand, true, true);
        }
        else if (isAir(new BlockPos(SexyBurrow.mc.player.posX + offset.getValue(), SexyBurrow.mc.player.posY, SexyBurrow.mc.player.posZ - offset.getValue()))) {
            makeUtil.safeBurrow(this.safePlace.getValue(), new BlockPos(SexyBurrow.mc.player.posX + offset.getValue(), SexyBurrow.mc.player.posY, SexyBurrow.mc.player.posZ - offset.getValue()), enumHand, true, true);
        }
        else if (isAir(new BlockPos(SexyBurrow.mc.player.posX - offset.getValue(), SexyBurrow.mc.player.posY, SexyBurrow.mc.player.posZ + offset.getValue()))) {
            makeUtil.safeBurrow(this.safePlace.getValue(), new BlockPos(SexyBurrow.mc.player.posX - offset.getValue(), SexyBurrow.mc.player.posY, SexyBurrow.mc.player.posZ + offset.getValue()), enumHand, true, true);
        }
        else if (isAir(new BlockPos(SexyBurrow.mc.player.posX - offset.getValue(), SexyBurrow.mc.player.posY, SexyBurrow.mc.player.posZ - offset.getValue()))) {
            makeUtil.safeBurrow(this.safePlace.getValue(), new BlockPos(SexyBurrow.mc.player.posX - offset.getValue(), SexyBurrow.mc.player.posY, SexyBurrow.mc.player.posZ - offset.getValue()), enumHand, true, true);
        }
        SexyBurrow.mc.player.inventory.currentItem = oldSlot;
        SexyBurrow.mc.playerController.updateController();
        SexyBurrow.mc.player.connection.sendPacket((Packet)new CPacketPlayer.Position(SexyBurrow.mc.player.posX, -7.0, SexyBurrow.mc.player.posZ, false));
        this.disable();
    }

    private synchronized void newMulPlace() {
        List<BlockPos> list = new ArrayList<>();
        BlockPos pos1 = new BlockPos(SexyBurrow.mc.player.posX + offset.getValue(), SexyBurrow.mc.player.posY, SexyBurrow.mc.player.posZ + offset.getValue());
        BlockPos pos2 = new BlockPos(SexyBurrow.mc.player.posX + offset.getValue(), SexyBurrow.mc.player.posY, SexyBurrow.mc.player.posZ - offset.getValue());
        BlockPos pos3 = new BlockPos(SexyBurrow.mc.player.posX - offset.getValue(), SexyBurrow.mc.player.posY, SexyBurrow.mc.player.posZ + offset.getValue());
        BlockPos pos4 = new BlockPos(SexyBurrow.mc.player.posX - offset.getValue(), SexyBurrow.mc.player.posY, SexyBurrow.mc.player.posZ - offset.getValue());
        if (isAir(pos1)) list.add(pos1);
        if (isAir(pos2)) list.add(pos2);
        if (isAir(pos3)) list.add(pos3);
        if (isAir(pos4)) list.add(pos4);
        int slot = UtilsRewrite.uInventory.itemSlot(Item.getItemFromBlock(Blocks.OBSIDIAN));
        new DelayBlockPlace().startPlace(list, time.getValue(), slot);
    }

    static Entity checkSelf(final BlockPos pos) {
        Entity test = null;
        final Vec3d[] vec3dList;
        final Vec3d[] array;
        final Vec3d[] varOffsets = array = EntityUtil.getVarOffsets(0, 0, 0);
        for (final Vec3d vec3d : array) {
            final BlockPos position = new BlockPos(pos).add(vec3d.x, vec3d.y, vec3d.z);
            for (final Object entity : SexyBurrow.mc.world.getEntitiesWithinAABB((Class)Entity.class, new AxisAlignedBB(position))) {
                if (entity == SexyBurrow.mc.player) {
                    if (test != null) {
                        continue;
                    }
                    test = (Entity) entity;
                }
            }
        }
        return test;
    }
    
    public static boolean isAir(final BlockPos pos) {
        final Block block = SexyBurrow.mc.world.getBlockState(pos).getBlock();
        return block instanceof BlockAir;
    }
}
