package cn.make.module.misc;

import cn.make.util.skid.BlockUtil;
import cn.make.util.skid.RenderUtil;
import chad.phobos.api.events.network.PacketEvent;
import chad.phobos.api.events.block.SelfDamageBlockEvent;
import chad.phobos.api.events.render.Render3DEvent;
import chad.phobos.api.center.Module;
import chad.phobos.api.setting.Bind;
import chad.phobos.api.setting.Setting;
import chad.phobos.api.utils.InventoryUtil;
import chad.phobos.api.utils.Timer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockObsidian;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class InstantMine extends Module {
    private final Timer breakSuccess = new Timer();
    private static InstantMine INSTANCE = new InstantMine();
    Setting<Boolean> creativeMode = rbool("CreativeMode", true);
    Setting<Boolean> ghostHand = rbool("GhostHand", true, v -> this.creativeMode.getValue());
    Setting<Boolean> render = rbool("Fill", true);
    Setting<Integer> falpha = rinte("FillAlpha", 30, 0, 255, v -> this.render.getValue());
    Setting<Boolean> render2 = rbool("Box", true);
    Setting<Integer> balpha = rinte("BoxAlpha", 100, 0, 255, v -> this.render2.getValue());
    private final Setting<Boolean> crystal = rbool("Crystal", true);
    private final Setting<Boolean> crystalp = rbool("Place", true, v -> this.crystal.getValue());
    public final Setting<Boolean> attackcrystal = rbool("Attack", true, v -> this.crystal.getValue());
    public final Setting<Bind> bind = rbind("ObbyBind", new Bind(-1), v -> this.crystal.getValue());
    public Setting<Boolean> db = rbool("Silent2Break", true);
    public final Setting<Float> health = rfloa("SilentHP", 0.0F, 0.0F, 36.0F, v -> this.db.getValue());
    Setting<Integer> red = rinte("Red", 255, 0, 255);
    Setting<Integer> green = rinte("Green", 255, 0, 255);
    Setting<Integer> blue = rinte("Blue", 255, 0, 255);
    Setting<Integer> alpha = rinte("BoxAlpha", 150, 0, 255);
    Setting<Integer> alpha2 = rinte("FillAlpha", 70, 0, 255);
    private final List<Block> godBlocks = Arrays.asList(
        Blocks.AIR, Blocks.FLOWING_LAVA, Blocks.LAVA, Blocks.FLOWING_WATER, Blocks.WATER, Blocks.BEDROCK
    );
    private boolean cancelStart = false;
    private boolean empty = false;
    private EnumFacing facing;
    public static BlockPos breakPos;
    public static BlockPos breakPos2;
    int slotMain2;
    int swithc2;
    double manxi = 0.0;
    double manxi2 = 0.0;
    public final Timer imerS = new Timer();
    public final Timer imerS2 = new Timer();
    static int ticked = 0;

    public InstantMine() {
        super("InstantMine", "Crazy packet miner.", Category.MISC, true, false, false);
        this.setInstance();
    }

    public static InstantMine getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new InstantMine();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    @Override
    public void onTick() {
        if (!mc.player.isCreative()) {
            this.slotMain2 = mc.player.inventory.currentItem;
            if (ticked <= 86 && ticked >= 0) {
                ++ticked;
            }

            if (breakPos2 == null) {
                this.manxi2 = 0.0;
            }

            if (breakPos2 != null && (ticked >= 65 || ticked >= 20 && mc.world.getBlockState(breakPos).getBlock() == Blocks.ENDER_CHEST)) {
                if (mc.player.getHeldItem(EnumHand.MAIN_HAND).getItem() != Items.GOLDEN_APPLE
                    && mc.player.getHeldItem(EnumHand.MAIN_HAND).getItem() != Items.CHORUS_FRUIT) {
                    if (isHealth()) {
                        if (InventoryUtil.getItemHotbar(Items.DIAMOND_PICKAXE) != -1 && this.db.getValue()) {
                            mc.player.connection.sendPacket(new CPacketHeldItemChange(InventoryUtil.getItemHotbars(Items.DIAMOND_PICKAXE)));
                            this.swithc2 = 1;
                            ++ticked;
                        }
                    } else if (this.swithc2 == 1) {
                        mc.player.connection.sendPacket(new CPacketHeldItemChange(this.slotMain2));
                        this.swithc2 = 0;
                    }
                } else if (!Mouse.isButtonDown(1)) {
                    if (isHealth()) {
                        if (InventoryUtil.getItemHotbar(Items.DIAMOND_PICKAXE) != -1 && this.db.getValue()) {
                            mc.player.connection.sendPacket(new CPacketHeldItemChange(InventoryUtil.getItemHotbars(Items.DIAMOND_PICKAXE)));
                            this.swithc2 = 1;
                            ++ticked;
                        }
                    } else if (this.swithc2 == 1) {
                        mc.player.connection.sendPacket(new CPacketHeldItemChange(this.slotMain2));
                        this.swithc2 = 0;
                    }
                } else if (this.swithc2 == 1) {
                    mc.player.connection.sendPacket(new CPacketHeldItemChange(this.slotMain2));
                    this.swithc2 = 0;
                }
            }

            if (breakPos2 != null && mc.world.getBlockState(breakPos2).getBlock() == Blocks.AIR) {
                if (this.swithc2 == 1) {
                    mc.player.connection.sendPacket(new CPacketHeldItemChange(this.slotMain2));
                    this.swithc2 = 0;
                }

                breakPos2 = null;
                this.manxi2 = 0.0;
                ticked = 0;
            }

            if (ticked == 0) {
                this.manxi2 = 0.0;
                breakPos2 = null;
            }

            if (ticked >= 140) {
                if (this.swithc2 == 1) {
                    mc.player.connection.sendPacket(new CPacketHeldItemChange(this.slotMain2));
                    this.swithc2 = 0;
                }

                this.manxi2 = 0.0;
                breakPos2 = null;
                ticked = 0;
            }

            if (breakPos != null && mc.world.getBlockState(breakPos).getBlock() == Blocks.AIR && breakPos2 == null) {
                ticked = 0;
            }

            if (!fullNullCheck()) {
                if (this.creativeMode.getValue()) {
                    if (this.cancelStart) {
                        if (this.crystal.getValue()
                            && this.attackcrystal.getValue()
                            && mc.world.getBlockState(breakPos).getBlock() == Blocks.AIR) {
                            attackcrystal();
                        }

                        if (this.bind.getValue().isDown()
                            && this.crystal.getValue()
                            && InventoryUtil.findHotbarBlock(BlockObsidian.class) != -1
                            && mc.world.getBlockState(breakPos).getBlock() == Blocks.AIR) {
                            int obbySlot = InventoryUtil.findHotbarBlock(BlockObsidian.class);
                            int old = mc.player.inventory.currentItem;
                            this.switchToSlot(obbySlot);
                            BlockUtil.placeBlock(breakPos, EnumHand.MAIN_HAND, false, true, false);
                            this.switchToSlot(old);
                        }

                        if (
                            InventoryUtil.getItemHotbar(Items.END_CRYSTAL) != -1
                            && this.crystal.getValue()
                            && mc.world.getBlockState(breakPos).getBlock() == Blocks.OBSIDIAN
                        ) {
                            if (this.empty) {
                                BlockUtil.placeCrystalOnBlock(breakPos, EnumHand.MAIN_HAND, true, false, true);
                            } else if (!this.crystalp.getValue()) {
                                BlockUtil.placeCrystalOnBlock(breakPos, EnumHand.MAIN_HAND, true, false, true);
                            }
                        }

                        if (!this.godBlocks.contains(mc.world.getBlockState(breakPos).getBlock())) {
                            if (mc.world.getBlockState(breakPos).getBlock() != Blocks.WEB) {
                                if (this.ghostHand.getValue()
                                    && InventoryUtil.getItemHotbar(Items.DIAMOND_PICKAXE) != -1
                                    && InventoryUtil.getItemHotbars(Items.DIAMOND_PICKAXE) != -1) {
                                    int slotMain = mc.player.inventory.currentItem;
                                    if (mc.world.getBlockState(breakPos).getBlock() == Blocks.OBSIDIAN) {
                                        if (!this.breakSuccess.passedMs(1234L)) {
                                            return;
                                        }

                                        mc.player.inventory.currentItem = InventoryUtil.getItemHotbar(Items.DIAMOND_PICKAXE);
                                        mc.playerController.updateController();
                                        mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, breakPos, this.facing));
                                        mc.player.inventory.currentItem = slotMain;
                                        mc.playerController.updateController();
                                        return;
                                    }

                                    mc.player.inventory.currentItem = InventoryUtil.getItemHotbar(Items.DIAMOND_PICKAXE);
                                    mc.playerController.updateController();
                                    mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, breakPos, this.facing));
                                    mc.player.inventory.currentItem = slotMain;
                                    mc.playerController.updateController();
                                    return;
                                }
                            } else if (this.ghostHand.getValue()
                                && InventoryUtil.getItemHotbar(Items.DIAMOND_SWORD) != -1
                                && InventoryUtil.getItemHotbars(Items.DIAMOND_SWORD) != -1) {
                                int slotMain = mc.player.inventory.currentItem;
                                mc.player.inventory.currentItem = InventoryUtil.getItemHotbar(Items.DIAMOND_SWORD);
                                mc.playerController.updateController();
                                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, breakPos, this.facing));
                                mc.player.inventory.currentItem = slotMain;
                                mc.playerController.updateController();
                                return;
                            }

                            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, breakPos, this.facing));
                        }
                    }
                }
            }
        }
    }

    private void switchToSlot(int slot) {
        mc.player.inventory.currentItem = slot;
        mc.playerController.updateController();
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (!mc.player.isCreative()) {
            if (breakPos2 != null) {
                AxisAlignedBB axisAlignedBB = mc.world.getBlockState(breakPos2).getSelectedBoundingBox(mc.world, breakPos2);
                double centerX = axisAlignedBB.minX + (axisAlignedBB.maxX - axisAlignedBB.minX) / 2.0;
                double centerY = axisAlignedBB.minY + (axisAlignedBB.maxY - axisAlignedBB.minY) / 2.0;
                double centerZ = axisAlignedBB.minZ + (axisAlignedBB.maxZ - axisAlignedBB.minZ) / 2.0;
                double var10001 = axisAlignedBB.maxX - centerX;
                double progressValX = getInstance().manxi2 * (var10001 / 10.0);
                var10001 = axisAlignedBB.maxY - centerY;
                double progressValY = getInstance().manxi2 * (var10001 / 10.0);
                var10001 = axisAlignedBB.maxZ - centerZ;
                double progressValZ = getInstance().manxi2 * (var10001 / 10.0);
                AxisAlignedBB axisAlignedBB1 = new AxisAlignedBB(
                    centerX - progressValX, centerY - progressValY, centerZ - progressValZ, centerX + progressValX, centerY + progressValY, centerZ + progressValZ
                );
                if (breakPos != null) {
                    if (!breakPos2.equals(breakPos)) {
                        RenderUtil.drawBBBox(
                            axisAlignedBB1, new Color(this.red.getValue(), this.green.getValue(), this.blue.getValue(), this.alpha.getValue()), this.alpha.getValue()
                        );
                        RenderUtil.drawBBFill(
                            axisAlignedBB1,
                            new Color(this.red.getValue(), this.green.getValue(), this.blue.getValue(), this.alpha2.getValue()),
                            this.alpha2.getValue()
                        );
                    }
                } else {
                    RenderUtil.drawBBBox(
                        axisAlignedBB1, new Color(this.red.getValue(), this.green.getValue(), this.blue.getValue(), this.alpha.getValue()), this.alpha.getValue()
                    );
                    RenderUtil.drawBBFill(
                        axisAlignedBB1, new Color(this.red.getValue(), this.green.getValue(), this.blue.getValue(), this.alpha2.getValue()), this.alpha2.getValue()
                    );
                }
            }

            if (this.creativeMode.getValue() && this.cancelStart) {
                if (this.godBlocks.contains(mc.world.getBlockState(breakPos).getBlock())) {
                    this.empty = true;
                }

                if (this.imerS.passedMs(15L)) {
                    if (this.manxi <= 10.0) {
                        this.manxi += 0.11;
                    }

                    this.imerS.reset();
                }

                if (this.imerS2.passedMs(22L)) {
                    if (this.manxi2 <= 10.0 && this.manxi2 >= 0.0) {
                        this.manxi2 += 0.11;
                    }

                    this.imerS2.reset();
                }

                AxisAlignedBB axisAlignedBB = mc.world.getBlockState(breakPos).getSelectedBoundingBox(mc.world, breakPos);
                double centerX = axisAlignedBB.minX + (axisAlignedBB.maxX - axisAlignedBB.minX) / 2.0;
                double centerY = axisAlignedBB.minY + (axisAlignedBB.maxY - axisAlignedBB.minY) / 2.0;
                double centerZ = axisAlignedBB.minZ + (axisAlignedBB.maxZ - axisAlignedBB.minZ) / 2.0;
                double progressValX = this.manxi * ((axisAlignedBB.maxX - centerX) / 10.0);
                double progressValY = this.manxi * ((axisAlignedBB.maxY - centerY) / 10.0);
                double progressValZ = this.manxi * ((axisAlignedBB.maxZ - centerZ) / 10.0);
                AxisAlignedBB axisAlignedBB1 = new AxisAlignedBB(
                    centerX - progressValX, centerY - progressValY, centerZ - progressValZ, centerX + progressValX, centerY + progressValY, centerZ + progressValZ
                );
                if (this.render.getValue()) {
                    RenderUtil.drawBBFill(axisAlignedBB1, new Color(this.empty ? 0 : 255, this.empty ? 255 : 0, 0, 255), this.falpha.getValue());
                }

                if (this.render2.getValue()) {
                    RenderUtil.drawBBBox(axisAlignedBB1, new Color(this.empty ? 0 : 255, this.empty ? 255 : 0, 0, 255), this.balpha.getValue());
                }
            }
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (!fullNullCheck()) {
            if (!mc.player.isCreative()) {
                if (event.getPacket() instanceof CPacketPlayerDigging) {
                    CPacketPlayerDigging packet = event.getPacket();
                    if (packet.getAction() == CPacketPlayerDigging.Action.START_DESTROY_BLOCK) {
                        event.setCanceled(this.cancelStart);
                    }
                }
            }
        }
    }

    public static void attackcrystal() {
        for(Entity crystal : mc.world
            .loadedEntityList
            .stream()
            .filter(e -> e instanceof EntityEnderCrystal && !e.isDead)
            .sorted(Comparator.comparing(e -> mc.player.getDistance(e)))
            .collect(Collectors.toList())) {
            if (crystal instanceof EntityEnderCrystal && crystal.getDistanceSq(breakPos) <= 2.0) {
                mc.player.connection.sendPacket(new CPacketUseEntity(crystal));
                mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
            }
        }
    }

    @SubscribeEvent
    public void onBlockEvent(SelfDamageBlockEvent.port1 event) {
        if (!fullNullCheck()) {
            if (!mc.player.isCreative()) {
                if (BlockUtil.canBreak(event.pos)) {
                    if (breakPos == null
                        || breakPos.getX() != event.pos.getX()
                        || breakPos.getY() != event.pos.getY()
                        || breakPos.getZ() != event.pos.getZ()) {
                        if (ticked == 0) {
                            ticked = 1;
                        }

                        if (this.manxi2 == 0.0) {
                            this.manxi2 = 0.11;
                        }

                        if (breakPos != null && breakPos2 == null && mc.world.getBlockState(breakPos).getBlock() != Blocks.AIR) {
                            breakPos2 = breakPos;
                        }

                        if (breakPos == null && breakPos2 == null) {
                            breakPos2 = event.pos;
                        }

                        this.manxi = 0.0;
                        this.empty = false;
                        this.cancelStart = false;
                        breakPos = event.pos;
                        this.breakSuccess.reset();
                        this.facing = event.facing;
                        if (breakPos != null) {
                            mc.player.swingArm(EnumHand.MAIN_HAND);
                            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, breakPos, this.facing));
                            this.cancelStart = true;
                            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, breakPos, this.facing));
                            event.setCanceled(true);
                        }
                    }
                }
            }
        }
    }
    boolean isHealth() {
        final float h = this.health.getValue();
        if (h <= 0.2f || h >= 36.0f) {
            return true;
        }
        return mc.player.getHealth() + mc.player.getAbsorptionAmount() >= h;
    }
}
