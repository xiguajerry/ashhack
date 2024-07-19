package dev.realme.ash.impl.module.combat;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.TickEvent;
import dev.realme.ash.util.player.PlayerUtil;
import dev.realme.ash.util.world.BlockPosX;
import dev.realme.ash.util.world.BlockUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class SelfBounceModule
extends ToggleModule {
    Config<Boolean> smart = new BooleanConfig("Smart", "", false);
    Config<Integer> offset = new NumberConfig<Integer>("Offset", "", -15, 1, 15);
    Config<Boolean> pauseWeb = new BooleanConfig("PauseWeb", "", true);

    public SelfBounceModule() {
        super("SelfBounce", "Rubberband.", ModuleCategory.COMBAT);
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (SelfBounceModule.nullCheck()) {
            return;
        }
        if (!SelfBounceModule.mc.player.isOnGround()) {
            this.disable();
            return;
        }
        if (PlayerUtil.isInWeb(SelfBounceModule.mc.player, !PlayerUtil.isInsideBlock(), true) && this.pauseWeb.getValue().booleanValue()) {
            this.disable();
            return;
        }
        double offsets = this.getOffsets();
        if (offsets == -1.0) {
            this.disable();
            return;
        }
        SelfBounceModule.mc.player.setPosition(SelfBounceModule.mc.player.getX(), SelfBounceModule.mc.player.getY() + offsets, SelfBounceModule.mc.player.getZ());
        this.disable();
    }

    private double getOffsets() {
        assert (SelfBounceModule.mc.player != null);
        BlockPos selfPos = PlayerUtil.playerPos(SelfBounceModule.mc.player);
        if (!this.smart.getValue().booleanValue()) {
            return this.offset.getValue().intValue();
        }
        if (this.canTeleport(selfPos.up(), 1)) {
            return 1.0;
        }
        if (this.canTeleport(selfPos.up(3), 3)) {
            return 2.0 + new Box(selfPos.up(2)).getLengthY();
        }
        return -1.0;
    }

    private boolean canTeleport(BlockPos pos, int dis) {
        if (dis == 1) {
            return BlockUtil.canMovement(pos) && BlockUtil.canMovement(pos.up()) && BlockUtil.canStand(pos);
        }
        BlockPosX pos1 = new BlockPosX((double)pos.getX() + 0.3, (double)pos.getY(), (double)pos.getZ() + 0.3);
        BlockPosX pos2 = new BlockPosX((double)pos.getX() - 0.3, (double)pos.getY(), (double)pos.getZ() + 0.3);
        BlockPosX pos3 = new BlockPosX((double)pos.getX() + 0.3, (double)pos.getY(), (double)pos.getZ() - 0.3);
        BlockPosX pos4 = new BlockPosX((double)pos.getX() - 0.3, (double)pos.getY(), (double)pos.getZ() - 0.3);
        return BlockUtil.canMovement(pos1) && BlockUtil.canMovement(pos2) && BlockUtil.canMovement(pos3) && BlockUtil.canMovement(pos4) && BlockUtil.canMovement(((BlockPos)pos1).up()) && BlockUtil.canMovement(((BlockPos)pos2).up()) && BlockUtil.canMovement(((BlockPos)pos3).up()) && BlockUtil.canMovement(((BlockPos)pos4).up()) && (BlockUtil.canStand(pos1) || BlockUtil.canStand(pos2) || BlockUtil.canStand(pos3) || BlockUtil.canStand(pos4));
    }
}
