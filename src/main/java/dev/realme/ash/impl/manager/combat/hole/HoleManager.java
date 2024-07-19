package dev.realme.ash.impl.manager.combat.hole;

import dev.realme.ash.Ash;
import dev.realme.ash.api.event.EventStage;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.Module;
import dev.realme.ash.impl.event.TickEvent;
import dev.realme.ash.init.Modules;
import dev.realme.ash.util.Globals;
import dev.realme.ash.util.world.BlastResistantBlocks;
import dev.realme.ash.util.world.BlockUtil;
import io.netty.util.internal.ConcurrentSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class HoleManager implements Globals {
   private final ExecutorService executor = Executors.newFixedThreadPool(1);
   private Future result;
   private Set holes = new ConcurrentSet();

   public HoleManager() {
      Ash.EVENT_HANDLER.subscribe(this);
   }

   @EventListener
   public void onTickEvent(TickEvent event) {
      if (event.getStage() == EventStage.PRE) {
         if (!Module.nullCheck()) {
            HoleTask runnable = new HoleTask(this.getSphere(mc.player.getPos()));
            this.result = this.executor.submit(runnable);
         }
      }
   }

   public List getSphere(Vec3d start) {
      List sphere = new ArrayList();
      double rad = Math.ceil(Math.max(5.0, Modules.HOLE_ESP.getRange()));

      for(double x = -rad; x <= rad; ++x) {
         for(double y = -rad; y <= rad; ++y) {
            for(double z = -rad; z <= rad; ++z) {
               Vec3i pos = new Vec3i((int)(start.getX() + x), (int)(start.getY() + y), (int)(start.getZ() + z));
               BlockPos p = new BlockPos(pos);
               sphere.add(p);
            }
         }
      }

      return sphere;
   }

   public Hole checkHole(BlockPos pos) {
      if (pos.getY() == mc.world.getBottomY() && !BlastResistantBlocks.isUnbreakable(pos)) {
         return new Hole(pos, HoleType.VOID);
      } else {
         int resistant = 0;
         int unbreakable = 0;
         if (BlockUtil.isBlockAccessible(pos)) {
            BlockPos pos1 = pos.add(-1, 0, 0);
            BlockPos pos2 = pos.add(0, 0, -1);
            if (BlastResistantBlocks.isBlastResistant(pos1)) {
               ++resistant;
            } else if (BlastResistantBlocks.isUnbreakable(pos1)) {
               ++unbreakable;
            }

            if (BlastResistantBlocks.isBlastResistant(pos2)) {
               ++resistant;
            } else if (BlastResistantBlocks.isUnbreakable(pos2)) {
               ++unbreakable;
            }

            if (resistant + unbreakable < 2) {
               return null;
            } else {
               BlockPos pos3 = pos.add(0, 0, 1);
               BlockPos pos4 = pos.add(1, 0, 0);
               boolean air3 = mc.world.isAir(pos3);
               boolean air4 = mc.world.isAir(pos4);
               BlockPos[] quad;
               int var13;
               if (air3 && air4) {
                  BlockPos pos5 = pos.add(1, 0, 1);
                  if (!mc.world.isAir(pos5)) {
                     return null;
                  } else {
                     quad = new BlockPos[]{pos.add(-1, 0, 1), pos.add(0, 0, 2), pos.add(1, 0, 2), pos.add(2, 0, 1), pos.add(2, 0, 0), pos.add(1, 0, -1)};
                     BlockPos[] var18 = quad;
                     var13 = quad.length;

                     for(int var20 = 0; var20 < var13; ++var20) {
                        BlockPos p = var18[var20];
                        if (BlastResistantBlocks.isBlastResistant(p)) {
                           ++resistant;
                        } else if (BlastResistantBlocks.isUnbreakable(p)) {
                           ++unbreakable;
                        }
                     }

                     if (resistant != 8 && unbreakable != 8 && resistant + unbreakable != 8) {
                        return null;
                     } else {
                        Hole quadHole = new Hole(pos, resistant == 8 ? HoleType.OBSIDIAN : (unbreakable == 8 ? HoleType.BEDROCK : HoleType.OBSIDIAN_BEDROCK), pos1, pos2, pos3, pos4, pos5);
                        quadHole.addHoleOffsets(quad);
                        return quadHole;
                     }
                  }
               } else {
                  BlockPos[] doubleX;
                  int var12;
                  BlockPos p;
                  Hole doubleXHole;
                  if (air3 && BlockUtil.isBlockAccessible(pos3)) {
                     doubleX = new BlockPos[]{pos.add(-1, 0, 1), pos.add(0, 0, 2), pos.add(1, 0, 1), pos.add(1, 0, 0)};
                     quad = doubleX;
                     var12 = doubleX.length;

                     for(var13 = 0; var13 < var12; ++var13) {
                        p = quad[var13];
                        if (BlastResistantBlocks.isBlastResistant(p)) {
                           ++resistant;
                        } else if (BlastResistantBlocks.isUnbreakable(p)) {
                           ++unbreakable;
                        }
                     }

                     if (resistant != 6 && unbreakable != 6 && resistant + unbreakable != 6) {
                        return null;
                     } else {
                        doubleXHole = new Hole(pos, resistant == 6 ? HoleType.OBSIDIAN : (unbreakable == 6 ? HoleType.BEDROCK : HoleType.OBSIDIAN_BEDROCK), pos1, pos2, pos3);
                        doubleXHole.addHoleOffsets(doubleX);
                        return doubleXHole;
                     }
                  } else if (air4 && BlockUtil.isBlockAccessible(pos4)) {
                     doubleX = new BlockPos[]{pos.add(0, 0, 1), pos.add(1, 0, 1), pos.add(2, 0, 0), pos.add(1, 0, -1)};
                     quad = doubleX;
                     var12 = doubleX.length;

                     for(var13 = 0; var13 < var12; ++var13) {
                        p = quad[var13];
                        if (BlastResistantBlocks.isBlastResistant(p)) {
                           ++resistant;
                        } else if (BlastResistantBlocks.isUnbreakable(p)) {
                           ++unbreakable;
                        }
                     }

                     if (resistant != 6 && unbreakable != 6 && resistant + unbreakable != 6) {
                        return null;
                     } else {
                        doubleXHole = new Hole(pos, resistant == 6 ? HoleType.OBSIDIAN : (unbreakable == 6 ? HoleType.BEDROCK : HoleType.OBSIDIAN_BEDROCK), pos1, pos2, pos4);
                        doubleXHole.addHoleOffsets(doubleX);
                        return doubleXHole;
                     }
                  } else {
                     if (BlastResistantBlocks.isBlastResistant(pos3)) {
                        ++resistant;
                     } else if (BlastResistantBlocks.isUnbreakable(pos3)) {
                        ++unbreakable;
                     }

                     if (BlastResistantBlocks.isBlastResistant(pos4)) {
                        ++resistant;
                     } else if (BlastResistantBlocks.isUnbreakable(pos4)) {
                        ++unbreakable;
                     }

                     return resistant != 4 && unbreakable != 4 && resistant + unbreakable != 4 ? null : new Hole(pos, resistant == 4 ? HoleType.OBSIDIAN : (unbreakable == 4 ? HoleType.BEDROCK : HoleType.OBSIDIAN_BEDROCK), pos1, pos2, pos3, pos4);
                  }
               }
            }
         } else {
            return null;
         }
      }
   }

   public Set<Hole> getHoles() {
      if (this.result != null) {
         try {
            this.holes = (Set)this.result.get();
         } catch (InterruptedException | ExecutionException var2) {
         }
      }

      return this.holes;
   }

   public class HoleTask implements Callable {
      private final List blocks;

      public HoleTask(List blocks) {
         this.blocks = blocks;
      }

      public Set call() throws Exception {
         Set holes1 = new HashSet();

          for (Object block : this.blocks) {
              BlockPos blockPos = (BlockPos) block;
              Hole hole = HoleManager.this.checkHole(blockPos);
              if (hole != null) {
                  holes1.add(hole);
              }
          }

         return holes1;
      }
   }
}
