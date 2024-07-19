package dev.realme.ash.impl.module.movement;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.EnumConfig;
import dev.realme.ash.api.event.EventStage;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.TickEvent;
import dev.realme.ash.impl.event.entity.player.PlayerJumpEvent;
import dev.realme.ash.impl.event.network.PacketEvent;
import dev.realme.ash.impl.event.network.UpdateWalkingEvent;
import dev.realme.ash.impl.event.world.BlockCollisionEvent;
import dev.realme.ash.init.Modules;
import dev.realme.ash.mixin.accessor.AccessorKeyBinding;
import dev.realme.ash.mixin.accessor.AccessorPlayerMoveC2SPacket;
import dev.realme.ash.util.player.MovementUtil;
import dev.realme.ash.util.string.EnumFormatter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.fluid.Fluids;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShapes;

public class JesusModule
extends ToggleModule {
    final Config<JesusMode> modeConfig = new EnumConfig<>("Mode", "The mode for walking on water", JesusMode.SOLID, JesusMode.values());
    final Config<Boolean> strictConfig = new BooleanConfig("Strict", "NCP Updated bypass for floating offsets", false, () -> this.modeConfig.getValue() == JesusMode.SOLID);
    private int floatTimer = 1000;
    private boolean fluidState;
    private double floatOffset;

    public JesusModule() {
        super("Jesus", "Allow player to walk on water", ModuleCategory.MOVEMENT);
    }

    @Override
    public String getModuleData() {
        return EnumFormatter.formatEnum(this.modeConfig.getValue());
    }

    @Override
    public void onDisable() {
        this.floatOffset = 0.0;
        this.floatTimer = 1000;
        KeyBinding.setKeyPressed(((AccessorKeyBinding) JesusModule.mc.options.jumpKey).getBoundKey(), false);
    }

    @EventListener
    public void onBlockCollision(BlockCollisionEvent event) {
        BlockState state = event.getState();
        if (Modules.FLIGHT.isEnabled() || Modules.PACKET_FLY.isEnabled() || JesusModule.mc.player.isSpectator() || JesusModule.mc.player.isOnFire() || state.getFluidState().isEmpty()) {
            return;
        }
        if (this.modeConfig.getValue() != JesusMode.DOLPHIN && (state.getBlock() == Blocks.WATER | state.getFluidState().getFluid() == Fluids.WATER || state.getBlock() == Blocks.LAVA)) {
            event.cancel();
            event.setVoxelShape(VoxelShapes.cuboid(new Box(0.0, 0.0, 0.0, 1.0, 0.99, 1.0)));
            if (JesusModule.mc.player.getVehicle() != null) {
                event.setVoxelShape(VoxelShapes.cuboid(new Box(0.0, 0.0, 0.0, 1.0, 0.95f, 1.0)));
            } else if (this.modeConfig.getValue() == JesusMode.TRAMPOLINE) {
                event.setVoxelShape(VoxelShapes.cuboid(new Box(0.0, 0.0, 0.0, 1.0, 0.96, 1.0)));
            }
        }
    }

    @EventListener
    public void onPlayerJump(PlayerJumpEvent event) {
        if (!this.isInFluid() && this.isOnFluid()) {
            event.cancel();
        }
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (event.getStage() == EventStage.PRE) {
            if (Modules.FLIGHT.isEnabled() || Modules.PACKET_FLY.isEnabled()) {
                return;
            }
            if (this.modeConfig.getValue() == JesusMode.SOLID) {
                if (this.isInFluid() || JesusModule.mc.player.fallDistance > 3.0f || JesusModule.mc.player.isSneaking()) {
                    // empty if block
                }
                if (!JesusModule.mc.options.sneakKey.isPressed() && !JesusModule.mc.options.jumpKey.isPressed()) {
                    if (this.isInFluid()) {
                        this.floatTimer = 0;
                        MovementUtil.setMotionY(0.11);
                        return;
                    }
                    if (this.floatTimer == 0) {
                        MovementUtil.setMotionY(0.3);
                    } else if (this.floatTimer == 1) {
                        MovementUtil.setMotionY(0.0);
                    }
                    ++this.floatTimer;
                }
            } else if (this.modeConfig.getValue() == JesusMode.DOLPHIN && this.isInFluid() && !JesusModule.mc.options.sneakKey.isPressed() && !JesusModule.mc.options.jumpKey.isPressed()) {
                KeyBinding.setKeyPressed(((AccessorKeyBinding) JesusModule.mc.options.jumpKey).getBoundKey(), true);
            }
        }
    }

    @EventListener
    public void onPlayerUpdate(UpdateWalkingEvent event) {
        if (Modules.FLIGHT.isEnabled() || Modules.PACKET_FLY.isEnabled()) {
            return;
        }
        if (event.getStage() == EventStage.PRE && this.modeConfig.getValue() == JesusMode.TRAMPOLINE) {
            boolean inFluid;
            boolean bl = inFluid = this.getFluidBlockInBB(JesusModule.mc.player.getBoundingBox()) != null;
            if (inFluid && !JesusModule.mc.player.isSneaking()) {
                JesusModule.mc.player.setOnGround(false);
            }
            Block block = JesusModule.mc.world.getBlockState(new BlockPos((int)Math.floor(JesusModule.mc.player.getX()), (int)Math.floor(JesusModule.mc.player.getY()), (int)Math.floor(JesusModule.mc.player.getZ()))).getBlock();
            if (this.fluidState && !JesusModule.mc.player.getAbilities().flying && !JesusModule.mc.player.isTouchingWater()) {
                if (JesusModule.mc.player.getVelocity().y < -0.3 || JesusModule.mc.player.isOnGround() || JesusModule.mc.player.isHoldingOntoLadder()) {
                    this.fluidState = false;
                    return;
                }
                MovementUtil.setMotionY(JesusModule.mc.player.getVelocity().y / (double)0.98f + 0.08);
                MovementUtil.setMotionY(JesusModule.mc.player.getVelocity().y - 0.03120000000005);
            }
            if (this.isInFluid()) {
                MovementUtil.setMotionY(0.1);
                this.fluidState = false;
                return;
            }
            if (!this.isInFluid() && block instanceof FluidBlock && JesusModule.mc.player.getVelocity().y < 0.2) {
                MovementUtil.setMotionY(this.strictConfig.getValue() ? 0.184 : 0.5);
                this.fluidState = true;
            }
        }
    }

    @EventListener
    public void onPacketOutbound(PacketEvent.Send event) {
        PlayerMoveC2SPacket packet;
        if (event.isClientPacket() || JesusModule.mc.player == null || mc.getNetworkHandler() == null || JesusModule.mc.player.age <= 20 || Modules.FLIGHT.isEnabled() || Modules.PACKET_FLY.isEnabled()) {
            return;
        }
        Packet<?> packet2 = event.getPacket();
        if (packet2 instanceof PlayerMoveC2SPacket && (packet = (PlayerMoveC2SPacket) packet2).changesPosition() && this.modeConfig.getValue() == JesusMode.SOLID && !this.isInFluid() && this.isOnFluid() && JesusModule.mc.player.fallDistance <= 3.0f) {
            double y = packet.getY(JesusModule.mc.player.getY());
            if (!this.strictConfig.getValue()) {
                this.floatOffset = JesusModule.mc.player.age % 2 == 0 ? 0.0 : 0.05;
            }
            ((AccessorPlayerMoveC2SPacket) packet).hookSetY(y - this.floatOffset);
            if (this.strictConfig.getValue()) {
                this.floatOffset += 0.12;
                if (this.floatOffset > 0.4) {
                    this.floatOffset = 0.2;
                }
            }
        }
    }

    public boolean isInFluid() {
        return JesusModule.mc.player.isTouchingWater() || JesusModule.mc.player.isInLava();
    }

    public BlockState getFluidBlockInBB(Box box) {
        return this.getFluidBlockInBB(MathHelper.floor(box.minY - 0.2));
    }

    public BlockState getFluidBlockInBB(int minY) {
        for (int i = MathHelper.floor(JesusModule.mc.player.getBoundingBox().minX); i < MathHelper.ceil(JesusModule.mc.player.getBoundingBox().maxX); ++i) {
            for (int j = MathHelper.floor(JesusModule.mc.player.getBoundingBox().minZ); j < MathHelper.ceil(JesusModule.mc.player.getBoundingBox().maxZ); ++j) {
                BlockState state = JesusModule.mc.world.getBlockState(new BlockPos(i, minY, j));
                if (!(state.getBlock() instanceof FluidBlock)) continue;
                return state;
            }
        }
        return null;
    }

    public boolean isOnFluid() {
        if (JesusModule.mc.player.fallDistance >= 3.0f) {
            return false;
        }
        Box bb = JesusModule.mc.player.getVehicle() != null ? JesusModule.mc.player.getVehicle().getBoundingBox().contract(0.0, 0.0, 0.0).offset(0.0, -0.05f, 0.0) : JesusModule.mc.player.getBoundingBox().contract(0.0, 0.0, 0.0).offset(0.0, -0.05f, 0.0);
        boolean onLiquid = false;
        int y = (int)bb.minY;
        for (int x = MathHelper.floor(bb.minX); x < MathHelper.floor(bb.maxX + 1.0); ++x) {
            for (int z = MathHelper.floor(bb.minZ); z < MathHelper.floor(bb.maxZ + 1.0); ++z) {
                Block block = JesusModule.mc.world.getBlockState(new BlockPos(x, y, z)).getBlock();
                if (block == Blocks.AIR) continue;
                if (!(block instanceof FluidBlock)) {
                    return false;
                }
                onLiquid = true;
            }
        }
        return onLiquid;
    }

    public enum JesusMode {
        SOLID,
        DOLPHIN,
        TRAMPOLINE

    }
}
