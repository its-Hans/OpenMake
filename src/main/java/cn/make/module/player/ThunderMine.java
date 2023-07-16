package cn.make.module.player;

import cn.make.util.skid.RebirthUtil;
import cn.make.util.skid.SilentRotationUtil;
import cn.make.util.skid.two.BlockUtil;
import chad.phobos.api.events.block.BlockEvent;
import chad.phobos.api.events.network.PacketEvent;
import chad.phobos.api.events.render.Render3DEvent;
import chad.phobos.api.events.player.UpdateWalkingPlayerEvent;
import chad.phobos.api.center.Module;
import chad.phobos.api.setting.Setting;
import chad.phobos.asm.accessors.IPlayerControllerMP;
import chad.phobos.api.utils.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraft.network.play.client.CPacketConfirmTransaction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;


public class ThunderMine extends Module {

    private static float mineDamage;
    private final Setting<Float> range = this.register(new Setting<Float>("Range", 4.2f, 3.0f, 10.0f));
    public Setting<Boolean> rotate = this.register(new Setting<Boolean>("Rotate", false));
    public Setting<Boolean> rotationFix = rbool("RotationFix", true);
    public Setting<Boolean> strict = this.register(new Setting<Boolean>("Strict", false));
    public Setting<Boolean> strictReMine = this.register(new Setting<Boolean>("StrictBreak", false));
    public Setting<Boolean> render = this.register(new Setting<Boolean>("Render", false));
    Setting<Integer> startAlpha = rinte("StartAlpha", 40, 1, 60, v -> render.getValue());
    private final Setting<Mode> mode = register(new Setting("Mode", Mode.Packet));
    private final Setting<Float> startDamage = this.register(new Setting<Float>("StartDamage", 0.1f, 0.0f, 1.0f, v -> mode.getValue() == Mode.Damage));
    private final Setting<Float> endDamage = this.register(new Setting<Float>("EndDamage", 0.9f, 0.0f, 1.0f, v -> mode.getValue() == Mode.Damage));
    private BlockPos minePosition;
    private EnumFacing mineFacing;
    private int mineBreaks;
    public ThunderMine() {
        super("ThunderMine", "Allows you to dig-quickly", Module.Category.PLAYER);
    }

