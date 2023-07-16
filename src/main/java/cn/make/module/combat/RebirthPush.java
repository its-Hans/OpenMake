package cn.make.module.combat;

import cn.make.util.skid.RebirthUtil;
import cn.make.util.skid.Timer;
import cn.make.util.skid.two.BlockUtil;
import chad.phobos.Client;
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
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.List;

public class RebirthPush extends Module {

   public static final List<Block> canPushBlock = Arrays.asList(
       Blocks.AIR, Blocks.ENDER_CHEST, Blocks.STANDING_SIGN, Blocks.WALL_SIGN, Blocks.REDSTONE_WIRE, Blocks.TRIPWIRE
   );
   private EntityPlayer DisplayTarget;
   private final Timer timer;

   public RebirthPush() {
      super("RebirthPush", "use piston push hole fag", Category.COMBAT);
      this.timer = new Timer();
      this.DisplayTarget = null;
   }

   private final Setting<Boolean> rotate = this.register(new Setting<>("Rotate", true));
   private final Setting<Boolean> mine = this.register(new Setting<>("Mine", true));
   private final Setting<Double> placeRange = this.register(new Setting<>("PlaceRange", 5.0, 0.0, 6.0));
   private final Setting<Double> range = this.register(new Setting<>("Range", 5.0, 0.0, 6.0));
   private final Setting<Integer> surroundCheck = this.register(new Setting<>("SurroundCheck", 2, 0, 4));
   private final Setting<Boolean> attackCrystal = this.register(new Setting<>("BreakCrystal", true));
   private final Setting<Boolean> crystalCheck = this.register(new Setting<>("CrystalCheck", true));
   private final Setting<Boolean> checkPiston = this.register(new Setting<>("PistonCheck", false));
   private final Setting<Boolean> redStonePacket = this.register(new Setting<>("RedStonePacket", true));
   private final Setting<Boolean> pistonPacket = this.register(new Setting<>("PistonPacket", false));
   private final Setting<Integer> updateDelay = this.register(new Setting<>("delayUpdate", 100, 0, 500));
   private final Setting<Double> maxSelfSpeed = this.register(new Setting<>("MaxSelfSpeed", 6.0, 1.0, 30.0));
   private final Setting<Double> maxTargetSpeed = this.register(new Setting<>("MaxTargetSpeed", 4.0, 1.0, 15.0));
   private final Setting<Boolean> eatingPause = this.register(new Setting<>("E-Pause", true, v -> attackCrystal.getValue()));
   private final Setting<Boolean> pullBack = this.register(new Setting<>("PullBack", true));
   private final Setting<Boolean> onlyBurrow = this.register(new Setting<>("OnlyBurrow", true, v -> pullBack.getValue()));
   private final Setting<Boolean> allowWeb = this.register(new Setting<>("AllowWeb", true));
   private final Setting<Boolean> autoDisable = this.register(new Setting<>("AutoDisable", true));
   private final Setting<Boolean> onlyCrystal = this.register(new Setting<>("OnlyCrystal", false));
   private final Setting<Boolean> selfGround = this.register(new Setting<>("SelfGround", true));
   private final Setting<Boolean> noEating = this.register(new Setting<>("NoEating", true));
   private final Setting<Boolean> onlyGround = this.register(new Setting<>("OnlyGround", false));

   @Override
   public String getDisplayInfo() {
      return this.DisplayTarget != null ? this.DisplayTarget.getName() : null;
   }

