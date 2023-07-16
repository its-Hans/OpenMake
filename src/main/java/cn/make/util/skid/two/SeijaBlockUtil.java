//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

//Decompiled by Procyon!

package cn.make.util.skid.two;

import cn.make.util.skid.CombatUtil;
import cn.make.util.skid.RotationUtil;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeijaBlockUtil
{
    private static Minecraft mc;
    
    public static ArrayList<BlockPos> haveNeighborBlock(final BlockPos pos, final Block neighbor) {
        final ArrayList<BlockPos> blockList = new ArrayList<BlockPos>();
        if (SeijaBlockUtil.mc.world.getBlockState(pos.add(1, 0, 0)).getBlock().equals(neighbor)) {
            blockList.add(pos.add(1, 0, 0));
        }
        if (SeijaBlockUtil.mc.world.getBlockState(pos.add(-1, 0, 0)).getBlock().equals(neighbor)) {
            blockList.add(pos.add(-1, 0, 0));
        }
        if (SeijaBlockUtil.mc.world.getBlockState(pos.add(0, 1, 0)).getBlock().equals(neighbor)) {
            blockList.add(pos.add(0, 1, 0));
        }
        if (SeijaBlockUtil.mc.world.getBlockState(pos.add(0, -1, 0)).getBlock().equals(neighbor)) {
            blockList.add(pos.add(0, -1, 0));
        }
        if (SeijaBlockUtil.mc.world.getBlockState(pos.add(0, 0, 1)).getBlock().equals(neighbor)) {
            blockList.add(pos.add(0, 0, 1));
        }
        if (SeijaBlockUtil.mc.world.getBlockState(pos.add(0, 0, -1)).getBlock().equals(neighbor)) {
            blockList.add(pos.add(0, 0, -1));
        }
        return blockList;
    }
    
    public static boolean isPlaceable(final BlockPos pos, final boolean helpBlock, final boolean bBoxCheck) {
        return SeijaBlockUtil.mc.world.getBlockState(pos).getBlock().equals(Blocks.AIR) && (!helpBlock || haveNeighborBlock(pos, Blocks.AIR).size() < 6) && (!bBoxCheck || isNoBBoxBlocked(pos));
    }
    
    public static boolean isFacing(final BlockPos pos, final EnumFacing enumFacing) {
        final ImmutableMap<IProperty<?>, Comparable<?>> properties = (ImmutableMap<IProperty<?>, Comparable<?>>)SeijaBlockUtil.mc.world.getBlockState(pos).getProperties();
        for (final IProperty<?> prop : properties.keySet()) {
            if (prop.getValueClass() == EnumFacing.class && (prop.getName().equals("facing") || prop.getName().equals("rotation")) && properties.get((Object)prop) == enumFacing) {
                return true;
            }
        }
        return false;
    }
    
    public static EnumFacing getFacing(final BlockPos pos) {
        final ImmutableMap<IProperty<?>, Comparable<?>> properties = (ImmutableMap<IProperty<?>, Comparable<?>>)SeijaBlockUtil.mc.world.getBlockState(pos).getProperties();
        for (final IProperty<?> prop : properties.keySet()) {
            if (prop.getValueClass() == EnumFacing.class && (prop.getName().equals("facing") || prop.getName().equals("rotation"))) {
                return (EnumFacing)properties.get((Object)prop);
            }
        }
        return null;
    }
    
    public static boolean isNoBBoxBlocked(final BlockPos pos) {
        final AxisAlignedBB axisAlignedBB = new AxisAlignedBB(pos);
        final List<Entity> l = SeijaBlockUtil.mc.world.getEntitiesWithinAABBExcludingEntity((Entity)null, axisAlignedBB);
        return l.size() == 0;
    }

    public static BlockPos getFlooredPosition(final Entity entity) {
        return new BlockPos(Math.floor(entity.posX), (double)Math.round(entity.posY), Math.floor(entity.posZ));
    }
    
    public static boolean isNoBBoxBlocked(final BlockPos pos, final boolean ignoreSomeEnt) {
        final AxisAlignedBB axisAlignedBB = new AxisAlignedBB(pos);
        final List<Entity> l = (List<Entity>)SeijaBlockUtil.mc.world.getEntitiesWithinAABBExcludingEntity((Entity)null, axisAlignedBB);
        if (ignoreSomeEnt) {
            for (final Entity entity : l) {
                if (!(entity instanceof EntityEnderCrystal) && !(entity instanceof EntityItem) && !(entity instanceof EntityArrow) && !(entity instanceof EntityTippedArrow) && !(entity instanceof EntityArrow)) {
                    if (entity instanceof EntityXPOrb) {
                        continue;
                    }
                    return false;
                }
            }
            return true;
        }
        return l.size() == 0;
    }
    
    public static boolean isPlaceable(final BlockPos pos, final ArrayList<Block> ignoreBlock, final boolean bBoxCheck, final boolean helpBlockCheck, final boolean rayTrace) {
        boolean placeable = false;
        for (final Block iGB : ignoreBlock) {
            if (SeijaBlockUtil.mc.world.getBlockState(pos).getBlock().equals(iGB) || SeijaBlockUtil.mc.world.getBlockState(pos).getBlock().equals(Blocks.AIR)) {
                placeable = true;
                break;
            }
        }
        if (bBoxCheck && !isNoBBoxBlocked(pos, true)) {
            placeable = false;
        }
        if (helpBlockCheck && haveNeighborBlock(pos, Blocks.AIR).size() >= 6) {
            placeable = false;
        }
        if (rayTrace && !CombatUtil.rayTraceRangeCheck(pos, 0.0, 0.0)) {
            placeable = false;
        }
        return placeable;
    }
    
    public static void placeBlock(final BlockPos pos, final EnumHand hand, final boolean rotate, final boolean packet, final EnumFacing placeFac) {
        EnumFacing side = placeFac;
        final HashMap<EnumFacing, Double> distanceMap = new HashMap<EnumFacing, Double>();
        for (final EnumFacing fac : EnumFacing.values()) {
            final BlockPos offsetBlock = pos.offset(fac);
            if (!SeijaBlockUtil.mc.world.getBlockState(offsetBlock).getBlock().equals(Blocks.AIR)) {
                distanceMap.put(fac, Math.sqrt(SeijaBlockUtil.mc.player.getDistanceSq(offsetBlock)));
            }
        }
        if (distanceMap.size() != 0) {
            final List<Map.Entry<EnumFacing, Double>> list = new ArrayList<Map.Entry<EnumFacing, Double>>(distanceMap.entrySet());
            side = list.get(0).getKey();
        }
        if (SeijaBlockUtil.mc.world.getBlockState(pos.offset(side, 1)).getBlock().equals(Blocks.AIR) && placeFac == null) {
            return;
        }
        if (side == null) {
            return;
        }
        final BlockPos neighbour = pos.offset(side);
        final EnumFacing opposite = side.getOpposite();
        final Vec3d hitVec = new Vec3d((Vec3i)neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
        final Block neighbourBlock = BlockUtil.mc.world.getBlockState(neighbour).getBlock();
        if (!BlockUtil.mc.player.isSneaking() && (BlockUtil.blackList.contains(neighbourBlock) || BlockUtil.shulkerList.contains(neighbourBlock))) {
            BlockUtil.mc.player.connection.sendPacket((Packet<net.minecraft.network.play.INetHandlerPlayServer>)new CPacketEntityAction((Entity)BlockUtil.mc.player, CPacketEntityAction.Action.START_SNEAKING));
            BlockUtil.mc.player.setSneaking(true);
        }
        if (rotate) {
            RotationUtil.faceVector(hitVec, true);
        }
        BlockUtil.rightClickBlock(neighbour, hitVec, hand, opposite, packet);
        BlockUtil.mc.rightClickDelayTimer = 4;
    }
    
    public static void sneak(final BlockPos pos) {
        if (!BlockUtil.mc.player.isSneaking() && (BlockUtil.blackList.contains(pos) || BlockUtil.shulkerList.contains(pos))) {
            BlockUtil.mc.player.connection.sendPacket((Packet<net.minecraft.network.play.INetHandlerPlayServer>)new CPacketEntityAction((Entity)BlockUtil.mc.player, CPacketEntityAction.Action.START_SNEAKING));
            BlockUtil.mc.player.setSneaking(true);
        }
    }
    
    public static BlockPos vec3toBlockPos(final Vec3d vec3d, final boolean Yfloor) {
        if (Yfloor) {
            return new BlockPos(Math.floor(vec3d.x), Math.floor(vec3d.y), Math.floor(vec3d.z));
        }
        return new BlockPos(Math.floor(vec3d.x), (double)Math.round(vec3d.y), Math.floor(vec3d.z));
    }
    
    public static BlockPos vec3toBlockPos(final Vec3d vec3d) {
        return new BlockPos(Math.floor(vec3d.x), (double)Math.round(vec3d.y), Math.floor(vec3d.z));
    }
    
    public static boolean fakeBBoxCheck(final EntityPlayer player, final Vec3d offset, final boolean headcheck) {
        final Vec3d actualPos = player.getPositionVector().add(offset);
        if (headcheck) {
            final Vec3d playerPos = player.getPositionVector();
            return isAir(actualPos.add(0.3, 0.0, 0.3)) && isAir(actualPos.add(-0.3, 0.0, 0.3)) && isAir(actualPos.add(0.3, 0.0, -0.3)) && isAir(actualPos.add(-0.3, 0.0, 0.3)) && isAir(actualPos.add(0.3, 1.8, 0.3)) && isAir(actualPos.add(-0.3, 1.8, 0.3)) && isAir(actualPos.add(0.3, 1.8, -0.3)) && isAir(actualPos.add(-0.3, 1.8, 0.3)) && isAir(playerPos.add(0.3, 2.8, 0.3)) && isAir(playerPos.add(-0.3, 2.8, 0.3)) && isAir(playerPos.add(-0.3, 2.8, -0.3)) && isAir(playerPos.add(0.3, 2.8, -0.3));
        }
        return isAir(actualPos.add(0.3, 0.0, 0.3)) && isAir(actualPos.add(-0.3, 0.0, 0.3)) && isAir(actualPos.add(0.3, 0.0, -0.3)) && isAir(actualPos.add(-0.3, 0.0, 0.3)) && isAir(actualPos.add(0.3, 1.8, 0.3)) && isAir(actualPos.add(-0.3, 1.8, 0.3)) && isAir(actualPos.add(0.3, 1.8, -0.3)) && isAir(actualPos.add(-0.3, 1.8, 0.3));
    }
    
    public static boolean isAir(final Vec3d vec3d) {
        return SeijaBlockUtil.mc.world.getBlockState(vec3toBlockPos(vec3d, true)).getBlock().equals(Blocks.AIR);
    }
    
    public static void placeBlock(final Vec3d vec3d, final EnumHand hand, final boolean rotate, final boolean packet) {
        final BlockPos pos = vec3toBlockPos(vec3d);
        final EnumFacing side = BlockUtil.getFirstFacing(pos);
        if (side == null) {
            return;
        }
        final BlockPos neighbour = pos.offset(side);
        final EnumFacing opposite = side.getOpposite();
        final Vec3d hitVec = new Vec3d((Vec3i)neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
        final Block neighbourBlock = BlockUtil.mc.world.getBlockState(neighbour).getBlock();
        if (!BlockUtil.mc.player.isSneaking() && (BlockUtil.blackList.contains(neighbourBlock) || BlockUtil.shulkerList.contains(neighbourBlock))) {
            BlockUtil.mc.player.connection.sendPacket((Packet<net.minecraft.network.play.INetHandlerPlayServer>)new CPacketEntityAction((Entity)BlockUtil.mc.player, CPacketEntityAction.Action.START_SNEAKING));
            BlockUtil.mc.player.setSneaking(true);
        }
        if (rotate) {
            RotationUtil.faceVector(hitVec, true);
        }
        BlockUtil.rightClickBlock(neighbour, hitVec, hand, opposite, packet);
        BlockUtil.mc.rightClickDelayTimer = 4;
    }
    
    static {
        SeijaBlockUtil.mc = Minecraft.getMinecraft();
    }
}
