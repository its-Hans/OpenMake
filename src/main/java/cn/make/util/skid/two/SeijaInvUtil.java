package cn.make.util.skid.two;

import cn.make.util.skid.InventoryUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;

public class SeijaInvUtil
{
    Minecraft mc;
    
    public SeijaInvUtil() {
        this.mc = Minecraft.getMinecraft();
    }
    
    public static int switchToItem(final Item itemIn) {
        final int slot = InventoryUtil.getItemHotbar(itemIn);
        if (slot == -1) {
            return -1;
        }
        InventoryUtil.switchToHotbarSlot(slot, false);
        return slot;
    }
}
