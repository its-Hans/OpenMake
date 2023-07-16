package cn.make.module.player;

import cn.make.util.skid.MathUtil;
import cn.make.util.skid.RotationUtil;
import chad.phobos.Client;
import chad.phobos.api.center.Module;
import chad.phobos.api.setting.Bind;
import chad.phobos.api.setting.Setting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class LoverMending extends Module
{
    public final Setting<Boolean> allowTakeOff;
    private final Setting<Integer> takeOffVal;
    private final Setting<Integer> delay;
    private final Setting<ROTATE> rotate;
    private final Setting<Bind> bind;
    private int delay_count;
    int prvSlot;
    public static Boolean inft;
    public static Bind binds;
    enum ROTATE{a,b,no}
    
    public LoverMending() {
        super("LoverMending", "AutoXP", Category.PLAYER, false, false, false);
        this.allowTakeOff = (Setting<Boolean>)this.register(new Setting("AutoMend", true));
        this.rotate = (Setting<ROTATE>)this.register(new Setting("RotateMode", ROTATE.a));
        this.takeOffVal = (Setting<Integer>)this.register(new Setting("Durable%", 100, 0, 100));
        this.delay = (Setting<Integer>)this.register(new Setting("Delay", 2, 0, 5));
        this.bind = (Setting<Bind>)this.register(new Setting("PacketBind", new Bind(-1)));
    }
    
    private void rotateToPos(final BlockPos pos) {
        final float[] angle = MathUtil.calcAngle(LoverMending.mc.player.getPositionEyes(LoverMending.mc.getRenderPartialTicks()), new Vec3d((double)(pos.getX() + 0.5f), (double)(pos.getY() - 0.5f), (double)(pos.getZ() + 0.5f)));
        Client.rotationManager.setPlayerRotations(angle[0], angle[1]);
    }
    
    public void onEnable() {
        this.delay_count = 0;
    }
    
    public void onUpdate() {
        LoverMending.inft = this.allowTakeOff.getValue();
        LoverMending.binds = this.bind.getValue();
        if (this.findExpInHotbar() == -1) {
            return;
        }
        if (this.bind.getValue().getKey() > -1) {
            if (Keyboard.isKeyDown(this.bind.getValue().getKey()) && LoverMending.mc.currentScreen == null) {
                if (this.findExpInHotbar() == -1) {
                    return;
                }
                this.usedXp();
            }
        }
        else if (this.bind.getValue().getKey() < -1 && Mouse.isButtonDown(convertToMouse(this.bind.getValue().getKey())) && LoverMending.mc.currentScreen == null) {
            if (this.findExpInHotbar() == -1) {
                return;
            }
            this.usedXp();
        }
    }
    
    public static int convertToMouse(final int key) {
        switch (key) {
            case -2: {
                return 0;
            }
            case -3: {
                return 1;
            }
            case -4: {
                return 2;
            }
            case -5: {
                return 3;
            }
            case -6: {
                return 4;
            }
            default: {
                return -1;
            }
        }
    }
    
    private int findExpInHotbar() {
        int slot = 0;
        for (int i = 0; i < 9; ++i) {
            if (LoverMending.mc.player.inventory.getStackInSlot(i).getItem() == Items.EXPERIENCE_BOTTLE) {
                slot = i;
                break;
            }
        }
        return slot;
    }
    
    public void usedXp() {
        this.prvSlot = LoverMending.mc.player.inventory.currentItem;
        LoverMending.mc.player.connection.sendPacket((Packet)new CPacketHeldItemChange(this.findExpInHotbar()));
        switch (this.rotate.getValue()) {
            case a: {
                mc.player.rotationPitch = 90f;
                break;
            }
            case b: {
                RotationUtil.facePos(mc.player.getPosition().down());
                break;
            }
            case no: {
                break;
            }
        }
        LoverMending.mc.player.connection.sendPacket((Packet)new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
        LoverMending.mc.player.connection.sendPacket((Packet)new CPacketHeldItemChange(this.prvSlot));
        if (this.allowTakeOff.getValue()) {
            this.takeArmorOff();
        }
    }
    
    private ItemStack getArmor(final int first) {
        return (ItemStack)LoverMending.mc.player.inventoryContainer.getInventory().get(first);
    }
    
    private void takeArmorOff() {
        for (int slot = 5; slot <= 8; ++slot) {
            final ItemStack item = this.getArmor(slot);
            final double max_dam = item.getMaxDamage();
            final double dam_left = item.getMaxDamage() - item.getItemDamage();
            final double percent = dam_left / max_dam * 100.0;
            if (percent >= this.takeOffVal.getValue() && !item.equals(Items.AIR)) {
                if (!this.notInInv(Items.AIR)) {
                    return;
                }
                if (this.delay_count < this.delay.getValue()) {
                    ++this.delay_count;
                    return;
                }
                this.delay_count = 0;
                LoverMending.mc.playerController.windowClick(0, slot, 0, ClickType.QUICK_MOVE, (EntityPlayer)LoverMending.mc.player);
            }
        }
    }
    
    public Boolean notInInv(final Item itemOfChoice) {
        int n = 0;
        if (itemOfChoice == LoverMending.mc.player.getHeldItemOffhand().getItem()) {
            return true;
        }
        for (int i = 35; i >= 0; --i) {
            final Item item = LoverMending.mc.player.inventory.getStackInSlot(i).getItem();
            if (item == itemOfChoice) {
                return true;
            }
            if (item != itemOfChoice) {
                ++n;
            }
        }
        if (n >= 35) {
            return false;
        }
        return true;
    }
    
    static {
        LoverMending.inft = false;
    }
}