   @Override
   public void onUpdate() {
      if (this.timer.passedMs((long) this.updateDelay.getValue())) {
         if (this.selfGround.getValue() && !mc.player.onGround) {
            if (this.autoDisable.getValue()) {
               this.disable();
            }
         } else if (RebirthUtil.findHotbarBlock(Blocks.REDSTONE_BLOCK) != -1 && RebirthUtil.findHotbarClass(BlockPistonBase.class) != -1) {
            if (Client.speedManager.getPlayerSpeed(mc.player) > this.maxSelfSpeed.getValue()) {
               if (this.autoDisable.getValue()) {
                  this.disable();
               }
            } else if (!this.noEating.getValue() || !RebirthUtil.isEating()) {
               if (!this.onlyCrystal.getValue()
                   || mc.player.getHeldItemOffhand().getItem().equals(Items.END_CRYSTAL)
                   || mc.player.getHeldItemMainhand().getItem().equals(Items.END_CRYSTAL)) {
                  this.timer.reset();
                  boolean var10000 = false;

                  for(EntityPlayer var2 : mc.world.playerEntities) {
                     if (!RebirthUtil.invalid(var2, this.range.getValue()) && this.canPush(var2) && (var2.onGround || !this.onlyGround.getValue())) {
                        if (Client.speedManager.getPlayerSpeed(var2) > this.maxTargetSpeed.getValue()) {
                           var10000 = false;
                        } else if (isWeb(var2) && !this.allowWeb.getValue()) {
                           var10000 = false;
                        } else {
                           this.DisplayTarget = var2;
                           if (this.doPush(new BlockPos(var2.posX + 0.1, var2.posY + 0.5, var2.posZ + 0.1), var2)) {
                              return;
                           }

                           if (this.doPush(new BlockPos(var2.posX - 0.1, var2.posY + 0.5, var2.posZ + 0.1), var2)) {
                              return;
                           }

                           if (this.doPush(new BlockPos(var2.posX + 0.1, var2.posY + 0.5, var2.posZ - 0.1), var2)) {
                              return;
                           }

                           if (this.doPush(new BlockPos(var2.posX - 0.1, var2.posY + 0.5, var2.posZ - 0.1), var2)) {
                              return;
                           }

                           var10000 = false;
                        }
                     }
                  }

                  if (this.autoDisable.getValue()) {
                     this.disable();
                  }

                  this.DisplayTarget = null;
               }
            }
         } else {
            if (this.autoDisable.getValue()) {
               this.disable();
            }
         }
      }
   }

   private Block getBlock(BlockPos var1) {
      return mc.world.getBlockState(var1).getBlock();
   }

   public static boolean isWeb(EntityPlayer var0) {
      if (isWeb(new BlockPos(var0.posX + 0.2, var0.posY + 0.5, var0.posZ + 0.2))) {
         return true;
      } else if (isWeb(new BlockPos(var0.posX - 0.2, var0.posY + 0.5, var0.posZ + 0.2))) {
         return true;
      } else {
         return isWeb(new BlockPos(var0.posX - 0.2, var0.posY + 0.5, var0.posZ - 0.2))
             || isWeb(new BlockPos(var0.posX + 0.2, var0.posY + 0.5, var0.posZ - 0.2));
      }
   }

