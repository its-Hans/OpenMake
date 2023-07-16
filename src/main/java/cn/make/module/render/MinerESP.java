//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package cn.make.module.render;

import cn.make.module.misc.InstantMine;
import chad.phobos.Client;
import chad.phobos.api.events.render.Render3DEvent;
import chad.phobos.api.center.Module;
import chad.phobos.api.setting.Setting;
import chad.phobos.api.utils.Timer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.Objects;

public class MinerESP extends Module {
   private static MinerESP INSTANCE;
   BlockPos pos;
   private final Setting<Integer> boxRed;
   private final Setting<Integer> outlineGreen;
   private final Setting<Integer> boxGreen;
   private final Setting<Boolean> box;
   private final Setting<Boolean> cOutline;
   private final Setting<Integer> outlineBlue;
   private final Setting<Integer> boxAlpha;
   private final Setting<Float> outlineWidth;
   private final Setting<Integer> outlineRed;
   private final Setting<Boolean> outline;
   private final Setting<Integer> boxBlue;
   private final Setting<Integer> outlineAlpha;
   public static char SECTIONSIGN = 167;
   public static String GREEN = SECTIONSIGN + "a";
   protected Setting<Double> m_sRange = this.register(new Setting<>("Range", 10.0, 1.0, 20.0));
   private final Setting<Boolean> Break = this.register(new Setting<>("CheckMine", true));
   private final Setting<Boolean> potion = this.register(new Setting<>("CheckMinePotion", true));
   private final Setting<Boolean> checkSelfMine = this.register(new Setting<>("CheckSelfMine", false));

   public final Timer imerS;
   double manxi;

   public MinerESP() {
      super("MinerESP", "Show enemy's break packet.", Module.Category.RENDER, true, false, false);
      this.box = new Setting<>("Box", true);
      this.boxRed = this.register(new Setting<>("BoxRed", 255, 0, 255, var1 -> this.box.getValue()));
      this.boxGreen = this.register(new Setting<>("BoxGreen", 255, 0, 255, var1 -> this.box.getValue()));
      this.boxBlue = this.register(new Setting<>("BoxBlue", 255, 0, 255, var1 -> this.box.getValue()));
      this.boxAlpha = this.register(new Setting<>("BoxAlpha", 0, 0, 255, var1 -> this.box.getValue()));
      this.outline = this.register(new Setting<>("Outline", true));
      this.outlineWidth = this.register(new Setting<>("OutlineWidth", 1.5F, 0.0F, 5.0F, var1 -> this.outline.getValue()));
      this.cOutline = this.register(new Setting<>("CustomOutline", true, var1 -> this.outline.getValue()));
      this.outlineRed = this.register(new Setting<>("OutlineRed", 255, 0, 255, var1 -> this.cOutline.getValue()));
      this.outlineGreen = this.register(new Setting<>("OutlineGreen", 255, 0, 255, var1 -> this.cOutline.getValue()));
      this.outlineBlue = this.register(new Setting<>("OutlineBlue", 255, 0, 255, var1 -> this.cOutline.getValue()));
      this.outlineAlpha = this.register(new Setting<>("OutlineAlpha", 255, 0, 255, var1 -> this.cOutline.getValue()));
      this.imerS = new Timer();
      this.manxi = 0.0;
      this.setInstance();
   }

