package cn.make.module.combat;

import cn.make.Targets;
import cn.make.module.player.CatBurrow;
import cn.make.util.BlockChecker;
import chad.phobos.Client;
import chad.phobos.api.center.Module;
import chad.phobos.api.setting.Setting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.BlockPistonExtension;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.List;

public class AutoBurrow extends Module {
   public EntityPlayer target = null;
   private final List<Block> hardBlock = Arrays.asList(Blocks.OBSIDIAN, Blocks.BEDROCK);

   public AutoBurrow() {
      super("AutoBurrow", "LOL", Category.COMBAT);
   }
   private final Setting<Boolean> push = this.register(new Setting<>("OnPush", true));
   private final Setting<Boolean> pull = this.register(new Setting<>("OnPull", true));
   private final Setting<Boolean> hole = this.register(new Setting<>("OnHole", true));
   private final Setting<Boolean> debug = this.register(new Setting<>("Debug", true));
   private final Setting<BurrowType> burtype = this.register(new Setting<>("BurrowType", BurrowType.Cat3));
   private final Setting<String> moduleNameEarth = this.register(new Setting<>("ModuleName3ar", "BurrowY", v -> burtype.getValue() == BurrowType.Earth));
   private final Setting<String> modulePrefixEarth = this.register(new Setting<>("ModulePrefix3ar", "!", v -> burtype.getValue() == BurrowType.Earth));

   private boolean mayBurrow() {
      if (fullNullCheck()) return false;

      EntityPlayer target = Targets.getTarget();
      if (target == null) {
         if (debug.getValue()) sendModuleMessage("no target");
         return false;
      }

      BlockPos burrowpos = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
      if (mc.world.getBlockState(burrowpos).getBlock() == Blocks.OBSIDIAN | mc.world.getBlockState(burrowpos.add(0, 0.4, 0)).getBlock() == Blocks.ENDER_CHEST) {
         if (debug.getValue()) sendModuleMessage("on burrow: " + BlockChecker.simpleXYZString(burrowpos));
         return false;
      }

      if (push.getValue()) {
         List<BlockPos> phList = Arrays.asList(
             burrowpos.east(),
             burrowpos.west(),
             burrowpos.north(),
             burrowpos.south()
         );
         for (BlockPos PistonHead : phList) {
            if (BlockChecker.getBlockType(PistonHead) instanceof BlockPistonExtension)
               return true;
         }
         if (debug.getValue()) sendModuleMessage("no feet pistonhead found");
      }
      if (pull.getValue()) {
         BlockPos piston = burrowpos.down();
         if (BlockChecker.getBlockType(piston) instanceof BlockPistonBase)
            return true;
         if (debug.getValue()) sendModuleMessage("no downpos pistonbase found");
      }
      if (hole.getValue()) {
         int hardblocks = 0;
         List<BlockPos> surroundList = Arrays.asList(
             burrowpos.east(),
             burrowpos.west(),
             burrowpos.north(),
             burrowpos.south()
         );
         for (BlockPos surblock : surroundList) {
            if (hardBlock.contains(BlockChecker.getBlockType(surblock)))
               hardblocks++;
         }
         if (hardblocks >= 4) return true;
         else if (debug.getValue()) sendModuleMessage("not onhole");
      }
      return false;
   }
   @Override
   public void onUpdate() {
      if (!mayBurrow()) return;
      switch (burtype.getValue()) {
         case SexyBurrow: {
            Client.moduleManager.getModuleByClass(SexyBurrow.class).enable();
         }
         case Earth: {
            AutoBurrow.mc.player.connection.sendPacket(new CPacketChatMessage(modulePrefixEarth.getValue() + moduleNameEarth.getValue() + " enabled true"));
         }
         case Cat3: {
            Client.moduleManager.getModuleByClass(CatBurrow.class).enable();
         }
      }
   }
   enum BurrowType{Earth, SexyBurrow, Cat3}
}