   public boolean doPush(BlockPos var1, EntityPlayer var2) {
      if (this.checkPiston.getValue() && this.checkPiston(var1)) {
         return true;
      } else {
         if (mc.world.isAirBlock(var1.up(2))) {
            for(EnumFacing var38 : EnumFacing.VALUES) {
               if (var38 != EnumFacing.DOWN) {
                  if (var38 == EnumFacing.UP) {
                     boolean var77 = false;
                  } else {
                     BlockPos var46 = var1.offset(var38).up();
                     if (this.getBlock(var46) instanceof BlockPistonBase
                        && canPushBlock.contains(this.getBlock(var46.offset(var38, -2)))
                        && (this.getBlock(var46.offset(var38, -2).up()) == Blocks.AIR || this.getBlock(var46.offset(var38, -2).up()) == Blocks.REDSTONE_BLOCK)
                        )
                      {
                        if (((EnumFacing)this.getBlockState(var46).getValue(BlockDirectional.FACING)).getOpposite() != var38) {
                           boolean var76 = false;
                        } else {
                           for(EnumFacing var61 : EnumFacing.VALUES) {
                              if (this.getBlock(var46.offset(var61)) == Blocks.REDSTONE_BLOCK) {
                                 if (this.mine.getValue()) {
                                    this.mine(var46.offset(var61));
                                 }

                                 if (this.autoDisable.getValue()) {
                                    this.disable();
                                 }

                                 return true;
                              }

                              boolean var75 = false;
                           }
                        }
                     }
                  }
               }

               boolean var78 = false;
            }

            for(EnumFacing var39 : EnumFacing.VALUES) {
               if (var39 != EnumFacing.DOWN) {
                  if (var39 == EnumFacing.UP) {
                     boolean var79 = false;
                  } else {
                     BlockPos var47 = var1.offset(var39).up();
                     if (this.getBlock(var47) instanceof BlockPistonBase
                        && canPushBlock.contains(this.getBlock(var47.offset(var39, -2)))
                        && (this.getBlock(var47.offset(var39, -2).up()) == Blocks.AIR || this.getBlock(var47.offset(var39, -2).up()) == Blocks.REDSTONE_BLOCK)
                        )
                      {
                        if (((EnumFacing)this.getBlockState(var47).getValue(BlockDirectional.FACING)).getOpposite() != var39) {
                           boolean var80 = false;
                        } else if (this.doPower(var47)) {
                           return true;
                        }
                     }
                  }
               }

               boolean var81 = false;
            }

            for(EnumFacing var40 : EnumFacing.VALUES) {
               if (var40 != EnumFacing.DOWN) {
                  if (var40 == EnumFacing.UP) {
                     boolean var82 = false;
                  } else {
                     BlockPos var48 = var1.offset(var40).up();
                     if ((mc.player.posY - var2.posY <= -1.0 || mc.player.posY - var2.posY >= 2.0)
                        && RebirthUtil.distanceToXZ((double)var48.getX() + 0.5, (double)var48.getZ() + 0.5) < 2.6) {
                        boolean var85 = false;
                     } else if (this.attackCrystal(var48) && this.crystalCheck.getValue()) {
                        boolean var84 = false;
                     } else if (RebirthUtil.canPlace2(var48)
                        && canPushBlock.contains(this.getBlock(var48.offset(var40, -2)))
                        && canPushBlock.contains(this.getBlock(var48.offset(var40, -2).up()))) {
                        if (RebirthUtil.canBlockFacing(var48) || !this.downPower(var48)) {
                           this.doPiston(var40, var48);
                           return true;
                        }

                        boolean var83 = false;
                        break;
                     }
                  }
               }

               boolean var86 = false;
            }

            if (this.getBlock(var1) == Blocks.AIR && this.onlyBurrow.getValue() || !this.pullBack.getValue()) {
               if (this.autoDisable.getValue()) {
                  this.disable();
               }

               return true;
            }

            for(EnumFacing var41 : EnumFacing.VALUES) {
               if (var41 != EnumFacing.DOWN) {
                  if (var41 == EnumFacing.UP) {
                     boolean var89 = false;
                  } else {
                     BlockPos var49 = var1.offset(var41).up();

                     for(EnumFacing var62 : EnumFacing.VALUES) {
                        if (this.getBlock(var49) instanceof BlockPistonBase && this.getBlock(var49.offset(var62)) == Blocks.REDSTONE_BLOCK) {
                           if (((EnumFacing)this.getBlockState(var49).getValue(BlockDirectional.FACING)).getOpposite() == var41) {
                              this.mine(var49.offset(var62));
                              if (this.autoDisable.getValue()) {
                                 this.disable();
                              }

                              return true;
                           }

                           boolean var87 = false;
                        }

                        boolean var88 = false;
                     }
                  }
               }

               boolean var90 = false;
            }

            for(EnumFacing var42 : EnumFacing.VALUES) {
               if (var42 != EnumFacing.DOWN) {
                  if (var42 == EnumFacing.UP) {
                     boolean var95 = false;
                  } else {
                     BlockPos var50 = var1.offset(var42).up();

                     for(EnumFacing var63 : EnumFacing.VALUES) {
                        if (this.getBlock(var50) instanceof BlockPistonBase && this.getBlock(var50.offset(var63)) == Blocks.AIR) {
                           if (((EnumFacing)this.getBlockState(var50).getValue(BlockDirectional.FACING)).getOpposite() != var42) {
                              boolean var91 = false;
                           } else if (this.attackCrystal(var50.offset(var63)) && this.crystalCheck.getValue()) {
                              boolean var93 = false;
                           } else {
                              if (!this.doPower(var50, var63)) {
                                 this.mine(var50.offset(var63));
                                 return true;
                              }

                              boolean var92 = false;
                           }
                        }

                        boolean var94 = false;
                     }
                  }
               }

               boolean var96 = false;
            }

            for(EnumFacing var43 : EnumFacing.VALUES) {
               if (var43 != EnumFacing.DOWN) {
                  if (var43 == EnumFacing.UP) {
                     boolean var97 = false;
                  } else {
                     BlockPos var51 = var1.offset(var43).up();
                     if ((mc.player.posY - var2.posY <= -1.0 || mc.player.posY - var2.posY >= 2.0)
                        && RebirthUtil.distanceToXZ((double)var51.getX() + 0.5, (double)var51.getZ() + 0.5) < 2.6) {
                        boolean var100 = false;
                     } else if (this.attackCrystal(var51) && this.crystalCheck.getValue()) {
                        boolean var99 = false;
                     } else if (RebirthUtil.canPlace2(var51)) {
                        if (!this.downPower(var51)) {
                           this.doPiston(var43, var51);
                           return true;
                        }

                        boolean var98 = false;
                     }
                  }
               }

               boolean var101 = false;
            }

            boolean var102 = false;
         } else {
            for(EnumFacing var6 : EnumFacing.VALUES) {
               if (var6 != EnumFacing.DOWN) {
                  if (var6 == EnumFacing.UP) {
                     boolean var65 = false;
                  } else {
                     BlockPos var7 = var1.offset(var6).up();
                     if (this.getBlock(var7) instanceof BlockPistonBase
                        && (
                           mc.world.isAirBlock(var7.offset(var6, -2)) && mc.world.isAirBlock(var7.offset(var6, -2).down())
                              || checkTarget(var7.offset(var6, 2), var2)
                        )) {
                        if (((EnumFacing)this.getBlockState(var7).getValue(BlockDirectional.FACING)).getOpposite() != var6) {
                           boolean var64 = false;
                        } else {
                           for(EnumFacing var11 : EnumFacing.VALUES) {
                              if (this.getBlock(var7.offset(var11)) == Blocks.REDSTONE_BLOCK) {
                                 if (this.mine.getValue()) {
                                    this.mine(var7.offset(var11));
                                 }

                                 if (this.autoDisable.getValue()) {
                                    this.disable();
                                 }

                                 return true;
                              }

                              boolean var10000 = false;
                           }
                        }
                     }
                  }
               }

               boolean var66 = false;
            }

            for(EnumFacing var36 : EnumFacing.VALUES) {
               if (var36 != EnumFacing.DOWN) {
                  if (var36 == EnumFacing.UP) {
                     boolean var67 = false;
                  } else {
                     BlockPos var44 = var1.offset(var36).up();
                     if (this.getBlock(var44) instanceof BlockPistonBase
                        && (
                           mc.world.isAirBlock(var44.offset(var36, -2)) && mc.world.isAirBlock(var44.offset(var36, -2).down())
                              || checkTarget(var44.offset(var36, 2), var2)
                        )) {
                        if (((EnumFacing)this.getBlockState(var44).getValue(BlockDirectional.FACING)).getOpposite() != var36) {
                           boolean var68 = false;
                        } else if (this.doPower(var44)) {
                           return true;
                        }
                     }
                  }
               }

               boolean var69 = false;
            }

            for(EnumFacing var37 : EnumFacing.VALUES) {
               if (var37 != EnumFacing.DOWN) {
                  if (var37 == EnumFacing.UP) {
                     boolean var70 = false;
                  } else {
                     BlockPos var45 = var1.offset(var37).up();
                     if ((mc.player.posY - var2.posY <= -1.0 || mc.player.posY - var2.posY >= 2.0)
                        && RebirthUtil.distanceToXZ((double)var45.getX() + 0.5, (double)var45.getZ() + 0.5) < 2.6) {
                        boolean var73 = false;
                     } else if (this.attackCrystal(var45) && this.crystalCheck.getValue()) {
                        boolean var72 = false;
                     } else if (RebirthUtil.canPlace2(var45)
                        && (
                           mc.world.isAirBlock(var45.offset(var37, -2)) && mc.world.isAirBlock(var45.offset(var37, -2).down())
                              || checkTarget(var45.offset(var37, 2), var2)
                        )
                        && canPushBlock.contains(this.getBlock(var45.offset(var37, -2).up()))) {
                        if (RebirthUtil.canBlockFacing(var45) || !this.downPower(var45)) {
                           this.doPiston(var37, var45);
                           return true;
                        }

                        boolean var71 = false;
                        break;
                     }
                  }
               }

               boolean var74 = false;
            }
         }

         return false;
      }
   }

