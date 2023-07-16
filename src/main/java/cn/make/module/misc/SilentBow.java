package cn.make.module.misc;

import chad.phobos.api.center.Module;
import cn.make.util.UtilsRewrite;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumHand;

public class SilentBow extends Module {
    int slot;

    public SilentBow() {
        super("SilentBow", "sb", Category.MISC);
    }

    @Override
    public void onEnable() {
        int bowslot = getBowAtHotbar();
        slot = mc.player.inventory.currentItem;
        if (bowslot != -1) {
            UtilsRewrite.uInventory.heldItemChange(bowslot, true, false, true);
        } else {
            sendModuleMessage("У тебя лука в хотбаре нема, дуранчеус");
            disable();
        }
    }


    @Override
    public void onTick() {
        if (fullNullCheck()) {
            disable();
            return;
        }
        if (
            mc.player.inventory.getCurrentItem().getItem() instanceof ItemBow
                && mc.player.isHandActive()
                && mc.player.getItemInUseMaxCount() >= 3
        ) {
            mc.playerController.onStoppedUsingItem(mc.player);
            UtilsRewrite.uInventory.heldItemChange(slot, true, false, true);
            disable();
        }
    }
    public int getBowAtHotbar() {
        Item item = Items.BOW;
        for (int i = 0; i < 9; ++i) {
            ItemStack itemStack = mc.player.inventory.getStackInSlot(i);
            if (!(itemStack.getItem() == item)) continue;
            return i;
        }
        return -1;
    }

}