   public static MinerESP getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new MinerESP();
      }

      return INSTANCE;
   }

   @Override
   public void onRender3D(Render3DEvent var1) {
      if (this.pos != null) {
         if (this.imerS.passedMs(15L)) {
            if (this.manxi <= 10.0) {
               this.manxi += 0.11;
            }

            this.imerS.reset();
         }

         if (this.potion.getValue()) {
            if (this.checkSelfMine.getValue() && InstantMine.breakPos != null && InstantMine.breakPos.equals(new BlockPos(this.pos))) {
               return;
            }

            AxisAlignedBB var2 = mc.world.getBlockState(this.pos).getSelectedBoundingBox(mc.world, this.pos);
            double var3 = var2.minX + (var2.maxX - var2.minX) / 2.0;
            double var5 = var2.minY + (var2.maxY - var2.minY) / 2.0;
            double var7 = var2.minZ + (var2.maxZ - var2.minZ) / 2.0;
            double var9 = this.manxi * (var2.maxX - var3) / 10.0;
            double var11 = this.manxi * (var2.maxY - var5) / 10.0;
            double var13 = this.manxi * (var2.maxZ - var7) / 10.0;
            AxisAlignedBB var15 = new AxisAlignedBB(var3 - var9, var5 - var11, var7 - var13, var3 + var9, var5 + var11, var7 + var13);
            int var16 = this.pos.getY();
            chad.phobos.api.utils.RenderUtil.drawBoxESP(
               this.pos,
               new Color(this.boxRed.getValue(), this.boxGreen.getValue(), this.boxBlue.getValue(), this.boxAlpha.getValue()),
               true,
               new Color(this.outlineRed.getValue(), this.outlineGreen.getValue(), this.outlineBlue.getValue(), this.outlineAlpha.getValue()),
               this.outlineWidth.getValue(),
               this.outline.getValue(),
               this.box.getValue(),
               this.boxAlpha.getValue(),
               true
            );
            RenderUtil.drawText(var15.getCenter().add(0.0, 0.15, 0.0), "Unknown ID");
            RenderUtil.drawText(var15.getCenter().add(0.0, -0.15, 0.0), GREEN + "Miner...");
         }

         if (this.Break.getValue()) {
            mc.renderGlobal
               .damagedBlocks
               .forEach(
                  (var1x, var2x) -> {
                     if (var2x != null) {
                        BlockPos var3x = var2x.getPosition();
                        if (mc.world.getBlockState(var3x).getBlock() == Blocks.AIR) {
                           return;
                        }
      
                        if (var3x.getDistance((int)mc.player.posX, (int)mc.player.posY, (int)mc.player.posZ)
                           <= this.m_sRange.getValue()) {
                           int var4 = var2x.getPartialBlockDamage();
                           AxisAlignedBB var5x = mc.world.getBlockState(var3x).getSelectedBoundingBox(mc.world, var3x);
      
                           try {
                              this.renderESP(
                                 var5x,
                                 var3x,
                                 (float)var4,
                                 ((Entity)Objects.requireNonNull(mc.world.getEntityByID(var2x.miningPlayerEntId))).getName()
                              );
                           } catch (Exception var7x) {
                           }
                        }
                     }
                  }
               );
         }
      }
   }

   private void setInstance() {
      INSTANCE = this;
   }

   @SubscribeEvent
   public void BrokenBlock(PlaySoundEvent var1) {
      if (!this.checkSelfMine.getValue()
         || InstantMine.breakPos == null
         || !InstantMine.breakPos
            .equals(new BlockPos((double)var1.getSound().getXPosF(), (double)var1.getSound().getYPosF(), (double)var1.getSound().getZPosF()))) {
         if (var1.getName().endsWith("hit")) {
            if (!var1.getName().endsWith("arrow.hit")) {
               if (!var1.getName().endsWith("stand.hit")) {
                  if (this.pos == null
                     || !this.pos
                        .equals(
                           new BlockPos(
                              (double)var1.getSound().getXPosF(), (double)var1.getSound().getYPosF(), (double)var1.getSound().getZPosF()
                           )
                        )) {
                     if (mc.world
                           .getBlockState(
                              new BlockPos(
                                 (double)var1.getSound().getXPosF(), (double)var1.getSound().getYPosF(), (double)var1.getSound().getZPosF()
                              )
                           )
                           .getBlock()
                        != Blocks.BEDROCK) {
                        this.pos = new BlockPos(
                           (double)var1.getSound().getXPosF(), (double)var1.getSound().getYPosF(), (double)var1.getSound().getZPosF()
                        );
                        this.manxi = 0.0;
                     }
                  }
               }
            }
         }
      }
   }

   @SubscribeEvent
   public void BrokenBlock2(PlaySoundEvent var1) {
      if (!fullNullCheck()) {
         if (!this.checkSelfMine.getValue()
            || InstantMine.breakPos == null
            || !InstantMine.breakPos
               .equals(new BlockPos((double)var1.getSound().getXPosF(), (double)var1.getSound().getYPosF(), (double)var1.getSound().getZPosF()))
            )
          {
            if (var1.getName().endsWith("break")) {
               if (!var1.getName().endsWith("potion.break")) {
                  if (this.pos == null
                     || !this.pos
                        .equals(
                           new BlockPos(
                              (double)var1.getSound().getXPosF(), (double)var1.getSound().getYPosF(), (double)var1.getSound().getZPosF()
                           )
                        )) {
                     if (mc.world
                           .getBlockState(
                              new BlockPos(
                                 (double)var1.getSound().getXPosF(), (double)var1.getSound().getYPosF(), (double)var1.getSound().getZPosF()
                              )
                           )
                           .getBlock()
                        != Blocks.BEDROCK) {
                        this.pos = new BlockPos(
                           (double)var1.getSound().getXPosF(), (double)var1.getSound().getYPosF(), (double)var1.getSound().getZPosF()
                        );
                        this.manxi = 0.0;
                     }
                  }
               }
            }
         }
      }
   }

   private void renderESP(AxisAlignedBB var1, BlockPos var2, float var3, String var4) {
      double var5 = var1.minX + (var1.maxX - var1.minX) / 2.0;
      double var7 = var1.minY + (var1.maxY - var1.minY) / 2.0;
      double var9 = var1.minZ + (var1.maxZ - var1.minZ) / 2.0;
      double var11 = var1.maxX - var5;
      double var13 = var11 * (double)var3;
      double var15 = var11 * (double)var3;
      double var17 = var11 * (double)var3;
      AxisAlignedBB var19 = new AxisAlignedBB(var5 - var13, var7 - var15, var9 - var17, var5 + var13, var7 + var15, var9 + var17);
      int var20 = var2.getY();
      RenderUtil.drawText(var19.getCenter().add(0.0, 0.15, 0.0), var4);
      RenderUtil.drawText(var19.getCenter().add(0.0, -0.15, 0.0), GREEN + "Miner...");
      chad.phobos.api.utils.RenderUtil.drawBoxESP(
         var2,
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
   static class RenderUtil {


      public static void drawText(BlockPos var0, String var1) {
         GlStateManager.pushMatrix();
         chad.phobos.api.utils.RenderUtil.glBillboardDistanceScaled(
             ((float)var0.getX() + 0.5F),
             ((float)var0.getY() + 0.5F),
             ((float)var0.getZ() + 0.5F),
             mc.player,
             1.0F
         );
         GlStateManager.disableDepth();
         GlStateManager.translate(-((double) Client.textManager.getStringWidth(var1) / 2.0), 0.0, 0.0);
         Client.textManager.drawStringWithShadow(var1, 0.0F, 0.0F, -5592406);
         GlStateManager.popMatrix();
      }

      public static void drawText(Vec3d var0, String var1) {
         GlStateManager.pushMatrix();
         chad.phobos.api.utils.RenderUtil.glBillboardDistanceScaled((float) var0.x, (float) var0.y, (float) var0.z, mc.player, 1.0F);
         GlStateManager.disableDepth();
         GlStateManager.translate(-((double) Client.textManager.getStringWidth(var1) / 2.0), 0.0, 0.0);
         Client.textManager.drawStringWithShadow(var1, 0.0F, 0.0F, -5592406);
         GlStateManager.popMatrix();
      }

      public static void drawText(BlockPos var0, String var1, int var2) {
         GlStateManager.pushMatrix();
         chad.phobos.api.utils.RenderUtil.glBillboardDistanceScaled(
             ((float)var0.getX() + 0.5F),
             ((float)var0.getY() + 0.5F),
             ((float)var0.getZ() + 0.5F),
             mc.player,
             1.0F
         );
         GlStateManager.disableDepth();
         GlStateManager.translate(-((double) Client.textManager.getStringWidth(var1) / 2.0), 0.0, 0.0);
         Client.textManager.drawStringWithShadow(var1, 0.0F, 0.0F, var2);
         GlStateManager.popMatrix();
      }
   }
}
