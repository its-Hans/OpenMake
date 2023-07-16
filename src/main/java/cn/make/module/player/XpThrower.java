package cn.make.module.player;

import chad.phobos.Client;
import chad.phobos.api.center.Module;
import chad.phobos.api.setting.Bind;
import chad.phobos.api.setting.Setting;
import cn.make.util.skid.EntityUtil;
import cn.make.util.skid.InventoryUtil;
import cn.make.util.skid.RotationUtil;
import cn.make.util.skid.Timer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

import java.util.ArrayList;
import java.util.Comparator;

public class XpThrower extends Module
{
    Setting<Boolean> feetThrow = rbool("FeetThrow", true);
    Setting<Integer> throwSpeed = rinte("ThorwDelay", 20, 1, 1000);
    Setting<Bind> throwKey = rbind("ThrowKey", new Bind(0));
    Setting<Boolean> stopXp = rbool("StopXP", true);
    Setting<Boolean> noLowDurable = rbool("NoLowDurable", true, v -> this.stopXp.getValue());
    Setting<Integer> stopDurable = rinte("StopDurable", 100, 0, 100, v -> this.stopXp.getValue());
    Setting<Boolean> autoXp = rbool("AutoThrowXp", false);
    Setting<Integer> autoXpRange = rinte("AutoXpRange", 8, 0, 20, v -> this.autoXp.getValue());
    Setting<Boolean> takeoffArmor = rbool("TakeoffArmor", true, v -> this.stopXp.getValue());
    Setting<Integer> takeoffDurable = rinte("TakeoffDurable", 100, 0, 100, v -> this.takeoffArmor.getValue());
    Setting<Integer> takeoffDelay = rinte("Delay", 0, 0, 5, v -> this.takeoffArmor.getValue());

    Timer timer = new Timer();
    int delay_count;
    
    public XpThrower() {
        super("XpThrower", "AutoXp by KijinSeija", Category.PLAYER, true, false, false);
    }
    
    @Override
    public void onUpdate() {
        if (!this.throwKey.getValue().isDown() || !this.timer.passedDms(this.throwSpeed.getValue())) {
            if (this.autoXp.getValue() && !this.isRangeNotPlayer(this.autoXpRange.getValue())) {
                return;
            }
            if (!this.autoXp.getValue()) {
                return;
            }
        }
        if (this.stopXp.getValue() && this.stopDurable.getValue() <= this.getArmorDurable(this.noLowDurable.getValue())) {
            return;
        }
        final int XpSlot = InventoryUtil.getItemHotbar(Items.EXPERIENCE_BOTTLE);
        if (XpSlot == -1) {
            return;
        }
        if (this.takeoffArmor.getValue()) {
            this.takeArmorOff();
        }
        final int oldSlot = XpThrower.mc.player.inventory.currentItem;
        if (this.feetThrow.getValue()) {
            final float yaw = XpThrower.mc.player.cameraYaw;
            RotationUtil.faceYawAndPitch(yaw, 90.0f);
        }
        InventoryUtil.switchToHotbarSlot(XpSlot, false);
        XpThrower.mc.playerController.processRightClick(XpThrower.mc.player, XpThrower.mc.world, EnumHand.MAIN_HAND);
        InventoryUtil.switchToHotbarSlot(oldSlot, false);
    }
    
    public Double getArmorDurable(final boolean getLowestValue) {
        final ArrayList<Double> DurableList = new ArrayList<>();
        for (int i = 5; i <= 8; ++i) {
            final ItemStack armor = XpThrower.mc.player.inventoryContainer.getInventory().get(i);
            final double max_dam = armor.getMaxDamage();
            final double dam_left = armor.getMaxDamage() - armor.getItemDamage();
            final double percent = dam_left / max_dam * 100.0;
            DurableList.add(percent);
        }
        DurableList.sort(Comparator.naturalOrder());
        if (getLowestValue) {
            return DurableList.get(0);
        }
        return DurableList.get(DurableList.size() - 1);
    }
    
    private Boolean isRangeNotPlayer(final double range) {
        EntityPlayer target = null;
        double distance = range;
        for (final EntityPlayer player : XpThrower.mc.world.playerEntities) {
            if (!EntityUtil.isntValid(player, range) && !Client.friendManager.isFriend(player.getName())) {
                if (XpThrower.mc.player.posY - player.posY >= 5.0) {
                    continue;
                }
                if (target != null) {
                    if (EntityUtil.mc.player.getDistanceSq(player) >= distance) {
                        continue;
                    }
                }
                target = player;
                distance = EntityUtil.mc.player.getDistanceSq(player);
            }
        }
        return target == null;
    }
    
    private ItemStack getArmor(final int first) {
        return XpThrower.mc.player.inventoryContainer.getInventory().get(first);
    }
    
    private void takeArmorOff() {
        for (int slot = 5; slot <= 8; ++slot) {
            final ItemStack item = this.getArmor(slot);
            final double max_dam = item.getMaxDamage();
            final double dam_left = item.getMaxDamage() - item.getItemDamage();
            final double percent = dam_left / max_dam * 100.0;
            if (percent >= this.takeoffDurable.getValue() && item.getItem() != Items.AIR) {
                if (InventoryUtil.findItemInventorySlot(Items.AIR, false) == -1) {
                    return;
                }
                if (this.delay_count < this.takeoffDelay.getValue()) {
                    ++this.delay_count;
                    return;
                }
                this.delay_count = 0;
                XpThrower.mc.playerController.windowClick(0, slot, 0, ClickType.QUICK_MOVE, XpThrower.mc.player);
            }
        }
    }
    
    @Override
    public void onEnable() {
        this.delay_count = 0;
    }
}