   private static boolean checkEntity(BlockPos var0) {
      for(Entity var2 : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(var0))) {
         if (var2 instanceof EntityPlayer) {
            if (var2 != mc.player) {
               return true;
            }

            boolean var10000 = false;
         }
      }

      return false;
   }

   private Boolean canPush(EntityPlayer var1) {
      int var2 = 0;
      if (!mc.world.isAirBlock(new BlockPos(var1.posX + 1.0, var1.posY + 0.5, var1.posZ))) {
         ++var2;
      }

      if (!mc.world.isAirBlock(new BlockPos(var1.posX - 1.0, var1.posY + 0.5, var1.posZ))) {
         ++var2;
      }

      if (!mc.world.isAirBlock(new BlockPos(var1.posX, var1.posY + 0.5, var1.posZ + 1.0))) {
         ++var2;
      }

      if (!mc.world.isAirBlock(new BlockPos(var1.posX, var1.posY + 0.5, var1.posZ - 1.0))) {
         ++var2;
      }

      if (mc.world.isAirBlock(new BlockPos(var1.posX, var1.posY + 2.5, var1.posZ))) {
         if (!mc.world.isAirBlock(new BlockPos(var1.posX, var1.posY + 0.5, var1.posZ))) {
            return true;
         } else {
            boolean var10;
            if (var2 > this.surroundCheck.getValue() - 1) {
               var10 = true;
               boolean var11 = false;
            } else {
               var10 = false;
            }

            return var10;
         }
      } else {
         for(EnumFacing var6 : EnumFacing.VALUES) {
            if (var6 != EnumFacing.UP) {
               if (var6 == EnumFacing.DOWN) {
                  boolean var10000 = false;
               } else {
                  BlockPos var7 = RebirthUtil.getEntityPos(var1).offset(var6);
                  if (mc.world.isAirBlock(var7) && mc.world.isAirBlock(var7.up()) || checkTarget(var7, this.DisplayTarget)) {
                     if (!mc.world.isAirBlock(new BlockPos(var1.posX, var1.posY + 0.5, var1.posZ))) {
                        return true;
                     } else {
                        boolean var9;
                        if (var2 > this.surroundCheck.getValue() - 1) {
                           var9 = true;
                           boolean var10001 = false;
                        } else {
                           var9 = false;
                        }

                        return var9;
                     }
                  }
               }
            }

            boolean var8 = false;
         }

         return false;
      }
   }

   private void doPiston(EnumFacing var1, BlockPos var2) {
      if (RebirthUtil.canPlace(var2, this.placeRange.getValue())) {
         if (this.rotate.getValue()) {
            RebirthUtil.facePlacePos(var2);
         }

         pistonFacing(var1);
         int var3 = mc.player.inventory.currentItem;
         RebirthUtil.doSwap(RebirthUtil.findHotbarClass(BlockPistonBase.class));
         BlockUtil.placeBlock(var2, EnumHand.MAIN_HAND, false, this.pistonPacket.getValue());
         RebirthUtil.doSwap(var3);
         if (this.rotate.getValue()) {
            RebirthUtil.facePlacePos(var2);
         }

         for(EnumFacing var7 : EnumFacing.VALUES) {
            if (this.getBlock(var2.offset(var7)) == Blocks.REDSTONE_BLOCK) {
               if (this.mine.getValue()) {
                  this.mine(var2.offset(var7));
               }

               if (this.autoDisable.getValue()) {
                  this.disable();
               }

               return;
            }

            boolean var10000 = false;
         }

         this.doPower(var2);
         boolean var8 = false;
      }
   }

   private boolean doPower(BlockPos var1) {
      EnumFacing var2 = RebirthUtil.getBestNeighboring(var1, null);
      if (var2 != null) {
         if (this.attackCrystal(var1.offset(var2)) && this.crystalCheck.getValue()) {
            return true;
         }

         if (!this.doPower(var1, var2)) {
            return true;
         }
      }

      for(EnumFacing var6 : EnumFacing.VALUES) {
         if (this.attackCrystal(var1.offset(var6)) && this.crystalCheck.getValue()) {
            return true;
         }

         if (!this.doPower(var1, var6)) {
            return true;
         }

         boolean var10000 = false;
         var10000 = false;
      }

      return false;
   }

   private boolean checkPiston(BlockPos var1) {
      for(EnumFacing var5 : EnumFacing.VALUES) {
         if (var5 != EnumFacing.DOWN) {
            if (var5 == EnumFacing.UP) {
               boolean var12 = false;
            } else {
               BlockPos var6 = var1.up();
               if (this.getBlock(var6.offset(var5)) instanceof BlockPistonBase) {
                  if (((EnumFacing)this.getBlockState(var6.offset(var5)).getValue(BlockDirectional.FACING)).getOpposite() != var5) {
                     boolean var11 = false;
                  } else {
                     for(EnumFacing var10 : EnumFacing.VALUES) {
                        if (this.getBlock(var6.offset(var5).offset(var10)) == Blocks.REDSTONE_BLOCK && this.mine.getValue()) {
                           this.mine(var6.offset(var5).offset(var10));
                           if (this.autoDisable.getValue()) {
                              this.disable();
                           }

                           return true;
                        }

                        boolean var10000 = false;
                     }
                  }
               }
            }
         }

         boolean var13 = false;
      }

      return false;
   }


   static boolean checkTarget(BlockPos var0, Entity var1) {
      Vec3d[] var2 = RebirthUtil.getVarOffsets(0, 0, 0);

      for(Vec3d var6 : var2) {
         BlockPos var7 = new BlockPos(var0).add(var6.x, var6.y, var6.z);

         for(Entity var9 : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(var7))) {
            if (var9 == var1) {
               return true;
            }

            boolean var10000 = false;
         }

         boolean var10 = false;
      }

      return false;
   }

   private IBlockState getBlockState(BlockPos var1) {
      return mc.world.getBlockState(var1);
   }

   private boolean attackCrystal(BlockPos var1) {
      for(Entity var3 : mc.world.loadedEntityList) {
         if (var3 instanceof EntityEnderCrystal
            && (!(var3.getDistance((double)var1.getX() + 0.5, (double)var1.getY(), (double)var1.getZ() + 0.5) > 4.0) || !this.crystalCheck.getValue())) {
            if (!(var3.getDistance((double)var1.getX() + 0.5, (double)var1.getY(), (double)var1.getZ() + 0.5) > 2.0) || this.crystalCheck.getValue()) {
               RebirthUtil.attackCrystal(var3, this.rotate.getValue(), this.eatingPause.getValue());
               return true;
            }

            boolean var10000 = false;
         }
      }

      return false;
   }


   private boolean doPower(BlockPos var1, EnumFacing var2) {
      if (!RebirthUtil.canPlace(var1.offset(var2), this.placeRange.getValue())) {
         return true;
      } else {
         int var3 = mc.player.inventory.currentItem;
         RebirthUtil.doSwap(RebirthUtil.findHotbarBlock(Blocks.REDSTONE_BLOCK));
         BlockUtil.placeBlock(var1.offset(var2), EnumHand.MAIN_HAND, this.rotate.getValue(), this.redStonePacket.getValue());
         RebirthUtil.doSwap(var3);
         return false;
      }
   }


   private boolean downPower(BlockPos var1) {
      if (!RebirthUtil.canBlockFacing(var1)) {
         boolean var2 = true;

         for(EnumFacing var6 : EnumFacing.VALUES) {
            if (this.getBlock(var1.offset(var6)) == Blocks.REDSTONE_BLOCK) {
               var2 = false;
               boolean var8 = false;
               break;
            }

            boolean var10000 = false;
         }

         if (var2) {
            if (!RebirthUtil.canPlace(var1.add(0, -1, 0), this.placeRange.getValue())) {
               return true;
            }

            int var7 = mc.player.inventory.currentItem;
            RebirthUtil.doSwap(RebirthUtil.findHotbarBlock(Blocks.REDSTONE_BLOCK));
            BlockUtil.placeBlock(var1.add(0, -1, 0), EnumHand.MAIN_HAND, this.rotate.getValue(), this.redStonePacket.getValue());
            RebirthUtil.doSwap(var7);
         }
      }

      return false;
   }

   private static boolean isWeb(BlockPos var0) {
      boolean var10000;
      if (mc.world.getBlockState(var0).getBlock() == Blocks.WEB && checkEntity(var0)) {
         var10000 = true;
         boolean var10001 = false;
      } else {
         var10000 = false;
      }

      return var10000;
   }

   private void mine(BlockPos var1) {
      RebirthUtil.mineBlock(var1);
   }

   public static void pistonFacing(EnumFacing var0) {
      if (var0 == EnumFacing.EAST) {
         RebirthUtil.faceYawAndPitch(-90.0F, 5.0F);
         boolean var10000 = false;
      } else if (var0 == EnumFacing.WEST) {
         RebirthUtil.faceYawAndPitch(90.0F, 5.0F);
         boolean var1 = false;
      } else if (var0 == EnumFacing.NORTH) {
         RebirthUtil.faceYawAndPitch(180.0F, 5.0F);
         boolean var2 = false;
      } else if (var0 == EnumFacing.SOUTH) {
         RebirthUtil.faceYawAndPitch(0.0F, 5.0F);
      }
   }

}
