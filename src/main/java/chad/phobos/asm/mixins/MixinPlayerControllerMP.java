package chad.phobos.asm.mixins;

import chad.phobos.api.events.block.BlockEvent;
import chad.phobos.api.events.block.SelfDamageBlockEvent;
import chad.phobos.api.events.block.ProcessRightClickBlockEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = {PlayerControllerMP.class})
public class MixinPlayerControllerMP {

    @Inject(method = {"clickBlock"}, at = {@At(value = "HEAD")}, cancellable = true)
    private void clickBlockHook(BlockPos pos, EnumFacing face, CallbackInfoReturnable<Boolean> info) {
        BlockEvent event = new BlockEvent(3, pos, face);
        MinecraftForge.EVENT_BUS.post(event);
    }

    @Inject(method = {"onPlayerDamageBlock"}, at = {@At(value = "HEAD")}, cancellable = true)
    private void onPlayerDamageBlockHook(BlockPos pos, EnumFacing face, CallbackInfoReturnable<Boolean> info) {
        BlockEvent event = new BlockEvent(4, pos, face);
        MinecraftForge.EVENT_BUS.post(event);
    }
    @Inject(method={"onPlayerDamageBlock"}, at={@At(value="HEAD")})
    private void onPlayerDamageBlockHooktwo(BlockPos pos, EnumFacing face, CallbackInfoReturnable<Boolean> ci) {
        MinecraftForge.EVENT_BUS.post(new SelfDamageBlockEvent.port1(0,pos,face));
        MinecraftForge.EVENT_BUS.post(new SelfDamageBlockEvent.port2(0,pos,face));
        MinecraftForge.EVENT_BUS.post(new SelfDamageBlockEvent.port3(0,pos,face));
        MinecraftForge.EVENT_BUS.post(new SelfDamageBlockEvent.port4(0,pos,face));
    }

    @Inject(method = {"processRightClickBlock"}, at = {@At(value = "HEAD")}, cancellable = true)
    public void processRightClickBlock(EntityPlayerSP player, WorldClient worldIn, BlockPos pos, EnumFacing direction, Vec3d vec, EnumHand hand, CallbackInfoReturnable<EnumActionResult> cir) {
        ProcessRightClickBlockEvent event = new ProcessRightClickBlockEvent(pos, hand, Minecraft.instance.player.getHeldItem(hand));
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            cir.cancel();
        }
    }
}

