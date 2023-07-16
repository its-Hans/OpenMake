package cn.make.module.misc;

import chad.phobos.api.events.block.SelfDamageBlockEvent;
import cn.make.module.combat.CivSelect;
import cn.make.util.BlockChecker;
import cn.make.util.UtilsRewrite;
import chad.phobos.api.events.render.Render3DEvent;
import chad.phobos.api.center.Module;
import chad.phobos.api.setting.Setting;
import chad.phobos.api.utils.Timer;
import net.minecraft.block.BlockAir;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

public class AutoPlace
extends Module {
    BlockPos select = null;
    Timer timer = new Timer();

    @Override
    public void onDisable() {
        resets();
    }

    public AutoPlace() {
        super("SelectPlacer", "n+1", Category.MISC, true, false, false);
    }
    enum BlockMode {
        Shulker,
        EnderChest
    }
    public Setting<BlockMode> blockMode = rother("BlockMode", BlockMode.Shulker);
    public Setting<Boolean> rotate = rbool("Rotate", true);
    public Setting<Boolean> packet = rbool("Packet", false);
    public Setting<Boolean> swapBack = rbool("SwapBack", true);
    public Setting<Integer> delay = rinte("Delay", 10, 0, 500);
    public Setting<Boolean> render = rbool("Render", true);
    public Setting<Integer> red = rinte("Red", 0, 0, 255, v -> render.getValue());
    public Setting<Integer> green = rinte("Green", 0, 0, 255, v -> render.getValue());
    public Setting<Integer> blue = rinte("Blue", 150, 0, 255, v -> render.getValue());
    public Setting<Integer> alpha = rinte("Alpha", 240, 0, 255, v -> render.getValue());
    @Override
    public void onUpdate() {
        if (timer.passedMs(delay.getValue())) {
            timer.reset();
        } else {
            return;
        }
        if (fullNullCheck()) {
            this.disable();
            return;
        }
        if (select == null) {
            return;
        }
        if (!(UtilsRewrite.uBlock.getBlock(select) instanceof BlockAir)) {
            return;
        }
        int old = mc.player.inventory.currentItem;
        int slot = findBlock(blockMode.getValue());
        if (slot != -1) {
            UtilsRewrite.uInventory.heldItemChange(slot, true, false, true);
            UtilsRewrite.uBlock.placeUseSlotNoSwing(select, null, rotate.getValue(), packet.getValue(), slot);
            if (swapBack.getValue()) UtilsRewrite.uInventory.heldItemChange(old, true, false, true);
        }
    }

    public Color getColor() {
        return new Color(red.getValue(), green.getValue(), blue.getValue());
    }
    @Override
    public void onRender3D(Render3DEvent event) {
        if (!render.getValue()) return;
        if (select != null) {
            CivSelect.renderc.drawProgressBB(
                progress,
                select,
                getColor(),
                alpha.getValue(),
                reverse
            );
        }
    }

    public int findBlock(final BlockMode block) {
        for (int x = 0; x < 9; ++x) {
            Item item = mc.player.inventory.getStackInSlot(x).getItem();
            switch (block) {
                case Shulker: {
                    if (item instanceof ItemShulkerBox) return x;
                    break;
                }
                case EnderChest: {
                    if (item == Item.getItemFromBlock(Blocks.ENDER_CHEST)) return x;
                    break;
                }
            }
        }

        return -1;
    }
    @SubscribeEvent
    public void onDmgBlock(SelfDamageBlockEvent.port2 event) {
        select = event.pos;
    }

    boolean reverse = false;
    int progress = 0;
    public void resets() {
        timer.reset();
        select = null;
        progress = 0;
    }
    @Override
    public void onTick() {
        if (select != null) {
            if (reverse) { // 如果反向渲染为true
                if (progress > 0) { // 如果progress大于0
                    progress--; // 减少progress
                } else { // 如果progress等于0
                    reverse = false; // 反转reverse变量
                }
            } else { // 如果反向渲染为false
                if (progress < 100) { // 如果progress小于100
                    progress++; // 增加progress
                } else { // 如果progress等于100
                    reverse = true; // 反转reverse变量
                }
            }
        } else {
            resets();
        }
    }
    @Override
    public String getDisplayInfo() {
        if (select == null) return "waiting";
        return BlockChecker.simpleXYZString(select);
    }
}

