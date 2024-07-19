package dev.realme.ash.impl.module.misc;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.TickEvent;
import dev.realme.ash.impl.event.block.PlaceBlockEvent;
import dev.realme.ash.init.Modules;
import dev.realme.ash.util.world.BlockUtil;
import java.util.ArrayList;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class NukerModule
extends ToggleModule {
    Config<Float> range = new NumberConfig<Float>("Range", "", 0.0f, 5.0f, 6.0f);
    Config<Boolean> own = new BooleanConfig("Own", "", false);
    private final ArrayList<BlockPos> ownBlocks = new ArrayList();

    public NukerModule() {
        super("Nuker", "zyx.sb", ModuleCategory.MISCELLANEOUS);
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (Modules.PACKET_DIGGING.breakPos != null && NukerModule.mc.world.getBlockState(Modules.PACKET_DIGGING.breakPos).getBlock() instanceof ShulkerBoxBlock) {
            return;
        }
        if (Modules.CHEST_STEALER.isEnabled()) {
            return;
        }
        ShulkerBoxBlockEntity block = this.getBlock();
        if (block != null) {
            BlockPos pos = block.getPos();
            if (this.ownBlocks.contains(pos) && this.own.getValue().booleanValue()) {
                return;
            }
            Modules.PACKET_DIGGING.mine(this.getBlock().getPos());
        }
    }

    @EventListener
    public void onPlace(PlaceBlockEvent event) {
        if (event.block instanceof ShulkerBoxBlock) {
            this.ownBlocks.add(event.blockPos);
        }
    }

    private ShulkerBoxBlockEntity getBlock() {
        for (BlockEntity entity : BlockUtil.getTileEntities()) {
            ShulkerBoxBlockEntity shulker;
            if (!(entity instanceof ShulkerBoxBlockEntity) || !(MathHelper.sqrt((float)NukerModule.mc.player.squaredDistanceTo((shulker = (ShulkerBoxBlockEntity) entity).getPos().toCenterPos())) <= this.range.getValue().floatValue())) continue;
            return shulker;
        }
        return null;
    }
}
