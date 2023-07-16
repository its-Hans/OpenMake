package cn.make.module.combat;

import cn.make.util.skid.RebirthUtil;
import cn.make.util.skid.Timer;
import chad.phobos.api.center.Module;
import chad.phobos.api.setting.Setting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class RebirthPullCrystal extends Module {
   public static RebirthPullCrystal INSTANCE;
   private EntityPlayer target;
   private final Timer timer;
   public static BlockPos crystalPos;
   public static BlockPos powerPos;

   public RebirthPullCrystal() {
      super("PullCrystal", "use piston pull crystal and boom", Category.COMBAT);
      INSTANCE = this;
      this.target = null;
      this.timer = new Timer();
   }

   private final Setting<Boolean> pistonPacket = this.register(new Setting<>("PistonPacket", false));
   private final Setting<Boolean> onlyGround = this.register(new Setting<>("onlyGround", true));
   private final Setting<Boolean> onlyStatic = this.register(new Setting<>("onlyStatic", true));
   private final Setting<Integer> updateDelay = this.register(new Setting<>("UpdateDelay", 100, 0, 500));
   private final Setting<Boolean> packet = this.register(new Setting<>("Packet", true));
   private final Setting<Float> range = this.register(new Setting<>("Range", 5.0F, 1.0F, 8.0F));
   private final Setting<Boolean> fire = this.register(new Setting<>("Fire", true));
   private final Setting<Boolean> noEating = this.register(new Setting<>("EatingPause", true));
   private final Setting<Boolean> multiPlace = this.register(new Setting<>("MultiPlace", false));
   private final Setting<Boolean> debug = this.register(new Setting<>("debug", true));
   private final Setting<Boolean> fixPlaceCrystal = this.register(new Setting<>("placeFix", true));

   @Override
   public String getDisplayInfo() {
      return this.target != null ? this.target.getName() : null;
   }

   @Override
   public void onUpdate() {
      if (this.timer.passedMs((long) this.updateDelay.getValue())) {
         if (!this.noEating.getValue() || !RebirthUtil.isEating()) {
            boolean var10000 = this.onlyStatic.getValue();
            boolean var10001;
            if (!mc.player.onGround) {
               var10001 = true;
               boolean var10002 = false;
            } else {
               var10001 = false;
            }

            if (!check(var10000, var10001, this.onlyGround.getValue())) {
               this.target = this.getTarget((double)this.range.getValue().floatValue());
               if (this.target == null) {
                  this.target = RebirthUtil.getTarget((double)this.range.getValue().floatValue());
                  if (this.target != null) {
                     this.mineBlock(RebirthUtil.getEntityPos(this.target));
                     var10000 = false;
                  }
               } else {
                  this.timer.reset();
                  var10000 = false;
                  BlockPos var1 = RebirthUtil.getEntityPos(this.target);
                  if (this.checkCrystal(var1.up(0))) {
                     RebirthUtil.attackCrystal(var1.up(0), true, true);
                  }

                  if (this.checkCrystal(var1.up(1))) {
                     RebirthUtil.attackCrystal(var1.up(1), true, true);
                  }

                  if (this.checkCrystal(var1.up(2))) {
                     RebirthUtil.attackCrystal(var1.up(2), true, true);
                  }

                  if (this.checkCrystal(var1.up(3))) {
                     RebirthUtil.attackCrystal(var1.up(3), true, true);
                  }

                  if (!this.doPullCrystal(var1)) {
                     if (!this.doPullCrystal(new BlockPos(this.target.posX + 0.1, this.target.posY + 0.5, this.target.posZ + 0.1))) {
                        if (!this.doPullCrystal(new BlockPos(this.target.posX - 0.1, this.target.posY + 0.5, this.target.posZ + 0.1))) {
                           if (!this.doPullCrystal(new BlockPos(this.target.posX + 0.1, this.target.posY + 0.5, this.target.posZ - 0.1))) {
                              this.doPullCrystal(new BlockPos(this.target.posX - 0.1, this.target.posY + 0.5, this.target.posZ - 0.1));
                              var10000 = false;
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private boolean checkCrystal(BlockPos var1) {
      for(Entity var3 : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(var1))) {
         if (var3 instanceof EntityEnderCrystal) {
            float var4 = RebirthUtil.calculateDamage(var3, this.target);
            if (var4 > 6.0F) {
               return true;
            }
         }

         boolean var10000 = false;
      }

      return false;
   }

   private IBlockState getBlockState(BlockPos var1) {
      return mc.world.getBlockState(var1);
   }

   private boolean placePiston(BlockPos var1, EnumFacing var2, BlockPos var3) {
      return this.placePiston(var1, var2, var3, false) || this.placePiston(var1, var2, var3, true);
   }

   public static boolean check(boolean var0, boolean var1, boolean var2) {
      if (RebirthUtil.isMoving() && var0) {
         return true;
      } else if (var1 && var2) {
         return true;
      } else if (RebirthUtil.findHotbarBlock(Blocks.REDSTONE_BLOCK) == -1) {
         return true;
      } else if (RebirthUtil.findHotbarClass(BlockPistonBase.class) == -1) {
         return true;
      } else {
         boolean var10000;
         if (RebirthUtil.findItemInHotbar(Items.END_CRYSTAL) == -1) {
            var10000 = true;
            boolean var10001 = false;
         } else {
            var10000 = false;
         }

         return var10000;
      }
   }

   public boolean crystal(BlockPos var1) {
      for(Entity var3 : mc.world.loadedEntityList) {
         if (var3 instanceof EntityEnderCrystal) {
            if (!(var3.getDistance((double)var1.getX() + 0.5, (double)var1.getY(), (double)var1.getZ() + 0.5) > 4.0)) {
               RebirthUtil.attackCrystal(var3, true, false);
               return true;
            }

            boolean var10000 = false;
         }
      }

      return false;
   }

   private boolean power(BlockPos var1) {
      for(EnumFacing var5 : EnumFacing.VALUES) {
         if (var5 != EnumFacing.DOWN) {
            if (var5 == EnumFacing.UP) {
               boolean var10000 = false;
            } else {
               int var6 = var1.offset(var5).getX() - var1.getX();
               int var7 = var1.offset(var5).getZ() - var1.getZ();
               if (this.placePower(var1.offset(var5, 1).add(var7, 0, var6), var5, var1)) {
                  return true;
               }

               if (this.placePower(var1.offset(var5, 1).add(-var7, 0, -var6), var5, var1)) {
                  return true;
               }

               if (this.placePower(var1.offset(var5, -1).add(var7, 0, var6), var5, var1)) {
                  return true;
               }

               if (this.placePower(var1.offset(var5, -1).add(-var7, 0, -var6), var5, var1)) {
                  return true;
               }
            }
         }

         boolean var8 = false;
      }

      return false;
   }

   private boolean doPullCrystal(BlockPos var1) {
      if (this.pull(var1.up(2))) {
         return true;
      } else if (this.pull(var1.up())) {
         return true;
      } else if (this.crystal(var1.up())) {
         return true;
      } else if (this.power(var1.up(2))) {
         return true;
      } else if (this.power(var1.up())) {
         return true;
      } else {
         return this.piston(var1.up(2)) ? true : this.piston(var1.up());
      }
   }

   private boolean placePower(BlockPos var1, EnumFacing var2, BlockPos var3, boolean var4) {
      if (var4) {
         var1 = var1.up();
      }

      if (!RebirthUtil.canPlaceCrystal(var3.offset(var2, -1)) && !RebirthUtil.posHasCrystal(var3.offset(var2, -1))) {
         return false;
      } else if (!(this.getBlock(var1) instanceof BlockPistonBase)) {
         return false;
      } else if (((EnumFacing)this.getBlockState(var1).getValue(BlockDirectional.FACING)).getOpposite() != var2) {
         return false;
      } else if (this.getBlock(var1.offset(var2, -1)) == Blocks.PISTON_HEAD || this.getBlock(var1.offset(var2, -1)) == Blocks.PISTON_EXTENSION) {
         return true;
      } else if (!mc.world.isAirBlock(var1.offset(var2, -1))
         && this.getBlock(var1.offset(var2, -1)) != Blocks.PISTON_HEAD
         && this.getBlock(var1.offset(var2, -1)) != Blocks.PISTON_EXTENSION
         && this.getBlock(var1.offset(var2, -1)) != Blocks.FIRE) {
         return false;
      } else {
         int var5 = mc.player.inventory.currentItem;
         return this.placeRedStone(var1, var2, var5, var3);
      }
   }

   private boolean placePower(BlockPos var1, EnumFacing var2, BlockPos var3) {
      return this.placePower(var1, var2, var3, false) ? true : this.placePower(var1, var2, var3, true);
   }

   private boolean piston(BlockPos var1) {
      for(EnumFacing var5 : EnumFacing.VALUES) {
         if (var5 != EnumFacing.DOWN) {
            if (var5 == EnumFacing.UP) {
               boolean var10000 = false;
            } else {
               int var6 = var1.offset(var5).getX() - var1.getX();
               int var7 = var1.offset(var5).getZ() - var1.getZ();
               if (this.placePiston(var1.offset(var5, 1).add(var7, 0, var6), var5, var1)) {
                  return true;
               }

               if (this.placePiston(var1.offset(var5, 1).add(-var7, 0, -var6), var5, var1)) {
                  return true;
               }

               if (this.placePiston(var1.offset(var5, -1).add(var7, 0, var6), var5, var1)) {
                  return true;
               }

               if (this.placePiston(var1.offset(var5, -1).add(-var7, 0, -var6), var5, var1)) {
                  return true;
               }
            }
         }

         boolean var8 = false;
      }

      return false;
   }

   private void doFire(BlockPos var1, EnumFacing var2) {
      if (this.fire.getValue()) {
         if (RebirthUtil.findItemInHotbar(Items.FLINT_AND_STEEL) != -1) {
            int var3 = mc.player.inventory.currentItem;

            for(EnumFacing var7 : EnumFacing.VALUES) {
               if (var7 != EnumFacing.DOWN) {
                  if (var7 == EnumFacing.UP) {
                     boolean var10000 = false;
                  } else if (var1.offset(var7).equals(var1.offset(var2))) {
                     boolean var12 = false;
                  } else if (mc.world.getBlockState(var1.offset(var7)).getBlock() == Blocks.FIRE) {
                     return;
                  }
               }

               boolean var13 = false;
            }

            for(EnumFacing var11 : EnumFacing.VALUES) {
               if (var11 != EnumFacing.DOWN) {
                  if (var11 == EnumFacing.UP) {
                     boolean var14 = false;
                  } else if (var1.offset(var11).equals(var1.offset(var2))) {
                     boolean var15 = false;
                  } else if (var1.offset(var11).equals(var1.offset(var2, -1)) && !RebirthUtil.posHasCrystal(var1.offset(var2, -1))) {
                     boolean var16 = false;
                  } else if (canFire(var1.offset(var11))) {
                     RebirthUtil.doSwap(RebirthUtil.findItemInHotbar(Items.FLINT_AND_STEEL));
                     RebirthUtil.placeBlock(var1.offset(var11), EnumHand.MAIN_HAND, true, this.packet.getValue());
                     RebirthUtil.doSwap(var3);
                     return;
                  }
               }

               boolean var17 = false;
            }

            if (canFire(var1.offset(var2, 1))) {
               RebirthUtil.doSwap(RebirthUtil.findItemInHotbar(Items.FLINT_AND_STEEL));
               RebirthUtil.placeBlock(var1.offset(var2, 1), EnumHand.MAIN_HAND, true, this.packet.getValue());
               RebirthUtil.doSwap(var3);
            }
         }
      }
   }

   private boolean pistonActive(BlockPos var1, EnumFacing var2, BlockPos var3, boolean var4) {
      if (var4) {
         var1 = var1.up();
      }

      if (!RebirthUtil.canPlaceCrystal(var3.offset(var2, -1)) && !RebirthUtil.posHasCrystal(var3.offset(var2, -1))) {
         return false;
      } else if (!(this.getBlock(var1) instanceof BlockPistonBase)) {
         return false;
      } else if (((EnumFacing)this.getBlockState(var1).getValue(BlockDirectional.FACING)).getOpposite() != var2) {
         return false;
      } else if (this.getBlock(var1.offset(var2, -1)) == Blocks.PISTON_EXTENSION) {
         return true;
      } else if (this.getBlock(var1.offset(var2, -1)) != Blocks.PISTON_HEAD) {
         return false;
      } else {
         for(EnumFacing var8 : EnumFacing.VALUES) {
            if (this.getBlock(var1.offset(var8)) == Blocks.REDSTONE_BLOCK) {
               if (!RebirthUtil.posHasCrystal(var3.offset(var2, -1))) {
                  int var9 = mc.player.inventory.currentItem;
                  crystalPos = var3.offset(var2, -1);
                  RebirthUtil.doSwap(RebirthUtil.findItemInHotbar(Items.END_CRYSTAL));
                  placeCrystal(var3.offset(var2, -1), true, debug.getValue());
                  RebirthUtil.doSwap(var9);
               }

               this.doFire(var3, var2);
               powerPos = var1.offset(var8);
               this.mineBlock(var1.offset(var8));
               return true;
            }

            boolean var10000 = false;
         }

         return false;
      }
   }

   private boolean pull(BlockPos var1) {
      for(EnumFacing var5 : EnumFacing.VALUES) {
         if (var5 != EnumFacing.DOWN) {
            if (var5 == EnumFacing.UP) {
               boolean var10000 = false;
            } else {
               int var6 = var1.offset(var5).getX() - var1.getX();
               int var7 = var1.offset(var5).getZ() - var1.getZ();
               if (this.pistonActive(var1.offset(var5, 1).add(var7, 0, var6), var5, var1)) {
                  return true;
               }

               if (this.pistonActive(var1.offset(var5, 1).add(-var7, 0, -var6), var5, var1)) {
                  return true;
               }

               if (this.pistonActive(var1.offset(var5, -1).add(var7, 0, var6), var5, var1)) {
                  return true;
               }

               if (this.pistonActive(var1.offset(var5, -1).add(-var7, 0, -var6), var5, var1)) {
                  return true;
               }
            }
         }

         boolean var8 = false;
      }

      return false;
   }

   private void mineBlock(BlockPos var1) {
      RebirthUtil.mineBlock(var1);
   }

   private static boolean canFire(BlockPos var0) {
      return RebirthUtil.canReplace(var0.down()) ? false : mc.world.isAirBlock(var0);
   }

   private Block getBlock(BlockPos var1) {
      return mc.world.getBlockState(var1).getBlock();
   }

   private EntityPlayer getTarget(double var1) {
      EntityPlayer var3 = null;
      double var4 = var1;

      for(EntityPlayer var7 : mc.world.playerEntities) {
         if (RebirthUtil.invalid(var7, var1)) {
         } else if (this.getBlock(RebirthUtil.getEntityPos(var7)) != Blocks.AIR) {
         } else if (var3 == null) {
            var3 = var7;
            var4 = mc.player.getDistanceSq(var7);
         } else if (mc.player.getDistanceSq(var7) >= var4) {
         } else {
            var3 = var7;
            var4 = mc.player.getDistanceSq(var7);
         }
      }

      return var3;
   }

   private boolean placePiston(BlockPos var1, EnumFacing var2, BlockPos var3, boolean var4) {
      if (var4) {
         var1 = var1.up();
      }

      if (!RebirthUtil.canPlaceCrystal(var3.offset(var2, -1)) && !RebirthUtil.posHasCrystal(var3.offset(var2, -1))) {
         return false;
      } else if (!RebirthUtil.canPlace(var1) && !(this.getBlock(var1) instanceof BlockPistonBase)) {
         return false;
      } else if (this.getBlock(var1) instanceof BlockPistonBase
         && ((EnumFacing)this.getBlockState(var1).getValue(BlockDirectional.FACING)).getOpposite() != var2) {
         return false;
      } else if (this.getBlock(var1.offset(var2, -1)) == Blocks.PISTON_HEAD || this.getBlock(var1.offset(var2, -1)) == Blocks.PISTON_EXTENSION) {
         return true;
      } else if (!mc.world.isAirBlock(var1.offset(var2, -1))
         && this.getBlock(var1.offset(var2, -1)) != Blocks.PISTON_HEAD
         && this.getBlock(var1.offset(var2, -1)) != Blocks.PISTON_EXTENSION) {
         return false;
      } else if ((mc.player.posY - (double)var1.down().getY() <= -1.0 || mc.player.posY - (double)var1.down().getY() >= 2.0)
         && RebirthUtil.distanceToXZ((double)var1.getX() + 0.5, (double)var1.getZ() + 0.5) < 2.6) {
         return false;
      } else {
         int var5 = mc.player.inventory.currentItem;
         if (RebirthUtil.canPlace(var1)) {
            RebirthUtil.facePlacePos(var1);
            RebirthPush.pistonFacing(var2);
            RebirthUtil.doSwap(RebirthUtil.findHotbarBlock(Blocks.PISTON));
            RebirthUtil.placeBlock(var1, EnumHand.MAIN_HAND, false, this.pistonPacket.getValue());
            RebirthUtil.doSwap(var5);
            RebirthUtil.facePlacePos(var1);
            return this.multiPlace.getValue() && this.placeRedStone(var1, var2, var5, var3) ? true : true;
         } else {
            return this.placeRedStone(var1, var2, var5, var3);
         }
      }
   }

   private boolean placeRedStone(BlockPos var1, EnumFacing var2, int var3, BlockPos var4) {
      for(EnumFacing var8 : EnumFacing.VALUES) {
         if (this.getBlock(var1.offset(var8)) == Blocks.REDSTONE_BLOCK) {
            powerPos = var1.offset(var8);
            return true;
         }

         boolean var10000 = false;
      }

      EnumFacing var10 = RebirthUtil.getBestNeighboring(var1, var2);
      if (var10 != null && !var1.offset(var10).equals(var4.offset(var2, -1)) && !var1.offset(var10).equals(var4.offset(var2, -1).up())) {
         powerPos = var1.offset(var10);
         if (RebirthUtil.canPlace(powerPos)) {
            RebirthUtil.doSwap(RebirthUtil.findHotbarBlock(Blocks.REDSTONE_BLOCK));
            RebirthUtil.placeBlock(var1.offset(var10), EnumHand.MAIN_HAND, true, this.packet.getValue());
            RebirthUtil.doSwap(var3);
            return true;
         }
      }

      for(EnumFacing var9 : EnumFacing.VALUES) {
         if (var1.offset(var9).equals(var1.offset(var2, -1))) {
            boolean var14 = false;
         } else if (var1.offset(var9).equals(var4.offset(var2, -1))) {
            boolean var15 = false;
         } else if (var1.offset(var9).equals(var4.offset(var2, -1).up())) {
            boolean var16 = false;
         } else if (RebirthUtil.canPlace(var1.offset(var9))) {
            powerPos = var1.offset(var9);
            RebirthUtil.doSwap(RebirthUtil.findHotbarBlock(Blocks.REDSTONE_BLOCK));
            RebirthUtil.placeBlock(var1.offset(var9), EnumHand.MAIN_HAND, true, this.packet.getValue());
            RebirthUtil.doSwap(var3);
            return true;
         }

         boolean var17 = false;
      }

      return false;
   }

   private boolean pistonActive(BlockPos var1, EnumFacing var2, BlockPos var3) {
      return this.pistonActive(var1, var2, var3, false) || this.pistonActive(var1, var2, var3, true);
   }
   private void placeCrystal(BlockPos var0, boolean var1, boolean debug) {
      if (fixPlaceCrystal.getValue()){
         RebirthUtil.placeCrystalFix(var0, true, debug);
      } else RebirthUtil.placeCrystal(var0, true, debug);
   }
}