    @Override
    public void onUpdate() {
        if (!mc.player.capabilities.isCreativeMode) {
            if (minePosition != null) {
                double mineDistance = mc.player.getDistanceSq(minePosition.add(0.5, 0.5, 0.5));
                if (mineBreaks >= 2 && strictReMine.getValue() || mineDistance > getPow2Value(range)) {
                    minePosition = null;
                    mineFacing = null;
                    mineDamage = 0;
                    mineBreaks = 0;
                }
            }
            if (mode.getValue() == Mode.Damage) {
                if (((IPlayerControllerMP)mc.playerController).getCurBlockDamageMP() < startDamage.getValue())
                    ((IPlayerControllerMP)mc.playerController).setCurBlockDamageMP(startDamage.getValue());
                if (((IPlayerControllerMP)mc.playerController).getCurBlockDamageMP() >= endDamage.getValue())
                    ((IPlayerControllerMP)mc.playerController).setCurBlockDamageMP(1f);
            } else if (mode.getValue() == Mode.NexusGrief) {
                if (mc.player.getHeldItemMainhand().getItem() instanceof ItemPickaxe) {
                    if (((IPlayerControllerMP)mc.playerController).getCurBlockDamageMP() < 0.17f)
                        ((IPlayerControllerMP)mc.playerController).setCurBlockDamageMP(0.17f);
                    if (((IPlayerControllerMP)mc.playerController).getCurBlockDamageMP() >= 0.83) {
                        ((IPlayerControllerMP)mc.playerController).setCurBlockDamageMP(1f);}
                } else if (mc.player.getHeldItemMainhand().getItem() instanceof ItemAxe) {
                    if (((IPlayerControllerMP)mc.playerController).getCurBlockDamageMP() < 0.17f)
                        ((IPlayerControllerMP)mc.playerController).setCurBlockDamageMP(0.17f);
                    if (((IPlayerControllerMP)mc.playerController).getCurBlockDamageMP() >= 1f)
                        ((IPlayerControllerMP)mc.playerController).setCurBlockDamageMP(1f);
                } else if (mc.player.getHeldItemMainhand().getItem() == Items.STONE_SHOVEL || mc.player.getHeldItemMainhand().getItem() == Items.IRON_SHOVEL || mc.player.getHeldItemMainhand().getItem() == Items.DIAMOND_SHOVEL) {
                    if (((IPlayerControllerMP)mc.playerController).getCurBlockDamageMP() < 0.17f)
                        ((IPlayerControllerMP)mc.playerController).setCurBlockDamageMP(0.17f);
                    if (((IPlayerControllerMP)mc.playerController).getCurBlockDamageMP() >= 1f)
                        ((IPlayerControllerMP)mc.playerController).setCurBlockDamageMP(1f);
                }
            } else if (mode.getValue() == Mode.Packet) {
                if (minePosition != null && !mc.world.isAirBlock(minePosition)) {
                    if (mineDamage >= 1) {
                        int previousSlot = mc.player.inventory.currentItem;
                        int swapSlot = getTool(minePosition);
                        if (swapSlot == -1) return;
                        if (strict.getValue()) {
                            short nextTransactionID = mc.player.openContainer.getNextTransactionID(mc.player.inventory);
                            ItemStack itemstack = mc.player.openContainer.slotClick(swapSlot, mc.player.inventory.currentItem, ClickType.SWAP, mc.player);
                            mc.player.connection.sendPacket(new CPacketClickWindow(mc.player.inventoryContainer.windowId, swapSlot, mc.player.inventory.currentItem, ClickType.SWAP, itemstack, nextTransactionID));
                        } else {
                            mc.player.connection.sendPacket(new CPacketHeldItemChange(swapSlot));
                        }

                        mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, minePosition, mineFacing));
                        mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, minePosition, EnumFacing.UP));

                        if (strict.getValue()) {
                            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, minePosition, mineFacing));
                        }
                        mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, minePosition, mineFacing));
                        if (previousSlot != -1) {
                            if (strict.getValue()) {
                                short nextTransactionID = mc.player.openContainer.getNextTransactionID(mc.player.inventory);
                                ItemStack itemstack = mc.player.openContainer.slotClick(swapSlot, mc.player.inventory.currentItem, ClickType.SWAP, mc.player);
                                mc.player.connection.sendPacket(new CPacketClickWindow(mc.player.inventoryContainer.windowId, swapSlot, mc.player.inventory.currentItem, ClickType.SWAP, itemstack, nextTransactionID));
                                mc.player.connection.sendPacket(new CPacketConfirmTransaction(mc.player.inventoryContainer.windowId, nextTransactionID, true));
                            } else {
                                mc.player.connection.sendPacket(new CPacketHeldItemChange(previousSlot));
                            }
                        }
                        mineDamage = 0;
                        mineBreaks++;
                    }
                    mineDamage += getBlockStrength(mc.world.getBlockState(minePosition), minePosition);
                } else {
                    mineDamage = 0;
                }
            }
        }
    }

    public float getBlockStrength(IBlockState state, BlockPos position) {
        float hardness = state.getBlockHardness(mc.world, position);
        if (hardness < 0) {
            return 0;
        }
        if (!canBreak(position)) {
            return getDigSpeed(state) / hardness / 100F;
        } else {
            return getDigSpeed(state) / hardness / 30F;
        }
    }

    public float getDigSpeed(IBlockState state) {
        float digSpeed = getDestroySpeed(state);
        if (digSpeed > 1) {
            ItemStack itemstack = getTool2(state);
            int efficiencyModifier = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, itemstack);
            if (efficiencyModifier > 0 && !itemstack.isEmpty()) {
                digSpeed += StrictMath.pow(efficiencyModifier, 2) + 1;
            }
        }
        if (mc.player.isPotionActive(MobEffects.HASTE)) {
            digSpeed *= 1 + (mc.player.getActivePotionEffect(MobEffects.HASTE).getAmplifier() + 1) * 0.2F;
        }
        if (mc.player.isPotionActive(MobEffects.MINING_FATIGUE)) {
            float fatigueScale;
            switch (mc.player.getActivePotionEffect(MobEffects.MINING_FATIGUE).getAmplifier()) {
                case 0:
                    fatigueScale = 0.3F;
                    break;
                case 1:
                    fatigueScale = 0.09F;
                    break;
                case 2:
                    fatigueScale = 0.0027F;
                    break;
                case 3:
                default:
                    fatigueScale = 8.1E-4F;
            }

            digSpeed *= fatigueScale;
        }
        if (mc.player.isInsideOfMaterial(Material.WATER) && !EnchantmentHelper.getAquaAffinityModifier(mc.player)) {
            digSpeed /= 5;
        }
        if (!mc.player.onGround) {
            digSpeed /= 5;
        }
        return (digSpeed < 0 ? 0 : digSpeed);
    }

    public float getDestroySpeed(IBlockState state) {
        float destroySpeed = 1;
        if (getTool2(state) != null && !getTool2(state).isEmpty()) {
            destroySpeed *= getTool2(state).getDestroySpeed(state);
        }
        return destroySpeed;
    }

    @Override
    public void onDisable() {
        minePosition = null;
        mineFacing = null;
        mineDamage = 0;
        mineBreaks = 0;
    }

    @SubscribeEvent
    public void onRender3D(Render3DEvent e) {
        if (mode.getValue() == Mode.Packet) {
            if (minePosition != null && !mc.world.isAirBlock(minePosition)) {
                Color olC = new Color(240,240,240,60);
                Color bxC = new Color(255, 255, 255, getMineProgColor(mineDamage));
                RenderUtil.drawBlockOutline(minePosition, olC, 2.4F, false);
                RenderUtil.drawBox(minePosition, bxC);

            }
        }
    }

    @SubscribeEvent
    public void onLeftClickBlock(BlockEvent event) {
        if (canBreak(event.pos) && !mc.player.capabilities.isCreativeMode) {
            if (mode.getValue() == Mode.Creative) {
                mc.player.swingArm(EnumHand.MAIN_HAND);
                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, event.pos, event.facing));
                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, event.pos, event.facing));
                mc.playerController.onPlayerDestroyBlock(event.pos);
                mc.world.setBlockToAir(event.pos);
            }
            if (mode.getValue() == Mode.Packet) {
                if (!event.pos.equals(minePosition)) {
                    minePosition = event.pos;
                    if (rotationFix.getValue()) {
                        mineFacing = BlockUtil.getRayTraceFacing(minePosition);
                    } else mineFacing = event.facing;
                    mineDamage = 0;
                    mineBreaks = 0;
                    if (minePosition != null && mineFacing != null) {
                        mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, minePosition, mineFacing));
                        mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, minePosition, EnumFacing.UP));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onEntitySync(UpdateWalkingPlayerEvent event) {
        if (rotate.getValue()) {
            if (mineDamage > 0.95) {
                if (minePosition != null) {
                    if (rotationFix.getValue()) {
                        RebirthUtil.facePosFacing(minePosition, BlockUtil.getRayTraceFacing(minePosition));
                    }
                    float[] angle = SilentRotationUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), new Vec3d(minePosition.add(0.5, 0.5, 0.5)));
                    mc.player.rotationYaw = angle[0];
                    mc.player.rotationPitch = angle[1];
                }
            }
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketHeldItemChange) {
            if (strict.getValue()) {
                mineDamage = 0;
            }
        }
    }

    private int getTool(final BlockPos pos) {
        int index = -1;
        float CurrentFastest = 1.0f;
        for (int i = 0; i < 9; ++i) {
            final ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack != ItemStack.EMPTY) {
                final float digSpeed = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, stack);
                final float destroySpeed = stack.getDestroySpeed(mc.world.getBlockState(pos));
                if (digSpeed + destroySpeed > CurrentFastest) {
                    CurrentFastest = digSpeed + destroySpeed;
                    index = i;
                }
            }
        }
        return index;
    }

    private ItemStack getTool2(final IBlockState pos) {
        ItemStack itemStack = null;
        float CurrentFastest = 1.0f;
        for (int i = 0; i < 9; ++i) {
            final ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack != ItemStack.EMPTY) {
                final float digSpeed = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, stack);
                final float destroySpeed = stack.getDestroySpeed(pos);

                if (digSpeed + destroySpeed > CurrentFastest) {
                    CurrentFastest = digSpeed + destroySpeed;
                    itemStack = stack;
                }
            }
        }
        return itemStack;
    }

    private boolean canBreak(BlockPos pos) {
        final IBlockState blockState = mc.world.getBlockState(pos);
        final Block block = blockState.getBlock();
        return block.getBlockHardness(blockState, mc.world, pos) != -1;
    }

    public enum Mode {
        Packet, Damage, Creative, NexusGrief
    }

    public float getPow2Value(Setting<Float> val) {
        return val.getValue() * val.getValue();
    }
    public int getMineProgColor(float progress) {
        int sa = this.startAlpha.getValue();
        if (progress <= 0.18) return sa;
        if (progress <= 0.3) return sa + 27;
        if (progress <= 0.44) return sa + 39;
        if (progress <= 0.56) return sa + 50;
        if (progress <= 0.7) return sa + 67;
        if (progress <= 0.81) return sa + 79;
        if (progress <= 0.9) return sa + 86;
        if (progress <= 0.97) return sa + 94;
        return sa + 110;
    }
}

