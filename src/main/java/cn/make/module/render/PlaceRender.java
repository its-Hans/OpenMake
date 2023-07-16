package cn.make.module.render;

import java.awt.Color;
import java.util.HashMap;

import cn.make.util.skid.FadeUtils;
import cn.make.util.skid.RebirthUtil;
import chad.phobos.api.events.render.Render3DEvent;
import chad.phobos.api.center.Module;
import chad.phobos.api.setting.Setting;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class PlaceRender extends Module {
   public static HashMap<BlockPos, PlaceRender.placePosition> PlaceMap = new HashMap<>();
   public static PlaceRender INSTANCE;
   private final Setting<Integer> red = register(new Setting<Integer>("Red", 255, 1, 255));
   private final Setting<Integer> green = register(new Setting<Integer>("Green", 255, 1, 255));
   private final Setting<Integer> blue = register(new Setting<Integer>("Blue", 255, 1, 255));
   private final Setting<Integer> alpha = register(new Setting<Integer>("Alpha", 100, 1, 255));
   private final Setting<Integer> animationTime = register(new Setting<>("animationTime", 1000, 0, 5000));
   private final Setting<Boolean> outline = this.register(new Setting<>("Outline", false));
   private final Setting<Boolean> box = this.register(new Setting<>("Box", true));

   public PlaceRender() {
      super("PlaceRender", "rebirth", Category.RENDER);
      INSTANCE = this;
   }

   @Override
   public void onEnable() {
      PlaceMap.clear();
   }
   @Override
   public void onDisable() {
      PlaceMap.clear();
   }

   public Color getNowColor() {
      return new Color(red.getValue(), green.getValue(), blue.getValue(), alpha.getValue());
   }
   static int getAnimatime() {
      return PlaceRender.INSTANCE.animationTime.getValue();
   }

   private void drawBlock(BlockPos drawPos, double fade, Color color) {
      AxisAlignedBB axis = mc.world.getBlockState(drawPos).getSelectedBoundingBox(mc.world, drawPos);
      if (this.outline.getValue()) {
         RebirthUtil.RenderUtil.drawBBBox(axis, color, (int)((double)color.getAlpha() * -fade));
      }
      if (this.box.getValue()) {
         RebirthUtil.RenderUtil.drawBoxESP(drawPos, color, (int)((double)color.getAlpha() * -fade));
      }
   }

   @Override
   public void onRender3D(Render3DEvent event) {
      boolean doclear = true;
      for(PlaceRender.placePosition var4 : PlaceMap.values()) {
         if (!(var4.firstFade.easeOutQuad() == 1.0)) {
            doclear = false;
            this.drawBlock(var4.pos, var4.firstFade.easeOutQuad() - 1.0, var4.posColor);
         }
      }

      if (doclear) {
         PlaceMap.clear();
      }
   }

   public static class placePosition {
      public Color posColor;
      public BlockPos pos;
      public final FadeUtils firstFade = new FadeUtils((long)(getAnimatime()));

      public placePosition(BlockPos placepos) {
         this.pos = placepos;
         this.posColor = PlaceRender.INSTANCE.getNowColor();
      }
   }
   public static void putMap(BlockPos pos) {
      if (PlaceRender.INSTANCE.isDisabled()) return;
      PlaceRender.PlaceMap.put(pos, new placePosition(pos));
   }
}
