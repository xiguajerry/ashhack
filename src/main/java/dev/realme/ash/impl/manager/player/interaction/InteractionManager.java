package dev.realme.ash.impl.manager.player.interaction;

import dev.realme.ash.Ash;
import dev.realme.ash.impl.module.client.CombatSettingModule;
import dev.realme.ash.impl.module.render.PlaceRenderModule;
import dev.realme.ash.init.Managers;
import dev.realme.ash.init.Modules;
import dev.realme.ash.mixin.accessor.IClientWorld;
import dev.realme.ash.util.Globals;
import dev.realme.ash.util.player.PlayerUtil;
import dev.realme.ash.util.world.BlockUtil;
import dev.realme.ash.util.world.EntityUtil;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class InteractionManager implements Globals {
   public InteractionManager() {
      Ash.EVENT_HANDLER.subscribe(this);
   }

   public void useItem(Hand hand) {
      this.useItem(hand, false);
   }

   public void useItem(Hand hand, boolean swing) {
      Managers.NETWORK.sendSequencedPacket((id) -> {
         return new PlayerInteractItemC2SPacket(hand, id);
      });
      if (swing) {
         PlayerUtil.doSwing(hand);
      }

   }

   public void placeBlock(BlockPos pos, boolean rotate) {
      Direction side = this.getPlaceDirection(pos);
      if (side != null) {
         this.placeBlock(pos, Hand.MAIN_HAND, true, rotate, Modules.COMBAT_SETTING.packetPlace.getValue());
      }
   }

   public void placeBlock(BlockPos pos, boolean rotate, Hand hand) {
      Direction side = this.getPlaceDirection(pos);
      if (side != null) {
         this.placeBlock(pos, hand, true, rotate, Modules.COMBAT_SETTING.packetPlace.getValue());
      }
   }

   public void placeBlock(BlockPos pos, boolean rotate, boolean swing) {
      Direction side = this.getPlaceDirection(pos);
      if (side != null) {
         this.placeBlock(pos, Hand.MAIN_HAND, swing, rotate, Modules.COMBAT_SETTING.packetPlace.getValue());
      }
   }

   public void placeBlock(BlockPos pos, Hand hand, boolean swing, boolean rotate) {
      Direction side = this.getPlaceDirection(pos);
      if (side != null) {
         this.placeBlock(pos, hand, swing, rotate, Modules.COMBAT_SETTING.packetPlace.getValue());
      }
   }

   public void placeBlock(BlockPos pos, Hand hand, boolean swing, boolean rotate, boolean packet) {
      Direction side = this.getPlaceDirection(pos);
      if (side != null) {
         Modules.PLACE_RENDER.PlaceMap.put(pos, new PlaceRenderModule.placePosition(pos));
         this.clickBlock(pos.offset(side.getOpposite()), side, hand, swing, rotate, packet);
      }
   }

   public void clickBlock(BlockPos pos, boolean rotate) {
      Direction side = Managers.INTERACT.getClickDirection(pos);
      this.clickBlock(pos, side, Hand.MAIN_HAND, true, rotate, Modules.COMBAT_SETTING.packetPlace.getValue());
   }

   public void clickBlock(BlockPos pos, Direction side, boolean rotate) {
      this.clickBlock(pos, side, Hand.MAIN_HAND, true, rotate, Modules.COMBAT_SETTING.packetPlace.getValue());
   }

   public void clickBlock(BlockPos pos, Direction side, boolean rotate, boolean swing) {
      this.clickBlock(pos, side, Hand.MAIN_HAND, swing, rotate, Modules.COMBAT_SETTING.packetPlace.getValue());
   }

   public void clickBlock(BlockPos pos, Direction side, Hand hand, boolean rotate, boolean swing) {
      this.clickBlock(pos, side, hand, swing, rotate, Modules.COMBAT_SETTING.packetPlace.getValue());
   }

   public void clickBlock(BlockPos pos, Direction side, Hand hand, boolean rotate) {
      this.clickBlock(pos, side, hand, true, rotate, Modules.COMBAT_SETTING.packetPlace.getValue());
   }

   public void clickBlock(BlockPos pos, Direction side, Hand hand, boolean swing, boolean rotate, boolean packet) {
      if (side != null) {
         Vec3d directionVec = new Vec3d((double)pos.getX() + 0.5 + (double)side.getVector().getX() * 0.5, (double)pos.getY() + 0.5 + (double)side.getVector().getY() * 0.5, (double)pos.getZ() + 0.5 + (double)side.getVector().getZ() * 0.5);
         if (rotate) {
            Managers.ROTATION.faceVector(directionVec, false);
         }

         if (swing) {
            PlayerUtil.doSwing(hand);
         }

         BlockHitResult result = new BlockHitResult(directionVec, side, pos, false);
         if (packet) {
            mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(hand, result, getWorldActionId(mc.world)));
         } else {
            mc.interactionManager.interactBlock(mc.player, hand, result);
         }

      }
   }

   public static int getWorldActionId(ClientWorld world) {
      PendingUpdateManager pum = getUpdateManager(world);
      int p = pum.getSequence();
      pum.close();
      return p;
   }

   public static PendingUpdateManager getUpdateManager(ClientWorld world) {
      return ((IClientWorld)world).acquirePendingUpdateManager();
   }

   public Direction getPlaceDirection(BlockPos pos) {
      double dis = Double.MAX_VALUE;
      Direction side = null;
      Direction[] var5 = Direction.values();
      int var6 = var5.length;

      int var7;
      Direction i;
      for(var7 = 0; var7 < var6; ++var7) {
         i = var5[var7];
         if (BlockUtil.canClick(pos.offset(i)) && !BlockUtil.canReplace(pos.offset(i)) && !this.heightCheck(pos.offset(i)) && (!this.strict() || this.isStrictDirection(pos.offset(i), i.getOpposite())) && (mc.player.isSneaking() || !BlockUtil.getState(pos.offset(i)).hasBlockEntity() && !this.isChargedRespawnAnchor(pos.offset(i)))) {
            double vecDis = (float)EntityUtil.getEyesPos().squaredDistanceTo(pos.toCenterPos().add((double)i.getVector().getX() * 0.5, (double)i.getVector().getY() * 0.5, (double)i.getVector().getZ() * 0.5));
            if (!((double)MathHelper.sqrt((float)vecDis) > this.range()) && (side == null || vecDis < dis)) {
               side = i;
               dis = vecDis;
            }
         }
      }

      if (side == null && airPlace()) {
         var5 = Direction.values();
         var6 = var5.length;

         for(var7 = 0; var7 < var6; ++var7) {
            i = var5[var7];
            if (BlockUtil.isAir(pos.offset(i))) {
               return i.getOpposite();
            }
         }
      }

      return side == null ? null : side.getOpposite();
   }

   public Direction getClickDirection(BlockPos pos) {
      Direction side = null;
      double dis = Double.MAX_VALUE;
      Direction[] var5 = Direction.values();
      int var6 = var5.length;

      int var7;
      Direction i;
      double vecDis;
      for(var7 = 0; var7 < var6; ++var7) {
         i = var5[var7];
         if (EntityUtil.canSee(pos, i) && !((double)MathHelper.sqrt((float)mc.player.squaredDistanceTo(pos.offset(i).toCenterPos())) > dis)) {
            vecDis = (float)EntityUtil.getEyesPos().squaredDistanceTo(pos.toCenterPos().add((double)i.getVector().getX() * 0.5, (double)i.getVector().getY() * 0.5, (double)i.getVector().getZ() * 0.5));
            if (!((double)MathHelper.sqrt((float)vecDis) > this.range())) {
               side = i;
               dis = MathHelper.sqrt((float)mc.player.squaredDistanceTo(pos.offset(i).toCenterPos()));
            }
         }
      }

      if (side != null) {
         return side;
      } else if (PlayerUtil.isTargetHere(pos, mc.player)) {
         return Direction.UP;
      } else {
         var5 = Direction.values();
         var6 = var5.length;

         for(var7 = 0; var7 < var6; ++var7) {
            i = var5[var7];
            if (!this.strict() || BlockUtil.canClickThrough(pos.offset(i)) && this.isStrictDirection(pos, i)) {
               vecDis = (float)EntityUtil.getEyesPos().squaredDistanceTo(pos.toCenterPos().add((double)i.getVector().getX() * 0.5, (double)i.getVector().getY() * 0.5, (double)i.getVector().getZ() * 0.5));
               if (!((double)MathHelper.sqrt((float)vecDis) > this.range()) && !((double)MathHelper.sqrt((float)mc.player.squaredDistanceTo(pos.offset(i).toCenterPos())) > dis)) {
                  side = i;
                  dis = MathHelper.sqrt((float)mc.player.squaredDistanceTo(pos.offset(i).toCenterPos()));
               }
            }
         }

         return side;
      }
   }

   private boolean heightCheck(BlockPos pos) {
      int var10000 = pos.getY();
      short var10001;
      switch (Modules.COMBAT_SETTING.maxHeight.getValue()) {
         case Old -> var10001 = 255;
         case New -> var10001 = 319;
         case Disabled -> var10001 = 1000;
         default -> throw new IncompatibleClassChangeError();
      }

      return var10000 >= var10001;
   }

   public double range() {
      return (double) Modules.COMBAT_SETTING.placeRange.getValue();
   }

   public boolean vanilla() {
      return Modules.COMBAT_SETTING.placement.getValue() == CombatSettingModule.Placement.Vanilla;
   }

   public boolean strict() {
      return Modules.COMBAT_SETTING.placement.getValue() == CombatSettingModule.Placement.Strict;
   }

   public static boolean airPlace() {
      return Modules.COMBAT_SETTING.placement.getValue() == CombatSettingModule.Placement.AirPlace;
   }

   public boolean isChargedRespawnAnchor(BlockPos pos) {
      return BlockUtil.getState(pos).getBlock() instanceof RespawnAnchorBlock && BlockUtil.getState(pos).get(Properties.CHARGES) > 0;
   }

   public boolean isStrictDirection(BlockPos pos, Direction dir) {
      if (!this.strict()) {
         return true;
      } else {
         boolean var10000;
         switch (dir) {
            case DOWN -> var10000 = mc.player.getEyePos().y <= (double)pos.getY() + 0.5;
            case UP -> var10000 = mc.player.getEyePos().y >= (double)pos.getY() + 0.5;
            case NORTH -> var10000 = mc.player.getZ() < (double)pos.getZ();
            case SOUTH -> var10000 = mc.player.getZ() >= (double)(pos.getZ() + 1);
            case WEST -> var10000 = mc.player.getX() < (double)pos.getX();
            case EAST -> var10000 = mc.player.getX() >= (double)(pos.getX() + 1);
            default -> throw new IncompatibleClassChangeError();
         }

         return var10000;
      }
   }
}
