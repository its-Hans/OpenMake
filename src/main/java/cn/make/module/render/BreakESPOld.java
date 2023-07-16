
package cn.make.module.render;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import cn.make.util.skid.FadeUtils;
import chad.phobos.api.events.render.Render3DEvent;
import chad.phobos.api.center.Module;
import chad.phobos.api.setting.Setting;
import chad.phobos.api.utils.RenderUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BreakESPOld extends Module {
   public Setting<Integer> range = this.register(new Setting<>("Range", 20, 1, 200));
   BreakESPOld.BreakESPFade fade = new BreakESPOld.BreakESPFade(200);
   private final Setting<Integer> boxRed;
   private final Setting<Integer> outlineGreen;
   private final Setting<Integer> boxGreen;
   private final Setting<Boolean> box = new Setting<>("Box", true);
   private final Setting<Boolean> cOutline;
   private final Setting<Integer> outlineBlue;
   private final Setting<Integer> boxAlpha;
   private final Setting<Float> outlineWidth;
   private final Setting<Integer> outlineRed;
   private final Setting<Boolean> outline;
   private final Setting<Integer> boxBlue;
   private final Setting<Integer> outlineAlpha;
   private final Map<EntityPlayer, BlockPos> burrowedPlayers;

   public BreakESPOld() {
      super("BreakESPOld", "Speeds up mining.", Module.Category.RENDER, true, false, false);
      this.boxRed = this.register(new Setting<>("BoxRed", 255, 0, 255, v -> this.box.getValue()));
      this.boxGreen = this.register(new Setting<>("BoxGreen", 255, 0, 255, v -> this.box.getValue()));
      this.boxBlue = this.register(new Setting<>("BoxBlue", 255, 0, 255, v -> this.box.getValue()));
      this.boxAlpha = this.register(new Setting<>("BoxAlpha", 125, 0, 255, v -> this.box.getValue()));
      this.outline = this.register(new Setting<>("Outline", true));
      this.outlineWidth = this.register(new Setting<>("OutlineWidth", 1.0F, 0.0F, 5.0F, v -> this.outline.getValue()));
      this.cOutline = this.register(new Setting<>("CustomOutline", false, v -> this.outline.getValue()));
      this.outlineRed = this.register(new Setting<>("OutlineRed", 255, 0, 255, v -> this.cOutline.getValue()));
      this.outlineGreen = this.register(new Setting<>("OutlineGreen", 255, 0, 255, v -> this.cOutline.getValue()));
      this.outlineBlue = this.register(new Setting<>("OutlineBlue", 255, 0, 255, v -> this.cOutline.getValue()));
      this.outlineAlpha = this.register(new Setting<>("OutlineAlpha", 255, 0, 255, v -> this.cOutline.getValue()));
      this.burrowedPlayers = new HashMap();
   }

   @Override
   public void onRender3D(Render3DEvent render3DEvent) {
      if (mc.player != null && mc.world != null) {
         mc.renderGlobal
            .damagedBlocks
            .forEach(
               (integer, destroyBlockProgress) -> {
                  if (destroyBlockProgress != null) {
                     BlockPos blockPos = destroyBlockProgress.getPosition();
                     if (mc.world.getBlockState(blockPos).getBlock() == Blocks.AIR) {
                        if (blockPos != null) {
                           RenderUtil.drawBoxESP(
                              blockPos,
                              new Color(this.boxRed.getValue(), this.boxGreen.getValue(), this.boxBlue.getValue(), this.boxAlpha.getValue()),
                              true,
                              new Color(this.outlineRed.getValue(), this.outlineGreen.getValue(), this.outlineBlue.getValue(), this.outlineAlpha.getValue()),
                              this.outlineWidth.getValue(),
                              this.outline.getValue(),
                              this.box.getValue(),
                              this.boxAlpha.getValue(),
                              true
                           );
                        }
      
                        if (!this.burrowedPlayers.isEmpty()) {
                           this.burrowedPlayers.entrySet().forEach(this::lambda$onRender3D$8);
                        }
      
                        return;
                     }
      
                     if (blockPos.getDistance((int)mc.player.posX, (int)mc.player.posY, (int)mc.player.posZ)
                        <= (double)this.range.getValue().intValue()) {
                        int progress = destroyBlockProgress.getPartialBlockDamage();
                        this.fade.setNewProgress((double)progress);
                        RenderUtil.drawBoxESP(
                           blockPos,
                           new Color(this.boxRed.getValue(), this.boxGreen.getValue(), this.boxBlue.getValue(), this.boxAlpha.getValue()),
                           true,
                           new Color(this.outlineRed.getValue(), this.outlineGreen.getValue(), this.outlineBlue.getValue(), this.outlineAlpha.getValue()),
                           this.outlineWidth.getValue(),
                           this.outline.getValue(),
                           this.box.getValue(),
                           this.boxAlpha.getValue(),
                           true
                        );
                        if (!this.burrowedPlayers.isEmpty()) {
                           this.burrowedPlayers.entrySet().forEach(this::lambda$onRender3D$8);
                        }
                     }
                  }
               }
            );
      }
   }

   @SubscribeEvent
   public void onWorldRender(RenderWorldLastEvent event) {
      mc.renderGlobal.damagedBlocks.forEach((integer, destroyBlockProgress) -> {
         if (destroyBlockProgress != null) {
            BlockPos blockPos = destroyBlockProgress.getPosition();
            int progress = destroyBlockProgress.getPartialBlockDamage();
            if (!this.burrowedPlayers.isEmpty()) {
               this.burrowedPlayers.entrySet().forEach(this::lambda$onRender3D$8);
            }
         }
      });
   }

   @Override
   public void onUpdate() {
      if (!fullNullCheck()) {
         this.burrowedPlayers.clear();
      }
   }

   private void lambda$onRender3D$8(Entry entry) {
      this.renderBurrowedBlock((BlockPos)entry.getValue());
   }

   private void renderBurrowedBlock(BlockPos blockPos) {
      RenderUtil.drawBoxESP(
         blockPos,
         new Color(this.boxRed.getValue(), this.boxGreen.getValue(), this.boxBlue.getValue(), this.boxAlpha.getValue()),
         true,
         new Color(this.outlineRed.getValue(), this.outlineGreen.getValue(), this.outlineBlue.getValue(), this.outlineAlpha.getValue()),
         this.outlineWidth.getValue(),
         this.outline.getValue(),
         this.box.getValue(),
         this.boxAlpha.getValue(),
         true
      );
   }

   private static class BreakESPFade {
      private final FadeUtils fade;
      private double lastProgress = 0.0;
      private double newProgress = 0.0;

      public BreakESPFade(int smoothLength) {
         this.fade = new FadeUtils((long)smoothLength);
      }

      public void setNewProgress(double progress) {
         if (progress != this.newProgress) {
            this.lastProgress = this.newProgress;
            this.newProgress = progress;
            this.fade.reset();
         }
      }

      public double getRenderSize() {
         if (this.lastProgress == 0.0 || this.newProgress == 0.0) {
            return 0.0;
         } else if (this.newProgress == 10.0) {
            return 0.0;
         } else {
            double Nprogress = 0.1 * this.newProgress;
            double Lprogress = 0.1 * this.lastProgress;
            double maxP = Math.max(Nprogress, Lprogress);
            double minP = Math.min(Nprogress, Lprogress);
            return minP + (maxP - minP) * this.fade.easeOutQuad();
         }
      }
   }

   public static enum Mode {
      Outline,
      ECHEST;
   }

   public static enum Modee {
      Fill,
      Box;
   }

   public static enum String {
      OOutTOIn,
      OutTOIn;
   }
}
